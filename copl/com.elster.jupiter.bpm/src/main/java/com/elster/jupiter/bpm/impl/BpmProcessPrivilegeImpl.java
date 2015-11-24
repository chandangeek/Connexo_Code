package com.elster.jupiter.bpm.impl;

import com.elster.jupiter.bpm.BpmProcessDefinition;
import com.elster.jupiter.bpm.BpmProcessPrivilege;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;
import java.time.Instant;


public class BpmProcessPrivilegeImpl implements BpmProcessPrivilege {

    private long processId;
    private String privilegeName;
    private String application;
    private final DataModel dataModel;
    private BpmProcessDefinition bpmProcessDefinition;

    @SuppressWarnings("unused")
    private Instant createTime;

    @Inject
    public BpmProcessPrivilegeImpl(DataModel dataModel){
        this.dataModel = dataModel;
    }

    static BpmProcessPrivilegeImpl from(DataModel dataModel, BpmProcessDefinition processDefinition, String privilegeName, String application){
        return dataModel.getInstance(BpmProcessPrivilegeImpl.class).init(processDefinition, privilegeName, application);
    }

    private BpmProcessPrivilegeImpl init(BpmProcessDefinition processDefinition, String privilegeName, String application){
        this.processId = processDefinition.getId();
        this.bpmProcessDefinition = processDefinition;
        this.privilegeName = privilegeName;
        this.application = application;
        return this;
    }


    @Override
    public String getPrivilegeName() {
        return privilegeName;
    }

    @Override
    public String getApplication() {
        return application;
    }

    @Override
    public long getProcessId() {
        return processId;
    }

    @Override
    public void setPrivilegeName(String privilegeName) {
        this.privilegeName = privilegeName;
    }

    @Override
    public void setApplication(String application) {
        this.application = application;
    }

    @Override
    public void persist() {
        dataModel.mapper(BpmProcessPrivilege.class).persist(this);
    }

    @Override
    public void delete() {
        dataModel.mapper(BpmProcessPrivilege.class).remove(this);
    }
}
