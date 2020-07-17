package io.barhan.http_server.impl;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;

import org.junit.Before;
import org.junit.Test;

import io.barhan.http_server.HttpRequest;
import io.barhan.http_server.HttpServerContext;
import io.barhan.http_server.config.HttpRequestDispatcher;
import io.barhan.http_server.config.HttpRequestParser;
import io.barhan.http_server.config.HttpResponseBuilder;
import io.barhan.http_server.config.HttpResponseWriter;
import io.barhan.http_server.config.HttpServerConfig;
import io.barhan.http_server.config.ReadableHttpResponse;
import io.barhan.http_server.exception.BadRequestException;
import io.barhan.http_server.exception.MethodNotAllowedException;

public class HttpClientSocketHandlerImplTest {
	private HttpClientSocketHandlerImpl httpClientSocketHandler;
	private Socket clientSocket;
	private String remoteAddress;
	private InputStream socketInputStream;
	private OutputStream socketOutputStream;

	private HttpServerConfig httpServerConfig;
	private HttpRequestParser httpRequestParser;
	private HttpResponseBuilder httpResponseBuilder;
	private HttpResponseWriter httpResponseWriter;
	private HttpServerContext httpServerContext;
	private HttpRequestDispatcher httpRequestDispatcher;

	private HttpRequest request;
	private ReadableHttpResponse response;

	@Before
	public void before() throws IOException {
		this.clientSocket = mock(Socket.class);
		this.remoteAddress = "localhost:1234";
		SocketAddress socketAddress = mock(SocketAddress.class);
		when(this.clientSocket.getRemoteSocketAddress()).thenReturn(socketAddress);
		when(socketAddress.toString()).thenReturn(this.remoteAddress);
		this.socketInputStream = mock(InputStream.class);
		this.socketOutputStream = mock(OutputStream.class);
		when(this.clientSocket.getInputStream()).thenReturn(this.socketInputStream);
		when(this.clientSocket.getOutputStream()).thenReturn(this.socketOutputStream);

		this.httpServerConfig = mock(HttpServerConfig.class);
		this.httpRequestParser = mock(HttpRequestParser.class);
		when(this.httpServerConfig.getHttpRequestParser()).thenReturn(this.httpRequestParser);
		this.httpResponseBuilder = mock(HttpResponseBuilder.class);
		when(this.httpServerConfig.getHttpResponseBuilder()).thenReturn(this.httpResponseBuilder);
		this.httpResponseWriter = mock(HttpResponseWriter.class);
		when(this.httpServerConfig.getHttpResponseWriter()).thenReturn(this.httpResponseWriter);
		this.httpRequestDispatcher = mock(HttpRequestDispatcher.class);
		when(this.httpServerConfig.getHttpRequestDispatcher()).thenReturn(this.httpRequestDispatcher);
		this.httpServerContext = mock(HttpServerContext.class);
		when(this.httpServerConfig.getHttpServerContext()).thenReturn(this.httpServerContext);
		this.httpClientSocketHandler = new HttpClientSocketHandlerImpl(this.clientSocket, this.httpServerConfig);

		this.request = mock(HttpRequest.class);
		when(this.httpRequestParser.parseHttpRequest(this.socketInputStream, this.remoteAddress)).thenReturn(request);
		when(this.request.getFirstLine()).thenReturn("GET /index.html HTTP/1.1");
		this.response = mock(ReadableHttpResponse.class);
		when(httpResponseBuilder.buildNewHttpResponse()).thenReturn(this.response);
	}

	@Test
	public void testCloseResourcesOnError() throws IOException {
		when(this.httpResponseBuilder.buildNewHttpResponse()).thenThrow(new RuntimeException("Close test"));

		this.httpClientSocketHandler.run();

		this.verifySocketInteractions();
	}

	@Test
	public void testSuccessfulRequestHandle() throws IOException {
		this.httpClientSocketHandler.run();

		this.verifySocketInteractions();
		this.verifyMainFlowWithoutParseErrors();
		verify(this.response, never()).setStatus(anyInt());
	}

	@Test
	public void testHandleRuntimeExceptionDuringProcessRequest() throws IOException {
		doThrow(new RuntimeException("Test exception")).when(this.httpRequestDispatcher).handle(this.httpServerContext,
				request, response);

		this.httpClientSocketHandler.run();

		this.verifySocketInteractions();
		this.verifyMainFlowWithoutParseErrors();
		verify(this.response).setStatus(500);
	}

