package com.energyict.mdc.multisense.api.impl;

import com.energyict.mdc.device.config.GatewayType;
import com.energyict.mdc.device.configuration.rest.GatewayTypeAdapter;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement
public class DeviceInfo extends LinkInfo {
    public String mIRD;
    //    public DeviceEstimationStatusInfo estimationStatus;
    public Long version;
    public String serialNumber;
    public String name;

    public DeviceConfigurationInfo deviceConfiguration;
    public Long deviceProtocolPluggeableClassId;
    public Integer yearOfCertification;
    public String batch;
    public DeviceInfo masterDevice;
    //    public List<DeviceTopologyInfo> slaveDevices;
    public Integer nbrOfDataCollectionIssues;
    @XmlJavaTypeAdapter(GatewayTypeAdapter.class)
    public GatewayType gatewayType;
    public Boolean isDirectlyAddressed;
    public Boolean isGateway;
    public String serviceCategory;
    public String usagePoint;
    public List<LinkInfo> registers;
    public List<LinkInfo> logBooks;
    public List<LinkInfo> loadProfiles;



    public DeviceInfo() {
    }

}

