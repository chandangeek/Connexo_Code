/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.time.TimeDuration;

import com.google.common.collect.ImmutableMap;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by h165696 on 1/11/2018.
 */
public enum ConnectionMethodTpl {
    GATEWAY_DLMS_BEACON("Gateway DLMS Beacon", // name
            "GATEWAY", // connectionFunction
            OutboundTCPComPortPoolTpl.OUTBOUND_TCP, //outbound comPortPool
            null, //inbound comPortPool
            "GatewayTcpDlmsDialect", // protocolDialectName
            true, // isDefault
            true, // isOutbound
            ImmutableMap.of("host", "165.195.38.149",
                    "portNumber", new BigDecimal(4059),
                    "connectionTimeout", TimeDuration.minutes(1))
    ),
    MIRROR_DLMS_BEACON("Mirror DLMS Beacon", // name
            "MIRROR", // connectionFunction
            OutboundTCPComPortPoolTpl.OUTBOUND_TCP, //outbound comPortPool
            null, //inbound comPortPool
            "MirrorTcpDlmsDialect", // protocolDialectName
            false, // isDefault
            true, // isOutbound
            ImmutableMap.of("host", "165.195.38.149",
                    "portNumber", new BigDecimal(4059),
                    "connectionTimeout", TimeDuration.minutes(1))
    ),
    PSK_INBOUND("PSK Inbound", // name
            "NONE", // connectionFunction,
            null, //outbound comPortPool
            InboundComPortPoolTpl.INBOUND_SERVLET_BEACON_PSK, //inbound comPortPool
            "GatewayTcpDlmsDialect", // protocolDialectName
            false, // isDefault
            false, // isOutbound
            Collections.emptyMap()
    );

    private String name;
    private String connectionFunction;
    private OutboundTCPComPortPoolTpl outboundcomPortPool;
    private InboundComPortPoolTpl inboundcomPortPool;
    private String protocolDialectName;
    private boolean isDefault;
    private boolean isOutbound;
    private Map<String, Object> properties = new HashMap<>();

    ConnectionMethodTpl(String name, String connectionFunction, OutboundTCPComPortPoolTpl outboundcomPortPool, InboundComPortPoolTpl inboundcomPortPool,
                        String protocolDialectName, boolean isDefault, boolean isOutbound, Map<String, Object> properties) {
        this.name = name;
        this.connectionFunction = connectionFunction;
        this.outboundcomPortPool = outboundcomPortPool;
        this.inboundcomPortPool = inboundcomPortPool;
        this.protocolDialectName = protocolDialectName;
        this.isDefault = isDefault;
        this.isOutbound = isOutbound;
        this.properties.putAll(properties);

    }

    public String getName() {
        return name;
    }

    public boolean isOutbound() {
        return isOutbound;
    }

    public String getConnectionFunction() {
        return connectionFunction;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public String getProtocolDialectName() {
        return protocolDialectName;
    }

    public OutboundTCPComPortPoolTpl getOutboundComPortPool() {
        return outboundcomPortPool;
    }

    public InboundComPortPoolTpl getInboundComPortPoolTpl() {
        return inboundcomPortPool;
    }

    public boolean getIsDefault() {
        return isDefault;
    }

}