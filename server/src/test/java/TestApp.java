import java.io.IOException;

import org.rodman.framework.server.ServerApp;

/**
 * @author 余勇
 * @date 2021年03月23日 13:50:00
 */
public class TestApp {

	public static void main(String[] args) throws IOException, IllegalAccessException {
		ServerApp.init(TestServlet.class);
	}

}
