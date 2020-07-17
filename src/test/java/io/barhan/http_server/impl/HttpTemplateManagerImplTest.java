package io.barhan.http_server.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;

import io.barhan.http_server.exception.HttpServerException;
import io.barhan.http_server.utils.DataUtils;

public class HttpTemplateManagerImplTest {
	private HtmlTemplateManagerImpl htmlTemplateManager;

	@Before
	public void before() {
		this.htmlTemplateManager = spy(HtmlTemplateManagerImpl.class);
	}

	private void setTemplate(String templateName, String templateContent) {
		when(this.htmlTemplateManager.getClasspathResource("html/templates/" + templateName))
				.thenReturn(new ByteArrayInputStream(templateContent.getBytes(StandardCharsets.UTF_8)));
	}

	@Test
	public void testTemplateWithoutParams() {
		setTemplate("test", "templateContent");
		String result = this.htmlTemplateManager.processTemplate("test", new HashMap<String, Object>());
		assertEquals("templateContent", result);
	}

	@Test
	public void testTemplateWithParams() {
		setTemplate("test", "templateContent ${PARAM1} / ${PARAM1}!");
		String result = this.htmlTemplateManager.processTemplate("test",
				DataUtils.buildMap(new Object[][] { { "PARAM1", "TEST" } }));
		assertEquals("templateContent TEST / TEST!", result);
	}

	@Test
	public void testClasspathResourceNotFound() {
		Exception exception = assertThrows(HttpServerException.class, () -> {
			this.htmlTemplateManager.processTemplate("not-found", new HashMap<String, Object>());
		});
		assertEquals(HttpServerException.class, exception.getClass());
		assertEquals("Classpath resource \"html/templates/not-found\" not found!", exception.getMessage());
	}

	@Test
	public void testIOException() throws IOException {
		when(this.htmlTemplateManager.getClasspathResource("html/templates/io-error"))
				.thenReturn(mock(InputStream.class));

		Exception exception = assertThrows(HttpServerException.class, () -> {
			this.htmlTemplateManager.processTemplate("io-error", new HashMap<String, Object>());
		});

		assertEquals(HttpServerException.class, exception.getClass());
		assertEquals("Cannot load template: io-error", exception.getMessage());
	}
}
