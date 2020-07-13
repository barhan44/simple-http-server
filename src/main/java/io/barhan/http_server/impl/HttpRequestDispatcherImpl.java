package io.barhan.http_server.impl;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import io.barhan.http_server.HttpHandler;
import io.barhan.http_server.HttpRequest;
import io.barhan.http_server.HttpResponse;
import io.barhan.http_server.HttpServerContext;
import io.barhan.http_server.config.HttpRequestDispatcher;
import io.barhan.http_server.exception.HttpServerException;

class HttpRequestDispatcherImpl implements HttpRequestDispatcher {

	private final HttpHandler defaultHttpHandler;
	private final Map<String, HttpHandler> httpHandlers;

	HttpRequestDispatcherImpl(HttpHandler defaultHttpHandler, Map<String, HttpHandler> httpHandlers) {
		Objects.requireNonNull(defaultHttpHandler, "Default http handler cannot be null!");
		Objects.requireNonNull(httpHandlers, "http handlers cannot be null!");
		this.defaultHttpHandler = defaultHttpHandler;
		this.httpHandlers = httpHandlers;
	}

	@Override
	public void handle(HttpServerContext context, HttpRequest request, HttpResponse response) throws IOException {
		try {
			HttpHandler handler = this.getHttpHandler(request);
			handler.handle(context, request, response);
		} catch (RuntimeException e) {
			if (e instanceof HttpServerException) {
				throw e;
			} else {
				throw new HttpServerException("Handle request: " + request.getURI() + " failed: " + e.getMessage(), e);
			}
		}

	}

	private HttpHandler getHttpHandler(HttpRequest request) {
		HttpHandler handler = this.httpHandlers.get(request.getURI());
		if (handler == null) {
			handler = this.defaultHttpHandler;
		}
		return handler;
	}

}
