package io.barhan.http_server.impl;

import java.util.Date;

import io.barhan.http_server.config.HttpResponseBuilder;
import io.barhan.http_server.config.HttpServerConfig;
import io.barhan.http_server.config.ReadableHttpResponse;

class HttpResponseBuilderImpl extends AbstractHttpConfigurableComponent implements HttpResponseBuilder {

	public HttpResponseBuilderImpl(HttpServerConfig httpServerConfig) {
		super(httpServerConfig);
	}

	private ReadableHttpResponse createReadableHttpResponseInstance() {
		return new ReadableHttpResponseImpl();
	}

	@Override
	public ReadableHttpResponse buildNewHttpResponse() {
		ReadableHttpResponse response = this.createReadableHttpResponseInstance();
		response.setHeader("Date", new Date());
		response.setHeader("Server", httpServerConfig.getServerInfo().getName());
		response.setHeader("Content-Language", "en");
		response.setHeader("Connection", "close");
		response.setHeader("Content-Type", "text/html");
		return response;
	}

	@Override
	public void prepareHttpResponse(ReadableHttpResponse response, boolean clearBody) {
		if (response.getStatus() >= 400 && response.isBodyEmpty()) {}
		this.setContentLength(response);
		if (clearBody) {
			this.clearBody(response);
		}
	}
	
	private void setContentLength(ReadableHttpResponse response) {
		response.setHeader("Content-Length", response.getBodyLength());
	}
	
	private void clearBody(ReadableHttpResponse response) {
		response.setBody("");
	}

}
