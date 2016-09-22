package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.syncobjects;

import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.protocolimpl.utils.ProtocolTools;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by cisac on 5/6/2016.
 */
@XmlRootElement
public class Beacon3100ClientDetails {

    private int dlmsClientId;
    private Beacon3100ConnectionDetails beacon3100ConnectionDetails;

    public Beacon3100ClientDetails(int dlmsClientId, Beacon3100ConnectionDetails beacon3100ConnectionDetails){

        this.dlmsClientId = dlmsClientId;
        this.beacon3100ConnectionDetails = beacon3100ConnectionDetails;
    }

    //JSon constructor
    private Beacon3100ClientDetails() {
    }

    @XmlAttribute
    public int getDlmsClientId() {
        return dlmsClientId;
    }

    @XmlAttribute
    public Beacon3100ConnectionDetails getBeacon3100ConnectionDetails() {
        return beacon3100ConnectionDetails;
    }

    public Structure toStructure() {
        final Structure structure = new Structure();
        structure.addDataType(new Unsigned16(getDlmsClientId()));
        structure.addDataType(getBeacon3100ConnectionDetails().toStructure());
        return structure;
    }
}
