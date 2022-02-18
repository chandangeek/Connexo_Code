package com.energyict.protocolimplv2.dlms.ei7.properties;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.dlms.a2.properties.A2ConfigurationSupport;
import com.energyict.protocolimplv2.dlms.ei7.frames.CommunicationType;

import java.util.List;

public class EI7ConfigurationSupport extends A2ConfigurationSupport {

    public static final String BACK_FILL_ON_INBOUND = "BackFillOnInbound";
    public static final String COMMUNICATION_TYPE_STR = "CommunicationType"; // GPRS/NB-IoT

    public EI7ConfigurationSupport(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = super.getUPLPropertySpecs();
        propertySpecs.add(backFillOnInboundCommunication());
        propertySpecs.add(getGPRSorNBIoTPropertySpec());
        return propertySpecs;
    }

    protected PropertySpec backFillOnInboundCommunication() {
        return UPLPropertySpecFactory.specBuilder(BACK_FILL_ON_INBOUND, false, PropertyTranslationKeys.V2_DLMS_BACK_FILL_ON_INBOUND, getPropertySpecService()::booleanSpec).finish();
    }

    protected PropertySpec getGPRSorNBIoTPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(COMMUNICATION_TYPE_STR, false, PropertyTranslationKeys.V2_COMMUNICATION_TYPE,
                getPropertySpecService()::stringSpec)
                .addValues(CommunicationType.GPRS.getName(),CommunicationType.NB_IoT.getName())
                .setDefaultValue(CommunicationType.GPRS.getName())
                .markExhaustive()
                .finish();
    }
}
