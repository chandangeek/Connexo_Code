package com.energyict.protocolimplv2.messages;

import com.energyict.protocolimpl.utils.ProtocolUtils;

public enum FirmwareImageType {
    ApplicationImage(0, "Application image"),
    BootloaderImage(1, "Bootloader image"),
    MetrologyImage(3, "Metrology image"),
    LanguageTableImage(4, "Language table image"),
    Invalid(99, "Invalid image type");

    private final int type;
    private final String description;

    FirmwareImageType(int mode, String description) {
        this.type = mode;
        this.description = description;
    }

    public static FirmwareImageType typeForDescription(String description) {
        for (FirmwareImageType firmwareImageType : values()) {
            if (firmwareImageType.getDescription().equals(description)) {
                return firmwareImageType;
            }
        }
        return FirmwareImageType.Invalid;
    }

    public int getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public byte[] getByteArray() {
        byte[] bytes = new byte[3];
        bytes[2] = (byte) type;
        return bytes;
    }

    public String getHexString(){
        String hexStringFromBytes = ProtocolUtils.outputHexString(getByteArray());
        return "000004";
    }
}
