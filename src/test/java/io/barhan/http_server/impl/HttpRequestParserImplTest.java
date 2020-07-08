package io.barhan.http_server.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;

import io.barhan.http_server.HttpRequest;

public class HttpRequestParserImplTest {
	private HttpRequestParserImpl httpRequestParserImpl;

	@Before
	public void before() {
		this.httpRequestParserImpl = new HttpRequestParserImpl();
	}

	private InputStream getClassPathResourceStream(String resourceName) {
		return getClass().getClassLoader().getResourceAsStream(resourceName);
	}

	@Test
	public void testSimpleGET() throws IOException {
		try (InputStream httpMessage = this.getClassPathResourceStream("simple-GET.txt")) {
			HttpRequest request = this.httpRequestParserImpl.parseHttpRequest(httpMessage, "localhost");
			assertEquals("GET", request.getMethod());
			assertEquals("/index.html", request.getURI());
			assertEquals("HTTP/1.1", request.getHTTPVersion());
			assertEquals("localhost", request.getHeaders().get("Host"));
			assertEquals(
					"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.116 Safari/537.36",
					request.getHeaders().get("User-Agent"));
			assertEquals("text/html", request.getHeaders().get("Accept"));
			assertEquals("close", request.getHeaders().get("Connection"));
			assertTrue(request.getParams().isEmpty());
			assertEquals("localhost", request.getRemoteAddress());
		}
	}
}
