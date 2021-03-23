package org.rodman.framework.server;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.sun.istack.internal.NotNull;

/**
 * @author 余勇
 * @date 2021年03月23日 12:55:00
 */
public class ServerInputStream extends ServletInputStream {

	private final InputStream delegate;

	public ServerInputStream(ByteArrayInputStream inputStream) {
		this.delegate = inputStream;
	}

	public boolean isFinished() {
		return false;
	}

	public boolean isReady() {
		return true;
	}

	public void setReadListener(ReadListener readListener) {
		throw new UnsupportedOperationException();
	}

	public int read() throws IOException {
		return this.delegate.read();
	}

	public int read(@NotNull byte[] b, int off, int len) throws IOException {
		return this.delegate.read(b, off, len);
	}

	public int read(@NotNull byte[] b) throws IOException {
		return this.delegate.read(b);
	}

	public long skip(long n) throws IOException {
		return this.delegate.skip(n);
	}

	public int available() throws IOException {
		return this.delegate.available();
	}

	public void close() throws IOException {
		this.delegate.close();
	}

	public synchronized void mark(int readlimit) {
		this.delegate.mark(readlimit);
	}

	public synchronized void reset() throws IOException {
		this.delegate.reset();
	}

	public boolean markSupported() {
		return this.delegate.markSupported();
	}

}
