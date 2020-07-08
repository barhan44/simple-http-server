package io.barhan.http_server.exception;

public class BadRequestException extends AbstractRequestParseFailedException {
	private static final long serialVersionUID = 674832111066535766L;

	public BadRequestException(String message, Throwable cause, String firstLine) {
		super(message, cause, firstLine);
		this.setStatusCode(400);
	}

}
