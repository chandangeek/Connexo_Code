/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cim.webservices.inbound.soap;

import java.util.Arrays;

public enum OperationEnum {
    CREATE("Create", "SIM1001"),
    UPDATE("Update", "SIM1002"),
    LINK("Link", "SIM1003"),
    UNLINK("Unlink", "SIM1004"),
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
        return Arrays.stream(OperationEnum.values()).filter(e -> e.getOperation().equals(operation)).findFirst()
                .orElse(UNDEFINED);
    }
}
