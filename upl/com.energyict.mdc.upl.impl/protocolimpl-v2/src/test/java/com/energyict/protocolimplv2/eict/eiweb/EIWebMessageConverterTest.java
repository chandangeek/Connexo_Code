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
import com.energyict.protocolimplv2.messages.*;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.eiweb.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test that creates OfflineDeviceMessages (the attributes are all filled with "1" values) and converts them to the legacy XML message, using the EIWebMessageConverter.
 * <p/>
 * Copyrights EnergyICT
 * Date: 2/10/13
 * Time: 13:11
 * Author: khe
 */
@RunWith(MockitoJUnitRunner.class)
public class EIWebMessageConverterTest {

    private static final int DEVICE_MESSAGE_ID = 1;

    @Mock
    private OfflineDeviceMessage setDescriptionMessage;
    @Mock
    private OfflineDeviceMessage setIntervalInSeconds;
    @Mock
    private OfflineDeviceMessage setProxyServer;
    @Mock
    private OfflineDeviceMessage setIPAddress;
    @Mock
    private OfflineDeviceMessage setDST;
    @Mock
    private OfflineDeviceMessage setRefreshClockEvery;
    @Mock
    private OfflineDeviceMessage setEIWebPassword;
    @Mock
    private OfflineDeviceMessage setEIWebCurrentInterval;
    @Mock
    private OfflineDeviceMessage setPOPUsername;
    @Mock
    private OfflineDeviceMessage setSMTPServer;
    @Mock
    private OfflineDeviceMessage setSmsDataNbr;
    @Mock
    private OfflineDeviceMessage setSmsCorrection;
    @Mock
    private OfflineDeviceMessage setDLMSDeviceID;
    @Mock
    private OfflineDeviceMessage setDLMSPassword;
    @Mock
    private OfflineDeviceMessage setDukePowerID;
    @Mock
    private OfflineDeviceMessage setDukePowerIdleTime;
    @Mock
    private OfflineDeviceMessage setDialCommand;
    @Mock
    private OfflineDeviceMessage setPPPBaudRate;
    @Mock
    private OfflineDeviceMessage setISP1Phone;
    @Mock
    private OfflineDeviceMessage setPPPIdleTimeout;
    @Mock
    private OfflineDeviceMessage setFunction;
    @Mock
    private OfflineDeviceMessage setName;
    @Mock
    private OfflineDeviceMessage setSumMask;
    @Mock
    private OfflineDeviceMessage setSubstractMask;
    @Mock
    private OfflineDeviceMessage setActiveChannel;
    @Mock
    private OfflineDeviceMessage setDifferenceAnalogOut;
    @Mock
    private OfflineDeviceMessage setSetpoint;
    @Mock
    private OfflineDeviceMessage setSwitchTime;
    @Mock
    private OfflineDeviceMessage setLoad;
    @Mock
    private OfflineDeviceMessage setInputChannel;
    @Mock
    private OfflineDeviceMessage setOutputChannel;
    @Mock
    private OfflineDeviceMessage setOpusOSNbr;
    @Mock
    private OfflineDeviceMessage setOpusTimeout;
    @Mock
    private OfflineDeviceMessage setMmEvery;
    @Mock
    private OfflineDeviceMessage setMmOverflow;
    @Mock
    private OfflineDeviceMessage setMBusEvery;
    @Mock
    private OfflineDeviceMessage setMBusInterFrameTime;
    @Mock
    private OfflineDeviceMessage setFTIONReboot;
    @Mock
    private OfflineDeviceMessage setFTIONClearMem;
    @Mock
    private OfflineDeviceMessage setChangeAdminPassword;
    @Mock
    private OfflineDeviceMessage setOutputOn;
    @Mock
    private OfflineDeviceMessage setOutputToggle;
    @Mock
    private OfflineDeviceMessage setAnalogOut;
    @Mock
    private OfflineDeviceMessage eiWebClrOption;
    @Mock
    private OfflineDeviceMessage pop3SetOption;
    @Mock
    private OfflineDeviceMessage xmlMessage;

