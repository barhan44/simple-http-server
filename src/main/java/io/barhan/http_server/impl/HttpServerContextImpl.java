package io.barhan.http_server.impl;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Properties;

import javax.sql.DataSource;

import io.barhan.http_server.Constants;
import io.barhan.http_server.HtmlTemplateManager;
import io.barhan.http_server.HttpServerContext;
import io.barhan.http_server.ServerInfo;

class HttpServerContextImpl extends AbstractHttpConfigurableComponent implements HttpServerContext {

	HttpServerContextImpl(HttpServerConfigImpl httpServerConfig) {
		super(httpServerConfig);
	}

	private HttpServerConfigImpl getHttpServerConfig() {
		return (HttpServerConfigImpl) httpServerConfig;
	}

	@Override
	public ServerInfo getServerInfo() {
		return this.getHttpServerConfig().getServerInfo();
	}

	@Override
	public Collection<String> getSupportedRequestMethods() {
		return Constants.ALLOWED_METHODS;
	}

	@Override
	public Properties getSupportedResponseMethods() {
		Properties props = new Properties();
		props.putAll(this.getHttpServerConfig().getStatusProperties());
		return props;
	}

	@Override
	public DataSource getDataSource() {
		return null;
	}

	@Override
	public Path getRootPath() {
		return this.getHttpServerConfig().createRootPath();
	}

	@Override
	public String getContentType(String extension) {
		String result = this.getHttpServerConfig().getMimeTypesPropeties().getProperty(extension);
		return result != null ? result : "text/plain";
	}

	@Override
	public HtmlTemplateManager getHtmlTemplateManager() {
		return this.getHttpServerConfig().getHtmlTemplateManager();
	}

	@Override
	public Integer getExpiresDaysForResource(String extension) {
		if (this.getHttpServerConfig().getStaticExpiresExtensions().contains(extension)) {
			return this.getHttpServerConfig().getStaticExpiresDays();
		} else {
			return null;
		}
	}

}
