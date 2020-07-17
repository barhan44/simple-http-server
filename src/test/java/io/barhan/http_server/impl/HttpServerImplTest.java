package io.barhan.http_server.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import io.barhan.http_server.ServerInfo;
import io.barhan.http_server.config.HttpClientSocketHandler;
import io.barhan.http_server.config.HttpServerConfig;
import io.barhan.http_server.exception.HttpServerException;

public class HttpServerImplTest {
	private HttpServerImpl httpServer;

	private HttpServerConfig httpServerConfig;
	private ServerSocket serverSocket;
	private ExecutorService executorService;
	private Thread mainServerThread;

	@Before
	public void before() {
		this.httpServerConfig = mock(HttpServerConfig.class);
		this.serverSocket = mock(ServerSocket.class);
		this.executorService = mock(ExecutorService.class);
		this.mainServerThread = mock(Thread.class);
	}

	@Test
	public void testCreateMainThread() {
		this.httpServer = new HttpServerImpl(this.httpServerConfig) {
			@Override
			protected ExecutorService createExecutorService() {
				return executorService;
			}

			@Override
			protected ServerSocket createServerSocket() {
				return serverSocket;
			}

			@Override
			protected Runnable createServerRunnable() {
				return mock(Runnable.class);
			}
		};

		Thread thread = this.httpServer.createMainServerThread(mock(Runnable.class));
		assertEquals(Thread.MAX_PRIORITY, thread.getPriority());
		assertEquals("Main Server Thread", thread.getName());
		assertFalse(thread.isDaemon());
		assertFalse(thread.isAlive());
	}

	@Test
	public void testClientConnectionDispatcherSuccess() throws Exception {
		final Runnable[] run = new Runnable[1];
		this.httpServer = new HttpServerImpl(this.httpServerConfig) {
			@Override
			protected ExecutorService createExecutorService() {
				return executorService;
			}

			@Override
			protected Thread createMainServerThread(Runnable r) {
				return mainServerThread;
			}

			@Override
			protected ServerSocket createServerSocket() {
				return serverSocket;
			}

			@Override
			protected Runnable createServerRunnable() {
				Runnable r = super.createServerRunnable();
				run[0] = r;
				return r;
			}
		};

		when(this.mainServerThread.isInterrupted()).thenReturn(false).thenReturn(true);
		Socket clientSocket = mock(Socket.class);
		when(this.serverSocket.accept()).thenReturn(clientSocket);
		HttpClientSocketHandler httpClientSocketHandler = mock(HttpClientSocketHandler.class);
		when(this.httpServerConfig.buildNewHttpClientSocketHandler(clientSocket)).thenReturn(httpClientSocketHandler);

		run[0].run();

		verify(this.mainServerThread, times(2)).isInterrupted();
		verify(this.serverSocket).accept();
		verify(this.httpServerConfig).buildNewHttpClientSocketHandler(clientSocket);
		verify(this.executorService).submit(httpClientSocketHandler);
	}

	@Test
	public void testClientConnectionDispatcherFailed() throws Exception {
		final Runnable[] run = new Runnable[1];
		this.httpServer = new HttpServerImpl(this.httpServerConfig) {
			@Override
			protected ExecutorService createExecutorService() {
				return executorService;
			}

			@Override
			protected Thread createMainServerThread(Runnable r) {
				return mainServerThread;
			}

			@Override
			protected ServerSocket createServerSocket() {
				return serverSocket;
			}

			@Override
			protected Runnable createServerRunnable() {
				Runnable r = super.createServerRunnable();
				run[0] = r;
				return r;
			}
		};

		when(this.mainServerThread.isInterrupted()).thenReturn(false).thenReturn(true);
		when(this.serverSocket.accept()).thenThrow(new IOException("Accept failed"));

		run[0].run();

		verify(this.mainServerThread, times(1)).isInterrupted();
		verify(this.serverSocket).accept();
		verify(this.executorService, never()).submit(any(Runnable.class));
	}

	@Test
	public void testCreateCachedExecutorService() throws Exception {
		ThreadFactory threadFactory = mock(ThreadFactory.class);
		when(this.httpServerConfig.getWorkerThreadFactory()).thenReturn(threadFactory);
		ServerInfo serverInfo = mock(ServerInfo.class);
		when(httpServerConfig.getServerInfo()).thenReturn(serverInfo);
		when(serverInfo.getThreadCount()).thenReturn(0);

		this.httpServer = new HttpServerImpl(this.httpServerConfig) {
			@Override
			protected Thread createMainServerThread(Runnable r) {
				return mainServerThread;
			}

			@Override
			protected ServerSocket createServerSocket() {
				return serverSocket;
			}

			@Override
			protected Runnable createServerRunnable() {
				return mock(Runnable.class);
			}
		};

		ThreadPoolExecutor executorService = (ThreadPoolExecutor) this.httpServer.createExecutorService();
		assertEquals(0, executorService.getCorePoolSize());
		assertEquals(Integer.MAX_VALUE, executorService.getMaximumPoolSize());
		assertEquals(60, executorService.getKeepAliveTime(TimeUnit.SECONDS));
		assertSame(threadFactory, executorService.getThreadFactory());
		assertTrue(executorService.getQueue() instanceof SynchronousQueue);
	}

