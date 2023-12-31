/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.device.config;


import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;
import com.elster.jupiter.util.collections.KPermutation;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.energyict.mdc.common.masterdata.ChannelType;
import com.energyict.mdc.common.masterdata.LoadProfileType;
import com.energyict.mdc.common.masterdata.LogBookType;
import com.energyict.mdc.common.masterdata.RegisterType;

import aQute.bnd.annotation.ConsumerType;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@ConsumerType
@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@XmlAccessorType(XmlAccessType.NONE)
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

    void touch();

    Set<DeviceCommunicationFunction> getCommunicationFunctions();

    void addCommunicationFunction(DeviceCommunicationFunction function);

    void removeCommunicationFunction(DeviceCommunicationFunction function);

    boolean hasCommunicationFunction(DeviceCommunicationFunction function);

    boolean canActAsGateway();

    void setCanActAsGateway(boolean actAsGateway);

    boolean isDirectlyAddressable();

    void setDirectlyAddressable(boolean directlyAddressable);

    DeviceConfValidationRuleSetUsage addValidationRuleSet(ValidationRuleSet validationRuleSet);

    void setValidationRuleSetStatus(ValidationRuleSet validationRuleSet, boolean status);

    boolean getValidationRuleSetStatus(ValidationRuleSet validationRuleSet);

    void removeValidationRuleSet(ValidationRuleSet validationRuleSet);

    List<ValidationRuleSet> getValidationRuleSets();

    List<DeviceConfValidationRuleSetUsage> getDeviceConfValidationRuleSetUsages();

    DeviceConfigurationEstimationRuleSetUsage addEstimationRuleSet(EstimationRuleSet estimationRuleSet);

    boolean isEstimationRuleSetActiveOnDeviceConfig(long estimationRuleSetId);

    void setEstimationRuleSetStatus(EstimationRuleSet estimationRuleSet, boolean status);

    boolean getEstimationRuleSetStatus(EstimationRuleSet estimationRuleSet);

    void removeEstimationRuleSet(EstimationRuleSet estimationRuleSet);

    void reorderEstimationRuleSets(KPermutation kpermutation);

    List<EstimationRuleSet> getEstimationRuleSets();

    List<DeviceConfigurationEstimationRuleSetUsage> getDeviceConfigEstimationRuleSetUsages();

    List<ValidationRule> getValidationRules(Collection<? extends ReadingType> readingTypes);

    GatewayType getGatewayType();

    void setGatewayType(GatewayType gatewayType);

    /**
     * Gets the general protocol properties that have been specified on
     * this DeviceConfiguration level.
     *
     * @return The {@link DeviceProtocolConfigurationProperties}
     */
    DeviceProtocolConfigurationProperties getDeviceProtocolProperties();

    long getVersion();

    void setDataloggerEnabled(boolean enabled);

    boolean getValidateOnStore();

    void setValidateOnStore(boolean validateOnStore);

    /**
     * Indicate whether or not this DeviceConfiguration allows itself to be
     * used as a Datalogger
     *
     * @return true if this DeviceConfiguration can be a Datalogger, false otherwise
     */
    boolean isDataloggerEnabled();

    /**
     * Indicate whether or not this DeviceConfiguration allows itself to be
     * used as a Multi-Element meter
     *
     * @return true if this DeviceConfiguration can be a Multi-Element meter, false otherwise
     */
    boolean isMultiElementEnabled();

    void setMultiElementEnabled(boolean enabled);

    boolean isDefault();

    /**
     * Sets the current device configuration as default on a device type.
     * Sets the old configuration default status to false
     * @param value true if setAsDefault, false if removeAsDefault
     */
    void setDefaultStatus(boolean value);

    /**
     * Received key accessor type if configured.
     *
     * @param keyAccessorType
     * @return
     */
    boolean isConfigured(SecurityAccessorType keyAccessorType);

    // The element below is only used during JSON xml (un)marshalling.
    @XmlElement(name = "type")
    String getXmlType();

    void setXmlType(String ignore);
}