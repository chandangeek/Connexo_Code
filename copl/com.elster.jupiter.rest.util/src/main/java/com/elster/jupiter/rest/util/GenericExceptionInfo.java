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

public class GenericExceptionInfo {

    protected final Thesaurus thesaurus;
    @JsonProperty("success")
    public final boolean success = false;
    @JsonProperty("message")
    public String message;
    @JsonProperty("error")
    public String error;
    @JsonProperty("errorCode")
    public String errorCode;

    @Inject
    public GenericExceptionInfo(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    public GenericExceptionInfo from(Exception exception){
        this.message = thesaurus.getSimpleFormat(MessageSeeds.INTERNAL_CONNEXO_ERROR).format();
        this.error = exception.getLocalizedMessage();
        this.errorCode = getErrorCode();
        return this;
    }

    protected String getErrorCode() {
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
