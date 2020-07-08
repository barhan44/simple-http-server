package io.barhan.http_server;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Properties;

import javax.sql.DataSource;

public interface HttpServerContext {
	ServerInfo getServerInfo();
	
	Collection<String> getSupportedRequestMethods();
	
	Properties getSupportedResponseMethods();
	
	DataSource getDataSource();
	
	Path getRootPath();
	
	String getContentType(String extension);
	
	HtmlTemplateManager getHtmlTemplateManager();
	
	Integer getExpiresDaysForResource(String extension);
}
