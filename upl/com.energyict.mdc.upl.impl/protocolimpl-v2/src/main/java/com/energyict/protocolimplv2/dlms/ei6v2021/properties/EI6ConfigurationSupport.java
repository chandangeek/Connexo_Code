package com.energyict.protocolimplv2.dlms.ei6v2021.properties;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.cbo.BaseUnit;
import com.energyict.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.dlms.a2.properties.A2ConfigurationSupport;
import com.energyict.protocolimplv2.messages.NetworkConnectivityMessage;

import java.util.List;

public class EI6ConfigurationSupport extends A2ConfigurationSupport {
    public static final String BACK_FILL_ON_INBOUND = "BackFillOnInbound";
    public static final String COMMUNICATION_TYPE_STR = "CommunicationType"; // GPRS/NB-IoT
    public static final String DATA_VOLUME_UNIT_PROPERTY = "DataVolumeUnit";
    public static final String DATA_VOLUME_SCALAR_PROPERTY = "DataVolumeScalar";

    public static final int EI6_DEFAULT_DATA_VOLUME_UNIT_PROPERTY = BaseUnit.NORMALCUBICMETER;
    public static final int EI6_DEFAULT_DATA_VOLUME_SCALAR_PROPERTY = -3;

    public EI6ConfigurationSupport(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = super.getUPLPropertySpecs();
        propertySpecs.add(backFillOnInboundCommunication());
        propertySpecs.add(gprsOrNbiotPropertySpec());
        propertySpecs.add(dataVolumeUnitPropertySpec());
        propertySpecs.add(dataVolumeScalarPropertySpec());
        return propertySpecs;
    }

    private PropertySpec backFillOnInboundCommunication() {
        return UPLPropertySpecFactory.specBuilder(BACK_FILL_ON_INBOUND, false, PropertyTranslationKeys.V2_DLMS_BACK_FILL_ON_INBOUND, getPropertySpecService()::booleanSpec).finish();
    }

    private PropertySpec gprsOrNbiotPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(COMMUNICATION_TYPE_STR, false, PropertyTranslationKeys.V2_COMMUNICATION_TYPE,
                getPropertySpecService()::stringSpec)
                .addValues(NetworkConnectivityMessage.TimeoutType.GPRS.name(), NetworkConnectivityMessage.TimeoutType.NBIOT.name())
                .setDefaultValue(NetworkConnectivityMessage.TimeoutType.GPRS.name())
                .markExhaustive()
                .finish();
    }

    private PropertySpec dataVolumeUnitPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(DATA_VOLUME_UNIT_PROPERTY, false,
                PropertyTranslationKeys.V2_DLMS_DATA_VOLUME_UNIT, getPropertySpecService()::integerSpec)
                .addValues(BaseUnit.CUBICMETER, BaseUnit.NORMALCUBICMETER)
                .setDefaultValue(EI6_DEFAULT_DATA_VOLUME_UNIT_PROPERTY)
                .markExhaustive()
                .finish();
    }

    private PropertySpec dataVolumeScalarPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(DATA_VOLUME_SCALAR_PROPERTY, false,
                PropertyTranslationKeys.V2_DLMS_DATA_VOLUME_SCALAR, getPropertySpecService()::integerSpec)
                .setDefaultValue(EI6_DEFAULT_DATA_VOLUME_SCALAR_PROPERTY)
                .finish();
    }
}
