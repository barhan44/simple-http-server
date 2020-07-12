package io.barhan.http_server.impl;

import java.io.IOException;

import io.barhan.http_server.HttpRequest;
import io.barhan.http_server.HttpResponse;
import io.barhan.http_server.HttpServerContext;
import io.barhan.http_server.config.HttpRequestDispatcher;

public class TemporaryHttpRequestDispatcher implements HttpRequestDispatcher {

	@Override
	public void handle(HttpServerContext context, HttpRequest request, HttpResponse response) throws IOException {
		response.setBody("<h1>I am alive!</h1>");
	}

}
