package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.rest.util.LocalizedExceptionMapper;

import javax.inject.Inject;

/**
 * Copyrights EnergyICT
 * Date: 31/03/14
 * Time: 15:52
 */
public class DeviceConfigurationLocalizedExceptionMapper extends LocalizedExceptionMapper {
    @Inject
    public DeviceConfigurationLocalizedExceptionMapper(NlsService nlsService) {
        super(nlsService);
        fieldMappings(DeviceTypeInfo.fieldMappings());
    }
}
