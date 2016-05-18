package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.syncobjects;

import com.energyict.dlms.axrdencoding.*;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Arrays;

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
    private int securitySuite;
    private int securityLevel;
    private int securityPolicy;
    private boolean isFirmware140OrAbove = false;

    public Beacon3100ClientType(long id, int clientMacAddress, int securitySuite, int securityLevel, int securityPolicy) {
        this.id = id;
        this.clientMacAddress = clientMacAddress;
        this.securitySuite = securitySuite;
        this.securityLevel = securityLevel;
        this.securityPolicy = securityPolicy;
    }

    //JSon constructor
    private Beacon3100ClientType() {
    }

    public boolean equals(AbstractDataType obj){
        try {
            byte[] otherByteArray = obj.getBEREncodedByteArray();
            byte[] thisByteArray = toStructure().getBEREncodedByteArray();

            return Arrays.equals(thisByteArray, otherByteArray);
        }catch (Exception ex){
            return false;
        }
    }

    public void setIsFirmware140orAbove(boolean isFirmware140OrAbove){
        this.isFirmware140OrAbove = isFirmware140OrAbove;
    }

    public boolean equals(Beacon3100ClientType anotherClientType){
        return this.equals(anotherClientType.toStructure());
    }

    public Structure toStructure() {
        final Structure structure = new Structure();
        structure.addDataType(new Unsigned32(getId()));
        structure.addDataType(new Unsigned16(getClientMacAddress()));
        structure.addDataType(new TypeEnum(getSecuritySuite()));
        structure.addDataType(new TypeEnum(getSecurityLevel()));
        if(isFirmware140OrAbove) {
            structure.addDataType(new TypeEnum(getConvertedSecurityPolicy()));
        } else {
            structure.addDataType(new TypeEnum(getSecurityPolicy()));
        }
        return structure;
    }

    private int getConvertedSecurityPolicy(){
        int policy = 0;
        final int SECURITYPOLICY_NONE = 0;
        final int SECURITYPOLICY_AUTHENTICATION = 1;
        final int SECURITYPOLICY_ENCRYPTION = 2;
        final int SECURITYPOLICY_BOTH = 3;
        switch (getSecurityPolicy()) {
            case SECURITYPOLICY_NONE:
                return 0;
            case SECURITYPOLICY_AUTHENTICATION:
                return Integer.parseInt("00100100", 2);
            case SECURITYPOLICY_ENCRYPTION:
                return Integer.parseInt("01001000", 2);
            case SECURITYPOLICY_BOTH:
                return Integer.parseInt("01101100", 2);
        }
        return policy;
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

    @XmlAttribute
    public int getSecuritySuite() {
        return securitySuite;
    }
}