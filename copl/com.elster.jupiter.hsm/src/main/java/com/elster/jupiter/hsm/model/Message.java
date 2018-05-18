package com.elster.jupiter.hsm.model;

import java.nio.charset.Charset;

public class Message {

    private final byte[] bytes;
    private final Charset charSet;


    public Message(byte[] data) {
        this.bytes = data;
        this.charSet = Charset.defaultCharset();
    }


    public Message(String string) {
        this.charSet = Charset.defaultCharset();
        this.bytes = string.getBytes(this.getCharSet());
    }

    public Message(String s, Charset charSet) {
        this.bytes = s.getBytes(charSet);
        this.charSet = charSet;
    }


    public String toString() {
        return new String(bytes, charSet);
    }

    public Charset getCharSet() {
        return charSet;
    }

    public byte[] getBytes() {
        return this.bytes;
    }

}
