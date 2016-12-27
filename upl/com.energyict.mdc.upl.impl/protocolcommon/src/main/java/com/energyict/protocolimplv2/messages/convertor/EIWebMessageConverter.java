package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.legacy.Extractor;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.messages.ChannelConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.ClockDeviceMessage;
import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;
import com.energyict.protocolimplv2.messages.DLMSConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.EIWebConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.GeneralDeviceMessage;
import com.energyict.protocolimplv2.messages.LogBookDeviceMessage;
import com.energyict.protocolimplv2.messages.MBusConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.MailConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.ModbusConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.ModemConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.NetworkConnectivityMessage;
import com.energyict.protocolimplv2.messages.OpusConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.OutputConfigurationMessage;
import com.energyict.protocolimplv2.messages.PPPConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.PeakShaverConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.SMSConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.TotalizersConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.eiweb.AnalogOutMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.eiweb.ChangeAdminPasswordMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.eiweb.ChannelMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.eiweb.EIWebConfigurationMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.eiweb.SetLoadMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.eiweb.SetSetpointMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.eiweb.SetSwitchTimeMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.eiweb.SimpleEIWebMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.eiweb.SimplePeakShaverMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.eiweb.TotalizerEIWebMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.eiweb.XMLAttributeDeviceMessageEntry;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Represents a MessageConverter that maps the new EIWeb messages to legacy XML
 * <p/>
 * Copyrights EnergyICT
 * Date: 8/03/13
 * Time: 16:26
 */
public class EIWebMessageConverter extends AbstractMessageConverter {

    public EIWebMessageConverter(Messaging messagingProtocol, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, Extractor extractor) {
        super(messagingProtocol, propertySpecService, nlsService, converter, extractor);
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return messageAttribute.toString();
    }

    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return ImmutableMap
                .<DeviceMessageSpec, MessageEntryCreator>builder()
                // General Parameters
                .put(messageSpec(ConfigurationChangeDeviceMessage.SetDescription), new SimpleEIWebMessageEntry())
                .put(messageSpec(ConfigurationChangeDeviceMessage.SetIntervalInSeconds), new SimpleEIWebMessageEntry())
                .put(messageSpec(ConfigurationChangeDeviceMessage.SetUpgradeUrl), new SimpleEIWebMessageEntry())
                .put(messageSpec(ConfigurationChangeDeviceMessage.SetUpgradeOptions), new SimpleEIWebMessageEntry())
                .put(messageSpec(ConfigurationChangeDeviceMessage.SetDebounceTreshold), new SimpleEIWebMessageEntry())
                .put(messageSpec(ConfigurationChangeDeviceMessage.SetTariffMoment), new SimpleEIWebMessageEntry())
                .put(messageSpec(ConfigurationChangeDeviceMessage.SetCommOffset), new SimpleEIWebMessageEntry())
                .put(messageSpec(ConfigurationChangeDeviceMessage.SetAggIntv), new SimpleEIWebMessageEntry())
                .put(messageSpec(ConfigurationChangeDeviceMessage.SetPulseTimeTrue), new SimpleEIWebMessageEntry())
                .put(messageSpec(ConfigurationChangeDeviceMessage.UpgradeSetOption), new SimpleEIWebMessageEntry())
                .put(messageSpec(ConfigurationChangeDeviceMessage.UpgradeClrOption), new SimpleEIWebMessageEntry())

                // Network Parameters
                .put(messageSpec(NetworkConnectivityMessage.SetProxyServer), new SimpleEIWebMessageEntry())
                .put(messageSpec(NetworkConnectivityMessage.SetProxyUsername), new SimpleEIWebMessageEntry())
                .put(messageSpec(NetworkConnectivityMessage.SetProxyPassword), new SimpleEIWebMessageEntry())
                .put(messageSpec(NetworkConnectivityMessage.SetDHCP), new SimpleEIWebMessageEntry())
                .put(messageSpec(NetworkConnectivityMessage.SetDHCPTimeout), new SimpleEIWebMessageEntry())
                .put(messageSpec(NetworkConnectivityMessage.SetIPAddress), new SimpleEIWebMessageEntry())
                .put(messageSpec(NetworkConnectivityMessage.SetSubnetMask), new SimpleEIWebMessageEntry())
                .put(messageSpec(NetworkConnectivityMessage.SetGateway), new SimpleEIWebMessageEntry())
                .put(messageSpec(NetworkConnectivityMessage.SetNameServer), new SimpleEIWebMessageEntry())
                .put(messageSpec(NetworkConnectivityMessage.SetHttpPort), new SimpleEIWebMessageEntry())

