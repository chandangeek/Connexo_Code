package com.energyict.protocolimplv2.nta.esmr50.common.registers.enums;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;

/**
 * Created by iulian on 3/24/2017.
 */
public class LTEPingAddress {

    private String ipAddress;
    private String port;
    private AbstractDataType structure;

    public LTEPingAddress(AbstractDataType abstractDataType) {
        Structure structure = abstractDataType.getStructure();
        ipAddress = structure.getDataType(0).getOctetString().stringValue();
        port = structure.getDataType(1).getOctetString().stringValue();
    }

    public LTEPingAddress(String ipAddress, String port) {
        this.ipAddress = ipAddress;
        this.port = port;
    }

    public String toString(){
        return ipAddress+":"+port;
    }

    public AbstractDataType getStructure() {
        Structure structure = new Structure();
        OctetString osIpAddress = new OctetString(ipAddress.getBytes());
        OctetString osPort = new OctetString(port.getBytes());

        structure.addDataType(osIpAddress);
        structure.addDataType(osPort);

        return structure;
    }
}
