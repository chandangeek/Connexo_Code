package com.elster.jupiter.bpm.impl;

import com.elster.jupiter.bpm.*;
import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


public class BpmProcessDefinitionImpl implements BpmProcessDefinition{

    private long id;
    private String type;
    private final DataModel dataModel;
    private String processName;
    private String association;
    private String version;
    private String status;
    private List<BpmProcessPrivilege> processPrivileges = new ArrayList<>();
    private List<Map<String, String>> associationData;


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
    public void update() {
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
    public List<Map<String, String>> getAssociationData() {
        return associationData;
    }

    @Override
    public void setAssociationData(List<Map<String, String>> associationData) {
        this.associationData = associationData;
    }

    @Override
    public void setProcessPrivileges(List<BpmProcessPrivilege> processPrivileges) {
        this.processPrivileges = processPrivileges;
    }

    @Override
    public List<BpmProcessPrivilege> getProcessPrivileges() {
        return processPrivileges;
    }


}
