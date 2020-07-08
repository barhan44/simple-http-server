package io.barhan.http_server.config;

import java.util.Map;

public interface ReadableHttpResponse {
	int getStatus();
	
	Map<String, String> getHeaders();
	
	byte[] getBody();
	
	boolean isBodyEmpty();
	
	int getBodyLength();
}
