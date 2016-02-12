package com.energyict.mdc.bpm.impl;

import com.elster.jupiter.bpm.BpmProcessDefinition;
import com.elster.jupiter.bpm.ProcessAssociationProvider;

import com.energyict.mdc.bpm.BpmProcessDeviceState;
import com.energyict.mdc.bpm.DeviceBpmService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;


@Component(name = "com.energyict.mdc.bpm.impl.DeviceProcessAssociationProvider",
        service = {ProcessAssociationProvider.class},
        property = "name=" + DeviceBpmService.COMPONENTNAME, immediate = true)
public class DeviceProcessAssociationProvider implements ProcessAssociationProvider {
    public static final String DEVICE_PROCESSASSOCIATION_PROVIDER = DeviceProcessAssociationProvider.class.getName();
    public static final String TYPE = "device";
    private String name;
    private String type = TYPE;
    private String data;
    private volatile DeviceBpmService deviceBpmService;

    //For OSGI purposes
    public DeviceProcessAssociationProvider() {
    }

    //For testing purposes
    @Inject
    public DeviceProcessAssociationProvider(String name, String type, String data) {
        this.name = name;
        this.type = type;
        this.data = data;
    }

    @Override
    public String getName() {
        return DEVICE_PROCESSASSOCIATION_PROVIDER;
    }
    //public String getName() {return name;}


    @Override
    public List<Map<String, String>> getDataProperties(BpmProcessDefinition bpmProcessDefinition) {
        List<Map<String, String>> deviceStates = new ArrayList<>();
        Optional<List<BpmProcessDeviceState>> processDeviceStates = deviceBpmService.findBpmProcessDeviceStates(bpmProcessDefinition.getId());
        if (processDeviceStates.isPresent()) {
            for (BpmProcessDeviceState state : processDeviceStates.get()) {
                Map<String, String> deviceStateElements = new HashMap<>();
                deviceStateElements.put("deviceStateId", String.valueOf(state.getDeviceStateId()));
                deviceStateElements.put("lifecycleId", String.valueOf(state.getDeviceLifeCycleId()));
                deviceStateElements.put("name", state.getName());
                deviceStateElements.put("deviceState", state.getDeviceState());
                deviceStates.add(deviceStateElements);
            }
        }
        return deviceStates;
    }

    @Override
    public String getType() {
        return type;
    }


    @Override
    public void update(BpmProcessDefinition bpmProcessDefinition, List<Map<String, String>> stateList) {
        Optional<List<BpmProcessDeviceState>> currentDeviceStates = deviceBpmService.findBpmProcessDeviceStates(bpmProcessDefinition.getId());
        Optional<List<BpmProcessDeviceState>> targetDeviceStates = Optional.of(stateList.stream()
                .map(s -> deviceBpmService.createBpmProcessDeviceState(bpmProcessDefinition, Long.parseLong(s.get("deviceStateId")),
                        Long.parseLong(s.get("deviceLifeCycleId")), s.get("name"), s.get("deviceState"))).collect(Collectors.toList()));
        doUpdateProcessDeviceStates(currentDeviceStates, targetDeviceStates);
    }


    @Reference
    public void setDeviceBpmService(DeviceBpmService deviceBpmService) {
        this.deviceBpmService = deviceBpmService;
    }


    private void doUpdateProcessDeviceStates(Optional<List<BpmProcessDeviceState>> currentDeviceStates, Optional<List<BpmProcessDeviceState>> targetDeviceStates) {
        currentDeviceStates.ifPresent(l -> l.sort((s1, s2) -> s1.getName().compareTo(s2.getName())));
        targetDeviceStates.ifPresent(l -> l.sort((s1, s2) -> s1.getName().compareTo(s2.getName())));
        if (targetDeviceStates.isPresent()) {
            if (currentDeviceStates.isPresent() && !targetDeviceStates.equals(currentDeviceStates)) {
                deviceBpmService.revokeProcessDeviceStates(currentDeviceStates.get());
                deviceBpmService.grantProcessDeviceStates(targetDeviceStates.get());
            } else if (!currentDeviceStates.isPresent()) {
                deviceBpmService.grantProcessDeviceStates(targetDeviceStates.get());
            }
        }
    }
}
