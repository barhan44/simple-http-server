package io.barhan.http_server.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ThreadFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.barhan.http_server.HtmlTemplateManager;
import io.barhan.http_server.HttpServerContext;
import io.barhan.http_server.ServerInfo;
import io.barhan.http_server.config.HttpClientSocketHandler;
import io.barhan.http_server.config.HttpRequestDispatcher;
import io.barhan.http_server.config.HttpRequestParser;
import io.barhan.http_server.config.HttpResponseBuilder;
import io.barhan.http_server.config.HttpResponseWriter;
import io.barhan.http_server.config.HttpServerConfig;
import io.barhan.http_server.exception.HttpServerConfigException;

class HttpServerConfigImpl implements HttpServerConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger(HttpServerConfigImpl.class);

	private final Properties serverProperties = new Properties();
	private final Properties statusProperties = new Properties();
	private final Properties mimeTypesProperties = new Properties();

	private final Path rootPath;
	private final HttpServerContext httpServerContext;
	private final HttpRequestParser httpRequestParser;
	private final HttpResponseWriter httpResponseWriter;
	private final HttpResponseBuilder httpResponseBuilder;
	private final HttpRequestDispatcher httpRequestDispatcher;
	private final ThreadFactory workerThreadFactory;
	private final HtmlTemplateManager htmlTemplateManager;
	private final ServerInfo serverInfo;
	private final List<String> staticExpiresExtensions;
	private final int staticExpiresDays;

	public HttpServerConfigImpl(Properties properties) {
		this.loadAllProperties(properties);
		this.rootPath = this.getRootPath();
		this.serverInfo = this.createServerInfo();
		this.staticExpiresDays = Integer.parseInt(this.serverProperties.getProperty("webapp.static.expires.days"));
		this.staticExpiresExtensions = Arrays
				.asList(this.serverProperties.getProperty("webapp.static.expires.extensions").split(","));

		this.httpServerContext = new HttpServerContextImpl(this);
		this.httpRequestParser = new HttpRequestParserImpl();
		this.httpResponseWriter = new HttpResponseWriterImpl(this);
		this.httpResponseBuilder = new HttpResponseBuilderImpl(this);
		this.httpRequestDispatcher = new TemporaryHttpRequestDispatcher();
		this.workerThreadFactory = null;
		this.htmlTemplateManager = null;
	}

	private void loadAllProperties(Properties properties) {
		ClassLoader classLoader = HttpServerConfigImpl.class.getClassLoader();
		this.loadProperties(this.statusProperties, classLoader, "status.properties");
		this.loadProperties(this.mimeTypesProperties, classLoader, "mime-types.properties");
		this.loadProperties(this.serverProperties, classLoader, "server.properties");
		if (properties != null) {
			LOGGER.info("Ovveriding default server properties");
			this.serverProperties.putAll(properties);
		}
		this.logServerProperties();
	};

	private void logServerProperties() {
		if (LOGGER.isDebugEnabled()) {
			StringBuilder res = new StringBuilder("Current server properties:\n");
			for (Map.Entry<Object, Object> entry : this.serverProperties.entrySet()) {
				res.append(entry.getKey()).append('=').append(entry.getValue()).append('\n');
			}
			LOGGER.debug(res.toString());
		}
	}

	private void loadProperties(Properties properties, ClassLoader classLoader, String resource) {
		try (InputStream in = classLoader.getResourceAsStream(resource)) {
			if (in != null) {
				properties.load(in);
				LOGGER.debug("Successful load properties from resource: {}", resource);
			} else {
				throw new HttpServerConfigException("Class path resource not found: " + resource);
			}
		} catch (IOException e) {
			throw new HttpServerConfigException("Fail on loading properties from resource: " + resource, e);
		}
	}

	private ServerInfo createServerInfo() {
		ServerInfo si = new ServerInfo(this.serverProperties.getProperty("server.name"),
				Integer.parseInt(this.serverProperties.getProperty("server.port")),
				Integer.parseInt(this.serverProperties.getProperty("server.thread.count")));
		if (si.getThreadCount() < 0) {
			throw new HttpServerConfigException("server.thread.count should be >= 0, where 0 is UNLIMITED threads");
		}
		return si;
	}

	protected Path getRootPath() {
		Path path = Paths
				.get(new File(this.serverProperties.getProperty("webapp.static.dir.root")).getAbsoluteFile().toURI());
		if (!Files.exists(path)) {
			throw new HttpServerConfigException("Root path not found: " + path.toString());
		}
		if (!Files.isDirectory(path)) {
			throw new HttpServerConfigException("Root path is not directory: " + path.toString());
		}
		LOGGER.info("Root path is {}", path.toAbsolutePath());
		return path;
	}

	protected Properties getMimeTypesPropeties() {
		return this.mimeTypesProperties;
	}

	protected HtmlTemplateManager getHtmlTemplateManager() {
		return this.htmlTemplateManager;
	}

	protected List<String> getStaticExpiresExtensions() {
		return this.staticExpiresExtensions;
	}

	protected int getStaticExpiresDays() {
		return this.staticExpiresDays;
	}

	protected Properties getStatusProperties() {
		return this.statusProperties;
	}

	@Override
	public ServerInfo getServerInfo() {
		return this.serverInfo;
	}

	@Override
	public String getStatusMessage(int statusCode) {
		String message = this.statusProperties.getProperty(String.valueOf(statusCode));
		return message != null ? message : statusProperties.getProperty("500");
	}

	@Override
	public HttpRequestParser getHttpRequestParser() {
		return this.httpRequestParser;
	}

	@Override
	public HttpResponseBuilder getHttpResponseBuilder() {
		return this.httpResponseBuilder;
	}

	@Override
	public HttpResponseWriter getHttpResponseWriter() {
		return this.httpResponseWriter;
	}

	@Override
	public HttpServerContext getHttpServerContext() {
		return this.httpServerContext;
	}

	@Override
	public HttpRequestDispatcher getHttpRequestDispatcher() {
		return this.httpRequestDispatcher;
	}

	@Override
	public ThreadFactory getWorkerThreadFactory() {
		return this.workerThreadFactory;
	}

	@Override
	public HttpClientSocketHandler buildNewHttpClientSocketHandler(Socket clientSocket) {
		return new HttpClientSocketHandlerImpl(clientSocket, this);
	}

}
