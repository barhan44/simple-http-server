package io.barhan.http_server.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import io.barhan.http_server.HtmlTemplateManager;
import io.barhan.http_server.exception.HttpServerException;

class HtmlTemplateManagerImpl implements HtmlTemplateManager {
	private final Map<String, String> templates = new HashMap<>();

	@Override
	public String processTemplate(String templateName, Map<String, Object> arguments) {
		String template = this.getTemplate(templateName);
		return this.populateTemplate(template, arguments);
	}

	private InputStream getClasspathResource(String name) {
		return HtmlTemplateManagerImpl.class.getClassLoader().getResourceAsStream(name);
	}

	private String getTemplate(String templateName) {
		String template = this.templates.get(templateName);
		if (template == null) {
			try (InputStream in = this.getClasspathResource("html/templates/" + templateName)) {
				if (in == null) {
					throw new HttpServerException(
							"Classpath resource \"html/templates/" + templateName + "\" not found!");
				}
				template = IOUtils.toString(in, StandardCharsets.UTF_8);
				this.templates.put(templateName, template);
			} catch (IOException e) {
				throw new HttpServerException("Cannot load template: " + templateName, e);
			}
		}
		return template;
	}

	private String populateTemplate(String template, Map<String, Object> args) {
		String html = template;
		for (Map.Entry<String, Object> entry : args.entrySet()) {
			html = html.replace("${" + entry.getKey() + "}", String.valueOf(entry.getValue()));
		}
		return html;
	}

}
