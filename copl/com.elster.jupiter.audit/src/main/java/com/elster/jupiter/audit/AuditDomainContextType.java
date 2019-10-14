/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.audit;

public enum AuditDomainContextType {

    NODOMAIN(0, "auditDomainContext.noDomainContext", AuditDomainType.UNKNOWN),
    GENERAL_ATTRIBUTES(1, "auditDomainContext.generalAttributes", AuditDomainType.DEVICE),
    DEVICE_ATTRIBUTES(2, "auditDomainContext.deviceAttributes", AuditDomainType.DEVICE),
    DEVICE_CUSTOM_ATTRIBUTES(3, "auditDomainContext.deviceCustomAttributes", AuditDomainType.DEVICE),
    DEVICE_DATA_SOURCE_SPECIFICATIONS(4, "auditDomainContext.deviceDataSourceSpecifications", AuditDomainType.DEVICE),
    DEVICE_CHANNEL_CUSTOM_ATTRIBUTES(5, "auditDomainContext.deviceChannelCustomAttributes", AuditDomainType.DEVICE),
    DEVICE_REGISTER_CUSTOM_ATTRIBUTES(6, "auditDomainContext.deviceRegisterCustomAttributes", AuditDomainType.DEVICE),
    DEVICE_PROTOCOL_DIALECTS_PROPS(7, "auditDomainContext.deviceProtocolDialectsProps", AuditDomainType.DEVICE),
    DEVICE_COMTASKS(8, "auditDomainContext.deviceComTasks", AuditDomainType.DEVICE),
    DEVICE_CONNECTION_METHODS(9, "auditDomainContext.deviceConnectionMethods", AuditDomainType.DEVICE),
    USAGEPOINT_GENERAL_ATTRIBUTES(10, "auditDomainContext.usagePointGeneralAttributes", AuditDomainType.USAGEPOINT),
    USAGEPOINT_TECHNICAL_ATTRIBUTES(11, "auditDomainContext.usagePointTechnicalAttributes", AuditDomainType.USAGEPOINT),
    USAGEPOINT_CUSTOM_ATTRIBUTES(12, "auditDomainContext.usagePointCustomAttributes", AuditDomainType.USAGEPOINT),
    USAGEPOINT_METROLOGY_CONFIGURATION(13, "auditDomainContext.usagePointMetrologyConfiguration", AuditDomainType.USAGEPOINT),
    ;

    private final Integer domainContextId;
    private final String domainContextType;
    private final AuditDomainType domainType;

    AuditDomainContextType(Integer domainContextId, String domainContextType, AuditDomainType domainType) {
        this.domainContextId = domainContextId;
        this.domainContextType = domainContextType;
        this.domainType = domainType;
    }

    public String type() {
        return domainContextType;
    }

    public AuditDomainType domainType() {
        return domainType;
    }

    public Integer domainContextId() {
        return domainContextId;
    }
}
