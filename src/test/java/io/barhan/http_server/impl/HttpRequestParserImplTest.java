package io.barhan.http_server.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;

import io.barhan.http_server.HttpRequest;
import io.barhan.http_server.exception.BadRequestException;
import io.barhan.http_server.exception.HttpVersionNotSupportedException;
import io.barhan.http_server.exception.MethodNotAllowedException;

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

	@Test
	public void testUnsensitiveCaseGet() throws IOException {
		try (InputStream httpMessage = this.getClassPathResourceStream("unsensitive-case-GET.txt")) {
			HttpRequest request = httpRequestParserImpl.parseHttpRequest(httpMessage, "localhost");
			assertEquals("localhost", request.getHeaders().get("Host"));
			assertEquals(
					"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.116 Safari/537.36",
					request.getHeaders().get("User-Agent"));
			assertEquals("text/html", request.getHeaders().get("Accept"));
			assertEquals("close", request.getHeaders().get("Connection"));
		}
	}

	@Test
	public void testNewLineHeadersGet() throws IOException {
		try (InputStream httpMessage = this.getClassPathResourceStream("new-line-headers-GET.txt")) {
			HttpRequest request = httpRequestParserImpl.parseHttpRequest(httpMessage, "localhost");
			assertEquals("localhost", request.getHeaders().get("Host"));
			assertEquals("text/html", request.getHeaders().get("Accept"));
			assertEquals("text/html;charset=windows-1251", request.getHeaders().get("Content-Type"));
		}
	}

	@Test
	public void testInvalidHttpVersion() throws IOException {
		try (InputStream httpMessage = this.getClassPathResourceStream("invalid-http-version-GET.txt")) {
			Exception exception = assertThrows(HttpVersionNotSupportedException.class, () -> {
				httpRequestParserImpl.parseHttpRequest(httpMessage, "localhost");
			});
			assertEquals("This server only supports HTTP/1.1 protocol", exception.getMessage());
		}
	}

	@Test
	public void testSimpleHead() throws IOException {
		try (InputStream httpMessage = this.getClassPathResourceStream("simple-HEAD.txt")) {
			HttpRequest request = httpRequestParserImpl.parseHttpRequest(httpMessage, "localhost");
			assertEquals("HEAD", request.getMethod());
			assertEquals("/index.html", request.getURI());
			assertEquals("HTTP/1.1", request.getHTTPVersion());
		}
	}

	@Test
	public void testSimpleParamsGet() throws IOException {
		try (InputStream httpsMessage = this.getClassPathResourceStream("simple-params-GET.txt")) {
			HttpRequest request = httpRequestParserImpl.parseHttpRequest(httpsMessage, "localhost");
			assertEquals("GET", request.getMethod());
			assertEquals("/index.html", request.getURI());
			assertEquals("HTTP/1.1", request.getHTTPVersion());

			assertEquals(2, request.getParams().size());
			assertEquals("value1", request.getParams().get("param1"));
			assertEquals("true", request.getParams().get("param2"));
		}
	}

	@Test
	public void testDuplicatedParamsGet() throws IOException {
		try (InputStream httpsMessage = this.getClassPathResourceStream("duplicated-params-GET.txt")) {
			HttpRequest request = httpRequestParserImpl.parseHttpRequest(httpsMessage, "localhost");
			assertEquals("GET", request.getMethod());
			assertEquals("/index.html", request.getURI());
			assertEquals("HTTP/1.1", request.getHTTPVersion());

			assertEquals(2, request.getParams().size());
			assertEquals("value1,value2,value1", request.getParams().get("param1"));
			assertEquals("true", request.getParams().get("param2"));
		}
	}

	@Test
	public void testDecodedParamsGet() throws IOException {
		try (InputStream httpsMessage = this.getClassPathResourceStream("decoded-params-GET.txt")) {
			HttpRequest request = httpRequestParserImpl.parseHttpRequest(httpsMessage, "localhost");
			assertEquals("GET", request.getMethod());
			assertEquals("/index.html", request.getURI());
			assertEquals("HTTP/1.1", request.getHTTPVersion());

			assertEquals(6, request.getParams().size());
			assertEquals("test@barhan44.github.io", request.getParams().get("email"));
			assertEquals("", request.getParams().get("password"));
			assertEquals("5", request.getParams().get("number"));
			assertEquals("test&qwerty?ty=u", request.getParams().get("p"));
			assertEquals("Simple Text", request.getParams().get("text"));
			assertEquals("http://barhan44.github.io", request.getParams().get("url"));
		}
	}

	@Test
	public void testSimplePost() throws IOException {
		try (InputStream httpsMessage = this.getClassPathResourceStream("simple-POST.txt")) {
			HttpRequest request = httpRequestParserImpl.parseHttpRequest(httpsMessage, "localhost");
			assertEquals("POST", request.getMethod());
			assertEquals("/index.html", request.getURI());
			assertEquals("HTTP/1.1", request.getHTTPVersion());

			assertEquals(5, request.getParams().size());
			assertEquals("test@barhan44.github.io", request.getParams().get("email"));
			assertEquals("", request.getParams().get("password"));
			assertEquals("5", request.getParams().get("number"));
			assertEquals("Simple Text", request.getParams().get("text"));
			assertEquals("http://barhan44.github.io", request.getParams().get("url"));
		}
	}

	@Test
	public void testEmptyBodyPost() throws IOException {
		try (InputStream httpMessage = this.getClassPathResourceStream("empty-body-POST.txt")) {
			HttpRequest request = httpRequestParserImpl.parseHttpRequest(httpMessage, "localhost");
			assertEquals("POST", request.getMethod());
			assertEquals("/index.html", request.getURI());
			assertEquals("HTTP/1.1", request.getHTTPVersion());

			assertEquals(0, request.getParams().size());
		}
	}

	@Test
	public void testEmptyBodyZeroContentLengthPost() throws IOException {
		try (InputStream httpMessage = this.getClassPathResourceStream("empty-body-zero-content-length-POST.txt")) {
			HttpRequest request = httpRequestParserImpl.parseHttpRequest(httpMessage, "localhost");
			assertEquals("POST", request.getMethod());
			assertEquals("/index.html", request.getURI());
			assertEquals("HTTP/1.1", request.getHTTPVersion());

			assertEquals(0, request.getParams().size());
		}
	}

	@Test
	public void testRuntimeException() throws IOException {
		Exception exception = assertThrows(BadRequestException.class, () -> {
			httpRequestParserImpl.parseHttpRequest(null, "localhost");
		});
		assertEquals("Error on parsing HTTP request: null", exception.getMessage());
	}

	@Test
	public void testNotAllowedMethodException() throws IOException {
		try (InputStream httpMessage = this.getClassPathResourceStream("not-allowed-method.txt")) {
			Exception exception = assertThrows(MethodNotAllowedException.class, () -> {
				httpRequestParserImpl.parseHttpRequest(httpMessage, "localhost");
			});
			assertEquals("Only [GET, POST, HEAD] are supported. But the current method is PUT", exception.getMessage());
		}
	}
}
