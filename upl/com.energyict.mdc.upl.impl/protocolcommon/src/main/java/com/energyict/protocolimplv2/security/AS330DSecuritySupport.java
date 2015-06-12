package com.energyict.protocolimplv2.security;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecPossibleValuesImpl;

import java.math.BigDecimal;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 10/06/2015 - 11:45
 */
public class AS330DSecuritySupport extends DsmrSecuritySupport {

    /**
     * Same as the normal property spec, but with default value 2
     */
    @Override
    @SuppressWarnings("unchecked")
    protected PropertySpec getClientMacAddressPropertySpec() {
        PropertySpec propertySpec = super.getClientMacAddressPropertySpec();
        PropertySpecPossibleValuesImpl possibleValues = (PropertySpecPossibleValuesImpl) propertySpec.getPossibleValues();
        possibleValues.setDefault(BigDecimal.valueOf(2));
        return propertySpec;
    }
}