/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dataquality.impl;

import com.elster.jupiter.dataquality.DataQualityKpi;
import com.elster.jupiter.kpi.Kpi;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;

public class DataQualityKpiMemberImpl implements DataQualityKpiMember {

    public enum Fields {
        DATA_QUALITY_KPI("dataQualityKpi"),
        CHILD_KPI("childKpi");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    @IsPresent
    private Reference<DataQualityKpi> dataQualityKpi = Reference.empty();
    @IsPresent
    private Reference<Kpi> childKpi = Reference.empty();

    private long deviceId;
    private long usagePointId;
    private long channelContainer;

    private DataQualityKpiMemberImpl init(DataQualityKpi dataValidationKpi, Kpi childKpi, EndDevice device) {
        this.dataQualityKpi.set(dataValidationKpi);
        this.childKpi.set(childKpi);
        this.deviceId = device.getId();
        return this;
    }

    private DataQualityKpiMemberImpl init(DataQualityKpi dataValidationKpi, Kpi childKpi, UsagePoint usagePoint, ChannelsContainer purposeContainer) {
        this.dataQualityKpi.set(dataValidationKpi);
        this.childKpi.set(childKpi);
        this.usagePointId = usagePoint.getId();
        this.channelContainer = purposeContainer.getId();
        return this;
    }

    static DataQualityKpiMemberImpl from(DataModel dataModel, DataQualityKpi dataValidationKpi, Kpi childKpi, EndDevice device) {
        return dataModel.getInstance(DataQualityKpiMemberImpl.class).init(dataValidationKpi, childKpi, device);
    }

    static DataQualityKpiMemberImpl from(DataModel dataModel, DataQualityKpi dataValidationKpi, Kpi childKpi, UsagePoint usagePoint, ChannelsContainer purposeContainer) {
        return dataModel.getInstance(DataQualityKpiMemberImpl.class).init(dataValidationKpi, childKpi, usagePoint, purposeContainer);
    }

    @Override
    public Kpi getChildKpi() {
        return childKpi.get();
    }

    @Override
    public void remove() {
        childKpi.get().remove();
    }

    @Override
    public long getUsagePointId() {
        return usagePointId;
    }

    @Override
    public long getDeviceId() {
        return deviceId;
    }

    @Override
    public long getChannelContainer() {
        return channelContainer;
    }
}