    @Before
    public void mockMessages() {
        mockProviders();
        setDescriptionMessage = createMessage(ConfigurationChangeDeviceMessage.SetDescription);
        setIntervalInSeconds = createMessage(ConfigurationChangeDeviceMessage.SetIntervalInSeconds);
        setProxyServer = createMessage(NetworkConnectivityMessage.SetProxyServer);
        setIPAddress = createMessage(NetworkConnectivityMessage.SetIPAddress);
        setDST = createMessage(ClockDeviceMessage.SetDST);
        setRefreshClockEvery = createMessage(ClockDeviceMessage.SetRefreshClockEvery);
        setEIWebPassword = createMessage(EIWebConfigurationDeviceMessage.SetEIWebPassword);
        setEIWebCurrentInterval = createMessage(EIWebConfigurationDeviceMessage.SetEIWebCurrentInterval);
        setPOPUsername = createMessage(MailConfigurationDeviceMessage.SetPOPUsername);
        setSMTPServer = createMessage(MailConfigurationDeviceMessage.SetSMTPServer);
        setSmsDataNbr = createMessage(SMSConfigurationDeviceMessage.SetSmsDataNbr);
        setSmsCorrection = createMessage(SMSConfigurationDeviceMessage.SetSmsCorrection);
        setDLMSDeviceID = createMessage(DLMSConfigurationDeviceMessage.SetDLMSDeviceID);
        setDLMSPassword = createMessage(DLMSConfigurationDeviceMessage.SetDLMSPassword);
        setDukePowerID = createMessage(ConfigurationChangeDeviceMessage.SetDukePowerID);
        setDukePowerIdleTime = createMessage(ConfigurationChangeDeviceMessage.SetDukePowerIdleTime);
        setDialCommand = createMessage(ModemConfigurationDeviceMessage.SetDialCommand);
        setPPPBaudRate = createMessage(ModemConfigurationDeviceMessage.SetPPPBaudRate);
        setISP1Phone = createMessage(PPPConfigurationDeviceMessage.SetISP1Phone);
        setPPPIdleTimeout = createMessage(PPPConfigurationDeviceMessage.SetPPPIdleTimeout);
        setFunction = createMessage(ChannelConfigurationDeviceMessage.SetFunction);
        setName = createMessage(ChannelConfigurationDeviceMessage.SetName);
        setSumMask = createMessage(TotalizersConfigurationDeviceMessage.SetSumMask);
        setSubstractMask = createMessage(TotalizersConfigurationDeviceMessage.SetSubstractMask);
        setActiveChannel = createMessage(PeakShaverConfigurationDeviceMessage.SetActiveChannel);
        setDifferenceAnalogOut = createMessage(PeakShaverConfigurationDeviceMessage.SetDifferenceAnalogOut);
        setSetpoint = createMessage(PeakShaverConfigurationDeviceMessage.SetSetpoint);
        setSwitchTime = createMessage(PeakShaverConfigurationDeviceMessage.SetSwitchTime);
        setLoad = createMessage(PeakShaverConfigurationDeviceMessage.SetLoad);
        setInputChannel = createMessage(LogBookDeviceMessage.SetInputChannel);
        setOutputChannel = createMessage(LogBookDeviceMessage.SetOutputChannel);
        setOpusOSNbr = createMessage(OpusConfigurationDeviceMessage.SetOpusOSNbr);
        setOpusTimeout = createMessage(OpusConfigurationDeviceMessage.SetOpusTimeout);
        setMmEvery = createMessage(ModbusConfigurationDeviceMessage.SetMmEvery);
        setMmOverflow = createMessage(ModbusConfigurationDeviceMessage.SetMmOverflow);
        setMBusEvery = createMessage(MBusConfigurationDeviceMessage.SetMBusEvery);
        setMBusInterFrameTime = createMessage(MBusConfigurationDeviceMessage.SetMBusInterFrameTime);
        setFTIONReboot = createMessage(DeviceActionMessage.SetFTIONReboot);
        setFTIONClearMem = createMessage(DeviceActionMessage.SetFTIONClearMem);
        setChangeAdminPassword = createMessage(DeviceActionMessage.SetChangeAdminPassword);
        setOutputOn = createMessage(OutputConfigurationMessage.SetOutputOn);
        setOutputToggle = createMessage(OutputConfigurationMessage.SetOutputToggle);
        setAnalogOut = createMessage(DeviceActionMessage.SetAnalogOut);
        eiWebClrOption = createMessage(EIWebConfigurationDeviceMessage.EIWebClrOption);
        pop3SetOption = createMessage(MailConfigurationDeviceMessage.POP3SetOption);
        xmlMessage = createMessage(GeneralDeviceMessage.SEND_XML_MESSAGE);
    }

