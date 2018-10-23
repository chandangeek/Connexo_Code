package com.elster.jupiter.hsm.model;

import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Objects;

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

    public Message(byte[] bytes, Charset charSet) {
        this.bytes = bytes;
        this.charSet = charSet;
    }


    @Override
    public String toString() {
        return new String(bytes, charSet);
    }

    public String toHex() {
        return Hex.toHexString(getBytes());
    }

    public String toBase64() {
        return Base64.toBase64String(getBytes());
    }

    public Charset getCharSet() {
        return charSet;
    }

    public byte[] getBytes() { return this.bytes; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Message)) {
            return false;
        }
        Message message = (Message) o;
        return Arrays.equals(bytes, message.bytes) &&
                Objects.equals(charSet, message.charSet);
    }

    @Override
    public int hashCode() {

        int result = Objects.hash(charSet);
        result = 31 * result + Arrays.hashCode(bytes);
        return result;
    }
}
