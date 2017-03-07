/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dataquality.impl;

import com.elster.jupiter.dataquality.DataQualityKpiService;
import com.elster.jupiter.dataquality.UsagePointDataQualityKpi;
import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Subquery;
import com.elster.jupiter.util.sql.SqlFragment;

import javax.inject.Inject;
import java.util.List;

import static com.elster.jupiter.util.conditions.Where.where;

public class UsagePointDataQualityKpiFinderImpl implements DataQualityKpiService.UsagePointDataQualityKpiFinder {

    private final DataModel dataModel;

    private UsagePointGroup usagePointGroup;
    private MetrologyPurpose metrologyPurpose;
    private Integer start;
    private Integer pageSize;

    @Inject
    public UsagePointDataQualityKpiFinderImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public Finder<UsagePointDataQualityKpi> paged(int start, int pageSize) {
        this.start = start;
        this.pageSize = pageSize;
        return this;
    }

    @Override
    public DataQualityKpiService.UsagePointDataQualityKpiFinder forGroup(UsagePointGroup usagePointGroup) {
        this.usagePointGroup = usagePointGroup;
        return this;
    }

    @Override
    public DataQualityKpiService.UsagePointDataQualityKpiFinder forPurpose(MetrologyPurpose metrologyPurpose) {
        this.metrologyPurpose = metrologyPurpose;
        return this;
    }

    @Override
    public Finder<UsagePointDataQualityKpi> sorted(String sortColumn, boolean ascending) {
        // not supported
        return this;
    }

    @Override
    public List<UsagePointDataQualityKpi> find() {
        DefaultFinder<UsagePointDataQualityKpi> finder = DefaultFinder.of(UsagePointDataQualityKpi.class, buildCondition(), dataModel, UsagePointGroup.class, MetrologyPurpose.class);
        if (this.start != null && this.pageSize != null) {
            finder = finder.paged(this.start, this.pageSize);
        }
        return finder.defaultSortColumn(UsagePointDataQualityKpiImpl.Fields.USAGEPOINT_GROUP.fieldName() + ".name").find();
    }

    private Condition buildCondition() {
        Condition condition = where(DataQualityKpiImpl.Fields.OBSOLETE_TIME.fieldName()).isNull();
        if (this.usagePointGroup != null) {
            condition = condition.and(where(UsagePointDataQualityKpiImpl.Fields.USAGEPOINT_GROUP.fieldName()).isEqualTo(this.usagePointGroup));
        }
        if (this.metrologyPurpose != null) {
            condition = condition.and(where(UsagePointDataQualityKpiImpl.Fields.METROLOGY_PURPOSE.fieldName()).isEqualTo(this.metrologyPurpose));
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
