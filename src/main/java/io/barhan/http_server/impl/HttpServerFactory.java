package io.barhan.http_server.impl;

import java.util.Properties;

import io.barhan.http_server.HttpServer;

public class HttpServerFactory {
	protected HttpServerFactory() {};
	
	public static HttpServerFactory create() {
		return new HttpServerFactory();
	}
	
	public HttpServer createHttpServer(Properties serverProperties) {
		return new HttpServer() {

			@Override
			public void start() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void stop() {
				// TODO Auto-generated method stub
				
			}
			
		};
	}
}
