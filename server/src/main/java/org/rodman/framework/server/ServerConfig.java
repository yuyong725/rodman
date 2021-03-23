package org.rodman.framework.server;

/**
 * @author 余勇
 * @date 2021年03月06日 19:40:00
 */
public class ServerConfig {

	public static final String PREFIX = "rodman.server";
	public static final String sessionIdField = "JSESSIONID";

	/**
	 * 支持的Method
	 */
	public static final String method = "POST,GET,PUT,OPTIONS";

	/**
	 * http 线程数量
	 */
	public static Integer httpThread = 500;

	/**
	 * 任务线程数量
	 */
	public static Integer taskThread = 20;

	/**
	 * 最大Head长度, 默认8k
	 */
	public static Integer maxHeaderLength = 8 * 1024;

	/**
	 * 端口，默认 3728
	 */
	public static Integer port = 3728;

	/**
	 * session 超时时间，默认半个小时
	 */
	public static Integer sessionTimeout = 30 * 60 * 1000;

	/**
	 * schema
	 */
	public static String schema = "http";
}
