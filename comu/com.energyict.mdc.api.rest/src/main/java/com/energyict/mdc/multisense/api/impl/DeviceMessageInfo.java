package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.rest.util.VersionInfo;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.energyict.mdc.common.rest.IdWithNameInfo;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;

import javax.ws.rs.core.Link;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.time.Instant;
import java.util.List;

/**
 * Created by bvn on 9/24/15.
 */
public class DeviceMessageInfo extends LinkInfo {
    public Long id;
    @XmlJavaTypeAdapter(MessageStatusAdapter.class)
    public DeviceMessageStatus status;
    public Instant sentDate;
    public LinkInfo device;
    public String trackingId;
    public LinkInfo messageSpecification;
//    public String category; SPEC
    public Instant creationDate;
    public Instant releaseDate;
    public String user;
    public String protocolInfo;
    public Boolean willBePickedUpByComTask;
    public Boolean willBePickedUpByPlannedComTask;
    public List<PropertyInfo> properties;
    public IdWithNameInfo preferredComTask;

}
