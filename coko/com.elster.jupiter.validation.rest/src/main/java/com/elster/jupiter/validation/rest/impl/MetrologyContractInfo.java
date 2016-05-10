package com.elster.jupiter.validation.rest.impl;



import com.elster.jupiter.metering.config.MetrologyContract;

public class MetrologyContractInfo {
    public long id;
    public String name;

    public MetrologyContractInfo() {
    }

    public MetrologyContractInfo(MetrologyContract metrologyContract) {
        id = metrologyContract.getId();
        name = metrologyContract.getMetrologyPurpose().getName();
    }
}
