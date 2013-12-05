package com.energyict.genericprotocolimpl.common;

import com.energyict.mdc.protocol.device.data.MeterData;
import com.energyict.mdc.protocol.device.data.MeterReadingData;
import com.energyict.mdc.protocol.device.data.ProfileData;
import com.energyict.mdc.protocol.device.data.RegisterValue;
import com.energyict.mdw.core.Channel;
import com.energyict.mdw.core.Device;

/**
 * Copyrights EnergyICT
 * Date: 23/02/11
 * Time: 11:30
 */
public class StoreObjectItem {

    private final Object key;
    private final Object value;

    public StoreObjectItem(Device rtu, ProfileData profileData) {
        this.key = rtu;
        this.value = profileData;
    }

    public StoreObjectItem(Channel channel, ProfileData profileData) {
        this.key = channel;
        this.value = profileData;
    }

    public StoreObjectItem(com.energyict.mdw.amr.Register register, RegisterValue registerValue) {
        this.key = register;
        this.value = registerValue;
    }

    public StoreObjectItem(ProfileData profileData, Device rtu) {
        this.key = profileData;
        this.value = rtu;
    }

    public StoreObjectItem(MeterReadingData meterReadingData, Device rtu) {
        this.key = meterReadingData;
        this.value = rtu;
    }

    public StoreObjectItem(MeterData meterReadingData, Device rtu) {
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
