package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.rest.util.JsonInstantAdapter;
import java.time.Instant;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Created by bvn on 10/22/14.
 */
public class DeviceMessageInfo {

    public long id;
    public String trackingId;
    public String name;
    public String category;
    public String status;
    @XmlJavaTypeAdapter(JsonInstantAdapter.class)
    public Instant creationDate;
    @XmlJavaTypeAdapter(JsonInstantAdapter.class)
    public Instant releaseDate;
    @XmlJavaTypeAdapter(JsonInstantAdapter.class)
    public Instant sentDate;
    public String user;
    public String errorMessage;
    public Boolean willBePickedUpByComTask;
    public Boolean willBePickedUpByScheduledComTask;
}
