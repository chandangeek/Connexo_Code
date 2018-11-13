package com.energyict.smartmeterprotocolimpl.nta.esmr50.common;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.cosem.ObjectReference;
import com.energyict.dlms.cosem.methods.DLMSClassMethods;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.common.customdlms.cosem.DSMR4_MbusClient;
import com.energyict.smartmeterprotocolimpl.nta.esmr50.common.attributes.ESMR50MbusClientAttributes;
import com.energyict.smartmeterprotocolimpl.nta.esmr50.common.methods.ESMR50MbusClientMethods;

import java.io.IOException;

@Deprecated
public class ESMR50MbusClient extends DSMR4_MbusClient {

    private int version;

    public ESMR50MbusClient(ProtocolLink protocolLink, ObjectReference objectReference, int version) {
        super(protocolLink, objectReference, version);
    }

    public void transferFUAK(byte[] encryptedRequest) throws IOException {
        DLMSClassMethods method = ESMR50MbusClientMethods.TRANSFER_FUAK.forVersion(getUsedVersion());
        OctetString fuak = new OctetString(encryptedRequest);
        methodInvoke(method, fuak.getBEREncodedByteArray());
    }

    public byte[] readDetailedVersionInformation(Unsigned8 data) throws IOException {
        DLMSClassMethods method = ESMR50MbusClientMethods.READ_DETAILED_VERSION_INFORMATION;
        return methodInvoke(method, data.getBEREncodedByteArray());
    }

    public Unsigned8 getVersion() throws IOException {
        return new Unsigned8(getResponseData(ESMR50MbusClientAttributes.VERSION.forVersion(getUsedVersion())), 0);
    }

    /**
     * Write the given unsigned8 version to the device
     *
     * @param version
     * @throws IOException
     */
    public void setVersion(Unsigned8 version) throws IOException {
        write(ESMR50MbusClientAttributes.VERSION.forVersion(getUsedVersion()), version.getBEREncodedByteArray());
    }

    /**
     * Write the version to the meter.
     *
     * @param version
     * @throws IOException
     */
    public void setVersion(int version) throws IOException {
        setVersion(new Unsigned8(version));
    }

    private int getUsedVersion(){
        return 0; // todo Replaced getUsedVersion, access type modified in Connexo
    }

}
