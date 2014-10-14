package com.elster.jupiter.metering.groups.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EnumeratedEndDeviceGroup;
import com.elster.jupiter.metering.groups.MessageSeeds;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.google.common.collect.ImmutableMap;

import javax.validation.constraints.NotNull;
import java.util.Map;

public abstract class AbstractEndDeviceGroup extends AbstractGroup implements EndDeviceGroup {

    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.NAME_REQUIRED_KEY + "}")
    private String queryProviderName;
    private String label;

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

    // ORM inheritance map
    static final Map<String, Class<? extends EndDeviceGroup>> IMPLEMENTERS = ImmutableMap.<String, Class<? extends EndDeviceGroup>>of(QueryEndDeviceGroup.TYPE_IDENTIFIER, QueryEndDeviceGroupImpl.class, EnumeratedEndDeviceGroup.TYPE_IDENTIFIER, EnumeratedEndDeviceGroupImpl.class);

}
