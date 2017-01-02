package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.MessageEntryCreator;
import com.energyict.mdc.upl.messages.legacy.Messaging;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.DeviceMessageFile;
import com.energyict.mdc.upl.properties.Password;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocolimplv2.messages.ClockDeviceMessage;
import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.EIWebConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.FirmwareDeviceMessage;
import com.energyict.protocolimplv2.messages.LoggingConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.OutputConfigurationMessage;
import com.energyict.protocolimplv2.messages.PLCConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.SecurityMessage;
import com.energyict.protocolimplv2.messages.WavenisDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.eiweb.SimpleEIWebMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.eiwebplus.ForceMessageToFailedMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.eiwebplus.IDISDiscoveryConfigurationMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.eiwebplus.IDISRepeaterCallConfigurationMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleAttributeMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleInnerTagsMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleTagMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleValueMessageEntry;
import com.google.common.collect.ImmutableMap;

import java.nio.charset.Charset;
import java.util.Map;

/**
 * List of all supported messages for the RTU+Server using the EIWeb+ servlet.
 * <p>
 * Copyrights EnergyICT
 * Date: 8/03/13
 * Time: 16:26
 */
public class EIWebPlusMessageConverter extends AbstractMessageConverter {

    private final DeviceMessageFileExtractor deviceMessageFileExtractor;

    protected EIWebPlusMessageConverter(Messaging messagingProtocol, PropertySpecService propertySpecService, NlsService nlsService, Converter converter, DeviceMessageFileExtractor deviceMessageFileExtractor) {
        super(messagingProtocol, propertySpecService, nlsService, converter);
        this.deviceMessageFileExtractor = deviceMessageFileExtractor;
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getName().equals(DeviceMessageConstants.waveCardFirmware)
                || propertySpec.getName().equals(DeviceMessageConstants.sslCertificateUserFile)
                || propertySpec.getName().equals(DeviceMessageConstants.firmwareUpdateUserFileAttributeName)
                || propertySpec.getName().equals(DeviceMessageConstants.PricingInformationUserFileAttributeName)
                || propertySpec.getName().equals(DeviceMessageConstants.nodeListUserFile)) {
            return this.deviceMessageFileExtractor.contents((DeviceMessageFile) messageAttribute, Charset.forName("US-ASCII"));
        } else if (propertySpec.getName().equals(DeviceMessageConstants.newPasswordAttributeName)) {
            return ((Password) messageAttribute).getValue();
        }

