package com.elster.jupiter.bpm.impl;

import com.elster.jupiter.bpm.BpmProcessDefinition;
import com.elster.jupiter.bpm.BpmProcessDeviceState;
import com.elster.jupiter.bpm.BpmProcessPrivilege;
import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class BpmProcessDefinitionImpl implements BpmProcessDefinition{

    private long id;
    private final DataModel dataModel;
    private String processName;
    private String association;
    private String version;
    private String status;
    private List<BpmProcessPrivilege> processPrivileges = new ArrayList<>();
    private List<BpmProcessDeviceState> processDeviceStates = new ArrayList<>();
    private final BpmService bpmService;
    private final UserService userService;

    @Inject
    public BpmProcessDefinitionImpl(DataModel dataModel,BpmService bpmService, UserService userService){
        this.dataModel = dataModel;
        this.bpmService = bpmService;
        this.userService = userService;
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
    public void revokePrivileges(List<BpmProcessPrivilege> processPrivileges){
            processPrivileges.stream().forEach(BpmProcessPrivilege::delete);
    }

    @Override
    public void revokeProcessDeviceStates(List<BpmProcessDeviceState> processDeviceStates){
        processDeviceStates.stream().forEach(BpmProcessDeviceState::delete);
    }

    @Override
    public void grantProcessDeviceStates(List<BpmProcessDeviceState> processDeviceStates){
        processDeviceStates.stream().forEach(BpmProcessDeviceState::persist);
    }

    @Override
    public void grantPrivileges(List<BpmProcessPrivilege> targetPrivileges){
            targetPrivileges.stream().forEach(BpmProcessPrivilege::persist);
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

    @Override
    public List<BpmProcessPrivilege> getPrivileges() {
        return processPrivileges;
    }

    @Override
    public List<BpmProcessDeviceState> getProcessDeviceStates() {
        return processDeviceStates;
    }
}
