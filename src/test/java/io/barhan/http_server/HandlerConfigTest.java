package io.barhan.http_server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import io.barhan.http_server.exception.HttpServerConfigException;

public class HandlerConfigTest {
	private HandlerConfig config;
	private HttpHandler httpHandlerStub;

	@Before
	public void before() {
		this.config = new HandlerConfig();
		this.httpHandlerStub = mock(HttpHandler.class);
	}

	@Test
	public void testAddHandler() {
		this.config.addHandler("/test", this.httpHandlerStub);
		Map<String, HttpHandler> map = this.config.toMap();

		assertSame(this.httpHandlerStub, map.get("/test"));
	}

	@Test
	public void testUnmodificableMap() {
		this.config.addHandler("/test", this.httpHandlerStub);
		Map<String, HttpHandler> map = config.toMap();
		Exception exception = assertThrows(UnsupportedOperationException.class, () -> {
			map.clear();
		});
		assertEquals(UnsupportedOperationException.class, exception.getClass());
	}

	@Test
	public void testRequiredUrl() {
		Exception exception = assertThrows(NullPointerException.class, () -> {
			config.addHandler(null, this.httpHandlerStub);
		});
		assertEquals(NullPointerException.class, exception.getClass());
	}

	@Test
	public void testRequiredHandler() {
		Exception exception = assertThrows(NullPointerException.class, () -> {
			config.addHandler("/test", null);
		});
		assertEquals(NullPointerException.class, exception.getClass());
	}

	@Test
	public void testAddHandlerWithError() {
		Exception exception = assertThrows(HttpServerConfigException.class, () -> {
			config.addHandler("/test", this.httpHandlerStub);
			config.addHandler("/test", this.httpHandlerStub);
		});

		assertEquals(HttpServerConfigException.class, exception.getClass());
		assertEquals("Http handler already exists for url=/test. Http handler class: "
				+ this.httpHandlerStub.getClass().getName(), exception.getMessage());
	}
}
