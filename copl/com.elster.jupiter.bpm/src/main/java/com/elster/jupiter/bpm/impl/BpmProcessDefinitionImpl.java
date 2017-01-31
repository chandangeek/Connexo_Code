/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm.impl;

import com.elster.jupiter.bpm.BpmProcessDefinition;
import com.elster.jupiter.bpm.BpmProcessDeviceState;
import com.elster.jupiter.bpm.BpmProcessPrivilege;
import com.elster.jupiter.bpm.BpmService;
import com.elster.jupiter.bpm.ProcessAssociationProvider;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.collections.ArrayDiffList;
import com.elster.jupiter.util.collections.DiffList;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/* Cannot use validators as we need to be 10.1 backward compatible;
 * but they can be used later when we will no longer need to maintain compatibility with 10.1
 *@HasValidProperties(groups = {Save.Create.class, Save.Update.class}, requiredPropertyMissingMessage = "{" + MessageSeeds.Constants.FIELD_CAN_NOT_BE_EMPTY + "}")
 */
class BpmProcessDefinitionImpl implements BpmProcessDefinition {

    private final BpmService bpmService;
    private final DataModel dataModel;
    @SuppressWarnings("unused") // Managed by ORM
    private long id;
    private String processName;
    private String association;
    private String version;
    private String status;
    private String appKey;

    @SuppressWarnings("unused") // Managed by ORM
    private long versionDB;

    // Deprecated, for 10.1 compatibility only
    private List<BpmProcessDeviceState> processDeviceStates = new ArrayList<>();

    // Cannot use validators as we need to be 10.1 backward compatible
    //@Valid
    //@Size(min = 1, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Constants.FIELD_CAN_NOT_BE_EMPTY + "}")
    private List<BpmProcessPrivilege> processPrivileges = new ArrayList<>();

    // Cannot use validators as we need to be 10.1 backward compatible
    //@Valid
    private List<BpmProcessProperty> properties = new ArrayList<>();

    @Inject
    BpmProcessDefinitionImpl(DataModel dataModel,BpmService bpmService) {
        this.dataModel = dataModel;
        this.bpmService = bpmService;
    }

    static BpmProcessDefinitionImpl from(DataModel dataModel, String processName, String association, String version, String status, String appKey, List<BpmProcessPrivilege> processPrivileges) {
        return dataModel.getInstance(BpmProcessDefinitionImpl.class).init(processName, association, version, status, appKey, processPrivileges);
    }

    private BpmProcessDefinitionImpl init(String processName, String association, String version, String status, String appKey, List<BpmProcessPrivilege> processPrivileges) {
        this.association = association;
        this.version = version;
        this.processName = processName;
        this.status = status;
        this.appKey = appKey;
        this.processPrivileges.addAll(processPrivileges);
        return this;
    }

    @Override
    public void revokePrivileges(List<BpmProcessPrivilege> processPrivileges){
        processPrivileges.stream().forEach(BpmProcessPrivilege::delete);
    }

    @Override
    public void revokeProcessDeviceStates(List<BpmProcessDeviceState> processDeviceStates) {
        processDeviceStates.stream().forEach(BpmProcessDeviceState::delete);
    }

    @Override
    public void grantProcessDeviceStates(List<BpmProcessDeviceState> processDeviceStates) {
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
        Optional<ProcessAssociationProvider> foundProvider = getAssociationProvider();
        return foundProvider.isPresent() ? foundProvider.get().getType() : this.association;
    }

    @Override
    public void setAssociation(String association) {
        this.association = association;
    }

    @Override
    public Optional<ProcessAssociationProvider> getAssociationProvider() {
        return bpmService.getProcessAssociationProvider(association);
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public long getVersionDB() {
        return versionDB;
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
    public String getAppKey(){
        return appKey;
    }

    @Override
    public void setAppKey(String appKey) {
        this.appKey = appKey;
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
    public void delete(){
        dataModel.remove(this);
    }

    @Override
    public List<BpmProcessPrivilege> getPrivileges() {
        return Collections.unmodifiableList(processPrivileges);
    }

    @Override
    public void setPrivileges(List<BpmProcessPrivilege> privileges) {
        this.processPrivileges.clear();
        this.processPrivileges.addAll(privileges);
    }

    @Override
    public List<BpmProcessDeviceState> getProcessDeviceStates() {
        return Collections.unmodifiableList(processDeviceStates);
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
        Optional<ProcessAssociationProvider> processAssociationProvider = getAssociationProvider();
        if(processAssociationProvider.isPresent()) {
            return processAssociationProvider.get().getPropertySpecs();
        }
        return Collections.emptyList();
    }

}