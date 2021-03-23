package org.rodman.framework.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import cn.hutool.core.lang.Console;

/**
 * @author 余勇
 * @date 2021年03月06日 20:16:00
 */
public class ServerService {

	private ServerSocket server;

	public void openPort(Integer port) throws IOException {
		server = new ServerSocket(port);
	}

	public void doService() throws IOException {
		while (true) {
			Socket socket = server.accept();
			doSocket(socket);
		}
	}

	private void doSocket(final Socket socket) {
		ServerThreadPool.HTTP_POOL.execute(() -> {
			try {
				HttpBuilder builder = new HttpBuilder(socket);
				builder.builder();
				builder.flushAndClose();
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

}
