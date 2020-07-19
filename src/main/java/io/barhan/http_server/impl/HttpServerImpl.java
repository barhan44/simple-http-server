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
    private volatile boolean stopRequest;

    protected HttpServerImpl(HttpServerConfig httpServerConfig) {
        this.httpServerConfig = httpServerConfig;
        this.executorService = this.createExecutorService();
        this.mainServerThread = this.createMainServerThread(this.createServerRunnable());
        this.serverSocket = this.createServerSocket();
        this.isServerStopped = false;
    }

    protected ServerSocket createServerSocket() {
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

    protected ExecutorService createExecutorService() {
        ThreadFactory threadFactory = this.httpServerConfig.getWorkerThreadFactory();
        int threadCount = this.httpServerConfig.getServerInfo().getThreadCount();
        if (threadCount > 0) {
            return Executors.newFixedThreadPool(threadCount, threadFactory);
        }
        return Executors.newCachedThreadPool(threadFactory);
    }

    protected Thread createMainServerThread(Runnable r) {
        Thread thread = new Thread(r, "Main Server Thread");
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.setDaemon(false);
        return thread;
    }

    protected Runnable createServerRunnable() {
        return () -> {
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
			if (stopRequest) {
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
        this.stopRequest = true;
        this.mainServerThread.interrupt();
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            LOGGER.warn("Closing server socket failed: " + e.getMessage(), e);
        }

    }

    protected Thread getShutdownHook() {
        return new Thread(() -> {
			if (!isServerStopped) {
				destroyHttpServer();
			}

		}, "ShutdownHook");
    }

    protected void destroyHttpServer() {
        this.executorService.shutdownNow();
        LOGGER.info("Server stopped");
        this.isServerStopped = true;
    }
}
