package io.barhan.http_server.impl;

import java.util.concurrent.ThreadFactory;

class ThreadFactoryImpl implements ThreadFactory {
	private String name;
	private int count;

	public ThreadFactoryImpl() {
		this.count = 1;
		this.name = "executor-thread-";
	}

	@Override
	public Thread newThread(Runnable r) {
		Thread thread = new Thread(r, name + (count++));
		thread.setDaemon(false);
		thread.setPriority(8);
		return thread;
	}

}
