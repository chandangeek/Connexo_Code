/*
 * TaskExecutionRow.java
 *
 * Created on 29 september 2003, 11:11
 */

package com.energyict.mdc.engine.offline.gui.rows.task;

import com.elster.jupiter.metering.readings.MeterReading;
import com.elster.jupiter.metering.readings.Reading;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.DeviceIdentifierById;
import com.energyict.mdc.engine.impl.core.offline.ComJobExecutionModel;
import com.energyict.mdc.engine.impl.core.offline.ComJobResult;
import com.energyict.mdc.engine.impl.core.offline.ComJobState;
import com.energyict.mdc.engine.offline.core.TaskManager;
import com.energyict.mdc.engine.offline.gui.UiHelper;
import com.energyict.mdc.engine.offline.gui.windows.ManualMeterReadingsDialog;
import com.energyict.mdc.engine.offline.model.CustomCompletionCode;
import com.energyict.mdc.engine.offline.model.ValueForObis;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.tasks.ManualMeterReadingsTask;
import com.energyict.mdc.tasks.ProtocolTask;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.obis.ObisCode;

import javax.swing.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Koen, khe, Geert (2014)
 */
public class TaskExecutionRow extends TaskCommonRow {

    private TaskManager taskManager;

    public TaskExecutionRow() {
        //Default constructor, used in TableCreator using reflection
    }

    /**
     * Creates a new instance of TaskExecutionRow
     */
    public TaskExecutionRow(TaskManager taskManager, ComJobExecutionModel model) {
        super(model);
        this.taskManager = taskManager;
    }

    synchronized public void updateTask() {
        prepareRead();
        taskManager.saveTask(getComJobExecutionModel());
        updateTable();
    }

    synchronized private void prepareRead() {
        taskManager.setReading(false);
    }

    /*
    *  method that updates the whole table and switch to the read action state
    */
    synchronized protected void updateTable() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                taskManager.getGuiWorker().getTaskExecutionTableCreatorAllTasks().fireTableDataChanged();
            }
        });
    }

    private boolean isNotSkipped() {
        return !getComJobExecutionModel().isSkipped();
    }

    public boolean canBeReadManual() {
        return isNotSkipped() && (hasManualReadingTasks());
    }

    public boolean canBeReadRemote() {
        return isNotSkipped() && hasNonManualReadingTasks();
    }

    public void skip(CustomCompletionCode customCompletionCode) {
        ComJobExecutionModel model = getComJobExecutionModel();
        model.resetCollectedData();     //Also resets the comtasks to 'not executed'
        model.setSkipped(true);
        model.setCompletionCode(customCompletionCode.getCompletionCode());
        model.setReasonCode(customCompletionCode.getReasonCode());
        model.setCompleted(true);
        model.setResult(ComJobResult.Failed);
        model.setState(ComJobState.AwaitingStore);
        UiHelper.getMainWindow().updateTasks(model);
    }

    public void triggerManualRead() {
        List<ValueForObis> mmrValues = getMMRInput();
        if (!mmrValues.isEmpty()) { // else: user canceled
            Map<DeviceIdentifier, MeterReading> manualMeterReadingsMap = new HashMap<>();
            for (ValueForObis valueForObis : mmrValues) {
                if (valueForObis.getValue() == null) {
                    continue;
                }
                OfflineRegister offlineRegister = getOfflineRegister(valueForObis.getObisCode());
                DeviceIdentifierById deviceIdentifierById = DeviceIdentifierById.from(offlineRegister.getDeviceId());
                if (!manualMeterReadingsMap.containsKey(deviceIdentifierById)) {
                    MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
                    manualMeterReadingsMap.put(deviceIdentifierById, meterReading);
                }
                Reading reading = ReadingImpl.of(offlineRegister.getReadingTypeMRID(), valueForObis.getValue(), valueForObis.getToDate().toInstant());
                manualMeterReadingsMap.get(deviceIdentifierById).getReadings().add(reading);
            }

            //Update the state of the comtasks that consist of MMR tasks only
            for (ComTaskExecution comTaskExecution : getComJobExecutionModel().getComTaskExecutions()) {
                if (isMMROnly(comTaskExecution)) {
                    getComJobExecutionModel().addSuccessfulComTaskExecution(comTaskExecution, true);
                }
            }

            //Add MMR values to the model
            getComJobExecutionModel().setManualMeterReadingsMap(manualMeterReadingsMap);

            getComJobExecutionModel().forceResult(ComJobResult.Success);
            getComJobExecutionModel().setState(ComJobState.AwaitingStore);


            UiHelper.getMainWindow().updateTasks(getComJobExecutionModel());
        }
    }

    private boolean isMMROnly(ComTaskExecution comTaskExecution) {
        for (ProtocolTask protocolTask : comTaskExecution.getComTask().getProtocolTasks()) {
            if (!(protocolTask instanceof ManualMeterReadingsTask)) {
                return false;
            }
        }
        return true;
    }

    private OfflineRegister getOfflineRegister(ObisCode obisCode) {
        List<OfflineRegister> allOfflineRegisters = getComJobExecutionModel().getOfflineDevice().getAllOfflineRegisters();
        for (OfflineRegister offlineRegister : allOfflineRegisters) {
            if (offlineRegister.getObisCode().equals(obisCode)) {
                return offlineRegister;
            }
        }
        return null;
    }

    public void triggerRemoteRead() {
        // Add the comjob to the running ComServer, execution is done by the working thread
        taskManager.getGuiWorker().getOfflineExecuter().executeComJob(getComJobExecutionModel());
        updateTable();
    }

    public void abortRemoteRead() {
        // Set AtomicBoolean to true, indicating all communication must be aborted
        ComChannel.abortCommunication.set(true);

        // Notify RunningComServer to restart all ComPorts, this will set the interrupt flag of each ComPort schedule Thread
        taskManager.getGuiWorker().getOfflineExecuter().getRunningComServer().restartAllComPorts();

        // Add an additional entry to the 'Transaction logging'
        taskManager.addNewTransactionToModel(getComJobExecutionModel());

        updateTable();
    }

    public void complete(CustomCompletionCode customCompletionCode) {
        if (customCompletionCode != null) {
            ComJobExecutionModel model = getComJobExecutionModel();
            model.setCompletionCode(customCompletionCode.getCompletionCode());
            model.setReasonCode(customCompletionCode.getReasonCode());
            model.setCompleted(true);
            taskManager.saveTask(model);
            UiHelper.getMainWindow().updateTasks(null);
        }
    }

    private List<ValueForObis> getMMRInput() {
        ManualMeterReadingsDialog mmrDialog = new ManualMeterReadingsDialog(UiHelper.getMainWindow(), true, getComJobExecutionModel());
        mmrDialog.setVisible(true);
        return mmrDialog.getValues();
    }
}