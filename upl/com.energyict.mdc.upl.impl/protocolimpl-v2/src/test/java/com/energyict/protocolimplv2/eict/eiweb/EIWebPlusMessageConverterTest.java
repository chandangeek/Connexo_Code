package com.energyict.protocolimplv2.eict.eiweb;

import com.energyict.mdc.messages.DeviceMessageAttributeImpl;
import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.messages.OfflineDeviceMessageAttribute;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.mdw.core.DataVaultProvider;
import com.energyict.mdw.core.RandomProvider;
import com.energyict.mdw.crypto.KeyStoreDataVaultProvider;
import com.energyict.mdw.crypto.SecureRandomProvider;
import com.energyict.mdw.offlineimpl.OfflineDeviceMessageAttributeImpl;
import com.energyict.protocolimpl.properties.TypedProperties;
import com.energyict.protocolimplv2.eict.rtuplusserver.eiwebplus.RtuServer;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.OutputConfigurationMessage;
import com.energyict.protocolimplv2.messages.PLCConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.EIWebPlusMessageConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test that creates OfflineDeviceMessages (the attributes are all filled with "1" values) and converts them to the legacy XML message, using the EIWebPlusMessageConverter.
 * <p/>
 * Copyrights EnergyICT
 * Date: 2/10/13
 * Time: 13:11
 * Author: khe
 */
@RunWith(MockitoJUnitRunner.class)
public class EIWebPlusMessageConverterTest {

    private static final long DEVICE_MESSAGE_ID = 1L;

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
    private PropertySpecService propertySpecService;
    @Mock
    private NlsService nlsService;
    @Mock
    private Converter uplConverter;
    @Mock
    private DeviceMessageFileExtractor deviceMessageFileConverter;
    @Mock
    private CollectedDataFactory collectedDataFactory;

    private ExtendedEIWebPlusMessageConverter converter;

    @Before
    public void mockMessages() {
        mockProviders();
        absoluteDOSwitchRuleMessage = createMessage(OutputConfigurationMessage.AbsoluteDOSwitchRule.get(this.propertySpecService, this.nlsService, this.uplConverter));
        rtuPlusServerEnterMaintenanceModeMessage = createMessage(DeviceActionMessage.RtuPlusServerEnterMaintenanceMode.get(this.propertySpecService, this.nlsService, this.uplConverter));
        forceMessageToFailedMessage = createMessage(DeviceActionMessage.ForceMessageToFailed.get(this.propertySpecService, this.nlsService, this.uplConverter));
        ftionUpgradeWithNewEIServerURLMessage = createMessage(DeviceActionMessage.FTIONUpgradeWithNewEIServerURL.get(this.propertySpecService, this.nlsService, this.uplConverter));
        upgradeBootloaderMessage = createMessage(FirmwareDeviceMessage.UpgradeBootloader.get(this.propertySpecService, this.nlsService, this.uplConverter));
        idisDiscoveryConfigurationMessage = createMessage(PLCConfigurationDeviceMessage.IDISDiscoveryConfiguration.get(this.propertySpecService, this.nlsService, this.uplConverter));
        idisRepeaterCallConfigurationMessage = createMessage(PLCConfigurationDeviceMessage.IDISRepeaterCallConfiguration.get(this.propertySpecService, this.nlsService, this.uplConverter));
        IDISPhyConfigurationMessage = createMessage(PLCConfigurationDeviceMessage.IDISPhyConfiguration.get(this.propertySpecService, this.nlsService, this.uplConverter));
        IDISWhitelistConfiguration = createMessage(PLCConfigurationDeviceMessage.IDISWhitelistConfiguration.get(this.propertySpecService, this.nlsService, this.uplConverter));
        IDISRunAlarmDiscoveryCallNow = createMessage(PLCConfigurationDeviceMessage.IDISRunAlarmDiscoveryCallNow.get(this.propertySpecService, this.nlsService, this.uplConverter));
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

    private ExtendedEIWebPlusMessageConverter getConverter() {
        if (converter == null) {
            converter = new ExtendedEIWebPlusMessageConverter(null, this.propertySpecService, this.nlsService, this.uplConverter, this.deviceMessageFileConverter);
        }
        return converter;
    }

    private MessageEntryCreator getMessageEntryCreator(OfflineDeviceMessage deviceMessage) {
        return getConverter().getRegistry().get(deviceMessage.getSpecification());
    }

    private void mockProviders() {
        DataVaultProvider.instance.set(new KeyStoreDataVaultProvider());
        RandomProvider.instance.set(new SecureRandomProvider());
    }

    /**
     * Create a device message based on the given spec, and fill its attributes with "1" values.
     */
    private OfflineDeviceMessage createMessage(DeviceMessageSpec messageSpec) {
        OfflineDeviceMessage message = getEmptyMessageMock();
        List<OfflineDeviceMessageAttribute> attributes = new ArrayList<>();
        DeviceMessage deviceMessage = mock(DeviceMessage.class);
        when(deviceMessage.getMessageId()).thenReturn(DEVICE_MESSAGE_ID);

        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        for (PropertySpec propertySpec : messageSpec.getPropertySpecs()) {
            TypedProperties propertyStorage = TypedProperties.empty();
            propertyStorage.setProperty(propertySpec.getName(), "1");
            attributes.add(new OfflineDeviceMessageAttributeImpl(offlineDevice, message, new DeviceMessageAttributeImpl(propertySpec, deviceMessage, propertyStorage), new RtuServer(this.collectedDataFactory, propertySpecService, nlsService, converter)));
        }
        when(message.getDeviceMessageAttributes()).thenReturn(attributes);
        when(message.getSpecification()).thenReturn(messageSpec);
        return message;
    }

    private OfflineDeviceMessage getEmptyMessageMock() {
        OfflineDeviceMessage mock = mock(OfflineDeviceMessage.class);
        when(mock.getTrackingId()).thenReturn("");
        return mock;
    }

    /**
     * Make the registry of this converter public, only for test usage
     */
    private class ExtendedEIWebPlusMessageConverter extends EIWebPlusMessageConverter {
        protected ExtendedEIWebPlusMessageConverter(Messaging messagingProtocol, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, DeviceMessageFileExtractor deviceMessageFileExtractor) {
            super(messagingProtocol, propertySpecService, nlsService, converter, deviceMessageFileExtractor);
        }

        @Override
        public Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
            return super.getRegistry();
        }
    }
}