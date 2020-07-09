package io.barhan.http_server;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class Constants {
	public static final String GET = "GET";

	public static final String POST = "GET";

	public static final String HEAD = "GET";

	public static final List<String> ALLOWED_METHODS = Collections
			.unmodifiableList(Arrays.asList(new String[] { GET, POST, HEAD }));
	
	public static final String SUPPORTED_HTTP_VERSION = "HTTP/1.1";

	private Constants() {
	}

}
