package com.elster.resteasy.test;

import org.kie.api.task.model.Status;
import org.kie.api.task.model.Task;



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
}
