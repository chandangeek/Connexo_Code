package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.metering.config.DefaultMetrologyPurpose;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.orm.DataModel;

import static com.elster.jupiter.util.conditions.Where.where;

public class MetrologyPurposeBuilderImpl implements MetrologyPurpose.MetrologyPurposeBuilder {
    private final DataModel dataModel;

    private String name;
    private String description;

    public MetrologyPurposeBuilderImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public MetrologyPurpose fromDefaultMetrologyPurpose(DefaultMetrologyPurpose defaultMetrologyPurpose) {
        return dataModel.query(MetrologyPurpose.class)
                .select(where(MetrologyPurposeImpl.Fields.DEFAULT_PURPOSE.fieldName()).isNotNull())
                .stream()
                .map(MetrologyPurposeImpl.class::cast)
                .filter(metrologyPurpose -> metrologyPurpose.getDefaultMetrologyPurpose().get() == defaultMetrologyPurpose)
                .findAny()
                .orElseGet(() -> createDefaultMetrologyPurpose(defaultMetrologyPurpose));
    }

    private MetrologyPurposeImpl createDefaultMetrologyPurpose(DefaultMetrologyPurpose defaultMetrologyPurpose) {
        MetrologyPurposeImpl metrologyPurpose = dataModel.getInstance(MetrologyPurposeImpl.class)
                .init(defaultMetrologyPurpose);
        metrologyPurpose.save();
        return metrologyPurpose;
    }

    @Override
    public MetrologyPurpose.MetrologyPurposeBuilder withName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public MetrologyPurpose.MetrologyPurposeBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    @Override
    public MetrologyPurpose create() {
        MetrologyPurposeImpl metrologyPurpose = dataModel.getInstance(MetrologyPurposeImpl.class)
                .init(this.name, this.description);
        metrologyPurpose.save();
        return metrologyPurpose;
    }
}
