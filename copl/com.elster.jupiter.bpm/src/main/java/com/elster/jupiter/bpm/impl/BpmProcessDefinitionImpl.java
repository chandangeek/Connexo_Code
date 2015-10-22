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
    private boolean state;

    @Inject
    public BpmProcessDefinitionImpl(DataModel dataModel){
        this.dataModel = dataModel;
    }

    static BpmProcessDefinitionImpl from(DataModel dataModel, String processName, String association, String version, boolean state ){
        return dataModel.getInstance(BpmProcessDefinitionImpl.class).init(processName, association, version, state);
    }

    private BpmProcessDefinitionImpl init(String processName, String association, String version, boolean state ){
        this.association = association;
        this.version = version;
        this.processName = processName;
        this.state = state;
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
    public boolean getState(){
        return state;
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
    public void setState(boolean state){
        this.state = state;
    }

    @Override
    public void delete(){
        dataModel.remove(this);
    }
}
