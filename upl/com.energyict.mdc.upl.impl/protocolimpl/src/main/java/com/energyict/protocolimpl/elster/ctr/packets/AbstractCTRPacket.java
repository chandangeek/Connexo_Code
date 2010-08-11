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

    private static final byte[] STX = new byte[]{0x0A};
    private static final byte[] ETX = new byte[]{0x0D};

    private final WakeUp wakeUp;
    private final AddressField addressField;
    private final boolean sms;

    protected AbstractCTRPacket(AddressField addressField) {
        this(addressField, false, false);
    }

    protected AbstractCTRPacket(AddressField addressField, boolean sms) {
        this(addressField, sms, false);
    }
    
    protected AbstractCTRPacket(AddressField addressField, boolean sms, boolean useWakeUp) {
        this.addressField = addressField;
        this.wakeUp = new WakeUp(useWakeUp);
        this.sms = sms;
    }

    public Channel getChannel() {
        return new Channel();
    }

    public StructureCode getStructureCode() {
        return new StructureCode();
    }

    public AddressField getAddress() {
        return addressField;
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
        return new ClientProfile();
    }

    public Aleo getAleo() {
        return new Aleo();
    }

    public Cpa getCpa() {
        return new Cpa(getData());
    }

    public Crc getCrc() {
        return new Crc(this);
    }

    public byte[] getBytes() {
        ByteArrayOutputStream packet = new ByteArrayOutputStream();
        try {
            if (!isSMS()) {
                packet.write(STX);
            }
            packet.write(getAddress().getBytes());
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
