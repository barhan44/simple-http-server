package io.barhan.http_server.impl;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.barhan.http_server.Constants;
import io.barhan.http_server.HttpRequest;
import io.barhan.http_server.HttpResponse;
import io.barhan.http_server.HttpServerContext;
import io.barhan.http_server.config.HttpClientSocketHandler;
import io.barhan.http_server.config.HttpServerConfig;
import io.barhan.http_server.config.ReadableHttpResponse;
import io.barhan.http_server.exception.AbstractRequestParseFailedException;
import io.barhan.http_server.exception.HttpServerException;
import io.barhan.http_server.exception.MethodNotAllowedException;

class HttpClientSocketHandlerImpl implements HttpClientSocketHandler {
	private static final Logger ACCESS_LOGGER = LoggerFactory.getLogger("ACCESS_LOG");
	private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientSocketHandlerImpl.class);

	private final Socket clientSocket;
	private final String remoteAddress;
	private final HttpServerConfig httpServerConfig;

	HttpClientSocketHandlerImpl(Socket clientSocket, HttpServerConfig httpServerConfig) {
		this.clientSocket = clientSocket;
		this.remoteAddress = clientSocket.getRemoteSocketAddress().toString();
		this.httpServerConfig = httpServerConfig;
	}

	@Override
	public void run() {
		try {
			this.execute();
		} catch (Exception e) {
			LOGGER.error("Client request failed: " + e.getMessage(), e);
		}

	}

	private void execute() throws Exception {
		try (Socket s = this.clientSocket) {
			s.setKeepAlive(false);
			try (InputStream in = s.getInputStream(); OutputStream out = s.getOutputStream()) {
				this.processRequest(this.remoteAddress, in, out);
			}
		}
	}

	private void processRequest(String remoteAddress, InputStream in, OutputStream out) throws IOException {
		ReadableHttpResponse response = this.httpServerConfig.getHttpResponseBuilder().buildNewHttpResponse();
		String firstLine = null;
		try {
			HttpRequest request = this.httpServerConfig.getHttpRequestParser().parseHttpRequest(in, remoteAddress);
			firstLine = request.getFirstLine();
			this.processRequest(request, response);
		} catch (AbstractRequestParseFailedException e) {
			firstLine = e.getFirstLine();
			this.handleException(e, response);
		} catch (EOFException e) {
			LOGGER.warn("Client socket closed connection...");
			return;
		}
		this.httpServerConfig.getHttpResponseBuilder().prepareHttpResponse(response,
				firstLine.startsWith(Constants.HEAD));
		ACCESS_LOGGER.info("Request: {} - \"{}\", Response: {} ({} bytes)", this.remoteAddress, firstLine,
				response.getStatus(), response.getBodyLength());
		this.httpServerConfig.getHttpResponseWriter().writeHttpResponse(out, response);
	}

	private void processRequest(HttpRequest request, HttpResponse response) {
		HttpServerContext context = this.httpServerConfig.getHttpServerContext();
		try {
			this.httpServerConfig.getHttpRequestDispatcher().handle(context, request, response);
		} catch (Exception e) {
			this.handleException(e, response);
		}
	}

	private void handleException(Exception ex, HttpResponse response) {
		LOGGER.error("Exception during request: " + ex.getMessage(), ex);
		if (ex instanceof HttpServerException) {
			HttpServerException e = (HttpServerException) ex;
			response.setStatus(e.getStatusCode());
			if (e instanceof MethodNotAllowedException) {
				response.setHeader("Allow", StringUtils.join(Constants.ALLOWED_METHODS, ", "));
			}
		} else {
			response.setStatus(500);
		}
	}

}
