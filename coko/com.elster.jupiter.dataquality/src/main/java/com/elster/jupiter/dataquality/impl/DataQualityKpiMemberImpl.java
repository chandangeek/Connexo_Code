/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.dataquality.impl;

import com.elster.jupiter.dataquality.DataQualityKpi;
import com.elster.jupiter.kpi.Kpi;
import com.elster.jupiter.kpi.KpiMember;
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

    private DataQualityKpiMemberImpl init(DataQualityKpi dataValidationKpi, Kpi childKpi) {
        this.dataQualityKpi.set(dataValidationKpi);
        this.childKpi.set(childKpi);
        return this;
    }

    static DataQualityKpiMemberImpl from(DataModel dataModel, DataQualityKpi dataValidationKpi, Kpi childKpi) {
        return dataModel.getInstance(DataQualityKpiMemberImpl.class).init(dataValidationKpi, childKpi);
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
    public String getTargetIdentifier() {
        return getChildKpi().getMembers().stream()
                .map(this::parseIdentifier)
                .findFirst().get();
    }

    private String parseIdentifier(KpiMember member) {
        return member.getName().substring(member.getName().indexOf("_") + 1);
    }
}