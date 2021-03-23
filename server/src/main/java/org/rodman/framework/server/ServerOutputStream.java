package org.rodman.framework.server;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import java.io.ByteArrayOutputStream;

/**
 * @author 余勇
 * @date 2021年03月23日 13:11:00
 */
public class ServerOutputStream extends ServletOutputStream {

	protected final ByteArrayOutputStream buf = new ByteArrayOutputStream();

	public ServerOutputStream() {
	}

	public byte[] toByteArray() {
		return this.buf.toByteArray();
	}

	public void write(int b) {
		this.buf.write(b);
	}

	public boolean isReady() {
		return false;
	}

	public void setWriteListener(WriteListener listener) {
	}

	public void reset() {
		this.buf.reset();
	}
}