                // Time Parameters
                .put(messageSpec(ClockDeviceMessage.SetDST), new SimpleEIWebMessageEntry())
                .put(messageSpec(ClockDeviceMessage.SetTimezone), new SimpleEIWebMessageEntry())
                .put(messageSpec(ClockDeviceMessage.SetTimeAdjustment), new SimpleEIWebMessageEntry())
                .put(messageSpec(ClockDeviceMessage.SetNTPServer), new SimpleEIWebMessageEntry())
                .put(messageSpec(ClockDeviceMessage.SetRefreshClockEvery), new SimpleEIWebMessageEntry())
                .put(messageSpec(ClockDeviceMessage.SetNTPOptions), new SimpleEIWebMessageEntry())
                .put(messageSpec(ClockDeviceMessage.NTPSetOption), new SimpleEIWebMessageEntry())
                .put(messageSpec(ClockDeviceMessage.NTPClrOption), new SimpleEIWebMessageEntry())

                // EIWeb Parameters
                .put(messageSpec(EIWebConfigurationDeviceMessage.SetEIWebPassword), new EIWebConfigurationMessageEntry())
                .put(messageSpec(EIWebConfigurationDeviceMessage.SetEIWebPage), new EIWebConfigurationMessageEntry())
                .put(messageSpec(EIWebConfigurationDeviceMessage.SetEIWebFallbackPage), new EIWebConfigurationMessageEntry())
                .put(messageSpec(EIWebConfigurationDeviceMessage.SetEIWebSendEvery), new EIWebConfigurationMessageEntry())
                .put(messageSpec(EIWebConfigurationDeviceMessage.SetEIWebCurrentInterval), new EIWebConfigurationMessageEntry())
                .put(messageSpec(EIWebConfigurationDeviceMessage.SetEIWebDatabaseID), new EIWebConfigurationMessageEntry())
                .put(messageSpec(EIWebConfigurationDeviceMessage.SetEIWebOptions), new EIWebConfigurationMessageEntry())
                .put(messageSpec(EIWebConfigurationDeviceMessage.EIWebSetOption), new EIWebConfigurationMessageEntry())
                .put(messageSpec(EIWebConfigurationDeviceMessage.EIWebClrOption), new EIWebConfigurationMessageEntry())

                // Read Mail (POP3) Parameters
                .put(messageSpec(MailConfigurationDeviceMessage.SetPOPUsername), new SimpleEIWebMessageEntry())
                .put(messageSpec(MailConfigurationDeviceMessage.SetPOPPassword), new SimpleEIWebMessageEntry())
                .put(messageSpec(MailConfigurationDeviceMessage.SetPOPHost), new SimpleEIWebMessageEntry())
                .put(messageSpec(MailConfigurationDeviceMessage.SetPOPReadMailEvery), new SimpleEIWebMessageEntry())
                .put(messageSpec(MailConfigurationDeviceMessage.SetPOP3Options), new SimpleEIWebMessageEntry())
                .put(messageSpec(MailConfigurationDeviceMessage.SetSMTPFrom), new SimpleEIWebMessageEntry())
                .put(messageSpec(MailConfigurationDeviceMessage.SetSMTPTo), new SimpleEIWebMessageEntry())
                .put(messageSpec(MailConfigurationDeviceMessage.SetSMTPConfigurationTo), new SimpleEIWebMessageEntry())
                .put(messageSpec(MailConfigurationDeviceMessage.SetSMTPServer), new SimpleEIWebMessageEntry())
                .put(messageSpec(MailConfigurationDeviceMessage.SetSMTPDomain), new SimpleEIWebMessageEntry())
                .put(messageSpec(MailConfigurationDeviceMessage.SetSMTPSendMailEvery), new SimpleEIWebMessageEntry())
                .put(messageSpec(MailConfigurationDeviceMessage.SetSMTPCurrentInterval), new SimpleEIWebMessageEntry())
                .put(messageSpec(MailConfigurationDeviceMessage.SetSMTPDatabaseID), new SimpleEIWebMessageEntry())
                .put(messageSpec(MailConfigurationDeviceMessage.SetSMTPOptions), new SimpleEIWebMessageEntry())
                .put(messageSpec(MailConfigurationDeviceMessage.POP3SetOption), new SimpleEIWebMessageEntry())
                .put(messageSpec(MailConfigurationDeviceMessage.POP3ClrOption), new SimpleEIWebMessageEntry())
                .put(messageSpec(MailConfigurationDeviceMessage.SMTPSetOption), new SimpleEIWebMessageEntry())
                .put(messageSpec(MailConfigurationDeviceMessage.SMTPClrOption), new SimpleEIWebMessageEntry())

