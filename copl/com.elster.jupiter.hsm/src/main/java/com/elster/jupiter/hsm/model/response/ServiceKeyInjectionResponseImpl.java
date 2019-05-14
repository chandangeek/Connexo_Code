/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.hsm.model.response;

import com.elster.jupiter.hsm.model.Message;

public class ServiceKeyInjectionResponseImpl extends Message implements ServiceKeyInjectionResponse  {

    private String warning;

    public ServiceKeyInjectionResponseImpl(byte[] data, String warning) {
        super(data);
        this.warning = warning;
    }

    public String getWarning() {
        return warning;
    }
}