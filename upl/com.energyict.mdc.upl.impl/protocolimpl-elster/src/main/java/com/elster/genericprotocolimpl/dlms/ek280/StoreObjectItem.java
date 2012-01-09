package com.elster.genericprotocolimpl.dlms.ek280;

import com.energyict.mdw.amr.RtuRegister;
import com.energyict.mdw.core.Channel;
import com.energyict.mdw.core.Rtu;
import com.energyict.protocol.*;

/**
 * Copyrights EnergyICT
 * Date: 23/02/11
 * Time: 11:30
 */
public class StoreObjectItem {

    private final Object key;
    private final Object value;

    public StoreObjectItem(Rtu rtu, ProfileData profileData) {
        this.key = rtu;
        this.value = profileData;
    }

    public StoreObjectItem(Channel channel, ProfileData profileData) {
        this.key = channel;
        this.value = profileData;
    }

    public StoreObjectItem(RtuRegister register, RegisterValue registerValue) {
        this.key = register;
        this.value = registerValue;
    }

    public StoreObjectItem(ProfileData profileData, Rtu rtu) {
        this.key = profileData;
        this.value = rtu;
    }

    public StoreObjectItem(MeterReadingData meterReadingData, Rtu rtu) {
        this.key = meterReadingData;
        this.value = rtu;
    }

    public Object getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("StoreObjectItem");
        sb.append("{key=").append(key);
        sb.append(", value=").append(value);
        sb.append('}');
        return sb.toString();
    }
}
