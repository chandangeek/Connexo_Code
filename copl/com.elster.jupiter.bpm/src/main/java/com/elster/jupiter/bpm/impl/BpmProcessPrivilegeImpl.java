/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm.impl;

import com.elster.jupiter.bpm.BpmProcessDefinition;
import com.elster.jupiter.bpm.BpmProcessPrivilege;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Arrays;
import java.util.stream.Stream;


public class BpmProcessPrivilegeImpl implements BpmProcessPrivilege {

    private final DataModel dataModel;
    private long processId;
    private String privilegeName;
    private String application;
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

    static BpmProcessPrivilegeImpl from(DataModel dataModel, String privilegeName, String application) {
        return dataModel.getInstance(BpmProcessPrivilegeImpl.class).init(privilegeName, application);
    }

    private BpmProcessPrivilegeImpl init(String privilegeName, String application) {
        this.privilegeName = privilegeName;
        this.application = application;
        return this;
    }

    private BpmProcessPrivilegeImpl init(BpmProcessDefinition processDefinition, String privilegeName, String application){
        this.processId = processDefinition.getId();
        this.bpmProcessDefinition = processDefinition;
        init(privilegeName, application);
        return this;
    }


    @Override
    public String getPrivilegeName() {
        return privilegeName;
    }

    @Override
    public void setPrivilegeName(String privilegeName) {
        this.privilegeName = privilegeName;
    }

    @Override
    public String getApplication() {
        return application;
    }

    @Override
    public void setApplication(String application) {
        this.application = application;
    }

    @Override
    public long getProcessId() {
        return processId;
    }

    @Override
    public void setProcessId(long processId) {
        this.processId = processId;
    }

    @Override
    public void persist() {
        if (this.bpmProcessDefinition == null && processId > 0) {
            this.bpmProcessDefinition = dataModel.mapper(BpmProcessDefinition.class).find("id", processId).get(0);
        }
        dataModel.mapper(BpmProcessPrivilege.class).persist(this);
    }

    @Override
    public void delete() {
        dataModel.mapper(BpmProcessPrivilege.class).remove(this);
    }

    @Override
    public int hashCode() {
        int result = (int) (processId ^ (processId >>> 32));
        result = 31 * result + privilegeName.hashCode();
        result = 31 * result + application.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BpmProcessPrivilegeImpl that = (BpmProcessPrivilegeImpl) o;

        if (processId != that.processId || !privilegeName.equals(that.privilegeName)) {
            return false;
        }
        return application.equals(that.application);
    }
}
