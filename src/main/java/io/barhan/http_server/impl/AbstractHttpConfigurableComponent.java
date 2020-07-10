package io.barhan.http_server.impl;

import io.barhan.http_server.config.HttpServerConfig;

class AbstractHttpConfigurableComponent {
	final HttpServerConfig httpServerConfig;

	public AbstractHttpConfigurableComponent(HttpServerConfig httpServerConfig) {
		this.httpServerConfig = httpServerConfig;
	}
}
