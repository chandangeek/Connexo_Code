/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.inbound.soap;

import java.util.Arrays;

public enum OperationEnum {
    CREATE("Create"),
    UPDATE("Update"),
    UNDEFINED("-");

    private String operation;

    OperationEnum(String operation) {
        this.operation = operation;
    }

    public String getOperation() {
        return operation;
    }

    public static OperationEnum getFromString(String operation) {
        return Arrays.stream(OperationEnum.values())
                .filter(e -> e.getOperation().equals(operation))
                .findFirst()
                .orElse(UNDEFINED);
    }
}
