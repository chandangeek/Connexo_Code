package com.energyict.mdc.device.data.impl.configchange;

import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfigChangeAction;
import com.energyict.mdc.device.config.DeviceConfigChangeEngine;
import com.energyict.mdc.device.config.DeviceConfiguration;

import java.util.List;
import java.util.function.Predicate;

/**
 * Copyrights EnergyICT
 * Date: 23.09.15
 * Time: 14:00
 */
public class ComTaskExecutionConfigChangeItem extends AbstractConfigChangeItem {

    private static final ComTaskExecutionConfigChangeItem INSTANCE = new ComTaskExecutionConfigChangeItem();

    private ComTaskExecutionConfigChangeItem() {
    }

    static ComTaskExecutionConfigChangeItem getInstance(){
        return INSTANCE;
    }

    @Override
    public void apply(ServerDeviceForConfigChange device, DeviceConfiguration originDeviceConfiguration, DeviceConfiguration destinationDeviceConfiguration) {
        final List<DeviceConfigChangeAction<ComTaskEnablement>> comTaskActions = DeviceConfigChangeEngine.INSTANCE.calculateDeviceConfigChangeActionsFor(new DeviceConfigComTaskEnablementItem(originDeviceConfiguration, destinationDeviceConfiguration));

        //TODO continue
    }

    private class DeviceConfigComTaskEnablementItem extends AbstractDeviceConfigChangeItem<ComTaskEnablement>{

        DeviceConfigComTaskEnablementItem(DeviceConfiguration originDeviceConfig, DeviceConfiguration destinationDeviceConfig) {
            super(originDeviceConfig, destinationDeviceConfig);
        }

        @Override
        public List<ComTaskEnablement> getOriginItems() {
            return originDeviceConfig.getComTaskEnablements();
        }

        @Override
        public List<ComTaskEnablement> getDestinationItems() {
            return destinationDeviceConfig.getComTaskEnablements();
        }

        @Override
        public Predicate<ComTaskEnablement> exactSameItem(ComTaskEnablement item) {
            return comTaskEnablement -> comTaskEnablement.getComTask().getId() == item.getComTask().getId();
        }

        @Override
        public Predicate<ComTaskEnablement> isItAConflict(ComTaskEnablement item) {
            return comTaskEnablement -> false; // no conflicts possible (for now)
        }
    }
}
