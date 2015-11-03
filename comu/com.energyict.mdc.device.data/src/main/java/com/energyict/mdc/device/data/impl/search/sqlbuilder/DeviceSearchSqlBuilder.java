package com.energyict.mdc.device.data.impl.search.sqlbuilder;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.search.SearchablePropertyCondition;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.search.JoinClauseBuilder;
import com.energyict.mdc.device.data.impl.search.SearchableDeviceProperty;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Provides support to build a sql query that will search for {@link Device}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-29 (08:56)
 */
public class DeviceSearchSqlBuilder implements JoinClauseBuilder {

    private final List<SearchablePropertyCondition> conditions;
    private final SqlBuilder underConstruction;
    private final Set<JoinType> joins = new LinkedHashSet<>();
    private final Instant effectiveDate;
    private SqlBuilder complete;

    public DeviceSearchSqlBuilder(DataModel dataModel, List<SearchablePropertyCondition> conditions, Instant effectiveDate) {
        super();
        this.conditions = conditions;
        this.effectiveDate = effectiveDate;
        this.underConstruction = dataModel.mapper(Device.class).builder("dev");
        this.appendJoinClauses();
        this.appendWhereClauses();
    }

    private void appendJoinClauses() {
        this.conditions
                .stream()
                .map(SearchablePropertyCondition::getProperty)
                .map(SearchableDeviceProperty.class::cast)
                .forEach(p -> p.appendJoinClauses(this));
        this.joins.stream().forEach(each -> each.appendTo(this.underConstruction));
    }

    private void appendWhereClauses() {
        this.complete = new SqlBuilder();
        this.complete.add(this.underConstruction);
        this.complete.append(" where ");
        this.complete.add(
                this.conditions
                        .stream()
                        .map(FragmentBuilder::new)
                        .map(FragmentBuilder::build)
                        .reduce(
                                new SqlBuilder(),   // We could inject this.underConstruction but that would not be the Identity element
                                (sqlBuilder, fragment) -> {
                                    SqlBuilder temp;
                                    if (sqlBuilder.getText().isEmpty()) {
                                        if (fragment instanceof SqlBuilder) {
                                            temp = (SqlBuilder) fragment;
                                        } else {
                                            temp = new SqlBuilder();
                                            temp.add(fragment);
                                        }
                                    } else {
                                        temp = new SqlBuilder();
                                        temp.add(sqlBuilder);
                                        temp.append(" and ");
                                        temp.add(fragment);
                                    }
                                    return temp;
                                },
                                (sqlBuilder, sqlBuilder2) -> {
                                    sqlBuilder.add(sqlBuilder2);
                                    return sqlBuilder;
                                }));
    }

    public SqlBuilder toSqlBuilder() {
        return this.complete;
    }

    @Override
    public JoinClauseBuilder addEndDevice() {
        this.joins.add(Joins.EndDevice);
        return this;
    }

    @Override
    public JoinClauseBuilder addEndDeviceStatus() {
        this.joins.add(Joins.EndDeviceStatus);
        return this;
    }

    @Override
    public JoinClauseBuilder addFiniteState() {
        this.joins.add(Joins.FiniteState);
        return this;
    }

    @Override
    public JoinClauseBuilder addBatch() {
        this.joins.add(Joins.Batch);
        return this;
    }

    @Override
    public JoinClauseBuilder addConnectionTask() {
        this.joins.add(Joins.ConnectionTask);
        return this;
    }

    @Override
    public JoinClauseBuilder addComTaskExecution() {
        this.joins.add(Joins.ComTaskExecution);
        return this;
    }

    @Override
    public JoinClauseBuilder addComSchedule() {
        this.joins.add(Joins.ComTaskExecution);
        this.joins.add(Joins.ComSchedule);
        return this;
    }

    @Override
    public JoinClauseBuilder addUsagePoint() {
        this.joins.add(Joins.EndDevice);
        this.joins.add(Joins.MeterActivation);
        this.joins.add(Joins.UsagePoint);
        return this;
    }

    @Override
    public JoinClauseBuilder addServiceCategory() {
        addUsagePoint();
        this.joins.add(Joins.ServiceCategory);
        return this;
    }

