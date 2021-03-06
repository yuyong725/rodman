/**
 * @author 余勇
 * @date 2021年03月06日 19:40:00
 */
public class ServerConfig extends BaseConfig {

	public static final String PREFIX = "rodman.server";

	/**
	 * http 线程数量
	 */
	public static Integer httpThread;
	public static Integer maxHeaderLength;
	public static String encode;
	public static Integer port;
	public static String sessionIdField;
	public static Integer sessionTimeout;
}
