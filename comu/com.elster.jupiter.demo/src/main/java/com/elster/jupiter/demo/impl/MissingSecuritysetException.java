package com.elster.jupiter.demo.impl;

/**
 * Copyrights EnergyICT
 * Date: 22/04/2015
 * Time: 10:40
 */
public class MissingSecuritysetException extends RuntimeException {
    public MissingSecuritysetException(String securitySetName) {
        super(String.format("Security set %s is missing", securitySetName));
    }
}
