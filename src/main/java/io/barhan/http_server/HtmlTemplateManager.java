package io.barhan.http_server;

import java.util.Map;

public interface HtmlTemplateManager {
	String processTemplate(String templateName, Map<String, Object> arguments);
}
