/*
 * TaskManager.java
 *
 * Created on 1 oktober 2003, 10:47
 */

package com.energyict.mdc.engine.offline.core;


import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.common.ComServerExecutionException;
import com.energyict.mdc.common.comserver.ComPort;
import com.energyict.mdc.common.comserver.OutboundComPort;
import com.energyict.mdc.engine.exceptions.DataAccessException;
import com.energyict.mdc.engine.impl.core.ComJob;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.offline.ComJobExecutionModel;
import com.energyict.mdc.engine.impl.core.offline.ComJobState;
import com.energyict.mdc.engine.impl.core.remote.RemoteComServerDAOImpl;
import com.energyict.mdc.engine.offline.gui.OfflineFrame;
import com.energyict.mdc.engine.offline.gui.UiHelper;
import com.energyict.mdc.engine.offline.gui.rows.task.TaskCommonRow;
import com.energyict.mdc.engine.offline.gui.rows.task.TaskExecutionRow;
import com.energyict.mdc.engine.offline.gui.rows.task.TaskManagementRow;
import com.energyict.mdc.upl.MeterProtocol;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Koen, Geert (2014)
 *         Changes 14052004 avoid deleting of files in case of an exception
 */
public class TaskManager {

    /**
     * A list of comjob models. This contains the comjob and all the relevant information for its offline execution.
     */
    private List<ComJobExecutionModel> comJobModels = null;

    private List<TaskManagementRow> taskManagementRows;
    private List<TaskExecutionRow> taskExecutionRows;
    private OfflineWorker guiWorker;
    private boolean online;
    private boolean reading = false;

    /**
     * Creates a new instance of TaskManager
     */
    public TaskManager(OfflineWorker guiWorker) {
        this.guiWorker = guiWorker;
        taskManagementRows = new ArrayList<>();
        taskExecutionRows = new ArrayList<>();
        online = false;
    }

    /*
    *   Get the task list from file or initiate a database query. Depending on the
    *   online status of the application, the task_x.txt files will be written to disk.
    *   @param date If online, query will search for all comJobs with nextcommunication <= date
    *
    *   Show a progress bar that shows the actual progress while reading out the relevant information for the pending comjobs.
    */
    public void queryPendingComJobs(Date date) {
        try {
            UiHelper.getMainWindow().setWaitingCursor(true);

            if (isOnline()) {   //Get new list of pending comjobs. Note that 'busy' comjobs were reset to 'pending' because of the coldboot.
                setComJobModels(new ArrayList<ComJobExecutionModel>());

                ComPort comPort = guiWorker.getOfflineExecuter().getComPort();
                if (comPort != null) {
                    ComServerDAO remoteComServerDAO = guiWorker.getOfflineExecuter().getRemoteComServerDAO();

                    try {
                        List<ComJob> comJobs = remoteComServerDAO.findPendingOutboundComTasks((OutboundComPort) comPort);
                        //Now that the number of comjobs is known, set the size of the progress bar (no longer indeterminate)
                        UiHelper.getMainWindow().setProgressBarSize(comJobs.size());

                        //Now fetch all relevant information for every comjob
                        for (ComJob comJob : comJobs) {
                            UiHelper.getMainWindow().increaseProgress();

                            //Create a model for this comjob and add it to the list so it can be executed
                            ComJobExecutionModel model = new ComJobExecutionModel(comJob, guiWorker.getOfflineExecuter().getComPort().getId(), guiWorker.getOfflineExecuter().getOfflineComServer().getCommunicationLogLevel());
                            model.initializeModel((RemoteComServerDAOImpl) remoteComServerDAO);
                            getComJobModels().add(model);
                        }
                    } catch (DataAccessException e) {
                        //Stop querying, reset the jobs.
                        //Note that the tasks that were already locked in EIServer will be reset by the next cold boot.
                        setComJobModels(null);
                        guiWorker.getOfflineFrame().handleConnectionProblem(e);
                    }

                    Helpers.deleteAllFiles(RegistryConfiguration.getDefault().get("datafilesdirectory"));
                    guiWorker.getFileManager().saveComJobModels(getComJobModels());

                } else {
                    throw new ApplicationException("Cannot request the pending ComJobs, no ComPort is defined.");
                }
            } else {
                try {
                    // Read in the existing ComJobModels from the files
                    setComJobModels(guiWorker.getFileManager().loadComJobExecutionModels());
                } catch (DataAccessException e) {
                    //Stop querying, reset the jobs.
                    setComJobModels(null);
                    guiWorker.getOfflineFrame().handleRuntimeProblem(new ComServerExecutionException("Can't read ComTasks from file:\n" + e.getMessage(), e));
                }
            }
        } finally {
            OfflineFrame mainWindow = UiHelper.getMainWindow();
            mainWindow.setWaitingCursor(false);
            mainWindow.updateTasks(null);
            mainWindow.invokeUpdateConfigPanel();
            //Disable the progress bar. The UI thread will notice this and continue (because it was intentionally blocked by the progress bar)
            mainWindow.stopProgressBar();
        }
    }

