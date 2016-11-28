package com.energyict.mdc.device.alarms.entity;

import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface DeviceAlarm extends Issue {

    String getDeviceMRID();

    void setDeviceMRID(String deviceMRID);

    List<EndDeviceEventRecord> getRelatedEventRecords();

    EndDeviceEventRecord getCurrentEventRecord();
}