/*
 * TaskManagementRow.java
 *
 * Created on 26 september 2003, 17:41
 */

package com.energyict.mdc.engine.offline.gui.rows.task;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.core.offline.ComJobExecutionModel;
import com.energyict.mdc.engine.impl.core.offline.ComJobResult;
import com.energyict.mdc.engine.offline.gui.UiHelper;
import com.energyict.mdc.engine.offline.gui.rows.AbstractRowData;
import com.energyict.mdc.engine.offline.gui.windows.taskinfo.TaskInfoDialog;
import com.energyict.mdc.engine.offline.model.MeterReadingType;
import com.energyict.mdc.engine.offline.model.TaskState;
import com.energyict.mdc.tasks.ManualMeterReadingsTask;
import com.energyict.mdc.tasks.ProtocolTask;
import com.energyict.mdc.upl.MeterProtocol;
import com.energyict.mdc.upl.offline.OfflineLoadProfile;

import javax.swing.*;
import java.awt.*;
import java.util.Date;
import java.util.List;

/**
 * @author Koen
 */
public class TaskCommonRow extends AbstractRowData {

    private String rtuName;
    private String rtuSerialNr;
    private String location;
    private String usagePoint;
    private String deviceId;
    private String nodeAddress;
    private Date lastReading;
    private ComJobExecutionModel comJobExecutionModel;
    private ComJobResult result;

    public TaskCommonRow(ComJobExecutionModel comJobExecutionModel) {
        this.comJobExecutionModel = comJobExecutionModel;
    }

    public TaskCommonRow() {
        //Default constructor
    }

    public ComJobResult getResult() {
        result = getComJobExecutionModel().getResult();
        return result;
    }

    public String getLocation() {
        if (location == null) {
            location = comJobExecutionModel.getOfflineDevice().getLocation();
        }
        return location;
    }

    public String getUsagePoint() {
        if (usagePoint == null) {
            usagePoint = comJobExecutionModel.getOfflineDevice().getUsagePoint();
        }
        return usagePoint;
    }

    public void setResult(ComJobResult result) {
        this.result = result;
    }

    public ComJobExecutionModel getComJobExecutionModel() {
        return comJobExecutionModel;
    }

    // columns
    public TaskState getState() {
        switch (comJobExecutionModel.getState()) {
            case Done:
                return TaskState.POST_DONE;
            case Storing:
                return TaskState.POSTING;
            case Executing:
                return TaskState.EXECUTING;
            case Aborting:
                return TaskState.ABORTING;
            default:
                switch (getResult()) {
                    case Failed:
                        return TaskState.READ_FAILED;
                    case Success:
                        return TaskState.READ_SUCCESS;
                    case Pending:
                    default:
                        return TaskState.READY_TO_READ;
                }
        }
    }

    public MeterReadingType getRMR() {
        boolean remote = hasNonManualReadingTasks();
        if (remote) {
            return MeterReadingType.Remote;
        } else {
            return MeterReadingType.None;
        }
    }

    public MeterReadingType getMMR() {
        boolean manual = hasManualReadingTasks();
        if (manual) {
            return MeterReadingType.Manual;
        } else {
            return MeterReadingType.None;
        }
    }

    public String getRtuName() {
        if (rtuName == null) {
            rtuName = comJobExecutionModel.getDevice().getName();
        }
        return rtuName;
    }

    public String getRtuSerialNr() {
        if (rtuSerialNr == null) {
            rtuSerialNr = comJobExecutionModel.getDevice().getSerialNumber();
        }
        return rtuSerialNr;
    }

    public String getDeviceId() {
        if (deviceId == null) {
            deviceId = String.valueOf(comJobExecutionModel.getOfflineDevice().getAllProperties().getProperty(MeterProtocol.Property.ADDRESS.getName()));
        }
        return deviceId;
    }

    public String getNodeAddress() {
        if (nodeAddress == null) {
            nodeAddress = String.valueOf(comJobExecutionModel.getOfflineDevice().getAllProperties().getProperty(MeterProtocol.Property.NODEID.getName()));
        }
        return nodeAddress;
    }

    public Date getLastReading() {
        if (lastReading == null) {
            List<OfflineLoadProfile> offlineLoadProfiles = comJobExecutionModel.getOfflineDevice().getAllOfflineLoadProfiles();
            if (offlineLoadProfiles != null) {
                for (OfflineLoadProfile offlineLoadProfile : offlineLoadProfiles) {
                    Date aLastReading = offlineLoadProfile.getLastReading();
                    if (lastReading != null) {
                        if (aLastReading.before(lastReading)) {
                            lastReading = aLastReading;
                        }
                    } else {
                        lastReading = aLastReading;
                    }
                }
            }
        }
        return lastReading;
    }

    protected String[] getSelectedColumnProperties() {
        return new String[]{
                "rtuName",
                "rtuSerialNr",
                "location",
                "usagePoint",
                "lastReading",
                "state",
                "RMR",
                "MMR"
        };
    }

    protected String[] getSelectedColumnTranslationKeys() {
        return new String[]{
                "rtuName",
                "rtuSerialNr",
                "location",
                "usagePoint",
                "lastReading",
                "mmr.state",
                "mmr.RMR",
                "mmr.MMR"
        };
    }

    protected Object[] getSelectedColumnWidthObjects() {
        return new Object[]{
                getWidth(10),
                getWidth(10),
                getWidth(10),
                getWidth(10),
                ">120",
                getWidth(10),
                "=" + (new JLabel(UiHelper.translate("mmr.RMR")).getPreferredSize().width + 30/*Sorting icon*/),
                "=" + (new JLabel(UiHelper.translate("mmr.MMR")).getPreferredSize().width + 30/*Sorting icon*/)
        };
    }

    private String getWidth(int width) {
        String returnValue = "";
        for (int i = 0; i < width; i++) {
            returnValue += " ";
        }
        return returnValue;
    }

    public void showInfo(Frame parent) {
        new TaskInfoDialog(parent, getComJobExecutionModel()).setVisible(true);
    }

    public String toString() {
        return getRtuName() + " " + getRtuSerialNr();
    }

    public boolean hasManualReadingTasks() {
        return hasManualReadingTasks(getComJobExecutionModel());
    }

    public boolean hasNonManualReadingTasks() {
        return hasNonManualReadingTasks(getComJobExecutionModel());
    }

    public static boolean hasReadingTasks(ComJobExecutionModel model) {
        for (ComTaskExecution comTaskExecution : model.getComTaskExecutions()) {
            if (!comTaskExecution.getComTask().getProtocolTasks().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasManualReadingTasks(ComJobExecutionModel model) {
        for (ComTaskExecution comTaskExecution : model.getComTaskExecutions()) {
            for (ProtocolTask protocolTask : comTaskExecution.getComTask().getProtocolTasks()) {
                if (protocolTask instanceof ManualMeterReadingsTask) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean hasNonManualReadingTasks(ComJobExecutionModel model) {
        for (ComTaskExecution comTaskExecution : model.getComTaskExecutions()) {
            for (ProtocolTask protocolTask : comTaskExecution.getComTask().getProtocolTasks()) {
                if (!(protocolTask instanceof ManualMeterReadingsTask)) {
                    return true;
                }
            }
        }
        return false;
    }
}