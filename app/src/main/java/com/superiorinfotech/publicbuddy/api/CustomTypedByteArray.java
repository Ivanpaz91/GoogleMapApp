package com.superiorinfotech.publicbuddy.api;

import java.io.IOException;
import java.io.OutputStream;

import retrofit.mime.TypedByteArray;

/**
 * Created by alex on 27.01.15.
 */
public class CustomTypedByteArray extends TypedByteArray {
    private final byte[] bytes;
    /**
     * Constructs a new typed byte array.  Sets mimeType to {@code application/unknown} if absent.
     *
     * @param mimeType
     * @param bytes
     * @throws NullPointerException if bytes are null
     */
    public CustomTypedByteArray(String mimeType, byte[] bytes) {
        super(mimeType, bytes);
        this.bytes = bytes;
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        out.write(this.bytes);
    }
}
