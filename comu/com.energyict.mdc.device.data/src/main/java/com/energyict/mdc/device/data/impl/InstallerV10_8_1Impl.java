/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.upgrade.FullInstaller;

import org.apache.commons.io.IOUtils;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InstallerV10_8_1Impl implements FullInstaller {
    private final DataModel dataModel;
    private final OrmService ormService;

    @Inject
    public InstallerV10_8_1Impl(DataModel dataModel, OrmService ormService) {
        this.dataModel = dataModel;
        this.ormService = ormService;
    }

    @Override
    public void install(DataModelUpgrader dataModelUpgrader, Logger logger) {
        if (!ormService.isTest()) {
            execute(dataModel, createConnectionTaskIndex());
            prepareDashboard(logger);
        }
    }

    private String createConnectionTaskIndex() {
        return "CREATE INDEX IX_CONNECTIONTASK_IDASC ON DDC_CONNECTIONTASK (COMPORTPOOL, NEXTEXECUTIONTIMESTAMP, mod(ID, 100), ID)";
    }

    public void prepareDashboard(Logger logger){
        createDashBordTables();
        createOrUpdateDashboardProcedures(logger);
        createDashBordJobs();
    }

    private void createDashBordTables(){
        if(!dataModel.doesTableExist("DASHBOARD_CONTASKBREAKDOWN")){
            execute(dataModel, getDashboardContaskbreakdownTableStatement());
        }
        if(!dataModel.doesTableExist("DASHBOARD_CONTYPEHEATMAP")){
            execute(dataModel, getDashboardContypeheatmapTableStatement());
        }
        if(!dataModel.doesTableExist("DASHBOARD_CTLCSSUCINDCOUNT")){
            execute(dataModel, getDashboardCtlcssucindcountTableStatement());
        }
        if(!dataModel.doesTableExist("DASHBOARD_CTLCSWITHATLSTONEFT")){
            execute(dataModel, getDashboardCtlcswithatlstoneftTableStatement());
        }
        if(!dataModel.doesTableExist("DASHBOARD_COMTASK")){
            execute(dataModel, getDashboardComtaskTableStatement());
        }
    }

    private String getDashboardContaskbreakdownTableStatement(){
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("create table DASHBOARD_CONTASKBREAKDOWN (");
        sqlBuilder.append("  grouperby               VARCHAR2(15),");
        sqlBuilder.append("  devicetype              NUMBER,");
        sqlBuilder.append("  mrid                    VARCHAR2(30),");
        sqlBuilder.append("  item                    VARCHAR2(20),");
        sqlBuilder.append("  taskstatus              VARCHAR2(30),");
        sqlBuilder.append("  count                   NUMBER");
        sqlBuilder.append(")");
        return sqlBuilder.toString();
    }

    private String getDashboardContypeheatmapTableStatement(){
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("create table DASHBOARD_CONTYPEHEATMAP (");
        sqlBuilder.append("  connectiontypepluggableclass NUMBER,");
        sqlBuilder.append("  devicetype                   NUMBER,");
        sqlBuilder.append("  mrid                    VARCHAR2(30),");
        sqlBuilder.append("  comportpool                  NUMBER,");
        sqlBuilder.append("  completeSucces               NUMBER,");
        sqlBuilder.append("  atLeastOneFailure            NUMBER,");
        sqlBuilder.append("  failureSetupError            NUMBER,");
        sqlBuilder.append("  failureBroken                NUMBER,");
        sqlBuilder.append("  failureInterrupted           NUMBER,");
        sqlBuilder.append("  failureNot_Execute           NUMBER");
        sqlBuilder.append(")");
        return sqlBuilder.toString();
    }

    private String getDashboardCtlcssucindcountTableStatement(){
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("create table DASHBOARD_CTLCSSUCINDCOUNT (");
        sqlBuilder.append("  devicetype                  NUMBER,");
        sqlBuilder.append("  mrid                    VARCHAR2(30),");
        sqlBuilder.append("  lastSessionSuccessIndicator NUMBER,");
        sqlBuilder.append("  count                       NUMBER");
        sqlBuilder.append(")");
        return sqlBuilder.toString();
    }

    private String getDashboardCtlcswithatlstoneftTableStatement(){
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("create table DASHBOARD_CTLCSWITHATLSTONEFT (");
        sqlBuilder.append("  devicetype                  NUMBER,");
        sqlBuilder.append("  mrid                    VARCHAR2(30),");
        sqlBuilder.append("  count                       NUMBER");
        sqlBuilder.append(")");
        return sqlBuilder.toString();
    }

    private String getDashboardComtaskTableStatement(){
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("create table DASHBOARD_COMTASK (");
        sqlBuilder.append("  querytype               VARCHAR2(15),");
        sqlBuilder.append("  devicetype              NUMBER,");
        sqlBuilder.append("  mrid                    VARCHAR2(30),");
        sqlBuilder.append("  lastsesshighestcompcode NUMBER,");
        sqlBuilder.append("  heatmapcount            NUMBER,");
        sqlBuilder.append("  tasktype                VARCHAR2(20),");
        sqlBuilder.append("  status                  VARCHAR2(30),");
        sqlBuilder.append("  comschedule             NUMBER,");
        sqlBuilder.append("  count                   NUMBER");
        sqlBuilder.append(")");
        return sqlBuilder.toString();
    }

    void createOrUpdateDashboardProcedures(Logger logger){
        try {
            execute(dataModel, getStoredProcedureScript("con_task_dashboard_procedure.sql"));
            execute(dataModel, getStoredProcedureScript("com_task_dashboard_procedure.sql"));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Errors on creation dashboard stored procedures!", e);
        }
    }

    static String getStoredProcedureScript(String storedProcedureFile) throws IOException {
        StringWriter writer = new StringWriter();
        try(InputStream inputStream = InstallerV10_8_1Impl.class.getResourceAsStream("/sql/v10.8.1/"+storedProcedureFile);) {
            IOUtils.copy(inputStream, writer, "UTF-8");
        }
        return writer.toString();
    }

    private void createDashBordJobs(){
        execute(dataModel, getRefreshConTaskDashboardJobStatement());
        execute(dataModel, getRefreshComTaskDashboardJobStatement());
    }

    private String getRefreshConTaskDashboardJobStatement() {
        return dataModel.getRefreshJobStatement("REF_CONTASK_DASHBOARD", "CONNECTION_TASK_STATUS();", 5);
    }

    private String getRefreshComTaskDashboardJobStatement() {
        return dataModel.getRefreshJobStatement("REF_COMTASK_DASHBOARD", "COMMUNICATION_TASK_STATUS();", 5);
    }
}
