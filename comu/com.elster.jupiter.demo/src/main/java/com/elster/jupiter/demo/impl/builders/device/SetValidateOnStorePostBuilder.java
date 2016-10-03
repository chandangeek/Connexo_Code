package com.elster.jupiter.demo.impl.builders.device;

import com.elster.jupiter.demo.impl.builders.DataValidationTaskBuilder;
import com.elster.jupiter.demo.impl.builders.DeviceGroupBuilder;
import com.elster.jupiter.demo.impl.templates.DeviceGroupTpl;
import com.elster.jupiter.demo.impl.templates.DeviceTypeTpl;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.Device;

import javax.inject.Inject;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Enables the validationOnStore flag and activates the validation on a device
 * Copyrights EnergyICT
 * Date: 1/10/2015
 * Time: 14:31
 */
public class SetValidateOnStorePostBuilder implements Consumer<Device> {

    private final ValidationService validationService;
    private final MeteringService meteringService;
    private final DeviceGroupBuilder deviceGroupBuilder;
    private final DataValidationTaskBuilder validationTaskBuilder;

    private static EndDeviceGroup deviceGroup;

    @Inject
    public SetValidateOnStorePostBuilder(MeteringService meteringService,
                                         MeteringGroupsService meteringGroupsService,
                                         DeviceConfigurationService deviceConfigurationService,
                                         ValidationService validationService,
                                         SearchService searchService){
       this.meteringService = meteringService;
       this.validationService = validationService;
       this.deviceGroupBuilder =  DeviceGroupTpl.A1800_DEVICES.get(new DeviceGroupBuilder(meteringGroupsService, deviceConfigurationService, searchService));
       this.validationTaskBuilder = new DataValidationTaskBuilder(validationService);
    }

    @Override
    public void accept(Device device) {
       if (device.getDeviceType().getName().equals(DeviceTypeTpl.Elster_A1800.getLongName())) {
            if (deviceGroup == null) {
                initDeviceGroup();
            }
       }
       Optional<EndDevice> endDevice = meteringService.findEndDeviceByMRID(device.getmRID());
       if (endDevice.isPresent()) {
           Optional<Meter> meter = endDevice.get().getAmrSystem().findMeter("" + endDevice.get().getId());
           if (meter.isPresent()) {
               if (!device.getDeviceType().getName().equals(DeviceTypeTpl.Elster_A1800.getLongName())){
                   validationService.enableValidationOnStorage(meter.get());
               }
               validationService.activateValidation(meter.get());
           }
       }

    }

    private void initDeviceGroup(){
        deviceGroup = this.deviceGroupBuilder.withPostBuilder(new DeviceGroupPostBuilder()).get();
    }

    /**
     * Creates a Validation Task for the given Device Group
     **/
    private class DeviceGroupPostBuilder implements Consumer<EndDeviceGroup>{

        @Override
        public void accept(EndDeviceGroup deviceGroup) {
             SetValidateOnStorePostBuilder.this.validationTaskBuilder.withName(deviceGroup.getName())
                                                                     .withEndDeviceGroup(deviceGroup)
                                                                     .get();
        }
    }
}
