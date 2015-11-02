package com.elster.jupiter.bpm.impl;

import com.elster.jupiter.bpm.BpmProcessDefinition;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;


public class BpmProcessDefinitionImpl implements BpmProcessDefinition{

    private long id;
    private final DataModel dataModel;
    private String processName;
    private String association;
    private String version;
    private String status;

    @Inject
    public BpmProcessDefinitionImpl(DataModel dataModel){
        this.dataModel = dataModel;
    }

    static BpmProcessDefinitionImpl from(DataModel dataModel, String processName, String association, String version, String status ){
        return dataModel.getInstance(BpmProcessDefinitionImpl.class).init(processName, association, version, status);
    }

    private BpmProcessDefinitionImpl init(String processName, String association, String version, String status ){
        this.association = association;
        this.version = version;
        this.processName = processName;
        this.status = status;
        return this;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getProcessName() {
        return processName;
    }

    @Override
    public String getAssociation() {
        return association;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public String getStatus(){
        return status;
    }

    @Override
    public void save() {
        if (getId() == 0) {
            Save.CREATE.save(dataModel, this);
        } else {
            Save.UPDATE.save(dataModel, this);
        }
    }

    @Override
    public void setStatus(String status){
        this.status = status;
    }

    @Override
    public void delete(){
        dataModel.remove(this);
    }
}
