/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.cim.webservices.inbound.soap.impl.customattributeset;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.time.Instant;
import java.util.Map;

public final class CasInfo {
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

    @JsonIgnore
    public Instant getFromDate() {
        return fromDate;
    }

    public void setFromDate(Instant fromDate) {
        this.fromDate = fromDate;
    }

    @JsonGetter
    private long getEpochFromDate() {
        return fromDate != null ? fromDate.toEpochMilli() : 0;
    }

    @JsonSetter
    private void setEpochFromDate(long time) {
        fromDate = time == 0 ? null : Instant.ofEpochMilli(time);
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    @JsonIgnore
    public Instant getEndDate() {
        return endDate;
    }

    public void setEndDate(Instant endDate) {
        this.endDate = endDate;
    }

    @JsonGetter
    private long getEpochEndDate() {
        return endDate != null ? endDate.toEpochMilli() : 0;
    }

    @JsonSetter
    private void setEpochEndDate(long time) {
        endDate = time == 0 ? null : Instant.ofEpochMilli(time);
    }

    @JsonIgnore
    public Instant getVersionId() {
        return versionId;
    }

    public void setVersionId(Instant versionId) {
        this.versionId = versionId;
    }

    @JsonGetter
    private long getEpochVersionId() {
        return versionId != null ? versionId.toEpochMilli() : 0;
    }

    @JsonSetter
    private void setEpochVersionId(long time) {
        versionId = time == 0 ? null : Instant.ofEpochMilli(time);
    }

    public Boolean isUpdateRange() {
        return updateRange;
    }

    public void setUpdateRange(Boolean updateRange) {
        if (updateRange == null) {
            this.updateRange = false;
        } else {
            this.updateRange = updateRange;
        }
    }
}