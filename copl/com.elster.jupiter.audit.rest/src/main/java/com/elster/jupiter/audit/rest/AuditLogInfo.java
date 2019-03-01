/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.audit.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuditLogInfo {

    private String name;

    private Object value;

    private Object previousValue;

    private String type;

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    public Object getPreviousValue() {
        return previousValue;
    }

    public String getType() {
        return type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public void setPreviousValue(Object previousValue) {
        this.previousValue = previousValue;
    }

    public void setType(String type) {
        this.type = type;
    }
}