/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ConfirmedServiceError.java
 *
 * Created on 4 december 2006, 16:11
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

/**
 *
 * @author Koen
 */
public class ConfirmedServiceError extends AbstractReplyError {

    private int error;

    public String toString() {
        return "ConfirmedServiceError:\n" + "   error=" + getError() + "\n";
    }

    protected void parse(byte[] rawData) {
        int offset = 0;
        offset++; // skip read response
        setError((int)rawData[offset++] & 0xff);
    }

    public int getError() {
        return error;
    }

    public void setError(int error) {
        this.error = error;
    }



}
