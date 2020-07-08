package io.barhan.http_server.config;

import java.net.Socket;
import java.util.concurrent.ThreadFactory;

import io.barhan.http_server.HttpServerContext;
import io.barhan.http_server.ServerInfo;

public interface HttpServerConfig {
	ServerInfo getServerInfo();
	
	String getStatusMessage(int statusCode);
	
	HttpRequestParser getHttpRequestParser();
	
	HttpResponseBuilder getHttpResponseBuilder();
	
	HttpResponseWriter getHttpResponseWriter();
	
	HttpServerContext getHttpServerContext();
	
	HttpRequestDispatcher getHttpRequestDispatcher();
	
	ThreadFactory getWorkerThreadFactory();
	
	HttpClientSocketHandler buildNewHttpClientSocketHandler(Socket clientSocket);
}
