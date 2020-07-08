package io.barhan.http_server.config;

public interface HttpResponseBuilder {
	ReadableHttpResponse buildNewHttpResponse();

	void prepareHttpResponse(ReadableHttpResponse response, boolean clearBody);
}
