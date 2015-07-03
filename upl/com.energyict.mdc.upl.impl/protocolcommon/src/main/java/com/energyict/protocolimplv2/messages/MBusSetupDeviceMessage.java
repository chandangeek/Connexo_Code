package com.energyict.protocolimplv2.messages;

import com.energyict.cbo.HexString;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cuo.core.UserEnvironment;
import com.energyict.mdc.messages.DeviceMessageCategory;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.messages.DeviceMessageSpecPrimaryKey;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 28/02/13
 * Time: 9:10
 */
public enum MBusSetupDeviceMessage implements DeviceMessageSpec {

    Decommission(0),
    DataReadout(1),
    Commission(2),
    DecommissionAll(3),
    SetEncryptionKeys(4,
            PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.openKeyAttributeName),
            PropertySpecFactory.passwordPropertySpec(DeviceMessageConstants.transferKeyAttributeName)
    ),
    SetEncryptionKeysUsingCryptoserver(5,
            PropertySpecFactory.hexStringPropertySpec(DeviceMessageConstants.defaultKeyAttributeName)
    ),
    UseCorrectedValues(6),
    UseUncorrectedValues(7),
    WriteCaptureDefinition(8,
            PropertySpecFactory.hexStringPropertySpec(DeviceMessageConstants.dib),
            PropertySpecFactory.hexStringPropertySpec(DeviceMessageConstants.vib)
    ),
    Commission_With_Channel(9, PropertySpecFactory.bigDecimalPropertySpecWithValues(
            BigDecimal.valueOf(1),
            DeviceMessageConstants.mbusChannel,
            BigDecimal.valueOf(1),
            BigDecimal.valueOf(2),
            BigDecimal.valueOf(3),
            BigDecimal.valueOf(4)
    )),
    Reset_MBus_Client(10, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.mbusSerialNumber)),
    WriteCaptureDefinitionForAllInstances(11,
            PropertySpecFactory.hexStringPropertySpecWithDefaultValue(DeviceMessageConstants.dibInstance1, getCaptureDefinitionDefaultValue()),
            PropertySpecFactory.hexStringPropertySpecWithDefaultValue(DeviceMessageConstants.vibInstance1, getCaptureDefinitionDefaultValue()),
            PropertySpecFactory.hexStringPropertySpecWithDefaultValue(DeviceMessageConstants.dibInstance2, getCaptureDefinitionDefaultValue()),
            PropertySpecFactory.hexStringPropertySpecWithDefaultValue(DeviceMessageConstants.vibInstance2, getCaptureDefinitionDefaultValue()),
            PropertySpecFactory.hexStringPropertySpecWithDefaultValue(DeviceMessageConstants.dibInstance3, getCaptureDefinitionDefaultValue()),
            PropertySpecFactory.hexStringPropertySpecWithDefaultValue(DeviceMessageConstants.vibInstance3, getCaptureDefinitionDefaultValue()),
            PropertySpecFactory.hexStringPropertySpecWithDefaultValue(DeviceMessageConstants.dibInstance4, getCaptureDefinitionDefaultValue()),
            PropertySpecFactory.hexStringPropertySpecWithDefaultValue(DeviceMessageConstants.vibInstance4, getCaptureDefinitionDefaultValue())
    ),
    WriteMBusCapturePeriod(12, PropertySpecFactory.timeDurationPropertySpecWithSmallUnits(DeviceMessageConstants.capturePeriodAttributeName));

    private static HexString getCaptureDefinitionDefaultValue() {
        return new HexString("FFFFFFFFFFFFFFFFFFFFFF");
    }

    private static final DeviceMessageCategory category = DeviceMessageCategories.MBUS_SETUP;

    private final List<PropertySpec> deviceMessagePropertySpecs;
    private final int id;

    private MBusSetupDeviceMessage(int id, PropertySpec... deviceMessagePropertySpecs) {
        this.id = id;
        this.deviceMessagePropertySpecs = Arrays.asList(deviceMessagePropertySpecs);
    }

    @Override
    public DeviceMessageCategory getCategory() {
        return category;
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
        return MBusSetupDeviceMessage.class.getSimpleName() + "." + this.toString();
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