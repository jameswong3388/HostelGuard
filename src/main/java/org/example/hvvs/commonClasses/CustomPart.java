package org.example.hvvs.commonClasses;

import java.io.IOException;
import java.io.InputStream;

public class CustomPart implements jakarta.servlet.http.Part {
    private final String fileName;
    private final String contentType;
    private final long size;
    private final InputStream content;

    public CustomPart(String fileName, String contentType, long size, InputStream content) {
        this.fileName = fileName;
        this.contentType = contentType;
        this.size = size;
        this.content = content;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return content;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public String getName() {
        return "file";
    }

    @Override
    public String getSubmittedFileName() {
        return fileName;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public void write(String fileName) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getHeader(String name) {
        return null;
    }

    @Override
    public java.util.Collection<String> getHeaders(String name) {
        return java.util.Collections.emptyList();
    }

    @Override
    public java.util.Collection<String> getHeaderNames() {
        return java.util.Collections.emptyList();
    }
}