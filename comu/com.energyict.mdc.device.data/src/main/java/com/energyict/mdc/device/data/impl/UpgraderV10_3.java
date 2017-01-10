package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.DataModelUpgrader;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.Version;
import com.elster.jupiter.upgrade.Upgrader;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.scheduling.model.ComSchedule;

import javax.inject.Inject;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.streams.Predicates.not;

class UpgraderV10_3 implements Upgrader {

    private final DataModel dataModel;

    @Inject
    UpgraderV10_3(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public void migrate(DataModelUpgrader dataModelUpgrader) {
        upgradeExistingScheduledComTaskExecutions();
        dataModelUpgrader.upgrade(dataModel, Version.version(10,3));
    }

    private void upgradeExistingScheduledComTaskExecutions() {
        Map<Device, Map<ComSchedule, List<ComTaskExecution>>> collected = dataModel.mapper(ComTaskExecution.class).find()
                .stream()
                .filter(not(ComTaskExecution::isObsolete))
                .filter(ComTaskExecution::usesSharedSchedule)
                .collect(Collectors.groupingBy(ComTaskExecution::getDevice, Collectors.groupingBy(scheduledComTaskExecution -> scheduledComTaskExecution.getComSchedule().get())));

        for (Device device : collected.keySet()) {
            List<ComTaskEnablement> comTaskEnablements = device.getDeviceConfiguration().getComTaskEnablements();
            Map<ComSchedule, List<ComTaskExecution>> executionsPerSchedule = collected.get(device);
            executionsPerSchedule.keySet().stream()
                    .forEach(comschedule -> {
                        List<ComTaskEnablement> validEnablementsForSchedule = comTaskEnablements
                                .stream()
                                .filter(comTaskEnablement -> comschedule.containsComTask(comTaskEnablement.getComTask()))
                                .collect(Collectors.toList());

                        doSQL(validEnablementsForSchedule, executionsPerSchedule.get(comschedule).get(0));

                    });
        }
    }

    private void doSQL(List<ComTaskEnablement> validEnablementsForSchedule, ComTaskExecution comTaskExecution) {
        List<String> sql = new ArrayList<>();
        String updateSQL = "UPDATE DDC_COMTASKEXEC SET COMTASK ='" + validEnablementsForSchedule.get(0).getComTask().getId() + "', PROTOCOLDIALECTCONFIGPROPS = '" + validEnablementsForSchedule.get(0).getProtocolDialectConfigurationProperties().getId() + "' WHERE ID='" + comTaskExecution.getId() + "'";
        sql.add(updateSQL);

        for (int i = 1; i < validEnablementsForSchedule.size(); i++) {
            ComTaskEnablement comTaskEnablement = validEnablementsForSchedule.get(i);
            String insertSQL = "INSERT INTO DDC_COMTASKEXEC (ID, VERSIONCOUNT, CREATETIME, MODTIME, DISCRIMINATOR, DEVICE, COMTASK, COMSCHEDULE, NEXTEXECUTIONSPECS, LASTEXECUTIONTIMESTAMP, " +
                    "NEXTEXECUTIONTIMESTAMP, COMPORT, OBSOLETE_DATE, PRIORITY, USEDEFAULTCONNECTIONTASK, CURRENTRETRYCOUNT, PLANNEDNEXTEXECUTIONTIMESTAMP, EXECUTIONPRIORITY, EXECUTIONSTART, LASTSUCCESSFULCOMPLETION, " +
                    "LASTEXECUTIONFAILED, CONNECTIONTASK, PROTOCOLDIALECTCONFIGPROPS, IGNORENEXTEXECSPECS, LASTSESSION, LASTSESS_HIGHESTPRIOCOMPLCODE, LASTSESS_SUCCESSINDICATOR, ONHOLD) " +
                    "SELECT DDC_COMTASKEXECID.nextval, '0', CREATETIME, MODTIME, DISCRIMINATOR, DEVICE, '" + comTaskEnablement.getComTask().getId() + "', COMSCHEDULE, NEXTEXECUTIONSPECS, LASTEXECUTIONTIMESTAMP, " +
                    "NEXTEXECUTIONTIMESTAMP, COMPORT, OBSOLETE_DATE, PRIORITY, USEDEFAULTCONNECTIONTASK, CURRENTRETRYCOUNT, PLANNEDNEXTEXECUTIONTIMESTAMP, EXECUTIONPRIORITY, EXECUTIONSTART, LASTSUCCESSFULCOMPLETION, " +
                    "LASTEXECUTIONFAILED, CONNECTIONTASK, '" + comTaskEnablement.getProtocolDialectConfigurationProperties().getId() + "', IGNORENEXTEXECSPECS, LASTSESSION, LASTSESS_HIGHESTPRIOCOMPLCODE, LASTSESS_SUCCESSINDICATOR, ONHOLD " +
                    "FROM DDC_COMTASKEXEC " +
                    "WHERE ID='" + comTaskExecution.getId() + "'";
            sql.add(insertSQL);
        }

        dataModel.useConnectionRequiringTransaction(connection -> {
            try (Statement statement = connection.createStatement()) {
                sql.forEach(sqlCommand -> execute(statement, sqlCommand));
            }
        });
    }

    private void execute(Statement statement, String sql) {
        try {
            statement.execute(sql);
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }
}
