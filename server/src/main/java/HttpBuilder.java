import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.text.MessageFormat;

/**
 * @author 余勇
 * @date 2021年03月06日 20:21:00
 */
public class HttpBuilder {

	private Socket socket;

	protected MinicatServletRequestImpl request;

	protected MinicatServletResponseImpl response;

	protected static final String splitFlag = "\r\n\r\n";


	public HttpBuilder(Socket socket) {
		if (socket == null) {
			throw new NotConnectionException("未实例化Socket");
		}
		this.socket = socket;
	}

	public MinicatServletRequestImpl getRequest() {
		return request;
	}

	public void setRequest(MinicatServletRequestImpl request) {
		this.request = request;
	}

	public MinicatServletResponseImpl getResponse() {
		return response;
	}

	public void setResponse(MinicatServletResponseImpl response) {
		this.response = response;
	}

	public void buildResponse() throws IOException {
		if (response == null) {
			response = new MinicatServletResponseImpl();
		}
		buildResponse(response.getHttpCode(), response.getOutputStream().toByteArray());
	}

	public void buildResponse(byte[] data) throws IOException {
		if (response == null) {
			response = new MinicatServletResponseImpl();
		}
		buildResponse(response.getHttpCode(), data);
	}

	public void buildResponse(int httpCode, String msg) throws IOException {
		if (response == null) {
			response = new MinicatServletResponseImpl();
		}
		buildResponse(httpCode, msg.getBytes(MiniCatConfig.encode));
	}

	public void buildResponse(int httpCode, byte[] data) throws IOException {
		if (response == null) {
			response = new MinicatServletResponseImpl();
		}
		buildResponseHeader();
		if (MiniCatConfig.openGzip) {
			// 压缩数据
			data = GZIPUtils.compress(data);
		}
		Integer contextLength = 0;
		if (data != null) {
			contextLength = data.length;
		}
		response.setHeader("Content-Length", contextLength.toString());
		StringBuilder responseHeader = new StringBuilder("HTTP/1.1 ").append(httpCode).append(" ");
		if (httpCode == 302) {
			responseHeader.append("Found");
		}
		responseHeader.append("\r\n");
		for (String key : response.getHeaders().keySet()) {
			for (String header : response.getHeader(key)) {
				responseHeader.append(key).append(": ").append(header).append("\r\n");
			}
		}
		responseHeader.append("\r\n");
		response.getOutputStream().reset();
		response.getOutputStream().write(responseHeader.toString().getBytes(MiniCatConfig.encode));
		if (!CommonUtil.isNullOrEmpty(data)) {
			response.getOutputStream().write(data);
		}
	}

	public void buildResponseHeader() throws IOException {
		if (response == null) {
			throw new ResponseNotInitException("Response尚未初始化");
		}
		response.setHeader("Connection", "close");
		response.setHeader("Server", "MiniCat/1.0 By Coody");
		if (!response.containsHeader("Content-Type")) {
			switch (request.getSuffix()) {
				case "js":
					response.setHeader("Content-Type", "application/x-javascript;charset=" + MiniCatConfig.encode);
					break;
				case "css":
					response.setHeader("Content-Type", "text/css;charset=" + MiniCatConfig.encode);
					break;
				default:
					response.setHeader("Content-Type", "text/html;charset=" + MiniCatConfig.encode);
					break;
			}
		}
		if (MiniCatConfig.openGzip) {
			response.setHeader("Content-Encoding", "gzip");
		}
		if (request != null && !request.isSessionCread()) {
			String cookie = MessageFormat.format("{0}={1}; path=/ ; HttpOnly", MiniCatConfig.sessionIdField,
				request.getSessionId());
			response.setHeader("Set-Cookie", cookie);
		}
	}

	public abstract void buildRequestHeader();

