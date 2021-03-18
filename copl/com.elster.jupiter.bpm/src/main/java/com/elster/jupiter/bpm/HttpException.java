/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm;

public class HttpException extends RuntimeException {

    private int responseCode;

    public HttpException(String payload, int responseCode) {
        super(String.valueOf(responseCode) + ": " + payload);
        this.responseCode = responseCode;
    }

    public int getResponseCode() {
        return responseCode;
    }
}
