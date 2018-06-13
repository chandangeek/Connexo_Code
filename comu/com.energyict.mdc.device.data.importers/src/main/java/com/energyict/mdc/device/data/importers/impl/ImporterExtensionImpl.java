package com.energyict.mdc.device.data.importers.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.importers.ImporterExtension;

import org.osgi.service.component.annotations.Component;

import java.util.Map;
import java.util.logging.Logger;

@Component(name = "com.elster.jupiter.demo.importextension",
        property = "importer.extension=SecureDeviceShipmentImporter" )
public class ImporterExtensionImpl implements ImporterExtension {
    public void process(Device device, Map<String,String> values, Logger logger){
    }
}