/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dataquality.impl;

import com.elster.jupiter.dataquality.DataQualityKpiService;
import com.elster.jupiter.dataquality.DeviceDataQualityKpi;
import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Subquery;
import com.elster.jupiter.util.sql.SqlFragment;

import javax.inject.Inject;
import java.util.List;

import static com.elster.jupiter.util.conditions.Where.where;

public class DeviceDataQualityKpiFinderImpl implements DataQualityKpiService.DeviceDataQualityKpiFinder {

    private final DataModel dataModel;

    private EndDeviceGroup endDeviceGroup;
    private Integer start;
    private Integer pageSize;

    @Inject
    public DeviceDataQualityKpiFinderImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public DataQualityKpiService.DeviceDataQualityKpiFinder forGroup(EndDeviceGroup endDeviceGroup) {
        this.endDeviceGroup = endDeviceGroup;
        return this;
    }

    @Override
    public Finder<DeviceDataQualityKpi> paged(int start, int pageSize) {
        this.start = start;
        this.pageSize = pageSize;
        return this;
    }

    @Override
    public Finder<DeviceDataQualityKpi> sorted(String sortColumn, boolean ascending) {
        // not supported
        return this;
    }

    @Override
    public List<DeviceDataQualityKpi> find() {
        DefaultFinder<DeviceDataQualityKpi> finder = DefaultFinder.of(DeviceDataQualityKpi.class, buildCondition(), dataModel, EndDeviceGroup.class);
        if (this.start != null && this.pageSize != null) {
            finder = finder.paged(this.start, this.pageSize);
        }
        return finder.defaultSortColumn(DeviceDataQualityKpiImpl.Fields.ENDDEVICE_GROUP.fieldName() + ".name").find();
    }

    private Condition buildCondition() {
        Condition condition = where(DataQualityKpiImpl.Fields.OBSOLETE_TIME.fieldName()).isNull();
        if (this.endDeviceGroup != null) {
            condition = condition.and(where(DeviceDataQualityKpiImpl.Fields.ENDDEVICE_GROUP.fieldName()).isEqualTo(endDeviceGroup));
        }
        return condition;
    }

    @Override
    public Subquery asSubQuery(String... fieldNames) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public SqlFragment asFragment(String... fieldNames) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
