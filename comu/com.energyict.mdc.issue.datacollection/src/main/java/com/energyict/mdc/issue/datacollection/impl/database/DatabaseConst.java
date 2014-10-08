package com.energyict.mdc.issue.datacollection.impl.database;

public final class DatabaseConst {
    private DatabaseConst() {}

    public static final String IDC_ISSUE_OPEN_JRNL_TABLE_NAME = "IDC_ISSUE_OPEN_JRNL";
    public static final String IDC_ISSUE_HISTORY_JRNL_TABLE_NAME = "IDC_ISSUE_HISTORY_JRNL";

    public static final String IDC_ISSUE_HISTORY_PK = "IDC_ISSUE_HISTORY_PK";
    public static final String IDC_ISSUE_HISTORY_FK_TO_ISSUE = "IDC_ISS_HIST_FK_TO_ISSUE";
    public static final String IDC_ISSUE_HISTORY_FK_TO_CONNECTION_TASK = "IDC_ISS_HIST_FK_TO_CON_TASK";
    public static final String IDC_ISSUE_HISTORY_FK_TO_COM_TASK = "IDC_ISS_HIST_FK_TO_COM_TASK";

    public static final String IDC_ID = "ID";
    public static final String IDC_BASE_ISSUE = "ISSUE";
    public static final String IDC_CONNECTION_TASK = "CON_TASK";
    public static final String IDC_COMMUNICATION_TASK = "COM_TASK";
    public static final String IDC_DEVICE_NUMBER = "DEVICE_NUMBER";

    public static final String IDC_ISSUE_PK = "IDC_ISSUE_PK";
    public static final String IDC_ISSUE_FK_TO_ISSUE = "IDC_ISSUE_FK_TO_ISSUE";
    public static final String IDC_ISSUE_FK_TO_CONNECTION_TASK = "IDC_ISSUE_FK_TO_CON_TASK";
    public static final String IDC_ISSUE_FK_TO_COM_TASK = "IDC_ISSUE_FK_TO_COM_TASK";

    public static final String IDC_ISSUE_OPEN_PK = "IDC_ISSUE_OPEN_PK";
    public static final String IDC_ISSUE_OPEN_FK_TO_ISSUE = "IDC_ISSUE_OPEN_FK_TO_ISSUE";
    public static final String IDC_ISSUE_OPEN_FK_TO_CONNECTION_TASK = "IDC_ISSUE_OPEN_FK_TO_CON_TASK";
    public static final String IDC_ISSUE_OPEN_FK_TO_COM_TASK = "IDC_ISSUE_OPEN_FK_TO_COM_TASK";
}
