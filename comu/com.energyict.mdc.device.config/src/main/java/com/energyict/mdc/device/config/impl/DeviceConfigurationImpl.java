package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.estimation.EstimationRuleSet;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.collections.KPermutation;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.config.*;
import com.energyict.mdc.device.config.events.EventType;
import com.energyict.mdc.device.config.exceptions.CannotAddToActiveDeviceConfigurationException;
import com.energyict.mdc.device.config.exceptions.CannotDeleteFromActiveDeviceConfigurationException;
import com.energyict.mdc.device.config.exceptions.CannotDisableComTaskThatWasNotEnabledException;
import com.energyict.mdc.device.config.exceptions.DeviceConfigurationIsActiveException;
import com.energyict.mdc.device.config.exceptions.DeviceTypeIsRequiredException;
import com.energyict.mdc.device.config.exceptions.DuplicateLoadProfileTypeException;
import com.energyict.mdc.device.config.exceptions.DuplicateLogBookTypeException;
import com.energyict.mdc.device.config.exceptions.DuplicateObisCodeException;
import com.energyict.mdc.device.config.exceptions.MessageSeeds;
import com.energyict.mdc.masterdata.ChannelType;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.masterdata.MeasurementType;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.tasks.ComTask;
import org.hibernate.validator.constraints.NotEmpty;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.validation.Valid;
import javax.validation.constraints.Size;
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
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * User: gde
 * Date: 5/11/12
 */
@DeviceFunctionsAreSupportedByProtocol(groups = {Save.Update.class, Save.Create.class})
@ImmutablePropertiesCanNotChangeForActiveConfiguration(groups = {Save.Update.class, Save.Create.class})
@GatewayTypeMustBeSpecified(groups = {Save.Update.class, Save.Create.class})
public class DeviceConfigurationImpl extends PersistentNamedObject<DeviceConfiguration> implements DeviceConfiguration, ServerDeviceConfiguration {

    private static final DeviceCommunicationFunctionSetPersister deviceCommunicationFunctionSetPersister = new DeviceCommunicationFunctionSetPersister();

    enum Fields {
        CAN_ACT_AS_GATEWAY("canActAsGateway"), // 'virtual' BeanProperty not backed by actual member
        IS_DIRECTLY_ADDRESSABLE("isDirectlyAddressable"), // 'virtual' BeanProperty not backed by actual member
        GATEWAY_TYPE("gatewayType"),
        COM_TASK_ENABLEMENTS("comTaskEnablements"),
        SECURITY_PROPERTY_SETS("securityPropertySets"),
        DEVICE_MESSAGE_ENABLEMENTS("deviceMessageEnablements"),
        DEVICECONF_ESTIMATIONRULESET_USAGES("deviceConfigurationEstimationRuleSetUsages")
        ;
        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }

    @Size(max= Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.NAME_REQUIRED + "}")
    private String name;
    @Size(max= 4000, groups = {Save.Update.class, Save.Create.class}, message = "{"+ MessageSeeds.Keys.FIELD_TOO_LONG +"}")
    private String description;

    private boolean active;

    private final Reference<DeviceType> deviceType = ValueReference.absent();
    @Valid
    private List<RegisterSpec> registerSpecs = new ArrayList<>();
    @Valid
    private List<ChannelSpec> channelSpecs = new ArrayList<>();
    @Valid
    private List<LoadProfileSpec> loadProfileSpecs = new ArrayList<>();
    @Valid
    private List<LogBookSpec> logBookSpecs = new ArrayList<>();
    private List<SecurityPropertySet> securityPropertySets = new ArrayList<>();
    private List<ComTaskEnablement> comTaskEnablements = new ArrayList<>();
    private List<DeviceMessageEnablement> deviceMessageEnablements = new ArrayList<>();
    private boolean supportsAllProtocolMessages;
    private long supportsAllProtocolMessagesUserActionsBitVector = 0L;
    @Valid
    private List<PartialConnectionTask> partialConnectionTasks = new ArrayList<>();
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
    private final Provider<LoadProfileSpecImpl> loadProfileSpecProvider;
    private final Provider<NumericalRegisterSpecImpl> numericalRegisterSpecProvider;
    private final Provider<TextualRegisterSpecImpl> textualRegisterSpecProvider;
    private final Provider<LogBookSpecImpl> logBookSpecProvider;
    private final Provider<ChannelSpecImpl> channelSpecProvider;
    private final DeviceConfigurationService deviceConfigurationService;
    private final SchedulingService schedulingService;
    private final ThreadPrincipalService threadPrincipalService;

