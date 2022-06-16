package com.energyict.protocolimplv2.dlms.ei7.properties;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.cbo.BaseUnit;
import com.energyict.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.dlms.a2.properties.A2ConfigurationSupport;

import java.util.List;

public class EI7ConfigurationSupport extends A2ConfigurationSupport {

    public static final String BACK_FILL_ON_INBOUND = "BackFillOnInbound";
    public static final String COMMUNICATION_TYPE_STR = "CommunicationType"; // GPRS/NB-IoT
    public static final String DATA_VOLUME_UNIT_PROPERTY = "DataVolumeUnit";
    public static final String DATA_VOLUME_SCALAR_PROPERTY = "DataVolumeScalar";
    public static final int EI7_DEFAULT_DATA_VOLUME_UNIT_PROPERTY = BaseUnit.CUBICMETER;
    public static final int EI7_DEFAULT_DATA_VOLUME_SCALAR_PROPERTY = -2;

    public EI7ConfigurationSupport(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = super.getUPLPropertySpecs();
        propertySpecs.add(backFillOnInboundCommunication());
        propertySpecs.add(dataVolumeUnitPropertySpec());
        propertySpecs.add(dataVolumeScalarPropertySpec());
        return propertySpecs;
    }

    protected PropertySpec backFillOnInboundCommunication() {
        return UPLPropertySpecFactory.specBuilder(BACK_FILL_ON_INBOUND, false, PropertyTranslationKeys.V2_DLMS_BACK_FILL_ON_INBOUND, getPropertySpecService()::booleanSpec).finish();
    }

    protected PropertySpec dataVolumeUnitPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(DATA_VOLUME_UNIT_PROPERTY, false,
                PropertyTranslationKeys.V2_DLMS_DATA_VOLUME_UNIT, getPropertySpecService()::integerSpec)
                .addValues(BaseUnit.CUBICMETER, BaseUnit.NORMALCUBICMETER)
                .setDefaultValue(EI7_DEFAULT_DATA_VOLUME_UNIT_PROPERTY)
                .markExhaustive()
                .finish();
    }

    protected PropertySpec dataVolumeScalarPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(DATA_VOLUME_SCALAR_PROPERTY, false,
                PropertyTranslationKeys.V2_DLMS_DATA_VOLUME_SCALAR, getPropertySpecService()::integerSpec)
                .setDefaultValue(EI7_DEFAULT_DATA_VOLUME_SCALAR_PROPERTY)
                .finish();
    }
}
