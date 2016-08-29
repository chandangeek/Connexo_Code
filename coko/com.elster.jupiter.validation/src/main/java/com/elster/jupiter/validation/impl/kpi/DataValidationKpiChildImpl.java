package com.elster.jupiter.validation.impl.kpi;

import com.elster.jupiter.kpi.Kpi;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.validation.kpi.DataValidationKpi;
import com.elster.jupiter.validation.kpi.DataValidationKpiChild;

import com.google.inject.Inject;

public class DataValidationKpiChildImpl implements DataValidationKpiChild {

    public enum Fields {
        DATAVALIDATIONKPI("dataValidationKpi"),
        CHILDKPI("childKpi")
        ;

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    @IsPresent
    private Reference<DataValidationKpi> dataValidationKpi = Reference.empty();
    @IsPresent
    private Reference<Kpi> childKpi = Reference.empty();
    private final DataModel dataModel;

    @Inject
    DataValidationKpiChildImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    private DataValidationKpiChildImpl init(DataValidationKpi dataValidationKpi, Kpi childKpi) {
        this.dataValidationKpi.set(dataValidationKpi);
        this.childKpi.set(childKpi);
        return this;
    }

    static DataValidationKpiChildImpl from(DataModel dataModel,
                                   DataValidationKpi dataValidationKpi, Kpi childKpi) {
        return dataModel.getInstance(DataValidationKpiChildImpl.class).init(dataValidationKpi, childKpi);
    }

    @Override
    public DataValidationKpi getDataValidationKpi() {
        return dataValidationKpi.get();
    }

    @Override
    public Kpi getChildKpi() {
        return childKpi.get();
    }

    public DataModel getDataModel() {
        return dataModel;
    }

    @Override
    public void remove() {
        childKpi.get().remove();
    }

}