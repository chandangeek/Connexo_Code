package com.energyict.mdc.device.data.impl.search.sqlbuilder;

import com.elster.jupiter.util.sql.SqlBuilder;
import com.energyict.mdc.device.data.impl.TableSpecs;
import com.energyict.mdc.device.data.impl.search.JoinClauseBuilder;

enum Joins implements JoinType {
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

    RegisterSpec {
        @Override
        public void appendTo(SqlBuilder sqlBuilder) {
            sqlBuilder.append(" join DTC_REGISTERSPEC reg_spec on reg_spec.deviceconfigid = dev.deviceconfigid ");
        }
    },

    RegisterMeasurementType {
        @Override
        public void appendTo(SqlBuilder sqlBuilder) {
            sqlBuilder.append(" join MDS_MEASUREMENTTYPE reg_msr_type on reg_msr_type.id = reg_spec.registertypeid ");
        }
    },

    RegisterReadingType {
        @Override
        public void appendTo(SqlBuilder sqlBuilder) {
            sqlBuilder.append(" join MTR_READINGTYPE reg_rt on reg_rt.mrid = reg_msr_type.readingtype ");
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

    ChannelSpec {
        @Override
        public void appendTo(SqlBuilder sqlBuilder) {
            sqlBuilder.append(" join DTC_CHANNELSPEC ch_spec on ch_spec.deviceconfigid = dev.deviceconfigid ");
        }
    },

    ChannelMeasurementType {
        @Override
        public void appendTo(SqlBuilder sqlBuilder) {
            sqlBuilder.append(" join MDS_MEASUREMENTTYPE ch_msr_type on ch_msr_type.id = ch_spec.channeltypeid ");
        }
    },

    ChannelReadingType {
        @Override
        public void appendTo(SqlBuilder sqlBuilder) {
            sqlBuilder.append(" join MTR_READINGTYPE ch_rt on ch_rt.mrid = ch_msr_type.readingtype ");
        }
    },

    LogBook {
        @Override
        public void appendTo(SqlBuilder sqlBuilder) {
            sqlBuilder.append(" join DDC_LOGBOOK ");
            sqlBuilder.append(JoinClauseBuilder.Aliases.DEVICE_LOGBOOK);
            sqlBuilder.append(" on ");
            sqlBuilder.append(JoinClauseBuilder.Aliases.DEVICE_LOGBOOK);
            sqlBuilder.append(".DEVICEID = ");
            sqlBuilder.append(JoinClauseBuilder.Aliases.DEVICE);
            sqlBuilder.append(".id ");
        }
    },

    LogbookSpec {
        @Override
        public void appendTo(SqlBuilder sqlBuilder) {
            sqlBuilder.append(" join DTC_LOGBOOKSPEC ");
            sqlBuilder.append(JoinClauseBuilder.Aliases.LOGBOOK_SPEC);
            sqlBuilder.append(" on ");
            sqlBuilder.append(JoinClauseBuilder.Aliases.LOGBOOK_SPEC);
            sqlBuilder.append(".id = ");
            sqlBuilder.append(JoinClauseBuilder.Aliases.DEVICE_LOGBOOK);
            sqlBuilder.append(".LOGBOOKSPECID ");
        }
    },

    LogbookType{
        @Override
        public void appendTo(SqlBuilder sqlBuilder) {
            sqlBuilder.append(" join MDS_LOGBOOKTYPE ");
            sqlBuilder.append(JoinClauseBuilder.Aliases.LOGBOOK_TYPE);
            sqlBuilder.append(" on ");
            sqlBuilder.append(JoinClauseBuilder.Aliases.LOGBOOK_TYPE);
            sqlBuilder.append(".id = ");
            sqlBuilder.append(JoinClauseBuilder.Aliases.LOGBOOK_SPEC);
            sqlBuilder.append(".LOGBOOKTYPEID ");
        }
    },

    LoadProfile {
        @Override
        public void appendTo(SqlBuilder sqlBuilder) {
            sqlBuilder.append(" join DDC_LOADPROFILE ");
            sqlBuilder.append(JoinClauseBuilder.Aliases.DEVICE_LOADPROFILE);
            sqlBuilder.append(" on ");
            sqlBuilder.append(JoinClauseBuilder.Aliases.DEVICE_LOADPROFILE);
            sqlBuilder.append(".DEVICEID = ");
            sqlBuilder.append(JoinClauseBuilder.Aliases.DEVICE);
            sqlBuilder.append(".id ");
        }
    },

    LoadProfileSpec {
        @Override
        public void appendTo(SqlBuilder sqlBuilder) {
            sqlBuilder.append(" join DTC_LOADPROFILESPEC ");
            sqlBuilder.append(JoinClauseBuilder.Aliases.LOADPROFILE_SPEC);
            sqlBuilder.append(" on ");
            sqlBuilder.append(JoinClauseBuilder.Aliases.LOADPROFILE_SPEC);
            sqlBuilder.append(".id = ");
            sqlBuilder.append(JoinClauseBuilder.Aliases.DEVICE_LOADPROFILE);
            sqlBuilder.append(".LOADPROFILESPECID ");
        }
    },

    LoadProfileType{
        @Override
        public void appendTo(SqlBuilder sqlBuilder) {
            sqlBuilder.append(" join MDS_LOADPROFILETYPE ");
            sqlBuilder.append(JoinClauseBuilder.Aliases.LOADPROFILE_TYPE);
            sqlBuilder.append(" on ");
            sqlBuilder.append(JoinClauseBuilder.Aliases.LOADPROFILE_TYPE);
            sqlBuilder.append(".id = ");
            sqlBuilder.append(JoinClauseBuilder.Aliases.LOADPROFILE_SPEC);
            sqlBuilder.append(".LOADPROFILETYPEID ");
        }
    },

    ComTaskEnablement{
        @Override
        public void appendTo(SqlBuilder sqlBuilder) {
            sqlBuilder.append(" join DTC_COMTASKENABLEMENT ");
            sqlBuilder.append(JoinClauseBuilder.Aliases.COM_TASK_ENABLEMENT);
            sqlBuilder.append(" on ");
            sqlBuilder.append(JoinClauseBuilder.Aliases.COM_TASK_ENABLEMENT);
            sqlBuilder.append(".DEVICECOMCONFIG = ");
            sqlBuilder.append(JoinClauseBuilder.Aliases.DEVICE);
            sqlBuilder.append(".DEVICECONFIGID ");
        }
    },

    ComTask{
        @Override
        public void appendTo(SqlBuilder sqlBuilder) {
            sqlBuilder.append(" join CTS_COMTASK ");
            sqlBuilder.append(JoinClauseBuilder.Aliases.COM_TASK);
            sqlBuilder.append(" on ");
            sqlBuilder.append(JoinClauseBuilder.Aliases.COM_TASK);
            sqlBuilder.append(".ID = ");
            sqlBuilder.append(JoinClauseBuilder.Aliases.COM_TASK_ENABLEMENT);
            sqlBuilder.append(".COMTASK ");
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
