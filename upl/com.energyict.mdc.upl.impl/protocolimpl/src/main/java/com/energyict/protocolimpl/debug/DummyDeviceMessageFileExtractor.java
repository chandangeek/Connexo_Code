package com.energyict.protocolimpl.debug;

import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.Extractor;
import com.energyict.mdc.upl.properties.DeviceMessageFile;

import java.nio.charset.Charset;

/**
 * Provides an dummy implementation for the {@link Extractor} interface
 * that returns empty values in all of its methods.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-12-28 (09:49)
 */
final class DummyDeviceMessageFileExtractor implements DeviceMessageFileExtractor {
    @Override
    public String id(DeviceMessageFile deviceMessageFile) {
        return "";
    }

    @Override
    public String name(DeviceMessageFile deviceMessageFile) {
        return "";
    }

    @Override
    public String contents(DeviceMessageFile deviceMessageFile) {
        return "";
    }

    @Override
    public byte[] binaryContents(DeviceMessageFile deviceMessageFile) {
        return new byte[0];
    }

    @Override
    public String contents(DeviceMessageFile deviceMessageFile, String charSetName) {
        return "";
    }

    @Override
    public String contents(DeviceMessageFile deviceMessageFile, Charset charset) {
        return "";
    }
}