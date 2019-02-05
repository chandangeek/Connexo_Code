/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.cim.webservices.inbound.soap.impl;

import java.time.Instant;
import java.util.Map;

public final class CustomPropertySetInfo {
    private String id;
    private Instant versionId;
    private Instant fromDate;
    private Instant endDate;
    private Map<String, String> attributes;
    private Boolean updateRange;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Instant getFromDate() {
        return fromDate;
    }

    public void setFromDate(Instant fromDate) {
        this.fromDate = fromDate;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public Instant getEndDate() {
        return endDate;
    }

    public void setEndDate(Instant endDate) {
        this.endDate = endDate;
    }

    public Instant getVersionId() {
        return versionId;
    }

    public void setVersionId(Instant versionId) {
        this.versionId = versionId;
    }

    public boolean isUpdateRange() {
        return this.updateRange;
    }

    public void setUpdateRange(Boolean updateRange) {
        if(updateRange == null){
            this.updateRange = false;
        }
        else{ this.updateRange = updateRange;}
    }
}