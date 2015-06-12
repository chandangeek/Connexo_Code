package com.energyict.protocolimplv2.messages.convertor;

import com.energyict.cbo.Password;
import com.energyict.cpo.PropertySpec;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdw.core.UserFile;
import com.energyict.protocolimplv2.messages.*;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.eiweb.SimpleEIWebMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.eiwebplus.ForceMessageToFailedMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.eiwebplus.IDISDiscoveryConfigurationMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.eiwebplus.IDISRepeaterCallConfigurationMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleAttributeMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.MultipleInnerTagsMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleTagMessageEntry;
import com.energyict.protocolimplv2.messages.convertor.messageentrycreators.general.SimpleValueMessageEntry;

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
     * Represents a mapping between {@link com.energyict.mdc.messages.DeviceMessageSpec deviceMessageSpecs}
     * and the corresponding {@link com.energyict.protocolimplv2.messages.convertor.MessageEntryCreator}
     */
    private static Map<DeviceMessageSpec, MessageEntryCreator> registry = new HashMap<>();

    static {

        // Digital output switch rules
        registry.put(OutputConfigurationMessage.AbsoluteDOSwitchRule, new MultipleAttributeMessageEntry("DOSwitchRule", DeviceMessageConstants.id, DeviceMessageConstants.startTime, DeviceMessageConstants.endTime, DeviceMessageConstants.outputBitMap));
        registry.put(OutputConfigurationMessage.DeleteDOSwitchRule, new MultipleAttributeMessageEntry("DOSwitchRule", DeviceMessageConstants.id, DeviceMessageConstants.delete));
        registry.put(OutputConfigurationMessage.RelativeDOSwitchRule, new MultipleAttributeMessageEntry("DOSwitchRule", DeviceMessageConstants.id, DeviceMessageConstants.duration, DeviceMessageConstants.outputBitMap));

        // Firewall messages
        registry.put(ConfigurationChangeDeviceMessage.EnableFW, new SimpleTagMessageEntry("EnableFW"));
        registry.put(ConfigurationChangeDeviceMessage.DisableFW, new SimpleTagMessageEntry("DisableFW"));

        // General Commands
        registry.put(DeviceActionMessage.RtuPlusServerEnterMaintenanceMode, new SimpleTagMessageEntry("RtuPlusServerEnterMaintenanceMode"));
        registry.put(DeviceActionMessage.RtuPlusServerExitMaintenanceMode, new SimpleTagMessageEntry("RtuPlusServerExitMaintenanceMode"));
        registry.put(DeviceActionMessage.ForceMessageToFailed, new ForceMessageToFailedMessageEntry());
        registry.put(ClockDeviceMessage.FTIONForceTimeSync, new SimpleTagMessageEntry("FTIONForceTimeSync"));
        registry.put(DeviceActionMessage.FTIONInitDatabaseKeepConfig, new SimpleTagMessageEntry("FTIONInitDatabaseKeepConfig"));
        registry.put(DeviceActionMessage.FTIONReboot, new SimpleTagMessageEntry("FTIONReboot"));
        registry.put(DeviceActionMessage.FTIONRestart, new SimpleTagMessageEntry("FTIONRestart"));
        registry.put(DeviceActionMessage.FTIONScanBus, new SimpleTagMessageEntry("FTIONScanBus"));
        registry.put(DeviceActionMessage.SyncMasterdata, new SimpleTagMessageEntry("SyncMasterdata"));
        registry.put(FirmwareDeviceMessage.FTIONUpgradeRFMeshFirmware, new SimpleTagMessageEntry("FTIONUpgradeRFMeshFirmware"));
        registry.put(DeviceActionMessage.FTIONUpgrade, new SimpleTagMessageEntry("FTIONUpgrade"));
        registry.put(DeviceActionMessage.FTIONUpgradeAndInit, new SimpleTagMessageEntry("FTIONUpgradeAndInit"));
        registry.put(DeviceActionMessage.FTIONUpgradeAndInitWithNewEIServerURL, new SimpleValueMessageEntry("FTIONUpgradeAndInitWithNewEIServerURL"));
        registry.put(DeviceActionMessage.FTIONUpgradeWithNewEIServerURL, new SimpleValueMessageEntry("FTIONUpgradeWithNewEIServerURL"));
        registry.put(FirmwareDeviceMessage.UpgradeBootloader, new SimpleValueMessageEntry("UpgradeBootloader"));

        // General Parameters
        registry.put(SecurityMessage.CHANGE_PASSWORD_WITH_NEW_PASSWORD, new SimpleValueMessageEntry("Password"));
        registry.put(FirmwareDeviceMessage.RFMeshUpgradeURL, new SimpleValueMessageEntry("RFMeshUpgradeURL"));
        registry.put(ConfigurationChangeDeviceMessage.WhitelistedPhoneNumbers, new SimpleValueMessageEntry("WhitelistedPhoneNumbers"));
        registry.put(ConfigurationChangeDeviceMessage.BootSyncEnable, new SimpleValueMessageEntry("BootSyncEnable"));
        registry.put(EIWebConfigurationDeviceMessage.UpdateEIWebSSLCertificate, new SimpleValueMessageEntry("UpdateEIWebSSLCertificate"));
        registry.put(ConfigurationChangeDeviceMessage.SetUpgradeUrl, new SimpleEIWebMessageEntry());

        //IDIS stuff
        registry.put(PLCConfigurationDeviceMessage.IDISDiscoveryConfiguration, new IDISDiscoveryConfigurationMessageEntry());
        registry.put(PLCConfigurationDeviceMessage.IDISRepeaterCallConfiguration, new IDISRepeaterCallConfigurationMessageEntry());
        registry.put(PLCConfigurationDeviceMessage.IDISRunRepeaterCallNow, new SimpleTagMessageEntry("IDISRunRepeaterCallNow"));
        registry.put(PLCConfigurationDeviceMessage.IDISRunNewMeterDiscoveryCallNow, new SimpleTagMessageEntry("IDISRunNewMeterDiscoveryCallNow"));
        registry.put(PLCConfigurationDeviceMessage.IDISRunAlarmDiscoveryCallNow, new SimpleTagMessageEntry("IDISRunAlarmDiscoveryCallNow"));
        registry.put(PLCConfigurationDeviceMessage.IDISWhitelistConfiguration, new MultipleInnerTagsMessageEntry("IDISWhitelistConfiguration", "Enabled (true/false)", "Group Name (the group containing the meters included in the whitelist)"));
        registry.put(PLCConfigurationDeviceMessage.IDISOperatingWindowConfiguration, new MultipleInnerTagsMessageEntry("IDISOperatingWindowConfiguration", "Enabled (true/false)", "Start time (MM:SS) (GMT)", "End time (MM:SS) (GMT)"));
        registry.put(PLCConfigurationDeviceMessage.IDISPhyConfiguration, new MultipleInnerTagsMessageEntry("IDISPhyConfiguration", "Bit sync", "Zero cross adjust", "TX gain", "RX gain"));
        registry.put(PLCConfigurationDeviceMessage.IDISCreditManagementConfiguration, new MultipleInnerTagsMessageEntry("IDISCreditManagementConfiguration", "Add credit", "Min credit"));

        //Logging
        registry.put(LoggingConfigurationDeviceMessage.DownloadFile, new SimpleValueMessageEntry("DownloadFile"));
        registry.put(LoggingConfigurationDeviceMessage.PushConfiguration, new SimpleTagMessageEntry("PushConfiguration"));
        registry.put(LoggingConfigurationDeviceMessage.PushLogsNow, new SimpleTagMessageEntry("PushLogsNow"));

        //Output
        registry.put(OutputConfigurationMessage.OutputOff, new SimpleValueMessageEntry("OutputOff"));
        registry.put(OutputConfigurationMessage.OutputOn, new SimpleValueMessageEntry("OutputOn"));

        //PLC control
        registry.put(PLCConfigurationDeviceMessage.PLCEnableDisable, new SimpleValueMessageEntry("PLCEnableDisable"));
        registry.put(PLCConfigurationDeviceMessage.PLCFreqPairSelection, new SimpleValueMessageEntry("PLCFreqPairSelection"));
        registry.put(PLCConfigurationDeviceMessage.PLCRequestConfig, new SimpleTagMessageEntry("PLCRequestConfig"));
        registry.put(PLCConfigurationDeviceMessage.CIASEDiscoveryMaxCredits, new SimpleValueMessageEntry("CIASEDiscoveryMaxCredits"));
        registry.put(PLCConfigurationDeviceMessage.PLCChangeMacAddress, new SimpleValueMessageEntry("PLCChangeMacAddress"));

        //PLC Prime control
        registry.put(PLCConfigurationDeviceMessage.PLCPrimeCancelFirmwareUpgrade, new SimpleTagMessageEntry("PLCPrimeCancelFirmwareUpgrade"));
        registry.put(PLCConfigurationDeviceMessage.PLCPrimeReadPIB, new SimpleValueMessageEntry("PLCPrimeReadPIB"));
        registry.put(PLCConfigurationDeviceMessage.PLCPrimeRequestFirmwareVersion, new SimpleValueMessageEntry("PLCPrimeRequestFirmwareVersion"));
        registry.put(FirmwareDeviceMessage.PLCPrimeStartFirmwareUpgradeNodeList, new SimpleValueMessageEntry("PLCPrimeStartFirmwareUpgradeNodeList"));
        registry.put(FirmwareDeviceMessage.PLCPrimeSetFirmwareUpgradeFile, new SimpleValueMessageEntry("PLCPrimeSetFirmwareUpgradeFile"));
        registry.put(PLCConfigurationDeviceMessage.PLCPrimeWritePIB, new SimpleValueMessageEntry("PLCPrimeWritePIB"));

        //Wavenis stuff
        registry.put(WavenisDeviceMessage.WavenisAddAddressGetNetworkId, new SimpleValueMessageEntry("WavenisAddAddressGetNetworkId"));
        registry.put(WavenisDeviceMessage.WavenisAddAddressWithNetworkId, new SimpleValueMessageEntry("WavenisAddAddressWithNetworkId"));
        registry.put(WavenisDeviceMessage.WavenisBranchMove, new SimpleValueMessageEntry("WavenisBranchMove"));
        registry.put(WavenisDeviceMessage.WavenisChangeMasterAddress, new SimpleValueMessageEntry("WavenisChangeMasterAddress"));
        registry.put(WavenisDeviceMessage.WavenisCompareRepaireDatabases, new SimpleTagMessageEntry("WavenisCompareRepaireDatabases"));
        registry.put(WavenisDeviceMessage.WavenisDeleteAddress, new SimpleValueMessageEntry("WavenisDeleteAddress"));
        registry.put(WavenisDeviceMessage.WavenisInitBubbleUpSlotDatabase, new SimpleTagMessageEntry("WavenisInitBubbleUpSlotDatabase"));
        registry.put(WavenisDeviceMessage.WavenisInitDatabases, new SimpleTagMessageEntry("WavenisInitDatabases"));
        registry.put(WavenisDeviceMessage.WavenisProgramRadioAddress, new SimpleValueMessageEntry("WavenisProgramRadioAddress"));
        registry.put(WavenisDeviceMessage.WavenisRemoveBubbleUpSlot, new SimpleValueMessageEntry("WavenisRemoveBubbleUpSlot"));
        registry.put(WavenisDeviceMessage.WavenisRequestBubbleUpSlot, new SimpleValueMessageEntry("WavenisRequestBubbleUpSlot"));
        registry.put(WavenisDeviceMessage.WavenisRequestModuleStatus, new SimpleValueMessageEntry("WavenisRequestModuleStatus"));
        registry.put(WavenisDeviceMessage.WaveCardRadioAddress, new SimpleTagMessageEntry("WaveCardRadioAddress"));
        registry.put(WavenisDeviceMessage.WavenisRestoreDatabasesUsingEIServerMasterdata, new SimpleTagMessageEntry("WavenisRestoreDatabasesUsingEIServerMasterdata"));
        registry.put(WavenisDeviceMessage.WavenisRestoreBubbleUpDatabase, new SimpleTagMessageEntry("WavenisRestoreBubbleUpDatabase"));
        registry.put(WavenisDeviceMessage.WavenisRestoreLocalFromEIServer, new SimpleTagMessageEntry("WavenisRestoreLocalFromEIServer"));
        registry.put(WavenisDeviceMessage.WavenisRestoreRootDatabaseFromLocal, new SimpleTagMessageEntry("WavenisRestoreRootDatabaseFromLocal"));
        registry.put(WavenisDeviceMessage.WavenisResynchronizeModule, new SimpleValueMessageEntry("WavenisResynchronizeModule"));
        registry.put(WavenisDeviceMessage.WavenisFreeRequestResponse, new SimpleValueMessageEntry("WavenisFreeRequestResponse"));
        registry.put(WavenisDeviceMessage.WavenisSetRunLevelIdle, new SimpleTagMessageEntry("WavenisSetRunLevelIdle"));
        registry.put(WavenisDeviceMessage.WavenisSetRunLevelInit, new SimpleTagMessageEntry("WavenisSetRunLevelInit"));
        registry.put(WavenisDeviceMessage.WavenisSetRunLevelRun, new SimpleTagMessageEntry("WavenisSetRunLevelRun"));
        registry.put(WavenisDeviceMessage.WavenisSetFriendlyName, new SimpleValueMessageEntry("WavenisSetFriendlyName"));
        registry.put(WavenisDeviceMessage.WavenisSetL1PreferredList, new SimpleValueMessageEntry("WavenisSetL1PreferredList"));
        registry.put(WavenisDeviceMessage.WavenisSynchronizeModule, new SimpleValueMessageEntry("WavenisSynchronizeModule"));
        registry.put(WavenisDeviceMessage.WavenisUpdateEIServerMasterdataUsingLocalDatabases, new SimpleTagMessageEntry("WavenisUpdateEIServerMasterdataUsingLocalDatabases"));
        registry.put(FirmwareDeviceMessage.UpgradeWaveCard, new SimpleValueMessageEntry("UpgradeWaveCard"));
        registry.put(WavenisDeviceMessage.WavenisEnableDisable, new SimpleValueMessageEntry("WavenisEnableDisable"));

    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getName().equals(DeviceMessageConstants.waveCardFirmware)
                || propertySpec.getName().equals(DeviceMessageConstants.sslCertificateUserFile)
                || propertySpec.getName().equals(DeviceMessageConstants.firmwareUpdateUserFileAttributeName)
                || propertySpec.getName().equals(DeviceMessageConstants.PricingInformationUserFileAttributeName)
                || propertySpec.getName().equals(DeviceMessageConstants.nodeListUserFile)) {
            try {
                return new String(((UserFile) messageAttribute).loadFileInByteArray(), "US-ASCII");
            } catch (UnsupportedEncodingException e) {
                return new String(((UserFile) messageAttribute).loadFileInByteArray());
            }
        } else if (propertySpec.getName().equals(DeviceMessageConstants.newPasswordAttributeName)) {
            return ((Password) messageAttribute).getValue();
        }

        return messageAttribute.toString();
    }

    protected Map<DeviceMessageSpec, MessageEntryCreator> getRegistry() {
        return registry;
    }
}