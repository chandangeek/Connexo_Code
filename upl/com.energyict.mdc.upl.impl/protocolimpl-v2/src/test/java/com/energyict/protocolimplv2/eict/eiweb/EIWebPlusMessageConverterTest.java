package com.energyict.protocolimplv2.eict.eiweb;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.messages.DeviceMessage;
import com.energyict.mdc.messages.DeviceMessageAttributeImpl;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdw.core.DataVaultProvider;
import com.energyict.mdw.core.RandomProvider;
import com.energyict.mdw.crypto.KeyStoreDataVaultProvider;
import com.energyict.mdw.crypto.SecureRandomProvider;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.mdw.offline.OfflineDeviceMessageAttribute;
import com.energyict.mdw.offlineimpl.OfflineDeviceMessageAttributeImpl;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocolimplv2.eict.rtuplusserver.eiwebplus.RtuServer;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.OutputConfigurationMessage;
import com.energyict.protocolimplv2.messages.PLCConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.EIWebPlusMessageConverter;
import com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    private static final int DEVICE_MESSAGE_ID = 1;

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

    private ExtendedEIWebPlusMessageConverter converter;

    @Before
    public void mockMessages() {
        mockProviders();
        absoluteDOSwitchRuleMessage = createMessage(OutputConfigurationMessage.AbsoluteDOSwitchRule);
        rtuPlusServerEnterMaintenanceModeMessage = createMessage(DeviceActionMessage.RtuPlusServerEnterMaintenanceMode);
        forceMessageToFailedMessage = createMessage(DeviceActionMessage.ForceMessageToFailed);
        ftionUpgradeWithNewEIServerURLMessage = createMessage(DeviceActionMessage.FTIONUpgradeWithNewEIServerURL);
        upgradeBootloaderMessage = createMessage(FirmwareDeviceMessage.UpgradeBootloader);
        idisDiscoveryConfigurationMessage = createMessage(PLCConfigurationDeviceMessage.IDISDiscoveryConfiguration);
        idisRepeaterCallConfigurationMessage = createMessage(PLCConfigurationDeviceMessage.IDISRepeaterCallConfiguration);
        IDISPhyConfigurationMessage = createMessage(PLCConfigurationDeviceMessage.IDISPhyConfiguration);
        IDISWhitelistConfiguration = createMessage(PLCConfigurationDeviceMessage.IDISWhitelistConfiguration);
        IDISRunAlarmDiscoveryCallNow = createMessage(PLCConfigurationDeviceMessage.IDISRunAlarmDiscoveryCallNow);
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
            converter = new ExtendedEIWebPlusMessageConverter();
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
        when(deviceMessage.getId()).thenReturn(DEVICE_MESSAGE_ID);

        for (PropertySpec propertySpec : messageSpec.getPropertySpecs()) {
            TypedProperties propertyStorage = TypedProperties.empty();
            propertyStorage.setProperty(propertySpec.getName(), "1");
            attributes.add(new OfflineDeviceMessageAttributeImpl(new DeviceMessageAttributeImpl(propertySpec, deviceMessage, propertyStorage), new RtuServer()));
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

        @Override
        public Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
            return super.getRegistry();
        }
    }
}