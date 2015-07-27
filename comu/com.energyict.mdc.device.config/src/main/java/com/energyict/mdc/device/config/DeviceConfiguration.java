package com.energyict.mdc.device.config;


import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.util.HasName;
import com.elster.jupiter.util.collections.KPermutation;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.masterdata.ChannelType;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.masterdata.RegisterType;

import java.util.List;
import java.util.Set;

/**
 * User: gde
 * Date: 5/11/12
 */
@ProviderType
public interface DeviceConfiguration extends HasId, HasName, DeviceCommunicationConfiguration {

    void setName(String name);

    String getDescription();

    void setDescription(String description);

    DeviceType getDeviceType();

    List<RegisterSpec> getRegisterSpecs();

    NumericalRegisterSpec.Builder createNumericalRegisterSpec(RegisterType registerType);

    NumericalRegisterSpec.Updater getRegisterSpecUpdaterFor(NumericalRegisterSpec registerSpec);

    TextualRegisterSpec.Builder createTextualRegisterSpec(RegisterType registerType);

    TextualRegisterSpec.Updater getRegisterSpecUpdaterFor(TextualRegisterSpec registerSpec);

    void deleteRegisterSpec(RegisterSpec registerSpec);

    List<ChannelSpec> getChannelSpecs();

    ChannelSpec.ChannelSpecBuilder createChannelSpec(ChannelType channelType, LoadProfileSpec loadProfileSpec);

    ChannelSpec.ChannelSpecBuilder createChannelSpec(ChannelType channelType, LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder);

    ChannelSpec.ChannelSpecUpdater getChannelSpecUpdaterFor(ChannelSpec channelSpec);

    void removeChannelSpec(ChannelSpec channelSpec);

    List<LoadProfileSpec> getLoadProfileSpecs();

    LoadProfileSpec.LoadProfileSpecBuilder createLoadProfileSpec(LoadProfileType loadProfileType);

    LoadProfileSpec.LoadProfileSpecUpdater getLoadProfileSpecUpdaterFor(LoadProfileSpec loadProfileSpec);

    void deleteLoadProfileSpec(LoadProfileSpec loadProfileSpec);

    List<LogBookSpec> getLogBookSpecs();

    LogBookSpec.LogBookSpecBuilder createLogBookSpec(LogBookType logBookType);

    LogBookSpec.LogBookSpecUpdater getLogBookSpecUpdaterFor(LogBookSpec logBookSpec);

    void removeLogBookSpec(LogBookSpec logBookSpec);

    /**
     * Tests if the receiver is active.
     *
     * @return true if active, false otherwise
     */
    boolean isActive();

    void activate();

    void deactivate();

    boolean hasLogBookSpecForConfig(int logBookTypeId, int updateId);

    void save();

    Set<DeviceCommunicationFunction> getCommunicationFunctions();

    void addCommunicationFunction(DeviceCommunicationFunction function);

    void removeCommunicationFunction(DeviceCommunicationFunction function);

    boolean hasCommunicationFunction(DeviceCommunicationFunction function);

    public boolean canActAsGateway();
    public void setCanActAsGateway(boolean actAsGateway);

    public boolean isDirectlyAddressable();
    public void setDirectlyAddressable(boolean directlyAddressable);

    DeviceConfValidationRuleSetUsage addValidationRuleSet(ValidationRuleSet validationRuleSet);

    public void removeValidationRuleSet(ValidationRuleSet validationRuleSet);

    public List<ValidationRuleSet> getValidationRuleSets();

    public List<DeviceConfValidationRuleSetUsage> getDeviceConfValidationRuleSetUsages();

    DeviceConfigurationEstimationRuleSetUsage addEstimationRuleSet(EstimationRuleSet estimationRuleSet);

    void removeEstimationRuleSet(EstimationRuleSet estimationRuleSet);

    void reorderEstimationRuleSets(KPermutation kpermutation);

    List<EstimationRuleSet> getEstimationRuleSets();

    List<DeviceConfigurationEstimationRuleSetUsage> getDeviceConfigEstimationRuleSetUsages();

    public List<ValidationRule> getValidationRules(Iterable<? extends ReadingType> readingTypes);

    public GatewayType getGetwayType();

    public void setGatewayType(GatewayType gatewayType);
    //TODO we remove 'CreateDeviceTransaction' and 'DeviceConfigurationChanges' from the API, must be included when time comes ...

    /**
     * Gets the general protocol properties that have been specified on
     * this DeviceConfiguration level.
     *
     * @return The {@link DeviceProtocolConfigurationProperties}
     */
    public DeviceProtocolConfigurationProperties getDeviceProtocolProperties();

    public long getVersion();

}