    private List<DeviceConfValidationRuleSetUsage> deviceConfValidationRuleSetUsages = new ArrayList<>();
    private final Provider<DeviceConfValidationRuleSetUsageImpl> deviceConfValidationRuleSetUsageFactory;

    private List<DeviceConfigurationEstimationRuleSetUsage> deviceConfigurationEstimationRuleSetUsages = new ArrayList<>();
    private final Provider<DeviceConfigurationEstimationRuleSetUsageImpl> deviceConfigEstimationRuleSetUsageFactory;

    @Inject
    protected DeviceConfigurationImpl(
                        DataModel dataModel, EventService eventService, Thesaurus thesaurus,
                        Provider<LoadProfileSpecImpl> loadProfileSpecProvider,
                        Provider<NumericalRegisterSpecImpl> numericalRegisterSpecProvider,
                        Provider<TextualRegisterSpecImpl> textualRegisterSpecProvider,
                        Provider<LogBookSpecImpl> logBookSpecProvider,
                        Provider<ChannelSpecImpl> channelSpecProvider,
                        Provider<DeviceConfValidationRuleSetUsageImpl> deviceConfValidationRuleSetUsageFactory,
                        Provider<DeviceConfigurationEstimationRuleSetUsageImpl> deviceConfEstimationRuleSetUsageFactory,
                        DeviceConfigurationService deviceConfigurationService,
                        SchedulingService schedulingService,
                        ThreadPrincipalService threadPrincipalService) {
        super(DeviceConfiguration.class, dataModel, eventService, thesaurus);
        this.loadProfileSpecProvider = loadProfileSpecProvider;
        this.numericalRegisterSpecProvider = numericalRegisterSpecProvider;
        this.textualRegisterSpecProvider = textualRegisterSpecProvider;
        this.logBookSpecProvider = logBookSpecProvider;
        this.channelSpecProvider = channelSpecProvider;
        this.deviceConfValidationRuleSetUsageFactory = deviceConfValidationRuleSetUsageFactory;
        this.deviceConfigEstimationRuleSetUsageFactory = deviceConfEstimationRuleSetUsageFactory;
        this.deviceConfigurationService = deviceConfigurationService;
        this.schedulingService = schedulingService;
        this.threadPrincipalService = threadPrincipalService;
    }

    DeviceConfigurationImpl initialize(DeviceType deviceType, String name){
        this.deviceType.set(deviceType);
        setName(name);
        for (DeviceProtocolDialect deviceProtocolDialect : this.getDeviceType().getDeviceProtocolPluggableClass().getDeviceProtocol().getDeviceProtocolDialects()) {
            findOrCreateProtocolDialectConfigurationProperties(deviceProtocolDialect);
        }
        return this;
    }

