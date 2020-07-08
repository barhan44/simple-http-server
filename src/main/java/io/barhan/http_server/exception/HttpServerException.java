package io.barhan.http_server.exception;

public class HttpServerException extends RuntimeException {
	private static final long serialVersionUID = -7473604756796548871L;
	private int statusCode = 500;

	public HttpServerException(String message, Throwable cause) {
		super(message, cause);
	}

	public HttpServerException(String message) {
		super(message);
	}

	public HttpServerException(Throwable cause) {
		super(cause);
	}

	public int getStatusCode() {
		return this.statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

}
