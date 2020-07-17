package io.barhan.http_server.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import io.barhan.http_server.HandlerConfig;
import io.barhan.http_server.HttpHandler;
import io.barhan.http_server.HttpRequest;
import io.barhan.http_server.HttpResponse;
import io.barhan.http_server.HttpServerContext;
import io.barhan.http_server.config.HttpRequestDispatcher;
import io.barhan.http_server.exception.HttpServerException;

public class HttpRequestDispatcherImplTest {
	private HttpRequestDispatcher httpRequestDispatcher;
	private HttpHandler defaulthttpHandler;
	private HttpHandler urlHttpHandler;
	private HttpServerContext context;
	private HttpRequest request;
	private HttpResponse response;

	@Before
	public void before() {
		this.context = mock(HttpServerContext.class);
		this.request = mock(HttpRequest.class);
		this.response = mock(HttpResponse.class);
		this.defaulthttpHandler = mock(HttpHandler.class);
		this.urlHttpHandler = mock(HttpHandler.class);
		this.httpRequestDispatcher = new HttpRequestDispatcherImpl(this.defaulthttpHandler, Collections.emptyMap());
	}

	@Test
	public void testInvokeDefaultHttpHandler() throws IOException {
		this.httpRequestDispatcher.handle(this.context, this.request, this.response);

		verify(this.defaulthttpHandler).handle(this.context, this.request, this.response);
	}

	@Test
	public void testRuntimeException() throws IOException {
		when(this.request.getURI()).thenReturn("/test");
		RuntimeException re = new RuntimeException("Test Runtime Exception");
		doThrow(re).when(this.defaulthttpHandler).handle(this.context, this.request, this.response);

		Exception exception = assertThrows(HttpServerException.class, () -> {
			this.httpRequestDispatcher.handle(this.context, this.request, this.response);
		});

		assertEquals("Handle request: /test failed: Test Runtime Exception", exception.getMessage());
	}

	@Test
	public void testHttpServerException() throws IOException {
		HttpServerException hse = new HttpServerException("Test HTTP Server Exception");
		doThrow(hse).when(this.defaulthttpHandler).handle(this.context, this.request, this.response);

		Exception exception = assertThrows(HttpServerException.class, () -> {
			this.httpRequestDispatcher.handle(this.context, this.request, this.response);
		});

		assertEquals("Test HTTP Server Exception", exception.getMessage());
	}

	@Test
	public void testInvokeUrlHttpHandler() throws IOException {
		this.httpRequestDispatcher = new HttpRequestDispatcherImpl(this.defaulthttpHandler,
				new HandlerConfig().addHandler("/test", this.urlHttpHandler).toMap());

		when(request.getURI()).thenReturn("/test");
		this.httpRequestDispatcher.handle(this.context, this.request, this.response);
		verify(this.urlHttpHandler).handle(this.context, this.request, this.response);

		when(request.getURI()).thenReturn("/test2");
		this.httpRequestDispatcher.handle(this.context, this.request, this.response);
		verify(this.defaulthttpHandler).handle(this.context, this.request, this.response);
	}
}
