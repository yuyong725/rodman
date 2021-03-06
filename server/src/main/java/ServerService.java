import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author 余勇
 * @date 2021年03月06日 20:16:00
 */
public class ServerService {

	private ServerSocket server;

	public void openPort(Integer port, Integer timeOut) throws IOException {
		server = new ServerSocket(port);
		server.setSoTimeout(timeOut);
	}

	public void doService() throws IOException {
		while (true) {
			try {
				Socket socket = server.accept();
				doSocket(socket);
			} finally {
			}
		}
	}

	private void doSocket(final Socket socket) {
		ServerThreadPool.HTTP_POOL.execute(new Runnable() {
			public void run() {
				try {
					HttpBuilder builder = new HttpBuilder(socket);
					builder.builder();
					builder.invoke();
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
			}
		});
	}

}
