package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EndDeviceGroupEventData;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.metering.groups.EventType;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.orm.DataModel;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

@UniqueName(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.DUPLICATE_NAME + "}")
abstract class AbstractEndDeviceGroup extends AbstractGroup implements EndDeviceGroup {

    // ORM inheritance map
    static final Map<String, Class<? extends EndDeviceGroup>> IMPLEMENTERS = ImmutableMap.of(
            QueryEndDeviceGroup.TYPE_IDENTIFIER, QueryEndDeviceGroupImpl.class,
            EnumeratedEndDeviceGroup.TYPE_IDENTIFIER, EnumeratedEndDeviceGroupImpl.class);

    private final DataModel dataModel;
    private final EventService eventService;

    private String label;

    AbstractEndDeviceGroup(EventService eventService, DataModel dataModel) {
        this.eventService = eventService;
        this.dataModel = dataModel;
    }

    final DataModel getDataModel() {
        return dataModel;
    }

    @Override
    public boolean isDynamic() {
        return false;
    }

    @Override
    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public void delete() {
        this.validateNotUsed();
        dataModel.mapper(EndDeviceGroup.class).remove(this);
    }

    private void validateNotUsed() {
        this.eventService.postEvent(EventType.ENDDEVICEGROUP_VALIDATE_DELETED.topic(), new EndDeviceGroupEventData(this));
    }

}
