import javax.servlet.Servlet;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.StopWatch;
import cn.hutool.core.lang.Console;
import cn.hutool.http.server.filter.HttpFilter;

/**
 * @author 余勇
 * @date 2021年03月06日 20:01:00
 */
public class ServerApp {

	public static void init(Class<?>... classes) throws InstantiationException, IllegalAccessException,
		ClassNotFoundException, IOException, URISyntaxException {
		StopWatch stopWatch = new StopWatch();
		stopWatch.start("初始化服务器");
		if (classes.length == 0) {
			Console.error("初始化Servlet为空");
			return;
		}
		// 加載配置文件
		ServerConfigBuilder.builder();
		ServerConfigBuilder.flush(new ServerConfig(), ServerConfig.PREFIX);
		// 框架启动
		ServerService serverService = new ServerService();

		ServerThreadPool.TASK_POOL.execute(new Runnable() {
			@Override
			public void run() {
				try {
					initServerService(serverService, classes);
				} catch (Exception e) {
					Console.error("启动出错", e);
				}
			}
		});
		try {
			serverService.openPort(ServerConfig.port, ServerConfig.sessionTimeout);
		} catch (Exception e) {
			Console.error("启动失败", e);
		}
	}

	private static void initServerService(ServerService miniCatService, Class<?>... classes) throws Exception {
		for (Class<?> clazz : classes) {
			if (!HttpServlet.class.isAssignableFrom(clazz) && !HttpFilter.class.isAssignableFrom(clazz)) {
				continue;
			}
			WebServlet webServlet = clazz.getAnnotation(WebServlet.class);
			if (Objects.nonNull(webServlet) && webServlet.value().length > 0) {
				HttpServlet servlet = (HttpServlet) clazz.getDeclaredConstructor().newInstance();
				Console.log("注册Servlet>>" + clazz.getName() + ">>" + webServlet.value()[0]);
				for (String mapping : webServlet.value()) {
					ServletContainer.putServlet(mapping, servlet);
				}
				// 启动core容器
				initMiniCatHttpPart(servlet);
				continue;
			}
			WebFilter webFilter = clazz.getAnnotation(WebFilter.class);
			if (Objects.nonNull(webFilter) && webFilter.value().length > 0) {
				HttpFilter filter = (HttpFilter) clazz.getDeclaredConstructor().newInstance();
				filter.setMapping(filterFlag.value());
				LogUtil.log.info("注册Filter>>" + clazz.getName() + ">>" + filterFlag.value());
				FilterContainer.pushFilter(filter);
				initMiniCatHttpPart(filter);
			}
		}
		Integer serverOpen = queue.take();
		if (serverOpen == 0) {
			return;
		}
		LogUtil.log
			.info("MiniCat:" + MiniCatConfig.port + "启动完成,耗时>>" + (System.currentTimeMillis() - startTime) + "ms");
		// 处理请求
		miniCatService.doService();
	}


}
