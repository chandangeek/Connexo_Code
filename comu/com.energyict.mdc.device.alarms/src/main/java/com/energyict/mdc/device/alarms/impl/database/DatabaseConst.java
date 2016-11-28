package com.energyict.mdc.device.alarms.impl.database;

final class DatabaseConst {
    private DatabaseConst() {}

    static final String DAL_ALARM_HISTORY_PK = "DAL_ALARM_HISTORY_PK";
    static final String DAL_ALARM_HISTORY_FK_TO_ALARM = "DAL_ALR_HIST_FK_TO_ALARM";

    static final String DAL_ID = "ID";
    static final String DAL_BASE_ALARM = "ALARM";

    static final String DAL_ALARM_PK = "DAL_ALARM_PK";
    static final String DAL_ALARM_FK_TO_ALARM = "DAL_ALARM_FK_TO_ALARM";

    static final String DAL_ALARM_OPEN_PK = "DAL_ALARM_OPEN_PK";
    static final String DAL_ALARM_OPEN_FK_TO_ALARM = "DAL_ALARM_OPEN_FK_TO_ALARM";

    static final String DAL_DEVICE_MRID = "DAL_DEVICE_MRID_FK_TO_ALARM";//DEVICE
    static final String DAL_ALARM_EVENT_TYPE = "DAL_ALARM_EVENT_TYPE";
}