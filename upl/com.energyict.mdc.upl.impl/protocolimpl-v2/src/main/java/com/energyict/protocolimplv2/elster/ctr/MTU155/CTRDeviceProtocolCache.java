package com.energyict.protocolimplv2.elster.ctr.MTU155;

import com.energyict.mdc.upl.cache.DeviceProtocolCache;
import com.energyict.mdc.upl.cache.DeviceProtocolCacheXmlMarshallAdapter;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;

/**
 * Implementation of the {@link DeviceProtocolCache} interface, specific for SMS behavior of CTR protocols
 *
 * @author sva
 * @since 21/06/13 - 15:44
 */
@XmlJavaTypeAdapter(DeviceProtocolCacheXmlMarshallAdapter.class)
public class CTRDeviceProtocolCache implements DeviceProtocolCache, Serializable {

    /** The last WriteDataBlock ID used in SMS communication. **/
    int smsWriteDataBlockID = 0;

    /** The id of the mending firmware upgrade message, or -1 if no firmware upgrade is pending **/
    int pendingFirmwareMessageID = -1;

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
            this.contentChanged = true;
        }
    }

    public int getPendingFirmwareMessageID() {
        return pendingFirmwareMessageID;
    }

    public void setPendingFirmwareMessageID(int pendingFirmwareMessageID) {
        if (this.pendingFirmwareMessageID != pendingFirmwareMessageID) {
            this.pendingFirmwareMessageID = pendingFirmwareMessageID;
            this.contentChanged = true;
        }
    }

    @Override
    public boolean contentChanged() {
        return contentChanged;
    }

    @Override
    public void setContentChanged(boolean contentChanged) {
        this.contentChanged = contentChanged;
    }
}