	private void destroy() {
		if (response != null && response.getOutputStream() != null) {
			try {
				response.getOutputStream().close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (request != null && request.getInputStream() != null) {
			try {
				request.getInputStream().close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void builder() {
		try {
			buildRequest();
			if (!MiniCatConfig.method.contains(this.request.getMethod())) {
				buildResponse(400, "400 bad request");
				return;
			}
			buildRequestHeader();
			request.setSuffix("");
			if (request.getRequestURI() != null && request.getRequestURI().contains(".")) {
				request.setSuffix(request.getRequestURI()
					.substring(request.getRequestURI().lastIndexOf(".") + 1, request.getRequestURI().length())
					.toLowerCase());
			}
		} catch (BadRequestException e) {
			e.printStackTrace();
			try {
				buildResponse(400, "400 bad request");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
			try {
				buildResponse(500, "error execution");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} catch (PageNotFoundException e) {
			try {
				buildResponse(404, "page not found!");
			} catch (Exception e1) {
				e1.printStackTrace();
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
			this.response = new MinicatServletResponseImpl();
			MinicatProcess.doService(this);
			buildResponse();
		} catch (BadRequestException e) {
			e.printStackTrace();
			try {
				buildResponse(400, "400 bad request");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
			try {
				buildResponse(500, "error execution");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		} catch (PageNotFoundException e) {
			try {
				buildResponse(404, "page not found!");
			} catch (Exception e1) {
				e1.printStackTrace();
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

	protected void buildRequest() throws Exception {
		this.request = new MinicatServletRequestImpl();
	}

	protected void flush() throws IOException {
		byte[] data = response.getOutputStream().toByteArray();
		if (CommonUtil.isNullOrEmpty(data)) {
			return;
		}
		if (!socket.isClosed()) {
			socket.getOutputStream().write(data);
			socket.getOutputStream().flush();
			socket.getOutputStream().close();
		}
	}

	public void buildRequestHeader() {
		if (request == null) {
			throw new RequestNotInitException("Request尚未初始化");
		}
		try {
			byte[] data = ByteUtils.getBytes(socket.getInputStream(), MiniCatConfig.maxHeaderLength);
			if (CommonUtil.isNullOrEmpty(data)) {
				return;
			}
			boolean isReadEnd = data.length < MiniCatConfig.maxHeaderLength;
			String headerContext = new String(data, "iso-8859-1");
			String bodyContext = null;
			if (headerContext.contains(splitFlag)) {
				bodyContext = headerContext.substring(headerContext.indexOf(splitFlag) + splitFlag.length());
				headerContext = headerContext.substring(0, headerContext.indexOf(splitFlag));
			}
			headerContext += splitFlag;
			String[] headers = headerContext.split("\r\n");
			if (headers.length < 2) {
				throw new BadRequestException("错误的请求报文");
			}
			String line = headers[0];
			while (line.contains("  ")) {
				line = line.replace("  ", " ");
			}
			String[] vanguards = line.trim().split(" ");
			if (vanguards.length != 3) {
				throw new BadRequestException("错误的请求报文");
			}
			request.setMethod(vanguards[0].toUpperCase());
			String requestURI = vanguards[1];
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
					throw new BadRequestException("错误的请求头部:" + line);
				}
				String name = header.substring(0, index).trim();
				String value = header.substring(index + 1).trim();
				if (CommonUtil.hasNullOrEmpty(name, value)) {
					continue;
				}
				request.setHeader(name, value);
				if (name.equalsIgnoreCase("Content-Encoding")) {
					if (value.contains("gzip")) {
						request.setGzip(true);
					}
				}
				if (name.equalsIgnoreCase("Cookie")) {
					if (value != null && value.contains(MiniCatConfig.sessionIdField)) {
						request.setSessionCread(true);
					}
				}
				if (name.equalsIgnoreCase("Host")) {
					request.setBasePath(request.getScheme() + "://" + value);
					if (requestURI.startsWith(request.getBasePath())) {
						requestURI = requestURI.substring(request.getBasePath().length());
						request.setRequestURI(requestURI);
					}
				}
				if (name.equalsIgnoreCase("Content-Length")) {
					request.setContextLength(Integer.valueOf(value));
				}
			}
			try {
				if (isReadEnd || request.getContextLength() < 1) {
					if (!CommonUtil.isNullOrEmpty(bodyContext)) {
						request.setInputStream(new ByteArrayInputStream(bodyContext.getBytes("iso-8859-1")));
					}
					return;
				}
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				try {
					byte[] bodyData = bodyContext.getBytes("iso-8859-1");
					byteArrayOutputStream.write(bodyData);
					int remainLength = request.getContextLength() - bodyData.length;
					byte[] remainData = ByteUtils.getBytes(socket.getInputStream(), remainLength);
					byteArrayOutputStream.write(remainData);
					request.setInputStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
				} finally {
					byteArrayOutputStream.close();
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (MiniCatException e) {
			throw e;
		} catch (IOException e) {
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
