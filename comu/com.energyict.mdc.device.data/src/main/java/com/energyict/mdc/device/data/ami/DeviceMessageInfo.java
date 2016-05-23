package com.energyict.mdc.device.data.ami;

import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.Instant;

/**
 * Created by bvn on 9/24/15.
 */
public class DeviceMessageInfo {
    @XmlJavaTypeAdapter(MessageStatusAdapter.class)
    public DeviceMessageStatus status;
    public Instant sentDate;
}
