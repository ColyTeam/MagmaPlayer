package com.shirkanesi.magmaplayer.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StreamUtil {

    private static final int DEFAULT_BUFFER_SIZE = 512;

    /**
     * @param out
     * @return
     * @throws IOException
     * @see java.io.InputStream#transferTo(OutputStream)
     */
    public static long transferTo(InputStream in, OutputStream out) throws IOException {
        Objects.requireNonNull(out, "out");
        long transferred = 0;
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int read;
        while ((read = in.read(buffer, 0, DEFAULT_BUFFER_SIZE)) >= 0) {
            out.write(buffer, 0, read);
            out.flush();
            transferred += read;
        }
        return transferred;
    }

}
