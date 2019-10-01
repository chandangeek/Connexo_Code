package com.energyict.mdc.engine.offline.core;

import com.elster.jupiter.metering.readings.MeterReading;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.OutboundConnectionTask;
import com.energyict.mdc.engine.exceptions.DataAccessException;
import com.energyict.mdc.engine.impl.core.ComJob;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.RunningComServerImpl;
import com.energyict.mdc.engine.impl.core.offline.ComJobExecutionModel;
import com.energyict.mdc.engine.impl.core.offline.ComJobState;
import com.energyict.mdc.engine.impl.core.offline.DeviceMessageInformationWrapper;
import com.energyict.mdc.engine.impl.core.remote.ComSessionBuilderXmlWrapper;
import com.energyict.mdc.engine.impl.core.remote.DeviceProtocolCacheXmlWrapper;
import com.energyict.mdc.engine.offline.OfflineExecuter;
import com.energyict.mdc.engine.offline.gui.UiHelper;
import com.energyict.mdc.identifiers.DeviceIdentifierById;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLogBook;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LogBookIdentifier;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 29/09/2014 - 10:03
 */
public class StoringThread extends Thread {

    private OfflineExecuter offlineExecuter;
    private RunningComServerImpl.ServiceProvider serviceProvider;
    private List<ComJobExecutionModel> models;
    private AtomicBoolean storing = new AtomicBoolean(true);

    public StoringThread(RunningComServerImpl.ServiceProvider serviceProvider, OfflineExecuter offlineExecuter) {
        this.offlineExecuter = offlineExecuter;
        this.serviceProvider = serviceProvider;
    }

    private List<ComJobExecutionModel> getModels() {
        if (models == null) {
            models = new ArrayList<>();
        }
        return models;
    }

    public void setModels(List<ComJobExecutionModel> models) {
        this.models = models;
    }

    public void setStoring(boolean storing) {
        this.storing.set(storing);
    }

    @Override
    public void run() {
        serviceProvider.threadPrincipalService().runAs(offlineExecuter.getComServerUser(), () -> doRun());
    }

    public void doRun() {
        boolean cleanStop = true;
        try {
            //First set the actual value of the progress bar
            UiHelper.getMainWindow().setProgressBarSize(getNumberOfTasksToStore());
            for (ComJobExecutionModel model : getModels()) {
                if (storing.get()) {
                    if (isAwaitingStore(model)) {
                        //Show the increasing progress in the progress bar
                        UiHelper.getMainWindow().increaseProgress();

                        model.setState(ComJobState.Storing);
                        offlineExecuter.updateGUI(model);

                        try {
                            storeStartTime(model);
                            storeCollectedData(model);
                            executionFinished(model);
                            storeComSessionShadow(model);
                            unlockComJob(model);

                            model.setState(ComJobState.Done);
                            offlineExecuter.updateGUI(model);
                        } catch (DataAccessException e) {
                            resetModel(model);
                            cleanStop = false;
                            UiHelper.getMainWindow().handleConnectionProblem(e);
                            break;  //Error while storing data, break the loop
                        } catch (Throwable e) {
                            resetModel(model);
                            cleanStop = false;
                            throw e;
                        }
                    }
                } else {
                    cleanStop = true;
                    break;  //User has canceled, break the loop
                }
            }
        } finally {
            offlineExecuter.setStoringEndedClean(cleanStop); // False in case of exception
            offlineExecuter.setBusyStoringCollectedData(false);
            offlineExecuter.endStoringProgressInGUI(); // Closes the blocking progress bar, show store button again
        }
    }

    private void resetModel(ComJobExecutionModel model) {
        model.setState(ComJobState.AwaitingStore);
        offlineExecuter.updateGUI(model);
    }

    /**
     * First store the start time of the connection task and comTaskExecutions, if they were executed.
     */
    private void storeStartTime(ComJobExecutionModel model) {
        Date connectionStartTime = model.getConnectionStartTime();
        if (connectionStartTime != null) {
            getRemoteComServerDAO().executionStarted(model.getConnectionTask(), offlineExecuter.getComPort()); //TODO: check with Govanni
        }

        for (Object comTaskExecutionId : model.getComTaskExecutionStartTimes().keySet()) {
            ComTaskExecution comTaskExecution = null;
            for (ComTaskExecution aComTaskExecution : model.getComTaskExecutions()) {
                if (aComTaskExecution.getId() == Long.valueOf(comTaskExecutionId.toString())) {
                    comTaskExecution = aComTaskExecution;
                    break;
                }
            }
            if (comTaskExecution != null) {
                getRemoteComServerDAO().executionStarted(comTaskExecution, offlineExecuter.getComPort(), false);
            }
        }
    }

