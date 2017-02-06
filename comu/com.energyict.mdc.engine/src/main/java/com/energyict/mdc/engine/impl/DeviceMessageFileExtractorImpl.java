package com.energyict.mdc.engine.impl;

import com.elster.jupiter.orm.UnderlyingIOException;
import com.energyict.mdc.device.config.DeviceMessageFile;
import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.function.Consumer;

/**
 * Provides an implementation for the {@link DeviceMessageFileExtractor} interface
 * that assumes that all UPL objects are in fact {@link DeviceMessageFile}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-19 (10:52)
 */
@Component(name = "com.energyict.mdc.upl.messages.legacy.file.extractor", service = {DeviceMessageFileExtractor.class})
@SuppressWarnings("unused")
public class DeviceMessageFileExtractorImpl implements DeviceMessageFileExtractor {

    @Activate
    public void activate() {
        Services.deviceMessageFileExtractor(this);
    }

    @Deactivate
    public void deactivate() {
        Services.deviceMessageFileExtractor(null);
    }

    @Override
    public String id(com.energyict.mdc.upl.properties.DeviceMessageFile deviceMessageFile) {
        return Long.toString(((DeviceMessageFile) deviceMessageFile).getId());
    }

    @Override
    public String name(com.energyict.mdc.upl.properties.DeviceMessageFile deviceMessageFile) {
        return ((DeviceMessageFile) deviceMessageFile).getName();
    }

    @Override
    public String contents(com.energyict.mdc.upl.properties.DeviceMessageFile deviceMessageFile) {
        return this.contents(deviceMessageFile, Charset.defaultCharset());
    }

    @Override
    public String contents(com.energyict.mdc.upl.properties.DeviceMessageFile deviceMessageFile, String charSetName) throws UnsupportedEncodingException {
        return this.contents(deviceMessageFile, Charset.forName(charSetName));
    }

    @Override
    public String contents(com.energyict.mdc.upl.properties.DeviceMessageFile deviceMessageFile, Charset charset) {
        return DeviceMessageFileStringContentConsumer.readFrom((DeviceMessageFile) deviceMessageFile, charset);
    }

    @Override
    public byte[] binaryContents(com.energyict.mdc.upl.properties.DeviceMessageFile deviceMessageFile) {
        return DeviceMessageFileByteContentConsumer.readFrom((DeviceMessageFile) deviceMessageFile);
    }

    @Override
    public long size(com.energyict.mdc.upl.properties.DeviceMessageFile deviceMessageFile) throws SQLException {
        return ((DeviceMessageFile) deviceMessageFile).getSize();
    }

    @Override
    public void processFileAsStream(com.energyict.mdc.upl.properties.DeviceMessageFile deviceMessageFile, Consumer<InputStream> consumer) throws SQLException {
        ((DeviceMessageFile) deviceMessageFile).readWith(consumer);
    }

    private static class DeviceMessageFileStringContentConsumer implements Consumer<InputStream> {
        private static final int DEFAULT_BUFFER_SIZE = 1024;
        private static final String DEFAULT_CHAR_SET = "UTF-8";
        private final int bufferSize;
        private final Charset charset;
        private StringBuilder builder = new StringBuilder();

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

        public static String readFrom(com.energyict.mdc.protocol.api.DeviceMessageFile deviceMessageFile, String charSetName) {
            return readFrom(deviceMessageFile, Charset.forName(charSetName));
        }

        public static String readFrom(com.energyict.mdc.protocol.api.DeviceMessageFile deviceMessageFile, Charset charSet) {
            DeviceMessageFileStringContentConsumer consumer = new DeviceMessageFileStringContentConsumer(DEFAULT_BUFFER_SIZE, charSet);
            deviceMessageFile.readWith(consumer);
            return consumer.getContents();
        }

        String getContents() {
            return this.builder.toString();
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

    private static class DeviceMessageFileByteContentConsumer implements Consumer<InputStream> {
        private static final int DEFAULT_BUFFER_SIZE = 1024;
        private final int bufferSize;
        private byte[] bytes;

        DeviceMessageFileByteContentConsumer() {
            this(DEFAULT_BUFFER_SIZE);
        }

        DeviceMessageFileByteContentConsumer(int bufferSize) {
            super();
            this.bufferSize = bufferSize;
        }

        public static byte[] readFrom(com.energyict.mdc.protocol.api.DeviceMessageFile deviceMessageFile) {
            DeviceMessageFileByteContentConsumer consumer = new DeviceMessageFileByteContentConsumer();
            deviceMessageFile.readWith(consumer);
            return consumer.getBytes();
        }

        byte[] getBytes() {
            return this.bytes;
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

        private void append(byte[] buffer, int size) {
            if (this.bytes == null) {
                this.bytes = new byte[size];
                System.arraycopy(buffer, 0, this.bytes, 0, size);
            } else {
                byte[] extended = new byte[this.bytes.length + size];
                System.arraycopy(this.bytes, 0, extended, 0, this.bytes.length);
                System.arraycopy(buffer, 0, extended, this.bytes.length, size);
                this.bytes = extended;
            }
        }

    }
}