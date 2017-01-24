package com.energyict.protocols.messaging;

import com.elster.jupiter.orm.UnderlyingIOException;
import com.energyict.mdc.protocol.api.DeviceMessageFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.function.Consumer;

/**
 * Provides an implementation for the Consumer interface
 * that is required to read the contents of a
 * {@link DeviceMessageFile}.
 * This consumer will read the contents and return it as a String.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-05-12 (15:55)
 */
public class DeviceMessageFileStringContentConsumer implements Consumer<InputStream> {
    private static final int DEFAULT_BUFFER_SIZE = 1024;
    private static final String DEFAULT_CHAR_SET = "UTF-8";
    private StringBuilder builder = new StringBuilder();
    private final int bufferSize;
    private final Charset charset;

    public static String readFrom(DeviceMessageFile deviceMessageFile, String charSetName) {
        return readFrom(deviceMessageFile, Charset.forName(charSetName));
    }

    public static String readFrom(DeviceMessageFile deviceMessageFile, Charset charSet) {
        DeviceMessageFileStringContentConsumer consumer = new DeviceMessageFileStringContentConsumer(DEFAULT_BUFFER_SIZE, charSet);
        deviceMessageFile.readWith(consumer);
        return consumer.getContents();
    }

    String getContents() {
        return this.builder.toString();
    }

    DeviceMessageFileStringContentConsumer() {
        this(DEFAULT_BUFFER_SIZE);
    }

    DeviceMessageFileStringContentConsumer(int bufferSize) {
        this(bufferSize, Charset.forName(DEFAULT_CHAR_SET));
    }

    DeviceMessageFileStringContentConsumer(int bufferSize, Charset charset) {
        super();
        this.bufferSize = bufferSize;
        this.charset = charset;
    }

    @Override
    public void accept(InputStream inputStream) {
        try {
            byte[] buffer = new byte[this.bufferSize];
            int bytesRead;
            boolean proceed = true;
            while (proceed) {
                bytesRead = inputStream.read(buffer, 0, this.bufferSize);
                proceed = bytesRead == this.bufferSize;
                if (bytesRead != -1) {
                    this.append(buffer, bytesRead);
                }
            }
        } catch (IOException e) {
            throw new UnderlyingIOException(e);
        }
    }

    private void append(byte[] buffer, int bytesRead) {
        this.builder.append(new String(buffer, 0, bytesRead, this.charset));
    }

}