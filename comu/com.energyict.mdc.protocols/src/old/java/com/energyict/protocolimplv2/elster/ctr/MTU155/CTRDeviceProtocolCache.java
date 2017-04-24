/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155;

import com.energyict.mdc.protocol.api.DeviceProtocolCache;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.energyict.xml.DeviceProtocolCacheXmlMarshallAdapter;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.util.Date;

/**
 * Implementation of the {@link DeviceProtocolCache} interface, specific for SMS behavior of CTR protocols
 *
 * @author sva
 * @since 21/06/13 - 15:44
 */
@XmlJavaTypeAdapter(DeviceProtocolCacheXmlMarshallAdapter.class)
public class CTRDeviceProtocolCache implements DeviceProtocolCache, Serializable {

    private static final PendingFirmwareKey NO_FIRMWARE_UPGRADE_PENDING = new PendingFirmwareKey(null, new Date(1), -1);

    /** The last WriteDataBlock ID used in SMS communication. **/
    private int smsWriteDataBlockID = 0;

    /** The id of the pending firmware upgrade message, or -1 if no firmware upgrade is pending **/
    private PendingFirmwareKey pendingFirmwareMessageID = NO_FIRMWARE_UPGRADE_PENDING;

    private boolean dirty;

    public CTRDeviceProtocolCache() {
        super();
        this.markDirty();
    }

    public int getSmsWriteDataBlockID() {
        return smsWriteDataBlockID;
    }

    public void setSmsWriteDataBlockID(int smsWriteDataBlockID) {
        if (this.smsWriteDataBlockID != smsWriteDataBlockID) {
            this.smsWriteDataBlockID = smsWriteDataBlockID;
            this.markDirty();
        }
    }

    public PendingFirmwareKey getPendingFirmwareMessageID() {
        return pendingFirmwareMessageID;
    }

    public void setPendingFirmwareMessageID(DeviceMessageId deviceMessageId, Date creationDate ,long deviceId) {
        PendingFirmwareKey pendingFirmwareKey = new PendingFirmwareKey(deviceMessageId, creationDate, deviceId);
        if (!this.pendingFirmwareMessageID.equals(pendingFirmwareKey)) {
            this.pendingFirmwareMessageID = pendingFirmwareKey;
            this.markDirty();
        }
    }

    @Override
    public void markClean() {
        this.dirty = false;
    }

    @Override
    public void markDirty() {
        this.dirty = true;
    }

    @Override
    public boolean isDirty() {
        return this.dirty;
    }

    public static class PendingFirmwareKey{

        private final DeviceMessageId deviceMessageId;
        private final Date creationDate;
        private final long deviceId;

        private PendingFirmwareKey(DeviceMessageId deviceMessageId, Date creationDate, long deviceId) {
            this.deviceMessageId = deviceMessageId;
            this.creationDate = creationDate;
            this.deviceId = deviceId;
        }

        public boolean isSame(DeviceMessageId deviceMessageId, Date creationDate, long deviceId){
            PendingFirmwareKey pendingFirmwareKey = new PendingFirmwareKey(deviceMessageId, creationDate, deviceId);
            return this.equals(pendingFirmwareKey);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof PendingFirmwareKey)) {
                return false;
            }

            PendingFirmwareKey that = (PendingFirmwareKey) o;

            return deviceId == that.deviceId && creationDate.equals(that.creationDate) && deviceMessageId == that.deviceMessageId;

        }

        @Override
        public int hashCode() {
            int result = deviceMessageId.hashCode();
            result = 31 * result + creationDate.hashCode();
            result = 31 * result + (int) (deviceId ^ (deviceId >>> 32));
            return result;
        }
    }

}