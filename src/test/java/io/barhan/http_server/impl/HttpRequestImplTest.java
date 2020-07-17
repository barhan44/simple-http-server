package io.barhan.http_server.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import io.barhan.http_server.HttpRequest;

public class HttpRequestImplTest {
	private Map<String, String> map;

	@Before
	public void before() {
		this.map = new HashMap<>();
		this.map.put("name", "value");
	}

	@Test
	public void testUnmodificableHeaderMap() {
		HttpRequest request = new HttpRequestImpl(null, null, null, null, this.map, this.map);

		assertThrows(UnsupportedOperationException.class, () -> {
			request.getHeaders().clear();
		});
	}

	@Test
	public void testUnmodificableParamsMap() {
		HttpRequest request = new HttpRequestImpl(null, null, null, null, this.map, this.map);

		assertThrows(UnsupportedOperationException.class, () -> {
			request.getParams().clear();
		});
	}

	@Test
	public void testGetFirstLine() {
		HttpRequest request = new HttpRequestImpl("GET", "/index.html", "HTTP/1.1", "localhost:1234", this.map,
				this.map);
		assertEquals("GET /index.html HTTP/1.1", request.getFirstLine());
	}
}
