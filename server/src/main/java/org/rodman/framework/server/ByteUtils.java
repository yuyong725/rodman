package org.rodman.framework.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unchecked")
public class ByteUtils {

	public static byte[] getBytes(InputStream inputStream, Integer length) {
		if (length < 1) {
			return null;
		}
		if (inputStream == null) {
			return null;
		}
		int company = ServerConfig.maxHeaderLength;
		if (length < company) {
			company = length;
		}
		ByteArrayOutputStream outputStream = null;
		try {
			outputStream = new ByteArrayOutputStream();
			byte[] buff = new byte[company];
			int totalReadLength = 0;
			while (totalReadLength < length) {
				int rcLength = inputStream.read(buff);
				if (rcLength == 0) {
					TimeUnit.MICROSECONDS.sleep(1);
					break;
				}
				totalReadLength += rcLength;
				outputStream.write(buff, 0, rcLength);
				if (rcLength < buff.length) {
					break;
				}
			}
			return outputStream.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				outputStream.close();
			} catch (IOException e) {
			}
		}
	}

	public static <T extends OutputStream> T buildToOutputStream(InputStream inputStream, OutputStream outputStream) {
		if (outputStream == null) {
			outputStream = new ByteArrayOutputStream();
		}
		try {
			outputStream = new ByteArrayOutputStream();
			byte[] buff = new byte[1024];
			int rc = 0;
			while ((rc = inputStream.read(buff, 0, 1024)) > 0) {
				outputStream.write(buff, 0, rc);
			}
			return (T) outputStream;
		} catch (Exception e) {

			return null;
		}
	}
}
