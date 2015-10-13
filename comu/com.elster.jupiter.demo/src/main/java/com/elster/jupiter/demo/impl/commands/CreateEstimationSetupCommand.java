package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.templates.EstimationRuleSetTpl;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.metering.*;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceService;

import javax.inject.Inject;
import java.util.List;

import static com.elster.jupiter.util.conditions.Where.where;

public class CreateEstimationSetupCommand {

    private final EstimationService estimationService;
    private final DeviceConfigurationService deviceConfigurationService;
    private final MeteringService meteringService;
    private final DeviceService deviceService;

    private EstimationRuleSet estimationRuleSet;

    @Inject
    public CreateEstimationSetupCommand(
            EstimationService estimationService,
            DeviceConfigurationService deviceConfigurationService,
            MeteringService meteringService,
            DeviceService deviceService) {
        this.estimationService = estimationService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.meteringService = meteringService;
        this.deviceService = deviceService;
    }

    public void run(){
        createEstimationRuleSet();
        addEstimationToDeviceConfigurations();
        addEstimationToDevices();
    }

    private void createEstimationRuleSet(){
        this.estimationRuleSet = Builders.from(EstimationRuleSetTpl.RESIDENTIAL_CUSTOMERS).get();
    }

    private void addEstimationToDeviceConfigurations(){
        List<DeviceConfiguration> configurations = deviceConfigurationService.getLinkableDeviceConfigurations(this.estimationRuleSet);
        for (DeviceConfiguration configuration : configurations) {
            System.out.println("==> Estimation rule set added to: " + configuration.getName() + " (id = " + configuration.getId() + ")");
            configuration.addEstimationRuleSet(this.estimationRuleSet);
            configuration.save();
        }
    }

    private void addEstimationToDevices(){

//        Condition devicesForActivation = where("mRID").like(Constants.Device.STANDARD_PREFIX + "*");
//        Finder<Device> finder = deviceService.findAllDevices(devicesForActivation);
//
//        List<Meter> meters =  deviceService.findAllDevices(devicesForActivation).find();
//        System.out.println("==> Validation will be activated for " + meters.size() + " devices");
//        for (Meter meter : meters) {
//            estimationService.activate(meterActivation, this.estimationRuleSet);
//        }
    }
}
