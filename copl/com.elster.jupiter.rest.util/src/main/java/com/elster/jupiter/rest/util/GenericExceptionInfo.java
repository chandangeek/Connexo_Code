/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.util;

import com.elster.jupiter.nls.Thesaurus;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.inject.Inject;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class GenericExceptionInfo {

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
    public GenericExceptionInfo(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }
}
