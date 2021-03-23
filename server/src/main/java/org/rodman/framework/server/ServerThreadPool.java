package org.rodman.framework.server;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author 余勇
 * @date 2021年03月06日 20:17:00
 */
public class ServerThreadPool {

	public static final ExecutorService HTTP_POOL =  new ThreadPoolExecutor(100, ServerConfig.httpThread,
		10, TimeUnit.SECONDS,
		new LinkedBlockingQueue<Runnable>());

	public static final ExecutorService TASK_POOL =  new ThreadPoolExecutor(10, ServerConfig.taskThread,
		10, TimeUnit.SECONDS,
		new LinkedBlockingQueue<Runnable>());

}
