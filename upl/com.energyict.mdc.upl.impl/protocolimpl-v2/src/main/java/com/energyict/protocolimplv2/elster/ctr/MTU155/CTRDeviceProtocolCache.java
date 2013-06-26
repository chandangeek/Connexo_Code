package com.energyict.protocolimplv2.elster.ctr.MTU155;

import com.energyict.mdc.protocol.DeviceProtocolCache;

/**
 * Implementation of the {@link DeviceProtocolCache} interface, specific for SMS behavior of CTR protocols
 *
 * @author sva
 * @since 21/06/13 - 15:44
 */
public class CTRDeviceProtocolCache implements DeviceProtocolCache {

    int smsWriteDataBlockID = 0;
    boolean contentChanged;

    public CTRDeviceProtocolCache() {
        contentChanged = true;
    }

    public int getSmsWriteDataBlockID() {
        return smsWriteDataBlockID;
    }

    public void setSmsWriteDataBlockID(int smsWriteDataBlockID) {
        if (this.smsWriteDataBlockID != smsWriteDataBlockID) {
            this.smsWriteDataBlockID = smsWriteDataBlockID;
            contentChanged = true;
        }
    }

    @Override
    public boolean contentChanged() {
        return contentChanged;
    }
}