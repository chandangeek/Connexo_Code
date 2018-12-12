/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.util;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.impl.MessageSeeds;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.inject.Inject;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class MacExceptionInfo {

    private final Thesaurus thesaurus;
    @JsonProperty("success")
    public final boolean success = false;
    @JsonProperty("message")
    public String message;
    @JsonProperty("error")
    public String error;
    @JsonProperty("errorCode")
    public String errorCode;

    @Inject
    public MacExceptionInfo(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public MacExceptionInfo from(Exception exception){
        this.error = exception.getLocalizedMessage();
        this.errorCode = getErrorCode();
        this.message = thesaurus.getSimpleFormat(MessageSeeds.MAC_ERROR).format();
        return this;
    }

    private String getErrorCode() {
        return getHostname() + "-" + Long.toHexString(System.currentTimeMillis()).toUpperCase();
    }

    private String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "UNKOWNHOST";
        }
    }
}