	@Test
	public void testHandleHttpServerExceptionDuringProcessRequest() throws IOException {
		doThrow(new BadRequestException("", null, "")).when(this.httpRequestDispatcher).handle(this.httpServerContext,
				this.request, this.response);

		this.httpClientSocketHandler.run();

		this.verifySocketInteractions();
		this.verifyMainFlowWithoutParseErrors();
		verify(this.response).setStatus(400);
	}

	@Test
	public void testMethodNotAllowedException() throws IOException {
		when(this.httpRequestParser.parseHttpRequest(this.socketInputStream, this.remoteAddress))
				.thenThrow(new MethodNotAllowedException("PUT", "PUT /index.html HTTP/1.1"));

		this.httpClientSocketHandler.run();

		this.verifySocketInteractions();
		verify(this.httpServerConfig, times(2)).getHttpResponseBuilder();
		verify(this.httpResponseBuilder).buildNewHttpResponse();
		verify(this.httpServerConfig).getHttpRequestParser();
		verify(this.httpRequestParser).parseHttpRequest(this.socketInputStream, this.remoteAddress);
		verify(this.request, never()).getFirstLine();
		verify(this.httpServerConfig, never()).getHttpServerContext();
		verify(this.httpRequestDispatcher, never()).handle(this.httpServerContext, this.request, this.response);
		verify(this.httpResponseBuilder).prepareHttpResponse(this.response, false);
		verify(this.httpServerConfig).getHttpResponseWriter();
		verify(this.httpResponseWriter).writeHttpResponse(this.socketOutputStream, this.response);
		verify(this.response).setStatus(405);
		verify(this.response).setHeader("Allow", "GET, POST, HEAD");
	}

	@Test
	public void testHandleFirstLineFromException() throws IOException {
		when(this.httpRequestParser.parseHttpRequest(this.socketInputStream, this.remoteAddress))
				.thenThrow(new MethodNotAllowedException("HEAD", "HEAD /index.html HTTP/1.1"));

		this.httpClientSocketHandler.run();

		verify(this.httpResponseBuilder).prepareHttpResponse(this.response, true);
	}

	@Test
	public void testEOFException() throws IOException {
		when(this.httpRequestParser.parseHttpRequest(this.socketInputStream, this.remoteAddress))
				.thenThrow(new EOFException("InputStream closed!"));

		this.httpClientSocketHandler.run();

		this.verifySocketInteractions();
		verify(this.httpServerConfig, times(1)).getHttpResponseBuilder();
		verify(this.httpResponseBuilder).buildNewHttpResponse();
		verify(this.httpServerConfig).getHttpRequestParser();
		verify(this.httpRequestParser).parseHttpRequest(this.socketInputStream, this.remoteAddress);

		verify(this.request, never()).getFirstLine();
		verify(this.httpServerConfig, never()).getHttpServerContext();
		verify(this.httpRequestDispatcher, never()).handle(this.httpServerContext, this.request, this.response);
		verify(this.httpResponseBuilder, never()).prepareHttpResponse(this.response, false);
		verify(this.httpServerConfig, never()).getHttpResponseWriter();
		verify(this.httpResponseWriter, never()).writeHttpResponse(this.socketOutputStream, this.response);
	}

	private void verifySocketInteractions() throws IOException {
		verify(this.clientSocket).setKeepAlive(false);
		verify(this.clientSocket).close();
		verify(this.clientSocket).getInputStream();
		verify(this.clientSocket).getOutputStream();
		verify(this.socketInputStream).close();
		verify(this.socketOutputStream).close();
	}

	private void verifyMainFlowWithoutParseErrors() throws IOException {
		verify(this.httpServerConfig, times(2)).getHttpResponseBuilder();
		verify(this.httpResponseBuilder).buildNewHttpResponse();
		verify(this.httpServerConfig).getHttpRequestParser();
		verify(this.httpRequestParser).parseHttpRequest(this.socketInputStream, this.remoteAddress);
		verify(this.request).getFirstLine();
		verify(this.httpServerConfig).getHttpServerContext();
		verify(this.httpRequestDispatcher).handle(this.httpServerContext, this.request, this.response);
		verify(this.httpResponseBuilder).prepareHttpResponse(this.response, false);
		verify(this.httpServerConfig).getHttpResponseWriter();
		verify(this.httpResponseWriter).writeHttpResponse(this.socketOutputStream, response);
	}
}
