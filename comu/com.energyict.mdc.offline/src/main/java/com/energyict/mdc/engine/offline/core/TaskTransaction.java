/*
 * TaskTransaction.java
 *
 * Created on 14 oktober 2003, 10:38
 */

package com.energyict.mdc.engine.offline.core;

import com.energyict.mdc.engine.impl.core.offline.ComJobResult;
import com.energyict.mdc.engine.impl.core.offline.ComJobState;
import com.energyict.mdc.engine.offline.persist.FilePersistData;

import java.util.Date;

/**
 * @author Koen
 */
public class TaskTransaction extends FilePersistData {

    long deviceId;
    String attributeDeviceId;
    ComJobResult taskResult;
    Date date;
    ComJobState taskState;
    String rtuName;

    /**
     * Creates a new instance of TaskTransaction
     */
    public TaskTransaction() {
    }

    public TaskTransaction(long deviceId, String attDeviceId, ComJobResult taskResult, ComJobState taskState, Date date, String rtuName) {
        this.deviceId = deviceId;
        this.attributeDeviceId = attDeviceId;
        this.taskResult = taskResult;
        this.taskState = taskState;
        this.date = date;
        this.rtuName = rtuName;
    }

    /**
     * Getter for property deviceId.
     *
     * @return Value of property taskId.
     */
    public long getDeviceId() {
        return deviceId;
    }

    /**
     * Setter for property deviceId.
     *
     * @param deviceId New value of property deviceId.
     */
    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * Getter for property taskResult.
     *
     * @return Value of property taskResult.
     */
    public ComJobResult getTaskResult() {
        return taskResult;
    }

    /**
     * Setter for property taskResult.
     *
     * @param taskResult New value of property taskResult.
     */
    public void setTaskResult(ComJobResult taskResult) {
        this.taskResult = taskResult;
    }

    /**
     * Getter for property date.
     *
     * @return Value of property date.
     */
    public Date getDate() {
        return date;
    }

    /**
     * Setter for property date.
     *
     * @param date New value of property date.
     */
    public void setDate(Date date) {
        this.date = date;
    }

    public String getDirectory() {
        return RegistryConfiguration.getDefault().get("datafilesdirectory");
    }

    /**
     * Getter for property taskState.
     *
     * @return Value of property taskState.
     */
    public ComJobState getTaskState() {
        return taskState;
    }

    /**
     * Setter for property taskState.
     *
     * @param taskState New value of property taskState.
     */
    public void setTaskState(ComJobState taskState) {
        this.taskState = taskState;
    }

    /**
     * Getter for property rtuName.
     *
     * @return Value of property rtuName.
     */
    public String getRtuName() {
        return rtuName;
    }

    /**
     * Setter for property rtuName.
     *
     * @param rtuName New value of property rtuName.
     */
    public void setRtuName(String rtuName) {
        this.rtuName = rtuName;
    }

    /**
     * Getter for property attributeDeviceId.
     *
     * @return Value of property attributeDeviceId.
     */
    public String getAttributeDeviceId() {
        return attributeDeviceId;
    }

    /**
     * Setter for property attributeDeviceId.
     *
     * @param attributeDeviceId New value of property attributeDeviceId.
     */
    public void setAttributeDeviceId(String attributeDeviceId) {
        this.attributeDeviceId = attributeDeviceId;
    }
}