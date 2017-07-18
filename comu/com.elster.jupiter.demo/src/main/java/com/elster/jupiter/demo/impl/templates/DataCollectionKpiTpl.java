/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.demo.impl.templates;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.builders.DynamicKpiBuilder;
import com.energyict.mdc.device.data.kpi.DataCollectionKpi;

public enum DataCollectionKpiTpl implements Template<DataCollectionKpi, DynamicKpiBuilder> {
    NORTH_REGION(DeviceGroupTpl.NORTH_REGION),
    SOUTH_REGION(DeviceGroupTpl.SOUTH_REGION),
    ;

    private DeviceGroupTpl deviceGroup;

    DataCollectionKpiTpl(DeviceGroupTpl deviceGroup) {
        this.deviceGroup = deviceGroup;
    }

    @Override
    public Class<DynamicKpiBuilder> getBuilderClass() {
        return DynamicKpiBuilder.class;
    }

    @Override
    public DynamicKpiBuilder get(DynamicKpiBuilder builder) {
        return builder.withGroup(Builders.from(deviceGroup).get());
    }
}
