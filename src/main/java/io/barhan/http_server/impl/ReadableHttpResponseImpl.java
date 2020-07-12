package io.barhan.http_server.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.io.IOUtils;

import io.barhan.http_server.config.ReadableHttpResponse;
import io.barhan.http_server.exception.HttpServerException;
import io.barhan.http_server.utils.HttpUtils;

class ReadableHttpResponseImpl implements ReadableHttpResponse {
	private final Map<String, String> headers;
	private byte[] body;
	private int status;

	protected ReadableHttpResponseImpl() {
		this.status = 200;
		this.headers = new LinkedHashMap<>();
		this.body = new byte[0];
	}

	@Override
	public int getStatus() {
		return this.status;
	}

	@Override
	public Map<String, String> getHeaders() {
		return this.headers;
	}

	@Override
	public byte[] getBody() {
		return this.body;
	}

	@Override
	public boolean isBodyEmpty() {
		return this.getBodyLength() == 0;
	}

	@Override
	public int getBodyLength() {
		return this.body.length;
	}

	@Override
	public void setStatus(int status) {
		this.status = status;
	}

	@Override
	public void setHeader(String name, Object value) {
		Objects.requireNonNull(name, "Name cannot be null!");
		Objects.requireNonNull(value, "Value cannot be null!");
		name = HttpUtils.normalizeHeaderName(name);
		if (value instanceof Date) {
			this.headers.put(name, new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z").format(value));
		} else if (value instanceof FileTime) {
			this.headers.put(name, new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z")
					.format(new Date(((FileTime) value).toMillis())));
		} else {
			this.headers.put(name, String.valueOf(value));
		}
	}

	@Override
	public void setBody(String content) {
		Objects.requireNonNull(content, "Content cannot be null!");
		this.body = content.getBytes(StandardCharsets.UTF_8);
	}

	@Override
	public void setBody(InputStream in) {
		try {
			Objects.requireNonNull(in, "InputStream cannot be null!");
			this.body = IOUtils.toByteArray(in);
		} catch (IOException e) {
			throw new HttpServerException("Setting http response body from InputStream failed: " + e.getMessage(), e);
		}

	}

	@Override
	public void setBody(Reader reader) {
		try {
			Objects.requireNonNull(reader, "Reader cannot be null!");
			this.body = IOUtils.toByteArray(reader, StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new HttpServerException("Setting http response body from Reader failed: " + e.getMessage(), e);
		}

	}

}