                // SMS
                .put(messageSpec(SMSConfigurationDeviceMessage.SetSmsDataNbr), new SimpleEIWebMessageEntry())
                .put(messageSpec(SMSConfigurationDeviceMessage.SetSmsAlarmNbr), new SimpleEIWebMessageEntry())
                .put(messageSpec(SMSConfigurationDeviceMessage.SetSmsEvery), new SimpleEIWebMessageEntry())
                .put(messageSpec(SMSConfigurationDeviceMessage.SetSmsNbr), new SimpleEIWebMessageEntry())
                .put(messageSpec(SMSConfigurationDeviceMessage.SetSmsCorrection), new SimpleEIWebMessageEntry())
                .put(messageSpec(SMSConfigurationDeviceMessage.SetSmsConfig), new SimpleEIWebMessageEntry())
                .put(messageSpec(SMSConfigurationDeviceMessage.SMSSetOption), new SimpleEIWebMessageEntry())
                .put(messageSpec(SMSConfigurationDeviceMessage.SMSClrOption), new SimpleEIWebMessageEntry())

                //DLMS
                .put(messageSpec(DLMSConfigurationDeviceMessage.SetDLMSDeviceID), new SimpleEIWebMessageEntry())
                .put(messageSpec(DLMSConfigurationDeviceMessage.SetDLMSMeterID), new SimpleEIWebMessageEntry())
                .put(messageSpec(DLMSConfigurationDeviceMessage.SetDLMSPassword), new SimpleEIWebMessageEntry())
                .put(messageSpec(DLMSConfigurationDeviceMessage.SetDLMSIdleTime), new SimpleEIWebMessageEntry())

                // Duke Power Protocol
                .put(messageSpec(ConfigurationChangeDeviceMessage.SetDukePowerID), new SimpleEIWebMessageEntry())
                .put(messageSpec(ConfigurationChangeDeviceMessage.SetDukePowerPassword), new SimpleEIWebMessageEntry())
                .put(messageSpec(ConfigurationChangeDeviceMessage.SetDukePowerIdleTime), new SimpleEIWebMessageEntry())

                .put(messageSpec(ModemConfigurationDeviceMessage.SetDialCommand), new SimpleEIWebMessageEntry())
                .put(messageSpec(ModemConfigurationDeviceMessage.SetModemInit1), new SimpleEIWebMessageEntry())
                .put(messageSpec(ModemConfigurationDeviceMessage.SetModemInit2), new SimpleEIWebMessageEntry())
                .put(messageSpec(ModemConfigurationDeviceMessage.SetModemInit3), new SimpleEIWebMessageEntry())
                .put(messageSpec(ModemConfigurationDeviceMessage.SetPPPBaudRate), new SimpleEIWebMessageEntry())
                .put(messageSpec(ModemConfigurationDeviceMessage.SetModemtype), new SimpleEIWebMessageEntry())
                .put(messageSpec(ModemConfigurationDeviceMessage.SetResetCycle), new SimpleEIWebMessageEntry())

                // PPP
                .put(messageSpec(PPPConfigurationDeviceMessage.SetISP1Phone), new SimpleEIWebMessageEntry())
                .put(messageSpec(PPPConfigurationDeviceMessage.SetISP1Username), new SimpleEIWebMessageEntry())
                .put(messageSpec(PPPConfigurationDeviceMessage.SetISP1Password), new SimpleEIWebMessageEntry())
                .put(messageSpec(PPPConfigurationDeviceMessage.SetISP1Tries), new SimpleEIWebMessageEntry())
                .put(messageSpec(PPPConfigurationDeviceMessage.SetISP2Phone), new SimpleEIWebMessageEntry())
                .put(messageSpec(PPPConfigurationDeviceMessage.SetISP2Username), new SimpleEIWebMessageEntry())
                .put(messageSpec(PPPConfigurationDeviceMessage.SetISP2Password), new SimpleEIWebMessageEntry())
                .put(messageSpec(PPPConfigurationDeviceMessage.SetISP2Tries), new SimpleEIWebMessageEntry())
                .put(messageSpec(PPPConfigurationDeviceMessage.SetPPPIdleTimeout), new SimpleEIWebMessageEntry())
                .put(messageSpec(PPPConfigurationDeviceMessage.SetPPPRetryInterval), new SimpleEIWebMessageEntry())
                .put(messageSpec(PPPConfigurationDeviceMessage.SetPPPOptions), new SimpleEIWebMessageEntry())
                .put(messageSpec(PPPConfigurationDeviceMessage.PPPSetOption), new SimpleEIWebMessageEntry())
                .put(messageSpec(PPPConfigurationDeviceMessage.PPPClrOption), new SimpleEIWebMessageEntry())

