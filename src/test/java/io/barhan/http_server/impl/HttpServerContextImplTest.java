package io.barhan.http_server.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import io.barhan.http_server.HttpServerContext;

public class HttpServerContextImplTest {
	private HttpServerContext httpServerContext;
	private HttpServerConfigImpl httpServerConfig;

	@Before
	public void before() {
		this.httpServerConfig = spy(new HttpServerConfigImpl(null, null));
		this.httpServerContext = new HttpServerContextImpl(this.httpServerConfig);
	}

	@Test
	public void testGetServerInfo() {
		this.httpServerContext.getServerInfo();
		verify(this.httpServerConfig).getServerInfo();
	}

	@Test
	public void testGetRootPath() {
		this.httpServerContext.getRootPath();
		verify(this.httpServerConfig).createRootPath();
	}

	@Test
	public void testGetHtmlTemplateManager() {
		this.httpServerContext.getHtmlTemplateManager();
		verify(this.httpServerConfig).getHtmlTemplateManager();
	}

	@Test
	public void testGetSupportedMethods() {
		assertEquals(Arrays.asList(new String[] { "GET", "POST", "HEAD" }),
				this.httpServerContext.getSupportedRequestMethods());
	}

	@Test
	public void testGetExpiresDaysForResource() {
		when(this.httpServerConfig.getStaticExpiresDays()).thenReturn(7);
		when(this.httpServerConfig.getStaticExpiresExtensions()).thenReturn(Collections.singletonList("css"));

		Integer expires = this.httpServerContext.getExpiresDaysForResource("css");
		assertEquals(Integer.valueOf(7), expires);

		expires = this.httpServerContext.getExpiresDaysForResource("js");
		assertNull(expires);
	}

	@Test
	public void testGetSupportedResponseStatuses() {
		Properties originalProperties = new Properties();
		originalProperties.setProperty("key", "value");
		when(this.httpServerConfig.getStatusProperties()).thenReturn(originalProperties);

		Properties actual = this.httpServerContext.getSupportedResponseMethods();
		assertNotSame(originalProperties, actual);
		actual.clear();
		assertEquals("value", originalProperties.getProperty("key"));
		assertEquals(1, originalProperties.size());
	}

	@Test
	public void testGetContentType() {
		Properties contentType = new Properties();
		contentType.setProperty("css", "text/css");
		when(this.httpServerConfig.getMimeTypesPropeties()).thenReturn(contentType);

		String actual = this.httpServerContext.getContentType("css");
		assertEquals("text/css", actual);
		actual = this.httpServerContext.getContentType("not-found");
		assertEquals("text/plain", actual);
	}
}
