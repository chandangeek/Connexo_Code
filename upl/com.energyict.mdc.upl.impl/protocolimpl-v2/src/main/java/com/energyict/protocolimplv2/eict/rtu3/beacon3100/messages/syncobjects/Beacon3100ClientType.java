package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.syncobjects;

import com.energyict.dlms.axrdencoding.*;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 24/06/2015 - 14:09
 */
@XmlRootElement
public class Beacon3100ClientType {

    private long id;
    private int clientMacAddress;
    private int securityLevel;
    private int securityPolicy;

    public Beacon3100ClientType(long id, int clientMacAddress, int securityLevel, int securityPolicy) {
        this.id = id;
        this.clientMacAddress = clientMacAddress;
        this.securityLevel = securityLevel;
        this.securityPolicy = securityPolicy;
    }

    //JSon constructor
    private Beacon3100ClientType() {
    }

    public Structure toStructure() {
        final Structure structure = new Structure();
        structure.addDataType(new Unsigned32(getId()));
        structure.addDataType(new Unsigned16(getClientMacAddress()));
        structure.addDataType(new TypeEnum(getSecurityLevel()));
        structure.addDataType(new TypeEnum(getSecurityPolicy()));
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