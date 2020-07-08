package io.barhan.http_server.config;

import java.io.IOException;
import java.io.OutputStream;

public interface HttpResponseWriter {
	void writeHttpResponse(OutputStream out, ReadableHttpResponse response) throws IOException;
}
