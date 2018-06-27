package com.energyict.mdc.device.data.importers.impl;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.importers.ImporterExtension;

import org.osgi.service.component.annotations.Component;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component(name = "com.energyict.mdc.device.data.importers.importerextension",service = ImporterExtension.class, immediate = true, property = {
        "name=" + ImporterExtensionImpl.NAME })
public class ImporterExtensionImpl implements ImporterExtension {
    public final static String NAME = "ImporterExtension";
    private static final Logger LOGGER = Logger.getLogger(ImporterExtensionImpl.class.getName());

    @Override
    public void process(Device device, Map<String,String> values, Logger logger){
        LOGGER.log(Level.INFO, "Unsupported operation: in need to be overriden");
    }
}