package io.barhan.http_server.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.barhan.http_server.Constants;
import io.barhan.http_server.HttpRequest;
import io.barhan.http_server.config.HttpRequestParser;
import io.barhan.http_server.exception.BadRequestException;
import io.barhan.http_server.exception.HttpServerException;
import io.barhan.http_server.exception.HttpVersionNotSupportedException;
import io.barhan.http_server.exception.MethodNotAllowedException;
import io.barhan.http_server.utils.DataUtils;
import io.barhan.http_server.utils.HttpUtils;

class HttpRequestParserImpl implements HttpRequestParser {

	@Override
	public HttpRequest parseHttpRequest(InputStream inputStream, String remoteAddress)
			throws HttpServerException, IOException {
		String firstLine = null;
		try {
			ParsedRequest request = this.parseInputStream(inputStream);
			return this.convertParsedRequestToHttpRequest(request, remoteAddress);
		} catch (RuntimeException e) {
			if (e instanceof HttpServerException) {
				throw e;
			} else {
				throw new BadRequestException("Error on parsing HTTP request: " + e.getMessage(), e, firstLine);
			}
		}
	}

	public HttpRequest convertParsedRequestToHttpRequest(ParsedRequest request, String remoteAddress)
			throws IOException {
		String[] firstLineData = request.firstLine.split(" ");
		String method = firstLineData[0];
		String uri = firstLineData[1];
		String httpVersion = firstLineData[2];
		validateHttpVersion(request.firstLine, httpVersion);
		Map<String, String> headers = this.parseHeaders(request.headersLine);
		ProcessedUri processedUri = this.extractParamsIfPresent(method, uri, httpVersion, request.messageBody);
		return new HttpRequestImpl(method, processedUri.uri, httpVersion, remoteAddress, headers, processedUri.params);
	}

	private void validateHttpVersion(String firstLine, String httpVersion) {
		if (!Constants.SUPPORTED_HTTP_VERSION.equals(httpVersion)) {
			throw new HttpVersionNotSupportedException(
					"This server only supports " + Constants.SUPPORTED_HTTP_VERSION + " protocol", firstLine);
		}
	}

	private Map<String, String> parseHeaders(List<String> list) throws IOException {
		Map<String, String> map = new LinkedHashMap<>();
		String prevName = null;
		for (String headerItem : list) {
			prevName = this.putHeader(prevName, map, headerItem);
		}
		return map;
	}

	private String putHeader(String prevName, Map<String, String> map, String header) {
		if (header.charAt(0) == ' ') {
			String value = map.get(prevName) + header.trim();
			map.put(prevName, value);
			return prevName;
		} else {
			int index = header.indexOf(':');
			String name = HttpUtils.normalizeHeaderName(header.substring(0, index));
			String value = header.substring(index + 1).trim();
			map.put(name, value);
			return name;
		}
	}

	private ParsedRequest parseInputStream(InputStream inputStream) throws IOException {
		String firstLineAndHeaders = HttpUtils.readFirstLineAndHeaders(inputStream);
		int contentLengthIndex = HttpUtils.getContentLengthIndex(firstLineAndHeaders);
		if (contentLengthIndex != -1) {
			int contentLength = HttpUtils.getContentLengthValue(firstLineAndHeaders, contentLengthIndex);
			String messageBody = HttpUtils.readMessageBody(inputStream, contentLength);
			return new ParsedRequest(firstLineAndHeaders, messageBody);
		} else {
			return new ParsedRequest(firstLineAndHeaders, null);
		}
	}

	private ProcessedUri extractParamsIfPresent(String method, String uri, String httpVersion, String messageBody)
			throws IOException {
		Map<String, String> params = Collections.emptyMap();
		if (Constants.GET.equalsIgnoreCase(method) || Constants.HEAD.equalsIgnoreCase(method)) {
			int indexOfDelimeter = uri.indexOf('?');
			if (indexOfDelimeter != -1) {
				return extractParamsFromUri(uri, indexOfDelimeter);
			}
		} else if (Constants.POST.equalsIgnoreCase(method)) {
			if (messageBody != null && !"".equals(messageBody)) {
				params = this.getParams(messageBody);
			}
		} else {
			throw new MethodNotAllowedException(method, String.format("%s %s %s", method, uri, httpVersion));
		}
		return new ProcessedUri(uri, params);
	}

	private ProcessedUri extractParamsFromUri(String uri, int indexOfDelimeter) throws UnsupportedEncodingException {
		String paramString = uri.substring(indexOfDelimeter + 1);
		Map<String, String> params = this.getParams(paramString);
		uri = uri.substring(0, indexOfDelimeter);
		return new ProcessedUri(uri, params);
	}

	private Map<String, String> getParams(String paramString) throws UnsupportedEncodingException {
		Map<String, String> map = new HashMap<>();
		String[] params = paramString.split("&");
		for (String param : params) {
			String[] items = param.split("=");
			if (items.length == 1) {
				items = new String[] { items[0], "" };
			}
			String name = items[0];
			String value = map.get(name);
			if (value != null) {
				value += "," + URLDecoder.decode(items[1], "UTF-8");
			} else {
				value = URLDecoder.decode(items[1], "UTF-8");
			}
			map.put(name, value);
		}
		return map;
	}

	private static class ParsedRequest {
		private final String firstLine;
		private final List<String> headersLine;
		private final String messageBody;

		public ParsedRequest(String firstLineAndHeaders, String messageBody) {
			List<String> list = DataUtils.convertToLineList(firstLineAndHeaders);
			this.firstLine = list.remove(0);
			if (list.isEmpty()) {
				this.headersLine = Collections.emptyList();
			} else {
				this.headersLine = Collections.unmodifiableList(list);
			}
			this.messageBody = messageBody;
		}
	}

	private static class ProcessedUri {
		final String uri;
		final Map<String, String> params;

		public ProcessedUri(String uri, Map<String, String> params) {
			this.uri = uri;
			this.params = params;
		}
	}
}
