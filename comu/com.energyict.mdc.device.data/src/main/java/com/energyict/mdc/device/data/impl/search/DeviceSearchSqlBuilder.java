package com.energyict.mdc.device.data.impl.search;

import com.energyict.mdc.device.data.Device;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.search.SearchablePropertyCondition;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;

import java.time.Instant;
import java.util.EnumSet;
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
    private final Set<Joins> joins = EnumSet.noneOf(Joins.class);
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
                                        }
                                        else {
                                            temp = new SqlBuilder();
                                            temp.add(fragment);
                                        }
                                    }
                                    else {
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

    private enum Joins {
        EndDevice {
            @Override
            void appendTo(SqlBuilder sqlBuilder) {
                sqlBuilder.append(" join MTR_ENDDEVICE ed on ed.AMRID = dev.id ");
            }
        },

        EndDeviceStatus {
            @Override
            void appendTo(SqlBuilder sqlBuilder) {
                sqlBuilder.append(" join MTR_ENDDEVICESTATUS eds on eds.ENDDEVICE = ed.id ");
            }
        },

        FiniteState {
            @Override
            void appendTo(SqlBuilder sqlBuilder) {
                sqlBuilder.append(" join FSM_STATE fs on eds.STATE = fs.id ");
            }
        };

        abstract void appendTo(SqlBuilder sqlBuilder);
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