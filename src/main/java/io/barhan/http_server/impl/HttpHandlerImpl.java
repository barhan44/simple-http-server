package io.barhan.http_server.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FilenameUtils;

import io.barhan.http_server.HttpHandler;
import io.barhan.http_server.HttpRequest;
import io.barhan.http_server.HttpResponse;
import io.barhan.http_server.HttpServerContext;
import io.barhan.http_server.utils.DataUtils;

class HttpHandlerImpl implements HttpHandler {

	@Override
	public void handle(HttpServerContext context, HttpRequest request, HttpResponse response) throws IOException {
		String url = request.getURI();
		Path path = Paths.get(context.getRootPath().toString() + url);
		if (Files.exists(path)) {
			if (Files.isDirectory(path)) {
				this.handleDirectoryUrl(context, response, path);
			} else {
				this.handleFileUrl(context, response, path);
			}
		} else {
			response.setStatus(404);
		}

	}

	private void handleFileUrl(HttpServerContext context, HttpResponse response, Path path) throws IOException {
		this.setEntityHeaders(context, response, path);
		try (InputStream in = Files.newInputStream(path, StandardOpenOption.READ)) {
			response.setBody(in);
		}
	}

	private void setEntityHeaders(HttpServerContext context, HttpResponse response, Path path) throws IOException {
		String extension = FilenameUtils.getExtension(path.toString());
		response.setHeader("Content-Type", context.getContentType(extension));
		response.setHeader("Last-Modified", Files.getLastModifiedTime(path, LinkOption.NOFOLLOW_LINKS));
		Integer expiresDays = context.getExpiresDaysForResource(extension);
		if (expiresDays != null) {
			response.setHeader("Expires", new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(expiresDays)));
		}
	}

	private void handleDirectoryUrl(HttpServerContext context, HttpResponse response, Path path) throws IOException {
		String content = this.getResponseForDirectory(context, path);
		response.setBody(content);
	}

	private String getResponseForDirectory(HttpServerContext context, Path dir) throws IOException {
		String root = context.getRootPath().toString();
		StringBuilder htmlBody = new StringBuilder();

		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(dir)) {
			for (Path path : directoryStream) {
				htmlBody.append("<a href=\"").append(getHref(root, path)).append("\">").append(path.getFileName())
						.append("</a><br>\r\n");
			}
		}
		Map<String, Object> args = DataUtils
				.buildMap(new Object[][] { { "TITLE", "List of files for " + dir.getFileName() },
						{ "HEADER", "List of files for " + dir.getFileName() }, { "BODY", htmlBody } });
		return context.getHtmlTemplateManager().processTemplate("simple.html", args);
	}

	private String getHref(String root, Path path) {
		return path.toString().replace(root, "");
	}

}
