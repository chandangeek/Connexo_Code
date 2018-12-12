/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.util;

/**
 * Created by bvn on 8/11/16.
 */
public enum StatusCode {
    UNPROCESSABLE_ENTITY(422);

    private final int code;

    StatusCode(int i) {
        this.code = i;
    }

    public int getStatusCode() {
        return code;
    }
}
