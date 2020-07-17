package io.barhan.http_server.impl;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.attribute.FileTime;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import io.barhan.http_server.config.ReadableHttpResponse;
import io.barhan.http_server.exception.HttpServerException;

public class ReadableHttpResponseImplTest {
	private ReadableHttpResponse httpResponse;

	@Before
	public void before() {
		this.httpResponse = new ReadableHttpResponseImpl();
	}

	@Test
	public void testInitState() {
		assertEquals(200, httpResponse.getStatus());
		assertEquals(LinkedHashMap.class, httpResponse.getHeaders().getClass());
		assertTrue(httpResponse.isBodyEmpty());
	}

	@Test
	public void testStatus() {
		assertEquals(200, httpResponse.getStatus());
		httpResponse.setStatus(404);
		assertEquals(404, httpResponse.getStatus());
	}

	@Test
	public void testSetBodyStringNotNull() {
		Exception exception = assertThrows(NullPointerException.class, () -> {
			this.httpResponse.setBody((String) null);
		});

		assertEquals("Content cannot be null!", exception.getMessage());
	}

	@Test
	public void testSetBodyInputStreamNotNull() {
		Exception exception = assertThrows(NullPointerException.class, () -> {
			this.httpResponse.setBody((InputStream) null);
		});

		assertEquals("InputStream cannot be null!", exception.getMessage());
	}

	@Test
	public void testSetBodyReaderNotNull() {
		Exception exception = assertThrows(NullPointerException.class, () -> {
			this.httpResponse.setBody((Reader) null);
		});

		assertEquals("Reader cannot be null!", exception.getMessage());
	}

	@Test
	public void testEmptyContentString() {
		this.httpResponse.setBody("");
		assertTrue(this.httpResponse.isBodyEmpty());
	}

	@Test
	public void testEmptyGetBodyLength() {
		this.httpResponse.setBody("");
		assertEquals(0, this.httpResponse.getBodyLength());
	}

	@Test
	public void testNotEmptyGetBodyLength() {
		this.httpResponse.setBody("123");
		assertEquals(3, this.httpResponse.getBodyLength());
	}

	@Test
	public void testEmptyContentInputStream() {
		ByteArrayInputStream in = new ByteArrayInputStream(new byte[] {});
		this.httpResponse.setBody(in);
		assertTrue(this.httpResponse.isBodyEmpty());
	}

	@Test
	public void testEmptyContentReader() {
		StringReader reader = new StringReader("");
		this.httpResponse.setBody(reader);
		assertTrue(this.httpResponse.isBodyEmpty());
	}

	@Test
	public void testNotEmptyContent() {
		httpResponse.setBody("h");
		assertFalse(httpResponse.isBodyEmpty());
	}

	@Test
	public void testSetHeaderString() {
		this.httpResponse.setHeader("Test", "value");
		assertEquals("value", this.httpResponse.getHeaders().get("Test"));
	}

	@Test
	public void testSetHeaderNormalize() {
		this.httpResponse.setHeader("TEST", "value");
		assertEquals("value", this.httpResponse.getHeaders().get("Test"));

		this.httpResponse.setHeader("TEST-header", "value");
		assertEquals("value", this.httpResponse.getHeaders().get("Test-Header"));

		this.httpResponse.setHeader("test-header", "value");
		assertEquals("value", this.httpResponse.getHeaders().get("Test-Header"));
	}

	@Test
	public void testSetHeaderNameNotNull() {
		Exception exception = assertThrows(NullPointerException.class, () -> {
			this.httpResponse.setHeader(null, "value");
		});

		assertEquals("Name cannot be null!", exception.getMessage());
	}

	@Test
	public void testSetHeaderValueNotNull() {
		Exception exception = assertThrows(NullPointerException.class, () -> {
			this.httpResponse.setHeader("name", null);
		});

		assertEquals("Value cannot be null!", exception.getMessage());
	}

	@Test
	public void testSetHeaderInt() {
		this.httpResponse.setHeader("Test", 1);
		assertEquals("1", this.httpResponse.getHeaders().get("Test"));
	}

	@Test
	public void testSetHeaderDouble() {
		this.httpResponse.setHeader("Test", 1.2);
		assertEquals("1.2", this.httpResponse.getHeaders().get("Test"));
	}

	@Test
	public void testSetHeaderBoolean() {
		this.httpResponse.setHeader("Test", true);
		assertEquals("true", this.httpResponse.getHeaders().get("Test"));
	}

	@Test
	public void testSetHeaderDate() {
		long time = 1594932497000L;
		this.httpResponse.setHeader("Test", new Date(time).getTime());
		assertEquals("1594932497000", this.httpResponse.getHeaders().get("Test"));
	}

	@Test
	public void testSetHeaderFileTime() {
		long time = 1594932497000L;
		this.httpResponse.setHeader("Test", FileTime.from(time, TimeUnit.MILLISECONDS).toMillis());
		assertEquals("1594932497000", this.httpResponse.getHeaders().get("Test"));
	}

	@Test
	public void testGetBody() {
		this.httpResponse.setBody("A");
		assertArrayEquals(new byte[] { 65 }, this.httpResponse.getBody());
		assertEquals(1, this.httpResponse.getBody().length);
	}

	@Test
	public void testGetEmptyBody() {
		this.httpResponse.setBody("");
		assertArrayEquals(new byte[] {}, this.httpResponse.getBody());
		assertEquals(0, this.httpResponse.getBody().length);
	}

	@Test
	public void testSetBodyInputStreamIOException() throws IOException {
		final IOException cause = new IOException("Test");
		InputStream in = mock(InputStream.class);
		when(in.read(any(byte[].class))).thenThrow(cause);

		Exception exception = assertThrows(HttpServerException.class, () -> {
			this.httpResponse.setBody(in);
		});

		assertEquals("Setting http response body from InputStream failed: Test", exception.getMessage());
	}

	@Test
	public void testSetBodyInputReaderIOException() throws IOException {
		final IOException cause = new IOException("Test");
		Reader reader = mock(Reader.class);
		when(reader.read(any(char[].class))).thenThrow(cause);

		Exception exception = assertThrows(HttpServerException.class, () -> {
			this.httpResponse.setBody(reader);
		});

		assertEquals("Setting http response body from Reader failed: Test", exception.getMessage());
	}
}
