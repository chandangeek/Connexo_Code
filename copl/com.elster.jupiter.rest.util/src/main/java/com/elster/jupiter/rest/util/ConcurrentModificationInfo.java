/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.util;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ConcurrentModificationInfo {
    @JsonProperty("error")
    public String messageBody;
    @JsonProperty("message")
    public String messageTitle;
    @JsonProperty("version")
    public Long version; // allow null values
    @JsonProperty("parent")
    public VersionInfo parent;

    public ConcurrentModificationInfo(){
    }

    public ConcurrentModificationInfo from(ConcurrentModificationException exception){
        this.messageBody = exception.getMessageBody();
        this.messageTitle = exception.getMessageTitle();
        this.version = exception.getVersion();
        if (exception.getParentId() != null && exception.getParentId().length != 0){
            this.parent = new VersionInfo();
            this.parent.id = exception.getParentId().length == 1 ? exception.getParentId()[0] : exception.getParentId();
            this.parent.version = exception.getParentVersion();
        }
        return this;
    }
}
