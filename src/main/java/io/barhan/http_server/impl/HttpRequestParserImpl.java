package io.barhan.http_server.impl;

import java.io.IOException;
import java.io.InputStream;

import io.barhan.http_server.HttpRequest;
import io.barhan.http_server.config.HttpRequestParser;
import io.barhan.http_server.exception.HttpServerException;

public class HttpRequestParserImpl implements HttpRequestParser {

	@Override
	public HttpRequest parseHttpRequest(InputStream inputStream, String remoteAddress)
			throws IOException, HttpServerException {
		// TODO Auto-generated method stub
		return new HttpRequestImpl();
	}

}
