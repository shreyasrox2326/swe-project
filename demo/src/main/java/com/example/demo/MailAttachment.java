package com.example.demo;

public class MailAttachment {

    private final String filename;
    private final String content;
    private final String contentType;

    public MailAttachment(String filename, String content, String contentType) {
        this.filename = filename;
        this.content = content;
        this.contentType = contentType;
    }

    public String getFilename() {
        return filename;
    }

    public String getContent() {
        return content;
    }

    public String getContentType() {
        return contentType;
    }
}
