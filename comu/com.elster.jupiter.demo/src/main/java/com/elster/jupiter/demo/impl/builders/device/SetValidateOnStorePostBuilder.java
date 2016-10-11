package com.elster.jupiter.demo.impl.builders.device;

import com.elster.jupiter.demo.impl.templates.DeviceTypeTpl;
import com.elster.jupiter.metering.*;
import com.elster.jupiter.validation.ValidationService;
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

    @Inject
    public SetValidateOnStorePostBuilder(MeteringService meteringService,
                                         ValidationService validationService){
       this.meteringService = meteringService;
       this.validationService = validationService;
    }

    @Override
    public void accept(Device device) {
       Optional<EndDevice> endDevice = meteringService.findEndDevice(device.getmRID());
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
}
