package com.energyict.genericprotocolimpl.common;

import com.energyict.mdc.protocol.api.device.BaseChannel;
import com.energyict.mdc.protocol.api.device.BaseRegister;
import com.energyict.mdc.protocol.api.device.data.MeterData;
import com.energyict.mdc.protocol.api.device.data.MeterReadingData;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.device.BaseDevice;

/**
 * Copyrights EnergyICT
 * Date: 23/02/11
 * Time: 11:30
 */
public class StoreObjectItem {

    private final Object key;
    private final Object value;

    public StoreObjectItem(BaseDevice rtu, ProfileData profileData) {
        this.key = rtu;
        this.value = profileData;
    }

    public StoreObjectItem(BaseChannel channel, ProfileData profileData) {
        this.key = channel;
        this.value = profileData;
    }

    public StoreObjectItem(BaseRegister register, RegisterValue registerValue) {
        this.key = register;
        this.value = registerValue;
    }

    public StoreObjectItem(ProfileData profileData, BaseDevice rtu) {
        this.key = profileData;
        this.value = rtu;
    }

    public StoreObjectItem(MeterReadingData meterReadingData, BaseDevice rtu) {
        this.key = meterReadingData;
        this.value = rtu;
    }

    public StoreObjectItem(MeterData meterReadingData, BaseDevice rtu) {
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
