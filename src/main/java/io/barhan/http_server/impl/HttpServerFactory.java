package io.barhan.http_server.impl;

import java.util.Properties;

import io.barhan.http_server.HttpServer;
import io.barhan.http_server.config.HttpServerConfig;

public class HttpServerFactory {
	protected HttpServerFactory() {
	};

	public static HttpServerFactory create() {
		return new HttpServerFactory();
	}

	public HttpServer createHttpServer(Properties serverProperties) {
		HttpServerConfig httpServerConfig = null;
	}
}
