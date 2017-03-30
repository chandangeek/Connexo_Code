/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.search.sqlbuilder;

import com.elster.jupiter.util.sql.SqlBuilder;
import com.energyict.mdc.device.data.impl.TableSpecs;
import com.energyict.mdc.device.data.impl.search.JoinClauseBuilder;

enum Joins implements JoinType {
    EndDevice {
        @Override
        public void appendTo(SqlBuilder sqlBuilder) {
            sqlBuilder.append(" join MTR_ENDDEVICE ed on ed.AMRID = TO_CHAR(dev.id) ");
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
            sqlBuilder.append(" join DDC_BATCH bch on bch.ID = dev.BATCH_ID ");
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

    DeviceType {
        @Override
        public void appendTo(SqlBuilder sqlBuilder) {
            sqlBuilder.append(" join DTC_DEVICETYPE ");
            sqlBuilder.append(JoinClauseBuilder.Aliases.DEVICE_TYPE);
            sqlBuilder.append(" on ");
            sqlBuilder.append(JoinClauseBuilder.Aliases.DEVICE_TYPE);
            sqlBuilder.append(".id = ");
            sqlBuilder.append(JoinClauseBuilder.Aliases.DEVICE);
            sqlBuilder.append(".devicetype ");
        }
    },

    PluggableClass {
        @Override
        public void appendTo(SqlBuilder sqlBuilder) {
            sqlBuilder.append(" join CPC_PLUGGABLECLASS plug_class on dev_type.DEVICEPROTOCOLPLUGGABLEID = plug_class.id ");
        }
    },

    ;

}
