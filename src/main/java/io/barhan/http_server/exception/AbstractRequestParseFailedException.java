package io.barhan.http_server.exception;

public abstract class AbstractRequestParseFailedException extends HttpServerException {
	private static final long serialVersionUID = 5853534902045418931L;
	private final String firstLine;

	public AbstractRequestParseFailedException(String message, Throwable cause, String firstLine) {
		super(message, cause);
		this.firstLine = firstLine;
	}

	public AbstractRequestParseFailedException(String message, String firstLine) {
		super(message);
		this.firstLine = firstLine;
	}

	public String getFirstLine() {
		return this.firstLine;
	}

}
