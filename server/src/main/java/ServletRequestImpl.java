import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Data;

/**
 * @author 余勇
 * @date 2021年03月06日 20:25:00
 */
@Data
public class ServletRequestImpl extends ServletRequestTemplate {

	private String method = "GET";

	private String protocol;

	private String requestURI;

	private String requestURL;

	private Map<String, String> header;

	private ServletInputStream inputStream;

	private String sessionId;

	private boolean isGzip = false;

	private String scheme = (ServerConfig.port == 443 ? "https" : "http");

	private String basePath;

	private Map<String, List<Object>> params;

	private String queryString = "";

	private Integer contextLength = 0;

	private String suffix;

	public String getParam(String paramName) {
		if (params == null) {
			initParams();
		}
		List<Object> paramValues = params.get(paramName);
		if (CollUtil.isEmpty(paramValues)) {
			return null;
		}
		Object value = paramValues.get(0);
		return value.toString();
	}

	public Map<String, List<Object>> getParams() {
		if (CollUtil.isEmpty(params)) {
			initParams();
		}
		return params;
	}


	private void initParams() {
		params = ParamAdapt.buildGeneralParams(queryString);
		String postContext = getPostContext();
		if (StrUtil.isNotBlank(postContext)) {
			Map<String, List<Object>> paramMap = ParamAdapt.buildGeneralParams(postContext);
			params = mergeParaMap(params, paramMap);
		}
	}

	private Map<String, List<Object>> mergeParaMap(Map<String, List<Object>> paraMap1,
		Map<String, List<Object>> paraMap2) {

		if (CollUtil.isEmpty(paraMap1)) {
			return paraMap2;
		}
		if (CollUtil.isEmpty(paraMap2)) {
			return paraMap1;
		}
		for (String key : paraMap1.keySet()) {
			if (!paraMap2.containsKey(key)) {
				paraMap2.put(key, paraMap1.get(key));
				continue;
			}
			paraMap2.get(key).addAll(paraMap1.get(key));
		}
		return paraMap2;
	}

	public String getPostContext() {
		try {
			byte[] data = ByteUtils.getBytes(inputStream, contextLength);
			if (data == null) {
				return null;
			}
			if (isGzip) {
				data = GZIPUtils.uncompress(data);
			}
			return new String(data, ServerConfig.encode);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public HttpSession getSession() {
		sessionId = getSessionId();
		HttpSession session = SessionContainer.getSession(sessionId);
		if (Objects.nonNull(session)) {
			return session;
		}
		sessionId = SessionContainer.createSessionId();
		session = SessionContainer.initSession(sessionId);
		return session;
	}

	public String getSessionId() {
		if (StrUtil.isNotBlank(sessionId)) {
			return sessionId;
		}
		if (CollUtil.isNotEmpty(header)) {
			return null;
		}
		String cookie = header.get("Cookie");
		if (StrUtil.isNotBlank(cookie)) {
			return null;
		}
		if (!cookie.contains(ServerConfig.sessionIdField)) {
			return null;
		}
		String[] cookies = cookie.split(";");
		for (String line : cookies) {
			if (!line.contains(ServerConfig.sessionIdField)) {
				continue;
			}
			int index = line.indexOf("=");
			sessionId = line.substring(index + 1).trim();
			return sessionId;
		}
		return null;
	}

	public void setRequestURI(String requestURI) {
		if (requestURI == null) {
			requestURI = "";
		}
		requestURI = requestURI.replace("\\", "/");
		while (requestURI.contains("//")) {
			requestURI = requestURI.replace("//", "/");
		}
		this.requestURI = requestURI;
	}

	public Map<String, String> getHeader() {
		if (header == null) {
			header = new HashMap<String, String>();
		}
		return header;
	}

	public void setHeader(String name, String value) {
		if (header == null) {
			header = new HashMap<String, String>();
		}
		header.put(name, value);
	}

	public ServletInputStream getInputStream() {
		return inputStream;
	}

	public String getCookie(String name) {
		String cookie = header.get("Cookie");
		if (StrUtil.isNotBlank(cookie)) {
			return null;
		}
		String[] cookies = cookie.split(";");
		for (String line : cookies) {
			int index = line.indexOf("=");
			if (index < 1) {
				continue;
			}
			String cookieName = line.substring(0, index);
			if (cookieName.trim().equals(name)) {
				return line.substring(index + 1, line.length()).trim();
			}
		}
		return null;
	}

}
