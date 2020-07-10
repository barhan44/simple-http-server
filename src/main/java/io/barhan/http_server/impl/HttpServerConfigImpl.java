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

		this.httpServerContext = null;
		this.httpRequestParser = new HttpRequestParserImpl();
		this.httpResponseWriter = null;
		this.httpResponseBuilder = null;
		this.httpRequestDispatcher = null;
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

	private Path getRootPath() {
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

	@Override
	public ServerInfo getServerInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getStatusMessage(int statusCode) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpRequestParser getHttpRequestParser() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpResponseBuilder getHttpResponseBuilder() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpResponseWriter getHttpResponseWriter() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpServerContext getHttpServerContext() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpRequestDispatcher getHttpRequestDispatcher() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ThreadFactory getWorkerThreadFactory() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpClientSocketHandler buildNewHttpClientSocketHandler(Socket clientSocket) {
		// TODO Auto-generated method stub
		return null;
	}

}
