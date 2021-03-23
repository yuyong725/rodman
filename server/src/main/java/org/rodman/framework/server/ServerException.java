package org.rodman.framework.server;

/**
 * @author 余勇
 * @date 2021年03月06日 19:02:00
 */
public class ServerException extends RuntimeException {

	public ServerException() {
		super();
	}

	public ServerException(String msg) {
		super(msg);
	}

	public ServerException(String msg, Exception e) {
		super(msg, e);
	}
}