    @Override
    public JoinClauseBuilder addTopologyForSlaves() {
        this.joins.add(Joins.TopologyForSlaves);
        return this;
    }

    @Override
    public JoinClauseBuilder addTopologyForMasters() {
        this.joins.add(Joins.TopologyForMasters);
        return this;
    }

    @Override
    public JoinClauseBuilder addMeterValidation() {
        this.addEndDevice();
        this.joins.add(Joins.MeterValidation);
        return this;
    }

    @Override
    public JoinClauseBuilder addDeviceEstimation() {
        this.joins.add(Joins.DeviceEstimation);
        return this;
    }

    @Override
    public JoinClauseBuilder addRegisterSpec() {
        this.joins.add(Joins.RegisterSpec);
        this.joins.add(Joins.RegisterMeasurementType);
        return this;
    }

    @Override
    public JoinClauseBuilder addRegisterReadingType() {
        this.joins.add(Joins.RegisterSpec);
        this.joins.add(Joins.RegisterMeasurementType);
        this.joins.add(Joins.RegisterReadingType);
        return this;
    }

    @Override
    public JoinClauseBuilder addDeviceType() {
        this.joins.add(Joins.DeviceType);
        return this;
    }

    @Override
    public JoinClauseBuilder addChannelReadingType() {
        this.joins.add(Joins.ChannelSpec);
        this.joins.add(Joins.ChannelMeasurementType);
        this.joins.add(Joins.ChannelReadingType);
        return this;
    }

    @Override
    public JoinClauseBuilder addChannelSpec() {
        this.joins.add(Joins.ChannelSpec);
        this.joins.add(Joins.ChannelMeasurementType);
        return this;
    }

    @Override
    public JoinClauseBuilder addLogbook() {
        this.joins.add(Joins.LogBook);
        return this;
    }

    @Override
    public JoinClauseBuilder addLogbookSpec() {
        this.joins.add(Joins.LogBook);
        this.joins.add(Joins.LogbookSpec);
        return this;
    }

    @Override
    public JoinClauseBuilder addLogbookType() {
        this.joins.add(Joins.LogBook);
        this.joins.add(Joins.LogbookSpec);
        this.joins.add(Joins.LogbookType);
        return this;
    }

    @Override
    public JoinClauseBuilder addLoadProfile() {
        this.joins.add(Joins.LoadProfile);
        return this;
    }

    @Override
    public JoinClauseBuilder addLoadProfileSpec() {
        this.joins.add(Joins.LoadProfile);
        this.joins.add(Joins.LoadProfileSpec);
        return this;
    }

    @Override
    public JoinClauseBuilder addLoadProfileType() {
        this.joins.add(Joins.LoadProfile);
        this.joins.add(Joins.LoadProfileSpec);
        this.joins.add(Joins.LoadProfileType);
        return this;
    }

    @Override
    public JoinClauseBuilder addComTaskEnablement() {
        this.joins.add(Joins.ComTaskEnablement);
        return this;
    }

    @Override
    public JoinClauseBuilder addComTask() {
        this.joins.add(Joins.ComTaskEnablement);
        this.joins.add(Joins.ComTask);
        return this;
    }

    @Override
    public JoinClauseBuilder addConnectionTaskProperties(ConnectionTypePluggableClass connectionTypePluggableClass) {
        this.joins.add(new ConnectionTypePropertyJoinType(connectionTypePluggableClass));
        return this;
    }

    @Override
    public JoinClauseBuilder addProtocolDialectProperties(long deviceProtocolId, String relationTableName) {
        this.joins.add(new ProtocolDialectPropertyJoinType(deviceProtocolId, relationTableName));
        return this;
    }

    private class FragmentBuilder {
        private final SearchablePropertyCondition spec;
        private final SearchableDeviceProperty property;

        private FragmentBuilder(SearchablePropertyCondition spec) {
            super();
            this.spec = spec;
            this.property = (SearchableDeviceProperty) spec.getProperty();
        }

        private SqlFragment build() {
            return this.property.toSqlFragment(this.spec.getCondition(), effectiveDate);
        }
    }
}