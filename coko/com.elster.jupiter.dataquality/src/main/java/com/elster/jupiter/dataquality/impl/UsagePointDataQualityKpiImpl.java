/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dataquality.impl;

import com.elster.jupiter.dataquality.UsagePointDataQualityKpi;
import com.elster.jupiter.dataquality.impl.calc.KpiType;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.kpi.KpiService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.groups.UsagePointGroup;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.validation.ValidationService;

import javax.inject.Inject;
import java.time.Clock;
import java.time.temporal.TemporalAmount;

@UniqueUsagePointGroupAndPurpose(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.USAGEPOINT_GROUP_AND_PURPOSE_MUST_BE_UNIQUE + "}")
public final class UsagePointDataQualityKpiImpl extends DataQualityKpiImpl implements UsagePointDataQualityKpi {

    public enum Fields {

        USAGEPOINT_GROUP("usagePointGroup"),
        METROLOGY_PURPOSE("metrologyPurpose");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.CAN_NOT_BE_EMPTY + "}")
    private Reference<UsagePointGroup> usagePointGroup = ValueReference.absent();

    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.CAN_NOT_BE_EMPTY + "}")
    private Reference<MetrologyPurpose> metrologyPurpose = ValueReference.absent();

    @Inject
    public UsagePointDataQualityKpiImpl(DataModel dataModel, MeteringService meteringService, ValidationService validationService,
                                        EstimationService estimationService, MessageService messageService, TaskService taskService,
                                        KpiService kpiService, Clock clock) {
        super(dataModel, meteringService, validationService, estimationService, messageService, taskService, kpiService, clock);
    }

    UsagePointDataQualityKpiImpl init(UsagePointGroup usagePointGroup, MetrologyPurpose metrologyPurpose, TemporalAmount calculationFrequency) {
        this.usagePointGroup.set(usagePointGroup);
        this.metrologyPurpose.set(metrologyPurpose);
        super.setFrequency(calculationFrequency);
        return this;
    }

    @Override
    public UsagePointGroup getUsagePointGroup() {
        return usagePointGroup.orNull();
    }

    @Override
    public MetrologyPurpose getMetrologyPurpose() {
        return metrologyPurpose.orNull();
    }

    @Override
    KpiType getKpiType() {
        return KpiType.USAGE_POINT_DATA_QUALITY_KPI;
    }

    @Override
    String getRecurrentTaskName() {
        return getKpiType().recurrentTaskName(getUsagePointGroup().getName() + "/" + getMetrologyPurpose().getName());
    }
}
