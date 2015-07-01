package com.energyict.protocolimplv2.eict.rtuplusserver.rtu3.messages.syncobjects;

import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned8;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 24/06/2015 - 14:38
 */
@XmlRootElement
public class RTU3ProtocolConfiguration {

    private String className;
    private int retries;
    private int timeout;

    public RTU3ProtocolConfiguration(String className, int retries, int timeout) {
        this.className = className;
        this.retries = retries;
        this.timeout = timeout;
    }

    //JSon constructor
    private RTU3ProtocolConfiguration() {
    }

    public Structure toStructure() {
        final Structure structure = new Structure();
        structure.addDataType(OctetString.fromString(getClassName()));
        structure.addDataType(new Unsigned8(getRetries()));
        structure.addDataType(new Unsigned16(getTimeout()));
        return structure;
    }

    @XmlAttribute
    public String getClassName() {
        return className;
    }

    @XmlAttribute
    public int getRetries() {
        return retries;
    }

    @XmlAttribute
    public int getTimeout() {
        return timeout;
    }
}