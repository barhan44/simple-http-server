package io.barhan.http_server.impl;

import java.util.Map;

import io.barhan.http_server.config.ReadableHttpResponse;

public class ReadableHttpResponseImpl implements ReadableHttpResponse {

	@Override
	public int getStatus() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Map<String, String> getHeaders() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] getBody() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isBodyEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getBodyLength() {
		// TODO Auto-generated method stub
		return 0;
	}

}
