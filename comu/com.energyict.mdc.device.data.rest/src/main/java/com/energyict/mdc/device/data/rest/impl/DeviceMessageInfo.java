package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.energyict.mdc.common.rest.IdWithNameInfo;
import java.time.Instant;
import java.util.List;

/**
 * Created by bvn on 10/22/14.
 */
public class DeviceMessageInfo {

    public long id;
    public String trackingId;
    public DeviceMessageSpecInfo messageSpecification;
    public String category;
    public StatusInfo status;
    public Instant creationDate;
    public Instant releaseDate;
    public Instant sentDate;
    public String user;
    public String errorMessage;
    public Boolean willBePickedUpByComTask;
    public Boolean willBePickedUpByPlannedComTask;
    public List<PropertyInfo> properties;
    public IdWithNameInfo preferredComTask;

    public DeviceMessageInfo() {
    }

}

class StatusInfo {
    public String displayValue;
    public String value;
}
