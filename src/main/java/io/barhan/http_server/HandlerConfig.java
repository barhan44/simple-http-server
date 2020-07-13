package io.barhan.http_server;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.barhan.http_server.exception.HttpServerConfigException;

public final class HandlerConfig {
	private final Map<String, HttpHandler> httpHandlers = new HashMap<>();

	public HandlerConfig addHandler(String url, HttpHandler httpHandler) {
		Objects.requireNonNull(url);
		Objects.requireNonNull(httpHandler);

		HttpHandler prevHttpHandler = this.httpHandlers.get(url);
		if (prevHttpHandler != null) {
			throw new HttpServerConfigException("Http handler already exists for url=" + url + ". Http handler class: "
					+ prevHttpHandler.getClass().getName());
		}
		this.httpHandlers.put(url, httpHandler);
		return this;
	}

	public Map<String, HttpHandler> toMap() {
		return Collections.unmodifiableMap(httpHandlers);
	}
}
