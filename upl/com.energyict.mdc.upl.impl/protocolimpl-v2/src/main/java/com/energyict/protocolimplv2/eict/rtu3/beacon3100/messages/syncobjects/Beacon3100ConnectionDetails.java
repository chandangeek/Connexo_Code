package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.syncobjects;

import com.energyict.dlms.axrdencoding.*;
import com.energyict.protocolimpl.utils.ProtocolTools;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by cisac on 5/6/2016.
 */
@XmlRootElement
public class Beacon3100ConnectionDetails {
    private String llsSecret;
    private String hlsSecret;
    private String authenticationKey;
    private String encryptionKey;
    private long frameCounter;

    public Beacon3100ConnectionDetails(String llsSecret, String hlsSecret, String authenticationKey, String encryptionKey, long frameCounter){
        this.llsSecret = llsSecret;
        this.hlsSecret = hlsSecret;
        this.authenticationKey = authenticationKey;
        this.encryptionKey = encryptionKey;
        this.frameCounter = frameCounter;
    }

    //JSon constructor
    private Beacon3100ConnectionDetails() {
    }


    @XmlAttribute
    public String getLlsSecret() {
        return llsSecret;
    }

    @XmlAttribute
    public String getHlsSecret() {
        return hlsSecret;
    }

    @XmlAttribute
    public String getAuthenticationKey() {
        return authenticationKey;
    }

    @XmlAttribute
    public String getEncryptionKey() {
        return encryptionKey;
    }

    @XmlAttribute
    public long getFrameCounter() {
        return frameCounter;
    }

    public Structure toStructure() {
        final Structure structure = new Structure();
        structure.addDataType(OctetString.fromByteArray(ProtocolTools.getBytesFromHexString(getLlsSecret(), "")));
        structure.addDataType(OctetString.fromByteArray(ProtocolTools.getBytesFromHexString(getHlsSecret(), "")));
        structure.addDataType(OctetString.fromByteArray(ProtocolTools.getBytesFromHexString(getAuthenticationKey(), "")));
        structure.addDataType(OctetString.fromByteArray(ProtocolTools.getBytesFromHexString(getEncryptionKey(), "")));
        structure.addDataType(getFrameCounter() >= 0 ? new Integer64Unsigned(getFrameCounter()) : new NullData());
        return structure;
    }
}
