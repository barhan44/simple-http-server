package io.barhan.http_server.impl;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import io.barhan.http_server.HttpServer;

public class HttpServerFactoryImplTest {
	private HttpServerFactory httpServerFactory;

	@Before
	public void before() {
		this.httpServerFactory = HttpServerFactory.create();
	}

	@Test
	public void testCreate() {
		assertEquals(HttpServerFactory.class, this.httpServerFactory.getClass());
	}

	@Test
	public void testCreateHttpServer() {
		HttpServer server = this.httpServerFactory.createHttpServer(null, null);
		assertEquals(HttpServerImpl.class, server.getClass());
	}
}
