package com.energyict.mdc.multisense.api.impl;

import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;

import javax.ws.rs.core.Link;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.Instant;

/**
 * Created by bvn on 9/24/15.
 */
public class DeviceMessageInfo extends LinkInfo {
    public Long id;
    @XmlJavaTypeAdapter(MessageStatusAdapter.class)
    public DeviceMessageStatus status;
    public Instant sentDate;
    public LinkInfo device;
}
