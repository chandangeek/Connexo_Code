package com.energyict.protocols.impl.channels.sms;

import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.io.CommunicationException;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.ComChannelType;
import com.energyict.protocols.mdc.services.impl.MessageSeeds;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author sva
 * @since 19/06/13 - 9:21
 */
public class ProximusSmsComChannel implements ComChannel {

    private ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(new byte[0]);
    private ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    private final String phoneNumber;
    private ProximusSmsSender smsSender;
    private TypedProperties connectionTaskProperties = TypedProperties.empty();

    public ProximusSmsComChannel(String phoneNumber, String apiConnectionURL, String apiSource, String apiAuthentication, String apiServiceCode) {
        super();
        this.phoneNumber = phoneNumber;
        this.smsSender = new ProximusSmsSender();
        this.smsSender.setConnectionURL(apiConnectionURL);
        this.smsSender.setSource(apiSource);
        this.smsSender.setAuthentication(apiAuthentication);
        this.smsSender.setServiceCode(apiServiceCode);
    }

    @Override
    public int available() {
        return byteArrayInputStream.available();
    }

    @Override
    public final void close () {
        try {
            byteArrayOutputStream.close();
        } catch (IOException e) {
            throw new CommunicationException(MessageSeeds.UNEXPECTED_IO_EXCEPTION, e);
        }
    }

    @Override
    public final void flush () throws IOException {
        // Get the SMS message form the byteArrayOutputStream
        byte[] messageContent = byteArrayOutputStream.toByteArray();

        // Send out the SMS and place the result on the byteArrayInputStream
        byte[] result = this.smsSender.sendSms(phoneNumber, messageContent);
        byteArrayInputStream = new ByteArrayInputStream(result);

        // Reset the byteArrayOutputStream
        byteArrayOutputStream.reset();
    }

    @Override
    public final boolean startReading () {
        return true;
    }

    @Override
    public final int read () {
        return byteArrayInputStream.read();
    }

    @Override
    public final int read (byte[] buffer) {
        return byteArrayInputStream.read(buffer, 0, buffer.length);
    }

    @Override
    public final int read (byte[] buffer, int offset, int length) {
        return byteArrayInputStream.read(buffer, offset, length);
    }

    @Override
    public final boolean startWriting () {
        return true;
    }

    @Override
    public final int write (int b) {
        byteArrayOutputStream.write(b);
        return 1;
    }

    @Override
    public final int write (byte[] bytes) {
        try {
            byteArrayOutputStream.write(bytes);
            return bytes.length;
        } catch (IOException e) {
            throw new CommunicationException(MessageSeeds.UNEXPECTED_IO_EXCEPTION, e);
        }
    }

    @Override
    public TypedProperties getProperties() {
        return this.connectionTaskProperties;
    }

    @Override
    public ComChannelType getComChannelType() {
        return ComChannelType.PROXIMUS_SMS_COM_CHANNEL;
    }

    @Override
    public void addProperties(TypedProperties typedProperties) {
        this.connectionTaskProperties.setAllProperties(typedProperties);
    }

}