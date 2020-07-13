package io.barhan.http_server.handler;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import io.barhan.http_server.Constants;
import io.barhan.http_server.HttpHandler;
import io.barhan.http_server.HttpRequest;
import io.barhan.http_server.HttpResponse;
import io.barhan.http_server.HttpServerContext;
import io.barhan.http_server.utils.DataUtils;

public class ServerInfoHttpHandler implements HttpHandler {

	@Override
	public void handle(HttpServerContext context, HttpRequest request, HttpResponse response) throws IOException {
		if (Constants.GET.equals(request.getMethod())) {
			Map<String, Object> args = this.getDataMap(context);
			response.setBody(context.getHtmlTemplateManager().processTemplate("server-info.html", args));
		} else {
			response.setStatus(400);
		}

	}

	private Map<String, Object> getDataMap(HttpServerContext context) {
		int threadCount = context.getServerInfo().getThreadCount();
		return DataUtils.buildMap(new Object[][] { { "SERVER-NAME", context.getServerInfo().getName() },
				{ "SERVER-PORT", context.getServerInfo().getPort() },
				{ "THREAD-COUNT", threadCount == 0 ? "UNLIMITED" : threadCount },
				{ "SUPPORTED-REQUEST-METHODS", context.getSupportedRequestMethods() },
				{ "SUPPORTED-RESPONSE-STATUSES", this.getSupportedResponseStatuses(context) }, });
	}

	private StringBuilder getSupportedResponseStatuses(HttpServerContext context) {
		StringBuilder sb = new StringBuilder();
		Map<Object, Object> map = new TreeMap<>(context.getSupportedResponseMethods());
		for (Map.Entry<Object, Object> entry : map.entrySet()) {
			sb.append(entry.getKey()).append(" [").append(entry.getValue()).append("]<br>");
		}
		return sb;
	}

}
