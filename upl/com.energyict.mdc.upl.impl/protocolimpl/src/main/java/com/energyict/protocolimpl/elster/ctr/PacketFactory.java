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

        boolean sms = true;
        if (rawPacket[0] == AbstractCTRPacket.STX[0]) {
            sms = false;
            offset++;
        }

        AddressField addressField = new AddressField(rawPacket, offset);
        offset += AddressField.LENGTH;
        System.out.println(addressField);

        ClientProfile clientProfile = new ClientProfile(rawPacket, offset);
        offset += ClientProfile.LENGTH;
        System.out.println(clientProfile);

        FunctionCode functionCode = new FunctionCode(rawPacket, offset);
        offset += FunctionCode.LENGTH;
        System.out.println(functionCode);

        Aleo aleo = new Aleo(rawPacket, offset);
        offset += Aleo.LENGTH;
        System.out.println(aleo);

        StructureCode structureCode = new StructureCode(rawPacket, offset);
        offset += StructureCode.LENGTH;
        System.out.println(structureCode);

        Channel channel = new Channel(rawPacket, offset);
        offset += Channel.LENGTH;
        System.out.println(channel);

        Cpa cpa = new Cpa(rawPacket, offset);
        offset += Cpa.LENGTH;
        System.out.println(cpa);

        Data data = new Data(rawPacket, offset);
        offset += Data.LENGTH;
        System.out.println(data);

        Crc crc = new Crc(rawPacket, offset);
        offset += Crc.LENGTH;
        System.out.println(crc);

        switch (functionCode.getFunctionType()) {
            case ACK:
            case NACK:
            case QUERY_REQUEST:
            case QUERY_RESPONSE:
            case QUERY_EVENTS_RESPONSE1:
            case QUERY_EVENTS_RESPONSE2:
            case IDENTIFICATION_REQUEST:
            case IDENTIFICATION_RESPONSE:
            case WRITE_REQUEST:
            case END_OF_SESSION_REQUEST:
                throw new NotImplementedException();
            default:
                throw new IOException();
        }
    }

}
