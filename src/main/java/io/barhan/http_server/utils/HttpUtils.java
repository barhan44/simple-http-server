package io.barhan.http_server.utils;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public final class HttpUtils {
	public static String normalizeHeaderName(String name) {
		StringBuilder headerName = new StringBuilder(name.trim());
		for (int i = 0; i < headerName.length(); i++) {
			char ch = headerName.charAt(i);
			if (i == 0) {
				toUpperCase(ch, i, headerName);
			} else if (ch == '-' && i < headerName.length() - 1) {
				toUpperCase(headerName.charAt(i + 1), i + 1, headerName);
				i++;
			} else {
				if (Character.isUpperCase(ch)) {
					headerName.setCharAt(i, Character.toLowerCase(ch));
				}
			}
		}
		return headerName.toString();
	}

	public static String readFirstLineAndHeaders(InputStream inputStream) throws IOException {
		ByteArray byteArray = new ByteArray();
		do {
			int read = inputStream.read();
			if (read == -1) {
				throw new EOFException("Input stream closed.");
			}
			byteArray.add((byte) read);
		} while (!byteArray.isEmptyLine());
		return new String(byteArray.toArray(), StandardCharsets.UTF_8);
	}

	public static String readMessageBody(InputStream inputStream, int contentLength) throws IOException {
		StringBuilder messageBody = new StringBuilder();
		while (contentLength > 0) {
			byte[] messageBytes = new byte[contentLength];
			int read = inputStream.read(messageBytes);
			messageBody.append(new String(messageBytes, 0, read, StandardCharsets.UTF_8));
			contentLength -= read;
		}
		return messageBody.toString();
	}

	public static int getContentLengthIndex(String firstLineAndHeaders) {
		return firstLineAndHeaders.toLowerCase().indexOf(CONTENT_LENGTH);
	}

	public static int getContentLengthValue(String firstLineAndHeaders, int contentLengthIndex) {
		int startCutIndex = contentLengthIndex + CONTENT_LENGTH.length();
		int endCutIndex = firstLineAndHeaders.indexOf("\r\n", startCutIndex);
		if (endCutIndex == -1) {
			endCutIndex = firstLineAndHeaders.length();
		}
		return Integer.parseInt(firstLineAndHeaders.substring(startCutIndex, endCutIndex).trim());
	}

	private static void toUpperCase(char ch, int index, StringBuilder headerName) {
		if (Character.isLowerCase(ch)) {
			headerName.setCharAt(index, Character.toUpperCase(ch));
		}
	}

	private static final String CONTENT_LENGTH = "content-length: ";

	private static class ByteArray {
		private byte[] array = new byte[1024];
		private int size;

		private void add(byte value) {
			if (size == array.length) {
				byte[] temp = array;
				array = new byte[array.length * 2];
				System.arraycopy(temp, 0, array, 0, size);
			}
			array[size++] = value;
		}

		private byte[] toArray() {
			if (size > 4) {
				return Arrays.copyOf(array, size - 4);
			} else {
				throw new IllegalStateException(
						"Byte array has invalid size: " + Arrays.toString(Arrays.copyOf(array, size)));
			}
		}

		private boolean isEmptyLine() {
			if (size >= 4) {
				return array[size - 1] == '\n' && array[size - 2] == '\r' && array[size - 3] == '\n'
						&& array[size - 4] == '\r';
			} else {
				return false;
			}
		}
	}

	private HttpUtils() {
	}
}
