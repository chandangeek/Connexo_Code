package com.energyict.mdc.engine.impl.commands.offline;

import com.energyict.mdc.device.config.LogBookSpec;
import com.energyict.mdc.upl.offline.OfflineLogBookSpec;
import com.energyict.obis.ObisCode;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 18/01/2017 - 14:40
 */
@XmlRootElement
public class OfflineLogBookSpecImpl implements OfflineLogBookSpec {

    private final int logBookSpecId;
    private final long deviceConfigId;
    private final long logBookTypeId;
    private final ObisCode deviceObisCode;
    private final ObisCode obisCode;

    protected OfflineLogBookSpecImpl(LogBookSpec logBookSpec) {
        logBookSpecId = (int) logBookSpec.getId();
        deviceConfigId = logBookSpec.getDeviceConfiguration().getId();
        logBookTypeId = logBookSpec.getLogBookType().getId();
        deviceObisCode = logBookSpec.getDeviceObisCode();
        obisCode = logBookSpec.getObisCode();
    }

    @Override
    @XmlAttribute
    public int getLogBookSpecId() {
        return logBookSpecId;
    }

    @Override
    @XmlAttribute
    public long getDeviceConfigId() {
        return deviceConfigId;
    }

    @Override
    @XmlAttribute
    public long getLogBookTypeId() {
        return logBookTypeId;
    }

    @Override
    @XmlAttribute
    public ObisCode getDeviceObisCode() {
        return deviceObisCode;
    }

    @Override
    @XmlAttribute
    public ObisCode getObisCode() {
        return obisCode;
    }

    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    public void setXmlType(String ignore) {
        // For xml unmarshalling purposes only
    }
}