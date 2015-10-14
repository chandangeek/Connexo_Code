package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.search.SearchablePropertyCondition;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.impl.TableSpecs;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePropertyRelationAttributeTypeNames;

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
    public JoinClauseBuilder addConnectionTaskProperties(ConnectionTypePluggableClass connectionTypePluggableClass) {
        this.joins.add(new ConnectionTypePropertyJoinType(connectionTypePluggableClass));
        return this;
    }

    private interface JoinType {
        void appendTo(SqlBuilder sqlBuilder);
    }

    private enum Joins implements JoinType {
        EndDevice {
            @Override
            public void appendTo(SqlBuilder sqlBuilder) {
                sqlBuilder.append(" join MTR_ENDDEVICE ed on ed.AMRID = dev.id ");
            }
        },

        EndDeviceStatus {
            @Override
            public void appendTo(SqlBuilder sqlBuilder) {
                sqlBuilder.append(" join MTR_ENDDEVICESTATUS eds on eds.ENDDEVICE = ed.id ");
            }
        },

        FiniteState {
            @Override
            public void appendTo(SqlBuilder sqlBuilder) {
                sqlBuilder.append(" join FSM_STATE fs on eds.STATE = fs.id ");
            }
        },

        Batch {
            @Override
            public void appendTo(SqlBuilder sqlBuilder) {
                sqlBuilder.append(" join DDC_DEVICEINBATCH dib on dib.DEVICEID = dev.id ");
                sqlBuilder.append(" join DDC_BATCH bch on bch.ID = dib.BATCHID ");
            }
        },

        ConnectionTask {
            @Override
            public void appendTo(SqlBuilder sqlBuilder) {
                sqlBuilder.append(" join ");
                sqlBuilder.append(TableSpecs.DDC_CONNECTIONTASK.name());
                sqlBuilder.append(" ct on ct.device = dev.id ");
            }
        },

        ComTaskExecution {
            @Override
            public void appendTo(SqlBuilder sqlBuilder) {
                sqlBuilder.append(" join ");
                sqlBuilder.append(TableSpecs.DDC_COMTASKEXEC.name());
                sqlBuilder.append(" cte on cte.device = dev.id ");
            }
        },

        ComSchedule {
            @Override
            public void appendTo(SqlBuilder sqlBuilder) {
                sqlBuilder.append(" join SCH_COMSCHEDULE csh on csh.id = cte.comschedule ");
            }
        },

        MeterActivation {
            @Override
            public void appendTo(SqlBuilder sqlBuilder) {
                sqlBuilder.append(" join MTR_METERACTIVATION ma on ma.meterid = ed.id ");
            }
        },

        UsagePoint {
            @Override
            public void appendTo(SqlBuilder sqlBuilder) {
                sqlBuilder.append(" join MTR_USAGEPOINT up on up.id = ma.usagepointid ");
            }
        },

        ServiceCategory {
            @Override
            public void appendTo(SqlBuilder sqlBuilder) {
                sqlBuilder.append(" join MTR_SERVICECATEGORY serv_cat on serv_cat.id = up.servicekind ");
            }
        },

        TopologyForSlaves {
            @Override
            public void appendTo(SqlBuilder sqlBuilder) {
                sqlBuilder.append(" join DTL_PHYSICALGATEWAYREFERENCE gateway_ref on gateway_ref.originid = dev.id ");
            }
        },

        TopologyForMasters {
            @Override
            public void appendTo(SqlBuilder sqlBuilder) {
                sqlBuilder.append(" join DTL_PHYSICALGATEWAYREFERENCE gateway_ref on gateway_ref.gatewayid = dev.id ");
            }
        },

        MeterValidation {
            @Override
            public void appendTo(SqlBuilder sqlBuilder) {
                sqlBuilder.append(" left join VAL_METER_VALIDATION val on val.meterid = ed.id ");
            }
        },

        DeviceEstimation {
            @Override
            public void appendTo(SqlBuilder sqlBuilder) {
                sqlBuilder.append(" left join DDC_DEVICEESTACTIVATION est on est.device = dev.id ");
            }
        },

    }

    private static class ConnectionTypePropertyJoinType implements JoinType {
        private final ConnectionTypePluggableClass pluggableClass;

        private ConnectionTypePropertyJoinType(ConnectionTypePluggableClass pluggableClass) {
            super();
            this.pluggableClass = pluggableClass;
        }

        @Override
        public void appendTo(SqlBuilder sqlBuilder) {
            sqlBuilder.append(" join ");
            sqlBuilder.append(TableSpecs.DDC_CONNECTIONTASK.name());
            sqlBuilder.append(" ct on ct.device = dev.id and ct.connectiontypepluggableclass =");
            sqlBuilder.addLong(this.pluggableClass.getId());
            sqlBuilder.append(" join ");
            sqlBuilder.append(this.pluggableClass.findRelationType().getDynamicAttributeTableName());
            sqlBuilder.append(" props on props.");
            sqlBuilder.append(ConnectionTypePropertyRelationAttributeTypeNames.CONNECTION_TASK_ATTRIBUTE_NAME);
            sqlBuilder.append(" = ct.id");
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ConnectionTypePropertyJoinType that = (ConnectionTypePropertyJoinType) o;
            return pluggableClass.getId() == that.pluggableClass.getId();

        }

        @Override
        public int hashCode() {
            return Long.hashCode(pluggableClass.getId());
        }

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