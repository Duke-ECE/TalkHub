package com.talkhub.backend.im.protocol;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

final class GzipCompression {

    private GzipCompression() {
    }

    static byte[] compress(byte[] source) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outputStream)) {
            gzipOutputStream.write(source);
        }
        return outputStream.toByteArray();
    }

    static byte[] decompress(byte[] source) throws IOException {
        try (GZIPInputStream inputStream = new GZIPInputStream(new ByteArrayInputStream(source))) {
            return inputStream.readAllBytes();
        }
    }
}
