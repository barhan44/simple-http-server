package io.barhan.http_server.impl;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.barhan.http_server.HttpServer;
import io.barhan.http_server.config.HttpServerConfig;
import io.barhan.http_server.exception.HttpServerException;

class HttpServerImpl implements HttpServer {
	private static final Logger LOGGER = LoggerFactory.getLogger(HttpServerImpl.class);
	private final HttpServerConfig httpServerConfig;
	private final ServerSocket serverSocket;
	private final ExecutorService executorService;
	private final Thread mainServerThread;
	private volatile boolean isServerStopped;

	protected HttpServerImpl(HttpServerConfig httpServerConfig) {
		this.httpServerConfig = httpServerConfig;
		this.executorService = this.createExecutorService();
		this.mainServerThread = this.createMainServerThread(this.createServerRunnable());
		this.serverSocket = this.createServerSocket();
		this.isServerStopped = false;
	}

	private ServerSocket createServerSocket() {
		try {
			ServerSocket serverSocket = new ServerSocket(this.httpServerConfig.getServerInfo().getPort());
			serverSocket.setReuseAddress(true);
			return serverSocket;
		} catch (IOException e) {
			throw new HttpServerException(
					"Creating server socket with port=" + this.httpServerConfig.getServerInfo().getPort() + " failed.",
					e);
		}
	}

	private ExecutorService createExecutorService() {
		ThreadFactory threadFactory = this.httpServerConfig.getWorkerThreadFactory();
		int threadCount = this.httpServerConfig.getServerInfo().getThreadCount();
		if (threadCount > 0) {
			return Executors.newFixedThreadPool(threadCount, threadFactory);
		} else {
			return Executors.newCachedThreadPool(threadFactory);
		}
	}

	private Thread createMainServerThread(Runnable r) {
		Thread thread = new Thread(r, "Main Server Thread");
		thread.setPriority(Thread.MAX_PRIORITY);
		thread.setDaemon(false);
		return thread;
	}

	private Runnable createServerRunnable() {
		return new Runnable() {

			@Override
			public void run() {
				while (!mainServerThread.isInterrupted()) {
					try {
						Socket clientSocket = serverSocket.accept();
						executorService.submit(httpServerConfig.buildNewHttpClientSocketHandler(clientSocket));
					} catch (IOException e) {
						if (!serverSocket.isClosed()) {
							LOGGER.error("Cannot accept client socket: " + e.getMessage(), e);
						}
						destroyHttpServer();
						break;
					}
				}
				System.exit(0);
			}
		};
	}

	@Override
	public void start() {
		if (this.mainServerThread.getState() != Thread.State.NEW) {
			throw new HttpServerException(
					"Current sever already started or stopped! Create a new http server instance");
		}
		Runtime.getRuntime().addShutdownHook(this.getShutdownHook());
		this.mainServerThread.start();
		LOGGER.info("Server has been started: " + this.httpServerConfig.getServerInfo());

	}

	@Override
	public void stop() {
		LOGGER.info("Stop server command!");
		this.mainServerThread.interrupt();
		try {
			this.serverSocket.close();
		} catch (IOException e) {
			LOGGER.warn("Closing server socket failed: " + e.getMessage(), e);
		}

	}

	private Thread getShutdownHook() {
		return new Thread(new Runnable() {

			@Override
			public void run() {
				if (!isServerStopped) {
					destroyHttpServer();
				}

			}
		}, "ShutdownHook");
	}

	private void destroyHttpServer() {
		this.executorService.shutdownNow();
		LOGGER.info("Server stopped");
		this.isServerStopped = true;
	}
}
