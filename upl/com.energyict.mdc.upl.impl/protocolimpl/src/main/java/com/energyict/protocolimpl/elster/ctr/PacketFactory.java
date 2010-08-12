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
public class PacketFactory {

    private final AddressField addressField;
    private final String encryptionKey;
    private final boolean encrypted;

    public PacketFactory(AddressField addressField, String encryptionKey, boolean encrypted) {
        this.addressField = addressField;
        this.encryptionKey = encryptionKey;
        this.encrypted = encrypted;
    }

    public PacketFactory(ProtocolProperties properties) {
        this(new AddressField(properties.getNodeAddress()), properties.getEncryptionKey());
    }

    public PacketFactory(AddressField addressField) {
        this(addressField, "", false);
    }

    public PacketFactory(AddressField addressField, String encryptionKey) {
        this(addressField, encryptionKey, (encryptionKey != null) && (encryptionKey.length() == 16));
    }

    public IdentificationRequest getIdentificationRequest() {
        IdentificationRequest request = new IdentificationRequest(addressField);


        System.out.println(ProtocolTools.getHexStringFromBytes(request.getBytes()));
        return request;
    }

    public EndOfSessionRequest getEndOfSessionRequest() {
        EndOfSessionRequest request = new EndOfSessionRequest(addressField);
        return request;
    }

}
