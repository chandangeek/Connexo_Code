package com.energyict.mdc.device.config.impl;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.ComTaskEnablementBuilder;
import com.energyict.mdc.device.config.ConnectionStrategy;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.DeviceCommunicationFunction;
import com.energyict.mdc.device.config.DeviceConfValidationRuleSetUsage;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.LogBookSpec;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.config.PartialConnectionInitiationTask;
import com.energyict.mdc.device.config.PartialConnectionInitiationTaskBuilder;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.device.config.PartialInboundConnectionTaskBuilder;
import com.energyict.mdc.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.device.config.PartialScheduledConnectionTaskBuilder;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.config.SecurityPropertySetBuilder;
import com.energyict.mdc.device.config.TextualRegisterSpec;
import com.energyict.mdc.device.config.exceptions.CannotAddToActiveDeviceConfigurationException;
import com.energyict.mdc.device.config.exceptions.CannotDeleteFromActiveDeviceConfigurationException;
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
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.tasks.ComTask;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import org.hibernate.validator.constraints.NotEmpty;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *     //TODO the creation of the CommunicationConfiguration is currently skipped ...
 *
 * User: gde
 * Date: 5/11/12
 */
@DeviceFunctionsAreSupportedByProtocol(groups = {Save.Update.class, Save.Create.class})
@ImmutablePropertiesCanNotChangeForActiveConfiguration(groups = {Save.Update.class, Save.Create.class})
public class DeviceConfigurationImpl extends PersistentNamedObject<DeviceConfiguration> implements DeviceConfiguration, ServerDeviceConfiguration {

    private static final DeviceCommunicationFunctionSetPersister deviceCommunicationFunctionSetPersister = new DeviceCommunicationFunctionSetPersister();

    enum Fields {
        CAN_ACT_AS_GATEWAY("canActAsGateway"), // 'virtual' BeanProperty not backed by actual member
        IS_DIRECTLY_ADDRESSABLE("isDirectlyAddressable"); // 'virtual' BeanProperty not backed by actual member
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
    private DeviceCommunicationConfiguration communicationConfiguration;
    private Set<DeviceCommunicationFunction> deviceCommunicationFunctions;
    private int communicationFunctionMask;
    private Date modificationDate;
    private Clock clock;
    private final Provider<LoadProfileSpecImpl> loadProfileSpecProvider;
    private final Provider<NumericalRegisterSpecImpl> numericalRegisterSpecProvider;
    private final Provider<TextualRegisterSpecImpl> textualRegisterSpecProvider;
    private final Provider<LogBookSpecImpl> logBookSpecProvider;
    private final Provider<ChannelSpecImpl> channelSpecProvider;
    private final DeviceConfigurationService deviceConfigurationService;

    private List<DeviceConfValidationRuleSetUsage> deviceConfValidationRuleSetUsages = new ArrayList<>();
    private final Provider<DeviceConfValidationRuleSetUsageImpl> deviceConfValidationRuleSetUsageFactory;

    @Inject
    protected DeviceConfigurationImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, Clock clock,
                                      Provider<LoadProfileSpecImpl> loadProfileSpecProvider,
                                      Provider<NumericalRegisterSpecImpl> numericalRegisterSpecProvider,
                                      Provider<TextualRegisterSpecImpl> textualRegisterSpecProvider,
                                      Provider<LogBookSpecImpl> logBookSpecProvider,
                                      Provider<ChannelSpecImpl> channelSpecProvider,
                                      Provider<DeviceConfValidationRuleSetUsageImpl> deviceConfValidationRuleSetUsageFactory,
                                      DeviceConfigurationService deviceConfigurationService) {
        super(DeviceConfiguration.class, dataModel, eventService, thesaurus);
        this.clock = clock;

        this.loadProfileSpecProvider = loadProfileSpecProvider;
        this.numericalRegisterSpecProvider = numericalRegisterSpecProvider;
        this.textualRegisterSpecProvider = textualRegisterSpecProvider;
        this.logBookSpecProvider = logBookSpecProvider;
        this.channelSpecProvider = channelSpecProvider;
        this.deviceConfValidationRuleSetUsageFactory = deviceConfValidationRuleSetUsageFactory;
        this.deviceConfigurationService = deviceConfigurationService;
    }

    DeviceConfigurationImpl initialize(DeviceType deviceType, String name){
        this.deviceType.set(deviceType);
        setName(name);
        return this;
    }

