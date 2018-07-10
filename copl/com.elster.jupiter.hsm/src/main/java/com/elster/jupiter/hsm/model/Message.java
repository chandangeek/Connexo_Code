package com.elster.jupiter.hsm.model;

import javax.annotation.Nonnull;
import java.nio.charset.Charset;

public class Message {

    private final byte[] bytes;
    private final Charset charSet;


    public Message(@Nonnull byte[] data) {
        this.bytes = data;
        this.charSet = Charset.defaultCharset();
    }


    public Message(@Nonnull  String string) {
        this.charSet = Charset.defaultCharset();
        this.bytes = string.getBytes(this.getCharSet());
    }

    public Message(@Nonnull String s, @Nonnull Charset charSet) {
        this.bytes = s.getBytes(charSet);
        this.charSet = charSet;
    }

    public Message(byte[] bytes, Charset charSet) {
        this.bytes = bytes;
        this.charSet = charSet;
    }


    @Override
    public String toString() {
        return new String(bytes, charSet);
    }

    public Charset getCharSet() {
        return charSet;
    }

    public byte[] getBytes() { return this.bytes; }


}
