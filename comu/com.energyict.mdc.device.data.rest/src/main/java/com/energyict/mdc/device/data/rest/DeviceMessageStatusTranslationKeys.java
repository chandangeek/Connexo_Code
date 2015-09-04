package com.energyict.mdc.device.data.rest;

import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;

import java.util.stream.Stream;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-09-04 (10:23)
 */
public enum DeviceMessageStatusTranslationKeys implements TranslationKey {

    REVOKED(DeviceMessageStatus.REVOKED, "Revoked"),
    CONFIRMED(DeviceMessageStatus.CONFIRMED, "Confirmed"),
    FAILED(DeviceMessageStatus.FAILED, "Failed"),
    INDOUBT(DeviceMessageStatus.INDOUBT, "In doubt"),
    PENDING(DeviceMessageStatus.PENDING, "Pending"),
    SENT(DeviceMessageStatus.SENT, "Sent"),
    WAITING(DeviceMessageStatus.WAITING, "Waiting");

    private DeviceMessageStatus deviceMessageStatus;
    private String defaultFormat;

    DeviceMessageStatusTranslationKeys(DeviceMessageStatus deviceMessageStatus, String defaultFormat) {
        this.deviceMessageStatus = deviceMessageStatus;
        this.defaultFormat = defaultFormat;
    }

    public DeviceMessageStatus getDeviceMessageStatus() {
        return deviceMessageStatus;
    }

    @Override
    public String getKey() {
        return DeviceMessageStatus.class.getSimpleName() + "." + this.deviceMessageStatus.name();
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultFormat;
    }

    public static DeviceMessageStatusTranslationKeys from(DeviceMessageStatus deviceMessageStatus) {
        return Stream
                .of(values())
                .filter(each -> each.deviceMessageStatus.equals(deviceMessageStatus))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Translation missing for completion code: " + deviceMessageStatus));
    }

    public static String translationFor(DeviceMessageStatus deviceMessageStatus, Thesaurus thesaurus) {
        return thesaurus.getFormat(from(deviceMessageStatus)).format();
    }

}