package com.codecrafters.gitjava;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public final class ZlibHelper {
    private ZlibHelper() {}

    public static byte[] deflate(byte[] input) {
        Deflater deflater = new Deflater();
        deflater.setInput(input);
        deflater.finish();
        ByteArrayOutputStream output = new ByteArrayOutputStream(input.length);
        byte[] buffer = new byte[1024];
        while (!deflater.finished()) {
            int count = deflater.deflate(buffer);
            output.write(buffer, 0, count);
        }
        deflater.end();
        return output.toByteArray();
    }

    public static byte[] inflate(byte[] input) throws IOException {
        Inflater inflater = new Inflater();
        inflater.setInput(input);
        ByteArrayOutputStream output = new ByteArrayOutputStream(input.length);
        byte[] buffer = new byte[1024];
        try {
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                if (count == 0) {
                    if (inflater.finished()) {
                        break;
                    }
                    throw new IOException("Inflation ended unexpectedly");
                }
                output.write(buffer, 0, count);
            }
            return output.toByteArray();
        } catch (DataFormatException e) {
            throw new IOException("Invalid compressed payload", e);
        } finally {
            inflater.end();
        }
    }
}
