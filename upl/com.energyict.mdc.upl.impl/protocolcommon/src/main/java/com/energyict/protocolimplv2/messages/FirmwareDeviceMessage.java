package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.ProtocolSupportedFirmwareOptions;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.DeviceGroup;
import com.energyict.mdc.upl.properties.FirmwareVersion;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilder;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocolimplv2.messages.enums.DlmsEncryptionLevelMessageValues;
import com.energyict.protocolimplv2.messages.nls.TranslationKeyImpl;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.BlocksPerCycle;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.BlocksPerCycleDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.BroadcastClientWPort;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.BroadcastClientWPortDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.DelayAfterLastBlock;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.DelayAfterLastBlockDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.DelayBetweenBlockSentFast;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.DelayBetweenBlockSentFastDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.DelayBetweenBlockSentSlow;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.DelayBetweenBlockSentSlowDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.DelayPerBlock;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.DelayPerBlockDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.LogicalDeviceLSap;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.LogicalDeviceLSapDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.MaxCycles;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.MaxCyclesDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.MeterTimeZone;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.MeterTimeZoneDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.MulticastClientWPort;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.MulticastClientWPortDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.PadLastBlock;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.PadLastBlockDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.RequestedBlockSize;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.RequestedBlockSizeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.SecurityLevelBroadcast;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.SecurityLevelBroadcastDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.SecurityLevelMulticast;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.SecurityLevelMulticastDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.SecurityLevelUnicast;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.SecurityLevelUnicastDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.SecurityPolicyBroadcast;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.SecurityPolicyBroadcastDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.SecurityPolicyMulticastV0;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.SecurityPolicyMulticastV0DefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.SkipStepActivate;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.SkipStepActivateDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.SkipStepEnable;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.SkipStepEnableDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.SkipStepVerify;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.SkipStepVerifyDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.UnicastClientWPort;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.UnicastClientWPortDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.UnicastFrameCounterType;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.UnicastFrameCounterTypeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.UseTransferredBlockStatus;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.UseTransferredBlockStatusDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.broadcastAuthenticationKeyAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.broadcastAuthenticationKeyAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.broadcastClientMacAddressAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.broadcastClientMacAddressAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.broadcastEncryptionKeyAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.broadcastEncryptionKeyAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.broadcastGroupIdAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.broadcastGroupIdAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.broadcastInitialTimeBetweenBlocksAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.broadcastInitialTimeBetweenBlocksAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.broadcastLogicalDeviceIdAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.broadcastLogicalDeviceIdAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.broadcastNumberOfBlocksInCycleAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.broadcastNumberOfBlocksInCycleAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.deviceIdsAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.deviceIdsAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateActivationDateAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateImageIdentifierAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateImageIdentifierAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateURLAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateURLAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateUserFileAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateUserFileAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateVersionNumberAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.firmwareUpdateVersionNumberAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.plcTypeFirmwareUpdateAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.plcTypeFirmwareUpdateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.resumeFirmwareUpdateAttributeDefaultTranslation;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.resumeFirmwareUpdateAttributeName;

