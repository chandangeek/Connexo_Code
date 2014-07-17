package com.energyict.mdc.device.config;


import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.masterdata.ChannelType;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.masterdata.MeasurementType;
import com.energyict.mdc.masterdata.RegisterType;

import java.util.List;
import java.util.Set;

/**
 * User: gde
 * Date: 5/11/12
 */
public interface DeviceConfiguration extends HasId, DeviceCommunicationConfiguration {


    /**
     * Returns the object's unique id
     *
     * @return the id
     */
    public long getId();

    /**
     * Returns the object's name
     *
     * @return the name
     */
    public String getName();

    void setName(String name);

    /**
     * Returns a description of the receiver
     *
     * @return description
     */
    String getDescription();

    void setDescription(String description);
    /**
     * Returns the <code>DeviceType</code> this device config belongs to
     *
     * @return the <code>DeviceType</code> this device config belongs to
     */
    DeviceType getDeviceType();

    List<RegisterSpec> getRegisterSpecs();

    RegisterSpec.RegisterSpecBuilder createRegisterSpec(RegisterType registerType);

    RegisterSpec.RegisterSpecUpdater getRegisterSpecUpdaterFor(RegisterSpec registerSpec);

    void deleteRegisterSpec(RegisterSpec registerSpec);

    List<ChannelSpec> getChannelSpecs();

    ChannelSpec.ChannelSpecBuilder createChannelSpec(ChannelType channelType, Phenomenon phenomenon, LoadProfileSpec loadProfileSpec);

    ChannelSpec.ChannelSpecBuilder createChannelSpec(ChannelType channelType, Phenomenon phenomenon, LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder);

    ChannelSpec.ChannelSpecUpdater getChannelSpecUpdaterFor(ChannelSpec channelSpec);

    void deleteChannelSpec(ChannelSpec channelSpec);

    List<LoadProfileSpec> getLoadProfileSpecs();

    LoadProfileSpec.LoadProfileSpecBuilder createLoadProfileSpec(LoadProfileType loadProfileType);

    LoadProfileSpec.LoadProfileSpecUpdater getLoadProfileSpecUpdaterFor(LoadProfileSpec loadProfileSpec);

    void deleteLoadProfileSpec(LoadProfileSpec loadProfileSpec);

    List<LogBookSpec> getLogBookSpecs();

    LogBookSpec.LogBookSpecBuilder createLogBookSpec(LogBookType logBookType);

    LogBookSpec.LogBookSpecUpdater getLogBookSpecUpdaterFor(LogBookSpec logBookSpec);

    void deleteLogBookSpec(LogBookSpec logBookSpec);

    /**
     * Tests if the receiver is active.
     *
     * @return true if active, false otherwise
     */
    boolean isActive();

    void activate();

    void deactivate();

    boolean hasLogBookSpecForConfig(int logBookTypeId, int updateId);

    /**
     * Gets the details of this DeviceConfiguration that relate to communication.
     *
     * @return The DeviceCommunicationConfiguration
     */
    public DeviceCommunicationConfiguration getCommunicationConfiguration();

    void save();

    Set<DeviceCommunicationFunction> getCommunicationFunctions();

    void addCommunicationFunction(DeviceCommunicationFunction function);

    void removeCommunicationFunction(DeviceCommunicationFunction function);

    boolean hasCommunicationFunction(DeviceCommunicationFunction function);

    public boolean canActAsGateway();
    public void setCanActAsGateway(boolean actAsGateway);

    public boolean canBeDirectlyAddressable();
    public void setCanBeDirectlyAddressed(boolean canBeDirectlyAddressed);

    DeviceConfValidationRuleSetUsage addValidationRuleSet(ValidationRuleSet validationRuleSet);

    public void removeValidationRuleSet(ValidationRuleSet validationRuleSet);

    public List<ValidationRuleSet> getValidationRuleSets();

    public List<DeviceConfValidationRuleSetUsage> getDeviceConfValidationRuleSetUsages();

    public List<ValidationRule> getValidationRules(List readingTypes);

    //TODO we remove 'CreateDeviceTransaction' and 'DeviceConfigurationChanges' from the API, must be included when time comes ...

}