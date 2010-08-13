package com.energyict.protocolimpl.elster.ctr;

import com.energyict.protocolimpl.elster.ctr.packets.*;
import com.energyict.protocolimpl.elster.ctr.packets.fields.*;
import com.energyict.protocolimpl.utils.ProtocolTools;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;

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

    public static CTRPacket parse(byte[] rawPacket) throws IOException {
        int offset = 0;
        offset += (rawPacket[offset] == WakeUp.WAKE_UP_VALUE) ? 20 : 0;
        offset += (rawPacket[offset] == CTRPacket.STX) ? 1 : 0;
        offset += AddressField.LENGTH;
        offset += ClientProfile.LENGTH;
        FunctionCode functionCode = new FunctionCode(rawPacket, offset);

        switch (functionCode.getFunctionType()) {
            case IDENTIFICATION_RESPONSE:
                return new IdentificationResponse(rawPacket);
            case ACK:
                return new Ack(rawPacket);
            case NACK:
                return new NAck(rawPacket);
            case QUERY_REQUEST:
            case QUERY_RESPONSE:
            case QUERY_EVENTS_RESPONSE1:
            case QUERY_EVENTS_RESPONSE2:
            case IDENTIFICATION_REQUEST:
            case EXECUTE_REQUEST:
            case WRITE_REQUEST:
            case END_OF_SESSION_REQUEST:
                throw new NotImplementedException();
            default:
                throw new IOException();
        }
    }

}
