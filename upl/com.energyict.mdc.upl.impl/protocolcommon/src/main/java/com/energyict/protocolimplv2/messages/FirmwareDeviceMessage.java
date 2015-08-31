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
    UPGRADE_FIRMWARE_WITH_USER_FILE_AND_IMAGE_IDENTIFIER(15,
            PropertySpecFactory.userFileReferencePropertySpec(firmwareUpdateUserFileAttributeName),
            PropertySpecFactory.stringPropertySpec(firmwareUpdateImageIdentifierAttributeName)),
    UPGRADE_FIRMWARE_WITH_USER_FILE_AND_ACTIVATE_AND_IMAGE_IDENTIFIER(14,
            PropertySpecFactory.userFileReferencePropertySpec(firmwareUpdateUserFileAttributeName),
            PropertySpecFactory.dateTimePropertySpec(firmwareUpdateActivationDateAttributeName),
            PropertySpecFactory.stringPropertySpec(firmwareUpdateImageIdentifierAttributeName)),
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
    BroadcastFirmwareUpgrade(14,
            PropertySpecFactory.groupReferencePropertySpec(DeviceMessageConstants.broadcastDevicesGroupAttributeName),
            PropertySpecFactory.bigDecimalPropertySpec(broadcastLogicalDeviceIdAttributeName),        //TODO also get the default hardcoded value from Alex?
            PropertySpecFactory.bigDecimalPropertySpec(broadcastClientMacAddressAttributeName, BigDecimal.valueOf(102)),
            PropertySpecFactory.bigDecimalPropertySpec(broadcastGroupIdAttributeName, BigDecimal.valueOf(1)),   //Default group 1 is broadcast (to all devices)
            PropertySpecFactory.bigDecimalPropertySpec(broadcastNumberOfBlocksInCycleAttributeName, BigDecimal.valueOf(100)),
            PropertySpecFactory.timeDurationPropertySpecWithSmallUnitsAndDefaultValue(broadcastInitialTimeBetweenBlocksAttributeName, TimeDuration.seconds(1)), //TODO check if this is a good default value??
            PropertySpecFactory.userFileReferencePropertySpec(firmwareUpdateUserFileAttributeName),
            PropertySpecFactory.stringPropertySpec(broadcastFirmwareUpdateImageIdentifierAttributeName),
            PropertySpecFactory.passwordPropertySpec(broadcastEncryptionKeyAttributeName),
            PropertySpecFactory.passwordPropertySpec(broadcastAuthenticationKeyAttributeName),
            PropertySpecFactory.stringPropertySpecWithValues(DeviceMessageConstants.encryptionLevelAttributeName, DlmsEncryptionLevelMessageValues.getNames())
    ),
    VerifyAndActivateFirmware(15);

    private static final DeviceMessageCategory firmwareCategory = DeviceMessageCategories.FIRMWARE;

    private final List<PropertySpec> deviceMessagePropertySpecs;
    private final int id;

    private FirmwareDeviceMessage(int id, PropertySpec... deviceMessagePropertySpecs) {
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
