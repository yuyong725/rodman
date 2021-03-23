import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import cn.hutool.json.JSONUtil;

/**
 * @author 余勇
 * @date 2021年03月23日 13:38:00
 */
@WebServlet("/test")
public class TestServlet extends HttpServlet {

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		Map<String, String> ret = new HashMap<>();
		ret.put("name", "张三");
		ret.put("age", "18");
		resp.getOutputStream().write(JSONUtil.toJsonStr(ret).getBytes());
	}
}
