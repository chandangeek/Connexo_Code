package com.energyict.mdc.device.data.importers;
import com.energyict.mdc.device.data.Device;

import aQute.bnd.annotation.ProviderType;

import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by H241414 on 3/19/2018.
 */@ProviderType
public interface ImporterExtension {
    void process(Device device, Map<String,String> values, Logger logger);
}

