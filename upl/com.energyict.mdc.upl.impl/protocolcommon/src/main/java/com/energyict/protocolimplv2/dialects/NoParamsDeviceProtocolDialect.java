package com.energyict.protocolimplv2.dialects;

import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.nls.Thesaurus;
import com.energyict.mdc.upl.properties.PropertySpec;

import com.energyict.protocolimplv2.DeviceProtocolDialectTranslationKeys;

import java.util.Collections;
import java.util.List;

/**
 * Simple dialect that has no parameters.
 * <p>
 * Copyrights EnergyICT
 * Date: 3/06/13
 * Time: 13:39
 * Author: khe
 */
public class NoParamsDeviceProtocolDialect extends AbstractDeviceProtocolDialect {

    public NoParamsDeviceProtocolDialect(NlsService nlsService) {
        super(null, nlsService);
    }

    public NoParamsDeviceProtocolDialect(Thesaurus thesaurus) {
        super(null, thesaurus);
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Collections.emptyList();
    }

    @Override
    public String getDeviceProtocolDialectName() {
        return DeviceProtocolDialectTranslationKeys.NO_PARAMETERS_PROTOCOL_DIALECT_NAME.getName();
    }

    @Override
    public String getDeviceProtocolDialectDisplayName() {
        return getThesaurus().getFormat(DeviceProtocolDialectTranslationKeys.NO_PARAMETERS_PROTOCOL_DIALECT_NAME).format();
    }
}