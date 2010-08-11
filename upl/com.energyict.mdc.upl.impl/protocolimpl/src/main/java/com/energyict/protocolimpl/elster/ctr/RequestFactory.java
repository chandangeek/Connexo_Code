package com.energyict.protocolimpl.elster.ctr;

import com.energyict.protocolimpl.elster.ctr.packets.EndOfSessionRequest;
import com.energyict.protocolimpl.elster.ctr.packets.IdentificationRequest;
import com.energyict.protocolimpl.elster.ctr.packets.fields.AddressField;
import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 * Copyrights EnergyICT
 * Date: 10-aug-2010
 * Time: 14:29:59
 */
public class RequestFactory {

    private final AddressField addressField;
    private final String encryptionKey;
    private final boolean encrypted;

    public RequestFactory(ProtocolProperties properties) {
        this.addressField = new AddressField(properties.getNodeAddress());
        this.encryptionKey = properties.getEncryptionKey();
        this.encrypted = (encryptionKey != null) && (encryptionKey.length() == 16);
    }

    public IdentificationRequest getIdentificationRequest() {
        IdentificationRequest request = new IdentificationRequest(addressField);


        System.out.println(ProtocolTools.getHexStringFromBytes(request.getBytes()));
        return request;
    }

    public EndOfSessionRequest getEndOfSessionRequest() {
        EndOfSessionRequest request = new EndOfSessionRequest(addressField);


        System.out.println(ProtocolTools.getHexStringFromBytes(request.getBytes()));
        return request;
    }



}
