package com.energyict.protocolimpl.elster.ctr.packets;

import com.energyict.protocolimpl.elster.ctr.packets.fields.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 9-aug-2010
 * Time: 14:55:53
 */
public abstract class AbstractCTRPacket implements CTRPacket {

    private final WakeUp wakeUp;
    private final AddressField addressField;
    private final boolean sms;
    private final Aleo aleo;
    private final ClientProfile clientProfile;
    private final StructureCode structureCode;
    private final FunctionCode functionCode;
    private final Channel channel;

    protected AbstractCTRPacket(FunctionType functionType, AddressField addressField) {
        this(functionType, addressField, false, false);
    }

    protected AbstractCTRPacket(FunctionType functionType, AddressField addressField, boolean sms) {
        this(functionType, addressField, sms, false);
    }
    
    protected AbstractCTRPacket(FunctionType functionType, AddressField addressField, boolean sms, boolean useWakeUp) {
        this.addressField = addressField;
        this.wakeUp = new WakeUp(useWakeUp);
        this.sms = sms;
        this.aleo = new Aleo();
        this.functionCode = functionType.getFunctionCode();
        this.channel = new Channel();
        this.clientProfile = new ClientProfile();
        this.structureCode = new StructureCode();
    }

    protected AbstractCTRPacket(byte[] rawPacket) { //TODO: implement method
        int offset = 0;
        this.wakeUp = new WakeUp(rawPacket[offset] == WakeUp.WAKE_UP_VALUE);
        offset += wakeUp.getBytes().length;

        this.sms = rawPacket[offset] != STX;
        offset += sms ? 0 : 1;

        this.addressField = new AddressField(rawPacket, offset);
        offset += AddressField.LENGTH;

        this.clientProfile = new ClientProfile(rawPacket, offset);
        offset += ClientProfile.LENGTH;

        this.functionCode = new FunctionCode(rawPacket, offset);
        offset += FunctionCode.LENGTH;

        this.aleo = new Aleo(rawPacket, offset);
        offset += Aleo.LENGTH;

        this.structureCode = new StructureCode(rawPacket, offset);
        offset += StructureCode.LENGTH;

        this.channel = new Channel(rawPacket, offset);
        offset += Channel.LENGTH;

        //this.cpa = new Cpa(rawPacket, offset);
        offset += Cpa.LENGTH;

    }

    protected int getDataOffset() {
        int offset = getWakeUp().getBytes().length;
        offset += isSMS() ? 0 : 1;
        offset += getAddressField().getBytes().length;
        offset += getClientProfile().getBytes().length;
        offset += getFunctionCode().getBytes().length;
        offset += getAleo().getBytes().length;
        offset += getStructureCode().getBytes().length;
        offset += getChannel().getBytes().length;
        offset += Cpa.LENGTH;
        return offset;
    };

    public Channel getChannel() {
        return channel;
    }

    public StructureCode getStructureCode() {
        return structureCode;
    }

    public boolean isSMS() {
        return sms;
    }

    public boolean isWakeUp() {
        return getWakeUp().isWakeUpEnabled();
    }

    public WakeUp getWakeUp() {
        return wakeUp;
    }

    public ClientProfile getClientProfile() {
        return clientProfile;
    }

    public Aleo getAleo() {
        return aleo;
    }

    public Cpa getCpa() {
        return new Cpa(getData());
    }

    public Crc getCrc() {
        return new Crc(this);
    }

    public FunctionCode getFunctionCode() {
        return functionCode;
    }

    public AddressField getAddressField() {
        return addressField;
    }

    public boolean isSms() {
        return sms;
    }

    public byte[] getBytes() {
        ByteArrayOutputStream packet = new ByteArrayOutputStream();
        try {
            if (!isSMS()) {
                packet.write(STX);
            }
            packet.write(getAddressField().getBytes());
            packet.write(getClientProfile().getBytes());
            packet.write(getFunctionCode().getBytes());
            packet.write(getAleo().getBytes());
            packet.write(getStructureCode().getBytes());
            packet.write(getChannel().getBytes());
            packet.write(getCpa().getBytes());
            packet.write(getData().getBytes());
            packet.write(getCrc().getBytes());
            if (!isSMS()) {
                packet.write(ETX);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return packet.toByteArray();
    }
}
