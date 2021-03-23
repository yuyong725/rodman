package org.rodman.framework.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.digest.DigestUtil;

/**
 * @author 余勇
 * @date 2021年03月06日 21:41:00
 */
public class SessionContainer {

	private static final Map<String, HttpSessionImpl> SYSTEM_SESSION_CONTAINER = new ConcurrentHashMap<String,
			HttpSessionImpl>();


	static{
		ServerThreadPool.TASK_POOL.execute(SessionContainer::sessionGuard);
	}

	private static void sessionGuard(){
		while(true){
			try {
				if(CollUtil.isEmpty(SYSTEM_SESSION_CONTAINER)){
					return;
				}
				List<String> willCleanSessionIds=new ArrayList<String>();
				for(String key : SYSTEM_SESSION_CONTAINER.keySet()){
					HttpSessionImpl session=SYSTEM_SESSION_CONTAINER.get(key);
					if(System.currentTimeMillis() - session.getActiveTime().getTime() > ServerConfig.sessionTimeout){
						willCleanSessionIds.add(key);
					}
				}
				for(String key : willCleanSessionIds){
					SYSTEM_SESSION_CONTAINER.remove(key);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}finally{
				try {
					TimeUnit.SECONDS.sleep(1L);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static boolean containsSession(String sessionId) {
		return SYSTEM_SESSION_CONTAINER.containsKey(sessionId);
	}

	public static HttpSessionImpl getSession(String sessionId) {
		HttpSessionImpl session = SYSTEM_SESSION_CONTAINER.get(sessionId);
		if(Objects.isNull(session)){
			return null;
		}
		session.setActiveTime(new Date());
		return session;
	}

	public static HttpSessionImpl setSession(String sessionId, HttpSessionImpl session) {
		return SYSTEM_SESSION_CONTAINER.put(sessionId, session);
	}

	public static HttpSessionImpl initSession(String sessionId) {
		if (SYSTEM_SESSION_CONTAINER.containsKey(sessionId)) {
			return SYSTEM_SESSION_CONTAINER.get(sessionId);
		}
		HttpSessionImpl session = new HttpSessionImpl();
		SYSTEM_SESSION_CONTAINER.put(sessionId, session);
		return session;
	}

	public static String createSessionId() {
		String key = IdUtil.objectId();
		return DigestUtil.md5Hex(key);
	}

}
