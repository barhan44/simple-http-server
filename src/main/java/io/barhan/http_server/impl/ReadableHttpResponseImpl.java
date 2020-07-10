package io.barhan.http_server.impl;

import java.io.InputStream;
import java.io.Reader;
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

	@Override
	public void setStatus(int status) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setHeader(String name, Object value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setBody(String content) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setBody(InputStream in) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setBody(Reader reader) {
		// TODO Auto-generated method stub
		
	}

}
