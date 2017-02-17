package com.energyict.protocolimplv2.messages;

import com.energyict.cbo.TimeDuration;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cuo.core.UserEnvironment;
import com.energyict.mdc.messages.DeviceMessageCategory;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.messages.DeviceMessageSpecPrimaryKey;
import com.energyict.protocolimplv2.messages.enums.DlmsEncryptionLevelMessageValues;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.*;

/**
 * Provides a summary of all <i>Firmware</i> related messages
 * <p/>
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum FirmwareDeviceMessage implements DeviceMessageSpec {

    UPGRADE_FIRMWARE_WITH_USER_FILE(0, PropertySpecFactory.userFileReferencePropertySpec(firmwareUpdateUserFileAttributeName)),
    UPGRADE_FIRMWARE_WITH_USER_FILE_AND_RESUME_OPTION(1,
            PropertySpecFactory.userFileReferencePropertySpec(firmwareUpdateUserFileAttributeName),
            PropertySpecFactory.notNullableBooleanPropertySpec(resumeFirmwareUpdateAttributeName, true)),
    UPGRADE_FIRMWARE_WITH_USER_FILE_AND_RESUME_OPTION_AND_TYPE(2,
            PropertySpecFactory.userFileReferencePropertySpec(firmwareUpdateUserFileAttributeName),
            PropertySpecFactory.notNullableBooleanPropertySpec(resumeFirmwareUpdateAttributeName, true),
            PropertySpecFactory.notNullableBooleanPropertySpec(plcTypeFirmwareUpdateAttributeName)),
    UPGRADE_FIRMWARE_ACTIVATE(3, PropertySpecFactory.dateTimePropertySpec(firmwareUpdateActivationDateAttributeName)),
    UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE(4,
            PropertySpecFactory.userFileReferencePropertySpec(firmwareUpdateUserFileAttributeName),
            PropertySpecFactory.dateTimePropertySpec(firmwareUpdateActivationDateAttributeName)),
    UPGRADE_FIRMWARE_WITH_USER_FILE_VERSION_AND_ACTIVATE(5, PropertySpecFactory.userFileReferencePropertySpec(firmwareUpdateUserFileAttributeName),
            PropertySpecFactory.dateTimePropertySpec(firmwareUpdateActivationDateAttributeName),
            PropertySpecFactory.stringPropertySpec(firmwareUpdateVersionNumberAttributeName)),
    UPGRADE_FIRMWARE_URL(6, PropertySpecFactory.stringPropertySpec(firmwareUpdateURLAttributeName)),
    UPGRADE_FIRMWARE_URL_AND_ACTIVATE(7,
            PropertySpecFactory.stringPropertySpec(firmwareUpdateURLAttributeName),
            PropertySpecFactory.dateTimePropertySpec(firmwareUpdateActivationDateAttributeName)),

    UpgradeWaveCard(8, PropertySpecFactory.userFileReferencePropertySpec(DeviceMessageConstants.waveCardFirmware)),
    PLCPrimeSetFirmwareUpgradeFile(9, PropertySpecFactory.userFileReferencePropertySpec(DeviceMessageConstants.firmwareUpdateUserFileAttributeName)),
    PLCPrimeStartFirmwareUpgradeNodeList(10, PropertySpecFactory.userFileReferencePropertySpec(DeviceMessageConstants.nodeListUserFile)),
    FTIONUpgradeRFMeshFirmware(11),
    RFMeshUpgradeURL(12, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.SetUpgradeUrlAttributeName)),
    UpgradeBootloader(13, PropertySpecFactory.userFileReferencePropertySpec(DeviceMessageConstants.PricingInformationUserFileAttributeName)),
    UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE_AND_IMAGE_IDENTIFIER(14,
            PropertySpecFactory.userFileReferencePropertySpec(firmwareUpdateUserFileAttributeName),
            PropertySpecFactory.dateTimePropertySpec(firmwareUpdateActivationDateAttributeName),
            PropertySpecFactory.stringPropertySpec(firmwareUpdateImageIdentifierAttributeName)),
    UPGRADE_FIRMWARE_WITH_USER_FILE_AND_IMAGE_IDENTIFIER(15,
            PropertySpecFactory.userFileReferencePropertySpec(firmwareUpdateUserFileAttributeName),
            PropertySpecFactory.stringPropertySpec(firmwareUpdateImageIdentifierAttributeName)),
    BroadcastFirmwareUpgrade(16,
            PropertySpecFactory.groupReferencePropertySpec(DeviceMessageConstants.broadcastDevicesGroupAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(broadcastLogicalDeviceIdAttributeName, BigDecimal.valueOf(16)),
            PropertySpecFactory.bigDecimalPropertySpec(broadcastClientMacAddressAttributeName, BigDecimal.valueOf(102)),
            PropertySpecFactory.bigDecimalPropertySpec(broadcastGroupIdAttributeName, BigDecimal.valueOf(1)),   //Default group 1 is broadcast (to all devices)
            PropertySpecFactory.bigDecimalPropertySpec(broadcastNumberOfBlocksInCycleAttributeName, BigDecimal.valueOf(100)),
            PropertySpecFactory.timeDurationPropertySpecWithSmallUnitsAndDefaultValue(broadcastInitialTimeBetweenBlocksAttributeName, TimeDuration.seconds(1)), //TODO check if this is a good default value??
            PropertySpecFactory.userFileReferencePropertySpec(firmwareUpdateUserFileAttributeName),
            PropertySpecFactory.stringPropertySpec(firmwareUpdateImageIdentifierAttributeName),
            PropertySpecFactory.passwordPropertySpec(broadcastEncryptionKeyAttributeName),
            PropertySpecFactory.passwordPropertySpec(broadcastAuthenticationKeyAttributeName),
            PropertySpecFactory.stringPropertySpecWithValues(DeviceMessageConstants.encryptionLevelAttributeName, DlmsEncryptionLevelMessageValues.getNames())
    ),
    VerifyAndActivateFirmware(17),
    DataConcentratorMulticastFirmwareUpgrade(18,
            PropertySpecFactory.stringPropertySpec(deviceIdsAttributeName),
            PropertySpecFactory.userFileReferencePropertySpec(firmwareUpdateUserFileAttributeName),
            PropertySpecFactory.stringPropertySpec(firmwareUpdateImageIdentifierAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(UnicastClientWPort, BigDecimal.ONE),
            PropertySpecFactory.bigDecimalPropertySpec(BroadcastClientWPort, BigDecimal.valueOf(64)),
            PropertySpecFactory.bigDecimalPropertySpec(MulticastClientWPort, BigDecimal.valueOf(102)),
            PropertySpecFactory.bigDecimalPropertySpec(LogicalDeviceLSap, BigDecimal.ONE),
            PropertySpecFactory.bigDecimalPropertySpec(SecurityLevelUnicast, BigDecimal.valueOf(3)),
            PropertySpecFactory.bigDecimalPropertySpec(SecurityLevelBroadcast, BigDecimal.valueOf(3)),
            PropertySpecFactory.bigDecimalPropertySpec(SecurityPolicyBroadcast, BigDecimal.ZERO),
            PropertySpecFactory.timeDurationPropertySpecWithSmallUnitsAndDefaultValue(DelayAfterLastBlock, new TimeDuration(5)),
            PropertySpecFactory.timeDurationPropertySpecWithSmallUnitsAndDefaultValue(DelayPerBlock, new TimeDuration(4)),
            PropertySpecFactory.timeDurationPropertySpecWithSmallUnitsAndDefaultValue(DelayBetweenBlockSentFast, new TimeDuration(250, TimeDuration.MILLISECONDS)),
            PropertySpecFactory.timeDurationPropertySpecWithSmallUnitsAndDefaultValue(DelayBetweenBlockSentSlow, new TimeDuration(500, TimeDuration.MILLISECONDS)),
            PropertySpecFactory.bigDecimalPropertySpec(BlocksPerCycle, BigDecimal.valueOf(30)),
            PropertySpecFactory.bigDecimalPropertySpec(MaxCycles, BigDecimal.ONE),
            PropertySpecFactory.bigDecimalPropertySpec(RequestedBlockSize, BigDecimal.valueOf(1024)),
            PropertySpecFactory.notNullableBooleanPropertySpec(PadLastBlock, false),
            PropertySpecFactory.notNullableBooleanPropertySpec(UseTransferredBlockStatus, true)
    ),
    ReadMulticastProgress(19),
    FirmwareUpgradeWithUrlJarJadFileSize(20,
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.URL_PATH),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.JAR_FILE_SIZE),
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.JAD_FILE_SIZE)),
    UPGRADE_FIRMWARE_WITH_USER_FILE_RESUME_AND_IMAGE_IDENTIFIER(21,
            PropertySpecFactory.userFileReferencePropertySpec(firmwareUpdateUserFileAttributeName),
            PropertySpecFactory.stringPropertySpec(firmwareUpdateImageIdentifierAttributeName),
            PropertySpecFactory.notNullableBooleanPropertySpec(resumeFirmwareUpdateAttributeName, true)),
    ENABLE_IMAGE_TRANSFER(
    		22,
    		PropertySpecFactory.notNullableBooleanPropertySpec(FW_UPGRADE_INITIATE_ENABLE_AND_INITIATE, false),
    		PropertySpecFactory.bigDecimalPropertySpec(FW_UPGADE_IMAGE_SIZE),
    		PropertySpecFactory.stringPropertySpec(firmwareUpdateImageIdentifierAttributeName)
    ),
    TRANSFER_SLAVE_FIRMWARE_FILE_TO_DATA_CONCENTRATOR(23,
            PropertySpecFactory.userFileReferencePropertySpec(firmwareUpdateUserFileAttributeName),
            PropertySpecFactory.stringPropertySpec(firmwareUpdateImageIdentifierAttributeName)
    ),
    CONFIGURE_MULTICAST_BLOCK_TRANSFER_TO_SLAVE_DEVICES(24,
            PropertySpecFactory.stringPropertySpec(deviceIdsAttributeName),
            PropertySpecFactory.notNullableBooleanPropertySpec(SkipStepEnable, true),
            PropertySpecFactory.notNullableBooleanPropertySpec(SkipStepVerify, true),
            PropertySpecFactory.notNullableBooleanPropertySpec(SkipStepActivate, true),
            PropertySpecFactory.bigDecimalPropertySpec(UnicastClientWPort, BigDecimal.valueOf(2)),
            PropertySpecFactory.bigDecimalPropertySpec(MulticastClientWPort, BigDecimal.valueOf(3)),
            PropertySpecFactory.stringPropertySpecWithValuesAndDefaultValue(UnicastFrameCounterType, "auth_hmac_sha256", "default", "auth_hmac_sha256"),
            PropertySpecFactory.stringPropertySpecWithValuesAndDefaultValue(MeterTimeZone, "Europe/Vienna", TimeZone.getAvailableIDs()),
            PropertySpecFactory.bigDecimalPropertySpec(SecurityLevelMulticast, BigDecimal.ZERO),
            PropertySpecFactory.bigDecimalPropertySpec(SecurityPolicyMulticastV0, BigDecimal.ZERO),
            PropertySpecFactory.timeDurationPropertySpecWithSmallUnitsAndDefaultValue(DelayBetweenBlockSentFast, new TimeDuration(20, TimeDuration.MILLISECONDS))
    ),
    START_MULTICAST_BLOCK_TRANSFER_TO_SLAVE_DEVICES(25),
    FIRMWARE_IMAGE_ACTIVATION_WITH_DATA_PROTECTION(26),
    COPY_ACTIVE_FIRMWARE_TO_INACTIVE_PARTITION(27)
    ;
    private static final DeviceMessageCategory firmwareCategory = DeviceMessageCategories.FIRMWARE;

    private final List<PropertySpec> deviceMessagePropertySpecs;
    private final int id;

    FirmwareDeviceMessage(int id, PropertySpec... deviceMessagePropertySpecs) {
        this.id = id;
        this.deviceMessagePropertySpecs = Arrays.asList(deviceMessagePropertySpecs);
    }

    @Override
    public DeviceMessageCategory getCategory() {
        return firmwareCategory;
    }

    @Override
    public String getName() {
        return UserEnvironment.getDefault().getTranslation(this.getNameResourceKey());
    }

    /**
     * Gets the resource key that determines the name
     * of this category to the user's language settings.
     *
     * @return The resource key
     */
    private String getNameResourceKey() {
        return FirmwareDeviceMessage.class.getSimpleName() + "." + this.toString();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return this.deviceMessagePropertySpecs;
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
    public int getMessageId() {
        return id;
    }
}
