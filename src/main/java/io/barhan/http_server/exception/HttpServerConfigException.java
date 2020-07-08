package io.barhan.http_server.exception;

public class HttpServerConfigException extends HttpServerException {
	private static final long serialVersionUID = -2010104875955187620L;

	public HttpServerConfigException(String message, Throwable cause) {
		super(message, cause);
	}

	public HttpServerConfigException(String message) {
		super(message);
	}

	public HttpServerConfigException(Throwable cause) {
		super(cause);
	}

}
