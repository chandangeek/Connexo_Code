package com.elster.jupiter.demo.impl.amiscsexample;

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
