package com.energyict.protocolimplv2.eict.rtuplusserver.rtu3.messages.syncobjects;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 24/06/2015 - 16:04
 */
@XmlRootElement
public class AllMasterData {

    private final List<RTU3ClientType> clientTypes = new ArrayList<>();
    private final List<RTU3DeviceType> deviceTypes = new ArrayList<>();
    private final List<RTU3Schedule> schedules = new ArrayList<>();

    public AllMasterData() {
    }

    @XmlAttribute
    public List<RTU3ClientType> getClientTypes() {
        return clientTypes;
    }

    @XmlAttribute
    public List<RTU3DeviceType> getDeviceTypes() {
        return deviceTypes;
    }

    @XmlAttribute
    public List<RTU3Schedule> getSchedules() {
        return schedules;
    }
}