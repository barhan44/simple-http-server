package io.barhan.http_server.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.WriterOutputStream;
import org.junit.Before;
import org.junit.Test;

import io.barhan.http_server.config.HttpResponseWriter;
import io.barhan.http_server.config.HttpServerConfig;
import io.barhan.http_server.config.ReadableHttpResponse;

public class HttpResponseWriterImplTest {
	private HttpResponseWriter writer;
	private HttpServerConfig httpServerConfig;
	private ReadableHttpResponse response;

	@Before
	public void before() {
		this.httpServerConfig = mock(HttpServerConfig.class);
		this.writer = new HttpResponseWriterImpl(this.httpServerConfig);
		when(this.httpServerConfig.getStatusMessage(200)).thenReturn("OK");
		response = new ReadableHttpResponseImpl();
		response.setHeader("Header 1", "value1");
		response.setHeader("Header 2", "value2");
	}

	private InputStream getClassPathResourceStream(String resourceName) {
		return getClass().getClassLoader().getResourceAsStream(resourceName);
	}

	@Test
	public void testWriteResponseWithBody() throws IOException {
		this.response.setBody("HTTP message body");

		StringWriter sw = new StringWriter();
		this.writer.writeHttpResponse(new WriterOutputStream(sw, StandardCharsets.UTF_8), this.response);

		try (InputStream in = this.getClassPathResourceStream("http-response-with-body.txt")) {
			String expected = IOUtils.toString(in, StandardCharsets.UTF_8);
			String actual = sw.toString();
			assertEquals(expected, actual);
		}
	}

	@Test
	public void testWriteResponseWithoutBody() throws IOException {
		StringWriter sw = new StringWriter();
		this.writer.writeHttpResponse(new WriterOutputStream(sw, StandardCharsets.UTF_8), this.response);

		try (InputStream in = this.getClassPathResourceStream("http-response-without-body.txt")) {
			String expected = IOUtils.toString(in, StandardCharsets.UTF_8);
			String actual = sw.toString();
			assertEquals(expected, actual);
		}
	}
}
