package com.energyict.mdc.multisense.api.impl;

import com.energyict.mdc.device.config.GatewayType;
import com.energyict.mdc.device.configuration.rest.GatewayTypeAdapter;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement
public class DeviceInfo extends LinkInfo {
    public String mRID;
    public Long version;
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


    public DeviceInfo() {
    }

}

