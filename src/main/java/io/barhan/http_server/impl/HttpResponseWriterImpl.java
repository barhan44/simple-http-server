package io.barhan.http_server.impl;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import io.barhan.http_server.Constants;
import io.barhan.http_server.config.HttpResponseWriter;
import io.barhan.http_server.config.HttpServerConfig;
import io.barhan.http_server.config.ReadableHttpResponse;

public class HttpResponseWriterImpl extends AbstractHttpConfigurableComponent implements HttpResponseWriter {

	public HttpResponseWriterImpl(HttpServerConfig httpServerConfig) {
		super(httpServerConfig);
	}

	@Override
	public void writeHttpResponse(OutputStream out, ReadableHttpResponse response) throws IOException {
		PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8)));
		this.addFirstLine(writer, response);
		this.addHeaders(writer, response);
		writer.println();
		writer.flush();
		this.addMessageBody(out, response);
	}

	private void addFirstLine(PrintWriter out, ReadableHttpResponse response) {
		String httpVersion = Constants.SUPPORTED_HTTP_VERSION;
		int statusCode = response.getStatus();
		String statusMessage = httpServerConfig.getStatusMessage(statusCode);
		out.println(String.format("%s %s %s", httpVersion, statusCode, statusMessage));
	}

	private void addHeaders(PrintWriter out, ReadableHttpResponse response) {
		for (Map.Entry<String, String> entry : response.getHeaders().entrySet()) {
			out.println(String.format("%s: %s", entry.getKey(), entry.getValue()));
		}
	}

	private void addMessageBody(OutputStream out, ReadableHttpResponse response) throws IOException {
		if (!response.isBodyEmpty()) {
			out.write(response.getBody());
			out.flush();
		}
	}

}
