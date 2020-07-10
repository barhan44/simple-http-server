package io.barhan.http_server.config;

import java.util.Map;

import io.barhan.http_server.HttpResponse;

public interface ReadableHttpResponse extends HttpResponse {
	int getStatus();
	
	Map<String, String> getHeaders();
	
	byte[] getBody();
	
	boolean isBodyEmpty();
	
	int getBodyLength();
}
