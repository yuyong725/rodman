package org.rodman.framework.server;

import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.util.Objects;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.lang.Console;

/**
 * @author 余勇
 * @date 2021年03月06日 20:01:00
 */
public class ServerApp {

	private static final StopWatch stopWatch = new StopWatch();

	public static void init(Class<?>... classes) {
		stopWatch.start();
		if (classes.length == 0) {
			Console.error("初始化Servlet为空");
			return;
		}

		try {
			// 加載配置文件
			ServerConfigBuilder.builder();
			ServerConfigBuilder.flush(new ServerConfig(), ServerConfig.PREFIX);

			// 框架启动
			ServerService serverService = new ServerService();
			serverService.openPort(ServerConfig.port);
			Console.log("监听端口>>", ServerConfig.port);

			initServerService(serverService, classes);
		} catch (Exception e) {
			Console.error("启动出错", e);
			e.printStackTrace();
		}

	}

	private static void initServerService(ServerService serverService, Class<?>... classes) throws Exception {
		for (Class<?> clazz : classes) {
			if (!HttpServlet.class.isAssignableFrom(clazz) && !HttpFilter.class.isAssignableFrom(clazz)) {
				continue;
			}
			WebServlet webServlet = clazz.getAnnotation(WebServlet.class);
			if (Objects.nonNull(webServlet) && webServlet.value().length > 0) {
				HttpServlet servlet = (HttpServlet) clazz.getDeclaredConstructor().newInstance();
				for (String mapping : webServlet.value()) {
					Console.log("注册Servlet>>" + clazz.getName() + ">>" + mapping);
					ServletContainer.putServlet(mapping, servlet);
				}
				// 调用servlet的初始化逻辑
				servlet.init();
				continue;
			}
			WebFilter webFilter = clazz.getAnnotation(WebFilter.class);
			if (Objects.nonNull(webFilter) && webFilter.value().length > 0) {
				HttpFilter filter = (HttpFilter) clazz.getDeclaredConstructor().newInstance();
				for (String mapping : webFilter.value()) {
					Console.log("注册Filter>>" + clazz.getName() + ">>" + mapping);
					FilterContainer.putServlet(mapping, filter);
				}
				filter.init();
			}
		}
		stopWatch.stop();
		Console.log("server:" + ServerConfig.port + "启动完成,耗时>>" + stopWatch.getLastTaskTimeMillis() + "ms");
		// 处理请求
		serverService.doService();
	}

}
