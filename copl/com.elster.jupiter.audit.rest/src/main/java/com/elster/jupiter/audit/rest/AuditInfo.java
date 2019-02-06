/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.audit.rest;

import com.elster.jupiter.audit.AuditDomainContextType;
import com.elster.jupiter.audit.AuditDomainType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.time.Instant;
import java.util.List;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuditInfo {

    public long id;
    public String operation;
    public String operationType;
    public Instant changedOn;
    public String domain;
    public String context;
    public AuditDomainType domainType;
    public AuditDomainContextType contextType;
    public String user;
    public AuditReferenceInfo auditReference;
    public String name;
    public List<AuditLogInfo> auditLogs;
}