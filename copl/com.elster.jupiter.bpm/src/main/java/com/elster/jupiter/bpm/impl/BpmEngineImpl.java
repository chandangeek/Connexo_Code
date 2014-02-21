package com.elster.jupiter.bpm.impl;

import com.elster.jupiter.bpm.BpmEngine;
import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.time.UtcInstant;

import javax.inject.Inject;
import java.util.List;

public class BpmEngineImpl implements BpmEngine {
    private String name;
    private String location;

    protected final BpmService bpmService;
    protected final DataModel dataModel;

    private long version;
    private UtcInstant createTime;
    private UtcInstant modTime;
    @SuppressWarnings("unused")
    private String userName;

    @Inject
    public BpmEngineImpl(DataModel dataModel, BpmService bpmService) {
        this.bpmService = bpmService;
        this.dataModel = dataModel;
    }

    static BpmEngineImpl from(DataModel dataModel, String domain) {
        return dataModel.getInstance(BpmEngineImpl.class).init(domain);
    }

    BpmEngineImpl init(String name) {
        setName(name);
        return this;
    }

    @Override
    public String getLocation() {
        return location;
    }

    @Override
    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void save() {
        dataModel.persist(this);
    }

    @Override
    public List<String> getProcesses() {
        //TODO: get the list of defined processes from the BPM engine using the REST API
        return null;
    }

    @Override
    public long startProcessInstance(String process) {
        //TODO: start a new process instance on the BPM engine using the REST API
        return 0;
    }
}
