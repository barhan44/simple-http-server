package io.barhan.http_server.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyMapOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Date;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import io.barhan.http_server.HtmlTemplateManager;
import io.barhan.http_server.HttpHandler;
import io.barhan.http_server.HttpRequest;
import io.barhan.http_server.HttpResponse;
import io.barhan.http_server.HttpServerContext;
import io.barhan.http_server.config.ReadableHttpResponse;

public class HttpHandlerImplTest {
	private HttpServerContext context;
	private HttpRequest request;
	private HttpResponse response;

	private HttpHandler httpHandler;

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Before
	public void before() {
		this.context = mock(HttpServerContext.class);
		this.request = mock(HttpRequest.class);
		this.response = mock(ReadableHttpResponse.class);
		this.httpHandler = new HttpHandlerImpl();
	}

	@Test
	public void testNotFound() throws IOException {
		Path root = Paths.get(this.folder.newFolder("root").toURI());
		when(this.context.getRootPath()).thenReturn(root);
		when(this.request.getURI()).thenReturn("/not-found.file");

		this.httpHandler.handle(this.context, this.request, this.response);

		verify(this.response).setStatus(404);
		verify(this.response, never()).setBody(any(InputStream.class));
		verify(this.response, never()).setBody(any(Reader.class));
		verify(this.response, never()).setBody(anyString());
		verify(this.response, never()).setHeader(anyString(), any(Object.class));
	}

	@Test
	public void testFileUri() throws IOException {
		Path root = Paths.get(this.folder.newFolder("root").toURI());
		File file = new File(root.toFile(), "file.css");
		Files.write(Paths.get(file.toURI()), "test css".getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
		when(this.context.getContentType("css")).thenReturn("text/css");
		when(this.context.getExpiresDaysForResource("css")).thenReturn(7);
		when(this.context.getRootPath()).thenReturn(root);
		when(this.request.getURI()).thenReturn("/file.css");

		this.httpHandler.handle(this.context, this.request, this.response);

		verify(this.response).setHeader("Content-Type", "text/css");
		verify(this.response).setHeader("Last-Modified",
				Files.getLastModifiedTime(Paths.get(file.toURI()), LinkOption.NOFOLLOW_LINKS));
		verify(this.response).setHeader(eq("Expires"), any(Date.class));
		verify(this.response).setBody(any(InputStream.class));

		verify(this.response, never()).setStatus(anyInt());
		verify(this.response, never()).setBody(any(Reader.class));
		verify(this.response, never()).setBody(anyString());
	}

	@Test
	public void testFileWithoutExpiresUri() throws IOException {
		Path root = Paths.get(this.folder.newFolder("root").toURI());
		File file = new File(root.toFile(), "file.css");
		Files.write(Paths.get(file.toURI()), "test css".getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
		when(this.context.getContentType("css")).thenReturn("text/css");
		when(this.context.getExpiresDaysForResource("css")).thenReturn(null);
		when(this.context.getRootPath()).thenReturn(root);
		when(this.request.getURI()).thenReturn("/file.css");

		this.httpHandler.handle(this.context, this.request, this.response);

		verify(this.response).setHeader("Content-Type", "text/css");
		verify(this.response).setHeader("Last-Modified",
				Files.getLastModifiedTime(Paths.get(file.toURI()), LinkOption.NOFOLLOW_LINKS));
		verify(this.response).setBody(any(InputStream.class));

		verify(this.response, never()).setHeader(eq("Expires"), any(Date.class));
		verify(this.response, never()).setStatus(anyInt());
		verify(this.response, never()).setBody(any(Reader.class));
		verify(this.response, never()).setBody(anyString());
	}

	@Test
	public void testDirectoryUri() throws IOException {
		HtmlTemplateManager htmlTemplateManager = mock(HtmlTemplateManager.class);
		when(this.context.getHtmlTemplateManager()).thenReturn(htmlTemplateManager);
		when(htmlTemplateManager.processTemplate(eq("list.html"), anyMapOf(String.class, Object.class)))
				.thenReturn("result");
		Path root = Paths.get(this.folder.newFolder("root").toURI());
		File dir = new File(root.toFile(), "dir");
		dir.mkdirs();
		Files.write(Paths.get(dir.getAbsolutePath() + "/test.css"), "test css".getBytes(StandardCharsets.UTF_8),
				StandardOpenOption.CREATE);
		Files.write(Paths.get(dir.getAbsolutePath() + "/test.js"), "test js".getBytes(StandardCharsets.UTF_8),
				StandardOpenOption.CREATE);

		when(this.context.getContentType("css")).thenReturn("text/css");
		when(this.context.getRootPath()).thenReturn(root);
		when(this.request.getURI()).thenReturn("/dir");

		this.httpHandler.handle(this.context, this.request, this.response);

		verify(htmlTemplateManager).processTemplate(eq("list.html"), anyMapOf(String.class, Object.class));

		verify(this.response).setBody(anyString());

		verify(this.response, never()).setHeader(anyString(), any(Object.class));
		verify(this.response, never()).setStatus(anyInt());
		verify(this.response, never()).setBody(any(Reader.class));
		verify(this.response, never()).setBody(any(InputStream.class));
	}
}
