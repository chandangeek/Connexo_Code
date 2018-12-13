/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm.impl;

import com.elster.jupiter.bpm.BpmProcessDefinition;
import com.elster.jupiter.bpm.BpmProcessDefinitionBuilder;
import com.elster.jupiter.bpm.BpmProcessPrivilege;
import com.elster.jupiter.orm.DataModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class BpmProcessDefinitionBuilderImpl implements BpmProcessDefinitionBuilder {

    private final DataModel dataModel;
    @SuppressWarnings("unused") // Managed by ORM
    private String id;
    private String processName;
    private String association;
    private String version;
    private String status;
    private String appKey;
    private List<BpmProcessPrivilege> processPrivileges = new ArrayList<>();
    private Map<String, Object> properties = new HashMap<>();

    BpmProcessDefinitionBuilderImpl(DataModel dataModel) {
        this.dataModel = dataModel;
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
    public BpmProcessDefinitionBuilder setAppKey(String appKey) {
        this.appKey = appKey;
        return this;
    }

    @Override
    public BpmProcessDefinitionBuilder setProperties(Map<String, Object> properties) {
        this.properties = properties;
        return this;
    }

    @Override
    public BpmProcessDefinitionBuilder setPrivileges(List<BpmProcessPrivilege> privileges) {
        this.processPrivileges = Collections.unmodifiableList(privileges);
        return this;
    }

    @Override
    public BpmProcessDefinition create() {
        BpmProcessDefinitionImpl process = BpmProcessDefinitionImpl.from(dataModel, processName, association, version, status, appKey, processPrivileges);
        process.setProperties(properties);
        process.save();
        return process;
    }

}