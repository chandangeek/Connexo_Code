/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.outbound.soap;

import java.util.Arrays;

public enum OperationEnum {
    CREATE("Create", "SIM1001"),
    UPDATE("Update", "SIM1002"),
    GET("Get", "SIM1032"),
    DELETE("Delete", "SIM1037"),
    UNDEFINED("-", "");

    private String operation;
    private String defaultErrorCode;

    OperationEnum(String operation, String defaultErrorCode) {
        this.operation = operation;
        this.defaultErrorCode = defaultErrorCode;
    }

    public String getOperation() {
        return operation;
    }

    public String getDefaultErrorCode() {
        return defaultErrorCode;
    }

    public static OperationEnum getFromString(String operation) {
        return Arrays.stream(OperationEnum.values())
                .filter(e -> e.getOperation().equals(operation))
                .findFirst()
                .orElse(UNDEFINED);
    }
}
