package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.metering.groups.MessageSeeds;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.metering.impl.EventType;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.validation.constraints.NotNull;

public abstract class AbstractEndDeviceGroup extends AbstractGroup implements EndDeviceGroup {

    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.NAME_REQUIRED_KEY + "}")
    private String queryProviderName;
    private String label;
    public final EventService eventService;

    protected AbstractEndDeviceGroup(EventService eventService) {
        this.eventService = eventService;
    }

    public String getQueryProviderName() {
        return queryProviderName;
    }

    public boolean isDynamic() {
        return this instanceof QueryEndDeviceGroup;
    }

    public void setQueryProviderName(String queryProviderName) {
        this.queryProviderName = queryProviderName;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public void delete() {
        this.validateNotUsed();
        // TODO Do actual delete
    }

    private void validateNotUsed() {
        this.eventService.postEvent(EventType.ENDDEVICEGROUP_VALIDATE_DELETED.topic(), this);
    }

    // ORM inheritance map
    static final Map<String, Class<? extends EndDeviceGroup>> IMPLEMENTERS = ImmutableMap.<String, Class<? extends EndDeviceGroup>>of(QueryEndDeviceGroup.TYPE_IDENTIFIER, QueryEndDeviceGroupImpl.class, EnumeratedEndDeviceGroup.TYPE_IDENTIFIER, EnumeratedEndDeviceGroupImpl.class);

}
