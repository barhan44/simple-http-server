package io.barhan.http_server;

public class ServerInfo {
	private String name;
	private int port;
	private int threadCount;

	public ServerInfo(String name, int port, int threadCount) {
		this.name = name;
		this.port = port;
		this.threadCount = threadCount;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getPort() {
		return this.port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getThreadCount() {
		return this.threadCount;
	}

	public void setThreadCount(int threadCount) {
		this.threadCount = threadCount;
	}

	@Override
	public String toString() {
		return String.format("ServerInfo [name=%s, port=%s, threadCount=%s]", this.name, this.port, this.threadCount);
	}

}
