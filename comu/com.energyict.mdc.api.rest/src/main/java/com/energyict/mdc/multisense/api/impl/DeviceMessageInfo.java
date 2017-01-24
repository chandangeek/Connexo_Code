package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.rest.util.hypermedia.LinkInfo;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.Instant;
import java.util.List;

/**
 * Created by bvn on 9/24/15.
 */
public class DeviceMessageInfo extends LinkInfo<Long> {
    @XmlJavaTypeAdapter(MessageStatusAdapter.class)
    public DeviceMessageStatus status;
    public Instant sentDate;
    public LinkInfo<Long> device;
    public String trackingId;
    public LinkInfo<Long> messageSpecification;
//    public String category; SPEC
    public Instant creationDate;
    public Instant releaseDate;
    public String user;
    public String protocolInfo;
    public Boolean willBePickedUpByComTask;
    public Boolean willBePickedUpByPlannedComTask;
    public List<PropertyInfo> deviceMessageAttributes;
    public IdWithNameInfo preferredComTask;

}
