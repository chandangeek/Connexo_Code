package com.energyict.protocolimplv2.umi.types;

import com.energyict.protocolimplv2.umi.util.Limits;
import java.security.InvalidParameterException;

public enum ProtocolType {
    UMI_COMMAND_RESPONSE(0x0);

    private final int id;

    ProtocolType(final int id) {
        if (id < Limits.MIN_UNSIGNED || id > Limits.MAX_UNSIGNED_BYTE)
            throw new InvalidParameterException("expected value in range [" +
                    Limits.MIN_UNSIGNED + "," + Limits.MAX_UNSIGNED_BYTE + "]");
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static ProtocolType[] protocolTypes = ProtocolType.values();

    public static ProtocolType fromId(int id) {
        return protocolTypes[id];
    }
}
