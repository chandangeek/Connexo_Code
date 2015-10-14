package com.elster.jupiter.demo.impl.commands;

import com.elster.jupiter.demo.impl.Builders;
import com.elster.jupiter.demo.impl.Constants;
import com.elster.jupiter.demo.impl.templates.EstimationRuleSetTpl;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.*;
import com.energyict.mdc.device.data.impl.ServerDeviceService;

import javax.inject.Inject;
import java.util.List;

import static com.elster.jupiter.util.conditions.Where.where;

public class CreateEstimationSetupCommand {

    private final DeviceConfigurationService deviceConfigurationService;
    private final DeviceService deviceService;

    private EstimationRuleSet estimationRuleSet;

    @Inject
    public CreateEstimationSetupCommand(
            DeviceConfigurationService deviceConfigurationService,
            DeviceService deviceService) {
        this.deviceConfigurationService = deviceConfigurationService;
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
        Condition devicesForActivation = where("mRID").like(Constants.Device.STANDARD_PREFIX + "*");
        deviceService.findAllDevices(devicesForActivation)
                .stream().map(Device::forEstimation).forEach(DeviceEstimation::activateEstimation);

    }
}
