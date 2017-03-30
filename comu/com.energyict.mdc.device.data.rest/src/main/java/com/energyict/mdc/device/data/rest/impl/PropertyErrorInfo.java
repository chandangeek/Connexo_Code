/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-01-07 (16:50)
 */
class PropertyErrorInfo {
    public String id;
    public String msg;

    PropertyErrorInfo() {}

    PropertyErrorInfo(String id, String msg) {
        this.id = id;
        this.msg = msg;
    }
}