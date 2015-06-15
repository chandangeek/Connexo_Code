package com.energyict.protocolimplv2.messages.convertor;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.Password;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.protocol.api.UserFile;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.protocolimpl.generic.messages.GenericMessaging;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * List of all supported messages for the RTU+Server using the EIWeb+ servlet
 * Copyrights EnergyICT
 * Date: 8/03/13
 * Time: 16:26
 */
public class EIWebPlusMessageConverter extends AbstractMessageConverter {

    /**
     * Represents a mapping between DeviceMessageSpec
     * and the corresponding {@link com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator}
     */
    private static Map<DeviceMessageId, MessageEntryCreator> registry = new HashMap<>();

    static {

        //TODO Complete according to connexo specs!
//
//        // Digital output switch rules
//        registry.put(DeviceMessageId.OUTPUT_CONFIGURATION_ABSOLUTE_DO_SWITCH_RULE, new MultipleAttributeMessageEntry("DOSwitchRule", DeviceMessageConstants.id, DeviceMessageConstants.startTime, DeviceMessageConstants.endTime, DeviceMessageConstants.outputBitMap));
//        registry.put(DeviceMessageId.OUTPUT_CONFIGURATION_DELETE_DO_SWITCH_RULE, new MultipleAttributeMessageEntry("DOSwitchRule", DeviceMessageConstants.id, DeviceMessageConstants.delete));
//        registry.put(DeviceMessageId.OUTPUT_CONFIGURATION_RELATIVE_DO_SWITCH_RULE, new MultipleAttributeMessageEntry("DOSwitchRule", DeviceMessageConstants.id, DeviceMessageConstants.duration, DeviceMessageConstants.outputBitMap));
//
//        // Firewall messages
//        registry.put(DeviceMessageId.CONFIGURATION_CHANGE_ENABLE_FW, new SimpleTagMessageEntry("EnableFW"));
//        registry.put(DeviceMessageId.CONFIGURATION_CHANGE_DISABLE_FW, new SimpleTagMessageEntry("DisableFW"));
//
//        // General Commands
//        registry.put(DeviceMessageId.DEVICE_ACTIONS_RTU_PLUS_SERVER_ENTER_MAINTENANCE_MODE, new SimpleTagMessageEntry("RtuPlusServerEnterMaintenanceMode"));
//        registry.put(DeviceMessageId.DEVICE_ACTIONS_RTU_PLUS_SERVER_EXIT_MAINTENANCE_MODE, new SimpleTagMessageEntry("RtuPlusServerExitMaintenanceMode"));
//        registry.put(DeviceMessageId.DEVICE_ACTIONS_FORCE_MESSAGE_TO_FAILED, new ForceMessageToFailedMessageEntry());
//        registry.put(ClockDeviceMessage.FTIONForceTimeSync, new SimpleTagMessageEntry("FTIONForceTimeSync"));
//        registry.put(DeviceMessageId.DEVICE_ACTIONS_FTION_INIT_DATABASE_KEEP_CONFIG, new SimpleTagMessageEntry("FTIONInitDatabaseKeepConfig"));
//        registry.put(DeviceMessageId.DEVICE_ACTIONS_FTION_REBOOT, new SimpleTagMessageEntry("FTIONReboot"));
//        registry.put(DeviceMessageId.DEVICE_ACTIONS_FTION_RESTART, new SimpleTagMessageEntry("FTIONRestart"));
//        registry.put(DeviceMessageId.DEVICE_ACTIONS_FTION_SCAN_BUS, new SimpleTagMessageEntry("FTIONScanBus"));
//        registry.put(DeviceMessageId.DEVICE_ACTIONS_SYNC_MASTERDATA, new SimpleTagMessageEntry("SyncMasterdata"));
//        registry.put(FirmwareDeviceMessage.FTIONUpgradeRFMeshFirmware, new SimpleTagMessageEntry("FTIONUpgradeRFMeshFirmware"));
//        registry.put(DeviceMessageId.DEVICE_ACTIONS_FTION_UPGRADE, new SimpleTagMessageEntry("FTIONUpgrade"));
//        registry.put(DeviceMessageId.DEVICE_ACTIONS_FTION_UPGRADE_AND_INIT, new SimpleTagMessageEntry("FTIONUpgradeAndInit"));
//        registry.put(DeviceMessageId.DEVICE_ACTIONS_FTION_UPGRADE_AND_INIT_WITH_NEW_EISERVER_URL, new SimpleValueMessageEntry("FTIONUpgradeAndInitWithNewEIServerURL"));
//        registry.put(DeviceMessageId.DEVICE_ACTIONS_FTION_UPGRADE_WITH_NEW_EISERVER_URL, new SimpleValueMessageEntry("FTIONUpgradeWithNewEIServerURL"));
//        registry.put(FirmwareDeviceMessage.UpgradeBootloader, new SimpleValueMessageEntry("UpgradeBootloader"));
//
//        // General Parameters
//        registry.put(SecurityMessage.CHANGE_PASSWORD_WITH_NEW_PASSWORD, new SimpleValueMessageEntry("Password"));
//        registry.put(FirmwareDeviceMessage.RFMeshUpgradeURL, new SimpleValueMessageEntry("RFMeshUpgradeURL"));
//        registry.put(DeviceMessageId.CONFIGURATION_CHANGE.WhitelistedPhoneNumbers, new SimpleValueMessageEntry("WhitelistedPhoneNumbers"));
//        registry.put(DeviceMessageId.CONFIGURATION_CHANGE.BootSyncEnable, new SimpleValueMessageEntry("BootSyncEnable"));
//        registry.put(EIWebConfigurationDeviceMessage.UpdateEIWebSSLCertificate, new SimpleValueMessageEntry("UpdateEIWebSSLCertificate"));
//        registry.put(DeviceMessageId.CONFIGURATION_CHANGE.SetUpgradeUrl, new SimpleEIWebMessageEntry());
//
//        //IDIS stuff
//        registry.put(PLCConfigurationDeviceMessage.IDISDiscoveryConfiguration, new IDISDiscoveryConfigurationMessageEntry());
//        registry.put(PLCConfigurationDeviceMessage.IDISRepeaterCallConfiguration, new IDISRepeaterCallConfigurationMessageEntry());
//
//        //Logging
//        registry.put(LoggingConfigurationDeviceMessage.DownloadFile, new SimpleValueMessageEntry("DownloadFile"));
//        registry.put(LoggingConfigurationDeviceMessage.PushConfiguration, new SimpleTagMessageEntry("PushConfiguration"));
//        registry.put(LoggingConfigurationDeviceMessage.PushLogsNow, new SimpleTagMessageEntry("PushLogsNow"));
//
//        //Output
//        registry.put(OutputConfigurationMessage.OutputOff, new SimpleValueMessageEntry("OutputOff"));
//        registry.put(OutputConfigurationMessage.OutputOn, new SimpleValueMessageEntry("OutputOn"));
//
//        //PLC control
//        registry.put(PLCConfigurationDeviceMessage.PLCEnableDisable, new SimpleValueMessageEntry("PLCEnableDisable"));
//        registry.put(PLCConfigurationDeviceMessage.PLCFreqPairSelection, new SimpleValueMessageEntry("PLCFreqPairSelection"));
//        registry.put(PLCConfigurationDeviceMessage.PLCRequestConfig, new SimpleTagMessageEntry("PLCRequestConfig"));
//        registry.put(PLCConfigurationDeviceMessage.CIASEDiscoveryMaxCredits, new SimpleValueMessageEntry("CIASEDiscoveryMaxCredits"));
//        registry.put(PLCConfigurationDeviceMessage.PLCChangeMacAddress, new SimpleValueMessageEntry("PLCChangeMacAddress"));
//
//        //PLC Prime control
//        registry.put(PLCConfigurationDeviceMessage.PLCPrimeCancelFirmwareUpgrade, new SimpleTagMessageEntry("PLCPrimeCancelFirmwareUpgrade"));
//        registry.put(PLCConfigurationDeviceMessage.PLCPrimeReadPIB, new SimpleValueMessageEntry("PLCPrimeReadPIB"));
//        registry.put(PLCConfigurationDeviceMessage.PLCPrimeRequestFirmwareVersion, new SimpleValueMessageEntry("PLCPrimeRequestFirmwareVersion"));
//        registry.put(FirmwareDeviceMessage.PLCPrimeStartFirmwareUpgradeNodeList, new SimpleValueMessageEntry("PLCPrimeStartFirmwareUpgradeNodeList"));
//        registry.put(FirmwareDeviceMessage.PLCPrimeSetFirmwareUpgradeFile, new SimpleValueMessageEntry("PLCPrimeSetFirmwareUpgradeFile"));
//        registry.put(PLCConfigurationDeviceMessage.PLCPrimeWritePIB, new SimpleValueMessageEntry("PLCPrimeWritePIB"));
//
//        //Wavenis stuff
//        registry.put(WavenisDeviceMessage.WavenisAddAddressGetNetworkId, new SimpleValueMessageEntry("WavenisAddAddressGetNetworkId"));
//        registry.put(WavenisDeviceMessage.WavenisAddAddressWithNetworkId, new SimpleValueMessageEntry("WavenisAddAddressWithNetworkId"));
//        registry.put(WavenisDeviceMessage.WavenisBranchMove, new SimpleValueMessageEntry("WavenisBranchMove"));
//        registry.put(WavenisDeviceMessage.WavenisChangeMasterAddress, new SimpleValueMessageEntry("WavenisChangeMasterAddress"));
//        registry.put(WavenisDeviceMessage.WavenisCompareRepaireDatabases, new SimpleTagMessageEntry("WavenisCompareRepaireDatabases"));
//        registry.put(WavenisDeviceMessage.WavenisDeleteAddress, new SimpleValueMessageEntry("WavenisDeleteAddress"));
//        registry.put(WavenisDeviceMessage.WavenisInitBubbleUpSlotDatabase, new SimpleTagMessageEntry("WavenisInitBubbleUpSlotDatabase"));
//        registry.put(WavenisDeviceMessage.WavenisInitDatabases, new SimpleTagMessageEntry("WavenisInitDatabases"));
//        registry.put(WavenisDeviceMessage.WavenisProgramRadioAddress, new SimpleValueMessageEntry("WavenisProgramRadioAddress"));
//        registry.put(WavenisDeviceMessage.WavenisRemoveBubbleUpSlot, new SimpleValueMessageEntry("WavenisRemoveBubbleUpSlot"));
//        registry.put(WavenisDeviceMessage.WavenisRequestBubbleUpSlot, new SimpleValueMessageEntry("WavenisRequestBubbleUpSlot"));
//        registry.put(WavenisDeviceMessage.WavenisRequestModuleStatus, new SimpleValueMessageEntry("WavenisRequestModuleStatus"));
//        registry.put(WavenisDeviceMessage.WaveCardRadioAddress, new SimpleTagMessageEntry("WaveCardRadioAddress"));
//        registry.put(WavenisDeviceMessage.WavenisRestoreDatabasesUsingEIServerMasterdata, new SimpleTagMessageEntry("WavenisRestoreDatabasesUsingEIServerMasterdata"));
//        registry.put(WavenisDeviceMessage.WavenisRestoreBubbleUpDatabase, new SimpleTagMessageEntry("WavenisRestoreBubbleUpDatabase"));
//        registry.put(WavenisDeviceMessage.WavenisRestoreLocalFromEIServer, new SimpleTagMessageEntry("WavenisRestoreLocalFromEIServer"));
//        registry.put(WavenisDeviceMessage.WavenisRestoreRootDatabaseFromLocal, new SimpleTagMessageEntry("WavenisRestoreRootDatabaseFromLocal"));
//        registry.put(WavenisDeviceMessage.WavenisResynchronizeModule, new SimpleValueMessageEntry("WavenisResynchronizeModule"));
//        registry.put(WavenisDeviceMessage.WavenisFreeRequestResponse, new SimpleValueMessageEntry("WavenisFreeRequestResponse"));
//        registry.put(WavenisDeviceMessage.WavenisSetRunLevelIdle, new SimpleTagMessageEntry("WavenisSetRunLevelIdle"));
//        registry.put(WavenisDeviceMessage.WavenisSetRunLevelInit, new SimpleTagMessageEntry("WavenisSetRunLevelInit"));
//        registry.put(WavenisDeviceMessage.WavenisSetRunLevelRun, new SimpleTagMessageEntry("WavenisSetRunLevelRun"));
//        registry.put(WavenisDeviceMessage.WavenisSetFriendlyName, new SimpleValueMessageEntry("WavenisSetFriendlyName"));
//        registry.put(WavenisDeviceMessage.WavenisSetL1PreferredList, new SimpleValueMessageEntry("WavenisSetL1PreferredList"));
//        registry.put(WavenisDeviceMessage.WavenisSynchronizeModule, new SimpleValueMessageEntry("WavenisSynchronizeModule"));
//        registry.put(WavenisDeviceMessage.WavenisUpdateEIServerMasterdataUsingLocalDatabases, new SimpleTagMessageEntry("WavenisUpdateEIServerMasterdataUsingLocalDatabases"));
//        registry.put(FirmwareDeviceMessage.UpgradeWaveCard, new SimpleValueMessageEntry("UpgradeWaveCard"));
//        registry.put(WavenisDeviceMessage.WavenisEnableDisable, new SimpleValueMessageEntry("WavenisEnableDisable"));

    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getName().equals(DeviceMessageConstants.sslCertificateUserFile)
                || propertySpec.getName().equals(DeviceMessageConstants.PricingInformationUserFileAttributeName)
                || propertySpec.getName().equals(DeviceMessageConstants.nodeListUserFile)) {
            try {
                return new String(((UserFile) messageAttribute).loadFileInByteArray(), "US-ASCII");
            } catch (UnsupportedEncodingException e) {
                return new String(((UserFile) messageAttribute).loadFileInByteArray());
            }
        } else if (propertySpec.getName().equals(DeviceMessageConstants.waveCardFirmware) || propertySpec.getName().equals(DeviceMessageConstants.firmwareUpdateFileAttributeName)) {
            FirmwareVersion firmwareVersion = ((FirmwareVersion) messageAttribute);
            return GenericMessaging.zipAndB64EncodeContent(firmwareVersion.getFirmwareFile());  //Bytes of the firmwareFile as string
        } else if (propertySpec.getName().equals(DeviceMessageConstants.newPasswordAttributeName)) {
            return ((Password) messageAttribute).getValue();
        }

        return messageAttribute.toString();
    }

    protected Map<DeviceMessageId, MessageEntryCreator> getRegistry() {
        return registry;
    }
}