/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;
import com.energyict.mdc.device.config.GatewayType;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.Instant;
import java.util.List;

@XmlRootElement
public class DeviceInfo extends LinkInfo<Long> {
    @NotNull
    public String mRID;
    public String serialNumber;
    public String name;

    public DeviceConfigurationInfo deviceConfiguration;
    public Long deviceProtocolPluggeableClassId;
    public Integer yearOfCertification;
    public String batch;
    public DeviceInfo masterDevice;
    @XmlJavaTypeAdapter(GatewayTypeAdapter.class)
    public GatewayType gatewayType;
    public Boolean isDirectlyAddressable;
    public Boolean isGateway;
    public List<LinkInfo> connectionMethods;
    public List<LinkInfo> slaveDevices;
    public List<LinkInfo> actions;
    public String lifecycleState;
    public List<LinkInfo> communicationsTaskExecutions;
    public List<LinkInfo> deviceMessages;

    public Instant installationDate;

    public String usagePoint;
    public String meterRole;
}

