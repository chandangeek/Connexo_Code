package com.energyict.mdc.protocol.inbound.dlms;

import com.energyict.mdc.upl.properties.PropertySpecPossibleValues;

import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.dlms.common.ObisCodePropertySpec;
import com.energyict.protocolimpl.properties.PropertySpecPossibleValuesImpl;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-04 (13:50)
 */
class ObisCodeWithDefaultValuePropertySpec extends ObisCodePropertySpec {

    private final ObisCode defaultValue;

    ObisCodeWithDefaultValuePropertySpec(String name, ObisCode defaultValue, String displayName, String description) {
        super(name, false, displayName, description);
        this.defaultValue = defaultValue;
    }

    @Override
    public PropertySpecPossibleValues getPossibleValues() {
        PropertySpecPossibleValuesImpl possibleValues = new PropertySpecPossibleValuesImpl();
        possibleValues.setDefault(this.defaultValue);
        return super.getPossibleValues();
    }

}