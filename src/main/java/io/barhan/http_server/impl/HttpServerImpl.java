package io.barhan.http_server.impl;

import io.barhan.http_server.HttpServer;
import io.barhan.http_server.config.HttpServerConfig;

class HttpServerImpl implements HttpServer {
	private final HttpServerConfig httpServerConfig;

	protected HttpServerImpl(HttpServerConfig httpServerConfig) {
		this.httpServerConfig = httpServerConfig;
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub

	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}
}
