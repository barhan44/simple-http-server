package io.barhan.http_server.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Properties;

import org.apache.commons.io.input.ReaderInputStream;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import io.barhan.http_server.HandlerConfig;
import io.barhan.http_server.HttpHandler;
import io.barhan.http_server.ServerInfo;
import io.barhan.http_server.config.HttpClientSocketHandler;
import io.barhan.http_server.exception.HttpServerConfigException;

public class HttpServerConfigImplTest {
	private HttpServerConfigImpl httpServerConfig;

	@Before
	public void before() {
		this.httpServerConfig = spy(new HttpServerConfigImpl(null, null));
	}

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void testDefaultInitState() {
		assertEquals(HttpRequestParserImpl.class, this.httpServerConfig.getHttpRequestParser().getClass());
		assertEquals(HttpResponseBuilderImpl.class, this.httpServerConfig.getHttpResponseBuilder().getClass());
		assertEquals(HttpResponseWriterImpl.class, this.httpServerConfig.getHttpResponseWriter().getClass());
		assertEquals(HttpServerContextImpl.class, this.httpServerConfig.getHttpServerContext().getClass());
		assertEquals(HttpRequestDispatcherImpl.class, this.httpServerConfig.getHttpRequestDispatcher().getClass());
		assertEquals(ThreadFactoryImpl.class, this.httpServerConfig.getWorkerThreadFactory().getClass());
		assertEquals(HtmlTemplateManagerImpl.class, this.httpServerConfig.getHtmlTemplateManager().getClass());
		assertNotNull(this.httpServerConfig.getServerProperties());
	}

	@Test
	public void testHandlerConfig() {
		assertNotNull(this.httpServerConfig.getHttpHandlers());
		assertTrue(this.httpServerConfig.getHttpHandlers().isEmpty());

		this.httpServerConfig = spy(
				new HttpServerConfigImpl(new HandlerConfig().addHandler("/url", mock(HttpHandler.class)), null));
		assertEquals(1, this.httpServerConfig.getHttpHandlers().size());
		assertNotNull(this.httpServerConfig.getHttpHandlers().get("/url"));
	}

	@Test
	public void testOverrideProperties() {
		ServerInfo serverInfo = this.httpServerConfig.getServerInfo();
		assertEquals(5000, serverInfo.getPort());

		Properties props = new Properties();
		props.setProperty("server.port", "5001");
		this.httpServerConfig = spy(new HttpServerConfigImpl(null, props));

		serverInfo = this.httpServerConfig.getServerInfo();
		assertEquals(5001, serverInfo.getPort());
	}

	@Test
	public void testServerInfo() {
		Properties props = new Properties();
		props.setProperty("server.port", "5002");
		props.setProperty("server.name", "Simple HTTP Server");
		props.setProperty("server.thread.count", "0");
		this.httpServerConfig = spy(new HttpServerConfigImpl(null, props));
		ServerInfo serverInfo = this.httpServerConfig.getServerInfo();
		assertEquals(5002, serverInfo.getPort());
		assertEquals(0, serverInfo.getThreadCount());
		assertEquals("Simple HTTP Server", serverInfo.getName());
		assertEquals("ServerInfo [name=Simple HTTP Server, port=5002, threadCount=0]", serverInfo.toString());

		props.setProperty("server.thread.count", "5");
		this.httpServerConfig = spy(new HttpServerConfigImpl(null, props));
		serverInfo = this.httpServerConfig.getServerInfo();
		assertEquals(5, serverInfo.getThreadCount());
		assertEquals("ServerInfo [name=Simple HTTP Server, port=5002, threadCount=5]", serverInfo.toString());
	}

	@Test
	public void testServerInfoInvalidThreadCount() {
		Properties props = new Properties();
		props.setProperty("server.thread.count", "-1");

		Exception exception = assertThrows(HttpServerConfigException.class, () -> {
			this.httpServerConfig = spy(new HttpServerConfigImpl(null, props));
		});

		assertEquals("server.thread.count should be >= 0, where 0 is UNLIMITED threads", exception.getMessage());
	}

	@Test
	public void testSuccessLoadProperties() throws IOException {
		InputStream in = spy(new ReaderInputStream(new StringReader("k=v\r\na=b"), StandardCharsets.UTF_8));
		ClassLoader cl = mock(ClassLoader.class);
		when(cl.getResourceAsStream("name")).thenReturn(in);

		this.httpServerConfig.loadProperties(new Properties(), cl, "name");

		verify(in, atLeast(1)).read(any(byte[].class));
		verify(in).close();
	}

	@Test
	public void testNotFoundLoadProperties() throws IOException {
		ClassLoader cl = mock(ClassLoader.class);
		when(cl.getResourceAsStream("name")).thenReturn(null);

		Exception exception = assertThrows(HttpServerConfigException.class, () -> {
			this.httpServerConfig.loadProperties(new Properties(), cl, "name");
		});

		assertEquals("Class path resource not found: name", exception.getMessage());
	}

	@Test
	public void testIOExceptionLoadProperties() throws IOException {
		InputStream in = mock(InputStream.class);
		when(in.read(any(byte[].class))).thenThrow(new IOException("IO error"));
		ClassLoader cl = mock(ClassLoader.class);
		when(cl.getResourceAsStream("name")).thenReturn(in);

		Exception exception = assertThrows(HttpServerConfigException.class, () -> {
			this.httpServerConfig.loadProperties(new Properties(), cl, "name");
		});

		assertEquals("Fail on loading properties from resource: name", exception.getMessage());
	}

	@Test
	public void testBuildNewHttpClientSocketHandler() {
		Socket clientSocket = mock(Socket.class);
		SocketAddress socketAddress = mock(SocketAddress.class);
		when(clientSocket.getRemoteSocketAddress()).thenReturn(socketAddress);
		when(socketAddress.toString()).thenReturn("localhost:2000");

		HttpClientSocketHandler handler1 = this.httpServerConfig.buildNewHttpClientSocketHandler(clientSocket);
		HttpClientSocketHandler handler2 = this.httpServerConfig.buildNewHttpClientSocketHandler(clientSocket);

		assertEquals(HttpClientSocketHandlerImpl.class, handler1.getClass());
		assertEquals(HttpClientSocketHandlerImpl.class, handler2.getClass());
	}

	@Test
	public void testGetStatusMessage() throws IOException {
		assertEquals("OK", this.httpServerConfig.getStatusMessage(200));
		assertEquals("Internal Server Error", this.httpServerConfig.getStatusMessage(500));
	}

	@Test
	public void testCreateRootPath() throws IOException {
		File file = this.folder.newFolder("root");
		Properties props = new Properties();
		props.setProperty("webapp.static.dir.root", file.getAbsolutePath());
		this.httpServerConfig = spy(new HttpServerConfigImpl(null, props));
		Path root = this.httpServerConfig.createRootPath();
		assertEquals(file.getAbsoluteFile().toString(), root.toAbsolutePath().toString());
	}

	@Test
	public void testRootPathNotFound() throws IOException {
		Properties props = new Properties();
		props.setProperty("webapp.static.dir.root", "not-found-path");

		assertThrows(HttpServerConfigException.class, () -> {
			spy(new HttpServerConfigImpl(null, props));
		});
	}

	@Test
	public void testRootPathNotDirectory() throws IOException {
		File file = this.folder.newFile();
		Properties props = new Properties();
		props.setProperty("webapp.static.dir.root", file.getAbsolutePath());

		assertThrows(HttpServerConfigException.class, () -> {
			spy(new HttpServerConfigImpl(null, props));
		});
	}
}
