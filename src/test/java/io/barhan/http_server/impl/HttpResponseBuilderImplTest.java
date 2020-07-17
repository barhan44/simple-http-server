package io.barhan.http_server.impl;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import io.barhan.http_server.HtmlTemplateManager;
import io.barhan.http_server.HttpServerContext;
import io.barhan.http_server.ServerInfo;
import io.barhan.http_server.config.HttpResponseBuilder;
import io.barhan.http_server.config.HttpServerConfig;
import io.barhan.http_server.config.ReadableHttpResponse;
import io.barhan.http_server.utils.DataUtils;

public class HttpResponseBuilderImplTest {
	private HttpResponseBuilder httpResponseBuilder;
	private HttpServerConfig httpServerConfig;

	@Before
	public void before() {
		this.httpServerConfig = mock(HttpServerConfig.class);
		this.httpResponseBuilder = new HttpResponseBuilderImpl(this.httpServerConfig);
		ServerInfo serverInfo = mock(ServerInfo.class);
		when(this.httpServerConfig.getServerInfo()).thenReturn(serverInfo);
		when(serverInfo.getName()).thenReturn("server-info-test");
	}

	@Test
	public void testBuildNewHttpResponse() {
		ReadableHttpResponse response = this.httpResponseBuilder.buildNewHttpResponse();

		assertNotNull(response.getHeaders().get("Date"));
		assertEquals("server-info-test", response.getHeaders().get("Server"));
		assertEquals("en", response.getHeaders().get("Content-Language"));
		assertEquals("close", response.getHeaders().get("Connection"));
		assertEquals("text/html", response.getHeaders().get("Content-Type"));
	}

	@Test
	public void testPrepareStatusOkBodyFoundClearFalse() {
		String content = "Content";
		ReadableHttpResponse response = this.httpResponseBuilder.buildNewHttpResponse();
		response.setBody(content);

		this.httpResponseBuilder.prepareHttpResponse(response, false);

		assertNotNull(response.getHeaders().get("Content-Length"));
		assertEquals(String.valueOf(content.length()), response.getHeaders().get("Content-Length"));
		assertArrayEquals(content.getBytes(StandardCharsets.UTF_8), response.getBody());
	}

	@Test
	public void testPrepareStatusOkBodyFoundClearTrue() {
		String content = "Content";
		ReadableHttpResponse response = this.httpResponseBuilder.buildNewHttpResponse();
		response.setBody(content);

		this.httpResponseBuilder.prepareHttpResponse(response, true);

		assertNotNull(response.getHeaders().get("Content-Length"));
		assertEquals("7", response.getHeaders().get("Content-Length"));
		assertTrue(response.isBodyEmpty());
	}

	@Test
	public void testPrepareStatus404BodyFoundClearFalse() {
		String content = "Content";
		ReadableHttpResponse response = this.httpResponseBuilder.buildNewHttpResponse();
		response.setStatus(404);
		response.setBody(content);

		this.httpResponseBuilder.prepareHttpResponse(response, false);

		assertNotNull(response.getHeaders().get("Content-Length"));
		assertEquals("7", response.getHeaders().get("Content-Length"));
		assertArrayEquals(content.getBytes(StandardCharsets.UTF_8), response.getBody());
	}

	@Test
	public void testPrepareStatus404BodyNotFoundClearFalse() {
		String content = "error.html";
		when(this.httpServerConfig.getStatusMessage(404)).thenReturn("Not Found");
		HttpServerContext context = mock(HttpServerContext.class);
		when(this.httpServerConfig.getHttpServerContext()).thenReturn(context);
		HtmlTemplateManager htmlTemplateManager = mock(HtmlTemplateManager.class);
		when(context.getHtmlTemplateManager()).thenReturn(htmlTemplateManager);
		Map<String, Object> args = DataUtils
				.buildMap(new Object[][] { { "STATUS-CODE", 404 }, { "STATUS-MESSAGE", "Not Found" } });
		when(htmlTemplateManager.processTemplate("error.html", args)).thenReturn(content);
		ReadableHttpResponse response = this.httpResponseBuilder.buildNewHttpResponse();
		response.setStatus(404);

		this.httpResponseBuilder.prepareHttpResponse(response, false);

		assertNotNull(response.getHeaders().get("Content-Length"));
		assertEquals("10", response.getHeaders().get("Content-Length"));
		assertArrayEquals(content.getBytes(StandardCharsets.UTF_8), response.getBody());
	}
}
