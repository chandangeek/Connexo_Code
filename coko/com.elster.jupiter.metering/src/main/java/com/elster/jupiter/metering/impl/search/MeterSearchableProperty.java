/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl.search;

import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.util.conditions.Comparison;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.sql.SqlBuilder;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class MeterSearchableProperty implements SearchableUsagePointProperty {

    private final SearchDomain domain;
    private final PropertySpecService propertySpecService;
    private final ServerMeteringService meteringService;
    static final String FIELD_NAME = "meterActivations.meter";

    public MeterSearchableProperty(SearchDomain domain, PropertySpecService propertySpecService, ServerMeteringService meteringService) {
        super();
        this.domain = domain;
        this.propertySpecService = propertySpecService;
        this.meteringService = meteringService;
    }

    @Override
    public SearchDomain getDomain() {
        return domain;
    }

    @Override
    public boolean affectsAvailableDomainProperties() {
        return false;
    }

    @Override
    public Optional<SearchablePropertyGroup> getGroup() {
        return Optional.empty();
    }

    @Override
    public PropertySpec getSpecification() {
        return this.propertySpecService
                .specForValuesOf(new MeterValueFactory(this.meteringService))
                .named(FIELD_NAME, PropertyTranslationKeys.USAGEPOINT_METER)
                .fromThesaurus(this.meteringService.getThesaurus())
                .finish();
    }

    @Override
    public Visibility getVisibility() {
        return Visibility.REMOVABLE;
    }

    @Override
    public SelectionMode getSelectionMode() {
        return SelectionMode.SINGLE;
    }

    @Override
    public String getDisplayName() {
        return PropertyTranslationKeys.USAGEPOINT_METER.getDisplayName(this.meteringService.getThesaurus());
    }

    @Override
    public String toDisplay(Object value) {
        if (value instanceof Meter) {
            return ((Meter) value).getName();
        }
        throw new IllegalArgumentException("Value not compatible with domain");
    }

    @Override
    public List<SearchableProperty> getConstraints() {
        return Collections.emptyList();
    }

    @Override
    public void refreshWithConstrictions(List<SearchablePropertyConstriction> constrictions) {
        //nothing to refresh
    }

    public Condition toCondition(Condition specification) {
        return ListOperator.IN.contains(
                meteringService.getDataModel().query(MeterActivation.class)
                        .asSubquery(Where.where("meter").isEqualTo(((Comparison) specification).getValues()[0]),
                                "usagePoint"),
                "id");
    }

    private static class MeterValueFactory implements ValueFactory<Meter> {

        private final ServerMeteringService meteringService;

        private MeterValueFactory(ServerMeteringService meteringService) {
            super();
            this.meteringService = meteringService;
        }

        @Override
        public Meter fromStringValue(String name) {
            return meteringService.findMeterByName(name).orElse(null);
        }

        @Override
        public String toStringValue(Meter meter) {
            return meter.getName();
        }

        @Override
        public Class<Meter> getValueType() {
            return Meter.class;
        }

        @Override
        public Meter valueFromDatabase(Object object) {
            return this.fromStringValue((String) object);
        }

        @Override
        public Object valueToDatabase(Meter meter) {
            return this.toStringValue(meter);
        }

        @Override
        public void bind(PreparedStatement statement, int offset, Meter meter) throws SQLException {
            if (meter != null) {
                statement.setObject(offset, valueToDatabase(meter));
            } else {
                statement.setNull(offset, Types.VARCHAR);
            }
        }

        @Override
        public void bind(SqlBuilder builder, Meter meter) {
            if (meter != null) {
                builder.addObject(valueToDatabase(meter));
            } else {
                builder.addNull(Types.VARCHAR);
            }
        }
    }
}
