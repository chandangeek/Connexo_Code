/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.impl.database;

final class DatabaseConst {
    private DatabaseConst() {}

    static final String DAL_ALARM_HISTORY_PK = "DAL_ALARM_HISTORY_PK";
    static final String DAL_ALARM_HISTORY_FK_TO_ISSUE = "DAL_ALM_HST_FK_TO_ISU";

    static final String DAL_ID = "ID";
    static final String DAL_ALARM = "ALARM";

    static final String DAL_ALARM_PK = "DAL_ALARM_PK";
    static final String DAL_ALARM_FK_TO_ISSUE = "DAL_ALARM_FK_TO_ISSUE";

    static final String DAL_ALARM_OPEN_PK = "DAL_ALARM_OPEN_PK";
    static final String DAL_ALARM_OPEN_FK_TO_ISSUE = "DAL_ALM_OPN_FK_TO_ISU";

    static final String DAL_ALARM_CLEARED_STATUS = "CLEARED_STATUS";
}