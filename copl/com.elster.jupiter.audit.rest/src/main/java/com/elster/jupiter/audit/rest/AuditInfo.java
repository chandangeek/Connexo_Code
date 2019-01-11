/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.audit.rest;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.time.Instant;
import java.util.List;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuditInfo {

    public long id;
    public String operation;
    public Instant changedOn;
    public String category;
    public String subCategory;
    public String user;
    public String name;
    public List<AuditLogInfo> auditLogs;
}