/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.LegacyMessageConverter;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.eict.rtuplusserver.eiwebplus.RtuServer;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.OutputConfigurationMessage;
import com.energyict.protocolimplv2.messages.PLCConfigurationDeviceMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class EIWebPlusMessageConverterTest extends AbstractV2MessageConverterTest {

    @Mock
    private OfflineDeviceMessage absoluteDOSwitchRuleMessage;
    @Mock
    private OfflineDeviceMessage rtuPlusServerEnterMaintenanceModeMessage;
    @Mock
    private OfflineDeviceMessage forceMessageToFailedMessage;
    @Mock
    private OfflineDeviceMessage ftionUpgradeWithNewEIServerURLMessage;
    @Mock
    private OfflineDeviceMessage upgradeBootloaderMessage;
    @Mock
    private OfflineDeviceMessage idisDiscoveryConfigurationMessage;
    @Mock
    private OfflineDeviceMessage idisRepeaterCallConfigurationMessage;
    @Mock
    private OfflineDeviceMessage IDISPhyConfigurationMessage;
    @Mock
    private OfflineDeviceMessage IDISWhitelistConfiguration;
    @Mock
    private OfflineDeviceMessage IDISRunAlarmDiscoveryCallNow;
    @Mock
    private DeviceMessageFileExtractor deviceMessageFileConverter;
    @Mock
    private CollectedDataFactory collectedDataFactory;
    @Mock
    private DeviceMessageFileExtractor messageFileExtractor;

    private ExtendedEIWebPlusMessageConverter converter;

    @Before
    public void mockMessages() {
        absoluteDOSwitchRuleMessage = createMessage(OutputConfigurationMessage.AbsoluteDOSwitchRule.get(propertySpecService, this.nlsService, super.converter));
        rtuPlusServerEnterMaintenanceModeMessage = createMessage(DeviceActionMessage.RtuPlusServerEnterMaintenanceMode.get(propertySpecService, this.nlsService, super.converter));
        forceMessageToFailedMessage = createMessage(DeviceActionMessage.ForceMessageToFailed.get(propertySpecService, this.nlsService, super.converter));
        ftionUpgradeWithNewEIServerURLMessage = createMessage(DeviceActionMessage.FTIONUpgradeWithNewEIServerURL.get(propertySpecService, this.nlsService, super.converter));
        upgradeBootloaderMessage = createMessage(FirmwareDeviceMessage.UpgradeBootloader.get(propertySpecService, this.nlsService, super.converter));
        idisDiscoveryConfigurationMessage = createMessage(PLCConfigurationDeviceMessage.IDISDiscoveryConfiguration.get(propertySpecService, this.nlsService, super.converter));
        idisRepeaterCallConfigurationMessage = createMessage(PLCConfigurationDeviceMessage.IDISRepeaterCallConfiguration.get(propertySpecService, this.nlsService, super.converter));
        IDISPhyConfigurationMessage = createMessage(PLCConfigurationDeviceMessage.IDISPhyConfiguration.get(propertySpecService, this.nlsService, super.converter));
        IDISWhitelistConfiguration = createMessage(PLCConfigurationDeviceMessage.IDISWhitelistConfiguration.get(propertySpecService, this.nlsService, super.converter));
        IDISRunAlarmDiscoveryCallNow = createMessage(PLCConfigurationDeviceMessage.IDISRunAlarmDiscoveryCallNow.get(propertySpecService, this.nlsService, super.converter));
    }

    @Test
    public void testMessageConversion() {

        MessageEntry messageEntry;

        messageEntry = getMessageEntryCreator(absoluteDOSwitchRuleMessage).createMessageEntry(null, absoluteDOSwitchRuleMessage);
        assertEquals("<DOSwitchRule id=\"1\" startTime=\"1\" endTime=\"1\" outputBitMap=\"1\"> </DOSwitchRule>", messageEntry.getContent());

        messageEntry = getMessageEntryCreator(rtuPlusServerEnterMaintenanceModeMessage).createMessageEntry(null, rtuPlusServerEnterMaintenanceModeMessage);
        assertEquals("<RtuPlusServerEnterMaintenanceMode> </RtuPlusServerEnterMaintenanceMode>", messageEntry.getContent());

        messageEntry = getMessageEntryCreator(forceMessageToFailedMessage).createMessageEntry(null, forceMessageToFailedMessage);
        assertEquals("<ForceMessageToFailed><RtuID>1</RtuID><TrackingID>1</TrackingID> </ForceMessageToFailed>", messageEntry.getContent());

        messageEntry = getMessageEntryCreator(ftionUpgradeWithNewEIServerURLMessage).createMessageEntry(null, ftionUpgradeWithNewEIServerURLMessage);
        assertEquals("<FTIONUpgradeWithNewEIServerURL>1</FTIONUpgradeWithNewEIServerURL>", messageEntry.getContent());

        messageEntry = getMessageEntryCreator(upgradeBootloaderMessage).createMessageEntry(null, upgradeBootloaderMessage);
        assertEquals("<UpgradeBootloader>1</UpgradeBootloader>", messageEntry.getContent());

        messageEntry = getMessageEntryCreator(idisDiscoveryConfigurationMessage).createMessageEntry(null, idisDiscoveryConfigurationMessage);
        assertEquals("<IDISDiscoveryConfiguration><Interval between discoveries (in hours)>1</Interval between discoveries (in hours)><Duration of the discovery (in minutes)>1</Duration of the discovery (in minutes)></IDISDiscoveryConfiguration>", messageEntry.getContent());

        messageEntry = getMessageEntryCreator(idisRepeaterCallConfigurationMessage).createMessageEntry(null, idisRepeaterCallConfigurationMessage);
        assertEquals("<IDISRepeaterCallConfiguration><Interval (in minutes)>1</Interval (in minutes)><Reception threshold (dBV)>1</Reception threshold (dBV)><Number of timeslots for NEW systems>1</Number of timeslots for NEW systems></IDISRepeaterCallConfiguration>", messageEntry.getContent());

        messageEntry = getMessageEntryCreator(IDISPhyConfigurationMessage).createMessageEntry(null, IDISPhyConfigurationMessage);
        assertEquals("<IDISPhyConfiguration><Bit sync>1</Bit sync><Zero cross adjust>1</Zero cross adjust><TX gain>1</TX gain><RX gain>1</RX gain></IDISPhyConfiguration>", messageEntry.getContent());

        messageEntry = getMessageEntryCreator(IDISWhitelistConfiguration).createMessageEntry(null, IDISWhitelistConfiguration);
        assertEquals("<IDISWhitelistConfiguration><Enabled (true/false)>1</Enabled (true/false)><Group Name (the group containing the meters included in the whitelist)>1</Group Name (the group containing the meters included in the whitelist)></IDISWhitelistConfiguration>", messageEntry.getContent());

        messageEntry = getMessageEntryCreator(IDISRunAlarmDiscoveryCallNow).createMessageEntry(null, IDISRunAlarmDiscoveryCallNow);
        assertEquals("<IDISRunAlarmDiscoveryCallNow> </IDISRunAlarmDiscoveryCallNow>", messageEntry.getContent());

    }

    private MessageEntryCreator getMessageEntryCreator(OfflineDeviceMessage deviceMessage) {
        return getConverter().getRegistry().get(deviceMessage.getSpecification());
    }

    private ExtendedEIWebPlusMessageConverter getConverter() {
        if (converter == null) {
            converter = new ExtendedEIWebPlusMessageConverter(propertySpecService, this.nlsService, super.converter, this.deviceMessageFileConverter);
        }
        return converter;
    }

    protected Messaging getMessagingProtocol() {
        return null;
    }

    @Override
    LegacyMessageConverter doGetMessageConverter() {
        return new LegacyMessageConverter() {
            @Override
            public List<DeviceMessageSpec> getSupportedMessages() {
                List<DeviceMessageSpec> result = new ArrayList<>();
                result.addAll(getConverter().getRegistry().keySet());
                return result;
            }

            @Override
            public String format(PropertySpec propertySpec, Object messageAttribute) {
                return new RtuServer(collectedDataFactory, propertySpecService, nlsService, EIWebPlusMessageConverterTest.super.converter, messageFileExtractor, keyAccessorTypeExtractor).format(null, null, propertySpec, messageAttribute);
            }

            @Override
            public MessageEntry toMessageEntry(OfflineDeviceMessage offlineDeviceMessage) {
                return null;
            }

            @Override
            public void setMessagingProtocol(Messaging messagingProtocol) {
                // No implementation required
            }
        };
    }

    @Override
    protected Object getPropertySpecValue(PropertySpec propertySpec) {
        return "1";
    }

    /**
     * Make the registry of this converter public, only for test usage
     */
    public class ExtendedEIWebPlusMessageConverter extends EIWebPlusMessageConverter {
        public ExtendedEIWebPlusMessageConverter(PropertySpecService propertySpecService, NlsService nlsService, Converter converter, DeviceMessageFileExtractor deviceMessageFileExtractor) {
            super(propertySpecService, nlsService, converter, deviceMessageFileExtractor, keyAccessorTypeExtractor);
        }

        @Override
        public Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
            return super.getRegistry();
        }
    }
}