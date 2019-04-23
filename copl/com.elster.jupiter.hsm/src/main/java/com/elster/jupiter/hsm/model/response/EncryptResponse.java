/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.elster.jupiter.hsm.model.response;

import com.elster.jupiter.hsm.model.Message;

public class EncryptResponse extends Message {

    public EncryptResponse(byte[] data) {
        super(data);
    }
}