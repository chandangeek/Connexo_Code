/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.hsm;

public class EncryptionResponse {

    private final byte[] data;

    public EncryptionResponse(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }
}
