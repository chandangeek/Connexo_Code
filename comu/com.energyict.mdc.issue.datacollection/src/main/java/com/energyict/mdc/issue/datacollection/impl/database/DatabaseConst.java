/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datacollection.impl.database;

final class DatabaseConst {
    private DatabaseConst() {}

    static final String IDC_ISSUE_HISTORY_PK = "IDC_ISSUE_HISTORY_PK";
    static final String IDC_ISSUE_HISTORY_FK_TO_ISSUE = "IDC_ISS_HIST_FK_TO_ISSUE";
    static final String IDC_ISSUE_HISTORY_FK_TO_CONNECTION_TASK = "IDC_ISS_HIST_FK_TO_CON_TASK";
    static final String IDC_ISSUE_HISTORY_FK_TO_COM_TASK = "IDC_ISS_HIST_FK_TO_COM_TASK";
    static final String IDC_ISSUE_HISTORY_FK_TO_COM_SESSION = "IDC_ISS_HIST_FK_TO_COM_SESSN";

    static final String IDC_ID = "ID";
    static final String IDC_BASE_ISSUE = "ISSUE";
    static final String IDC_CONNECTION_TASK = "CON_TASK";
    static final String IDC_COMMUNICATION_TASK = "COM_TASK";
    static final String IDC_DEVICE_MRID = "DEVICE_MRID";
    static final String IDC_COM_SESSION = "COM_SESSION";
    static final String IDC_FIRST_TRY = "FIRST_TRY";
    static final String IDC_LAST_TRY = "LAST_TRY";
    static final String IDC_NUMBER_TRIES = "NUM_TRIES";

    static final String IDC_ISSUE_PK = "IDC_ISSUE_PK";
    static final String IDC_ISSUE_FK_TO_ISSUE = "IDC_ISSUE_FK_TO_ISSUE";
    static final String IDC_ISSUE_FK_TO_CONNECTION_TASK = "IDC_ISSUE_FK_TO_CON_TASK";
    static final String IDC_ISSUE_FK_TO_COM_TASK = "IDC_ISSUE_FK_TO_COM_TASK";
    static final String IDC_ISSUE_FK_TO_COM_SESSION = "IDC_ISSUE_FK_TO_COM_SESSN";

    static final String IDC_ISSUE_OPEN_PK = "IDC_ISSUE_OPEN_PK";
    static final String IDC_ISSUE_OPEN_FK_TO_ISSUE = "IDC_ISSUE_OPEN_FK_TO_ISSUE";
    static final String IDC_ISSUE_OPEN_FK_TO_CONNECTION_TASK = "IDC_ISSUE_OPEN_FK_TO_CON_TASK";
    static final String IDC_ISSUE_OPEN_FK_TO_COM_TASK = "IDC_ISSUE_OPEN_FK_TO_COM_TASK";
    static final String IDC_ISSUE_OPEN_FK_TO_COM_SESSION = "IDC_ISSUE_OPEN_FK_TO_COM_SESSN";

}