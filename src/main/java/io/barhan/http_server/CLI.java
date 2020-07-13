package io.barhan.http_server;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.barhan.http_server.handler.ServerInfoHttpHandler;
import io.barhan.http_server.impl.HttpServerFactory;

public class CLI {
	private static final Logger LOGGER = LoggerFactory.getLogger(CLI.class);
	private static final List<String> QUIT_COMMANDS = Collections
			.unmodifiableList(Arrays.asList(new String[] { "q", "quit", "exit" }));

	public static void main(String[] args) {
		Thread.currentThread().setName("CLI-main-thread");
		try {
			HttpServerFactory httpServerFactory = HttpServerFactory.create();
			HttpServer httpServer = httpServerFactory.createHttpServer(getHandlerConfig(), null);
			httpServer.start();
			waitForQuit(httpServer);
		} catch (Exception e) {
			LOGGER.error("Error during server start:" + e.getMessage(), e);
		}
	}

	private static void waitForQuit(HttpServer httpServer) {
		try (Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8.name())) {
			while (true) {
				String command = scanner.nextLine();
				if (QUIT_COMMANDS.contains(command.toLowerCase())) {
					httpServer.stop();
					break;
				} else {
					LOGGER.error("This command is not supported:" + command + ". Press q for stop server");
				}
			}
		}
	}

	private static HandlerConfig getHandlerConfig() {
		return new HandlerConfig().addHandler("/info", new ServerInfoHttpHandler());
	}

}
