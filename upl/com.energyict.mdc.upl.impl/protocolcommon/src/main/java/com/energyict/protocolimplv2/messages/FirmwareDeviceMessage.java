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

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.*;

/**
 * Provides a summary of all <i>Firmware</i> related messages.
 * <p/>
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum FirmwareDeviceMessage implements DeviceMessageSpecSupplier {

    UPGRADE_FIRMWARE_WITH_USER_FILE(5001, "Upload firmware and activate immediately") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.firmwareVersionSpec(service, firmwareUpdateFileAttributeName, firmwareUpdateUserFileAttributeDefaultTranslation));
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE);
        }
    },
    UPGRADE_FIRMWARE_WITH_USER_FILE_AND_RESUME_OPTION(5002, "Upload firmware with resume option and activate immediately") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.firmwareVersionSpec(service, firmwareUpdateFileAttributeName, firmwareUpdateUserFileAttributeDefaultTranslation),
                    getResumeFirmwareUploadPropertySpec(service).get()
            );
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE);
        }

        @Override
        public Optional<PropertySpec> getResumeFirmwareUploadPropertySpec(PropertySpecService service) {
            return Optional.of(this.booleanSpec(service, resumeFirmwareUpdateAttributeName, resumeFirmwareUpdateAttributeDefaultTranslation, Boolean.TRUE));
        }
    },
    UPGRADE_FIRMWARE_WITH_USER_FILE_AND_RESUME_OPTION_AND_TYPE(5003, "Upload firmware with resume option and type and activate immediately") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.firmwareVersionSpec(service, firmwareUpdateFileAttributeName, firmwareUpdateUserFileAttributeDefaultTranslation),
                    getResumeFirmwareUploadPropertySpec(service).get(),
                    this.booleanSpec(service, plcTypeFirmwareUpdateAttributeName, plcTypeFirmwareUpdateAttributeDefaultTranslation)
            );
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE);
        }

        @Override
        public Optional<PropertySpec> getResumeFirmwareUploadPropertySpec(PropertySpecService service) {
            return Optional.of(this.booleanSpec(service, resumeFirmwareUpdateAttributeName, resumeFirmwareUpdateAttributeDefaultTranslation, Boolean.TRUE));
        }
    },
    UPGRADE_FIRMWARE_ACTIVATE(5004, "Activate last uploaded firmware") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.dateTimeSpec(service, firmwareUpdateActivationDateAttributeName, firmwareUpdateActivationDateAttributeDefaultTranslation));
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.empty();
        }
    },
    UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE(5005, "Upload firmware with activation date") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.firmwareVersionSpec(service, firmwareUpdateFileAttributeName, firmwareUpdateUserFileAttributeDefaultTranslation),
                    this.dateTimeSpec(service, firmwareUpdateActivationDateAttributeName, firmwareUpdateActivationDateAttributeDefaultTranslation)
            );
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE);
        }
    },
    UPGRADE_FIRMWARE_WITH_USER_FILE_VERSION_AND_ACTIVATE(5006, "Upload firmware with version and activation date") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.firmwareVersionSpec(service, firmwareUpdateFileAttributeName, firmwareUpdateUserFileAttributeDefaultTranslation),
                    this.dateTimeSpec(service, firmwareUpdateActivationDateAttributeName, firmwareUpdateActivationDateAttributeDefaultTranslation),
                    this.stringSpec(service, firmwareUpdateVersionNumberAttributeName, firmwareUpdateVersionNumberAttributeDefaultTranslation)
            );
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE);
        }
    },
    UPGRADE_FIRMWARE_URL(5007, "Upload firmware via url and activate immediately") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(this.stringSpec(service, firmwareUpdateURLAttributeName, firmwareUpdateURLAttributeDefaultTranslation));
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE);
        }
    },
    UPGRADE_FIRMWARE_URL_AND_ACTIVATE(5008, "Upload firmware via url with activation date") {
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
    UPGRADE_FIRMWARE_WITH_USER_FILE_AND_KDL_AND_HASH_AND_ACTIVATION(5009, "Upload firmware with version, KDL, hashcode and activation date") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.firmwareVersionSpec(service, firmwareUpdateFileAttributeName, firmwareUpdateUserFileAttributeDefaultTranslation),
                    this.dateTimeSpec(service, firmwareUpdateActivationDateAttributeName, firmwareUpdateActivationDateAttributeDefaultTranslation),
                    this.stringSpecWithDefaultAndOtherValues(service, firmwareUpdateImageTypeAttributeName, firmwareUpdateImageTypeAttributeNameDefaultTranslation,
                            "",
                            FirmwareImageType.ApplicationImage.getDescription(),
                            FirmwareImageType.BootloaderImage.getDescription(),
                            FirmwareImageType.MetrologyImage.getDescription(),
                            FirmwareImageType.LanguageTableImage.getDescription()),
                    this.stringSpec(service, firmwareUpdateKDLAttributeName, firmwareUpdateKDLAttributeNameDefaultTranslation),
                    this.stringSpec(service, firmwareUpdateHashAttributeName, firmwareUpdateHashAttributeNameDefaultTranslation)
            );
        }
        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE);
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
            return Collections.singletonList(this.firmwareVersionSpec(service, DeviceMessageConstants.firmwareUpdateFileAttributeName, DeviceMessageConstants.firmwareUpdateUserFileAttributeDefaultTranslation));
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
    UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE_AND_IMAGE_IDENTIFIER(5016, "Upload firmware with file, activation date and image identifier") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.firmwareVersionSpec(service, firmwareUpdateFileAttributeName, firmwareUpdateUserFileAttributeDefaultTranslation),
                    this.dateTimeSpec(service, firmwareUpdateActivationDateAttributeName, firmwareUpdateActivationDateAttributeDefaultTranslation),
                    this.stringSpec(service, firmwareUpdateImageIdentifierAttributeName, firmwareUpdateImageIdentifierAttributeDefaultTranslation)
            );
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE);
        }

        @Override
        public Optional<PropertySpec> getFirmwareIdentifierPropertySpec(PropertySpecService service) {
            return Optional.of(this.stringSpec(service, firmwareUpdateImageIdentifierAttributeName, firmwareUpdateImageIdentifierAttributeDefaultTranslation));
        }
    },
    UPGRADE_FIRMWARE_WITH_USER_FILE_AND_IMAGE_IDENTIFIER(5017, "Upload firmware with file and image identifier") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.firmwareVersionSpec(service, firmwareUpdateFileAttributeName, firmwareUpdateUserFileAttributeDefaultTranslation),
                    this.getFirmwareIdentifierPropertySpec(service).get()
            );
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE);
        }

        @Override
        public Optional<PropertySpec> getFirmwareIdentifierPropertySpec(PropertySpecService service) {
            return Optional.of(this.stringSpec(service, firmwareUpdateImageIdentifierAttributeName, firmwareUpdateImageIdentifierAttributeDefaultTranslation));
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
                    this.firmwareVersionSpec(service, firmwareUpdateFileAttributeName, firmwareUpdateUserFileAttributeDefaultTranslation),
                    this.getFirmwareIdentifierPropertySpec(service).get(),
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

        @Override
        public Optional<PropertySpec> getFirmwareIdentifierPropertySpec(PropertySpecService service) {
            return Optional.of(this.stringSpec(service, firmwareUpdateImageIdentifierAttributeName, firmwareUpdateImageIdentifierAttributeDefaultTranslation));
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
    MulticastFirmwareUpgradeKSMW(5020, "Multicast firmware upgrade (pre-established client)") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.firmwareVersionSpec(service, firmwareUpdateFileAttributeName, firmwareUpdateUserFileAttributeDefaultTranslation),
                    this.getFirmwareIdentifierPropertySpec(service).get(),
                    this.bigDecimalSpec(service, RequestedBlockSize, RequestedBlockSizeDefaultTranslation, BigDecimal.valueOf(330)),
                    this.bigDecimalSpec(service, MulticastGroup, MulticastGroupDefaultTranslation, BigDecimal.ONE),
                    this.bigDecimalSpec(service, UdpMeterPort, UdpMeterPortDefaultTranslation, BigDecimal.valueOf(61616)),
                    this.bigDecimalSpec(service, LogicalDeviceLSap, LogicalDeviceLSapDefaultTranslation, BigDecimal.ONE),
                    this.bigDecimalSpec(service, MulticastClientWPort, MulticastClientWPortDefaultTranslation, BigDecimal.valueOf(3)),
                    this.bigDecimalSpec(service, MaxCycles, MaxCyclesDefaultTranslation, BigDecimal.ONE),
                    this.booleanSpec(service, PadLastBlock, PadLastBlockDefaultTranslation, Boolean.FALSE),
                    this.durationSpec(service, DelayBetweenBlockSentSlow, DelayBetweenBlockSentSlowDefaultTranslation, Duration.ofMillis(100)),
                    this.durationSpec(service, DelayAfterLastBlock, DelayAfterLastBlockDefaultTranslation, Duration.ofSeconds(5))
            );
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_LATER);
        }

        @Override
        public Optional<PropertySpec> getFirmwareIdentifierPropertySpec(PropertySpecService service) {
            return Optional.of(this.stringSpec(service, firmwareUpdateImageIdentifierAttributeName, firmwareUpdateImageIdentifierAttributeDefaultTranslation));
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
    FirmwareUpgradeWithUrlJarJadFileSize(5022, "Upload firmware with URL") {
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
                    this.firmwareVersionSpec(service, firmwareUpdateFileAttributeName, firmwareUpdateUserFileAttributeDefaultTranslation),
                    this.stringSpec(service, firmwareUpdateImageIdentifierAttributeName, firmwareUpdateImageIdentifierAttributeDefaultTranslation),
                    this.booleanSpec(service, resumeFirmwareUpdateAttributeName, resumeFirmwareUpdateAttributeDefaultTranslation, Boolean.TRUE)
            );
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE);
        }

        @Override
        public Optional<PropertySpec> getFirmwareIdentifierPropertySpec(PropertySpecService service) {
            return Optional.of(this.stringSpec(service, firmwareUpdateImageIdentifierAttributeName, firmwareUpdateImageIdentifierAttributeDefaultTranslation));
        }

        @Override
        public Optional<PropertySpec> getResumeFirmwareUploadPropertySpec(PropertySpecService service) {
            return Optional.of(this.booleanSpec(service, resumeFirmwareUpdateAttributeName, resumeFirmwareUpdateAttributeDefaultTranslation, Boolean.TRUE));
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
                    this.firmwareVersionSpec(service, firmwareUpdateFileAttributeName, firmwareUpdateUserFileAttributeDefaultTranslation),
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
    },
    FIRMWARE_IMAGE_ACTIVATION_WITH_DATA_PROTECTION(5028, "Firmware image activation with data protection") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.empty();
        }
    },
    COPY_ACTIVE_FIRMWARE_TO_INACTIVE_PARTITION(5029, "Copy active firmware to inactive partition") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.emptyList();
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.empty();
        }
    },
    UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE_AND_IMAGE_IDENTIFIER_AND_RESUME(5030, "Upload firmware with activation date, image identifier and resume") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.firmwareVersionSpec(service, firmwareUpdateFileAttributeName, firmwareUpdateUserFileAttributeDefaultTranslation),
                    this.dateTimeSpec(service, firmwareUpdateActivationDateAttributeName, firmwareUpdateActivationDateAttributeDefaultTranslation),
                    this.stringSpec(service, firmwareUpdateImageIdentifierAttributeName, firmwareUpdateImageIdentifierAttributeDefaultTranslation),
                    getResumeFirmwareUploadPropertySpec(service) .get()
            );
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE);
        }

        @Override
        public Optional<PropertySpec> getFirmwareIdentifierPropertySpec(PropertySpecService service) {
            return Optional.of(this.stringSpec(service, firmwareUpdateImageIdentifierAttributeName, firmwareUpdateImageIdentifierAttributeDefaultTranslation));
        }

        @Override
        public Optional<PropertySpec> getResumeFirmwareUploadPropertySpec(PropertySpecService service) {
            return Optional.of(this.booleanSpec(service, resumeFirmwareUpdateAttributeName, resumeFirmwareUpdateAttributeDefaultTranslation, Boolean.TRUE));
        }
    },
    VerifyAndActivateFirmwareAtGivenDate(5031, "Verify and activate firmware at given date") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(
                    this.dateTimeSpec(service, firmwareUpdateActivationDateAttributeName, firmwareUpdateActivationDateAttributeDefaultTranslation)
            );
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.empty();
        }
    },
    FIRMWARE_IMAGE_ACTIVATION_WITH_DATA_PROTECTION_AND_ACTIVATION_DATE(5032, "Firmware image activation with data protection and activation date") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Collections.singletonList(
                    this.dateTimeSpec(service, firmwareUpdateActivationDateAttributeName, firmwareUpdateActivationDateAttributeDefaultTranslation)
            );
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.empty();
        }
    },

    ENABLE_AND_INITIATE_IMAGE_TRANSFER(5033, "Enable and initiate image transfer") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, firmwareUpdateImageIdentifierAttributeName, firmwareUpdateImageIdentifierAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, FW_UPGRADE_IMAGE_SIZE, FW_UPGRADE_IMAGE_SIZE_DEFAULT_TRANSLATION)
            );
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.empty();
        }
    },

    CONFIGURABLE_IMAGE_TRANSFER_WITH_RESUME_OPTION(5034, "Configurable image transfer with resume option") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.firmwareVersionSpec(service, firmwareUpdateFileAttributeName, firmwareUpdateUserFileAttributeDefaultTranslation),
                    this.stringSpec(service, firmwareUpdateImageIdentifierAttributeName, firmwareUpdateImageIdentifierAttributeDefaultTranslation),
                    this.booleanSpec(service, resumeFirmwareUpdateAttributeName, resumeFirmwareUpdateAttributeDefaultTranslation, true),
                    this.booleanSpec(service, enableImageTransfer, enableImageTransferDefaultTranslation, true),
                    this.booleanSpec(service, initiateImageTransfer, initiateImageTransferDefaultTranslation, true),
                    this.booleanSpec(service, transferBlocks, transferBlocksDefaultTranslation, true),
                    this.booleanSpec(service, verifyImage, verifyImageDefaultTranslation, true),
                    this.booleanSpec(service, activateImage, activateImageDefaultTranslation, true)
            );
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.empty();
        }
    },

    TRANSFER_CA_CONFIG_IMAGE(5035, "Transfer CA config image") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.firmwareVersionSpec(service, configurationCAImageFileAttributeName, configurationCAImageFileDefaultTranslation),
                    this.getFirmwareIdentifierPropertySpec(service).get()
            );
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE);
        }

        @Override
        public Optional<PropertySpec> getFirmwareIdentifierPropertySpec(PropertySpecService service) {
            return Optional.of(this.stringSpec(service, firmwareUpdateImageIdentifierAttributeName, firmwareUpdateImageIdentifierAttributeDefaultTranslation));
        }
    },

    LTE_MODEM_FIRMWARE_UPGRADE(5036, "LTE Modem Firmware Upgrade") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.firmwareVersionSpec(service, firmwareUpdateFileAttributeName, firmwareUpdateUserFileAttributeDefaultTranslation),
                    this.bigDecimalSpec(service, LTEModemFirmwareUgradeDownloadTimeoutAttributeName, LTEModemFirmwareUgradeDownloadTimeoutAttributeNameDefaultTranslation)
            );
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_LATER);
        }

    },
    DataConcentratorMulticastFirmwareUpgrade(5037, "Multicast firmware upgrade (Data concentrator mode)") {
        @Override
        public List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.stringSpec(service, deviceIdsAttributeName, deviceIdsAttributeDefaultTranslation),
                    this.firmwareVersionSpec(service, firmwareUpdateFileAttributeName, firmwareUpdateUserFileAttributeDefaultTranslation),
                    this.getFirmwareIdentifierPropertySpec(service).get(),
                    this.bigDecimalSpec(service, UnicastClientWPort, UnicastClientWPortDefaultTranslation, BigDecimal.ONE),
                    this.bigDecimalSpec(service, BroadcastClientWPort, BroadcastClientWPortDefaultTranslation, BigDecimal.valueOf(64)),
                    this.bigDecimalSpec(service, MulticastClientWPort, MulticastClientWPortDefaultTranslation, BigDecimal.valueOf(102)),
                    this.bigDecimalSpec(service, LogicalDeviceLSap, LogicalDeviceLSapDefaultTranslation, BigDecimal.ONE),
                    this.bigDecimalSpec(service, SecurityLevelUnicast, SecurityLevelUnicastDefaultTranslation, BigDecimal.valueOf(3)),
                    this.bigDecimalSpec(service, SecurityLevelBroadcast, SecurityLevelBroadcastDefaultTranslation, BigDecimal.valueOf(3)),
                    this.bigDecimalSpec(service, SecurityPolicyBroadcast, SecurityPolicyBroadcastDefaultTranslation, BigDecimal.ZERO),
                    this.durationSpec(service, DelayAfterLastBlock, DelayAfterLastBlockDefaultTranslation, Duration.ofSeconds(5)),
                    this.durationSpec(service, DelayPerBlock, DelayPerBlockDefaultTranslation, Duration.ofSeconds(4)),
                    this.durationSpec(service, DelayBetweenBlockSentFast, DelayBetweenBlockSentFastDefaultTranslation, Duration.ofMillis(250)),
                    this.durationSpec(service, DelayBetweenBlockSentSlow, DelayBetweenBlockSentSlowDefaultTranslation, Duration.ofMillis(500)),
                    this.bigDecimalSpec(service, BlocksPerCycle, BlocksPerCycleDefaultTranslation, BigDecimal.valueOf(30)),
                    this.bigDecimalSpec(service, MaxCycles, MaxCyclesDefaultTranslation, BigDecimal.ONE),
                    this.bigDecimalSpec(service, RequestedBlockSize, RequestedBlockSizeDefaultTranslation, BigDecimal.valueOf(1024)),
                    this.booleanSpec(service, PadLastBlock, PadLastBlockDefaultTranslation, Boolean.FALSE),
                    this.booleanSpec(service, UseTransferredBlockStatus, UseTransferredBlockStatusDefaultTranslation, Boolean.TRUE)
            );
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_LATER);
        }

        @Override
        public Optional<PropertySpec> getFirmwareIdentifierPropertySpec(PropertySpecService service) {
            return Optional.of(this.stringSpec(service, firmwareUpdateImageIdentifierAttributeName, firmwareUpdateImageIdentifierAttributeDefaultTranslation));
        }
    },
    //from eiserver 8.11
    MBUS_ESMR5_FIRMWARE_UPGRADE(5038, "MBus ESMR5 Firmware Upgrade") {
        @Override
        protected List<PropertySpec> getPropertySpecs(PropertySpecService service) {
            return Arrays.asList(
                    this.firmwareVersionSpec(service, firmwareUpdateFileAttributeName, firmwareUpdateUserFileAttributeDefaultTranslation),
                    this.dateTimeSpec(service, firmwareUpdateActivationDateAttributeName, firmwareUpdateActivationDateAttributeDefaultTranslation)
            );
        }

        @Override
        public Optional<ProtocolSupportedFirmwareOptions> getProtocolSupportedFirmwareOption() {
            return Optional.of(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE);
        }
    },;

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

    protected PropertySpec booleanSpec(PropertySpecService service, String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, Boolean defaultValue) {
        return this.booleanPropertySpecBuilder(service, deviceMessageConstantKey, deviceMessageConstantDefaultTranslation)
                .setDefaultValue(defaultValue)
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

    /**
     * Additional PropertySpec to send the firmware identifier with the firmware file
     * @return an additional property spec for the firmware identifier when protocols expect it, default Optional.empty
     */
    public Optional<PropertySpec> getFirmwareIdentifierPropertySpec(PropertySpecService service){
        return Optional.empty();
    }

    /**
     * Additional PropertySpec to resume the reading of the firmware file
     * @return an additional property spec for the firmware identifier when protocols expect it, default Optional.empty
     */
    public Optional<PropertySpec> getResumeFirmwareUploadPropertySpec(PropertySpecService service){
        return Optional.empty();
    }

}