                // Channels
                .put(messageSpec(ChannelConfigurationDeviceMessage.SetFunction), new ChannelMessageEntry())
                .put(messageSpec(ChannelConfigurationDeviceMessage.SetParameters), new ChannelMessageEntry())
                .put(messageSpec(ChannelConfigurationDeviceMessage.SetName), new ChannelMessageEntry())
                .put(messageSpec(ChannelConfigurationDeviceMessage.SetUnit), new ChannelMessageEntry())

                // Totalizers
                .put(messageSpec(TotalizersConfigurationDeviceMessage.SetSumMask), new TotalizerEIWebMessageEntry())
                .put(messageSpec(TotalizersConfigurationDeviceMessage.SetSubstractMask), new TotalizerEIWebMessageEntry())

                // Peak Shavers
                .put(messageSpec(PeakShaverConfigurationDeviceMessage.SetActiveChannel), new SimplePeakShaverMessageEntry())
                .put(messageSpec(PeakShaverConfigurationDeviceMessage.SetReactiveChannel), new SimplePeakShaverMessageEntry())
                .put(messageSpec(PeakShaverConfigurationDeviceMessage.SetTimeBase), new SimplePeakShaverMessageEntry())
                .put(messageSpec(PeakShaverConfigurationDeviceMessage.SetPOut), new SimplePeakShaverMessageEntry())
                .put(messageSpec(PeakShaverConfigurationDeviceMessage.SetPIn), new SimplePeakShaverMessageEntry())
                .put(messageSpec(PeakShaverConfigurationDeviceMessage.SetDeadTime), new SimplePeakShaverMessageEntry())
                .put(messageSpec(PeakShaverConfigurationDeviceMessage.SetAutomatic), new SimplePeakShaverMessageEntry())
                .put(messageSpec(PeakShaverConfigurationDeviceMessage.SetCyclic), new SimplePeakShaverMessageEntry())
                .put(messageSpec(PeakShaverConfigurationDeviceMessage.SetInvert), new SimplePeakShaverMessageEntry())
                .put(messageSpec(PeakShaverConfigurationDeviceMessage.SetAdaptSetpoint), new SimplePeakShaverMessageEntry())
                .put(messageSpec(PeakShaverConfigurationDeviceMessage.SetInstantAnalogOut), new SimplePeakShaverMessageEntry())
                .put(messageSpec(PeakShaverConfigurationDeviceMessage.SetPredictedAnalogOut), new SimplePeakShaverMessageEntry())
                .put(messageSpec(PeakShaverConfigurationDeviceMessage.SetpointAnalogOut), new SimplePeakShaverMessageEntry())
                .put(messageSpec(PeakShaverConfigurationDeviceMessage.SetDifferenceAnalogOut), new SimplePeakShaverMessageEntry())
                .put(messageSpec(PeakShaverConfigurationDeviceMessage.SetTariff), new SimplePeakShaverMessageEntry())
                .put(messageSpec(PeakShaverConfigurationDeviceMessage.SetResetLoads), new SimplePeakShaverMessageEntry())

                .put(messageSpec(PeakShaverConfigurationDeviceMessage.SetSetpoint), new SetSetpointMessageEntry())
                .put(messageSpec(PeakShaverConfigurationDeviceMessage.SetSwitchTime), new SetSwitchTimeMessageEntry())
                .put(messageSpec(PeakShaverConfigurationDeviceMessage.SetLoad), new SetLoadMessageEntry())

                // Events
                .put(messageSpec(LogBookDeviceMessage.SetInputChannel), new SimpleEIWebMessageEntry())
                .put(messageSpec(LogBookDeviceMessage.SetCondition), new SimpleEIWebMessageEntry())
                .put(messageSpec(LogBookDeviceMessage.SetConditionValue), new SimpleEIWebMessageEntry())
                .put(messageSpec(LogBookDeviceMessage.SetTimeTrue), new SimpleEIWebMessageEntry())
                .put(messageSpec(LogBookDeviceMessage.SetTimeFalse), new SimpleEIWebMessageEntry())
                .put(messageSpec(LogBookDeviceMessage.SetOutputChannel), new SimpleEIWebMessageEntry())
                .put(messageSpec(LogBookDeviceMessage.SetAlarm), new SimpleEIWebMessageEntry())
                .put(messageSpec(LogBookDeviceMessage.SetTag), new SimpleEIWebMessageEntry())
                .put(messageSpec(LogBookDeviceMessage.SetInverse), new SimpleEIWebMessageEntry())
                .put(messageSpec(LogBookDeviceMessage.SetImmediate), new SimpleEIWebMessageEntry())

