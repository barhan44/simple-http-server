package io.barhan.http_server.exception;

public class HttpVersionNotSupportedException extends AbstractRequestParseFailedException {
	private static final long serialVersionUID = -7027497639945032084L;

	public HttpVersionNotSupportedException(String message, String firstLine) {
		super(message, firstLine);
		this.setStatusCode(505);
	}

}
