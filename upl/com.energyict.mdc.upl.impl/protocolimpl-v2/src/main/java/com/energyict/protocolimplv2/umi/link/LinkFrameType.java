package com.energyict.protocolimplv2.umi.link;

import com.energyict.protocolimplv2.umi.util.Limits;

import java.security.InvalidParameterException;
import java.util.Arrays;

public enum LinkFrameType {
    SIMPLE(0X00),
    SIMPLE_RESPONSE(0X01, false),

    FRAGMENT(0X02),
    FRAGMENT_RESPONSE(0X03, false),

    RESYNC(0X04),
    RESYNC_RESPONSE(0X05, false),

    ERROR(0XF);


    private final int id;
    private final boolean isCmd;

    LinkFrameType(final int id) {
        this(id, true);
    }

    LinkFrameType(final int id, boolean isCmd) {
        if (id < Limits.MIN_UNSIGNED || id > Limits.MAX_UNSIGNED_BYTE)
            throw new InvalidParameterException("expected value in range [" +
                    Limits.MIN_UNSIGNED + "," + Limits.MAX_UNSIGNED_BYTE + "]");
        this.id = id;
        this.isCmd = isCmd;
    }

    public int getId() {
        return id;
    }

    public boolean isCmd() {
        return isCmd;
    }

    public static LinkFrameType fromId(int id) {
        return Arrays.stream(LinkFrameType.values())
                .filter(type -> (byte)type.getId()==(byte)id)
                .findFirst()
                .orElseThrow(() -> new InvalidParameterException("Unsupported link frame type: " + String.format("0x%02X", id)));
    }
}
