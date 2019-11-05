package com.energyict.protocolimplv2.dlms.a2.properties;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsConfigurationSupport;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class A2ConfigurationSupport extends DlmsConfigurationSupport {

    public static final String CALLING_AP_TITLE = "CallingAPTitle";
    public static final String LIMIT_MAX_NR_OF_DAYS_PROPERTY = "LimitMaxNrOfDays";

    public A2ConfigurationSupport(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        List<PropertySpec> propertySpecs = new ArrayList<>();
        return propertySpecs;
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        List<PropertySpec> optionalProperties = new ArrayList<>(super.getOptionalProperties());
        optionalProperties.add(callingAPTitlePropertySpec());
        optionalProperties.add(limitMaxNumberOfDaysPropertySpec());
        return optionalProperties;
    }

    private PropertySpec limitMaxNumberOfDaysPropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(LIMIT_MAX_NR_OF_DAYS_PROPERTY, BigDecimal.ZERO);
    }

    private PropertySpec callingAPTitlePropertySpec() {
        return PropertySpecFactory.fixedLengthHexStringPropertySpec(CALLING_AP_TITLE, 8);
    }
}