    @Test
    public void testMessageConversion() {

        MessageEntry messageEntry;

        messageEntry = new SimpleEIWebMessageEntry().createMessageEntry(null, setDescriptionMessage);
        assertEquals("<Description>1</Description>", messageEntry.getContent());

        messageEntry = new SimpleEIWebMessageEntry().createMessageEntry(null, setIntervalInSeconds);
        assertEquals("<IntervalInSeconds>1</IntervalInSeconds>", messageEntry.getContent());

        messageEntry = new SimpleEIWebMessageEntry().createMessageEntry(null, setProxyServer);
        assertEquals("<ProxyServer>1</ProxyServer>", messageEntry.getContent());

        messageEntry = new SimpleEIWebMessageEntry().createMessageEntry(null, setIPAddress);
        assertEquals("<IPAddress>1</IPAddress>", messageEntry.getContent());

        messageEntry = new SimpleEIWebMessageEntry().createMessageEntry(null, setDST);
        assertEquals("<DST>1</DST>", messageEntry.getContent());

        messageEntry = new SimpleEIWebMessageEntry().createMessageEntry(null, setRefreshClockEvery);
        assertEquals("<RefreshClockEvery>1</RefreshClockEvery>", messageEntry.getContent());

        messageEntry = new EIWebConfigurationMessageEntry().createMessageEntry(null, setEIWebPassword);
        assertEquals("<EIWeb id=\"1\"><EIWebPassword>1</EIWebPassword></EIWeb>", messageEntry.getContent());

        messageEntry = new EIWebConfigurationMessageEntry().createMessageEntry(null, setEIWebCurrentInterval);
        assertEquals("<EIWeb id=\"1\"><EIWebCurrentInterval>1</EIWebCurrentInterval></EIWeb>", messageEntry.getContent());

        messageEntry = new SimpleEIWebMessageEntry().createMessageEntry(null, setPOPUsername);
        assertEquals("<POPUsername>1</POPUsername>", messageEntry.getContent());

        messageEntry = new SimpleEIWebMessageEntry().createMessageEntry(null, setSMTPServer);
        assertEquals("<SMTPServer>1</SMTPServer>", messageEntry.getContent());

        messageEntry = new SimpleEIWebMessageEntry().createMessageEntry(null, setSmsDataNbr);
        assertEquals("<SmsDataNbr>1</SmsDataNbr>", messageEntry.getContent());

        messageEntry = new SimpleEIWebMessageEntry().createMessageEntry(null, setSmsCorrection);
        assertEquals("<SmsCorrection>1</SmsCorrection>", messageEntry.getContent());

        messageEntry = new SimpleEIWebMessageEntry().createMessageEntry(null, setDLMSDeviceID);
        assertEquals("<DLMSDeviceID>1</DLMSDeviceID>", messageEntry.getContent());

        messageEntry = new SimpleEIWebMessageEntry().createMessageEntry(null, setDLMSPassword);
        assertEquals("<DLMSPassword>1</DLMSPassword>", messageEntry.getContent());

        messageEntry = new SimpleEIWebMessageEntry().createMessageEntry(null, setDukePowerID);
        assertEquals("<DukePowerID>1</DukePowerID>", messageEntry.getContent());

        messageEntry = new SimpleEIWebMessageEntry().createMessageEntry(null, setDukePowerIdleTime);
        assertEquals("<DukePowerIdleTime>1</DukePowerIdleTime>", messageEntry.getContent());

        messageEntry = new SimpleEIWebMessageEntry().createMessageEntry(null, setDialCommand);
        assertEquals("<DialCommand>1</DialCommand>", messageEntry.getContent());

        messageEntry = new SimpleEIWebMessageEntry().createMessageEntry(null, setPPPBaudRate);
        assertEquals("<PPPBaudRate>1</PPPBaudRate>", messageEntry.getContent());

        messageEntry = new SimpleEIWebMessageEntry().createMessageEntry(null, setISP1Phone);
        assertEquals("<ISP1Phone>1</ISP1Phone>", messageEntry.getContent());

        messageEntry = new SimpleEIWebMessageEntry().createMessageEntry(null, setPPPIdleTimeout);
        assertEquals("<PPPIdleTimeout>1</PPPIdleTimeout>", messageEntry.getContent());

        messageEntry = new ChannelMessageEntry().createMessageEntry(null, setFunction);
        assertEquals("<Channel id=\"1\"><Function>1</Function></Channel>", messageEntry.getContent());

        messageEntry = new ChannelMessageEntry().createMessageEntry(null, setName);
        assertEquals("<Channel id=\"1\"><Name>1</Name></Channel>", messageEntry.getContent());

        messageEntry = new TotalizerEIWebMessageEntry().createMessageEntry(null, setSumMask);
        assertEquals("<Totaliser id=\"1\"><SumMask>1</SumMask></Totaliser>", messageEntry.getContent());

        messageEntry = new TotalizerEIWebMessageEntry().createMessageEntry(null, setSubstractMask);
        assertEquals("<Totaliser id=\"1\"><SubstractMask>1</SubstractMask></Totaliser>", messageEntry.getContent());

        messageEntry = new SimplePeakShaverMessageEntry().createMessageEntry(null, setActiveChannel);
        assertEquals("<Peakshaver id=\"1\"><ActiveChannel>1</ActiveChannel></Peakshaver>", messageEntry.getContent());

        messageEntry = new SimplePeakShaverMessageEntry().createMessageEntry(null, setDifferenceAnalogOut);
        assertEquals("<Peakshaver id=\"1\"><DifferenceAnalogOut>1</DifferenceAnalogOut></Peakshaver>", messageEntry.getContent());

        messageEntry = new SetSetpointMessageEntry().createMessageEntry(null, setSetpoint);
        assertEquals("<Peakshaver id=\"1\"><Setpoint tariff=\"1\"><CurrentValue>1</CurrentValue><NewValue>1</NewValue></Setpoint></Peakshaver>", messageEntry.getContent());

        messageEntry = new SetSwitchTimeMessageEntry().createMessageEntry(null, setSwitchTime);
        assertEquals("<Peakshaver id=\"1\"><SwitchTime><Day>1</Day><Month>1</Month><Year>1</Year><Hour>1</Hour><Minute>1</Minute><Second>1</Second></SwitchTime></Peakshaver>", messageEntry.getContent());

        messageEntry = new SetLoadMessageEntry().createMessageEntry(null, setLoad);
        assertEquals("<Peakshaver id=\"1\"><Load id=\"1\"><MaxOff>1</MaxOff><Delay>1</Delay><Manual>1</Manual><Status>1</Status><IPAddress>1</IPAddress><ChnNbr>1</ChnNbr></Load></Peakshaver>", messageEntry.getContent());

        messageEntry = new SimpleEIWebMessageEntry().createMessageEntry(null, setInputChannel);
        assertEquals("<InputChannel>1</InputChannel>", messageEntry.getContent());

        messageEntry = new SimpleEIWebMessageEntry().createMessageEntry(null, setOutputChannel);
        assertEquals("<OutputChannel>1</OutputChannel>", messageEntry.getContent());

        messageEntry = new SimpleEIWebMessageEntry().createMessageEntry(null, setOpusOSNbr);
        assertEquals("<OpusOSNbr>1</OpusOSNbr>", messageEntry.getContent());

        messageEntry = new SimpleEIWebMessageEntry().createMessageEntry(null, setOpusTimeout);
        assertEquals("<OpusTimeout>1</OpusTimeout>", messageEntry.getContent());

        messageEntry = new SimpleEIWebMessageEntry().createMessageEntry(null, setMmEvery);
        assertEquals("<MmEvery>1</MmEvery>", messageEntry.getContent());

        messageEntry = new SimpleEIWebMessageEntry().createMessageEntry(null, setMmOverflow);
        assertEquals("<MmOverflow>1</MmOverflow>", messageEntry.getContent());

        messageEntry = new SimpleEIWebMessageEntry().createMessageEntry(null, setMBusEvery);
        assertEquals("<MBusEvery>1</MBusEvery>", messageEntry.getContent());

        messageEntry = new SimpleEIWebMessageEntry().createMessageEntry(null, setMBusInterFrameTime);
        assertEquals("<MBusInterFrameTime>1</MBusInterFrameTime>", messageEntry.getContent());

        messageEntry = new SimpleEIWebMessageEntry().createMessageEntry(null, setFTIONReboot);
        assertEquals("<FTIONReboot>1</FTIONReboot>", messageEntry.getContent());

        messageEntry = new SimpleEIWebMessageEntry().createMessageEntry(null, setFTIONClearMem);
        assertEquals("<FTIONClearMem>1</FTIONClearMem>", messageEntry.getContent());

        messageEntry = new ChangeAdminPasswordMessageEntry().createMessageEntry(null, setChangeAdminPassword);
        assertEquals("<AdminOld>1</AdminOld><AdminNew>1</AdminNew>", messageEntry.getContent());

        messageEntry = new SimpleEIWebMessageEntry().createMessageEntry(null, setOutputOn);
        assertEquals("<OutputOn>1</OutputOn>", messageEntry.getContent());

        messageEntry = new SimpleEIWebMessageEntry().createMessageEntry(null, setOutputToggle);
        assertEquals("<OutputToggle>1</OutputToggle>", messageEntry.getContent());

        messageEntry = new AnalogOutMessageEntry().createMessageEntry(null, setAnalogOut);
        assertEquals("<AnalOut id=\"1\"><value>1</value></AnalOut>", messageEntry.getContent());

        messageEntry = new SimpleEIWebMessageEntry().createMessageEntry(null, eiWebClrOption);
        assertEquals("<EIWebClrOption>1</EIWebClrOption>", messageEntry.getContent());

        messageEntry = new SimpleEIWebMessageEntry().createMessageEntry(null, pop3SetOption);
        assertEquals("<POP3SetOption>1</POP3SetOption>", messageEntry.getContent());

        messageEntry = new XMLAttributeDeviceMessageEntry().createMessageEntry(null, xmlMessage);
        assertEquals("<tag>value</tag>", messageEntry.getContent());
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
            if (messageSpec == GeneralDeviceMessage.SEND_XML_MESSAGE) {
                propertyStorage.setProperty(propertySpec.getName(), "<tag>value</tag>");
            } else {
                propertyStorage.setProperty(propertySpec.getName(), "1");
            }
            attributes.add(new OfflineDeviceMessageAttributeImpl(new DeviceMessageAttributeImpl(propertySpec, deviceMessage, propertyStorage), new EIWeb()));
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
}