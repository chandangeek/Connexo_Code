package com.energyict.mdc.issue.datacollection;

import aQute.bnd.annotation.ProviderType;

import com.elster.jupiter.issue.share.entity.Entity;
import com.elster.jupiter.issue.share.entity.Issue;
import com.energyict.mdc.common.device.data.Device;

import java.time.Instant;


@ProviderType
public interface DataCollectionEventMetadata extends Entity {

    String getEventType();

    Device getDevice();

    Issue getIssue();

    Instant getCreateDateTime();

    void setEventType(String eventType);

    void setDevice(Device device);

    void setIssue(Issue issue);

    void setCreateDateTime(Instant dateTime);
}
