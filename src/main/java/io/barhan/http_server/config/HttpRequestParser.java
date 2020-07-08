package io.barhan.http_server.config;

import java.io.IOException;
import java.io.InputStream;

import io.barhan.http_server.HttpRequest;
import io.barhan.http_server.exception.HttpServerException;

public interface HttpRequestParser {
	HttpRequest parseHttpRequest(InputStream inputStream, String remoteAddress) throws IOException, HttpServerException;
}
