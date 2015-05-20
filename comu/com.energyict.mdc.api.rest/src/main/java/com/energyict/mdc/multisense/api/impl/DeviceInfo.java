package com.energyict.mdc.multisense.api.impl;

import com.energyict.mdc.device.config.GatewayType;
import com.energyict.mdc.device.configuration.rest.GatewayTypeAdapter;
import com.energyict.mdc.device.lifecycle.config.rest.info.DeviceLifeCycleStateInfo;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement
public class DeviceInfo extends LinkInfo {
    public Long id;
    public String mIRD;
    public String serialNumber;
    public String name;
    public DeviceConfigurationInfo deviceConfiguration;

    public Long deviceProtocolPluggeableClassId;
    public Integer yearOfCertification;
    public String batch;
    public DeviceInfo masterDevice;
//    public List<DeviceTopologyInfo> slaveDevices;
    public Long nbrOfDataCollectionIssues;
    @XmlJavaTypeAdapter(GatewayTypeAdapter.class)
    public GatewayType gatewayType;
    public Boolean hasRegisters;
    public Boolean hasLogBooks;
    public Boolean hasLoadProfiles;
    public Boolean isDirectlyAddressed;
    public Boolean isGateway;
    public String serviceCategory;
    public String usagePoint;
//    public DeviceEstimationStatusInfo estimationStatus;
    public DeviceLifeCycleStateInfo state;
    public Long version;



    public DeviceInfo() {
    }

}

