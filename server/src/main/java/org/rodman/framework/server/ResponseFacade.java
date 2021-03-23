package org.rodman.framework.server;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import lombok.Data;

/**
 * @author 余勇
 * @date 2021年03月07日 14:08:00
 */
@Data
public class ResponseFacade implements HttpServletResponse {

	private final ServerOutputStream outputStream = new ServerOutputStream();

	private Locale locale;
	private int bufferSize;

	/**
	 * 编码
	 */
	private String characterEncoding = "UTF-8";

	/**
	 * 响应头
	 */
	private Map<String, String> headerMap = new LinkedHashMap<>();

	/**
	 * 响应码
	 */
	private int status = 200;
	private String statusMsg;

	/**
	 *
	 */

	@Override
	public void addCookie(Cookie cookie) {

	}

	@Override
	public boolean containsHeader(String name) {
		return headerMap.containsKey(name);
	}

	@Override
	public String encodeURL(String s) {
		return null;
	}

	@Override
	public String encodeRedirectURL(String s) {
		return null;
	}

	@Override
	public String encodeUrl(String s) {
		return null;
	}

	@Override
	public String encodeRedirectUrl(String s) {
		return null;
	}

	@Override
	public void sendError(int i, String s) throws IOException {

	}

	@Override
	public void sendError(int i) throws IOException {

	}

	@Override
	public void sendRedirect(String s) throws IOException {

	}

	@Override
	public void setDateHeader(String s, long l) {

	}

	@Override
	public void addDateHeader(String s, long l) {

	}

	@Override
	public void setHeader(String name, String value) {
		headerMap.put(name, value);
	}

	@Override
	public void addHeader(String name, String value) {
		addHeader(name, value);
	}

	@Override
	public void setIntHeader(String name, int value) {
		setHeader(name, String.valueOf(value));
	}

	@Override
	public void addIntHeader(String name, int value) {
		addHeader(name, String.valueOf(value));
	}

	@Override
	public void setStatus(int sc, String sm) {
		this.status = sc;
		this.statusMsg = sm;
	}

	@Override
	public String getHeader(String name) {
		return headerMap.get(name);
	}

	@Override
	public Collection<String> getHeaders(String name) {
		return Collections.singleton(headerMap.get(name));
	}

	@Override
	public Collection<String> getHeaderNames() {
		return headerMap.keySet();
	}

	@Override
	public String getContentType() {
		return getHeader("Content-Type");
	}


	@Override
	public PrintWriter getWriter() throws IOException {
		return null;
	}

	@Override
	public void setContentLength(int contentLength) {
		setIntHeader("Content-Length", contentLength);
	}

	@Override
	public void setContentLengthLong(long contentLength) {
		setIntHeader("Content-Length", (int) contentLength);
	}

	@Override
	public void setContentType(String contentType) {
		setHeader("Content-Type", contentType);
	}


	@Override
	public void flushBuffer() throws IOException {

	}

	@Override
	public void resetBuffer() {

	}

	@Override
	public boolean isCommitted() {
		return false;
	}

	@Override
	public void reset() {

	}

}
