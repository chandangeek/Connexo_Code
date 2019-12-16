/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.sap.soap.webservices.impl;

import java.util.logging.Level;

public enum SeverityCode {
    INFORMATION("1"),
    WARNING("2"),
    ERROR("3"),
    ABORT("4");

    private final String code;

    SeverityCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    static public String getSeverityCode(Level level) {
        if (level.getName().equals(Level.SEVERE.getName())) {
            return SeverityCode.ERROR.getCode();
        } else if (level.getName().equals(Level.WARNING.getName())) {
            return SeverityCode.WARNING.getCode();
        } else {
            return SeverityCode.INFORMATION.getCode();
        }
    }
}
