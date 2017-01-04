package com.energyict.mdc.protocol.inbound.dlms.aso;

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
public class ObisCodeWithDefaultValuePropertySpec extends ObisCodePropertySpec {

    private final ObisCode defaultValue;

    public ObisCodeWithDefaultValuePropertySpec(String name, ObisCode defaultValue) {
        super(name, false);
        this.defaultValue = defaultValue;
    }

    @Override
    public PropertySpecPossibleValues getPossibleValues() {
        PropertySpecPossibleValuesImpl possibleValues = new PropertySpecPossibleValuesImpl();
        possibleValues.setDefault(this.defaultValue);
        return super.getPossibleValues();
    }
}