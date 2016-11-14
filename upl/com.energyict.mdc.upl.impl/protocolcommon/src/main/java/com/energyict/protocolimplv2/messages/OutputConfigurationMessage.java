package com.energyict.protocolimplv2.messages;

import com.energyict.mdc.upl.messages.DeviceMessageCategory;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.DeviceMessageSpecPrimaryKey;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cuo.core.UserEnvironment;

import java.util.Arrays;
import java.util.List;

/**
 * Provides a summary of all messages related to general Device Actions
 * <p/>
 * Copyrights EnergyICT
 * Date: 11/03/13
 * Time: 11:59
 */
public enum OutputConfigurationMessage implements DeviceMessageSpec {

    SetOutputOn(0, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.OutputOn)),
    SetOutputOff(1, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.OutputOff)),
    SetOutputToggle(2, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.OutputToggle)),
    SetOutputPulse(3, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.OutputPulse)),
    OutputOff(4, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.output)),
    OutputOn(5, PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.output)),
    AbsoluteDOSwitchRule(6,
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.id),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.startTime),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.endTime),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.outputBitMap)
    ),
    DeleteDOSwitchRule(7,
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.id),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.delete)
    ),
    RelativeDOSwitchRule(8,
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.id),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.duration),
            PropertySpecFactory.stringPropertySpec(DeviceMessageConstants.outputBitMap)
    ),
    WriteOutputState(9,
            PropertySpecFactory.bigDecimalPropertySpec(DeviceMessageConstants.outputId),
            PropertySpecFactory.notNullableBooleanPropertySpec(DeviceMessageConstants.newState)
    ),;

    private final int id;

    public int getMessageId() {
        return id;
    }

    private static final DeviceMessageCategory category = DeviceMessageCategories.OUTPUT_CONFIGURATION;

    private final List<PropertySpec> deviceMessagePropertySpecs;

    private OutputConfigurationMessage(int id, PropertySpec... deviceMessagePropertySpecs) {
        this.id = id;
        this.deviceMessagePropertySpecs = Arrays.asList(deviceMessagePropertySpecs);
    }

    private static String translate(final String key) {
        return UserEnvironment.getDefault().getTranslation(key);
    }

    @Override
    public DeviceMessageCategory getCategory() {
        return category;
    }

    @Override
    public String getName() {
        return translate(this.getNameResourceKey());
    }

    /**
     * Gets the resource key that determines the name
     * of this category to the user's language settings.
     *
     * @return The resource key
     */
    private String getNameResourceKey() {
        return OutputConfigurationMessage.class.getSimpleName() + "." + this.toString();
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
}
