package com.elster.jupiter.bpm.impl;

import com.elster.jupiter.bpm.*;
import com.elster.jupiter.orm.DataModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BpmProcessDefinitionBuilderImpl implements BpmProcessDefinitionBuilder {


    private String id;
    private final DataModel dataModel;
    private String processName;
    private String association;
    private String version;
    private String status;
    private List<BpmProcessPrivilege> processPrivileges = new ArrayList<>();
    private List<Map<String,String>> associatedData = new ArrayList<>();
    private final BpmService bpmService;


    public BpmProcessDefinitionBuilderImpl(DataModel dataModel, BpmService bpmService) {
        this.dataModel = dataModel;
        this.bpmService = bpmService;
    }


    @Override
    public BpmProcessDefinitionBuilder setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public BpmProcessDefinitionBuilder setProcessName(String processName) {
        this.processName = processName;
        return this;
    }

    @Override
    public BpmProcessDefinitionBuilder setAssociation(String association) {
        this.association = association;
        return this;
    }

    @Override
    public BpmProcessDefinitionBuilder setVersion(String version) {
        this.version = version;
        return this;
    }

    @Override
    public BpmProcessDefinitionBuilder setStatus(String status) {
        this.status = status;
        return this;
    }

    @Override
    public BpmProcessDefinitionBuilder setAssociatedData(List<Map<String,String>> associatedData) {
        this.associatedData = associatedData;
        return this;
    }

    @Override
    public BpmProcessDefinitionBuilder setPrivileges(List<BpmProcessPrivilege> privileges) {
        this.processPrivileges = privileges;
        return this;
    }

    @Override
    public BpmProcessDefinition create() {
        BpmProcessDefinitionImpl process = BpmProcessDefinitionImpl.from(dataModel, processName, association, version, status);
        process.update();
        return process;
    }
}
