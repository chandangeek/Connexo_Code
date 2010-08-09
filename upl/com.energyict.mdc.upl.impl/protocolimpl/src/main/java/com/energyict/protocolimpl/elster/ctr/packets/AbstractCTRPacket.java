package com.energyict.protocolimpl.elster.ctr.packets;

import com.energyict.protocolimpl.elster.ctr.packets.fields.*;

/**
 * Copyrights EnergyICT
 * Date: 9-aug-2010
 * Time: 14:55:53
 */
public abstract class AbstractCTRPacket implements CTRPacket {

    private WakeUp wakeUp = null;
    private AddressField addressField = null;
    private boolean sms = false;


    public boolean isSMS() {
        return sms;
    }

    public boolean isWakeUp() {
        return getWakeUp().isWakeUpEnabled();
    }

    public WakeUp getWakeUp() {
        if (this.wakeUp == null) {
            this.wakeUp = new WakeUp();
        }
        return wakeUp;
    }

    public AddressField getAddress() {
        if (addressField == null) {
            this.addressField = new AddressField();
        }
        return addressField;
    }

    public ClientProfile getClientProfile() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public FunctionCode getFunctionCode() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Aleo getAleo() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public StructureCode getStructureCode() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Channel getChannel() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Cpa getCpa() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Data getData() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Crc getCrc() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
