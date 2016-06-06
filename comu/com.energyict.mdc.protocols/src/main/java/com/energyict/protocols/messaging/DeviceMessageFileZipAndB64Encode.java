package com.energyict.protocols.messaging;

import com.energyict.protocolimpl.generic.messages.GenericMessaging;

import java.io.InputStream;

/**
 * Extends the {@link DeviceMessageFileByteContentConsumer}
 * to ZIP and B64 encode the contents received from the InputStream.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-05-17 (11:46)
 */
public class DeviceMessageFileZipAndB64Encode extends DeviceMessageFileByteContentConsumer {
    private String zippedAndB64Encoded;

    @Override
    public void accept(InputStream inputStream) {
        super.accept(inputStream);
        this.zippedAndB64Encoded = GenericMessaging.zipAndB64EncodeContent(this.getBytes());
    }

    public String getZippedAndB64Encoded() {
        return zippedAndB64Encoded;
    }

}