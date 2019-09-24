/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.domain.util.VerboseConstraintViolationException;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.collections.KPermutation;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.energyict.mdc.common.device.config.ChannelSpec;
import com.energyict.mdc.common.device.config.ComTaskEnablement;
import com.energyict.mdc.common.device.config.ComTaskEnablementBuilder;
import com.energyict.mdc.common.device.config.ConnectionStrategy;
import com.energyict.mdc.common.device.config.DeleteEventType;
import com.energyict.mdc.common.device.config.DeviceCommunicationFunction;
import com.energyict.mdc.common.device.config.DeviceConfValidationRuleSetUsage;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.config.DeviceConfigurationEstimationRuleSetUsage;
import com.energyict.mdc.common.device.config.DeviceMessageEnablement;
import com.energyict.mdc.common.device.config.DeviceMessageEnablementBuilder;
import com.energyict.mdc.common.device.config.DeviceMessageUserAction;
import com.energyict.mdc.common.device.config.DeviceProtocolConfigurationProperties;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.config.EventType;
import com.energyict.mdc.common.device.config.GatewayType;
import com.energyict.mdc.common.device.config.LoadProfileSpec;
import com.energyict.mdc.common.device.config.LogBookSpec;
import com.energyict.mdc.common.device.config.NumericalRegisterSpec;
import com.energyict.mdc.common.device.config.PartialConnectionInitiationTask;
import com.energyict.mdc.common.device.config.PartialConnectionInitiationTaskBuilder;
import com.energyict.mdc.common.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.common.device.config.PartialInboundConnectionTaskBuilder;
import com.energyict.mdc.common.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.common.device.config.PartialScheduledConnectionTaskBuilder;
import com.energyict.mdc.common.device.config.RegisterSpec;
import com.energyict.mdc.common.device.config.SecurityPropertySet;
import com.energyict.mdc.common.device.config.SecurityPropertySetBuilder;
import com.energyict.mdc.common.device.config.ServerPartialConnectionTask;
import com.energyict.mdc.common.device.config.TextualRegisterSpec;
import com.energyict.mdc.common.masterdata.ChannelType;
import com.energyict.mdc.common.masterdata.LoadProfileType;
import com.energyict.mdc.common.masterdata.LogBookType;
import com.energyict.mdc.common.masterdata.MeasurementType;
import com.energyict.mdc.common.masterdata.RegisterType;
import com.energyict.mdc.common.protocol.ConnectionFunction;
import com.energyict.mdc.common.protocol.ConnectionTypePluggableClass;
import com.energyict.mdc.common.protocol.DeviceMessageCategory;
import com.energyict.mdc.common.protocol.DeviceMessageId;
import com.energyict.mdc.common.protocol.DeviceMessageSpec;
import com.energyict.mdc.common.protocol.DeviceProtocolDialect;
import com.energyict.mdc.common.protocol.DeviceProtocolPluggableClass;
import com.energyict.mdc.common.protocol.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.common.tasks.PartialConnectionTask;
import com.energyict.mdc.device.config.exceptions.CannotAddToActiveDeviceConfigurationException;
import com.energyict.mdc.device.config.exceptions.CannotDeleteFromActiveDeviceConfigurationException;
import com.energyict.mdc.device.config.exceptions.CannotDisableComTaskThatWasNotEnabledException;
import com.energyict.mdc.device.config.exceptions.DataloggerSlaveException;
import com.energyict.mdc.device.config.exceptions.DeviceConfigurationIsActiveException;
import com.energyict.mdc.device.config.exceptions.DeviceTypeIsRequiredException;
import com.energyict.mdc.device.config.exceptions.DuplicateLoadProfileTypeException;
import com.energyict.mdc.device.config.exceptions.DuplicateLogBookTypeException;
import com.energyict.mdc.device.config.exceptions.DuplicateObisCodeException;
import com.energyict.mdc.scheduling.SchedulingService;

import com.energyict.obis.ObisCode;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.security.Principal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.conditions.Where.where;

/**
 * Provides an implementation for the {@link DeviceConfiguration} interface.
 */
@DeviceFunctionsAreSupportedByProtocol(groups = {Save.Update.class, Save.Create.class})
@ImmutablePropertiesCanNotChangeForActiveConfiguration(groups = {Save.Update.class, Save.Create.class})
@GatewayTypeMustBeSpecified(groups = {Save.Update.class, Save.Create.class})
@ValidDataSources(groups = {Save.Update.class})
public class DeviceConfigurationImpl extends PersistentNamedObject<DeviceConfiguration> implements DeviceConfiguration, ServerDeviceConfiguration {

    private static final DeviceCommunicationFunctionSetPersister deviceCommunicationFunctionSetPersister = new DeviceCommunicationFunctionSetPersister();

