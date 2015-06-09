package com.energyict.mdc.channels.sms;

import com.energyict.mdc.protocol.AbstractComChannel;
import com.energyict.mdc.protocol.exceptions.ConnectionCommunicationException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author sva
 * @since 19/06/13 - 9:21
 */
public class ProximusSmsComChannel extends AbstractComChannel implements ServerProximusSmsComChannel {


    private final String phoneNumber;
    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(new byte[0]);
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    private ProximusSmsSender smsSender;

    public ProximusSmsComChannel(String phoneNumber, String apiConnectionURL, String apiSource, String apiAuthentication, String apiServiceCode) {
        this.phoneNumber = phoneNumber;

        this.smsSender = new ProximusSmsSender();
        this.smsSender.setConnectionURL(apiConnectionURL);
        this.smsSender.setSource(apiSource);
        this.smsSender.setAuthentication(apiAuthentication);
        this.smsSender.setServiceCode(apiServiceCode);
    }

    @Override
    protected void doClose() {
        try {
            byteArrayOutputStream.close();
        } catch (IOException e) {
            throw new ConnectionCommunicationException(e);
        }
    }

    @Override
    protected void doFlush() throws IOException {
        // Get the SMS message form the byteArrayOutputStream
        byte[] messageContent = byteArrayOutputStream.toByteArray();

        // Send out the SMS and place the result on the byteArrayInputStream
        byte[] result = this.smsSender.sendSms(phoneNumber, messageContent);
        byteArrayInputStream = new ByteArrayInputStream(result);

        // Reset the byteArrayOutputStream
        byteArrayOutputStream.reset();
    }

    @Override
    protected boolean doStartReading() {
        return true;
    }

    @Override
    protected boolean doStartWriting() {
        return true;
    }

    @Override
    protected int doWrite(int b) {
        byteArrayOutputStream.write(b);
        return 1;
    }

    @Override
    protected int doWrite(byte[] bytes) {
        try {
            byteArrayOutputStream.write(bytes);
            return bytes.length;
        } catch (IOException e) {
            throw new ConnectionCommunicationException(e);
        }
    }

    @Override
    protected int doRead() {
        return byteArrayInputStream.read();
    }

    @Override
    protected int doRead(byte[] buffer) {
        return byteArrayInputStream.read(buffer, 0, buffer.length);
    }

    @Override
    protected int doRead(byte[] buffer, int offset, int length) {
        return byteArrayInputStream.read(buffer, offset, length);
    }

    @Override
    protected int doAvailable() {
        return byteArrayInputStream.available();
    }
}