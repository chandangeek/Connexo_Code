/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl;

public class MissingProtocolException extends RuntimeException {
    public MissingProtocolException(String protocol) {
        super(String.format("Protocol %s is missing", protocol));
    }
}
