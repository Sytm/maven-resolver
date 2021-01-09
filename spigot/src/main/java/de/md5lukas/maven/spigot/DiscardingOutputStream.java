package de.md5lukas.maven.spigot;

import java.io.IOException;
import java.io.OutputStream;

final class DiscardingOutputStream extends OutputStream {

    private boolean closed = false;

    private void makeSureNotClosed() throws IOException {
        if (closed) {
            throw new IOException("Stream closed");
        }
    }

    @Override
    public void write(int b) throws IOException {
        makeSureNotClosed();
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        makeSureNotClosed();
        if (off < 0 || len < 0 || off + len > b.length) {
            throw new IndexOutOfBoundsException("Array-Length: " + b.length + " Offset: " + off + " Length: " + len);
        }
    }

    @Override
    public void flush() throws IOException {
        makeSureNotClosed();
    }

    @Override
    public void close() {
        closed = true;
    }
}
