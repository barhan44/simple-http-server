package io.barhan.http_server;

import java.util.Map;

public interface HttpRequest {
	String getFirstLine();

	String getMethod();

	String getURI();

	String getHTTPVersion();

	String getRemoteAddress();

	Map<String, String> getHeaders();

	Map<String, String> getParams();
}
