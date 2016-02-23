package com.elster.jupiter.bpm.impl;

import com.elster.jupiter.bpm.*;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.collections.ArrayDiffList;
import com.elster.jupiter.util.collections.DiffList;

import javax.inject.Inject;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;


public class BpmProcessDefinitionImpl implements BpmProcessDefinition{

    private final BpmService bpmService;
    private final DataModel dataModel;
    private long id;
    private String type;
    private String processName;
    private String association;
    private String version;
    private String status;
    @Valid
    private List<BpmProcessPrivilege> processPrivileges = new ArrayList<>();
    @Valid
    private List<BpmProcessProperty> properties = new ArrayList<>();

    @Inject
    public BpmProcessDefinitionImpl(DataModel dataModel,BpmService bpmService){
        this.dataModel = dataModel;
        this.bpmService = bpmService;
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
    public Optional<ProcessAssociationProvider> getAssociation() {
        return bpmService.getProcessAssociationProvider(association);
    }

    @Override
    public void setAssociation(String association) {
        this.association = association;
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
    public void setStatus(String status) {
        this.status = status;
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
    public void delete(){
        dataModel.remove(this);
    }

    @Override
    public List<BpmProcessPrivilege> getPrivileges() {
        return processPrivileges;
    }

    void setPrivileges(List<BpmProcessPrivilege> privileges) {
        this.processPrivileges = privileges;
    }

    @Override
    public Map<String, Object> getProperties() {
        return properties.stream().collect(Collectors.toMap(BpmProcessProperty::getName, BpmProcessProperty::getValue));
    }

    @Override
    public void setProperties(Map<String, Object> propertyMap) {
        Map<String, BpmProcessProperty> originalProps = properties.stream().collect(Collectors.toMap(BpmProcessProperty::getName, Function.identity()));
        DiffList<String> entryDiff = ArrayDiffList.fromOriginal(originalProps.keySet());
        entryDiff.clear();
        entryDiff.addAll(propertyMap.keySet());

        for (String property : entryDiff.getRemovals()) {
            BpmProcessProperty bpmProcessProperty = originalProps.get(property);
            properties.remove(bpmProcessProperty);
        }

        for (String property : entryDiff.getRemaining()) {
            BpmProcessProperty bpmProcessProperty = originalProps.get(property);
            bpmProcessProperty.setValue(propertyMap.get(property));
            dataModel.mapper(BpmProcessProperty.class).update(bpmProcessProperty);
        }

        for (String property : entryDiff.getAdditions()) {
            addProperty(property, propertyMap.get(property));
        }
    }

    BpmProcessProperty addProperty(String name, Object value) {
        BpmProcessProperty newProperty = dataModel.getInstance(BpmProcessPropertyImpl.class).init(this, name, value);
        properties.add(newProperty);
        return newProperty;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        Optional<ProcessAssociationProvider> processAssociationProvider  =getAssociation();
        if(processAssociationProvider.isPresent()) {
            return processAssociationProvider.get().getPropertySpecs();
        }
        return Collections.EMPTY_LIST;
    }
}
