package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.dcmulticast;

import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned32;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 31/03/2016 - 10:25
 */
@XmlRootElement
public class MulticastGlobalKeySet {

    /* Authentication key */
    private MulticastKey authenticationKey;
    /* Encryption key */
    private MulticastKey encryptionKey;
    /* Low-level security secret */
    private MulticastKey password;
    /* Initial TX frame counter value (null object for don't care) */
    private Unsigned32 txFrameCounter;

    public MulticastGlobalKeySet(MulticastKey authenticationKey, MulticastKey encryptionKey, MulticastKey password) {
        this.authenticationKey = authenticationKey;
        this.encryptionKey = encryptionKey;
        this.password = password;
        this.txFrameCounter = null; // don't care //TODO: maybe we care?
    }

    //JSon constructor
    private MulticastGlobalKeySet() {
    }

    @XmlAttribute
    public MulticastKey getAuthenticationKey() {
        return authenticationKey;
    }

    @XmlAttribute
    public MulticastKey getEncryptionKey() {
        return encryptionKey;
    }

    @XmlAttribute
    public MulticastKey getPassword() {
        return password;
    }

    @XmlAttribute
    public Unsigned32 getTxFrameCounter() {
        return txFrameCounter;
    }

    public Structure toStructure() {
        Structure structure = new Structure();
        structure.addDataType(getPassword().toDataType());
        structure.addDataType(getPassword().toDataType());
        structure.addDataType(getAuthenticationKey().toDataType());
        structure.addDataType(getEncryptionKey().toDataType());
        structure.addDataType(getTxFrameCounter());
        return structure;
    }
}