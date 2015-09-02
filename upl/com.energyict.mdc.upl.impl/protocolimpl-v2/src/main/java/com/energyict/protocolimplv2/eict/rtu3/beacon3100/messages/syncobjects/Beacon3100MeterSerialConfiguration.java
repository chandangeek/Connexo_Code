package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.syncobjects;

import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.obis.ObisCode;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 24/06/2015 - 14:09
 */
@XmlRootElement
public class Beacon3100MeterSerialConfiguration {

    private ObisCode serialNumberObisCode;
    private int clientTypeId;

    public Beacon3100MeterSerialConfiguration(ObisCode serialNumberObisCode, int clientTypeId) {
        this.serialNumberObisCode = serialNumberObisCode;
        this.clientTypeId = clientTypeId;
    }

    //JSon constructor
    private Beacon3100MeterSerialConfiguration() {
    }

    public Structure toStructure() {
        final Structure structure = new Structure();
        structure.addDataType(OctetString.fromObisCode(getSerialNumberObisCode()));
        structure.addDataType(new Unsigned32(getClientTypeId()));
        return structure;
    }

    @XmlAttribute
    public ObisCode getSerialNumberObisCode() {
        return serialNumberObisCode;
    }

    @XmlAttribute
    public int getClientTypeId() {
        return clientTypeId;
    }
}