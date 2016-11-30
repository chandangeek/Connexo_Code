package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.messages.DeviceMessageCategory;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.DeviceMessageSpecPrimaryKey;
import com.energyict.mdc.upl.properties.DeviceGroup;
import com.energyict.mdc.upl.properties.DeviceMessageFile;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilder;

import com.energyict.protocolimplv2.messages.enums.DlmsEncryptionLevelMessageValues;
import com.energyict.protocolimplv2.messages.nls.Thesaurus;
import com.energyict.protocolimplv2.messages.nls.TranslationKeyImpl;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
 * <p/>
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum FirmwareDeviceMessage implements DeviceMessageSpec {

    UPGRADE_FIRMWARE_WITH_USER_FILE(0) {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Collections.singletonList(this.deviceMessageFileSpec(firmwareUpdateActivationDateAttributeName, firmwareUpdateActivationDateAttributeDefaultTranslation));
        }
    },
    UPGRADE_FIRMWARE_WITH_USER_FILE_AND_RESUME_OPTION(1) {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Arrays.asList(
                    this.deviceMessageFileSpec(firmwareUpdateActivationDateAttributeName, firmwareUpdateActivationDateAttributeDefaultTranslation),
                    this.booleanSpec(resumeFirmwareUpdateAttributeName, resumeFirmwareUpdateAttributeDefaultTranslation, Boolean.TRUE)
            );
        }
    },
    UPGRADE_FIRMWARE_WITH_USER_FILE_AND_RESUME_OPTION_AND_TYPE(2) {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Arrays.asList(
                    this.deviceMessageFileSpec(firmwareUpdateUserFileAttributeName, firmwareUpdateUserFileAttributeDefaultTranslation),
                    this.booleanSpec(resumeFirmwareUpdateAttributeName, resumeFirmwareUpdateAttributeDefaultTranslation, Boolean.TRUE),
                    this.booleanSpec(plcTypeFirmwareUpdateAttributeName, plcTypeFirmwareUpdateAttributeDefaultTranslation)
            );
        }
    },
    UPGRADE_FIRMWARE_ACTIVATE(3) {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Collections.singletonList(this.dateTimeSpec(firmwareUpdateActivationDateAttributeName, firmwareUpdateActivationDateAttributeDefaultTranslation));
        }
    },
    UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE(4) {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Arrays.asList(
                    this.deviceMessageFileSpec(firmwareUpdateUserFileAttributeName, firmwareUpdateUserFileAttributeDefaultTranslation),
                    this.dateTimeSpec(firmwareUpdateActivationDateAttributeName, firmwareUpdateActivationDateAttributeDefaultTranslation)
            );
        }
    },
    UPGRADE_FIRMWARE_WITH_USER_FILE_VERSION_AND_ACTIVATE(5) {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Arrays.asList(
                    this.deviceMessageFileSpec(firmwareUpdateUserFileAttributeName, firmwareUpdateUserFileAttributeDefaultTranslation),
                    this.dateTimeSpec(firmwareUpdateActivationDateAttributeName, firmwareUpdateActivationDateAttributeDefaultTranslation),
                    this.stringSpec(firmwareUpdateVersionNumberAttributeName, firmwareUpdateVersionNumberAttributeDefaultTranslation)
            );
        }
    },
    UPGRADE_FIRMWARE_URL(6) {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Collections.singletonList(this.stringSpec(firmwareUpdateURLAttributeName, firmwareUpdateURLAttributeDefaultTranslation));
        }
    },
    UPGRADE_FIRMWARE_URL_AND_ACTIVATE(7) {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Arrays.asList(
                    this.stringSpec(firmwareUpdateURLAttributeName, firmwareUpdateURLAttributeDefaultTranslation),
                    this.dateTimeSpec(firmwareUpdateActivationDateAttributeName, firmwareUpdateActivationDateAttributeDefaultTranslation)
            );
        }
    },
    UpgradeWaveCard(8) {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Collections.singletonList(this.deviceMessageFileSpec(DeviceMessageConstants.waveCardFirmware, DeviceMessageConstants.waveCardFirmwareDefaultTranslation));
        }
    },
    PLCPrimeSetFirmwareUpgradeFile(9) {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Collections.singletonList(this.deviceMessageFileSpec(DeviceMessageConstants.firmwareUpdateUserFileAttributeName, DeviceMessageConstants.firmwareUpdateUserFileAttributeDefaultTranslation));
        }
    },
    PLCPrimeStartFirmwareUpgradeNodeList(10) {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Collections.singletonList(this.deviceMessageFileSpec(DeviceMessageConstants.nodeListUserFile, DeviceMessageConstants.nodeListUserFileDefaultTranslation));
        }
    },
    FTIONUpgradeRFMeshFirmware(11) {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Collections.emptyList();
        }
    },
    RFMeshUpgradeURL(12) {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Collections.singletonList(this.stringSpec(DeviceMessageConstants.SetUpgradeUrlAttributeName, DeviceMessageConstants.SetUpgradeUrlAttributeDefaultTranslation));
        }
    },
    UpgradeBootloader(13) {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Collections.singletonList(this.deviceMessageFileSpec(DeviceMessageConstants.PricingInformationUserFileAttributeName, DeviceMessageConstants.PricingInformationUserFileAttributeDefaultTranslation));
        }
    },
    UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE_AND_IMAGE_IDENTIFIER(14) {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Arrays.asList(
                    this.deviceMessageFileSpec(firmwareUpdateUserFileAttributeName, firmwareUpdateUserFileAttributeDefaultTranslation),
                    this.dateTimeSpec(firmwareUpdateActivationDateAttributeName, firmwareUpdateActivationDateAttributeDefaultTranslation),
                    this.stringSpec(firmwareUpdateImageIdentifierAttributeName, firmwareUpdateImageIdentifierAttributeDefaultTranslation)
            );
        }
    },
    UPGRADE_FIRMWARE_WITH_USER_FILE_AND_IMAGE_IDENTIFIER(15) {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Arrays.asList(
                    this.deviceMessageFileSpec(firmwareUpdateUserFileAttributeName, firmwareUpdateUserFileAttributeDefaultTranslation),
                    this.stringSpec(firmwareUpdateImageIdentifierAttributeName, firmwareUpdateImageIdentifierAttributeDefaultTranslation)
            );
        }
    },
    BroadcastFirmwareUpgrade(16) {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Arrays.asList(
                    this.deviceGroupSpec(DeviceMessageConstants.broadcastDevicesGroupAttributeName, DeviceMessageConstants.broadcastDevicesGroupAttributeDefaultTranslation),
                    this.bigDecimalSpec(broadcastLogicalDeviceIdAttributeName, broadcastLogicalDeviceIdAttributeDefaultTranslation, BigDecimal.valueOf(16)),
                    this.bigDecimalSpec(broadcastClientMacAddressAttributeName, broadcastClientMacAddressAttributeDefaultTranslation, BigDecimal.valueOf(102)),
                    this.bigDecimalSpec(broadcastGroupIdAttributeName, broadcastGroupIdAttributeDefaultTranslation, BigDecimal.ONE),   //Default group 1 is broadcast (to all devices)
                    this.bigDecimalSpec(broadcastNumberOfBlocksInCycleAttributeName, broadcastNumberOfBlocksInCycleAttributeDefaultTranslation, BigDecimal.valueOf(100)),
                    this.durationSpec(broadcastInitialTimeBetweenBlocksAttributeName, broadcastInitialTimeBetweenBlocksAttributeDefaultTranslation, Duration.ofSeconds(1)), //TODO check if this is a good default value??
                    this.deviceMessageFileSpec(firmwareUpdateUserFileAttributeName, firmwareUpdateUserFileAttributeDefaultTranslation),
                    this.stringSpec(firmwareUpdateImageIdentifierAttributeName, firmwareUpdateImageIdentifierAttributeDefaultTranslation),
                    this.passwordSpec(broadcastEncryptionKeyAttributeName, broadcastEncryptionKeyAttributeDefaultTranslation),
                    this.passwordSpec(broadcastAuthenticationKeyAttributeName, broadcastAuthenticationKeyAttributeDefaultTranslation),
                    this.stringSpec(
                            DeviceMessageConstants.encryptionLevelAttributeName, DeviceMessageConstants.encryptionLevelAttributeDefaultTranslation,
                            DlmsEncryptionLevelMessageValues.getNames())
            );
        }
    },
    VerifyAndActivateFirmware(17) {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Collections.emptyList();
        }
    },
    DataConcentratorMulticastFirmwareUpgrade(18) {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Arrays.asList(
                    this.stringSpec(deviceIdsAttributeName, deviceIdsAttributeDefaultTranslation),
                    this.deviceMessageFileSpec(firmwareUpdateUserFileAttributeName, firmwareUpdateUserFileAttributeDefaultTranslation),
                    this.stringSpec(firmwareUpdateImageIdentifierAttributeName, firmwareUpdateImageIdentifierAttributeDefaultTranslation),
                    this.bigDecimalSpecWithDefaultValue(UnicastClientWPort, UnicastClientWPortDefaultTranslation, BigDecimal.ONE),
                    this.bigDecimalSpecWithDefaultValue(BroadcastClientWPort, BroadcastClientWPortDefaultTranslation, BigDecimal.valueOf(64)),
                    this.bigDecimalSpecWithDefaultValue(MulticastClientWPort, MulticastClientWPortDefaultTranslation, BigDecimal.valueOf(102)),
                    this.bigDecimalSpecWithDefaultValue(LogicalDeviceLSap, LogicalDeviceLSapDefaultTranslation, BigDecimal.ONE),
                    this.bigDecimalSpecWithDefaultValue(SecurityLevelUnicast, SecurityLevelUnicastDefaultTranslation, BigDecimal.valueOf(3)),
                    this.bigDecimalSpecWithDefaultValue(SecurityLevelBroadcast, SecurityLevelBroadcastDefaultTranslation, BigDecimal.valueOf(3)),
                    this.bigDecimalSpecWithDefaultValue(SecurityPolicyBroadcast, SecurityPolicyBroadcastDefaultTranslation, BigDecimal.ZERO),
                    this.durationSpec(DelayAfterLastBlock, DelayAfterLastBlockDefaultTranslation, Duration.ofSeconds(5)),
                    this.durationSpec(DelayPerBlock, DelayPerBlockDefaultTranslation, Duration.ofSeconds(4)),
                    this.durationSpec(DelayBetweenBlockSentFast, DelayBetweenBlockSentFastDefaultTranslation, Duration.ofMillis(250)),
                    this.durationSpec(DelayBetweenBlockSentSlow, DelayBetweenBlockSentSlowDefaultTranslation, Duration.ofMillis(500)),
                    this.bigDecimalSpecWithDefaultValue(BlocksPerCycle, BlocksPerCycleDefaultTranslation, BigDecimal.valueOf(30)),
                    this.bigDecimalSpecWithDefaultValue(MaxCycles, MaxCyclesDefaultTranslation, BigDecimal.ONE),
                    this.bigDecimalSpec(RequestedBlockSize, RequestedBlockSizeDefaultTranslation, BigDecimal.valueOf(1024)),
                    this.booleanSpec(PadLastBlock, PadLastBlockDefaultTranslation, Boolean.FALSE),
                    this.booleanSpec(UseTransferredBlockStatus, UseTransferredBlockStatusDefaultTranslation, Boolean.TRUE)
            );
        }
    },
    ReadMulticastProgress(19) {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Collections.emptyList();
        }
    },
    FirmwareUpgradeWithUrlJarJadFileSize(20) {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Arrays.asList(
                    this.stringSpec(DeviceMessageConstants.URL_PATH, DeviceMessageConstants.URL_PATH_DEFAULT_TRANSLATION),
                    this.bigDecimalSpec(DeviceMessageConstants.JAR_FILE_SIZE, DeviceMessageConstants.JAR_FILE_SIZE_DEFAULT_TRANSLATION),
                    this.bigDecimalSpec(DeviceMessageConstants.JAD_FILE_SIZE, DeviceMessageConstants.JAD_FILE_SIZE_DEFAULT_TRANSLATION)
            );
        }
    },
    UPGRADE_FIRMWARE_WITH_USER_FILE_RESUME_AND_IMAGE_IDENTIFIER(21) {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Arrays.asList(
                    this.deviceMessageFileSpec(firmwareUpdateUserFileAttributeName, firmwareUpdateUserFileAttributeDefaultTranslation),
                    this.stringSpec(firmwareUpdateImageIdentifierAttributeName, firmwareUpdateImageIdentifierAttributeDefaultTranslation),
                    this.booleanSpec(resumeFirmwareUpdateAttributeName, resumeFirmwareUpdateAttributeDefaultTranslation, Boolean.TRUE)
            );
        }
    },
    ENABLE_IMAGE_TRANSFER(22) {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Collections.emptyList();
        }
    },
    TRANSFER_SLAVE_FIRMWARE_FILE_TO_DATA_CONCENTRATOR(23) {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Arrays.asList(
                    this.deviceMessageFileSpec(firmwareUpdateUserFileAttributeName, firmwareUpdateUserFileAttributeDefaultTranslation),
                    this.stringSpec(firmwareUpdateImageIdentifierAttributeName, firmwareUpdateImageIdentifierAttributeDefaultTranslation));
        }
    },
    CONFIGURE_MULTICAST_BLOCK_TRANSFER_TO_SLAVE_DEVICES(24) {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Arrays.asList(
                    this.stringSpec(deviceIdsAttributeName, deviceIdsAttributeDefaultTranslation),
                    this.booleanSpec(SkipStepEnable, SkipStepEnableDefaultTranslation, Boolean.TRUE),
                    this.booleanSpec(SkipStepVerify, SkipStepVerifyDefaultTranslation, Boolean.TRUE),
                    this.booleanSpec(SkipStepActivate, SkipStepActivateDefaultTranslation, Boolean.TRUE),
                    this.bigDecimalSpec(UnicastClientWPort, UnicastClientWPortDefaultTranslation, BigDecimal.valueOf(2)),
                    this.bigDecimalSpec(MulticastClientWPort, MulticastClientWPortDefaultTranslation, BigDecimal.valueOf(3)),
                    this.stringSpecWithDefaultAndOtherValues(UnicastFrameCounterType, UnicastFrameCounterTypeDefaultTranslation, "auth_hmac_sha256", "default", "auth_hmac_sha256"),
                    this.stringSpecWithDefaultAndOtherValues(MeterTimeZone, MeterTimeZoneDefaultTranslation, "Europe/Vienna", TimeZone.getAvailableIDs()),
                    this.bigDecimalSpec(SecurityLevelMulticast, SecurityLevelMulticastDefaultTranslation, BigDecimal.ZERO),
                    this.bigDecimalSpec(SecurityPolicyMulticastV0, SecurityPolicyMulticastV0DefaultTranslation, BigDecimal.ZERO),
                    this.durationSpec(DelayBetweenBlockSentFast, DelayBetweenBlockSentFastDefaultTranslation, Duration.ofMillis(20))
            );
        }
    },
    START_MULTICAST_BLOCK_TRANSFER_TO_SLAVE_DEVICES(25) {
        @Override
        public List<PropertySpec> getPropertySpecs() {
            return Collections.emptyList();
        }
    };

    private final long id;

    FirmwareDeviceMessage(long id) {
        this.id = id;
    }

    private PropertySpecBuilder<Boolean> booleanPropertySpecBuilder(String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return Services
                .propertySpecService()
                .booleanSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description());
    }
    protected PropertySpec booleanSpec(String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        return this.booleanPropertySpecBuilder(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation).finish();
    }

    protected PropertySpec booleanSpec(String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, Boolean defaultValue) {
        return this.booleanPropertySpecBuilder(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation)
                .setDefaultValue(defaultValue)
                .finish();
    }

    private PropertySpecBuilder<String> stringSpecBuilder(String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return Services
                .propertySpecService()
                .stringSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description());
    }

    protected PropertySpec stringSpec(String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        return this.stringSpecBuilder(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation).finish();
    }

    protected PropertySpec stringSpec(String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, String... possibleValues) {
        return this.stringSpecBuilder(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation)
                .addValues(possibleValues)
                .markExhaustive()
                .finish();
    }

    protected PropertySpec stringSpecWithDefaultAndOtherValues(String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, String defaultValue, String... possibleValues) {
        return this.stringSpecBuilder(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation)
                .setDefaultValue(defaultValue)
                .addValues(possibleValues)
                .markExhaustive()
                .finish();
    }

    protected PropertySpec passwordSpec(String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return Services
                .propertySpecService()
                .passwordSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .finish();
    }

    private PropertySpecBuilder<BigDecimal> bigDecimalPropertySpecBuilder(String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return Services
                .propertySpecService()
                .bigDecimalSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description());
    }

    protected PropertySpec bigDecimalSpec(String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        return this.bigDecimalPropertySpecBuilder(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation).finish();
    }

    protected PropertySpec bigDecimalSpec(String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, BigDecimal... possibleValues) {
        return this.bigDecimalPropertySpecBuilder(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation)
                .addValues(possibleValues)
                .markExhaustive()
                .finish();
    }

    protected PropertySpec bigDecimalSpecWithDefaultValue(String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, BigDecimal defaultValue) {
        return this.bigDecimalPropertySpecBuilder(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation)
                .setDefaultValue(defaultValue)
                .markExhaustive()
                .finish();
    }

    protected PropertySpec dateTimeSpec(String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return Services
                .propertySpecService()
                .dateTimeSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .finish();
    }

    protected PropertySpec durationSpec(String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation, Duration defaultValue) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return Services
                .propertySpecService()
                .durationSpec()
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .setDefaultValue(defaultValue)
                .finish();
    }

    protected PropertySpec deviceMessageFileSpec(String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return Services
                .propertySpecService()
                .referenceSpec(DeviceMessageFile.class)
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .finish();
    }

    protected PropertySpec deviceGroupSpec(String deviceMessageConstantKey, String deviceMessageConstantDefaultTranslation) {
        TranslationKeyImpl translationKey = new TranslationKeyImpl(deviceMessageConstantKey, deviceMessageConstantDefaultTranslation);
        return Services
                .propertySpecService()
                .referenceSpec(DeviceGroup.class)
                .named(deviceMessageConstantKey, translationKey)
                .describedAs(translationKey.description())
                .finish();
    }

    @Override
    public DeviceMessageCategory getCategory() {
        return DeviceMessageCategories.FIRMWARE;
    }

    @Override
    public String getName() {
        return Services
                .nlsService()
                .getThesaurus(Thesaurus.ID.toString())
                .getFormat(this.getNameTranslationKey())
                .format();
    }

    @Override
    public String getNameResourceKey() {
        return FirmwareDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    private TranslationKeyImpl getNameTranslationKey() {
        return new TranslationKeyImpl(this.getNameResourceKey(), "MR" + this.getNameResourceKey());
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        for (PropertySpec securityProperty : getPropertySpecs()) {
            if (securityProperty.getName().equals(name)) {
                return securityProperty;
            }
        }
        return null;
    }

    @Override
    public DeviceMessageSpecPrimaryKey getPrimaryKey() {
        return new DeviceMessageSpecPrimaryKey(this, name());
    }

    @Override
    public long getMessageId() {
        return id;
    }

}