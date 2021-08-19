package com.energyict.protocolimplv2.umi.link;

import java.security.InvalidParameterException;
import java.util.Arrays;

public enum FragmentType {
    FIRST(0x0),
    MIDDLE(0X1),
    LAST(0X2),
    NEXT(0X3),
    ACK(0X4),
    ERROR(0XF);

    private final int id;

    FragmentType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static FragmentType fromId(int id) {
        return Arrays.stream(FragmentType.values())
                .filter(val -> (byte)val.getId() == (byte)id)
                .findFirst()
                .orElseThrow(() -> new InvalidParameterException("Unsupported fragment type: " + id));
    }

}
