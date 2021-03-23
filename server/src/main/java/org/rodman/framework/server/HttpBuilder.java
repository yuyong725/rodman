package org.rodman.framework.server;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Objects;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.extra.compress.CompressUtil;
import cn.hutool.extra.servlet.ServletUtil;
import lombok.Data;

/**
 * @author 余勇
 * @date 2021年03月06日 20:21:00
 */
@Data
public class HttpBuilder {

	private Socket socket;

	protected RequestFacade request;

	protected ResponseFacade response;

	protected static final String splitFlag = "\r\n\r\n";

	public HttpBuilder(Socket socket) {
		if (socket == null) {
			throw new RuntimeException("未实例化Socket");
		}
		this.socket = socket;
	}

	public void buildResponse() throws IOException {
		if (response == null) {
			response = new ResponseFacade();
		}
		buildResponse(response.getStatus(), response.getOutputStream().toByteArray());
	}

	public void buildResponse(int httpCode, String msg) throws IOException {
		if (response == null) {
			response = new ResponseFacade();
		}
		buildResponse(httpCode, msg.getBytes());
	}

	public void buildResponse(int httpCode, byte[] data) throws IOException {
		if (response == null) {
			response = new ResponseFacade();
		}
		response.setStatus(httpCode);
		buildResponseHeader();
		// 压缩数据
		data = ZipUtil.gzip(data);

		int contextLength = 0;
		if (Objects.nonNull(data)) {
			contextLength = data.length;
		}
		response.setHeader("Content-Length", Integer.toString(contextLength));
		StringBuilder responseHeader = new StringBuilder("HTTP/1.1 ").append(httpCode).append("\r\n");
		for (String key : response.getHeaderNames()) {
			responseHeader.append(key).append(": ").append(response.getHeader(key)).append("\r\n");
		}
		responseHeader.append("\r\n");
		response.getOutputStream().reset();
		response.getOutputStream().write(responseHeader.toString().getBytes());
		if (Objects.nonNull(data) && data.length > 0) {
			response.getOutputStream().write(data);
		}
	}

	public void buildResponseHeader() throws IOException {
		if (response == null) {
			throw new ServerException("Response尚未初始化");
		}
		response.setHeader("Connection", "close");
		response.setHeader("Server", "server/1.0 By rodman");
		response.setHeader("Content-Type", "application/json;charset=UTF-8");

		String cookie = MessageFormat.format("{0}={1}; path=/ ; HttpOnly", ServerConfig.sessionIdField,
			request.getSessionId());
		response.setHeader("Set-Cookie", cookie);
		response.setHeader("Content-Encoding", "gzip");
	}

	private void destroy() {
		try {
			if (response != null && response.getOutputStream() != null) {
				response.getOutputStream().close();
			}
			if (request != null && request.getInputStream() != null) {
				request.getInputStream().close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void builder() {
		try {
			buildRequest();
			if (!ServerConfig.method.contains(this.request.getMethod())) {
				buildResponse(400, "400 bad request");
			}
		} catch (Exception e) {
			e.printStackTrace();
			try {
				buildResponse(500, "error execution");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

	}

	public void invoke() {
		try {
			this.response = new ResponseFacade();
			ServerProcess.doService(this);
			buildResponse();
		} catch (Exception e) {
			e.printStackTrace();
			try {
				buildResponse(500, "error execution");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

	}

	public void flushAndClose() {
		try {
			flush();
		} catch (IOException e) {
		}catch (Exception e) {
			e.printStackTrace();
		} finally {
			destroy();
		}
	}

	protected void flush() throws IOException {
		byte[] data = response.getOutputStream().toByteArray();
		if (data.length == 0) {
			return;
		}
		if (!socket.isClosed()) {
			socket.getOutputStream().write(data);
			socket.getOutputStream().flush();
			socket.getOutputStream().close();
		}
	}

	public void buildRequest() {
		this.request = new RequestFacade();
		try {
			byte[] data = IoUtil.readBytes(socket.getInputStream(), ServerConfig.maxHeaderLength);
			if (Objects.isNull(data) || data.length == 0) {
				return;
			}

			// 数据读取完全
			boolean isReadEnd = data.length < ServerConfig.maxHeaderLength;

			String headerContext = new String(data);
			String bodyContext = null;
			// 切割出请求头和请求体
			if (headerContext.contains(splitFlag)) {
				bodyContext = headerContext.substring(headerContext.indexOf(splitFlag) + splitFlag.length());
				headerContext = headerContext.substring(0, headerContext.indexOf(splitFlag));
			}
			headerContext += splitFlag;
			String[] headers = headerContext.split("\r\n");
			// 最少有协议体和方法
			if (headers.length < 2) {
				throw new ServerException("错误的请求报文");
			}
			String line = headers[0];
			while (line.contains("  ")) {
				line = line.replace("  ", " ");
			}
			String[] vanguards = line.trim().split(" ");
			// GET /api/v3/internal/my_resources HTTP/1.1
			if (vanguards.length != 3) {
				throw new ServerException("错误的请求报文");
			}
			request.setMethod(vanguards[0].toUpperCase());
			String requestURI = vanguards[1];
			// 拼接请求
			if (requestURI.contains("?")) {
				int index = requestURI.indexOf("?");
				if (index < requestURI.length() - 1) {
					request.setQueryString(requestURI.substring(index + 1));
				}
				requestURI = requestURI.substring(0, index);
			}
			request.setRequestURI(requestURI);
			request.setProtocol(vanguards[2]);
			for (int i = 1; i < headers.length; i++) {
				String header = headers[i];
				int index = header.indexOf(":");
				if (index < 1) {
					throw new ServerException("错误的请求头部:" + line);
				}
				String name = header.substring(0, index).trim();
				String value = header.substring(index + 1).trim();
				if (StrUtil.isBlank(name) || StrUtil.isBlank(value)) {
					continue;
				}
				request.setHeader(name, value);
				if (name.equalsIgnoreCase("Host")) {
					request.setBasePath(request.getScheme() + "://" + value);
					if (requestURI.startsWith(request.getBasePath())) {
						requestURI = requestURI.substring(request.getBasePath().length());
						request.setRequestURI(requestURI);
					}
				}
				if (name.equalsIgnoreCase("Content-Length")) {
					request.setContentLength(Integer.valueOf(value));
				}
				if (name.equalsIgnoreCase("Content-Type")) {
					request.setContentType(value);
				}
			}
			try {
				// 没有指定请求体长度 或者 数据读取完全了
				if (isReadEnd || request.getContentLength() < 1) {
					if (Objects.nonNull(bodyContext) && !bodyContext.isEmpty()) {
						request.setInputStream(new ByteArrayInputStream(bodyContext.getBytes()));
					}
					return;
				}

				// 指定请求体长度，并且 数据没有读完(只读取了服务器接受的最大长度)
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				try {
					byte[] bodyData = bodyContext.getBytes();
					byteArrayOutputStream.write(bodyData);
					// 读取超出的部分
					int remainLength = request.getContentLength() - bodyData.length;
					byte[] remainData = IoUtil.readBytes(socket.getInputStream(), remainLength);
					byteArrayOutputStream.write(remainData);
					request.setInputStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
				} finally {
					byteArrayOutputStream.close();
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
