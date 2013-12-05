package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.protocol.dynamic.PropertySpec;
import com.energyict.mdc.protocol.device.messages.DeviceMessageSpec;
import com.energyict.protocolimplv2.messages.ChannelConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.ClockDeviceMessage;
import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;
import com.energyict.protocolimplv2.messages.DLMSConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.EIWebConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.LogBookDeviceMessage;
import com.energyict.protocolimplv2.messages.MBusConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.MailConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.ModbusConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.ModemConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.NetworkConnectivityMessage;
import com.energyict.protocolimplv2.messages.OpusConfigurationDeviceMessage;
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

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a MessageConverter that maps the new EIWeb messages to legacy XML
 * <p/>
 * Copyrights EnergyICT
 * Date: 8/03/13
 * Time: 16:26
 */
public class EIWebMessageConverter extends AbstractMessageConverter {

    /**
     * Represents a mapping between {@link com.energyict.mdc.protocol.device.messages.DeviceMessageSpec}s
     * and the corresponding {@link com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator}
     */
    private static Map<DeviceMessageSpec, MessageEntryCreator> registry = new HashMap<>();

    static {

        // General Parameters
        registry.put(ConfigurationChangeDeviceMessage.SetDescription, new SimpleEIWebMessageEntry());
        registry.put(ConfigurationChangeDeviceMessage.SetIntervalInSeconds, new SimpleEIWebMessageEntry());
        registry.put(ConfigurationChangeDeviceMessage.SetUpgradeUrl, new SimpleEIWebMessageEntry());
        registry.put(ConfigurationChangeDeviceMessage.SetUpgradeOptions, new SimpleEIWebMessageEntry());
        registry.put(ConfigurationChangeDeviceMessage.SetDebounceTreshold, new SimpleEIWebMessageEntry());
        registry.put(ConfigurationChangeDeviceMessage.SetTariffMoment, new SimpleEIWebMessageEntry());
        registry.put(ConfigurationChangeDeviceMessage.SetCommOffset, new SimpleEIWebMessageEntry());
        registry.put(ConfigurationChangeDeviceMessage.SetAggIntv, new SimpleEIWebMessageEntry());
        registry.put(ConfigurationChangeDeviceMessage.SetPulseTimeTrue, new SimpleEIWebMessageEntry());

        // Network Parameters
        registry.put(NetworkConnectivityMessage.SetProxyServer, new SimpleEIWebMessageEntry());
        registry.put(NetworkConnectivityMessage.SetProxyUsername, new SimpleEIWebMessageEntry());
        registry.put(NetworkConnectivityMessage.SetProxyPassword, new SimpleEIWebMessageEntry());
        registry.put(NetworkConnectivityMessage.SetDHCP, new SimpleEIWebMessageEntry());
        registry.put(NetworkConnectivityMessage.SetDHCPTimeout, new SimpleEIWebMessageEntry());
        registry.put(NetworkConnectivityMessage.SetIPAddress, new SimpleEIWebMessageEntry());
        registry.put(NetworkConnectivityMessage.SetSubnetMask, new SimpleEIWebMessageEntry());
        registry.put(NetworkConnectivityMessage.SetGateway, new SimpleEIWebMessageEntry());
        registry.put(NetworkConnectivityMessage.SetNameServer, new SimpleEIWebMessageEntry());
        registry.put(NetworkConnectivityMessage.SetHttpPort, new SimpleEIWebMessageEntry());

        // Time Parameters
        registry.put(ClockDeviceMessage.SetDST, new SimpleEIWebMessageEntry());
        registry.put(ClockDeviceMessage.SetTimezone, new SimpleEIWebMessageEntry());
        registry.put(ClockDeviceMessage.SetTimeAdjustment, new SimpleEIWebMessageEntry());
        registry.put(ClockDeviceMessage.SetNTPServer, new SimpleEIWebMessageEntry());
        registry.put(ClockDeviceMessage.SetRefreshClockEvery, new SimpleEIWebMessageEntry());
        registry.put(ClockDeviceMessage.SetNTPOptions, new SimpleEIWebMessageEntry());

        // EIWeb Parameters
        registry.put(EIWebConfigurationDeviceMessage.SetEIWebPassword, new EIWebConfigurationMessageEntry());
        registry.put(EIWebConfigurationDeviceMessage.SetEIWebPage, new EIWebConfigurationMessageEntry());
        registry.put(EIWebConfigurationDeviceMessage.SetEIWebFallbackPage, new EIWebConfigurationMessageEntry());
        registry.put(EIWebConfigurationDeviceMessage.SetEIWebSendEvery, new EIWebConfigurationMessageEntry());
        registry.put(EIWebConfigurationDeviceMessage.SetEIWebCurrentInterval, new EIWebConfigurationMessageEntry());
        registry.put(EIWebConfigurationDeviceMessage.SetEIWebDatabaseID, new EIWebConfigurationMessageEntry());
        registry.put(EIWebConfigurationDeviceMessage.SetEIWebOptions, new EIWebConfigurationMessageEntry());

        // Read Mail (POP3) Parameters
        registry.put(MailConfigurationDeviceMessage.SetPOPUsername, new SimpleEIWebMessageEntry());
        registry.put(MailConfigurationDeviceMessage.SetPOPPassword, new SimpleEIWebMessageEntry());
        registry.put(MailConfigurationDeviceMessage.SetPOPHost, new SimpleEIWebMessageEntry());
        registry.put(MailConfigurationDeviceMessage.SetPOPReadMailEvery, new SimpleEIWebMessageEntry());
        registry.put(MailConfigurationDeviceMessage.SetPOP3Options, new SimpleEIWebMessageEntry());
        registry.put(MailConfigurationDeviceMessage.SetSMTPFrom, new SimpleEIWebMessageEntry());
        registry.put(MailConfigurationDeviceMessage.SetSMTPTo, new SimpleEIWebMessageEntry());
        registry.put(MailConfigurationDeviceMessage.SetSMTPConfigurationTo, new SimpleEIWebMessageEntry());
        registry.put(MailConfigurationDeviceMessage.SetSMTPServer, new SimpleEIWebMessageEntry());
        registry.put(MailConfigurationDeviceMessage.SetSMTPDomain, new SimpleEIWebMessageEntry());
        registry.put(MailConfigurationDeviceMessage.SetSMTPSendMailEvery, new SimpleEIWebMessageEntry());
        registry.put(MailConfigurationDeviceMessage.SetSMTPCurrentInterval, new SimpleEIWebMessageEntry());
        registry.put(MailConfigurationDeviceMessage.SetSMTPDatabaseID, new SimpleEIWebMessageEntry());
        registry.put(MailConfigurationDeviceMessage.SetSMTPOptions, new SimpleEIWebMessageEntry());

        // SMS
        registry.put(SMSConfigurationDeviceMessage.SetSmsDataNbr, new SimpleEIWebMessageEntry());
        registry.put(SMSConfigurationDeviceMessage.SetSmsAlarmNbr, new SimpleEIWebMessageEntry());
        registry.put(SMSConfigurationDeviceMessage.SetSmsEvery, new SimpleEIWebMessageEntry());
        registry.put(SMSConfigurationDeviceMessage.SetSmsNbr, new SimpleEIWebMessageEntry());
        registry.put(SMSConfigurationDeviceMessage.SetSmsCorrection, new SimpleEIWebMessageEntry());
        registry.put(SMSConfigurationDeviceMessage.SetSmsConfig, new SimpleEIWebMessageEntry());

        //DLMS
        registry.put(DLMSConfigurationDeviceMessage.SetDLMSDeviceID, new SimpleEIWebMessageEntry());
        registry.put(DLMSConfigurationDeviceMessage.SetDLMSMeterID, new SimpleEIWebMessageEntry());
        registry.put(DLMSConfigurationDeviceMessage.SetDLMSPassword, new SimpleEIWebMessageEntry());
        registry.put(DLMSConfigurationDeviceMessage.SetDLMSIdleTime, new SimpleEIWebMessageEntry());

        // Duke Power Protocol
        registry.put(ConfigurationChangeDeviceMessage.SetDukePowerID, new SimpleEIWebMessageEntry());
        registry.put(ConfigurationChangeDeviceMessage.SetDukePowerPassword, new SimpleEIWebMessageEntry());
        registry.put(ConfigurationChangeDeviceMessage.SetDukePowerIdleTime, new SimpleEIWebMessageEntry());

        registry.put(ModemConfigurationDeviceMessage.SetDialCommand, new SimpleEIWebMessageEntry());
        registry.put(ModemConfigurationDeviceMessage.SetModemInit1, new SimpleEIWebMessageEntry());
        registry.put(ModemConfigurationDeviceMessage.SetModemInit2, new SimpleEIWebMessageEntry());
        registry.put(ModemConfigurationDeviceMessage.SetModemInit3, new SimpleEIWebMessageEntry());
        registry.put(ModemConfigurationDeviceMessage.SetPPPBaudRate, new SimpleEIWebMessageEntry());
        registry.put(ModemConfigurationDeviceMessage.SetModemtype, new SimpleEIWebMessageEntry());
        registry.put(ModemConfigurationDeviceMessage.SetResetCycle, new SimpleEIWebMessageEntry());

        // PPP
        registry.put(PPPConfigurationDeviceMessage.SetISP1Phone, new SimpleEIWebMessageEntry());
        registry.put(PPPConfigurationDeviceMessage.SetISP1Username, new SimpleEIWebMessageEntry());
        registry.put(PPPConfigurationDeviceMessage.SetISP1Password, new SimpleEIWebMessageEntry());
        registry.put(PPPConfigurationDeviceMessage.SetISP1Tries, new SimpleEIWebMessageEntry());
        registry.put(PPPConfigurationDeviceMessage.SetISP2Phone, new SimpleEIWebMessageEntry());
        registry.put(PPPConfigurationDeviceMessage.SetISP2Username, new SimpleEIWebMessageEntry());
        registry.put(PPPConfigurationDeviceMessage.SetISP2Password, new SimpleEIWebMessageEntry());
        registry.put(PPPConfigurationDeviceMessage.SetISP2Tries, new SimpleEIWebMessageEntry());
        registry.put(PPPConfigurationDeviceMessage.SetPPPIdleTimeout, new SimpleEIWebMessageEntry());
        registry.put(PPPConfigurationDeviceMessage.SetPPPRetryInterval, new SimpleEIWebMessageEntry());
        registry.put(PPPConfigurationDeviceMessage.SetPPPOptions, new SimpleEIWebMessageEntry());

        // Channels
        registry.put(ChannelConfigurationDeviceMessage.SetFunction, new ChannelMessageEntry());
        registry.put(ChannelConfigurationDeviceMessage.SetParameters, new ChannelMessageEntry());
        registry.put(ChannelConfigurationDeviceMessage.SetName, new ChannelMessageEntry());
        registry.put(ChannelConfigurationDeviceMessage.SetUnit, new ChannelMessageEntry());

        // Totalizers
        registry.put(TotalizersConfigurationDeviceMessage.SetSumMask, new TotalizerEIWebMessageEntry());
        registry.put(TotalizersConfigurationDeviceMessage.SetSubstractMask, new TotalizerEIWebMessageEntry());

        // Peak Shavers
        registry.put(PeakShaverConfigurationDeviceMessage.SetActiveChannel, new SimplePeakShaverMessageEntry());
        registry.put(PeakShaverConfigurationDeviceMessage.SetReactiveChannel, new SimplePeakShaverMessageEntry());
        registry.put(PeakShaverConfigurationDeviceMessage.SetTimeBase, new SimplePeakShaverMessageEntry());
        registry.put(PeakShaverConfigurationDeviceMessage.SetPOut, new SimplePeakShaverMessageEntry());
        registry.put(PeakShaverConfigurationDeviceMessage.SetPIn, new SimplePeakShaverMessageEntry());
        registry.put(PeakShaverConfigurationDeviceMessage.SetDeadTime, new SimplePeakShaverMessageEntry());
        registry.put(PeakShaverConfigurationDeviceMessage.SetAutomatic, new SimplePeakShaverMessageEntry());
        registry.put(PeakShaverConfigurationDeviceMessage.SetCyclic, new SimplePeakShaverMessageEntry());
        registry.put(PeakShaverConfigurationDeviceMessage.SetInvert, new SimplePeakShaverMessageEntry());
        registry.put(PeakShaverConfigurationDeviceMessage.SetAdaptSetpoint, new SimplePeakShaverMessageEntry());
        registry.put(PeakShaverConfigurationDeviceMessage.SetInstantAnalogOut, new SimplePeakShaverMessageEntry());
        registry.put(PeakShaverConfigurationDeviceMessage.SetPredictedAnalogOut, new SimplePeakShaverMessageEntry());
        registry.put(PeakShaverConfigurationDeviceMessage.SetpointAnalogOut, new SimplePeakShaverMessageEntry());
        registry.put(PeakShaverConfigurationDeviceMessage.SetDifferenceAnalogOut, new SimplePeakShaverMessageEntry());
        registry.put(PeakShaverConfigurationDeviceMessage.SetTariff, new SimplePeakShaverMessageEntry());
        registry.put(PeakShaverConfigurationDeviceMessage.SetResetLoads, new SimplePeakShaverMessageEntry());

        registry.put(PeakShaverConfigurationDeviceMessage.SetSetpoint, new SetSetpointMessageEntry());
        registry.put(PeakShaverConfigurationDeviceMessage.SetSwitchTime, new SetSwitchTimeMessageEntry());
        registry.put(PeakShaverConfigurationDeviceMessage.SetLoad, new SetLoadMessageEntry());

        // Events
        registry.put(LogBookDeviceMessage.SetInputChannel, new SimpleEIWebMessageEntry());
        registry.put(LogBookDeviceMessage.SetCondition, new SimpleEIWebMessageEntry());
        registry.put(LogBookDeviceMessage.SetConditionValue, new SimpleEIWebMessageEntry());
        registry.put(LogBookDeviceMessage.SetTimeTrue, new SimpleEIWebMessageEntry());
        registry.put(LogBookDeviceMessage.SetTimeFalse, new SimpleEIWebMessageEntry());
        registry.put(LogBookDeviceMessage.SetOutputChannel, new SimpleEIWebMessageEntry());
        registry.put(LogBookDeviceMessage.SetAlarm, new SimpleEIWebMessageEntry());
        registry.put(LogBookDeviceMessage.SetTag, new SimpleEIWebMessageEntry());
        registry.put(LogBookDeviceMessage.SetInverse, new SimpleEIWebMessageEntry());
        registry.put(LogBookDeviceMessage.SetImmediate, new SimpleEIWebMessageEntry());

        // Opus
        registry.put(OpusConfigurationDeviceMessage.SetOpusOSNbr, new SimpleEIWebMessageEntry());
        registry.put(OpusConfigurationDeviceMessage.SetOpusPassword, new SimpleEIWebMessageEntry());
        registry.put(OpusConfigurationDeviceMessage.SetOpusTimeout, new SimpleEIWebMessageEntry());
        registry.put(OpusConfigurationDeviceMessage.SetOpusConfig, new SimpleEIWebMessageEntry());

        // Modbus Master
        registry.put(ModbusConfigurationDeviceMessage.SetMmEvery, new SimpleEIWebMessageEntry());
        registry.put(ModbusConfigurationDeviceMessage.SetMmTimeout, new SimpleEIWebMessageEntry());
        registry.put(ModbusConfigurationDeviceMessage.SetMmInstant, new SimpleEIWebMessageEntry());
        registry.put(ModbusConfigurationDeviceMessage.SetMmOverflow, new SimpleEIWebMessageEntry());
        registry.put(ModbusConfigurationDeviceMessage.SetMmConfig, new SimpleEIWebMessageEntry());

        // MBus Master
        registry.put(MBusConfigurationDeviceMessage.SetMBusEvery, new SimpleEIWebMessageEntry());
        registry.put(MBusConfigurationDeviceMessage.SetMBusInterFrameTime, new SimpleEIWebMessageEntry());
        registry.put(MBusConfigurationDeviceMessage.SetMBusConfig, new SimpleEIWebMessageEntry());

        // General Commands
        registry.put(DeviceActionMessage.SetFTIONReboot, new SimpleEIWebMessageEntry());
        registry.put(DeviceActionMessage.SetFTIONInitialize, new SimpleEIWebMessageEntry());
        registry.put(DeviceActionMessage.SetFTIONMailLog, new SimpleEIWebMessageEntry());
        registry.put(DeviceActionMessage.SetFTIONMailConfig, new SimpleEIWebMessageEntry());
        registry.put(DeviceActionMessage.SetFTIONSaveConfig, new SimpleEIWebMessageEntry());
        registry.put(DeviceActionMessage.SetFTIONUpgrade, new SimpleEIWebMessageEntry());
        registry.put(DeviceActionMessage.SetFTIONClearMem, new SimpleEIWebMessageEntry());
        registry.put(DeviceActionMessage.SetFTIONModemReset, new SimpleEIWebMessageEntry());
        registry.put(DeviceActionMessage.SetChangeAdminPassword, new ChangeAdminPasswordMessageEntry());

        registry.put(DeviceActionMessage.SetOutputOn, new SimpleEIWebMessageEntry());
        registry.put(DeviceActionMessage.SetOutputOff, new SimpleEIWebMessageEntry());
        registry.put(DeviceActionMessage.SetOutputToggle, new SimpleEIWebMessageEntry());
        registry.put(DeviceActionMessage.SetOutputPulse, new SimpleEIWebMessageEntry());
        registry.put(DeviceActionMessage.SetAnalogOut, new AnalogOutMessageEntry());
    }

    /**
     * Default constructor for at-runtime instantiation
     */
    public EIWebMessageConverter() {
        super();
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return messageAttribute.toString();
    }

    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return registry;
    }
}