/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl;

public class MissingSecuritysetException extends RuntimeException {
    public MissingSecuritysetException(String securitySetName) {
        super(String.format("Security set %s is missing", securitySetName));
    }
}
