package io.barhan.http_server.exception;

import io.barhan.http_server.Constants;

public class MethodNotAllowedException extends AbstractRequestParseFailedException {
	private static final long serialVersionUID = -220360874345776668L;

	public MethodNotAllowedException(String method, String firstLine) {
		super("Only " + Constants.ALLOWED_METHODS + " are supported. But the current method is " + method, firstLine);
		this.setStatusCode(405);
	}
}