    enum Fields {
        CAN_ACT_AS_GATEWAY("canActAsGateway"), // 'virtual' BeanProperty not backed by actual member
        IS_DIRECTLY_ADDRESSABLE("isDirectlyAddressable"), // 'virtual' BeanProperty not backed by actual member
        GATEWAY_TYPE("gatewayType"),
        COM_TASK_ENABLEMENTS("comTaskEnablements"),
        SECURITY_PROPERTY_SETS("securityPropertySets"),
        DEVICE_MESSAGE_ENABLEMENTS("deviceMessageEnablements"),
        DEVICECONF_ESTIMATIONRULESET_USAGES("deviceConfigurationEstimationRuleSetUsages"),
        DATALOGGER_ENABLED("dataloggerEnabled"),
        VALIDATE_ON_STORE("validateOnStore"),
        MULTI_ELEMENT_ENABLED("multiElementEnabled"),
        IS_DEFAULT("isDefault"),
        DEVICETYPE("deviceType");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }

    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private String name;
    @Size(max = 4000, groups = {Save.Update.class, Save.Create.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String description;

    private boolean active;

    private final Reference<DeviceType> deviceType = ValueReference.absent();
    @Valid
    private List<RegisterSpec> registerSpecs = new ArrayList<>();
    @Valid
    private List<LoadProfileSpecImpl> loadProfileSpecs = new ArrayList<>();
    @Valid
    private List<LogBookSpec> logBookSpecs = new ArrayList<>();
    private List<ServerSecurityPropertySet> securityPropertySets = new ArrayList<>();
    private List<ComTaskEnablement> comTaskEnablements = new ArrayList<>();
    private List<DeviceMessageEnablementImpl> deviceMessageEnablements = new ArrayList<>();
    private boolean supportsAllProtocolMessages;
    private long supportsAllProtocolMessagesUserActionsBitVector = 0L;
    @Valid
    private List<ServerPartialConnectionTask> partialConnectionTasks = new ArrayList<>();
    @Valid
    private List<ProtocolDialectConfigurationPropertiesImpl> configurationPropertiesList = new ArrayList<>();
    private Set<DeviceCommunicationFunction> deviceCommunicationFunctions;
    private int communicationFunctionMask;
    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    private GatewayType gatewayType = GatewayType.NONE;
    @Valid
    private List<DeviceProtocolConfigurationProperty> protocolProperties = new ArrayList<>();
    private ProtocolConfigurationPropertyChanges protocolConfigurationPropertyChanges = new ProtocolConfigurationPropertyChanges();
    private Provider<LoadProfileSpecImpl> loadProfileSpecProvider;
    private Provider<NumericalRegisterSpecImpl> numericalRegisterSpecProvider;
    private Provider<TextualRegisterSpecImpl> textualRegisterSpecProvider;
    private Provider<LogBookSpecImpl> logBookSpecProvider;
    private Provider<ChannelSpecImpl> channelSpecProvider;
    private SchedulingService schedulingService;
    private ThreadPrincipalService threadPrincipalService;

    private List<DeviceConfValidationRuleSetUsage> deviceConfValidationRuleSetUsages = new ArrayList<>();
    private Provider<DeviceConfValidationRuleSetUsageImpl> deviceConfValidationRuleSetUsageFactory;

    private List<DeviceConfigurationEstimationRuleSetUsage> deviceConfigurationEstimationRuleSetUsages = new ArrayList<>();
    private Provider<DeviceConfigurationEstimationRuleSetUsageImpl> deviceConfigEstimationRuleSetUsageFactory;
    private boolean dataloggerEnabled;
    private boolean multiElementEnabled;
    private boolean validateOnStore;
    private boolean isDefault;

    private PropertySpecService propertySpecService;

    protected DeviceConfigurationImpl() {
        super();
    }

    @Inject
    protected DeviceConfigurationImpl(
            DataModel dataModel, EventService eventService, Thesaurus thesaurus,
            PropertySpecService propertySpecService,
            Provider<LoadProfileSpecImpl> loadProfileSpecProvider,
            Provider<NumericalRegisterSpecImpl> numericalRegisterSpecProvider,
            Provider<TextualRegisterSpecImpl> textualRegisterSpecProvider,
            Provider<LogBookSpecImpl> logBookSpecProvider,
            Provider<ChannelSpecImpl> channelSpecProvider,
            Provider<DeviceConfValidationRuleSetUsageImpl> deviceConfValidationRuleSetUsageFactory,
            Provider<DeviceConfigurationEstimationRuleSetUsageImpl> deviceConfEstimationRuleSetUsageFactory,
            SchedulingService schedulingService,
            ThreadPrincipalService threadPrincipalService) {
        super(DeviceConfiguration.class, dataModel, eventService, thesaurus);
        this.propertySpecService = propertySpecService;
        this.loadProfileSpecProvider = loadProfileSpecProvider;
        this.numericalRegisterSpecProvider = numericalRegisterSpecProvider;
        this.textualRegisterSpecProvider = textualRegisterSpecProvider;
        this.logBookSpecProvider = logBookSpecProvider;
        this.channelSpecProvider = channelSpecProvider;
        this.deviceConfValidationRuleSetUsageFactory = deviceConfValidationRuleSetUsageFactory;
        this.deviceConfigEstimationRuleSetUsageFactory = deviceConfEstimationRuleSetUsageFactory;
        this.schedulingService = schedulingService;
        this.threadPrincipalService = threadPrincipalService;
    }

    DeviceConfigurationImpl initialize(DeviceType deviceType, String name) {
        this.deviceType.set(deviceType);
        setName(name);
        if (!getDeviceType().isDataloggerSlave() && ! getDeviceType().isMultiElementSlave()) {
            this.getDeviceType()
                    .getDeviceProtocolPluggableClass()
                    .ifPresent(deviceProtocolPluggableClass -> deviceProtocolPluggableClass
                            .getDeviceProtocol()
                            .getDeviceProtocolDialects()
                            .forEach(this::findOrCreateProtocolDialectConfigurationProperties));
        }
        return this;
    }

    @Override
    @XmlAttribute
    public String getName() {
        return name;
    }

    @Override
    protected void doSetName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }


    @Override
    public Set<DeviceCommunicationFunction> getCommunicationFunctions() {
        if (this.deviceCommunicationFunctions == null) {
            this.deviceCommunicationFunctions = this.createSetFromMasks(this.communicationFunctionMask);
        }
        return EnumSet.copyOf(this.deviceCommunicationFunctions);
    }

    private Set<DeviceCommunicationFunction> createSetFromMasks(int communicationFunctionMask) {
        return deviceCommunicationFunctionSetPersister.fromDb(communicationFunctionMask);
    }

    @Override
    public boolean hasCommunicationFunction(DeviceCommunicationFunction function) {
        return this.getCommunicationFunctions().contains(function);
    }

    @Override
    public boolean canActAsGateway() {
        return hasCommunicationFunction(DeviceCommunicationFunction.GATEWAY);
    }

    @Override
    public void setCanActAsGateway(boolean actAsGateway) {
        if (actAsGateway) {
            addCommunicationFunction(DeviceCommunicationFunction.GATEWAY);
        } else {
            removeCommunicationFunction(DeviceCommunicationFunction.GATEWAY);
            this.gatewayType = GatewayType.NONE;
        }
    }

    @Override
    public boolean isDirectlyAddressable() {
        return hasCommunicationFunction(DeviceCommunicationFunction.PROTOCOL_SESSION);
    }

    @Override
    public void setDirectlyAddressable(boolean directlyAddressable) {
        if (directlyAddressable) {
            addCommunicationFunction(DeviceCommunicationFunction.PROTOCOL_SESSION);
        } else {
            removeCommunicationFunction(DeviceCommunicationFunction.PROTOCOL_SESSION);
        }
    }

    @Override
    public void addCommunicationFunction(DeviceCommunicationFunction function) {
        this.getCommunicationFunctions();   // Load the current set
        this.deviceCommunicationFunctions.add(function);
        this.communicationFunctionMask = deviceCommunicationFunctionSetPersister.toDb(this.deviceCommunicationFunctions);
    }

    @Override
    public void removeCommunicationFunction(DeviceCommunicationFunction function) {
        this.getCommunicationFunctions();   // Load the current set
        this.deviceCommunicationFunctions.remove(function);
        this.communicationFunctionMask = deviceCommunicationFunctionSetPersister.toDb(this.deviceCommunicationFunctions);
    }

    @Override
    protected boolean validateUniqueName() {
        String name = this.getName();
        for (DeviceConfiguration deviceConfiguration : this.deviceType.get().getConfigurations()) {
            if (!isSameIdObject(deviceConfiguration, this) && deviceConfiguration.getName().equals(name)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void notifyDelete() {
        validateDelete();
    }

    @Override
    public void validateUpdateLoadProfileType(LoadProfileType loadProfileType) {
        this.validateAllLoadProfileSpecsHaveUniqueObisCodes(loadProfileType);
    }

    private void validateAllLoadProfileSpecsHaveUniqueObisCodes(LoadProfileType loadProfileType) {
        Set<String> loadProfileObisCodes = new HashSet<>();
        for (LoadProfileSpec each : this.getLoadProfileSpecs()) {
            String obisCodeValue = each.getObisCode().toString();
            if (!loadProfileObisCodes.contains(obisCodeValue)) {
                loadProfileObisCodes.add(obisCodeValue);
            } else {
                throw new DuplicateLoadProfileTypeException(this, loadProfileType, each, this.getThesaurus(), MessageSeeds.DEVICE_CONFIGURATION_DUPLICATE_LOAD_PROFILE_TYPE_IN_SPEC);
            }
        }
    }

    @Override
    public void validateUpdateLogBookType(LogBookType logBookType) {
        this.validateAllLogBookTypesHaveUniqueObisCodes();
    }

    private void validateAllLogBookTypesHaveUniqueObisCodes() {
        Map<String, String> obisCodeAndNameMap = new HashMap<>();
        for (LogBookSpec each : this.getLogBookSpecs()) {
            LogBookType eachLogBookType = each.getLogBookType();
            String obisCodeValue = this.findNextAvailableObisCode(eachLogBookType.getObisCode()
                    .getValue(), obisCodeAndNameMap.keySet());
            String logBookSpecName = eachLogBookType.getName();
            if (!obisCodeAndNameMap.containsKey(obisCodeValue)) {
                obisCodeAndNameMap.put(obisCodeValue, logBookSpecName);
            } else {
                throw DuplicateObisCodeException.forLogBookSpec(this, each.getDeviceObisCode(), this.getThesaurus(), MessageSeeds.DEVICE_CONFIGURATION_DUPLICATE_OBIS_CODE_FOR_LOGBOOK_SPEC);
            }
        }
    }

    @Override
    public void validateUpdateMeasurementTypes(MeasurementType measurementType) {
        this.validateAllChannelSpecsHaveUniqueObisCodes();
        this.validateAllRegisterSpecsHaveUniqueObisCodes();
    }

    @Override
    public void prepareDelete() {
        this.registerSpecs.clear();
        this.logBookSpecs.clear();
        this.loadProfileSpecs.forEach(LoadProfileSpec::prepareDelete);
        this.loadProfileSpecs.clear();
        this.comTaskEnablements.clear();
        this.partialConnectionTasks.forEach(ServerPartialConnectionTask::prepareDelete);
        this.partialConnectionTasks.clear();
        this.configurationPropertiesList.forEach(ProtocolDialectConfigurationPropertiesImpl::prepareDelete);
        this.configurationPropertiesList.clear();
        this.deviceConfValidationRuleSetUsages.clear();
        this.deviceConfigurationEstimationRuleSetUsages.clear();
        this.deleteChannelSpecs();
        this.deleteDeviceMessageEnablements();
        this.protocolProperties.clear();
        this.securityPropertySets.forEach(ServerSecurityPropertySet::prepareDelete);
        this.securityPropertySets.clear();
    }

    private void deleteChannelSpecs() {
        this.getDataModel()
                .mapper(ChannelSpec.class)
                .find(ChannelSpecImpl.ChannelSpecFields.DEVICE_CONFIG.fieldName(), this)
                .stream()
                .map(ServerChannelSpec.class::cast)
                .forEach(ServerChannelSpec::configurationBeingDeleted);
    }

    private void deleteDeviceMessageEnablements() {
        this.deviceMessageEnablements.forEach(DeviceMessageEnablementImpl::prepareDelete);
        this.deviceMessageEnablements.clear();
    }

    private void removeDeviceMessageEnablement(DeviceMessageEnablementImpl obsolete) {
        obsolete.prepareDelete();
        this.deviceMessageEnablements.remove(obsolete);
    }

    private void validateAllChannelSpecsHaveUniqueObisCodes() {
        Map<Long, Set<String>> loadProfileTypeObisCodes = new HashMap<>();
        for (ChannelSpec each : this.getChannelSpecs()) {
            ObisCode obisCode = each.getChannelType().getObisCode();
            String obisCodeValue = obisCode.getValue();
            long loadProfileTypeId = each.getLoadProfileSpec().getLoadProfileType().getId();
            Set<String> obisCodesForLoadProfileType = loadProfileTypeObisCodes.get(loadProfileTypeId);
            if (obisCodesForLoadProfileType == null) {
                obisCodesForLoadProfileType = new HashSet<>();
                loadProfileTypeObisCodes.put(loadProfileTypeId, obisCodesForLoadProfileType);
            }
            if (!obisCodesForLoadProfileType.contains(obisCodeValue)) {
                obisCodesForLoadProfileType.add(obisCodeValue);
            } else {
                if (!obisCode.anyChannel()) {
                    throw DuplicateObisCodeException.forChannelSpecInLoadProfileSpec(this.getThesaurus(), this, each.getDeviceObisCode(), each
                            .getLoadProfileSpec(), MessageSeeds.DEVICE_CONFIGURATION_DUPLICATE_OBIS_CODE_FOR_CHANNEL_SPEC_IN_LOAD_PROFILE_SPEC);
                }
            }
        }
    }

    private void validateAllRegisterSpecsHaveUniqueObisCodes() {
        Set<String> obisCodeSet = new HashSet<>();
        for (RegisterSpec registerSpec : this.getRegisterSpecs()) {
            String obisCodeValue = this.findNextAvailableObisCode(registerSpec.getDeviceObisCode()
                    .toString(), obisCodeSet);
            if (!obisCodeSet.contains(obisCodeValue)) {
                obisCodeSet.add(obisCodeValue);
            } else {
                throw DuplicateObisCodeException.forRegisterSpec(this.getThesaurus(), this, registerSpec.getDeviceObisCode(), MessageSeeds.DEVICE_CONFIGURATION_DUPLICATE_OBIS_CODE_FOR_REGISTER_SPEC);
            }
        }
    }

    /**
     * Looks for the next available ObisCode with different B-field.
     */
    private String findNextAvailableObisCode(String obisCodeValue, Collection<String> obisCodeKeys) {
        String availableObisCode = obisCodeValue;
        while (obisCodeKeys.contains(availableObisCode)) {
            ObisCode obisCode = ObisCode.fromString(availableObisCode).nextB();
            availableObisCode = obisCode.toString();
        }
        return availableObisCode;
    }

    @Override
    public DeviceType getDeviceType() {
        return this.deviceType.get();
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType.set(deviceType);
    }

    @Override
    public List<RegisterSpec> getRegisterSpecs() {
        return Collections.unmodifiableList(this.registerSpecs);
    }

    @Override
    public NumericalRegisterSpec.Builder createNumericalRegisterSpec(RegisterType registerType) {
        return new NumericalRegisterSpecBuilderForConfig(this.numericalRegisterSpecProvider, this, registerType);
    }

    private class NumericalRegisterSpecBuilderForConfig extends NumericalRegisterSpecImpl.AbstractBuilder {

        NumericalRegisterSpecBuilderForConfig(Provider<NumericalRegisterSpecImpl> registerSpecProvider, DeviceConfiguration deviceConfiguration, RegisterType registerType) {
            super(registerSpecProvider, deviceConfiguration, registerType);
        }

        @Override
        public NumericalRegisterSpec add() {
            NumericalRegisterSpec registerSpec = super.add();
            validateActiveDeviceConfiguration(CannotAddToActiveDeviceConfigurationException.aNewRegisterSpec(getThesaurus(), MessageSeeds.REGISTER_SPEC_CANNOT_ADD_TO_ACTIVE_CONFIG));
            validateUniqueRegisterSpecObisCode(registerSpec);
            DeviceConfigurationImpl.this.registerSpecs.add(registerSpec);
            if (DeviceConfigurationImpl.this.getId() > 0) {
                getDataModel().touch(DeviceConfigurationImpl.this);
            }
            return registerSpec;
        }
    }

    @Override
    public TextualRegisterSpec.Builder createTextualRegisterSpec(RegisterType registerType) {
        return new TextualRegisterSpecBuilderForConfig(this.textualRegisterSpecProvider, this, registerType);
    }

    private class TextualRegisterSpecBuilderForConfig extends TextualRegisterSpecImpl.AbstractBuilder {

        TextualRegisterSpecBuilderForConfig(Provider<TextualRegisterSpecImpl> registerSpecProvider, DeviceConfiguration deviceConfiguration, RegisterType registerType) {
            super(registerSpecProvider, deviceConfiguration, registerType);
        }

        @Override
        public TextualRegisterSpec add() {
            TextualRegisterSpec registerSpec = super.add();
            validateActiveDeviceConfiguration(CannotAddToActiveDeviceConfigurationException.aNewRegisterSpec(getThesaurus(), MessageSeeds.REGISTER_SPEC_CANNOT_ADD_TO_ACTIVE_CONFIG));
            validateUniqueRegisterSpecObisCode(registerSpec);
            DeviceConfigurationImpl.this.registerSpecs.add(registerSpec);
            if (DeviceConfigurationImpl.this.getId() > 0) {
                getDataModel().touch(DeviceConfigurationImpl.this);
            }
            return registerSpec;
        }

    }

    @Override
    public NumericalRegisterSpec.Updater getRegisterSpecUpdaterFor(NumericalRegisterSpec registerSpec) {
        return new NumericalRegisterSpecUpdaterForConfig((NumericalRegisterSpecImpl) registerSpec);
    }

    private class NumericalRegisterSpecUpdaterForConfig extends NumericalRegisterSpecImpl.AbstractUpdater {

        NumericalRegisterSpecUpdaterForConfig(NumericalRegisterSpecImpl registerSpec) {
            super(registerSpec);
        }

        @Override
        public void update() {
            validateUniqueRegisterSpecObisCode(this.updateTarget());
            super.update();
        }
    }

    @Override
    public TextualRegisterSpec.Updater getRegisterSpecUpdaterFor(TextualRegisterSpec registerSpec) {
        return new TextualRegisterSpecUpdaterForConfig((TextualRegisterSpecImpl) registerSpec);
    }

    private class TextualRegisterSpecUpdaterForConfig extends TextualRegisterSpecImpl.AbstractUpdater {

        TextualRegisterSpecUpdaterForConfig(TextualRegisterSpecImpl registerSpec) {
            super(registerSpec);
        }

        @Override
        public void update() {
            validateUniqueRegisterSpecObisCode(this.updateTarget());
            super.update();
        }
    }

    private void validateUniqueRegisterSpecObisCode(RegisterSpec registerSpec) {
        for (RegisterSpec spec : registerSpecs) {
            if (!isSameIdObject(registerSpec, spec) && spec.getDeviceObisCode()
                    .equals(registerSpec.getDeviceObisCode())) {
                throw DuplicateObisCodeException.forRegisterSpec(this.getThesaurus(), this, registerSpec.getDeviceObisCode(), MessageSeeds.DEVICE_CONFIGURATION_DUPLICATE_OBIS_CODE_FOR_REGISTER_SPEC);
            }
        }
    }

    public void deleteRegisterSpec(RegisterSpec registerSpec) {
        if (isActive()) {
            throw CannotDeleteFromActiveDeviceConfigurationException.canNotDeleteRegisterSpec(this.getThesaurus(), this, registerSpec, MessageSeeds.REGISTER_SPEC_CANNOT_DELETE_FOR_ACTIVE_CONFIG);
        }
        registerSpec.validateDelete();
        removeFromHasIdList(this.registerSpecs, registerSpec);
        this.getEventService().postEvent(EventType.DEVICETYPE_DELETED.topic(), registerSpec);
        if (getId() > 0) {
            getDataModel().touch(this);
        }
    }

    @Override
    public List<ChannelSpec> getChannelSpecs() {
        return this.loadProfileSpecs
                .stream()
                .flatMap(each -> each.getChannelSpecs().stream())
                .collect(Collectors.toList());
    }

    @Override
    public ChannelSpec.ChannelSpecBuilder createChannelSpec(ChannelType channelType, LoadProfileSpec loadProfileSpec) {
        return this.createChannelSpec(channelType, (LoadProfileSpecImpl) loadProfileSpec);
    }

    private ChannelSpec.ChannelSpecBuilder createChannelSpec(ChannelType channelType, LoadProfileSpecImpl loadProfileSpec) {
        return new ChannelSpecBuilderForConfig(channelSpecProvider, this, channelType, loadProfileSpec);
    }

    @Override
    public ChannelSpec.ChannelSpecBuilder createChannelSpec(ChannelType channelType, LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder) {
        return this.createChannelSpec(channelType, (ServerLoadProfileSpecBuilder) loadProfileSpecBuilder);
    }

    private ChannelSpec.ChannelSpecBuilder createChannelSpec(ChannelType channelType, ServerLoadProfileSpecBuilder loadProfileSpecBuilder) {
        return new ChannelSpecBuilderForConfig(channelSpecProvider, this, channelType, loadProfileSpecBuilder);
    }

    private class ChannelSpecBuilderForConfig extends ChannelSpecImpl.ChannelSpecBuilder {

        ChannelSpecBuilderForConfig(Provider<ChannelSpecImpl> channelSpecProvider, DeviceConfiguration deviceConfiguration, ChannelType channelType, LoadProfileSpecImpl loadProfileSpec) {
            super(channelSpecProvider, deviceConfiguration, channelType, loadProfileSpec);
        }

        ChannelSpecBuilderForConfig(Provider<ChannelSpecImpl> channelSpecProvider, DeviceConfiguration deviceConfiguration, ChannelType channelType, ServerLoadProfileSpecBuilder loadProfileSpecBuilder) {
            super(channelSpecProvider, deviceConfiguration, channelType, loadProfileSpecBuilder);
        }

        @Override
        public ChannelSpec add() {
            ChannelSpec channelSpec = super.add();
            validateActiveDeviceConfiguration(CannotAddToActiveDeviceConfigurationException.aNewChannelSpec(getThesaurus(), MessageSeeds.CHANNEL_SPEC_CANNOT_ADD_TO_ACTIVE_CONFIGURATION));
            validateUniqueChannelSpecPerLoadProfileSpec(channelSpec);
            return channelSpec;
        }
    }

    @Override
    public ChannelSpec.ChannelSpecUpdater getChannelSpecUpdaterFor(ChannelSpec channelSpec) {
        return new ChannelSpecUpdaterForConfig((ChannelSpecImpl) channelSpec);
    }

    private class ChannelSpecUpdaterForConfig extends ChannelSpecImpl.ChannelSpecUpdater {

        ChannelSpecUpdaterForConfig(ChannelSpecImpl channelSpec) {
            super(channelSpec);
        }

        @Override
        public void update() {
            validateUniqueChannelSpecPerLoadProfileSpec(channelSpec);
            super.update();
        }
    }

    private void validateUniqueChannelSpecPerLoadProfileSpec(ChannelSpec channelSpec) {
        for (ChannelSpec spec : getChannelSpecs()) {
            if (!isSameIdObject(spec, channelSpec)) {
                if (channelSpec.getLoadProfileSpec() == null) {
                    if (spec.getLoadProfileSpec() == null && channelSpec.getDeviceObisCode()
                            .equals(spec.getDeviceObisCode())) {
                        throw DuplicateObisCodeException.forChannelSpecConfigWithoutLoadProfileSpec(this, channelSpec.getDeviceObisCode(), this
                                .getThesaurus(), MessageSeeds.DEVICE_CONFIGURATION_DUPLICATE_OBIS_CODE_FOR_CHANNEL_SPEC);
                    }
                } else if (channelSpec.getLoadProfileSpec().getId() == spec.getLoadProfileSpec().getId()) {
                    if (channelSpec.getDeviceObisCode().equals(spec.getDeviceObisCode())) {
                        throw DuplicateObisCodeException.forChannelSpecInLoadProfileSpec(this.getThesaurus(), this, channelSpec
                                .getDeviceObisCode(), channelSpec.getLoadProfileSpec(), MessageSeeds.DEVICE_CONFIGURATION_DUPLICATE_OBIS_CODE_FOR_CHANNEL_SPEC_IN_LOAD_PROFILE_SPEC);
                    }
                }
            }
        }
    }

    @Override
    public void removeChannelSpec(ChannelSpec channelSpec) {
        this.removeChannelSpec((ServerChannelSpec) channelSpec);
    }

    private void removeChannelSpec(ServerChannelSpec channelSpec) {
        if (isActive()) {
            throw CannotDeleteFromActiveDeviceConfigurationException.forChannelSpec(this.getThesaurus(), channelSpec, this, MessageSeeds.CHANNEL_SPEC_CANNOT_DELETE_FROM_ACTIVE_CONFIG);
        }
        channelSpec.validateDelete();
        ServerLoadProfileSpec loadProfileSpec = (ServerLoadProfileSpec) channelSpec.getLoadProfileSpec();
        loadProfileSpec.removeChannelSpec(channelSpec);
        this.getEventService().postEvent(EventType.CHANNELSPEC_DELETED.topic(), channelSpec);
    }

    @Override
    public List<LoadProfileSpec> getLoadProfileSpecs() {
        return Collections.unmodifiableList(this.loadProfileSpecs);
    }

    @Override
    public LoadProfileSpec.LoadProfileSpecBuilder createLoadProfileSpec(LoadProfileType loadProfileType) {
        return new LoadProfileSpecBuilderForConfig(loadProfileSpecProvider, this, loadProfileType);
    }

    private class LoadProfileSpecBuilderForConfig extends LoadProfileSpecImpl.LoadProfileSpecBuilder {

        LoadProfileSpecBuilderForConfig(Provider<LoadProfileSpecImpl> loadProfileSpecProvider, DeviceConfiguration deviceConfiguration, LoadProfileType loadProfileType) {
            super(loadProfileSpecProvider, deviceConfiguration, loadProfileType);
        }

        public LoadProfileSpec add() {
            LoadProfileSpecImpl loadProfileSpec = (LoadProfileSpecImpl) super.add();
            validateActiveDeviceConfiguration(CannotAddToActiveDeviceConfigurationException.aNewLoadProfileSpec(getThesaurus(), MessageSeeds.LOAD_PROFILE_SPEC_CANNOT_ADD_TO_ACTIVE_CONFIGURATION));
            validateUniqueLoadProfileType(loadProfileSpec);
            validateUniqueLoadProfileObisCode(loadProfileSpec);
            DeviceConfigurationImpl.this.loadProfileSpecs.add(loadProfileSpec);
            if (DeviceConfigurationImpl.this.getId() > 0) {
                getDataModel().touch(DeviceConfigurationImpl.this);
            }
            return loadProfileSpec;
        }
    }

    @Override
    public LoadProfileSpec.LoadProfileSpecUpdater getLoadProfileSpecUpdaterFor(LoadProfileSpec loadProfileSpec) {
        return new LoadProfileSpecUpdater((LoadProfileSpecImpl) loadProfileSpec);
    }

    private class LoadProfileSpecUpdater extends LoadProfileSpecImpl.LoadProfileSpecUpdater {

        LoadProfileSpecUpdater(LoadProfileSpecImpl loadProfileSpec) {
            super(loadProfileSpec);
        }

        @Override
        public void update() {
            validateUniqueLoadProfileObisCode(loadProfileSpec);
            super.update();
            getDataModel().touch(DeviceConfigurationImpl.this);
        }
    }

    private void validateUniqueLoadProfileObisCode(LoadProfileSpec loadProfileSpec) {
        for (LoadProfileSpec profileSpec : loadProfileSpecs) {
            if (!isSameIdObject(loadProfileSpec, profileSpec)
                    && profileSpec.getDeviceObisCode().equals(loadProfileSpec.getDeviceObisCode())) {
                throw DuplicateObisCodeException.forLoadProfileSpec(this, loadProfileSpec.getDeviceObisCode(), this.getThesaurus(), MessageSeeds.DEVICE_CONFIGURATION_DUPLICATE_OBIS_CODE_FOR_LOAD_PROFILE_SPEC);
            }
        }
    }

    private void validateUniqueLoadProfileType(LoadProfileSpec loadProfileSpec) {
        for (LoadProfileSpec profileSpec : loadProfileSpecs) {
            if (profileSpec.getLoadProfileType().getId() == loadProfileSpec.getLoadProfileType().getId()) {
                throw new DuplicateLoadProfileTypeException(this, loadProfileSpec.getLoadProfileType(), loadProfileSpec, this
                        .getThesaurus(), MessageSeeds.DEVICE_CONFIGURATION_DUPLICATE_LOAD_PROFILE_TYPE_IN_SPEC);
            }
        }
    }

    @Override
    public void deleteLoadProfileSpec(LoadProfileSpec loadProfileSpec) {
        if (isActive()) {
            throw CannotDeleteFromActiveDeviceConfigurationException.forLoadProfileSpec(loadProfileSpec, this, this.getThesaurus(), MessageSeeds.LOAD_PROFILE_SPEC_CANNOT_DELETE_FROM_ACTIVE_CONFIG);
        }
        loadProfileSpec.prepareDelete();
        removeFromHasIdList(this.loadProfileSpecs, loadProfileSpec);
        this.getEventService().postEvent(EventType.DEVICETYPE_DELETED.topic(), loadProfileSpec);
        if (getId() > 0) {
            getDataModel().touch(this);
        }
    }

    private LogBookBehavior getLogBookBehavior() {
        return ((getDeviceType().isDataloggerSlave() || getDeviceType().isMultiElementSlave()) ? new LackingLogBookBehavior() : new RegularLogBookBehavior());
    }

    /**
     * Models different behavior for logbooks on the different 'types' of a DeviceType
     */
    interface LogBookBehavior {
        LogBookSpec.LogBookSpecBuilder createLogBookSpec(LogBookType logBookType);

        LogBookSpec.LogBookSpecUpdater getLogBookSpecUpdaterFor(LogBookSpec logBookSpec);

        List<LogBookSpec> getLogBookSpecs();

        void removeLogBookSpec(LogBookSpec logBookSpec);

        boolean hasLogBookSpecForConfig(int logBookTypeId, int updateId);
    }

    private class RegularLogBookBehavior implements LogBookBehavior {

        @Override
        public LogBookSpec.LogBookSpecBuilder createLogBookSpec(LogBookType logBookType) {
            return new LogBookSpecBuilderForConfig(logBookSpecProvider, DeviceConfigurationImpl.this, logBookType);
        }

        @Override
        public LogBookSpec.LogBookSpecUpdater getLogBookSpecUpdaterFor(LogBookSpec logBookSpec) {
            return new LogBookSpecUpdaterForConfig((LogBookSpecImpl) logBookSpec);
        }

        @Override
        public List<LogBookSpec> getLogBookSpecs() {
            return Collections.unmodifiableList(DeviceConfigurationImpl.this.logBookSpecs);
        }

        @Override
        public void removeLogBookSpec(LogBookSpec logBookSpec) {
            if (isActive()) {
                throw CannotDeleteFromActiveDeviceConfigurationException.forLogbookSpec(DeviceConfigurationImpl.this.getThesaurus(), logBookSpec, DeviceConfigurationImpl.this, MessageSeeds.LOGBOOK_SPEC_CANNOT_DELETE_FROM_ACTIVE_CONFIG);
            }
            logBookSpec.validateDelete();
            removeFromHasIdList(DeviceConfigurationImpl.this.logBookSpecs, logBookSpec);
            getEventService().postEvent(EventType.DEVICETYPE_DELETED.topic(), logBookSpec);
            if (getId() > 0) {
                getDataModel().touch(DeviceConfigurationImpl.this);
            }
        }

        @Override
        public boolean hasLogBookSpecForConfig(int logBookTypeId, int updateId) {
            if (getLogBookSpecs() != null && !getLogBookSpecs().isEmpty()) {
                for (LogBookSpec logBookSpec : getLogBookSpecs()) {
                    if (logBookSpec.getLogBookType().getId() == logBookTypeId && logBookSpec.getId() != updateId) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    private class LackingLogBookBehavior implements LogBookBehavior {

        @Override
        public LogBookSpec.LogBookSpecBuilder createLogBookSpec(LogBookType logBookType) {
            throw DataloggerSlaveException.logbookSpecsAreNotSupported(getThesaurus(), DeviceConfigurationImpl.this);
        }

        @Override
        public LogBookSpec.LogBookSpecUpdater getLogBookSpecUpdaterFor(LogBookSpec logBookSpec) {
            throw DataloggerSlaveException.logbookSpecsAreNotSupported(getThesaurus(), DeviceConfigurationImpl.this);
        }

        @Override
        public List<LogBookSpec> getLogBookSpecs() {
            return Collections.emptyList();
        }

        @Override
        public void removeLogBookSpec(LogBookSpec logBookSpec) {
            // don't do anything
        }

        @Override
        public boolean hasLogBookSpecForConfig(int logBookTypeId, int updateId) {
            return false;
        }
    }

    @Override
    public List<LogBookSpec> getLogBookSpecs() {
        return getLogBookBehavior().getLogBookSpecs();
    }

    @Override
    public LogBookSpec.LogBookSpecBuilder createLogBookSpec(LogBookType logBookType) {
        return getLogBookBehavior().createLogBookSpec(logBookType);
    }

    private class LogBookSpecBuilderForConfig extends LogBookSpecImpl.LogBookSpecBuilder {

        LogBookSpecBuilderForConfig(Provider<LogBookSpecImpl> logBookSpecProvider, DeviceConfiguration deviceConfiguration, LogBookType logBookType) {
            super(logBookSpecProvider, deviceConfiguration, logBookType);
        }

        @Override
        public LogBookSpecImpl add() {
            LogBookSpecImpl logBookSpec = super.add();
            validateActiveDeviceConfiguration(CannotAddToActiveDeviceConfigurationException.aNewLogBookSpec(getThesaurus(), MessageSeeds.LOGBOOK_SPEC_CANNOT_ADD_TO_ACTIVE_CONFIGURATION));
            validateUniqueLogBookType(logBookSpec);
            validateUniqueLogBookObisCode(logBookSpec);
            DeviceConfigurationImpl.this.logBookSpecs.add(logBookSpec);
            if (getId() > 0) {
                getDataModel().touch(DeviceConfigurationImpl.this);
            }
            return logBookSpec;
        }
    }

    private void validateActiveDeviceConfiguration(CannotAddToActiveDeviceConfigurationException specException) {
        if (isActive()) {
            throw specException;
        }
    }

    @Override
    public LogBookSpec.LogBookSpecUpdater getLogBookSpecUpdaterFor(LogBookSpec logBookSpec) {
        return getLogBookBehavior().getLogBookSpecUpdaterFor(logBookSpec);
    }

    private class LogBookSpecUpdaterForConfig extends LogBookSpecImpl.LogBookSpecUpdater {

        private LogBookSpecUpdaterForConfig(LogBookSpecImpl logBookSpec) {
            super(logBookSpec);
        }

        @Override
        public void update() {
            validateUniqueLogBookObisCode(logBookSpec);
            super.update();
        }
    }

    private void validateUniqueLogBookType(LogBookSpecImpl logBookSpec) {
        for (LogBookSpec spec : logBookSpecs) {
            if (spec.getLogBookType().getId() == logBookSpec.getLogBookType().getId()) {
                throw new DuplicateLogBookTypeException(this, logBookSpec.getLogBookType(), logBookSpec, this.getThesaurus(), MessageSeeds.DEVICE_CONFIGURATION_DUPLICATE_LOG_BOOK_TYPE_IN_SPEC);
            }
        }
    }

    private void validateUniqueLogBookObisCode(LogBookSpec logBookSpec) {
        for (LogBookSpec bookSpec : logBookSpecs) {
            if (!isSameIdObject(bookSpec, logBookSpec)
                    && bookSpec.getDeviceObisCode().equals(logBookSpec.getDeviceObisCode())) {
                throw DuplicateObisCodeException.forLogBookSpec(this, logBookSpec.getDeviceObisCode(), this.getThesaurus(), MessageSeeds.DEVICE_CONFIGURATION_DUPLICATE_OBIS_CODE_FOR_LOGBOOK_SPEC);
            }
        }
    }

    public void removeLogBookSpec(LogBookSpec logBookSpec) {
        getLogBookBehavior().removeLogBookSpec(logBookSpec);
    }

    @Override
    public boolean isActive() {
        return active;
    }

    public void activate() {
        this.active = true;
        super.save();
        getDataModel().touch(deviceType.get());
        this.getEventService().postEvent(EventType.DEVICECONFIGURATION_ACTIVATED.topic(), this);
    }

    public void deactivate() {
        this.getEventService().postEvent(EventType.DEVICECONFIGURATION_VALIDATEDEACTIVATE.topic(), this);
        this.active = false;
        super.save();
        getDataModel().touch(deviceType.get());
        this.getEventService().postEvent(EventType.DEVICECONFIGURATION_DEACTIVATED.topic(), this);
    }

    @Override
    public void save() {
        this.protocolConfigurationPropertyChanges.apply();
        boolean creating = getId() == 0;
        super.save();
        getDataModel().touch(deviceType.get());
        if (creating) {
            for (PartialConnectionTask partialConnectionTask : partialConnectionTasks) {
                this.getEventService()
                        .postEvent(((PersistentIdObject) partialConnectionTask).createEventType()
                                .topic(), partialConnectionTask);
            }
        }
    }

    @Override
    public void touch() {
        getDataModel().touch(this);
    }

    @Override
    protected void postNew() {
        validateRequiredFields();
        super.postNew();
    }

    private void validateRequiredFields() {
        validateDeviceTypeExists();
    }

    private void validateDeviceTypeExists() {
        if (!this.deviceType.isPresent()) {
            throw new DeviceTypeIsRequiredException(this.getThesaurus(), MessageSeeds.DEVICE_CONFIGURATION_DEVICE_TYPE_IS_REQUIRED);
        }
    }

    @Override
    protected void doDelete() {
        throw new UnsupportedOperationException("DeviceConfig is to be deleted by removing it from the device type");
    }

    @Override
    protected CreateEventType createEventType() {
        return CreateEventType.DEVICECONFIGURATION;
    }

    @Override
    protected UpdateEventType updateEventType() {
        return UpdateEventType.DEVICECONFIGURATION;
    }

    @Override
    protected DeleteEventType deleteEventType() {
        return DeleteEventType.DEVICECONFIGURATION;
    }

    @Override
    protected void validateDelete() {
        if (isActive()) {
            throw new DeviceConfigurationIsActiveException(this, this.getThesaurus(), MessageSeeds.DEVICE_CONFIGURATION_IS_ACTIVE_CAN_NOT_DELETE);
        }
    }

    public boolean hasLogBookSpecForConfig(int logBookTypeId, int updateId) {
        return getLogBookBehavior().hasLogBookSpecForConfig(logBookTypeId, updateId);
    }

    @Override
    public void remove(PartialConnectionTask partialConnectionTask) {
        this.remove((ServerPartialConnectionTask) partialConnectionTask);
    }

    private void remove(ServerPartialConnectionTask partialConnectionTask) {
        partialConnectionTask.validateDelete();
        partialConnectionTask.prepareDelete();
        getServerDeviceType().removeConflictsFor(partialConnectionTask);
        if (removeFromHasIdList(partialConnectionTasks, partialConnectionTask) && getId() > 0) {
            this.getEventService().postEvent(partialConnectionTask.deleteEventType().topic(), partialConnectionTask);
            getDataModel().touch(this);
        }
    }

    private ServerDeviceType getServerDeviceType() {
        return (ServerDeviceType) deviceType.get();
    }

    @Override
    public boolean supportsAllProtocolMessages() {
        return supportsAllProtocolMessages;
    }

    @Override
    public Set<DeviceMessageUserAction> getAllProtocolMessagesUserActions() {
        return fromDatabaseValue(this.supportsAllProtocolMessagesUserActionsBitVector);
    }

    /**
     * Returns an appropriate database value for the specified set of enum values.
     *
     * @param enumValues The set of enum values
     * @return The database value that is ready to be set on a PreparedStatement
     */
    private long toDatabaseValue(EnumSet<DeviceMessageUserAction> enumValues) {
        long dbValue = 0;
        long bitValue = 1;
        for (DeviceMessageUserAction enumValue : DeviceMessageUserAction.values()) {
            if (enumValues.contains(enumValue)) {
                dbValue = dbValue + bitValue;
            }
            bitValue = bitValue * 2;
        }
        return dbValue;
    }

    /**
     * Returns the set of enum values represented by the bit vector
     * that was read from a ResultSet. It is assumed that the bit vector
     * was produced by this same class.
     *
     * @param dbValue The bit vector that was stored as a database value earlier
     * @return The set of enum values
     */
    private EnumSet<DeviceMessageUserAction> fromDatabaseValue(long dbValue) {
        long bitPattern = 1;
        EnumSet<DeviceMessageUserAction> enumValues = EnumSet.noneOf(DeviceMessageUserAction.class);
        for (DeviceMessageUserAction enumValue : DeviceMessageUserAction.values()) {
            if ((dbValue & bitPattern) != 0) {
                // The bit for this enum value is set
                enumValues.add(enumValue);
            }
            bitPattern = bitPattern * 2;
        }
        return enumValues;
    }

    @Override
    public void setSupportsAllProtocolMessagesWithUserActions(boolean supportAllProtocolMessages, DeviceMessageUserAction... deviceMessageUserActions) {
        this.supportsAllProtocolMessages = supportAllProtocolMessages;
        if (this.supportsAllProtocolMessages) {
            this.deleteDeviceMessageEnablements();
            if (deviceMessageUserActions.length > 0) {
                this.supportsAllProtocolMessagesUserActionsBitVector = toDatabaseValue(EnumSet.copyOf(Arrays.asList(deviceMessageUserActions)));
            }
        } else {
            this.supportsAllProtocolMessagesUserActionsBitVector = 0;
        }
    }

    @Override
    public void addSecurityPropertySet(SecurityPropertySet securityPropertySet) {
        Save.CREATE.validate(this.getDataModel(), securityPropertySet);
        securityPropertySets.add((ServerSecurityPropertySet) securityPropertySet);
    }

    @Override
    public List<PartialConnectionTask> getPartialConnectionTasks() {
        return this.findAllPartialConnectionTasks();
    }

    private List<PartialConnectionTask> findAllPartialConnectionTasks() {
        return Collections.unmodifiableList(this.partialConnectionTasks);
    }

    @Override
    public List<PartialInboundConnectionTask> getPartialInboundConnectionTasks() {
        return this.findAllPartialConnectionTasks()
                .stream()
                .filter(each -> each instanceof PartialInboundConnectionTask)
                .map(PartialInboundConnectionTask.class::cast)
                .collect(Collectors.toList());
    }

    @Override
    public List<PartialScheduledConnectionTask> getPartialOutboundConnectionTasks() {
        return this.findAllPartialConnectionTasks()
                .stream()
                .filter(each -> each instanceof PartialScheduledConnectionTask)
                .map(PartialScheduledConnectionTask.class::cast)
                .collect(Collectors.toList());
    }

    @Override
    public List<PartialConnectionInitiationTask> getPartialConnectionInitiationTasks() {
        return this.findAllPartialConnectionTasks()
                .stream()
                .filter(each -> each instanceof PartialConnectionInitiationTask)
                .map(PartialConnectionInitiationTask.class::cast)
                .collect(Collectors.toList());
    }

    @Override
    public ProtocolDialectConfigurationProperties findOrCreateProtocolDialectConfigurationProperties(DeviceProtocolDialect protocolDialect) {
        for (ProtocolDialectConfigurationProperties candidate : configurationPropertiesList) {
            if (candidate.getDeviceProtocolDialect()
                    .getDeviceProtocolDialectName()
                    .equals(protocolDialect.getDeviceProtocolDialectName())) {
                return candidate;
            }
        }
        ProtocolDialectConfigurationPropertiesImpl properties = ProtocolDialectConfigurationPropertiesImpl.from(this.getDataModel(), this, protocolDialect);
        configurationPropertiesList.add(properties);
        return properties;
    }

    void clearDefaultExcept(ServerPartialConnectionTask partialConnectionTask) {
        this.partialConnectionTasks.stream()
                .filter(candidate -> partialConnectionTask == null || candidate.getId() != partialConnectionTask.getId())
                .forEach(ServerPartialConnectionTask::clearDefault);
    }

    @Override
    public List<ProtocolDialectConfigurationProperties> getProtocolDialectConfigurationPropertiesList() {
        return Collections.unmodifiableList(configurationPropertiesList);
    }

    @Override
    @XmlElement(type = SecurityPropertySetImpl.class)
    public List<SecurityPropertySet> getSecurityPropertySets() {
        return Collections.unmodifiableList(securityPropertySets);
    }

    @Override
    public SecurityPropertySetBuilder createSecurityPropertySet(String name) {
        return new InternalSecurityPropertySetBuilder(name);
    }

    @Override
    public void removeSecurityPropertySet(SecurityPropertySet propertySet) {
        this.removeSecurityPropertySet((ServerSecurityPropertySet) propertySet);
    }

    private void removeSecurityPropertySet(ServerSecurityPropertySet propertySet) {
        if (propertySet != null) {
            propertySet.prepareDelete();
            securityPropertySets.remove(propertySet);
            if (this.getId() > 0) {
                getEventService().postEvent(propertySet.deleteEventType().topic(), propertySet);
            }
        }
    }

    @Override
    public PartialScheduledConnectionTaskBuilder newPartialScheduledConnectionTask(String name, ConnectionTypePluggableClass connectionType, TimeDuration rescheduleRetryDelay, ConnectionStrategy connectionStrategy, ProtocolDialectConfigurationProperties configurationProperties) {
        return new PartialScheduledConnectionTaskBuilderImpl(this.getDataModel(), this, this.schedulingService, this.getEventService())
                .name(name)
                .pluggableClass(connectionType)
                .rescheduleDelay(rescheduleRetryDelay)
                .connectionStrategy(connectionStrategy)
                .setProtocolDialectConfigurationProperties(configurationProperties);
    }

    @Override
    public PartialInboundConnectionTaskBuilder newPartialInboundConnectionTask(String name, ConnectionTypePluggableClass connectionType, ProtocolDialectConfigurationProperties configurationProperties) {
        return new PartialInboundConnectionTaskBuilderImpl(this.getDataModel(), this)
                .name(name)
                .pluggableClass(connectionType)
                .setProtocolDialectConfigurationProperties(configurationProperties);
    }

    @Override
    public PartialConnectionInitiationTaskBuilder newPartialConnectionInitiationTask(String name, ConnectionTypePluggableClass connectionType, TimeDuration rescheduleRetryDelay, ProtocolDialectConfigurationProperties configurationProperties) {
        return new PartialConnectionInitiationTaskBuilderImpl(this.getDataModel(), this, this.schedulingService, this.getEventService())
                .name(name)
                .pluggableClass(connectionType)
                .rescheduleDelay(rescheduleRetryDelay)
                .setProtocolDialectConfigurationProperties(configurationProperties);
    }

    void addPartialConnectionTask(ServerPartialConnectionTask partialConnectionTask) {
        Save.CREATE.validate(this.getDataModel(), partialConnectionTask);
        partialConnectionTasks.add(partialConnectionTask);
    }

    @Override
    public ComTaskEnablementBuilder enableComTask(ComTask comTask, SecurityPropertySet securityPropertySet) {
        ComTaskEnablementImpl underConstruction = this.getDataModel()
                .getInstance(ComTaskEnablementImpl.class)
                .initialize(this, comTask, securityPropertySet);
        return new ComTaskEnablementBuilderImpl(underConstruction);
    }

    @Override
    public void disableComTask(ComTask comTask) {
        Iterator<ComTaskEnablement> comTaskEnablementIterator = this.comTaskEnablements.iterator();
        while (comTaskEnablementIterator.hasNext()) {
            ComTaskEnablement comTaskEnablement = comTaskEnablementIterator.next();
            if (comTaskEnablement.getComTask().getId() == comTask.getId()) {
                ComTaskEnablementImpl each = (ComTaskEnablementImpl) comTaskEnablement;
                each.validateDelete();
                comTaskEnablementIterator.remove();
                getDataModel().touch(this);
                return;
            }
        }
        throw new CannotDisableComTaskThatWasNotEnabledException(this, comTask, this.getThesaurus(), MessageSeeds.COM_TASK_ENABLEMENT_DOES_NOT_EXIST);
    }

    @Override
    @XmlElement(type = ComTaskEnablementImpl.class)
    public List<ComTaskEnablement> getComTaskEnablements() {
        return Collections.unmodifiableList(this.comTaskEnablements);
    }

    @Override
    public Optional<ComTaskEnablement> getComTaskEnablementFor(ComTask comTask) {
        return this.comTaskEnablements
                .stream()
                .filter(each -> comTask.getId() == each.getComTask().getId())
                .findFirst();
    }

    private void addComTaskEnablement(ComTaskEnablementImpl comTaskEnablement) {
        comTaskEnablement.adding();
        Save.CREATE.validate(this.getDataModel(), comTaskEnablement);
        this.comTaskEnablements.add(comTaskEnablement);
        comTaskEnablement.added();
        getDataModel().touch(this);
    }

    @Override
    public List<DeviceMessageEnablement> getDeviceMessageEnablements() {
        return Collections.unmodifiableList(deviceMessageEnablements);
    }

    @Override
    public DeviceMessageEnablementBuilder createDeviceMessageEnablement(DeviceMessageId deviceMessageId) {
        return new InternalDeviceMessageEnablementBuilder(deviceMessageId);
    }

    @Override
    public boolean removeDeviceMessageEnablement(DeviceMessageId deviceMessageId) {
        Optional<DeviceMessageEnablementImpl> enablement =
                this.deviceMessageEnablements
                        .stream()
                        .filter(deviceMessageEnablement -> deviceMessageEnablement.getDeviceMessageId().equals(deviceMessageId))
                        .findFirst();
        enablement.ifPresent(this::removeDeviceMessageEnablement);
        return enablement.isPresent();
    }

    private void addDeviceMessageEnablement(DeviceMessageEnablementImpl singleDeviceMessageEnablement) {
        Save.CREATE.validate(this.getDataModel(), singleDeviceMessageEnablement);
        this.deviceMessageEnablements.add(singleDeviceMessageEnablement);
        this.setSupportsAllProtocolMessagesWithUserActions(false);
        this.save();
    }

    @Override
    public boolean isAuthorized(DeviceMessageId deviceMessageId) {
        Optional<User> currentUser = getCurrentUser();
        if (currentUser.isPresent()) {
            User user = currentUser.get();
            if (supportsAllProtocolMessages()) {
                Optional<DeviceProtocolPluggableClass> deviceProtocolPluggableClass = this.getDeviceType().getDeviceProtocolPluggableClass();
                if ((deviceProtocolPluggableClass.isPresent()
                        && deviceProtocolPluggableClass.get().getDeviceProtocol().getSupportedMessages().stream()
                        .map(com.energyict.mdc.upl.messages.DeviceMessageSpec::getId)
                        .collect(Collectors.toList())
                        .contains(deviceMessageId.dbValue()))) {
                    return getAllProtocolMessagesUserActions().stream()
                            .anyMatch(deviceMessageUserAction -> isUserAuthorizedForAction(deviceMessageUserAction, user));
                }
            }
            java.util.Optional<DeviceMessageEnablement> deviceMessageEnablementOptional = getDeviceMessageEnablements().stream()
                    .filter(deviceMessageEnablement -> deviceMessageEnablement.getDeviceMessageId()
                            .equals(deviceMessageId))
                    .findAny();
            if (deviceMessageEnablementOptional.isPresent()) {
                return deviceMessageEnablementOptional.get()
                        .getUserActions()
                        .stream()
                        .anyMatch(deviceMessageUserAction -> isUserAuthorizedForAction(deviceMessageUserAction, user));
            }
        }
        return false;
    }

    private boolean isUserAuthorizedForAction(DeviceMessageUserAction action, User user) {
        return user.hasPrivilege("MDC", action.getPrivilege());
    }

    private Optional<User> getCurrentUser() {
        Principal principal = threadPrincipalService.getPrincipal();
        if (!(principal instanceof User)) {
            return Optional.empty();
        }
        return Optional.of((User) principal);
    }

    @Override
    public List<DeviceMessageSpec> getEnabledAndAuthorizedDeviceMessageSpecsIn(DeviceMessageCategory category) {
        List<Long> ids = this.getDeviceType().getDeviceProtocolPluggableClass()
                .map(pluggableClass -> pluggableClass.getDeviceProtocol().getSupportedMessages().stream()
                        .map(com.energyict.mdc.upl.messages.DeviceMessageSpec::getId)
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());

        EnumSet<DeviceMessageId> enabledDeviceMessageIds = EnumSet.noneOf(DeviceMessageId.class);
        this.getDeviceMessageEnablements()
                .stream()
                .map(DeviceMessageEnablement::getDeviceMessageId)
                .forEach(enabledDeviceMessageIds::add);

        return category.getMessageSpecifications()
                .stream()
                .filter(deviceMessageSpec -> ids.contains(deviceMessageSpec.getId().dbValue())) // limit to device message specs supported by the protocol
                .filter(deviceMessageSpec -> enabledDeviceMessageIds.contains(deviceMessageSpec.getId())) // limit to device message specs enabled on the config
                .filter(deviceMessageSpec -> this.isAuthorized(deviceMessageSpec.getId())) // limit to device message specs whom the user is authorized to
                .map(this::replaceDeviceMessageFileValueFactories)
                .collect(Collectors.toList());
    }

    @Override
    public void fileManagementDisabled() {
        Set<DeviceMessageId> fileManagementRelated = DeviceMessageId.fileManagementRelated();
        List<DeviceMessageEnablementImpl> obsoleteEnablements =
                this.deviceMessageEnablements
                        .stream()
                        .filter(enablement -> fileManagementRelated.contains(enablement.getDeviceMessageId()))
                        .collect(Collectors.toList());
        obsoleteEnablements.forEach(DeviceMessageEnablementImpl::prepareDelete);
        this.deviceMessageEnablements.removeAll(obsoleteEnablements);
    }

    private DeviceMessageSpec replaceDeviceMessageFileValueFactories(DeviceMessageSpec spec) {
        return new DeviceMessageSpecWithPossibleValuesImpl(this.getDeviceType(), spec, this.propertySpecService);
    }

    public List<DeviceConfValidationRuleSetUsage> getDeviceConfValidationRuleSetUsages() {
        return deviceConfValidationRuleSetUsages;
    }

    public List<ValidationRuleSet> getValidationRuleSets() {
        return this.deviceConfValidationRuleSetUsages
                .stream()
                .map(DeviceConfValidationRuleSetUsage::getValidationRuleSet)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public DeviceConfValidationRuleSetUsage addValidationRuleSet(ValidationRuleSet validationRuleSet) {
        DeviceConfValidationRuleSetUsage usage =
                deviceConfValidationRuleSetUsageFactory.get().init(validationRuleSet, this, true);
        deviceConfValidationRuleSetUsages.add(usage);
        getDataModel().touch(this);
        return usage;
    }

    @Override
    public void setValidationRuleSetStatus(ValidationRuleSet validationRuleSet, boolean status) {
        DeviceConfValidationRuleSetUsage usage = getUsage(validationRuleSet);
        if (usage != null) {
            usage.setRuleSetStatus(status);
            getDataModel().update(usage, "isRuleSetActive");
        }
    }

    @Override
    public boolean getValidationRuleSetStatus(ValidationRuleSet validationRuleSet) {
        DeviceConfValidationRuleSetUsage usage = getUsage(validationRuleSet);
        if (usage != null) {
            return usage.isRuleSetActive();
        }
        throw new UnsupportedOperationException("Unable to retrieve the validation rule set status for validation rule set with name: "+validationRuleSet.getName());
    }

    private DeviceConfValidationRuleSetUsage getUsage(ValidationRuleSet validationRuleSet) {
        List<DeviceConfValidationRuleSetUsage> usages = this.getDeviceConfValidationRuleSetUsages();
        for (DeviceConfValidationRuleSetUsage usage : usages) {
            if (usage.getValidationRuleSet().getId() == validationRuleSet.getId()) {
                return usage;
            }
        }
        return null;
    }

    @Override
    public void removeValidationRuleSet(ValidationRuleSet validationRuleSet) {
        DeviceConfValidationRuleSetUsage usage = getUsage(validationRuleSet);
        deviceConfValidationRuleSetUsages.remove(usage);
        getDataModel().touch(this);
    }

    @Override
    public List<ValidationRule> getValidationRules(Collection<? extends ReadingType> readingTypes) {
        List<ValidationRule> result = new ArrayList<>();
        List<ValidationRuleSet> ruleSets = getValidationRuleSets();
        for (ValidationRuleSet ruleSet : ruleSets) {
            result.addAll(ruleSet.getRules(readingTypes));
        }
        return result;
    }

    @Override
    public DeviceConfigurationEstimationRuleSetUsage addEstimationRuleSet(EstimationRuleSet estimationRuleSet) {
        return findEstimationRuleSetUsage(estimationRuleSet).orElseGet(() -> {
            DeviceConfigurationEstimationRuleSetUsage usage = deviceConfigEstimationRuleSetUsageFactory.get()
                    .init(this, estimationRuleSet, true);
            deviceConfigurationEstimationRuleSetUsages.add(usage);
            if (DeviceConfigurationImpl.this.getId() > 0) {
                getDataModel().touch(DeviceConfigurationImpl.this);
            }
            return usage;
        });
    }

    private Optional<DeviceConfigurationEstimationRuleSetUsage> findEstimationRuleSetUsage(EstimationRuleSet estimationRuleSet) {
        return deviceConfigurationEstimationRuleSetUsages.stream()
                .filter(usage -> usage.getEstimationRuleSet().getId() == estimationRuleSet.getId())
                .findFirst();
    }

    @Override
    public boolean isEstimationRuleSetActiveOnDeviceConfig(long estimationRuleSetId) {
        return deviceConfigurationEstimationRuleSetUsages.stream()
                .filter(usage -> usage.getEstimationRuleSet().getId() == estimationRuleSetId)
                .findFirst()
                .map(DeviceConfigurationEstimationRuleSetUsage::isRuleSetActive)
                .orElse(false);

    }

    @Override
    public void setEstimationRuleSetStatus(EstimationRuleSet estimationRuleSet, boolean status) {
        Optional<DeviceConfigurationEstimationRuleSetUsage> usage = findEstimationRuleSetUsage(estimationRuleSet);
        if (usage.isPresent()) {
            usage.get().setRuleSetStatus(status);
            getDataModel().update(usage.get(), DeviceConfigurationEstimationRuleSetUsageImpl.Fields.IS_RULE_SET_ACTIVE.fieldName());
        }
    }

    @Override
    public boolean getEstimationRuleSetStatus(EstimationRuleSet estimationRuleSet) {
        Optional<DeviceConfigurationEstimationRuleSetUsage> estimationRuleSetUsage = findEstimationRuleSetUsage(estimationRuleSet);
        if (estimationRuleSetUsage.isPresent()) {
            return estimationRuleSetUsage.get().isRuleSetActive();
        }
        throw new UnsupportedOperationException("Unable to retrieve the estimation rule set status for estimation rule set with name: "+estimationRuleSet.getName());
    }

    @Override
    public void removeEstimationRuleSet(EstimationRuleSet estimationRuleSet) {
        deviceConfigurationEstimationRuleSetUsages.stream()
                .filter((usage) -> usage.getEstimationRuleSet().getId() == estimationRuleSet.getId())
                .findFirst()
                .ifPresent(candidate -> {
                    deviceConfigurationEstimationRuleSetUsages.remove(candidate);
                    if (DeviceConfigurationImpl.this.getId() > 0) {
                        getDataModel().touch(DeviceConfigurationImpl.this);
                    }
                });
    }

    @Override
    public List<EstimationRuleSet> getEstimationRuleSets() {
        return deviceConfigurationEstimationRuleSetUsages.stream()
                .filter(usage -> usage.getEstimationRuleSet() != null)
                .map(DeviceConfigurationEstimationRuleSetUsage::getEstimationRuleSet)
                .collect(Collectors.toList());
    }

    @Override
    public List<DeviceConfigurationEstimationRuleSetUsage> getDeviceConfigEstimationRuleSetUsages() {
        return deviceConfigurationEstimationRuleSetUsages;
    }

    @Override
    public void reorderEstimationRuleSets(KPermutation kpermutation) {
        List<DeviceConfigurationEstimationRuleSetUsage> usages = getDeviceConfigEstimationRuleSetUsages();
        if (!kpermutation.isPermutation(usages)) {
            throw new IllegalArgumentException();
        }
        List<DeviceConfigurationEstimationRuleSetUsage> target = kpermutation.perform(usages);
        this.getDataModel().reorder(usages, target);
    }

    public GatewayType getGatewayType() {
        return this.gatewayType;
    }

    public void setGatewayType(GatewayType gatewayType) {
        if (gatewayType != null) {
            this.gatewayType = gatewayType;
        } else {
            this.gatewayType = GatewayType.NONE;
        }
    }

    @Override
    public DeviceProtocolConfigurationProperties getDeviceProtocolProperties() {
        return new DeviceProtocolConfigurationPropertiesImpl(this);
    }

    @Override
    public long getVersion() {
        return version;
    }

    public void setDataloggerEnabled(boolean dataloggerEnabled) {
        if (isActive() && dataloggerEnabled != this.dataloggerEnabled) {
            throw DataloggerSlaveException.cannotChangeDataloggerFunctionalityEnabledOnceTheConfigIsActive(getThesaurus(), MessageSeeds.DATALOGGER_ENABLED_CANNOT_CHANGE_ON_ACTIVE_CONFIG , this);
        }
        this.dataloggerEnabled = dataloggerEnabled;
    }
    @Override
    public boolean isDataloggerEnabled() {
        return this.dataloggerEnabled;
    }

    public void setMultiElementEnabled(boolean multiElementEnabled) {
        if (isActive() && multiElementEnabled != this.multiElementEnabled) {
            throw DataloggerSlaveException.cannotChangeDataloggerFunctionalityEnabledOnceTheConfigIsActive(getThesaurus(), MessageSeeds.MULTI_ELEMENT_ENABLEMENT_CANNOT_CHANGE_ON_ACTIVE_CONFIG ,this);
        }
        this.multiElementEnabled = multiElementEnabled;
    }

    @Override
    public boolean isMultiElementEnabled() {
        return this.multiElementEnabled;
    }

    @Override
    public boolean isDefault() {
        return this.isDefault;
    }

    @Override
    public void setDefaultStatus(boolean value) {
        if (value) {
            this.clearOldDefault();
        }
        this.isDefault = value;
        super.save();
    }

    private void clearOldDefault(){
        getDataModel()
                .query(DeviceConfigurationImpl.class)
                .select(where(Fields.IS_DEFAULT.fieldName()).isEqualTo(true)
                        .and(where(Fields.DEVICETYPE.fieldName()).isEqualTo(getDeviceType())))
                .forEach(deviceConfiguration -> deviceConfiguration.setDefaultStatus(false));
    }

    public boolean getValidateOnStore() {
        return validateOnStore;
    }

    @Override
    public void setValidateOnStore(boolean validateOnStore) {
        this.validateOnStore = validateOnStore;
    }

    List<DeviceProtocolConfigurationProperty> getProtocolPropertyList() {
        return protocolProperties;
    }

    void addProtocolProperty(DeviceProtocolConfigurationProperty property) {
        this.protocolConfigurationPropertyChanges.addProtocolProperty(property);
    }

    boolean removeProtocolProperty(String propertyName) {
        return this.protocolConfigurationPropertyChanges.removeProtocolProperty(propertyName);
    }

    private class ProtocolConfigurationPropertyChanges {
        private Map<String, DeviceProtocolConfigurationProperty> newProperties = new HashMap<>();
        private Map<String, DeviceProtocolConfigurationProperty> obsoleteProperties = new HashMap<>();

        private void addProtocolProperty(DeviceProtocolConfigurationProperty property) {
            this.newProperties.put(property.getName(), property);
        }

        private boolean removeProtocolProperty(String propertyName) {
            DeviceProtocolConfigurationProperty newProperty = this.newProperties.get(propertyName);
            if (newProperty != null) {
                // Property was added before, revoke it
                this.newProperties.remove(propertyName);
                return true;
            } else {
                Optional<DeviceProtocolConfigurationProperty> existingProperty = this.findProperty(propertyName);
                if (existingProperty.isPresent()) {
                    this.obsoleteProperties.put(propertyName, existingProperty.get());
                    return true;
                } else {
                    return false;   // There was not such property
                }
            }
        }

        private Optional<DeviceProtocolConfigurationProperty> findProperty(String propertyName) {
            return protocolProperties
                    .stream()
                    .filter(p -> p.getName().equals(propertyName))
                    .findFirst();
        }

        private void apply() {
            protocolProperties.removeAll(this.obsoleteProperties.values());
            Set<ConstraintViolation<?>> constraintViolations = new HashSet<>();
            this.newProperties.values().forEach(prop -> {
                try {
                    Save.CREATE.validate(getDataModel(), prop);
                } catch (VerboseConstraintViolationException e) {
                    constraintViolations.addAll(e.getConstraintViolations());
                }
            });
            if(constraintViolations.size() > 0 ) {
                throw new VerboseConstraintViolationException(constraintViolations);
            }
            protocolProperties.addAll(this.newProperties.values());
            this.newProperties.clear();
            this.obsoleteProperties.clear();
        }

    }

    private class InternalSecurityPropertySetBuilder implements SecurityPropertySetBuilder {

        private final SecurityPropertySetImpl underConstruction;

        private InternalSecurityPropertySetBuilder(String name) {
            this.underConstruction = SecurityPropertySetImpl.from(getDataModel(), DeviceConfigurationImpl.this, name);
        }

        @Override
        public SecurityPropertySetBuilder authenticationLevel(int level) {
            underConstruction.setAuthenticationLevelId(level);
            return this;
        }

        @Override
        public SecurityPropertySetBuilder encryptionLevel(int level) {
            underConstruction.setEncryptionLevelId(level);
            return this;
        }

        @Override
        public SecurityPropertySetBuilder client(Object client) {
            underConstruction.setClient(client);
            return this;
        }

        @Override
        public SecurityPropertySetBuilder securitySuite(int suite) {
            underConstruction.setSecuritySuiteId(suite);
            return this;
        }

        @Override
        public SecurityPropertySetBuilder requestSecurityLevel(int level) {
            underConstruction.setRequestSecurityLevelId(level);
            return this;
        }

        @Override
        public SecurityPropertySetBuilder responseSecurityLevel(int level) {
            underConstruction.setResponseSecurityLevelId(level);
            return this;
        }

        @Override
        public SecurityPropertySetBuilder addConfigurationSecurityProperty(String name, SecurityAccessorType keyAccessor) {
            underConstruction.addConfigurationSecurityProperty(name, keyAccessor);
            return this;
        }

        @Override
        public Set<PropertySpec> getPropertySpecs() {
            return underConstruction.getPropertySpecs();
        }

        @Override
        public SecurityPropertySet build() {
            DeviceConfigurationImpl.this.addSecurityPropertySet(underConstruction);
            if (DeviceConfigurationImpl.this.getId() > 0) {
                DeviceConfigurationImpl.this.getEventService().postEvent(underConstruction.createEventType().topic(), underConstruction);
            }
            if (DeviceConfigurationImpl.this.getId() > 0) {
                getDataModel().touch(DeviceConfigurationImpl.this);
            }
            return underConstruction;
        }
    }

    private class InternalDeviceMessageEnablementBuilder implements DeviceMessageEnablementBuilder {

        private final DeviceMessageEnablementImpl underConstruction;

        private InternalDeviceMessageEnablementBuilder(DeviceMessageId deviceMessageId) {
            this.underConstruction = DeviceMessageEnablementImpl.from(getDataModel(), DeviceConfigurationImpl.this, deviceMessageId);
        }

        @Override
        public DeviceMessageEnablementBuilder addUserAction(DeviceMessageUserAction deviceMessageUserAction) {
            this.underConstruction.addDeviceMessageUserAction(deviceMessageUserAction);
            return this;
        }

        @Override
        public DeviceMessageEnablementBuilder addUserActions(DeviceMessageUserAction... deviceMessageUserActions) {
            Stream
                    .of(deviceMessageUserActions)
                    .forEach(this.underConstruction::addDeviceMessageUserAction);
            return this;
        }

        @Override
        public DeviceMessageEnablement build() {
            DeviceConfigurationImpl.this.addDeviceMessageEnablement(underConstruction);
            return underConstruction;
        }
    }

    private enum ComTaskEnablementBuildingMode {
        UNDERCONSTRUCTION {
            @Override
            protected void verify() {
                // All calls are fine as long as we are under construction
            }
        },
        COMPLETE {
            @Override
            protected void verify() {
                throw new IllegalStateException("The communication task enablement building process is already complete");
            }
        };

        protected abstract void verify();
    }

    private class ComTaskEnablementBuilderImpl implements ComTaskEnablementBuilder {

        private ComTaskEnablementBuildingMode mode;
        private ComTaskEnablementImpl underConstruction;

        private ComTaskEnablementBuilderImpl(ComTaskEnablementImpl underConstruction) {
            super();
            this.mode = ComTaskEnablementBuildingMode.UNDERCONSTRUCTION;
            this.underConstruction = underConstruction;
        }

        @Override
        public ComTaskEnablementBuilder setIgnoreNextExecutionSpecsForInbound(boolean flag) {
            this.mode.verify();
            this.underConstruction.setIgnoreNextExecutionSpecsForInbound(flag);
            return this;
        }

        @Override
        public ComTaskEnablementBuilder setPartialConnectionTask(PartialConnectionTask partialConnectionTask) {
            this.mode.verify();
            this.underConstruction.setPartialConnectionTask(partialConnectionTask);
            return this;
        }

        @Override
        public ComTaskEnablementBuilder useDefaultConnectionTask(boolean flagValue) {
            this.mode.verify();
            this.underConstruction.useDefaultConnectionTask(flagValue);
            return this;
        }

        @Override
        public ComTaskEnablementBuilder setPriority(int priority) {
            this.mode.verify();
            this.underConstruction.setPriority(priority);
            return this;
        }

        @Override
        public ComTaskEnablementBuilder setMaxNumberOfTries(int maxNumberOfTries) {
            this.mode.verify();
            this.underConstruction.setMaxNumberOfTries(maxNumberOfTries);
            return this;
        }

        @Override
        public ComTaskEnablementBuilder setConnectionFunction(ConnectionFunction connectionFunction) {
            this.mode.verify();
            this.underConstruction.setConnectionFunction(connectionFunction);
            return this;
        }

        @Override
        public ComTaskEnablement add() {
            this.mode.verify();
            addComTaskEnablement(this.underConstruction);
            this.mode = ComTaskEnablementBuildingMode.COMPLETE;
            return this.underConstruction;
        }
    }

    @Override
    public DeviceConfiguration clone(String nameOfClone) {
        DeviceConfiguration clone = getDeviceType().newConfiguration(nameOfClone)
                .canActAsGateway(canActAsGateway())
                .description(getDescription())
                .gatewayType(getGatewayType())
                .isDirectlyAddressable(isDirectlyAddressable())
                .dataloggerEnabled(isDataloggerEnabled())
                .multiElementEnabled(isMultiElementEnabled())
                .validateOnStore(getValidateOnStore())
                .add();
        this.getDeviceProtocolProperties().getPropertySpecs().forEach(cloneDeviceProtocolProperties(clone));
        this.getProtocolDialectConfigurationPropertiesList().forEach(cloneDeviceProtocolDialectProperties(clone));
        getSecurityPropertySets().forEach(securityPropertySet -> ((ServerSecurityPropertySet) securityPropertySet).cloneForDeviceConfig(clone));
        getPartialConnectionTasks().forEach(partialConnectionTask -> ((ServerPartialConnectionTask) partialConnectionTask)
                .cloneForDeviceConfig(clone));
        getComTaskEnablements().forEach(comTaskEnablement -> ((ServerComTaskEnablement) comTaskEnablement).cloneForDeviceConfig(clone));
        getDeviceMessageEnablements().forEach(deviceMessageEnablement -> ((ServerDeviceMessageEnablement) deviceMessageEnablement)
                .cloneForDeviceConfig(clone));
        getRegisterSpecs().forEach(registerSpec -> ((ServerRegisterSpec) registerSpec).cloneForDeviceConfig(clone));
        getLogBookSpecs().forEach(logBookSpec -> ((ServerLogBookSpec) logBookSpec).cloneForDeviceConfig(clone));
        getLoadProfileSpecs().forEach(loadProfileSpec -> ((ServerLoadProfileSpec) loadProfileSpec).cloneForDeviceConfig(clone));
        getValidationRuleSets().forEach(clone::addValidationRuleSet);
        getEstimationRuleSets().forEach(clone::addEstimationRuleSet);
        clone.save();
        return clone;
    }

    private Consumer<ProtocolDialectConfigurationProperties> cloneDeviceProtocolDialectProperties(DeviceConfiguration clone) {
        return protocolDialectConfigurationProperties -> protocolDialectConfigurationProperties.getPropertySpecs()
                .forEach(copyDeviceProtocolDialectProperty(clone, protocolDialectConfigurationProperties));
    }

    private Consumer<PropertySpec> copyDeviceProtocolDialectProperty(DeviceConfiguration clone, ProtocolDialectConfigurationProperties protocolDialectConfigurationProperties) {
        return propertySpec -> {
            Object propertyValue = protocolDialectConfigurationProperties.getProperty(propertySpec.getName());
            if (propertyValue != null) {
                clone.findOrCreateProtocolDialectConfigurationProperties(protocolDialectConfigurationProperties.getDeviceProtocolDialect())
                        .setProperty(propertySpec.getName(), propertyValue);
            }
        };
    }

    private Consumer<PropertySpec> cloneDeviceProtocolProperties(DeviceConfiguration clone) {
        return propertySpec -> {
            Object propertyValue = this.getDeviceProtocolProperties().getProperty(propertySpec.getName());
            if (propertyValue != null) {
                clone.getDeviceProtocolProperties().setProperty(propertySpec.getName(), propertyValue);
            }
        };
    }

    @Override
    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    @Override
    public void setXmlType(String ignore) {
        //Ignore, only used for JSON
    }
}