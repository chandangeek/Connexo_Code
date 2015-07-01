package com.energyict.protocolimplv2.eict.rtuplusserver.rtu3.messages.syncobjects;

import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.axrdencoding.Unsigned8;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 24/06/2015 - 14:09
 */
@XmlRootElement
public class RTU3ClientType {

    private long id;
    private int clientMacAddress;
    private int securityLevel;
    private int securityPolicy;

    public RTU3ClientType(long id, int clientMacAddress, int securityLevel, int securityPolicy) {
        this.id = id;
        this.clientMacAddress = clientMacAddress;
        this.securityLevel = securityLevel;
        this.securityPolicy = securityPolicy;
    }

    //JSon constructor
    private RTU3ClientType() {
    }

    public Structure toStructure() {
        final Structure structure = new Structure();
        structure.addDataType(new Unsigned32(getId()));
        structure.addDataType(new Unsigned16(getClientMacAddress()));
        structure.addDataType(new Unsigned8(getSecurityLevel()));
        structure.addDataType(new Unsigned8(getSecurityPolicy()));
        return structure;
    }

    @XmlAttribute
    public long getId() {
        return id;
    }

    @XmlAttribute
    public int getClientMacAddress() {
        return clientMacAddress;
    }

    @XmlAttribute
    public int getSecurityLevel() {
        return securityLevel;
    }

    @XmlAttribute
    public int getSecurityPolicy() {
        return securityPolicy;
    }
}