/**
 * Provides a summary of all <i>Firmware</i> related messages.
 * <p>
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum FirmwareDeviceMessage implements DeviceMessageSpecSupplier {

    UPGRADE_FIRMWARE_WITH_USER_FILE(5001, "Firmware upgrade via user file") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.firmwareVersionSpec(service, firmwareUpdateUserFileAttributeName, firmwareUpdateUserFileAttributeDefaultTranslation));
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_LATER);
        }
    },
    UPGRADE_FIRMWARE_WITH_USER_FILE_AND_RESUME_OPTION(5002, "Firmware upgrade via user file with resume option") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.firmwareVersionSpec(service, firmwareUpdateUserFileAttributeName, firmwareUpdateUserFileAttributeDefaultTranslation),
                    this.booleanSpec(service, resumeFirmwareUpdateAttributeName, resumeFirmwareUpdateAttributeDefaultTranslation, Boolean.TRUE)
            );
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE);
        }
    },
    UPGRADE_FIRMWARE_WITH_USER_FILE_AND_RESUME_OPTION_AND_TYPE(5003, "Firmware upgrade via user file with resume option and type") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.firmwareVersionSpec(service, firmwareUpdateUserFileAttributeName, firmwareUpdateUserFileAttributeDefaultTranslation),
                    this.booleanSpec(service, resumeFirmwareUpdateAttributeName, resumeFirmwareUpdateAttributeDefaultTranslation, Boolean.TRUE),
                    this.booleanSpec(service, plcTypeFirmwareUpdateAttributeName, plcTypeFirmwareUpdateAttributeDefaultTranslation)
            );
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE);
        }
    },
    UPGRADE_FIRMWARE_ACTIVATE(5004, "Active last uploaded firmware") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.dateTimeSpec(service, firmwareUpdateActivationDateAttributeName, firmwareUpdateActivationDateAttributeDefaultTranslation));
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.empty();
        }
    },
    UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE(5005, "Firmware upgrade via user file with activation date") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.firmwareVersionSpec(service, firmwareUpdateUserFileAttributeName, firmwareUpdateUserFileAttributeDefaultTranslation),
                    this.dateTimeSpec(service, firmwareUpdateActivationDateAttributeName, firmwareUpdateActivationDateAttributeDefaultTranslation)
            );
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE);
        }
    },
    UPGRADE_FIRMWARE_WITH_USER_FILE_VERSION_AND_ACTIVATE(5006, "Firmware upgrade via user file with version and activation date") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.firmwareVersionSpec(service, firmwareUpdateUserFileAttributeName, firmwareUpdateUserFileAttributeDefaultTranslation),
                    this.dateTimeSpec(service, firmwareUpdateActivationDateAttributeName, firmwareUpdateActivationDateAttributeDefaultTranslation),
                    this.stringSpec(service, firmwareUpdateVersionNumberAttributeName, firmwareUpdateVersionNumberAttributeDefaultTranslation)
            );
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE);
        }
    },
    UPGRADE_FIRMWARE_URL(5007, "Firwmare upgrade via url") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, firmwareUpdateURLAttributeName, firmwareUpdateURLAttributeDefaultTranslation));
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE);
        }
    },
    UPGRADE_FIRMWARE_URL_AND_ACTIVATE(5008, "Firwmare upgrade via url with activation date") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, firmwareUpdateURLAttributeName, firmwareUpdateURLAttributeDefaultTranslation),
                    this.dateTimeSpec(service, firmwareUpdateActivationDateAttributeName, firmwareUpdateActivationDateAttributeDefaultTranslation)
            );
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE);
        }
    },
    UPGRADE_FIRMWARE_WITH_USER_FILE_ACTIVATE_IMMEDIATE(5009, "Upload firmware and activate immediately") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.firmwareVersionSpec(service, firmwareUpdateUserFileAttributeName, firmwareUpdateUserFileAttributeDefaultTranslation));
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE);
        }
    },
    UpgradeWaveCard(5010, "Upgrade the Wavecard firmware") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.firmwareVersionSpec(service, DeviceMessageConstants.waveCardFirmware, DeviceMessageConstants.waveCardFirmwareDefaultTranslation));
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE);
        }
    },
    PLCPrimeSetFirmwareUpgradeFile(5011, "Upload the firmware file") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.firmwareVersionSpec(service, DeviceMessageConstants.firmwareUpdateUserFileAttributeName, DeviceMessageConstants.firmwareUpdateUserFileAttributeDefaultTranslation));
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE);
        }
    },
    PLCPrimeStartFirmwareUpgradeNodeList(5012, "Start the firmware upgrade for nodes") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.firmwareVersionSpec(service, DeviceMessageConstants.nodeListUserFile, DeviceMessageConstants.nodeListUserFileDefaultTranslation));
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE);
        }
    },
    FTIONUpgradeRFMeshFirmware(5013, "Upgrade the RF mesh firmware") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE);
        }
    },
    RFMeshUpgradeURL(5014, "Change the update URL for RF mesh") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, DeviceMessageConstants.SetUpgradeUrlAttributeName, DeviceMessageConstants.SetUpgradeUrlAttributeDefaultTranslation));
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE);
        }
    },
    UpgradeBootloader(5015, "Upgrade the boot loader") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.firmwareVersionSpec(service, DeviceMessageConstants.PricingInformationUserFileAttributeName, DeviceMessageConstants.PricingInformationUserFileAttributeDefaultTranslation));
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE);
        }
    },
    UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE_AND_IMAGE_IDENTIFIER(5016, "Upgrade firmware with user file, activation date and image identifier") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.firmwareVersionSpec(service, firmwareUpdateUserFileAttributeName, firmwareUpdateUserFileAttributeDefaultTranslation),
                    this.dateTimeSpec(service, firmwareUpdateActivationDateAttributeName, firmwareUpdateActivationDateAttributeDefaultTranslation),
                    this.stringSpec(service, firmwareUpdateImageIdentifierAttributeName, firmwareUpdateImageIdentifierAttributeDefaultTranslation)
            );
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE);
        }
    },
    UPGRADE_FIRMWARE_WITH_USER_FILE_AND_IMAGE_IDENTIFIER(5017, "Upgrade firmware with user file and image identifier") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.firmwareVersionSpec(service, firmwareUpdateUserFileAttributeName, firmwareUpdateUserFileAttributeDefaultTranslation),
                    this.stringSpec(service, firmwareUpdateImageIdentifierAttributeName, firmwareUpdateImageIdentifierAttributeDefaultTranslation)
            );
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE);
        }
    },
    BroadcastFirmwareUpgrade(5018, "Broadcast firmware upgrade") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.deviceGroupSpec(service, DeviceMessageConstants.broadcastDevicesGroupAttributeName, DeviceMessageConstants.broadcastDevicesGroupAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, broadcastLogicalDeviceIdAttributeName, broadcastLogicalDeviceIdAttributeDefaultTranslation, BigDecimal.valueOf(16)),
                    this.bigDecimalSpec(service, broadcastClientMacAddressAttributeName, broadcastClientMacAddressAttributeDefaultTranslation, BigDecimal.valueOf(102)),
                    this.bigDecimalSpec(service, broadcastGroupIdAttributeName, broadcastGroupIdAttributeDefaultTranslation, BigDecimal.ONE),   //Default group 1 is broadcast (to all devices)
                    this.bigDecimalSpec(service, broadcastNumberOfBlocksInCycleAttributeName, broadcastNumberOfBlocksInCycleAttributeDefaultTranslation, BigDecimal.valueOf(100)),
                    this.durationSpec(service, broadcastInitialTimeBetweenBlocksAttributeName, broadcastInitialTimeBetweenBlocksAttributeDefaultTranslation, Duration.ofSeconds(1)),
                    this.firmwareVersionSpec(service, firmwareUpdateUserFileAttributeName, firmwareUpdateUserFileAttributeDefaultTranslation),
                    this.stringSpec(service, firmwareUpdateImageIdentifierAttributeName, firmwareUpdateImageIdentifierAttributeDefaultTranslation),
                    this.passwordSpec(service, broadcastEncryptionKeyAttributeName, broadcastEncryptionKeyAttributeDefaultTranslation),
                    this.passwordSpec(service, broadcastAuthenticationKeyAttributeName, broadcastAuthenticationKeyAttributeDefaultTranslation),
                    this.stringSpec(
                            service,
                            DeviceMessageConstants.encryptionLevelAttributeName, DeviceMessageConstants.encryptionLevelAttributeDefaultTranslation,
                            DlmsEncryptionLevelMessageValues.getNames())
            );
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_LATER);
        }
    },
    VerifyAndActivateFirmware(5019, "Verify and activate firmware") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.empty();
        }
    },
    DataConcentratorMulticastFirmwareUpgrade(5020, "Multicast firmware upgrade (Data concentrator mode)") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, deviceIdsAttributeName, deviceIdsAttributeDefaultTranslation),
                    this.firmwareVersionSpec(service, firmwareUpdateUserFileAttributeName, firmwareUpdateUserFileAttributeDefaultTranslation),
                    this.stringSpec(service, firmwareUpdateImageIdentifierAttributeName, firmwareUpdateImageIdentifierAttributeDefaultTranslation),
                    this.bigDecimalSpecWithDefaultValue(service, UnicastClientWPort, UnicastClientWPortDefaultTranslation, BigDecimal.ONE),
                    this.bigDecimalSpecWithDefaultValue(service, BroadcastClientWPort, BroadcastClientWPortDefaultTranslation, BigDecimal.valueOf(64)),
                    this.bigDecimalSpecWithDefaultValue(service, MulticastClientWPort, MulticastClientWPortDefaultTranslation, BigDecimal.valueOf(102)),
                    this.bigDecimalSpecWithDefaultValue(service, LogicalDeviceLSap, LogicalDeviceLSapDefaultTranslation, BigDecimal.ONE),
                    this.bigDecimalSpecWithDefaultValue(service, SecurityLevelUnicast, SecurityLevelUnicastDefaultTranslation, BigDecimal.valueOf(3)),
                    this.bigDecimalSpecWithDefaultValue(service, SecurityLevelBroadcast, SecurityLevelBroadcastDefaultTranslation, BigDecimal.valueOf(3)),
                    this.bigDecimalSpecWithDefaultValue(service, SecurityPolicyBroadcast, SecurityPolicyBroadcastDefaultTranslation, BigDecimal.ZERO),
                    this.durationSpec(service, DelayAfterLastBlock, DelayAfterLastBlockDefaultTranslation, Duration.ofSeconds(5)),
                    this.durationSpec(service, DelayPerBlock, DelayPerBlockDefaultTranslation, Duration.ofSeconds(4)),
                    this.durationSpec(service, DelayBetweenBlockSentFast, DelayBetweenBlockSentFastDefaultTranslation, Duration.ofMillis(250)),
                    this.durationSpec(service, DelayBetweenBlockSentSlow, DelayBetweenBlockSentSlowDefaultTranslation, Duration.ofMillis(500)),
                    this.bigDecimalSpecWithDefaultValue(service, BlocksPerCycle, BlocksPerCycleDefaultTranslation, BigDecimal.valueOf(30)),
                    this.bigDecimalSpecWithDefaultValue(service, MaxCycles, MaxCyclesDefaultTranslation, BigDecimal.ONE),
                    this.bigDecimalSpec(service, RequestedBlockSize, RequestedBlockSizeDefaultTranslation, BigDecimal.valueOf(1024)),
                    this.booleanSpec(service, PadLastBlock, PadLastBlockDefaultTranslation, Boolean.FALSE),
                    this.booleanSpec(service, UseTransferredBlockStatus, UseTransferredBlockStatusDefaultTranslation, Boolean.TRUE)
            );
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_LATER);
        }
    },
    ReadMulticastProgress(5021, "Read DC multicast progress") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.empty();
        }
    },
    FirmwareUpgradeWithUrlJarJadFileSize(5022, "Firmware upgrade with URL") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, DeviceMessageConstants.URL_PATH, DeviceMessageConstants.URL_PATH_DEFAULT_TRANSLATION),
                    this.bigDecimalSpec(service, DeviceMessageConstants.JAR_FILE_SIZE, DeviceMessageConstants.JAR_FILE_SIZE_DEFAULT_TRANSLATION),
                    this.bigDecimalSpec(service, DeviceMessageConstants.JAD_FILE_SIZE, DeviceMessageConstants.JAD_FILE_SIZE_DEFAULT_TRANSLATION)
            );
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE);
        }
    },
    UPGRADE_FIRMWARE_WITH_USER_FILE_RESUME_AND_IMAGE_IDENTIFIER(5023, "Download and verify firmware") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.firmwareVersionSpec(service, firmwareUpdateUserFileAttributeName, firmwareUpdateUserFileAttributeDefaultTranslation),
                    this.stringSpec(service, firmwareUpdateImageIdentifierAttributeName, firmwareUpdateImageIdentifierAttributeDefaultTranslation),
                    this.booleanSpec(service, resumeFirmwareUpdateAttributeName, resumeFirmwareUpdateAttributeDefaultTranslation, Boolean.TRUE)
            );
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE);
        }
    },
    ENABLE_IMAGE_TRANSFER(5024, "Enable image transfer") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.empty();
        }
    },
    TRANSFER_SLAVE_FIRMWARE_FILE_TO_DATA_CONCENTRATOR(5025, "Transfer slave firmware file") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.firmwareVersionSpec(service, firmwareUpdateUserFileAttributeName, firmwareUpdateUserFileAttributeDefaultTranslation),
                    this.stringSpec(service, firmwareUpdateImageIdentifierAttributeName, firmwareUpdateImageIdentifierAttributeDefaultTranslation));
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE);
        }
    },
    CONFIGURE_MULTICAST_BLOCK_TRANSFER_TO_SLAVE_DEVICES(5026, "Configure multicast block transfer") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, deviceIdsAttributeName, deviceIdsAttributeDefaultTranslation),
                    this.booleanSpec(service, SkipStepEnable, SkipStepEnableDefaultTranslation, Boolean.TRUE),
                    this.booleanSpec(service, SkipStepVerify, SkipStepVerifyDefaultTranslation, Boolean.TRUE),
                    this.booleanSpec(service, SkipStepActivate, SkipStepActivateDefaultTranslation, Boolean.TRUE),
                    this.bigDecimalSpec(service, UnicastClientWPort, UnicastClientWPortDefaultTranslation, BigDecimal.valueOf(2)),
                    this.bigDecimalSpec(service, MulticastClientWPort, MulticastClientWPortDefaultTranslation, BigDecimal.valueOf(3)),
                    this.stringSpecWithDefaultAndOtherValues(service, UnicastFrameCounterType, UnicastFrameCounterTypeDefaultTranslation, "auth_hmac_sha256", "default", "auth_hmac_sha256"),
                    this.stringSpecWithDefaultAndOtherValues(service, MeterTimeZone, MeterTimeZoneDefaultTranslation, "Europe/Vienna", TimeZone.getAvailableIDs()),
                    this.bigDecimalSpec(service, SecurityLevelMulticast, SecurityLevelMulticastDefaultTranslation, BigDecimal.ZERO),
                    this.bigDecimalSpec(service, SecurityPolicyMulticastV0, SecurityPolicyMulticastV0DefaultTranslation, BigDecimal.ZERO),
                    this.durationSpec(service, DelayBetweenBlockSentFast, DelayBetweenBlockSentFastDefaultTranslation, Duration.ofMillis(20))
            );
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.empty();
        }
    },
    START_MULTICAST_BLOCK_TRANSFER_TO_SLAVE_DEVICES(5027, "Start multicast block transfer") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_LATER);
        }
    };

    private final long id;
    private final String defaultNameTranslation;

    FirmwareDeviceMessage(long id, String defaultNameTranslation) {
        this.id = id;
        this.defaultNameTranslation = defaultNameTranslation;
    }

    @Override
    public long id() {
        return this.id;
    }

    protected abstract List<PropertySpec> getPropertySpecs(PropertySpecService service);

    private PropertySpecBuilder<Boolean> booleanPropertySpecBuilder(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .booleanSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired();
    }
    protected PropertySpec booleanSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        return this.booleanPropertySpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation).finish();
    }

    protected PropertySpec booleanSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, Boolean defaultValue) {
        return this.booleanPropertySpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation)
                .setDefaultValue(defaultValue)
                .finish();
    }

    private PropertySpecBuilder<String> stringSpecBuilder(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .stringSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired();
    }

    protected PropertySpec stringSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        return this.stringSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation).finish();
    }

    protected PropertySpec stringSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, String... possibleValues) {
        return this.stringSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation)
                .addValues(possibleValues)
                .markExhaustive()
                .markRequired()
                .finish();
    }

    protected PropertySpec stringSpecWithDefaultAndOtherValues(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, String defaultValue, String... possibleValues) {
        return this.stringSpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation)
                .setDefaultValue(defaultValue)
                .addValues(possibleValues)
                .markExhaustive()
                .markRequired()
                .finish();
    }

    protected PropertySpec passwordSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .passwordSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired()
                .finish();
    }

    private PropertySpecBuilder<BigDecimal> bigDecimalPropertySpecBuilder(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .bigDecimalSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired();
    }

    protected PropertySpec bigDecimalSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        return this.bigDecimalPropertySpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation).finish();
    }

    protected PropertySpec bigDecimalSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, BigDecimal... possibleValues) {
        return this.bigDecimalPropertySpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation)
                .addValues(possibleValues)
                .markExhaustive()
                .markRequired()
                .finish();
    }

    protected PropertySpec bigDecimalSpecWithDefaultValue(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, BigDecimal defaultValue) {
        return this.bigDecimalPropertySpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation)
                .setDefaultValue(defaultValue)
                .markExhaustive()
                .markRequired()
                .finish();
    }

    protected PropertySpec dateTimeSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .dateTimeSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired()
                .finish();
    }

    protected PropertySpec durationSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, Duration defaultValue) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .durationSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .setDefaultValue(defaultValue)
                .markRequired()
                .finish();
    }

    private <T> PropertySpec referenceSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, Class<T> apiClass) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return service
                .referenceSpec(apiClass.getName())
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .markRequired()
                .finish();
    }

    protected PropertySpec firmwareVersionSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        return this.referenceSpec(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation, FirmwareVersion.class);
    }

    protected PropertySpec deviceGroupSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        return this.referenceSpec(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation, DeviceGroup.class);
    }

    private String getNameResourceKey() {
        return FirmwareDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public DeviceMessageSpec get(PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        return new DeviceMessageSpecImpl(
                id, new TranslationKeyImpl(this.getNameResourceKey(), this.defaultNameTranslation),
                DeviceMessageCategories.FIRMWARE,
                this.getPropertySpecs(propertySpecService),
                propertySpecService, nlsService, converter);
    }

    public abstract Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption();

}