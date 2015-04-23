package com.elster.jupiter.demo.impl;

/**
 * Copyrights EnergyICT
 * Date: 22/04/2015
 * Time: 10:40
 */
public class MissingProtocolException extends RuntimeException {
    public MissingProtocolException(String protocol) {
        super(String.format("Protocol %s is missing", protocol));
    }
}
