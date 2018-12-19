/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.hsm.model.response;

import com.elster.jupiter.hsm.model.Message;

public class ServiceKeyInjectionResponse extends Message {

    private String warning;

    public ServiceKeyInjectionResponse(byte[] data, String warning) {
        super(data);
        this.warning = warning;
    }

    public String getWarning() {
        return warning;
    }
}