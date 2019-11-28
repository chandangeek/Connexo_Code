/*
 * TaskManagementRow.java
 *
 * Created on 29 september 2003, 10:52
 */

package com.energyict.mdc.engine.offline.gui.rows.task;

import com.energyict.mdc.engine.impl.core.offline.ComJobExecutionModel;
import com.energyict.mdc.engine.impl.core.offline.ComJobResult;
import com.energyict.mdc.engine.impl.core.offline.ComJobState;
import com.energyict.mdc.engine.offline.core.TaskManager;
import com.energyict.mdc.engine.offline.gui.UiHelper;

import javax.swing.*;

/**
 * @author Koen, Geert (2014)
 */
public class TaskManagementRow extends TaskCommonRow {

    TaskManager taskManager;

    public TaskManagementRow() {
        //Default constructor, used in TableCreator using reflection
    }

    /**
     * Creates a new instance of TaskManagementRow
     */
    public TaskManagementRow(TaskManager taskManager, ComJobExecutionModel comJobExecutionModel) {
        super(comJobExecutionModel);
        this.taskManager = taskManager;
    }


    /**
     * Persist the task changes to the task's file and update the table
     */
    synchronized public void updateTask(boolean saveTask) {
        if (saveTask) {
            taskManager.saveTask(getComJobExecutionModel());
        }
        updateTable();
    }

    synchronized protected void updateTable() {
        // table update...
        SwingUtilities.invokeLater(
                new Runnable() {
                    @Override
                    public void run() {
                        taskManager.getGuiWorker().getTaskManagementTableCreator().fireTableDataChanged();
                    }
                }
        );
    }

    public void doResetTask() {
        if (getComJobExecutionModel().getResult() != ComJobResult.Pending) {
            int retval = JOptionPane.showConfirmDialog(null,
                UiHelper.translate("resettask"),
                UiHelper.translate("warning"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

            if (retval == JOptionPane.YES_OPTION) {
                ComJobExecutionModel model = getComJobExecutionModel();
                model.setCompletionCode(null);
                model.setReasonCode(null);
                model.setCompleted(false);
                model.setSkipped(false);
                if (model.getState() == ComJobState.Done || model.getState() == ComJobState.Storing) {
                    model.setState(ComJobState.AwaitingStore);
                } else {
                    //Delete any previous history
                    model.resetCollectedData();
                    model.setState(ComJobState.Pending);
                    model.forceResult(ComJobResult.Pending);
                }
                setResult(model.getResult());
                UiHelper.getMainWindow().updateTasks(model);
                UiHelper.getMainWindow().invokeUpdateConfigPanel();
                updateTask(true);
            }
        }
    }
}