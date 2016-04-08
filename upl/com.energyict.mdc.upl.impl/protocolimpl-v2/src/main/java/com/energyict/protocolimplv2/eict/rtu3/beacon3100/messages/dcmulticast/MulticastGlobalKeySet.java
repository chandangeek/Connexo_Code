package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.dcmulticast;

import com.energyict.dlms.axrdencoding.Structure;

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

    public MulticastGlobalKeySet(MulticastKey authenticationKey, MulticastKey encryptionKey, MulticastKey password) {
        this.authenticationKey = authenticationKey;
        this.encryptionKey = encryptionKey;
        this.password = password;
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

    public Structure toStructure() {
        Structure structure = new Structure();
        structure.addDataType(getAuthenticationKey().toDataType());
        structure.addDataType(getEncryptionKey().toDataType());
        structure.addDataType(getPassword().toDataType());
        return structure;
    }
}