    private int getNumberOfTasksToStore() {
        int nrOfTasksToStore = 0;
        for (ComJobExecutionModel model : getModels()) {
            if (isAwaitingStore(model)) {
                nrOfTasksToStore++;
            }
        }
        return nrOfTasksToStore;
    }

    private boolean isAwaitingStore(ComJobExecutionModel model) {
        return model.getState() == ComJobState.AwaitingStore && model.getCompleted();
    }

    /**
     * Complete the connection task and comtask executions according to the result
     */
    private void executionFinished(ComJobExecutionModel model) {
        if (model.isConnectionTaskSuccess()) {
            getRemoteComServerDAO().executionCompleted(model.getConnectionTask());
        } else {
            getRemoteComServerDAO().executionFailed(model.getConnectionTask());
        }
        for (ComTaskExecution comTaskExecution : model.getSuccessFullComTaskExecutions()) {
            getRemoteComServerDAO().executionCompleted(comTaskExecution);
        }
        for (ComTaskExecution comTaskExecution : model.getFailedComTaskExecutions()) {
            getRemoteComServerDAO().executionFailed(comTaskExecution);
        }
    }

    private void unlockComJob(ComJobExecutionModel model) {
        ComJob comJob = model.getComJob();
        ConnectionTask connectionTask = comJob.getConnectionTask();
        if (connectionTask instanceof OutboundConnectionTask) {
            getRemoteComServerDAO().unlock((OutboundConnectionTask)connectionTask);
        }
    }

    private void storeComSessionShadow(ComJobExecutionModel model) {
        ComSessionBuilderXmlWrapper comSessionBuilder = model.getComSessionBuilder();
        getRemoteComServerDAO().createComSession(comSessionBuilder.getBuilder(), comSessionBuilder.getStopDate(), comSessionBuilder.getSuccessIndicator());
    }

    private boolean isUnexecutedMMRTask(ComJobExecutionModel model, ComTaskExecution comTaskExecution) {
        return comTaskExecution != null
                && model.isMMROnly(comTaskExecution)
                && !model.getSuccessFullComTaskExecutions().contains(comTaskExecution)
                && !model.getFailedComTaskExecutions().contains(comTaskExecution);
    }

    private void storeCollectedData(ComJobExecutionModel model) {
        if (model.getDeviceCache() != null) {
            DeviceProtocolCacheXmlWrapper cache = model.getDeviceCache();
            DeviceIdentifier deviceIdentifier = new DeviceIdentifierById(model.getDevice().getId());
            getRemoteComServerDAO().createOrUpdateDeviceCache(deviceIdentifier, cache);
        }

        for (Map.Entry<DeviceIdentifier, DeviceIdentifier> entry : model.getDeviceGatewayMap().entrySet()) {
            getRemoteComServerDAO().updateGateway(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<DeviceIdentifier, MeterReading> entry : model.getMeterReadingMap().entrySet()) {
            getRemoteComServerDAO().storeMeterReadings(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<DeviceIdentifier, MeterReading> entry : model.getManualMeterReadingsMap().entrySet()) {
            getRemoteComServerDAO().storeMeterReadings(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<LoadProfileIdentifier, CollectedLoadProfile> entry : model.getCollectedLoadProfileMap().entrySet()) {
            getRemoteComServerDAO().storeLoadProfile(entry.getKey(), entry.getValue(),  new Date(model.getLoadProfileReadDateMap().get(entry.getKey())).toInstant());
        }

        for (Map.Entry<LogBookIdentifier, CollectedLogBook> entry : model.getCollectedLogBookMap().entrySet()) {
            getRemoteComServerDAO().storeLogBookData(entry.getKey(), entry.getValue(), new Date(model.getLogBookReadDateMap().get(entry.getKey())).toInstant());
        }

        for (Map.Entry<LogBookIdentifier, Long> entry : model.getLogBookLastReadingsMap().entrySet()) {
            getRemoteComServerDAO().updateLogBookLastReading(entry.getKey(), new Date(entry.getValue()));
        }

        for (DeviceMessageInformationWrapper messageInformation : model.getCollectedDeviceMessageInformationList()) {
            getRemoteComServerDAO().updateDeviceMessageInformation(messageInformation.getMessageIdentifier(), messageInformation.getNewDeviceMessageStatus(),  messageInformation.getSentDate(), messageInformation.getProtocolInformation());
        }

        // New execution model data
        for(int i = 0; i < model.getLoadProfileUpdates().size() &&  i < model.getLogBookUpdates().size() ; i++) {
            getRemoteComServerDAO().updateLastDataSourceReadingsFor(model.getLoadProfileUpdates().get(i), model.getLogBookUpdates().get(i));
        }
    }

    private ComServerDAO getRemoteComServerDAO() {
        return offlineExecuter.getRemoteComServerDAO();
    }
}