    public OfflineWorker getGuiWorker() {
        return guiWorker;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public List<TaskManagementRow> getTaskManagementRows() {
        return taskManagementRows;
    }

    public List<TaskExecutionRow> getTaskExecutionRows() {
        return taskExecutionRows;
    }

    public boolean isTaskExecutable(ComJobExecutionModel model) {
        return (model.isActive() &&
                !model.getCompleted() &&
                (TaskCommonRow.hasReadingTasks(model))
        );
    }

    public boolean needToStoreCollectedData() {
        for (ComJobExecutionModel comJob : getComJobModels()) {
            if ((comJob.getState() == ComJobState.AwaitingStore) && comJob.getCompleted() && comJob.isActive()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasCollectedData() {
        for (ComJobExecutionModel comJob : getComJobModels()) {
            if (comJob.getState() == ComJobState.AwaitingStore) {
                return true;
            }
        }
        return false;
    }

    public List<ComJobExecutionModel> getComJobModels() {
        if (comJobModels == null) {
            comJobModels = new ArrayList<>();
        }
        return comJobModels;
    }

    public void setComJobModels(List<ComJobExecutionModel> comJobModels) {
        this.comJobModels = comJobModels;
    }

    /*
    *  Save task to disk in task_n.txt file
    */
    public void saveTask(ComJobExecutionModel model) {
        guiWorker.getFileManager().saveComJobExecutionModel(model);
    }

    /*
    *  Build the TaskManagementRow list from the comJobs
    */
    public List<TaskManagementRow> rebuildTableRows() {
        getTaskManagementRows().clear();
        for (ComJobExecutionModel comJobModel : getComJobModels()) {
            getTaskManagementRows().add(new TaskManagementRow(this, comJobModel));
        }
        return getTaskManagementRows();
    }

    /*
    * Initiate a build of the TaskExecutionRow list from the comJobs list.
    */

    public void initiateExecutionRowsBuild() {
        buildExecutionTableRows(getComJobModels());
    }

    /*
    *  Build the TaskExecutionRow list from the comJobs models
    */
    private void buildExecutionTableRows(List<ComJobExecutionModel> models) {
        getTaskExecutionRows().clear();
        for (ComJobExecutionModel model : models) {
            if (isTaskExecutable(model) && UiHelper.getMainWindow().passesThruFilter(model)) {
                getTaskExecutionRows().add(new TaskExecutionRow(this, model));
            }
        }
    }

    protected void addTransaction(ComJobExecutionModel model) {
        String attributeDeviceId = String.valueOf(model.getOfflineDevice().getAllProperties().getProperty(MeterProtocol.Property.ADDRESS.getName(), ""));
        TaskTransaction taskTransaction = new TaskTransaction(
                model.getDevice().getId(),
                attributeDeviceId,
                model.getResult(),
                model.getState(),
                Date.from(Instant.now()),
                model.getOfflineDevice().getSerialNumber()
        );
        taskTransaction.add();
    }

    /*
    * This method will update the status of the TaskExecutionRow controls depending
    * on the given task change. This method also saves the task to disk in an task_n.txt file.
    * @param task the task that notifies a change
    */
    public void updateTaskRows(ComJobExecutionModel model) {
        addTransaction(model);
        for (TaskManagementRow tmr : getTaskManagementRows()) {
            if (tmr.getComJobExecutionModel().equals(model)) {
                tmr.updateTask(true);
            }
        }
        for (TaskExecutionRow ter : getTaskExecutionRows()) {
            if (ter.getComJobExecutionModel().equals(model)) {
                ter.updateTask();
            }
        }
    }

   public void addNewTransactionToModel(ComJobExecutionModel model) {
       addTransaction(model);
   }

   /**
    * Getter for property reading.
    *
    * @return Value of property reading.
    */
    public boolean isReading() {
        return reading;
    }

    /**
     * Setter for property reading.
     *
     * @param reading New value of property reading.
     */
    public void setReading(boolean reading) {
        this.reading = reading;
    }
}