package io.barhan.http_server.impl;

import io.barhan.http_server.config.HttpResponseBuilder;
import io.barhan.http_server.config.ReadableHttpResponse;

public class HttpResponseBuilderImpl implements HttpResponseBuilder {

	@Override
	public ReadableHttpResponse buildNewHttpResponse() {
		// TODO Auto-generated method stub
		return new ReadableHttpResponseImpl();
	}

	@Override
	public void prepareHttpResponse(ReadableHttpResponse response, boolean clearBody) {
		// TODO Auto-generated method stub

	}

}