	@Test
	public void testCreateFixedExecutorService() throws Exception {
		ThreadFactory threadFactory = mock(ThreadFactory.class);
		when(this.httpServerConfig.getWorkerThreadFactory()).thenReturn(threadFactory);
		ServerInfo serverInfo = mock(ServerInfo.class);
		when(httpServerConfig.getServerInfo()).thenReturn(serverInfo);
		when(serverInfo.getThreadCount()).thenReturn(5);

		this.httpServer = new HttpServerImpl(this.httpServerConfig) {
			@Override
			protected Thread createMainServerThread(Runnable r) {
				return mainServerThread;
			}

			@Override
			protected ServerSocket createServerSocket() {
				return serverSocket;
			}

			@Override
			protected Runnable createServerRunnable() {
				return mock(Runnable.class);
			}
		};

		ThreadPoolExecutor executorService = (ThreadPoolExecutor) this.httpServer.createExecutorService();
		assertEquals(5, executorService.getCorePoolSize());
		assertEquals(5, executorService.getMaximumPoolSize());
		assertEquals(0, executorService.getKeepAliveTime(TimeUnit.SECONDS));
		assertSame(threadFactory, executorService.getThreadFactory());
		assertTrue(executorService.getQueue() instanceof LinkedBlockingQueue);
	}

	@Test
	public void testStartSuccess() {
		when(this.mainServerThread.getState()).thenReturn(Thread.State.NEW);
		this.httpServer = new HttpServerImpl(this.httpServerConfig) {
			@Override
			protected ExecutorService createExecutorService() {
				return executorService;
			}

			@Override
			protected Thread createMainServerThread(Runnable r) {
				return mainServerThread;
			}

			@Override
			protected ServerSocket createServerSocket() {
				return serverSocket;
			}

			@Override
			protected Runnable createServerRunnable() {
				return mock(Runnable.class);
			}
		};

		this.httpServer.start();

		verify(this.mainServerThread).start();
		verify(this.mainServerThread).getState();
	}

	@Test
	public void testStartFailed() {
		when(this.mainServerThread.getState()).thenReturn(Thread.State.TERMINATED);
		this.httpServer = new HttpServerImpl(this.httpServerConfig) {
			@Override
			protected ExecutorService createExecutorService() {
				return executorService;
			}

			@Override
			protected Thread createMainServerThread(Runnable r) {
				return mainServerThread;
			}

			@Override
			protected ServerSocket createServerSocket() {
				return serverSocket;
			}
		};

		assertThrows(HttpServerException.class, () -> {
			this.httpServer.start();
		});
	}

	@Test
	public void testStopSuccess() throws IOException {
		this.httpServer = new HttpServerImpl(this.httpServerConfig) {
			@Override
			protected ExecutorService createExecutorService() {
				return executorService;
			}

			@Override
			protected Thread createMainServerThread(Runnable r) {
				return mainServerThread;
			}

			@Override
			protected ServerSocket createServerSocket() {
				return serverSocket;
			}
		};

		this.httpServer.stop();

		verify(this.mainServerThread).interrupt();
		verify(this.serverSocket).close();
	}

	@Test
	public void testStopWithIOException() throws IOException {
		doThrow(new IOException("Close")).when(this.serverSocket).close();
		this.httpServer = new HttpServerImpl(this.httpServerConfig) {
			@Override
			protected ExecutorService createExecutorService() {
				return executorService;
			}

			@Override
			protected Thread createMainServerThread(Runnable r) {
				return mainServerThread;
			}

			@Override
			protected ServerSocket createServerSocket() {
				return serverSocket;
			}
		};

		this.httpServer.stop();

		verify(this.mainServerThread).interrupt();
		verify(this.serverSocket).close();
	}

	@Test
	public void testDestroyServerWithException() throws Exception {
		doThrow(new IOException("Close")).when(this.serverSocket).close();
		this.httpServer = new HttpServerImpl(this.httpServerConfig) {
			@Override
			protected ExecutorService createExecutorService() {
				return executorService;
			}

			@Override
			protected Thread createMainServerThread(Runnable r) {
				return mainServerThread;
			}

			@Override
			protected ServerSocket createServerSocket() {
				return serverSocket;
			}
		};

		this.httpServer.destroyHttpServer();

		verify(this.executorService).shutdownNow();
	}

	@Test
	public void testShutdownHook() throws Exception {
		this.httpServer = new HttpServerImpl(this.httpServerConfig) {
			@Override
			protected ExecutorService createExecutorService() {
				return executorService;
			}

			@Override
			protected Thread createMainServerThread(Runnable r) {
				return mainServerThread;
			}

			@Override
			protected ServerSocket createServerSocket() {
				return serverSocket;
			}
		};
		this.httpServer.getShutdownHook().run();

		verify(this.executorService).shutdownNow();
	}
}