        return messageAttribute.toString();
    }

    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return ImmutableMap
                .<DeviceMessageSpec, MessageEntryCreator>builder()
                // Digital output switch rules
                .put(messageSpec(OutputConfigurationMessage.AbsoluteDOSwitchRule), new MultipleAttributeMessageEntry("DOSwitchRule", DeviceMessageConstants.id, DeviceMessageConstants.startTime, DeviceMessageConstants.endTime, DeviceMessageConstants.outputBitMap))
                .put(messageSpec(OutputConfigurationMessage.DeleteDOSwitchRule), new MultipleAttributeMessageEntry("DOSwitchRule", DeviceMessageConstants.id, DeviceMessageConstants.delete))
                .put(messageSpec(OutputConfigurationMessage.RelativeDOSwitchRule), new MultipleAttributeMessageEntry("DOSwitchRule", DeviceMessageConstants.id, DeviceMessageConstants.duration, DeviceMessageConstants.outputBitMap))

                // Firewall messages
                .put(messageSpec(ConfigurationChangeDeviceMessage.EnableFW), new SimpleTagMessageEntry("EnableFW"))
                .put(messageSpec(ConfigurationChangeDeviceMessage.DisableFW), new SimpleTagMessageEntry("DisableFW"))

                // General Commands
                .put(messageSpec(DeviceActionMessage.RtuPlusServerEnterMaintenanceMode), new SimpleTagMessageEntry("RtuPlusServerEnterMaintenanceMode"))
                .put(messageSpec(DeviceActionMessage.RtuPlusServerExitMaintenanceMode), new SimpleTagMessageEntry("RtuPlusServerExitMaintenanceMode"))
                .put(messageSpec(DeviceActionMessage.ForceMessageToFailed), new ForceMessageToFailedMessageEntry())
                .put(messageSpec(ClockDeviceMessage.FTIONForceTimeSync), new SimpleTagMessageEntry("FTIONForceTimeSync"))
                .put(messageSpec(DeviceActionMessage.FTIONInitDatabaseKeepConfig), new SimpleTagMessageEntry("FTIONInitDatabaseKeepConfig"))
                .put(messageSpec(DeviceActionMessage.FTIONReboot), new SimpleTagMessageEntry("FTIONReboot"))
                .put(messageSpec(DeviceActionMessage.FTIONRestart), new SimpleTagMessageEntry("FTIONRestart"))
                .put(messageSpec(DeviceActionMessage.FTIONScanBus), new SimpleTagMessageEntry("FTIONScanBus"))
                .put(messageSpec(DeviceActionMessage.SyncMasterdata), new SimpleTagMessageEntry("SyncMasterdata"))
                .put(messageSpec(FirmwareDeviceMessage.FTIONUpgradeRFMeshFirmware), new SimpleTagMessageEntry("FTIONUpgradeRFMeshFirmware"))
                .put(messageSpec(DeviceActionMessage.FTIONUpgrade), new SimpleTagMessageEntry("FTIONUpgrade"))
                .put(messageSpec(DeviceActionMessage.FTIONUpgradeAndInit), new SimpleTagMessageEntry("FTIONUpgradeAndInit"))
                .put(messageSpec(DeviceActionMessage.FTIONUpgradeAndInitWithNewEIServerURL), new SimpleValueMessageEntry("FTIONUpgradeAndInitWithNewEIServerURL"))
                .put(messageSpec(DeviceActionMessage.FTIONUpgradeWithNewEIServerURL), new SimpleValueMessageEntry("FTIONUpgradeWithNewEIServerURL"))
                .put(messageSpec(FirmwareDeviceMessage.UpgradeBootloader), new SimpleValueMessageEntry("UpgradeBootloader"))

                // General Parameters
                .put(messageSpec(SecurityMessage.CHANGE_PASSWORD_WITH_NEW_PASSWORD), new SimpleValueMessageEntry("Password"))
                .put(messageSpec(FirmwareDeviceMessage.RFMeshUpgradeURL), new SimpleValueMessageEntry("RFMeshUpgradeURL"))
                .put(messageSpec(ConfigurationChangeDeviceMessage.WhitelistedPhoneNumbers), new SimpleValueMessageEntry("WhitelistedPhoneNumbers"))
                .put(messageSpec(ConfigurationChangeDeviceMessage.BootSyncEnable), new SimpleValueMessageEntry("BootSyncEnable"))
                .put(messageSpec(EIWebConfigurationDeviceMessage.UpdateEIWebSSLCertificate), new SimpleValueMessageEntry("UpdateEIWebSSLCertificate"))
                .put(messageSpec(ConfigurationChangeDeviceMessage.SetUpgradeUrl), new SimpleEIWebMessageEntry())

                //IDIS stuff
                .put(messageSpec(PLCConfigurationDeviceMessage.IDISDiscoveryConfiguration), new IDISDiscoveryConfigurationMessageEntry())
                .put(messageSpec(PLCConfigurationDeviceMessage.IDISRepeaterCallConfiguration), new IDISRepeaterCallConfigurationMessageEntry())
                .put(messageSpec(PLCConfigurationDeviceMessage.IDISRunRepeaterCallNow), new SimpleTagMessageEntry("IDISRunRepeaterCallNow"))
                .put(messageSpec(PLCConfigurationDeviceMessage.IDISRunNewMeterDiscoveryCallNow), new SimpleTagMessageEntry("IDISRunNewMeterDiscoveryCallNow"))
                .put(messageSpec(PLCConfigurationDeviceMessage.IDISRunAlarmDiscoveryCallNow), new SimpleTagMessageEntry("IDISRunAlarmDiscoveryCallNow"))
                .put(messageSpec(PLCConfigurationDeviceMessage.IDISWhitelistConfiguration), new MultipleInnerTagsMessageEntry("IDISWhitelistConfiguration", "Enabled (true/false)", "Group Name (the group containing the meters included in the whitelist)"))
                .put(messageSpec(PLCConfigurationDeviceMessage.IDISOperatingWindowConfiguration), new MultipleInnerTagsMessageEntry("IDISOperatingWindowConfiguration", "Enabled (true/false)", "Start time (MM:SS) (GMT)", "End time (MM:SS) (GMT)"))
                .put(messageSpec(PLCConfigurationDeviceMessage.IDISPhyConfiguration), new MultipleInnerTagsMessageEntry("IDISPhyConfiguration", "Bit sync", "Zero cross adjust", "TX gain", "RX gain"))
                .put(messageSpec(PLCConfigurationDeviceMessage.IDISCreditManagementConfiguration), new MultipleInnerTagsMessageEntry("IDISCreditManagementConfiguration", "Add credit", "Min credit"))

                //Logging
                .put(messageSpec(LoggingConfigurationDeviceMessage.DownloadFile), new SimpleValueMessageEntry("DownloadFile"))
                .put(messageSpec(LoggingConfigurationDeviceMessage.PushConfiguration), new SimpleTagMessageEntry("PushConfiguration"))
                .put(messageSpec(LoggingConfigurationDeviceMessage.PushLogsNow), new SimpleTagMessageEntry("PushLogsNow"))

                //Output
                .put(messageSpec(OutputConfigurationMessage.OutputOff), new SimpleValueMessageEntry("OutputOff"))
                .put(messageSpec(OutputConfigurationMessage.OutputOn), new SimpleValueMessageEntry("OutputOn"))

                //PLC control
                .put(messageSpec(PLCConfigurationDeviceMessage.PLCEnableDisable), new SimpleValueMessageEntry("PLCEnableDisable"))
                .put(messageSpec(PLCConfigurationDeviceMessage.PLCFreqPairSelection), new SimpleValueMessageEntry("PLCFreqPairSelection"))
                .put(messageSpec(PLCConfigurationDeviceMessage.PLCRequestConfig), new SimpleTagMessageEntry("PLCRequestConfig"))
                .put(messageSpec(PLCConfigurationDeviceMessage.CIASEDiscoveryMaxCredits), new SimpleValueMessageEntry("CIASEDiscoveryMaxCredits"))
                .put(messageSpec(PLCConfigurationDeviceMessage.PLCChangeMacAddress), new SimpleValueMessageEntry("PLCChangeMacAddress"))

                //PLC Prime control
                .put(messageSpec(PLCConfigurationDeviceMessage.PLCPrimeCancelFirmwareUpgrade), new SimpleTagMessageEntry("PLCPrimeCancelFirmwareUpgrade"))
                .put(messageSpec(PLCConfigurationDeviceMessage.PLCPrimeReadPIB), new SimpleValueMessageEntry("PLCPrimeReadPIB"))
                .put(messageSpec(PLCConfigurationDeviceMessage.PLCPrimeRequestFirmwareVersion), new SimpleValueMessageEntry("PLCPrimeRequestFirmwareVersion"))
                .put(messageSpec(FirmwareDeviceMessage.PLCPrimeStartFirmwareUpgradeNodeList), new SimpleValueMessageEntry("PLCPrimeStartFirmwareUpgradeNodeList"))
                .put(messageSpec(FirmwareDeviceMessage.PLCPrimeSetFirmwareUpgradeFile), new SimpleValueMessageEntry("PLCPrimeSetFirmwareUpgradeFile"))
                .put(messageSpec(PLCConfigurationDeviceMessage.PLCPrimeWritePIB), new SimpleValueMessageEntry("PLCPrimeWritePIB"))

                //Wavenis stuff
                .put(messageSpec(WavenisDeviceMessage.WavenisAddAddressGetNetworkId), new SimpleValueMessageEntry("WavenisAddAddressGetNetworkId"))
                .put(messageSpec(WavenisDeviceMessage.WavenisAddAddressWithNetworkId), new SimpleValueMessageEntry("WavenisAddAddressWithNetworkId"))
                .put(messageSpec(WavenisDeviceMessage.WavenisBranchMove), new SimpleValueMessageEntry("WavenisBranchMove"))
                .put(messageSpec(WavenisDeviceMessage.WavenisChangeMasterAddress), new SimpleValueMessageEntry("WavenisChangeMasterAddress"))
                .put(messageSpec(WavenisDeviceMessage.WavenisCompareRepaireDatabases), new SimpleTagMessageEntry("WavenisCompareRepaireDatabases"))
                .put(messageSpec(WavenisDeviceMessage.WavenisDeleteAddress), new SimpleValueMessageEntry("WavenisDeleteAddress"))
                .put(messageSpec(WavenisDeviceMessage.WavenisInitBubbleUpSlotDatabase), new SimpleTagMessageEntry("WavenisInitBubbleUpSlotDatabase"))
                .put(messageSpec(WavenisDeviceMessage.WavenisInitDatabases), new SimpleTagMessageEntry("WavenisInitDatabases"))
                .put(messageSpec(WavenisDeviceMessage.WavenisProgramRadioAddress), new SimpleValueMessageEntry("WavenisProgramRadioAddress"))
                .put(messageSpec(WavenisDeviceMessage.WavenisRemoveBubbleUpSlot), new SimpleValueMessageEntry("WavenisRemoveBubbleUpSlot"))
                .put(messageSpec(WavenisDeviceMessage.WavenisRequestBubbleUpSlot), new SimpleValueMessageEntry("WavenisRequestBubbleUpSlot"))
                .put(messageSpec(WavenisDeviceMessage.WavenisRequestModuleStatus), new SimpleValueMessageEntry("WavenisRequestModuleStatus"))
                .put(messageSpec(WavenisDeviceMessage.WaveCardRadioAddress), new SimpleTagMessageEntry("WaveCardRadioAddress"))
                .put(messageSpec(WavenisDeviceMessage.WavenisRestoreDatabasesUsingEIServerMasterdata), new SimpleTagMessageEntry("WavenisRestoreDatabasesUsingEIServerMasterdata"))
                .put(messageSpec(WavenisDeviceMessage.WavenisRestoreBubbleUpDatabase), new SimpleTagMessageEntry("WavenisRestoreBubbleUpDatabase"))
                .put(messageSpec(WavenisDeviceMessage.WavenisRestoreLocalFromEIServer), new SimpleTagMessageEntry("WavenisRestoreLocalFromEIServer"))
                .put(messageSpec(WavenisDeviceMessage.WavenisRestoreRootDatabaseFromLocal), new SimpleTagMessageEntry("WavenisRestoreRootDatabaseFromLocal"))
                .put(messageSpec(WavenisDeviceMessage.WavenisResynchronizeModule), new SimpleValueMessageEntry("WavenisResynchronizeModule"))
                .put(messageSpec(WavenisDeviceMessage.WavenisFreeRequestResponse), new SimpleValueMessageEntry("WavenisFreeRequestResponse"))
                .put(messageSpec(WavenisDeviceMessage.WavenisSetRunLevelIdle), new SimpleTagMessageEntry("WavenisSetRunLevelIdle"))
                .put(messageSpec(WavenisDeviceMessage.WavenisSetRunLevelInit), new SimpleTagMessageEntry("WavenisSetRunLevelInit"))
                .put(messageSpec(WavenisDeviceMessage.WavenisSetRunLevelRun), new SimpleTagMessageEntry("WavenisSetRunLevelRun"))
                .put(messageSpec(WavenisDeviceMessage.WavenisSetFriendlyName), new SimpleValueMessageEntry("WavenisSetFriendlyName"))
                .put(messageSpec(WavenisDeviceMessage.WavenisSetL1PreferredList), new SimpleValueMessageEntry("WavenisSetL1PreferredList"))
                .put(messageSpec(WavenisDeviceMessage.WavenisSynchronizeModule), new SimpleValueMessageEntry("WavenisSynchronizeModule"))
                .put(messageSpec(WavenisDeviceMessage.WavenisUpdateEIServerMasterdataUsingLocalDatabases), new SimpleTagMessageEntry("WavenisUpdateEIServerMasterdataUsingLocalDatabases"))
                .put(messageSpec(FirmwareDeviceMessage.UpgradeWaveCard), new SimpleValueMessageEntry("UpgradeWaveCard"))
                .put(messageSpec(WavenisDeviceMessage.WavenisEnableDisable), new SimpleValueMessageEntry("WavenisEnableDisable"))
                .build();
    }
}