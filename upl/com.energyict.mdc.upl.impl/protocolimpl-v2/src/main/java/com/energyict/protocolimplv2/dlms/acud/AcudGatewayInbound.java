package com.energyict.protocolimplv2.dlms.acud;

import com.energyict.mdc.protocol.inbound.g3.PushEventNotification;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.nls.PropertyTranslationKeys;
import com.energyict.protocolimplv2.dlms.acud.properties.AcudConfigurationSupport;
import com.energyict.protocolimplv2.dlms.acud.properties.AcudDlmsProperties;
import com.energyict.protocolimplv2.nta.dsmr23.DlmsProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import static com.energyict.dlms.common.DlmsProtocolProperties.TIMEZONE;


public class AcudGatewayInbound extends PushEventNotification {
    private AcudGatewayDataPushNotificationParser parser;
    private DlmsProperties dlmsProperties;
    protected AcudConfigurationSupport dlmsConfigurationSupport;

    private PropertySpecService propertySpecService;
    private TimeZone timeZone;

    public AcudGatewayInbound(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Override
    protected AcudGatewayDataPushNotificationParser getEventPushNotificationParser() {
        if (parser == null) {
            parser = new AcudGatewayDataPushNotificationParser(timeZone, comChannel, getContext());
        }
        return parser;
    }

    @Override
    public void setUPLProperties(TypedProperties properties) {
        super.setUPLProperties(properties);
        this.timeZone = properties.getTypedProperty(TIMEZONE, TimeZone.getTimeZone("GMT"));
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<com.energyict.mdc.upl.properties.PropertySpec> propertySpecs = new ArrayList<>(super.getUPLPropertySpecs());
        propertySpecs.add(
                this.propertySpecService.timeZoneSpec().named(TIMEZONE, PropertyTranslationKeys.V2_ELSTER_TIMEZONE).describedAs(PropertyTranslationKeys.V2_ELSTER_TIMEZONE_DESCRIPTION).finish());
        return propertySpecs;
    }

    @Override
    public DeviceIdentifier getDeviceIdentifier() {
            return getEventPushNotificationParser().getDeviceIdentifier();
    }

    public AcudDlmsProperties getDlmsProperties() {
        if (dlmsProperties == null) {
            dlmsProperties = new AcudDlmsProperties();
        }
        return (AcudDlmsProperties) dlmsProperties;
    }

    protected AcudConfigurationSupport getDlmsConfigurationSupport() {
        if (dlmsConfigurationSupport == null) {
            dlmsConfigurationSupport = new AcudConfigurationSupport(getPropertySpecService());
        }
        return dlmsConfigurationSupport;
    }

    public PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    @Override
    public String getVersion() {
        return "2022-09-07";
    }


}