                // Opus
                .put(messageSpec(OpusConfigurationDeviceMessage.SetOpusOSNbr), new SimpleEIWebMessageEntry())
                .put(messageSpec(OpusConfigurationDeviceMessage.SetOpusPassword), new SimpleEIWebMessageEntry())
                .put(messageSpec(OpusConfigurationDeviceMessage.SetOpusTimeout), new SimpleEIWebMessageEntry())
                .put(messageSpec(OpusConfigurationDeviceMessage.SetOpusConfig), new SimpleEIWebMessageEntry())
                .put(messageSpec(OpusConfigurationDeviceMessage.OpusSetOption), new SimpleEIWebMessageEntry())
                .put(messageSpec(OpusConfigurationDeviceMessage.OpusClrOption), new SimpleEIWebMessageEntry())

                // Modbus Master
                .put(messageSpec(ModbusConfigurationDeviceMessage.SetMmEvery), new SimpleEIWebMessageEntry())
                .put(messageSpec(ModbusConfigurationDeviceMessage.SetMmTimeout), new SimpleEIWebMessageEntry())
                .put(messageSpec(ModbusConfigurationDeviceMessage.SetMmInstant), new SimpleEIWebMessageEntry())
                .put(messageSpec(ModbusConfigurationDeviceMessage.SetMmOverflow), new SimpleEIWebMessageEntry())
                .put(messageSpec(ModbusConfigurationDeviceMessage.SetMmConfig), new SimpleEIWebMessageEntry())
                .put(messageSpec(ModbusConfigurationDeviceMessage.MmSetOption), new SimpleEIWebMessageEntry())
                .put(messageSpec(ModbusConfigurationDeviceMessage.MmClrOption), new SimpleEIWebMessageEntry())

                // MBus Master
                .put(messageSpec(MBusConfigurationDeviceMessage.SetMBusEvery), new SimpleEIWebMessageEntry())
                .put(messageSpec(MBusConfigurationDeviceMessage.SetMBusInterFrameTime), new SimpleEIWebMessageEntry())
                .put(messageSpec(MBusConfigurationDeviceMessage.SetMBusConfig), new SimpleEIWebMessageEntry())
                .put(messageSpec(MBusConfigurationDeviceMessage.MBusSetOption), new SimpleEIWebMessageEntry())
                .put(messageSpec(MBusConfigurationDeviceMessage.MBusClrOption), new SimpleEIWebMessageEntry())

                // General Commands
                .put(messageSpec(DeviceActionMessage.SetFTIONReboot), new SimpleEIWebMessageEntry())
                .put(messageSpec(DeviceActionMessage.SetFTIONInitialize), new SimpleEIWebMessageEntry())
                .put(messageSpec(DeviceActionMessage.SetFTIONMailLog), new SimpleEIWebMessageEntry())
                .put(messageSpec(DeviceActionMessage.SetFTIONMailConfig), new SimpleEIWebMessageEntry())
                .put(messageSpec(DeviceActionMessage.SetFTIONSaveConfig), new SimpleEIWebMessageEntry())
                .put(messageSpec(DeviceActionMessage.SetFTIONUpgrade), new SimpleEIWebMessageEntry())
                .put(messageSpec(DeviceActionMessage.SetFTIONClearMem), new SimpleEIWebMessageEntry())
                .put(messageSpec(DeviceActionMessage.SetFTIONModemReset), new SimpleEIWebMessageEntry())
                .put(messageSpec(DeviceActionMessage.SetChangeAdminPassword), new ChangeAdminPasswordMessageEntry())

                .put(messageSpec(OutputConfigurationMessage.SetOutputOn), new SimpleEIWebMessageEntry())
                .put(messageSpec(OutputConfigurationMessage.SetOutputOff), new SimpleEIWebMessageEntry())
                .put(messageSpec(OutputConfigurationMessage.SetOutputToggle), new SimpleEIWebMessageEntry())
                .put(messageSpec(OutputConfigurationMessage.SetOutputPulse), new SimpleEIWebMessageEntry())
                .put(messageSpec(DeviceActionMessage.SetAnalogOut), new AnalogOutMessageEntry())
                .put(messageSpec(GeneralDeviceMessage.SEND_XML_MESSAGE), new XMLAttributeDeviceMessageEntry())
                .build();
    }
}