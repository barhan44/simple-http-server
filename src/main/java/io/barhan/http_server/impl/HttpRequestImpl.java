package io.barhan.http_server.impl;

import java.util.Map;

import io.barhan.http_server.HttpRequest;

class HttpRequestImpl implements HttpRequest {
	private final String method;
	private final String uri;
	private final String httpVersion;
	private final String remoteAddress;
	private final Map<String, String> headers;
	private final Map<String, String> params;

	public HttpRequestImpl(String method, String uri, String httpVersion, String remoteAddress,
			Map<String, String> headers, Map<String, String> params) {
		this.method = method;
		this.uri = uri;
		this.httpVersion = httpVersion;
		this.remoteAddress = remoteAddress;
		this.headers = headers;
		this.params = params;
	}

	@Override
	public String getFirstLine() {
		return String.format("%s %s %s", this.getMethod(), this.getURI(), this.getHTTPVersion());
	}

	@Override
	public String getMethod() {
		return this.method;
	}

	@Override
	public String getURI() {
		return this.uri;
	}

	@Override
	public String getHTTPVersion() {
		return this.httpVersion;
	}

	@Override
	public String getRemoteAddress() {
		return this.remoteAddress;
	}

	@Override
	public Map<String, String> getHeaders() {
		return this.headers;
	}

	@Override
	public Map<String, String> getParams() {
		return this.params;
	}

}
