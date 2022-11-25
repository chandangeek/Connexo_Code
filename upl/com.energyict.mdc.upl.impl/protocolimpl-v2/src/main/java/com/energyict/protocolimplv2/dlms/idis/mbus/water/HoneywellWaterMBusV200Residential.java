package com.energyict.protocolimplv2.dlms.idis.mbus.water;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.KeyAccessorTypeExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.mdc.upl.tasks.support.DeviceMessageSupport;

import com.energyict.protocolcommon.exceptions.CodingException;
import com.energyict.protocolimplv2.common.AbstractMbusDevice;
import com.energyict.protocolimplv2.dlms.idis.as3000g.AS3000G;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HoneywellWaterMBusV200Residential extends AbstractMbusDevice {

    private final PropertySpecService propertySpecService;
    private final NlsService nlsService;
    private final Converter converter;

    public HoneywellWaterMBusV200Residential(PropertySpecService propertySpecService, NlsService nlsService, Converter converter, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, TariffCalendarExtractor calendarExtractor, DeviceMessageFileExtractor messageFileExtractor, KeyAccessorTypeExtractor keyAccessorTypeExtractor) {
        super(new AS3000G(propertySpecService, nlsService, converter, collectedDataFactory, issueFactory, calendarExtractor, messageFileExtractor, keyAccessorTypeExtractor) );
        this.propertySpecService = propertySpecService;
        this.nlsService = nlsService;
        this.converter = converter;
    }

    public PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    @Override
    public NlsService getNlsService() {
        return nlsService;
    }

    public Converter getConverter() {
        return converter;
    }

    @Override
    public String getProtocolDescription() {
        return "Honeywell water Mbus V200 residential";
    }

    @Override
    public String getVersion() {
        return "$Date: 2022-10-28 $";
    }

    //#### methods override ####
    @Override
    public DeviceMessageSupport getDeviceMessageSupport() {
        throw CodingException.unsupportedMethod(this.getClass(), "getSupportedMessages");
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return new ArrayList<>();
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        throw CodingException.unsupportedMethod(this.getClass(), "executePendingMessages");
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        throw CodingException.unsupportedMethod(this.getClass(), "updateSentMessages");
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, com.energyict.mdc.upl.properties.PropertySpec propertySpec, Object messageAttribute) {
        return messageAttribute.toString();
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Collections.emptyList();
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
    }
}