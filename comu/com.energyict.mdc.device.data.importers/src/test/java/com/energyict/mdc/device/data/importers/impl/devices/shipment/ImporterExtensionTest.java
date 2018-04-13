package com.energyict.mdc.device.data.importers.impl.devices.shipment;

import com.energyict.mdc.device.data.Device;

import com.energyict.mdc.device.data.importers.ImporterExtension;
import org.osgi.service.component.annotations.Component;

import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by H241414 on 3/19/2018.
 */
@Component(name = "com.elster.jupiter.demo.importextension",
        property = "importer.extension=SecureDeviceShipmentImporter" )
public class ImporterExtensionTest implements ImporterExtension {
    public void process(Device device, Map<String,String> values, Logger logger){
    }
}
