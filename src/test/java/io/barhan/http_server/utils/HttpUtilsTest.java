package io.barhan.http_server.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

public class HttpUtilsTest {
	@Test
	public void testNormalizeHeaderName() {
		assertEquals("Content-Type", HttpUtils.normalizeHeaderName("Content-Type"));
		assertEquals("Content-Type", HttpUtils.normalizeHeaderName("content-type"));
		assertEquals("Content-Type", HttpUtils.normalizeHeaderName("CONTENT-TYPE"));
		assertEquals("Content-Type", HttpUtils.normalizeHeaderName("CONTENT-type"));
		assertEquals("Content-Type", HttpUtils.normalizeHeaderName("CoNtEnT-tYpE"));

		assertEquals("Expires", HttpUtils.normalizeHeaderName("Expires"));
		assertEquals("Expires", HttpUtils.normalizeHeaderName("expires"));
		assertEquals("Expires", HttpUtils.normalizeHeaderName("EXPIRES"));

		assertEquals("If-Modified-Since", HttpUtils.normalizeHeaderName("If-Modified-Since"));
		assertEquals("If-Modified-Since", HttpUtils.normalizeHeaderName("if-modified-since"));
		assertEquals("If-Modified-Since", HttpUtils.normalizeHeaderName("IF-MODIFIED-SINCE"));

		assertEquals("Test-", HttpUtils.normalizeHeaderName("Test-"));
		assertEquals("Test-", HttpUtils.normalizeHeaderName("TEST-"));
		assertEquals("Test-", HttpUtils.normalizeHeaderName("test-"));
	}

	@Test
	public void testEOFException() throws IOException {
		InputStream in = mock(InputStream.class);
		when(in.read()).thenReturn(-1);
		Exception exception = assertThrows(EOFException.class, () -> {
			HttpUtils.readFirstLineAndHeaders(in);
		});
		assertEquals("Input stream closed.", exception.getMessage());
	}
}