    @Override
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
            if (!isSameIdObject(deviceConfiguration, this) && deviceConfiguration.getName().equals(name)){
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
            }
            else {
                throw new DuplicateLoadProfileTypeException(this.getThesaurus(), this, loadProfileType, each);
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
            String obisCodeValue = this.findNextAvailableObisCode(eachLogBookType.getObisCode().getValue(), obisCodeAndNameMap.keySet());
            String logBookSpecName = eachLogBookType.getName();
            if (!obisCodeAndNameMap.containsKey(obisCodeValue)) {
                obisCodeAndNameMap.put(obisCodeValue, logBookSpecName);
            }
            else {
                throw DuplicateObisCodeException.forLogBookSpec(this.getThesaurus(), this, each.getDeviceObisCode());
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
        this.channelSpecs.clear();
        this.logBookSpecs.clear();
        this.configurationPropertiesList.clear();
        this.deviceConfValidationRuleSetUsages.clear();
        this.deviceConfigurationEstimationRuleSetUsages.clear();
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
            }
            else {
                if (!obisCode.anyChannel()) {
                    throw DuplicateObisCodeException.forChannelSpecInLoadProfileSpec(this.getThesaurus(), this, each.getDeviceObisCode(), each.getLoadProfileSpec());
                }
            }
        }
    }

    private void validateAllRegisterSpecsHaveUniqueObisCodes() {
        Set<String> obisCodeSet = new HashSet<>();
        for (RegisterSpec registerSpec : this.getRegisterSpecs()) {
            String obisCodeValue = this.findNextAvailableObisCode(registerSpec.getDeviceObisCode().toString(), obisCodeSet);
            if (!obisCodeSet.contains(obisCodeValue)) {
                obisCodeSet.add(obisCodeValue);
            }
            else {
                throw DuplicateObisCodeException.forRegisterSpec(this.getThesaurus(), this, registerSpec.getDeviceObisCode());
            }
        }
    }

    /**
     * Looks for the next available Obiscode with different B-field.
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

    @Override
    public List<RegisterSpec> getRegisterSpecs() {
        return Collections.unmodifiableList(this.registerSpecs);
    }

    @Override
    public NumericalRegisterSpec.Builder createNumericalRegisterSpec(RegisterType registerType) {
        return new NumericalRegisterSpecBuilderForConfig(this.numericalRegisterSpecProvider, this, registerType);
    }

    class NumericalRegisterSpecBuilderForConfig extends NumericalRegisterSpecImpl.AbstractBuilder {

        NumericalRegisterSpecBuilderForConfig(Provider<NumericalRegisterSpecImpl> registerSpecProvider, DeviceConfiguration deviceConfiguration, RegisterType registerType) {
            super(registerSpecProvider, deviceConfiguration, registerType);
        }

        @Override
        public NumericalRegisterSpec add() {
            NumericalRegisterSpec registerSpec = super.add();
            validateActiveDeviceConfiguration(CannotAddToActiveDeviceConfigurationException.aNewRegisterSpec(getThesaurus()));
            validateUniqueRegisterSpecObisCode(registerSpec);
            DeviceConfigurationImpl.this.registerSpecs.add(registerSpec);
            return registerSpec;
        }
    }

    @Override
    public TextualRegisterSpec.Builder createTextualRegisterSpec(RegisterType registerType) {
        return new TextualRegisterSpecBuilderForConfig(this.textualRegisterSpecProvider, this, registerType);
    }

    class TextualRegisterSpecBuilderForConfig extends TextualRegisterSpecImpl.AbstractBuilder {

        TextualRegisterSpecBuilderForConfig(Provider<TextualRegisterSpecImpl> registerSpecProvider, DeviceConfiguration deviceConfiguration, RegisterType registerType) {
            super(registerSpecProvider, deviceConfiguration, registerType);
        }

        @Override
        public TextualRegisterSpec add() {
            TextualRegisterSpec registerSpec = super.add();
            validateActiveDeviceConfiguration(CannotAddToActiveDeviceConfigurationException.aNewRegisterSpec(getThesaurus()));
            validateUniqueRegisterSpecObisCode(registerSpec);
            DeviceConfigurationImpl.this.registerSpecs.add(registerSpec);
            return registerSpec;
        }

    }

    @Override
    public NumericalRegisterSpec.Updater getRegisterSpecUpdaterFor(NumericalRegisterSpec registerSpec) {
        return new NumericalRegisterSpecUpdaterForConfig(registerSpec);
    }

    class NumericalRegisterSpecUpdaterForConfig extends NumericalRegisterSpecImpl.AbstractUpdater {

        NumericalRegisterSpecUpdaterForConfig(NumericalRegisterSpec registerSpec) {
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
        return new TextualRegisterSpecUpdaterForConfig(registerSpec);
    }

    class TextualRegisterSpecUpdaterForConfig extends TextualRegisterSpecImpl.AbstractUpdater {

        TextualRegisterSpecUpdaterForConfig(TextualRegisterSpec registerSpec) {
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
            if (!isSameIdObject(registerSpec, spec) && spec.getDeviceObisCode().equals(registerSpec.getDeviceObisCode())) {
                throw DuplicateObisCodeException.forRegisterSpec(this.getThesaurus(), this, registerSpec.getDeviceObisCode());
            }
        }
    }

    public void deleteRegisterSpec(RegisterSpec registerSpec) {
        if (isActive()) {
            throw CannotDeleteFromActiveDeviceConfigurationException.canNotDeleteRegisterSpec(this.getThesaurus(), this, registerSpec);
        }
        registerSpec.validateDelete();
        removeFromHasIdList(this.registerSpecs,registerSpec);
        this.getEventService().postEvent(EventType.DEVICETYPE_DELETED.topic(), registerSpec);
    }

    @Override
    public List<ChannelSpec> getChannelSpecs() {
        return Collections.unmodifiableList(this.channelSpecs);
    }

    @Override
    public ChannelSpec.ChannelSpecBuilder createChannelSpec(ChannelType channelType, LoadProfileSpec loadProfileSpec) {
        return new ChannelSpecBuilderForConfig(channelSpecProvider, this, channelType, loadProfileSpec);
    }

    @Override
    public ChannelSpec.ChannelSpecBuilder createChannelSpec(ChannelType channelType, LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder) {
        return new ChannelSpecBuilderForConfig(channelSpecProvider, this, channelType, loadProfileSpecBuilder);
    }

    class ChannelSpecBuilderForConfig extends ChannelSpecImpl.ChannelSpecBuilder {

        ChannelSpecBuilderForConfig(Provider<ChannelSpecImpl> channelSpecProvider, DeviceConfiguration deviceConfiguration, ChannelType channelType, LoadProfileSpec loadProfileSpec) {
            super(channelSpecProvider, deviceConfiguration, channelType, loadProfileSpec);
        }

        ChannelSpecBuilderForConfig(Provider<ChannelSpecImpl> channelSpecProvider, DeviceConfiguration deviceConfiguration, ChannelType channelType, LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder) {
            super(channelSpecProvider, deviceConfiguration, channelType, loadProfileSpecBuilder);
        }

        @Override
        public ChannelSpec add() {
            ChannelSpec channelSpec = super.add();
            validateActiveDeviceConfiguration(CannotAddToActiveDeviceConfigurationException.aNewChannelSpec(getThesaurus()));
            validateUniqueChannelSpecPerLoadProfileSpec(channelSpec);
            DeviceConfigurationImpl.this.channelSpecs.add(channelSpec);
            return channelSpec;
        }
    }

    @Override
    public ChannelSpec.ChannelSpecUpdater getChannelSpecUpdaterFor(ChannelSpec channelSpec) {
        return new ChannelSpecUpdaterForConfig((ChannelSpecImpl) channelSpec);
    }

    class ChannelSpecUpdaterForConfig extends ChannelSpecImpl.ChannelSpecUpdater {

        protected ChannelSpecUpdaterForConfig(ChannelSpecImpl channelSpec) {
            super(channelSpec);
        }

        @Override
        public void update() {
            validateUniqueChannelSpecPerLoadProfileSpec(channelSpec);
            super.update();
        }
    }

    private void validateUniqueChannelSpecPerLoadProfileSpec(ChannelSpec channelSpec) {
        for (ChannelSpec spec : channelSpecs) {
            if (!isSameIdObject(spec, channelSpec)) {
                if (channelSpec.getLoadProfileSpec() == null) {
                    if (spec.getLoadProfileSpec() == null && channelSpec.getDeviceObisCode().equals(spec.getDeviceObisCode())) {
                        throw DuplicateObisCodeException.forChannelSpecConfigWithoutLoadProfileSpec(this.getThesaurus(), this, channelSpec.getDeviceObisCode());
                    }
                } else if (channelSpec.getLoadProfileSpec().getId() == spec.getLoadProfileSpec().getId()) {
                    if (channelSpec.getDeviceObisCode().equals(spec.getDeviceObisCode())) {
                        throw DuplicateObisCodeException.forChannelSpecInLoadProfileSpec(this.getThesaurus(), this, channelSpec.getDeviceObisCode(), channelSpec.getLoadProfileSpec());
                    }
                }
            }
        }
    }

    public void deleteChannelSpec(ChannelSpec channelSpec) {
        if (isActive()) {
            throw CannotDeleteFromActiveDeviceConfigurationException.forChannelSpec(this.getThesaurus(), channelSpec, this);
        }
        channelSpec.validateDelete();
        removeFromHasIdList(this.channelSpecs, channelSpec);
        this.getEventService().postEvent(EventType.DEVICETYPE_DELETED.topic(), channelSpec);
    }

    @Override
    public List<LoadProfileSpec> getLoadProfileSpecs() {
        return Collections.unmodifiableList(this.loadProfileSpecs);
    }

    @Override
    public LoadProfileSpec.LoadProfileSpecBuilder createLoadProfileSpec(LoadProfileType loadProfileType) {
        return new LoadProfileSpecBuilderForConfig(loadProfileSpecProvider, this, loadProfileType);
    }

    class LoadProfileSpecBuilderForConfig extends LoadProfileSpecImpl.LoadProfileSpecBuilder {

        LoadProfileSpecBuilderForConfig(Provider<LoadProfileSpecImpl> loadProfileSpecProvider, DeviceConfiguration deviceConfiguration, LoadProfileType loadProfileType) {
            super(loadProfileSpecProvider, deviceConfiguration, loadProfileType);
        }

        public LoadProfileSpec add() {
            LoadProfileSpec loadProfileSpec = super.add();
            validateActiveDeviceConfiguration(CannotAddToActiveDeviceConfigurationException.aNewLoadProfileSpec(getThesaurus()));
            validateUniqueLoadProfileType(loadProfileSpec);
            validateUniqueLoadProfileObisCode(loadProfileSpec);
            DeviceConfigurationImpl.this.loadProfileSpecs.add(loadProfileSpec);
            return loadProfileSpec;
        }
    }

    @Override
    public LoadProfileSpec.LoadProfileSpecUpdater getLoadProfileSpecUpdaterFor(LoadProfileSpec loadProfileSpec) {
        return new LoadProfileSpecUpdater((LoadProfileSpecImpl) loadProfileSpec);
    }

    private class LoadProfileSpecUpdater extends LoadProfileSpecImpl.LoadProfileSpecUpdater {

        protected LoadProfileSpecUpdater(LoadProfileSpecImpl loadProfileSpec) {
            super(loadProfileSpec);
        }

        @Override
        public void update() {
            validateUniqueLoadProfileObisCode(loadProfileSpec);
            super.update();
        }
    }

    private void validateUniqueLoadProfileObisCode(LoadProfileSpec loadProfileSpec) {
        for (LoadProfileSpec profileSpec : loadProfileSpecs) {
            if (!isSameIdObject(loadProfileSpec, profileSpec)
                && profileSpec.getDeviceObisCode().equals(loadProfileSpec.getDeviceObisCode())) {
                throw DuplicateObisCodeException.forLoadProfileSpec(this.getThesaurus(), this, loadProfileSpec.getDeviceObisCode());
            }
        }
    }

    private void validateUniqueLoadProfileType(LoadProfileSpec loadProfileSpec) {
        for (LoadProfileSpec profileSpec : loadProfileSpecs) {
            if (profileSpec.getLoadProfileType().getId() == loadProfileSpec.getLoadProfileType().getId()) {
                throw new DuplicateLoadProfileTypeException(this.getThesaurus(), this, loadProfileSpec.getLoadProfileType(), loadProfileSpec);
            }
        }
    }

    @Override
    public void deleteLoadProfileSpec(LoadProfileSpec loadProfileSpec) {
        if (isActive()) {
            throw CannotDeleteFromActiveDeviceConfigurationException.forLoadProfileSpec(this.getThesaurus(), loadProfileSpec, this);
        }
        loadProfileSpec.validateDelete();
        removeFromHasIdList(this.loadProfileSpecs,loadProfileSpec);
        this.getEventService().postEvent(EventType.DEVICETYPE_DELETED.topic(), loadProfileSpec);
    }

    @Override
    public List<LogBookSpec> getLogBookSpecs() {
        return Collections.unmodifiableList(this.logBookSpecs);
    }

    @Override
    public LogBookSpec.LogBookSpecBuilder createLogBookSpec(LogBookType logBookType) {
        return new LogBookSpecBuilderForConfig(logBookSpecProvider, this, logBookType);
    }

    private class LogBookSpecBuilderForConfig extends LogBookSpecImpl.LogBookSpecBuilder {

        LogBookSpecBuilderForConfig(Provider<LogBookSpecImpl> logBookSpecProvider, DeviceConfiguration deviceConfiguration, LogBookType logBookType) {
            super(logBookSpecProvider, deviceConfiguration, logBookType);
        }

        @Override
        public LogBookSpecImpl add() {
            LogBookSpecImpl logBookSpec = super.add();
            validateActiveDeviceConfiguration(CannotAddToActiveDeviceConfigurationException.aNewLogBookSpec(getThesaurus()));
            validateUniqueLogBookType(logBookSpec);
            validateUniqueLogBookObisCode(logBookSpec);
            DeviceConfigurationImpl.this.logBookSpecs.add(logBookSpec);
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
        return new LogBookSpecUpdaterForConfig((LogBookSpecImpl) logBookSpec);
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
                throw new DuplicateLogBookTypeException(this.getThesaurus(), this, logBookSpec.getLogBookType(), logBookSpec);
            }
        }
    }

    private void validateUniqueLogBookObisCode(LogBookSpec logBookSpec) {
        for (LogBookSpec bookSpec : logBookSpecs) {
            if (!isSameIdObject(bookSpec, logBookSpec)
                    && bookSpec.getDeviceObisCode().equals(logBookSpec.getDeviceObisCode())) {
                throw DuplicateObisCodeException.forLogBookSpec(this.getThesaurus(), this, logBookSpec.getDeviceObisCode());
            }
        }
    }

    public void deleteLogBookSpec(LogBookSpec logBookSpec) {
        if (isActive()) {
            throw CannotDeleteFromActiveDeviceConfigurationException.forLogbookSpec(this.getThesaurus(), logBookSpec, this);
        }
        logBookSpec.validateDelete();
        removeFromHasIdList(this.logBookSpecs,logBookSpec);
        this.getEventService().postEvent(EventType.DEVICETYPE_DELETED.topic(), logBookSpec);
    }

    @Override
    public boolean isActive() {
        return active;
    }

    public void activate() {
        this.active = true;
        super.save();
    }

    public void deactivate() {
        this.getEventService().postEvent(EventType.DEVICECONFIGURATION_VALIDATEDEACTIVATE.topic(), this);
        this.active = false;
        super.save();
    }

    @Override
    public void save() {
        this.protocolConfigurationPropertyChanges.apply();
        boolean creating = getId() == 0;
        super.save();
        if (creating) {
            for (PartialConnectionTask partialConnectionTask : partialConnectionTasks) {
                this.getEventService().postEvent(((PersistentIdObject) partialConnectionTask).createEventType().topic(), partialConnectionTask);
            }
        }
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
            throw new DeviceTypeIsRequiredException(this.getThesaurus());
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
            throw new DeviceConfigurationIsActiveException(this.getThesaurus(), this);
        }
    }

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

    @Override
    public void remove(PartialConnectionTask partialConnectionTask) {
        this.remove((PartialConnectionTaskImpl) partialConnectionTask);
    }

    private void remove(PartialConnectionTaskImpl partialConnectionTask) {
        partialConnectionTask.validateDelete();
        getServerDeviceType().removeConflictsFor(partialConnectionTask);
        if (partialConnectionTasks.remove(partialConnectionTask) && getId() > 0) {
            this.getEventService().postEvent(partialConnectionTask.deleteEventType().topic(), partialConnectionTask);
        }
    }

    private ServerDeviceType getServerDeviceType() {
        return (ServerDeviceType) deviceType.get();
    }

    @Override
    public boolean isSupportsAllProtocolMessages() {
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
            this.deviceMessageEnablements.clear();
            if (deviceMessageUserActions.length > 0) {
                this.supportsAllProtocolMessagesUserActionsBitVector = toDatabaseValue(EnumSet.copyOf(Arrays.asList(deviceMessageUserActions)));
            }
        }
        else {
            this.supportsAllProtocolMessagesUserActionsBitVector = 0;
        }
    }

    @Override
    public void addSecurityPropertySet(SecurityPropertySet securityPropertySet) {
        Save.CREATE.validate(this.getDataModel(), securityPropertySet);
        securityPropertySets.add(securityPropertySet);
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
            if (candidate.getDeviceProtocolDialect().getDeviceProtocolDialectName().equals(protocolDialect.getDeviceProtocolDialectName())) {
                return candidate;
            }
        }
        ProtocolDialectConfigurationPropertiesImpl properties = ProtocolDialectConfigurationPropertiesImpl.from(this.getDataModel(), this, protocolDialect);
        configurationPropertiesList.add(properties);
        return properties;
    }

    @Override
    public List<ProtocolDialectConfigurationProperties> getProtocolDialectConfigurationPropertiesList() {
        return Collections.unmodifiableList(configurationPropertiesList);
    }

    @Override
    public List<SecurityPropertySet> getSecurityPropertySets() {
        return Collections.unmodifiableList(securityPropertySets);
    }

    @Override
    public SecurityPropertySetBuilder createSecurityPropertySet(String name) {
        return new InternalSecurityPropertySetBuilder(name);
    }

    @Override
    public void removeSecurityPropertySet(SecurityPropertySet propertySet) {
        if (propertySet != null) {
            ((SecurityPropertySetImpl) propertySet).validateDelete();
            securityPropertySets.remove(propertySet);
        }
    }

    @Override
    public PartialScheduledConnectionTaskBuilder newPartialScheduledConnectionTask(String name, ConnectionTypePluggableClass connectionType, TimeDuration rescheduleRetryDelay, ConnectionStrategy connectionStrategy) {
        return new PartialScheduledConnectionTaskBuilderImpl(this.getDataModel(), this, this.schedulingService, this.getEventService()).name(name)
                .pluggableClass(connectionType)
                .rescheduleDelay(rescheduleRetryDelay)
                .connectionStrategy(connectionStrategy);
    }

    @Override
    public PartialInboundConnectionTaskBuilder newPartialInboundConnectionTask(String name, ConnectionTypePluggableClass connectionType) {
        return new PartialInboundConnectionTaskBuilderImpl(this.getDataModel(), this)
                .name(name)
                .pluggableClass(connectionType);
    }

    @Override
    public PartialConnectionInitiationTaskBuilder newPartialConnectionInitiationTask(String name, ConnectionTypePluggableClass connectionType, TimeDuration rescheduleRetryDelay) {
        return new PartialConnectionInitiationTaskBuilderImpl(this.getDataModel(), this, this.schedulingService, this.getEventService())
                .name(name)
                .pluggableClass(connectionType)
                .rescheduleDelay(rescheduleRetryDelay);
    }

    public void addPartialConnectionTask(PartialConnectionTask partialConnectionTask) {
        Save.CREATE.validate(this.getDataModel(), partialConnectionTask);
        partialConnectionTasks.add(partialConnectionTask);
    }

    @Override
    public ComTaskEnablementBuilder enableComTask(ComTask comTask, SecurityPropertySet securityPropertySet, ProtocolDialectConfigurationProperties configurationProperties) {
        ComTaskEnablementImpl underConstruction = this.getDataModel().getInstance(ComTaskEnablementImpl.class).initialize(this, comTask, securityPropertySet, configurationProperties);
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
                return;
            }
        }
        throw new CannotDisableComTaskThatWasNotEnabledException(this.getThesaurus(), this, comTask);
    }

    @Override
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
        return this.deviceMessageEnablements.removeIf(deviceMessageEnablement -> deviceMessageEnablement.getDeviceMessageId().equals(deviceMessageId));
    }

    private void addDeviceMessageEnablement(DeviceMessageEnablement singleDeviceMessageEnablement) {
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
            if (isSupportsAllProtocolMessages()) {
                if (this.getDeviceType().getDeviceProtocolPluggableClass().getDeviceProtocol().getSupportedMessages().contains(deviceMessageId)){
                    return getAllProtocolMessagesUserActions().stream().anyMatch(deviceMessageUserAction -> isUserAuthorizedForAction(deviceMessageUserAction, user));
                }
            }
            java.util.Optional<DeviceMessageEnablement> deviceMessageEnablementOptional = getDeviceMessageEnablements().stream().filter(deviceMessageEnablement -> deviceMessageEnablement.getDeviceMessageId().equals(deviceMessageId)).findAny();
            if (deviceMessageEnablementOptional.isPresent()) {
                return deviceMessageEnablementOptional.get().getUserActions().stream().anyMatch(deviceMessageUserAction -> isUserAuthorizedForAction(deviceMessageUserAction, user));
            }
        }
        return false;
    }

    private boolean isUserAuthorizedForAction(DeviceMessageUserAction action, User user) {
        return user.hasPrivilege("MDC",action.getPrivilege());
    }

    private Optional<User> getCurrentUser() {
        Principal principal = threadPrincipalService.getPrincipal();
        if (!(principal instanceof User)) {
            return Optional.empty();
        }
        return Optional.of((User) principal);
    }

    public List<DeviceConfValidationRuleSetUsage> getDeviceConfValidationRuleSetUsages() {
        return deviceConfValidationRuleSetUsages;
    }

    public List<ValidationRuleSet> getValidationRuleSets() {
        List<ValidationRuleSet> result = new ArrayList<>();
        for (DeviceConfValidationRuleSetUsage usage : this.deviceConfValidationRuleSetUsages)  {
            if (usage.getValidationRuleSet() != null) {
                result.add(usage.getValidationRuleSet());
            }
        }
        return result;
    }

    @Override
    public DeviceConfValidationRuleSetUsage addValidationRuleSet(ValidationRuleSet validationRuleSet) {
        DeviceConfValidationRuleSetUsage usage =
                deviceConfValidationRuleSetUsageFactory.get().init(validationRuleSet, this);
        deviceConfValidationRuleSetUsages.add(usage);
        return usage;
    }

    protected DeviceConfValidationRuleSetUsage getUsage(ValidationRuleSet validationRuleSet) {
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
    }

    public List<ValidationRule> getValidationRules(Iterable<? extends ReadingType> readingTypes) {
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
            DeviceConfigurationEstimationRuleSetUsage usage = deviceConfigEstimationRuleSetUsageFactory.get().init(this, estimationRuleSet);
            deviceConfigurationEstimationRuleSetUsages.add(usage);
            return usage;
        });
    }

    private Optional<DeviceConfigurationEstimationRuleSetUsage> findEstimationRuleSetUsage(EstimationRuleSet estimationRuleSet) {
        return deviceConfigurationEstimationRuleSetUsages.stream().filter(usage -> usage.getEstimationRuleSet().getId() == estimationRuleSet.getId()).findFirst();
    }

    @Override
    public void removeEstimationRuleSet(EstimationRuleSet estimationRuleSet) {
        deviceConfigurationEstimationRuleSetUsages.stream()
            .filter((usage) -> usage.getEstimationRuleSet().getId() == estimationRuleSet.getId())
            .findFirst()
            .ifPresent(deviceConfigurationEstimationRuleSetUsages::remove);
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

    public GatewayType getGetwayType(){
        return this.gatewayType;
    }

    public void setGatewayType(GatewayType gatewayType){
        if (gatewayType != null){
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
            }
            else {
                Optional<DeviceProtocolConfigurationProperty> existingProperty = this.findProperty(propertyName);
                if (existingProperty.isPresent()) {
                    this.obsoleteProperties.put(propertyName, existingProperty.get());
                    return true;
                }
                else {
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
            underConstruction.setAuthenticationLevel(level);
            return this;
        }

        @Override
        public SecurityPropertySetBuilder encryptionLevel(int level) {
            underConstruction.setEncryptionLevelId(level);
            return this;
        }

        @Override
        public SecurityPropertySetBuilder addUserAction(DeviceSecurityUserAction userAction) {
            underConstruction.addUserAction(userAction);
            return this;
        }

        @Override
        public SecurityPropertySet build() {
            DeviceConfigurationImpl.this.addSecurityPropertySet(underConstruction);
            return underConstruction;
        }
    }

    private class InternalDeviceMessageEnablementBuilder implements DeviceMessageEnablementBuilder {

        private final DeviceMessageEnablement underConstruction;

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
            Arrays.asList(deviceMessageUserActions).stream().forEach(this.underConstruction::addDeviceMessageUserAction);
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
        public ComTaskEnablementBuilder setProtocolDialectConfigurationProperties(ProtocolDialectConfigurationProperties properties) {
            this.mode.verify();
            this.underConstruction.setProtocolDialectConfigurationProperties(properties);
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
                .gatewayType(getGetwayType())
                .isDirectlyAddressable(isDirectlyAddressable())
                .add();
        this.getDeviceProtocolProperties().getPropertySpecs().stream().forEach(cloneDeviceProtocolProperties(clone));
        this.getProtocolDialectConfigurationPropertiesList().stream().forEach(cloneDeviceProtocolDialectProperties(clone));
        getSecurityPropertySets().forEach(securityPropertySet -> ((ServerSecurityPropertySet) securityPropertySet).cloneForDeviceConfig(clone));
        getPartialConnectionTasks().forEach(partialConnectionTask -> ((ServerPartialConnectionTask) partialConnectionTask).cloneForDeviceConfig(clone));
        getComTaskEnablements().forEach(comTaskEnablement -> ((ServerComTaskEnablement) comTaskEnablement).cloneForDeviceConfig(clone));
        getDeviceMessageEnablements().forEach(deviceMessageEnablement -> ((ServerDeviceMessageEnablement) deviceMessageEnablement).cloneForDeviceConfig(clone));
        getRegisterSpecs().forEach(registerSpec -> ((ServerRegisterSpec) registerSpec).cloneForDeviceConfig(clone));
        getLogBookSpecs().forEach(logBookSpec -> ((ServerLogBookSpec) logBookSpec).cloneForDeviceConfig(clone));
        getLoadProfileSpecs().forEach(loadProfileSpec -> ((ServerLoadProfileSpec) loadProfileSpec).cloneForDeviceConfig(clone));
        getValidationRuleSets().forEach(clone::addValidationRuleSet);
        getEstimationRuleSets().forEach(clone::addEstimationRuleSet);
        clone.save();
        return clone;
    }

    private Consumer<ProtocolDialectConfigurationProperties> cloneDeviceProtocolDialectProperties(DeviceConfiguration clone) {
        return protocolDialectConfigurationProperties -> protocolDialectConfigurationProperties.getPropertySpecs().stream().forEach(copyDeviceProtocolDialectProperty(clone, protocolDialectConfigurationProperties));
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
            if(propertyValue != null){
                clone.getDeviceProtocolProperties().setProperty(propertySpec.getName(), propertyValue);
            }
        };
    }
}