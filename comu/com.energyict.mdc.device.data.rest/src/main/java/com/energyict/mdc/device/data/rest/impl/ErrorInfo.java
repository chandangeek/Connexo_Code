/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

public class ErrorInfo {

    public String message;
    public String tooltip;

    public ErrorInfo(String message) {
        this.message = message;
    }

    public ErrorInfo(String message, String tooltip) {
        this.message = message;
        this.tooltip = tooltip;
    }
}
