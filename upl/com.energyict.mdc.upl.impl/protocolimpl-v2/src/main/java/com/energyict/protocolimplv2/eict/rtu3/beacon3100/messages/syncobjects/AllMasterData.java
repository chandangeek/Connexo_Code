package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.syncobjects;

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

    private final List<Beacon3100ClientType> clientTypes = new ArrayList<>();
    private final List<Beacon3100DeviceType> deviceTypes = new ArrayList<>();
    private final List<Beacon3100Schedule> schedules = new ArrayList<>();
    private final List<String> warningKeys = new ArrayList<>();
    private final List<String> warningArguments = new ArrayList<>();

    public AllMasterData() {
    }

    @XmlAttribute
    public List<Beacon3100ClientType> getClientTypes() {
        return clientTypes;
    }

    @XmlAttribute
    public List<Beacon3100DeviceType> getDeviceTypes() {
        return deviceTypes;
    }

    @XmlAttribute
    public List<Beacon3100Schedule> getSchedules() {
        return schedules;
    }

    /**
     * A list of translatable warning keys that occurred while parsing the EIServer model.
     * These will be used as issues for the collected message result.
     */
    @XmlAttribute
    public List<String> getWarningKeys() {
        return warningKeys;
    }

    /**
     * A list of arguments (in the same order as the warning keys) that will be used when translating the warning keys.
     */
    @XmlAttribute
    public List<String> getWarningArguments() {
        return warningArguments;
    }
}