    @Override
    public DeviceCommunicationConfiguration getCommunicationConfiguration() {
        if (this.communicationConfiguration == null) {
            communicationConfiguration = deviceConfigurationService.findDeviceCommunicationConfigurationFor(this);
            if (communicationConfiguration == null) {
                communicationConfiguration = deviceConfigurationService.newDeviceCommunicationConfiguration(this);
                communicationConfiguration.save();
            }
        }
        return this.communicationConfiguration;
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
        }
    }

    @Override
    public boolean canBeDirectlyAddressable() {
        return hasCommunicationFunction(DeviceCommunicationFunction.PROTOCOL_SESSION);
    }

    @Override
    public void setCanBeDirectlyAddressed(boolean canBeDirectlyAddressed) {
        if (canBeDirectlyAddressed) {
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
                throw new DuplicateLoadProfileTypeException(this.thesaurus, this, loadProfileType, each);
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
                throw DuplicateObisCodeException.forLogBookSpec(this.thesaurus, this, each.getDeviceObisCode(), each);
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
        if (this.communicationConfiguration == null) {
            communicationConfiguration = deviceConfigurationService.findDeviceCommunicationConfigurationFor(this);
        }
        if (communicationConfiguration != null) {
            communicationConfiguration.delete();
        }
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
                    throw DuplicateObisCodeException.forChannelSpecInLoadProfileSpec(this.thesaurus, this, each.getDeviceObisCode(), each, each.getLoadProfileSpec());
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
                throw DuplicateObisCodeException.forRegisterSpec(this.thesaurus, this, registerSpec.getDeviceObisCode(), registerSpec);
            }
        }
    }

    /**
     * Looks for the next available Obiscode with different B-field.
     *
     * @param obisCodeValue
     * @param obisCodeKeys
     * @return
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
                throw DuplicateObisCodeException.forRegisterSpec(thesaurus, this, registerSpec.getDeviceObisCode(), registerSpec);
            }
        }
    }

    public void deleteRegisterSpec(RegisterSpec registerSpec) {
        // TODO Complete!!!

//        if (getActive() && !shadow.getRegisterSpecShadows().getDeletedShadows().isEmpty()) {
//            throw new BusinessException("deleteRegisterSpecsFromActiveDeviceConfigIsNotAllowed",
//                    "It's not allowed to delete register specifications of an active device configuration");
//        }
        if (isActive()) {
            throw CannotDeleteFromActiveDeviceConfigurationException.canNotDeleteRegisterSpec(this.thesaurus, this, registerSpec);
        }
        registerSpec.validateDelete();
        removeFromHasIdList(this.registerSpecs,registerSpec);
        this.eventService.postEvent(EventType.DEVICETYPE_DELETED.topic(),registerSpec);
    }

    @Override
    public List<ChannelSpec> getChannelSpecs() {
        return Collections.unmodifiableList(this.channelSpecs);
    }

    @Override
    public ChannelSpec.ChannelSpecBuilder createChannelSpec(ChannelType channelType, Phenomenon phenomenon, LoadProfileSpec loadProfileSpec) {
        return new ChannelSpecBuilderForConfig(channelSpecProvider, this, channelType, phenomenon, loadProfileSpec);
    }

    @Override
    public ChannelSpec.ChannelSpecBuilder createChannelSpec(ChannelType channelType, Phenomenon phenomenon, LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder) {
        return new ChannelSpecBuilderForConfig(channelSpecProvider, this, channelType, phenomenon, loadProfileSpecBuilder);
    }

    class ChannelSpecBuilderForConfig extends ChannelSpecImpl.ChannelSpecBuilder {

        ChannelSpecBuilderForConfig(Provider<ChannelSpecImpl> channelSpecProvider, DeviceConfiguration deviceConfiguration, ChannelType channelType, Phenomenon phenomenon, LoadProfileSpec loadProfileSpec) {
            super(channelSpecProvider, deviceConfiguration, channelType, phenomenon, loadProfileSpec);
        }

        ChannelSpecBuilderForConfig(Provider<ChannelSpecImpl> channelSpecProvider, DeviceConfiguration deviceConfiguration, ChannelType channelType, Phenomenon phenomenon, LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder) {
            super(channelSpecProvider, deviceConfiguration, channelType, phenomenon, loadProfileSpecBuilder);
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
            if(!isSameIdObject(spec, channelSpec)){
                if (channelSpec.getLoadProfileSpec() == null) {
                    if (spec.getLoadProfileSpec() == null && channelSpec.getDeviceObisCode().equals(spec.getDeviceObisCode())) {
                        throw DuplicateObisCodeException.forChannelSpecConfigWithoutLoadProfileSpec(thesaurus, this, channelSpec.getDeviceObisCode(), channelSpec);
                    }
                } else if (channelSpec.getLoadProfileSpec().getId() == spec.getLoadProfileSpec().getId()) {
                    if (channelSpec.getDeviceObisCode().equals(spec.getDeviceObisCode())) {
                        throw DuplicateObisCodeException.forChannelSpecInLoadProfileSpec(thesaurus, this, channelSpec.getDeviceObisCode(), channelSpec, channelSpec.getLoadProfileSpec());
                    }
                }
            }
        }
    }

    public void deleteChannelSpec(ChannelSpec channelSpec) {
        if (isActive()) {
            throw CannotDeleteFromActiveDeviceConfigurationException.forChannelSpec(this.thesaurus, channelSpec, this);
        }
        channelSpec.validateDelete();
        removeFromHasIdList(this.channelSpecs, channelSpec);
        this.eventService.postEvent(EventType.DEVICETYPE_DELETED.topic(),channelSpec);
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
                throw DuplicateObisCodeException.forLoadProfileSpec(thesaurus, this, loadProfileSpec.getDeviceObisCode(), loadProfileSpec);
            }
        }
    }

    private void validateUniqueLoadProfileType(LoadProfileSpec loadProfileSpec) {
        for (LoadProfileSpec profileSpec : loadProfileSpecs) {
            if (profileSpec.getLoadProfileType().getId() == loadProfileSpec.getLoadProfileType().getId()) {
                throw new DuplicateLoadProfileTypeException(thesaurus, this, loadProfileSpec.getLoadProfileType(), loadProfileSpec);
            }
        }
    }

    @Override
    public void deleteLoadProfileSpec(LoadProfileSpec loadProfileSpec) {
        if (isActive()) {
            throw CannotDeleteFromActiveDeviceConfigurationException.forLoadProfileSpec(this.thesaurus, loadProfileSpec, this);
        }
        loadProfileSpec.validateDelete();
        removeFromHasIdList(this.loadProfileSpecs,loadProfileSpec);
        this.eventService.postEvent(EventType.DEVICETYPE_DELETED.topic(),loadProfileSpec);
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
                throw new DuplicateLogBookTypeException(thesaurus, this, logBookSpec.getLogBookType(), logBookSpec);
            }
        }
    }

    private void validateUniqueLogBookObisCode(LogBookSpec logBookSpec) {
        for (LogBookSpec bookSpec : logBookSpecs) {
            if (!isSameIdObject(bookSpec, logBookSpec)
                    && bookSpec.getDeviceObisCode().equals(logBookSpec.getDeviceObisCode())) {
                throw DuplicateObisCodeException.forLogBookSpec(thesaurus, this, logBookSpec.getDeviceObisCode(), logBookSpec);
            }
        }
    }

    public void deleteLogBookSpec(LogBookSpec logBookSpec) {
        if (isActive()) {
            throw CannotDeleteFromActiveDeviceConfigurationException.forLogbookSpec(this.thesaurus, logBookSpec, this);
        }
        logBookSpec.validateDelete();
        removeFromHasIdList(this.logBookSpecs,logBookSpec);
        this.eventService.postEvent(EventType.DEVICETYPE_DELETED.topic(),logBookSpec);
    }

    @Override
    public boolean isActive() {
        return active;
    }

    public void activate() {
        this.active = true;
        this.modificationDate = this.clock.now();
        super.save();
    }

    public void deactivate() {
        this.getEventService().postEvent(EventType.DEVICECONFIGURATION_VALIDATEDEACTIVATE.topic(), this);
        this.active = false;
        this.modificationDate = this.clock.now();
        super.save();
    }

    @Override
    public void save() {
        this.modificationDate = this.clock.now();
        super.save();
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
            throw new DeviceTypeIsRequiredException(this.thesaurus);
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
            throw new DeviceConfigurationIsActiveException(this.thesaurus, this);
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
    public DeviceConfiguration getDeviceConfiguration() {
        return this;
    }

    @Override
    public void remove(PartialConnectionTask partialConnectionTask) {
        getCommunicationConfiguration().remove(partialConnectionTask);
    }

    @Override
    public void setSupportsAllMessageCategories(boolean supportAllMessageCategories) {
        getCommunicationConfiguration().setSupportsAllMessageCategories(supportAllMessageCategories);
    }

    @Override
    public void addSecurityPropertySet(SecurityPropertySet securityPropertySet) {
        getCommunicationConfiguration().addSecurityPropertySet(securityPropertySet);
    }

    @Override
    public List<PartialConnectionTask> getPartialConnectionTasks() {
        return getCommunicationConfiguration().getPartialConnectionTasks();
    }

    @Override
    public List<PartialInboundConnectionTask> getPartialInboundConnectionTasks() {
        return getCommunicationConfiguration().getPartialInboundConnectionTasks();
    }

    @Override
    public List<PartialScheduledConnectionTask> getPartialOutboundConnectionTasks() {
        return getCommunicationConfiguration().getPartialOutboundConnectionTasks();
    }

    @Override
    public List<PartialConnectionInitiationTask> getPartialConnectionInitiationTasks() {
        return getCommunicationConfiguration().getPartialConnectionInitiationTasks();
    }

    @Override
    public ProtocolDialectConfigurationProperties findOrCreateProtocolDialectConfigurationProperties(DeviceProtocolDialect protocolDialect) {
        return getCommunicationConfiguration().findOrCreateProtocolDialectConfigurationProperties(protocolDialect);
    }

    @Override
    public List<ProtocolDialectConfigurationProperties> getProtocolDialectConfigurationPropertiesList() {
        return getCommunicationConfiguration().getProtocolDialectConfigurationPropertiesList();
    }

    @Override
    public List<SecurityPropertySet> getSecurityPropertySets() {
        return getCommunicationConfiguration().getSecurityPropertySets();
    }

    @Override
    public SecurityPropertySetBuilder createSecurityPropertySet(String name) {
        return getCommunicationConfiguration().createSecurityPropertySet(name);
    }

    @Override
    public void removeSecurityPropertySet(SecurityPropertySet propertySet) {
        getCommunicationConfiguration().removeSecurityPropertySet(propertySet);
    }

    @Override
    public PartialScheduledConnectionTaskBuilder newPartialScheduledConnectionTask(String name, ConnectionTypePluggableClass connectionType, TimeDuration rescheduleRetryDelay, ConnectionStrategy connectionStrategy) {
        return getCommunicationConfiguration().newPartialScheduledConnectionTask(name, connectionType, rescheduleRetryDelay, connectionStrategy);
    }

    @Override
    public PartialInboundConnectionTaskBuilder newPartialInboundConnectionTask(String name, ConnectionTypePluggableClass connectionType) {
        return getCommunicationConfiguration().newPartialInboundConnectionTask(name, connectionType);
    }

    @Override
    public PartialConnectionInitiationTaskBuilder newPartialConnectionInitiationTask(String name, ConnectionTypePluggableClass connectionType, TimeDuration rescheduleRetryDelay) {
        return getCommunicationConfiguration().newPartialConnectionInitiationTask(name, connectionType, rescheduleRetryDelay);
    }

    @Override
    public ComTaskEnablementBuilder enableComTask(ComTask comTask, SecurityPropertySet securityPropertySet) {
        return this.getCommunicationConfiguration().enableComTask(comTask, securityPropertySet);
    }

    @Override
    public void disableComTask(ComTask comTask) {
        this.getCommunicationConfiguration().disableComTask(comTask);
    }

    @Override
    public List<ComTaskEnablement> getComTaskEnablements() {
        return this.getCommunicationConfiguration().getComTaskEnablements();
    }

    public List<DeviceConfValidationRuleSetUsage> getDeviceConfValidationRuleSetUsages() {
        return deviceConfValidationRuleSetUsages;
    }

    public List<ValidationRuleSet> getValidationRuleSets() {
        List<ValidationRuleSet> result = new ArrayList<ValidationRuleSet>();
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

    public void removeValidationRuleSet(ValidationRuleSet validationRuleSet) {
        DeviceConfValidationRuleSetUsage usage = getUsage(validationRuleSet);
        deviceConfValidationRuleSetUsages.remove(usage);
    }

    public List<ValidationRule> getValidationRules(Iterable<? extends ReadingType> readingTypes) {
        List<ValidationRule> result = new ArrayList<ValidationRule>();
        List<ValidationRuleSet> ruleSets = getValidationRuleSets();
        for (ValidationRuleSet ruleSet : ruleSets) {
            result.addAll(ruleSet.getRules(readingTypes));
        }
        return result;
    }

}