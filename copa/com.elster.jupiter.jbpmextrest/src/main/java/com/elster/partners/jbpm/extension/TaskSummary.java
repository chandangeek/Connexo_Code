/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.partners.jbpm.extension;

import org.jbpm.services.task.impl.model.TaskImpl;
import org.kie.api.task.model.Group;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

public class TaskSummary {

    private long id;
    private String name;
    private String processName;
    private String deploymentId;
    private Date dueDate;
    private Date createdOn;
    private int priority;
    private Status status;
    private String actualOwner;
    private long processInstanceId;
    private long optLock = -1;
    private String workGroup;

    public TaskSummary(){}

    public TaskSummary(long id, String name, String processName, String deploymentId, Date dueDate, Date createdOn, int priority, Status status, String actualOwner, long processInstanceId) {
        this.id = id;
        this.name = name;
        this.processName = processName;
        this.deploymentId = deploymentId;
        this.dueDate = dueDate;
        this.createdOn = createdOn;
        this.priority = priority;
        this.status = status;
        this.actualOwner = actualOwner;
        this.processInstanceId = processInstanceId;
    }

    public TaskSummary(Task task){
        this.id = task.getId();
        task.getTaskData().getProcessId();
        this.name = task.getName();
        this.processName = task.getTaskData().getProcessId();
        this.deploymentId = task.getTaskData().getDeploymentId();
        this.dueDate = task.getTaskData().getExpirationTime();
        this.createdOn = task.getTaskData().getCreatedOn();
        this.priority = task.getPriority();
        this.status = task.getTaskData().getStatus();
        if(task.getTaskData().getActualOwner() != null) {
            this.actualOwner = task.getTaskData().getActualOwner().getId();
        }else{
            this.actualOwner = "";
        }
        this.processInstanceId = task.getTaskData().getProcessInstanceId();
        this.optLock = ((TaskImpl) task).getVersion();
        task.getPeopleAssignments().getPotentialOwners().stream().filter(group -> group instanceof Group).map(OrganizationalEntity::getId).findFirst().ifPresent(groupName -> workGroup = groupName);
    }

    public TaskSummary(Object[] obj){
        if(obj != null) {
            this.id = obj[0] == null ? -1 : ((BigDecimal) obj[0]).longValue();
            this.actualOwner = obj[1] == null ? "" : (String) obj[1];
            this.processName = obj[2] == null ? "" : (String) obj[2];
            this.createdOn = obj[3] == null ? null : (Timestamp) obj[3];
            String sts = obj[4] == null ? "" : (String) obj[4];
            if (!sts.equals("")) {
                for (int i = 0; i < Status.values().length; i++) {
                    if (Status.values()[i].toString().equals(sts)) {
                        this.status = Status.values()[i];
                    }
                }
            }
            this.name = obj[5] == null ? "" : (String) obj[5];
        }

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getActualOwner() {
        return actualOwner;
    }

    public void setActualOwner(String actualOwner) {
        this.actualOwner = actualOwner;
    }

    public long getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(long processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public long getOptLock() {
        return optLock;
    }

    public void setOptLock(int optLock) {
        this.optLock = optLock;
    }

    public String getWorkGroup() {
        return workGroup;
    }

    public void setWorkGroup(String workGroup) {
        this.workGroup = workGroup;
    }
}
