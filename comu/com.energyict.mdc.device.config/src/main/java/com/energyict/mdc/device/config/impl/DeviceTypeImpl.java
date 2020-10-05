/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.calendar.Calendar;
import com.elster.jupiter.calendar.OutOfTheBoxCategory;
import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.domain.util.HasNoBlacklistedCharacters;
import com.elster.jupiter.domain.util.HasNotAllowedChars;
import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryStream;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.TemporalReference;
import com.elster.jupiter.orm.associations.Temporals;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.device.config.AllowedCalendar;
import com.energyict.mdc.common.device.config.ChannelSpec;
import com.energyict.mdc.common.device.config.ConfigurationSecurityProperty;
import com.energyict.mdc.common.device.config.ConflictingConnectionMethodSolution;
import com.energyict.mdc.common.device.config.DeleteEventType;
import com.energyict.mdc.common.device.config.DeviceConfigConflictMapping;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.config.DeviceLifeCycleChangeEvent;
import com.energyict.mdc.common.device.config.DeviceMessageEnablementBuilder;
import com.energyict.mdc.common.device.config.DeviceMessageFile;
import com.energyict.mdc.common.device.config.DeviceMessageUserAction;
import com.energyict.mdc.common.device.config.DeviceSecurityAccessorType;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.config.DeviceTypePurpose;
import com.energyict.mdc.common.device.config.DeviceUsageType;
import com.energyict.mdc.common.device.config.EventType;
import com.energyict.mdc.common.device.config.GatewayType;
import com.energyict.mdc.common.device.config.LoadProfileSpec;
import com.energyict.mdc.common.device.config.LogBookSpec;
import com.energyict.mdc.common.device.config.NumericalRegisterSpec;
import com.energyict.mdc.common.device.config.RegisterSpec;
import com.energyict.mdc.common.device.config.SecurityAccessorTypeOnDeviceType;
import com.energyict.mdc.common.device.config.SecurityPropertySet;
import com.energyict.mdc.common.device.config.TextualRegisterSpec;
import com.energyict.mdc.common.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.common.masterdata.ChannelType;
import com.energyict.mdc.common.masterdata.LoadProfileType;
import com.energyict.mdc.common.masterdata.LogBookType;
import com.energyict.mdc.common.masterdata.MeasurementType;
import com.energyict.mdc.common.masterdata.RegisterType;
import com.energyict.mdc.common.protocol.DeviceMessageId;
import com.energyict.mdc.common.protocol.DeviceProtocol;
import com.energyict.mdc.common.protocol.DeviceProtocolPluggableClass;
import com.energyict.mdc.common.tasks.PartialConnectionTask;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.TimeOfUseOptions;
import com.energyict.mdc.device.config.exceptions.CannotDeleteBecauseStillInUseException;
import com.energyict.mdc.device.config.exceptions.DataloggerSlaveException;
import com.energyict.mdc.device.config.exceptions.DuplicateDeviceMessageFileException;
import com.energyict.mdc.device.config.exceptions.LoadProfileTypeAlreadyInDeviceTypeException;
import com.energyict.mdc.device.config.exceptions.LogBookTypeAlreadyInDeviceTypeException;
import com.energyict.mdc.device.config.exceptions.RegisterTypeAlreadyInDeviceTypeException;
import com.energyict.mdc.device.config.impl.deviceconfigchange.DeviceConfigConflictMappingImpl;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.upl.DeviceProtocolCapabilities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.Size;
import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ValidChangesWithExistingConfigurations(groups = {Save.Update.class})
@DeviceProtocolPluggableClassValidation(groups = {Save.Create.class, Save.Update.class})
@XmlRootElement
public class DeviceTypeImpl extends PersistentNamedObject<DeviceType> implements ServerDeviceType {
    enum Fields {
        DEVICE_PROTOCOL_PLUGGABLE_CLASS("deviceProtocolPluggableClassId"),
        NAME("name"),
        CONFLICTINGMAPPING("deviceConfigConflictMappings"),
        CUSTOMPROPERTYSETUSAGE("deviceTypeCustomPropertySetUsages"),
        ALLOWEDCALENDARS("allowedCalendars"),
        DEVICETYPEPURPOSE("deviceTypePurpose"),
        DEVICE_LIFE_CYCLE("deviceLifeCycle"),
        FILE_MANAGEMENT_ENABLED("fileManagementEnabled"),
        DEVICE_MESSAGE_FILES("deviceMessageFiles"),
        SECURITY_ACCESSOR_TYPES("securityAccessorTypes"),
        DEVICE_SHARED_SCHEDULES("deviceSharedSchedule");

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
    @HasNoBlacklistedCharacters(balcklistedCharRegEx = HasNotAllowedChars.Constant.SCRIPT_CHARS)
    private String name;
    @Size(max = 4000, groups = {Save.Update.class, Save.Create.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    @HasNoBlacklistedCharacters(balcklistedCharRegEx = HasNotAllowedChars.Constant.SCRIPT_CHARS)
    private String description;
    @Size(max = 4000, groups = {Save.Update.class, Save.Create.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String deviceSharedSchedule;
    @IsPresent(message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}", groups = {Save.Create.class, Save.Update.class})
    private TemporalReference<DeviceLifeCycleInDeviceType> deviceLifeCycle = Temporals.absent();
    private Reference<DeviceIcon> deviceIcon = Reference.empty();
    private int deviceUsageTypeId;
    private DeviceUsageType deviceUsageType;
    @Valid
    private List<ServerDeviceConfiguration> deviceConfigurations = new ArrayList<>();
    private List<DeviceTypeLogBookTypeUsage> logBookTypeUsages = new ArrayList<>();
    private List<DeviceTypeLoadProfileTypeUsage> loadProfileTypeUsages = new ArrayList<>();
    private List<DeviceTypeRegisterTypeUsage> registerTypeUsages = new ArrayList<>();
    private List<AllowedCalendar> allowedCalendars = new ArrayList<>();
    @Valid
    private List<DeviceConfigConflictMappingImpl> deviceConfigConflictMappings = new ArrayList<>();
    @Valid
    private List<DeviceTypeCustomPropertySetUsageImpl> deviceTypeCustomPropertySetUsages = new ArrayList<>();
    private long deviceProtocolPluggableClassId;
    private DeviceProtocolPluggableClass deviceProtocolPluggableClass;
    private boolean deviceProtocolPluggableClassChanged = false;
    private boolean fileManagementEnabled = false;
    private List<ServerDeviceMessageFile> deviceMessageFiles = new ArrayList<>();
    private List<SecurityAccessorTypeOnDeviceTypeImpl> securityAccessorTypes = new ArrayList<>();
    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;

    private Clock clock;
    private ProtocolPluggableService protocolPluggableService;
    private DeviceConfigurationService deviceConfigurationService;
    private CustomPropertySetService customPropertySetService;

    private DeviceTypePurpose deviceTypePurpose = DeviceTypePurpose.REGULAR;
    private boolean deviceTypePurposeChanged = false;

    private boolean logicalSlave;

    /**
     * The DeviceProtocol of this DeviceType, only for local usage.
     */
    private DeviceProtocol localDeviceProtocol;

    public DeviceTypeImpl() {
        super();
    }

    @Inject
    public DeviceTypeImpl(DataModel dataModel, EventService eventService, Thesaurus thesaurus, Clock clock, ProtocolPluggableService protocolPluggableService, DeviceConfigurationService deviceConfigurationService, CustomPropertySetService customPropertySetService) {
        super(DeviceType.class, dataModel, eventService, thesaurus);
        this.clock = clock;
        this.protocolPluggableService = protocolPluggableService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.customPropertySetService = customPropertySetService;
    }

    private DeviceTypeImpl initializeRegular(String name, DeviceProtocolPluggableClass deviceProtocolPluggableClass, DeviceLifeCycle deviceLifeCycle, String deviceSharedSchedule) {
        this.setName(name);
        this.setDeviceProtocolPluggableClass(deviceProtocolPluggableClass);
        this.setDeviceLifeCycle(deviceLifeCycle, this.clock.instant());
        this.setSharedSchedule(deviceSharedSchedule);
        return this;
    }

    private DeviceType initializeDataloggerSlave(String name, DeviceLifeCycle deviceLifeCycle) {
        this.setName(name);
        this.setDeviceLifeCycle(deviceLifeCycle, this.clock.instant());
        this.deviceTypePurpose = DeviceTypePurpose.DATALOGGER_SLAVE;
        return this;
    }

    private DeviceType initializeMultiElementSlave(String name, DeviceLifeCycle deviceLifeCycle) {
        this.setName(name);
        this.setDeviceLifeCycle(deviceLifeCycle, this.clock.instant());
        this.deviceTypePurpose = DeviceTypePurpose.MULTI_ELEMENT_SLAVE;
        return this;
    }

    DeviceConfigurationService getDeviceConfigurationService() {
        return deviceConfigurationService;
    }

    public void touch() {
        if (this.getId() != 0) {
            this.getDataModel().touch(this);
        }
    }

    @Override
    protected void doDelete() {
        this.registerTypeUsages.clear();
        this.loadProfileTypeUsages.clear();
        this.logBookTypeUsages.clear();
        this.deviceMessageFiles.clear();
        this.allowedCalendars.clear();
        this.removeCustomProperties();
        Iterator<ServerDeviceConfiguration> iterator = this.getNonObsoleteConfigurations().iterator();
        // do not replace with foreach!! the deviceConfiguration will be removed from the iterator
        while (iterator.hasNext()) {
            ServerDeviceConfiguration deviceConfiguration = iterator.next();
            deviceConfiguration.notifyDelete();
            deviceConfiguration.prepareDelete();
            deviceConfiguration.makeObsolete();
            //iterator.remove();
        }
        this.deleteTimeOfUseManagementOption();

        getEventService().postEvent(EventType.DEVICE_TYPE_PRE_DELETE.topic(), this);

        this.getDataMapper().remove(this);
    }

    private void removeCustomProperties() {
        this.getCustomPropertySets().stream().filter(cps -> cps.getCustomPropertySet().getDomainClass().equals(DeviceType.class))
                .collect(Collectors.toList())
                .forEach(this::removeCustomPropertiesFor);
    }

    private void removeCustomPropertiesFor(RegisteredCustomPropertySet customPropertySet) {
        this.removeCustomPropertiesFor(customPropertySet.getCustomPropertySet());
    }

    @SuppressWarnings("unchecked")
    private void removeCustomPropertiesFor(CustomPropertySet customPropertySet) {
        this.customPropertySetService.removeValuesFor(customPropertySet, this);
    }


    private void deleteTimeOfUseManagementOption() {
        this.getDataModel()
                .mapper(TimeOfUseOptions.class)
                .find(TimeOfUseOptionsImpl.Fields.DEVICETYPE.fieldName(), this)
                .forEach(TimeOfUseOptions::delete);
    }

    @Override
    protected CreateEventType createEventType() {
        return CreateEventType.DEVICETYPE;
    }

    @Override
    protected UpdateEventType updateEventType() {
        return UpdateEventType.DEVICETYPE;
    }

    @Override
    protected DeleteEventType deleteEventType() {
        return DeleteEventType.DEVICETYPE;
    }

    @Override
    protected void validateDelete() {
        if (this.hasActiveConfigurations()) {
            throw CannotDeleteBecauseStillInUseException.deviceTypeIsStillInUse(this.getThesaurus(), this, MessageSeeds.DEVICE_TYPE_STILL_HAS_ACTIVE_CONFIGURATIONS);
        }
        this.getEventService().postEvent(EventType.DEVICETYPE_VALIDATE_DELETE.topic(), this);
    }

    private boolean hasActiveConfigurations() {
        for (DeviceConfiguration configuration : this.getConfigurations()) {
            if (configuration.isActive()) {
                return true;
            }
        }
        return false;
    }

    @Override
    @XmlAttribute
    public String getName() {
        return name;
    }

    @Override
    public void setSharedSchedule(String newSharedSchedule) {
        this.deviceSharedSchedule = newSharedSchedule;
    }

    @Override
    public String getSharedSchedule() {
        return deviceSharedSchedule;
    }

    @Override
    protected void doSetName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    @Override
    public void setDescription(String newDescription) {
        this.description = newDescription;
    }

    @Override
    public void setDeviceIcon(InputStream inputStream) {
        removeDeviceIcon();
        DeviceIcon icon = this.getDataModel().getInstance(DeviceIcon.class).initialize(this, inputStream);
        icon.save();
        this.touch();
    }

    @Override
    public void removeDeviceIcon() {
        if(this.deviceIcon.isPresent()) {
            deviceIcon.get().delete();
            this.deviceIcon = Reference.empty();
        }
        this.touch();
    }

    @Override
    @JsonIgnore
    @XmlTransient
    public DeviceLifeCycle getDeviceLifeCycle() {
        return this.deviceLifeCycle
                .effective(this.clock.instant())
                .map(DeviceLifeCycleInDeviceType::getDeviceLifeCycle)
                .get(); // Required attribute so there should in fact always be an effective value
    }

    @Override
    public Optional<DeviceLifeCycle> getDeviceLifeCycle(Instant when) {
        return this.deviceLifeCycle
                .effective(when)
                .map(DeviceLifeCycleInDeviceType::getDeviceLifeCycle);
    }

    @Override
    public void updateDeviceLifeCycle(DeviceLifeCycle deviceLifeCycle) {
        Instant now = this.clock.instant();
        this.closeCurrentDeviceLifeCycle(now);
        this.setDeviceLifeCycle(deviceLifeCycle, now);
        this.touch();
    }

    @Override
    public List<DeviceSecurityAccessorType> getDeviceSecurityAccessorType() {
        return securityAccessorTypes.stream()
                .map(SecurityAccessorTypeOnDeviceTypeImpl::getDeviceSecurityAccessorType)
                .collect(Collectors.toList());
    }

    public Optional<SecurityAccessorType> getWrappingSecurityAccessorType(SecurityAccessorType securityAccessorType){
        List<Optional<SecurityAccessorType>> collect = getDeviceSecurityAccessorType().stream().filter(f -> f.getSecurityAccessor().equals(securityAccessorType)).map(f -> f.getWrappingSecurityAccessor()).collect(Collectors.toList());
        if (collect.size() == 1) {
            return collect.get(0);
        }
        if (collect.size() == 0) {
            throw new SecurityAccessorTypeCanNotBeFoundException(getThesaurus(), securityAccessorType.getName());
        }
        else {
            throw new SecurityAccessorTypeMultipleFoundException(getThesaurus(), securityAccessorType.getName());
        }
    }

    @Override
    public boolean addDeviceSecurityAccessorType(DeviceSecurityAccessorType... securityAccessorTypesToAdd) {
        Set<DeviceSecurityAccessorType> toAdd = Arrays.stream(securityAccessorTypesToAdd).collect(Collectors.toSet());
        securityAccessorTypes.stream()
                .map(SecurityAccessorTypeOnDeviceTypeImpl::getDeviceSecurityAccessorType)
                .forEach(toAdd::remove);
        if (toAdd.isEmpty()) {
            return false;
        }
        Set<SecurityAccessorTypeOnDeviceTypeImpl> newLinks = toAdd.stream()
                .map(securityAccessorType -> getDataModel()
                        .getInstance(SecurityAccessorTypeOnDeviceTypeImpl.class)
                        .init(this, securityAccessorType))
                .peek(securityAccessorType -> Save.CREATE.validate(getDataModel(), securityAccessorType))
                .collect(Collectors.toSet());
        securityAccessorTypes.addAll(newLinks);
        return true;
    }

    @Override
    public void setWrappingSecurityAccessor(DeviceSecurityAccessorType toUpdateDeviceSecurityAccessorType, Optional<SecurityAccessorType> wrappingSecurityAccessor){
        // We do not treat if we find many but that should not be the case
        Optional<SecurityAccessorTypeOnDeviceTypeImpl> first = securityAccessorTypes.stream().filter(f -> f.getDeviceSecurityAccessorType().equals(toUpdateDeviceSecurityAccessorType)).findFirst();
        if (!first.isPresent()) {
            throw new SecurityAccessorTypeCanNotBeFoundException(getThesaurus(), toUpdateDeviceSecurityAccessorType.getSecurityAccessor().getName());
        }
        first.get().setWrappingSecurityAccessor(wrappingSecurityAccessor);
    }

    @Override
    public Optional<SecurityAccessorTypeOnDeviceType> getSecurityAccessor(SecurityAccessorType keyAccessorType) {
        for (SecurityAccessorTypeOnDeviceTypeImpl f : securityAccessorTypes) {
            if (f.getSecurityAccessorType().getId() == keyAccessorType.getId()) {
                return Optional.of(f);
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean removeDeviceSecurityAccessorType(DeviceSecurityAccessorType securityAccessorType) {
        Stream<SecurityAccessorType> securityAccessorTypeStream = securityAccessorTypes.stream()
                .map(f -> f.getDeviceSecurityAccessorType().getWrappingSecurityAccessor())
                .filter(f -> f.isPresent())
                .map(f -> f.get())
                .filter(f -> f.getId() == securityAccessorType.getSecurityAccessor().getId());
        long inUseAsWrapper = securityAccessorTypeStream
                .count();
        if (inUseAsWrapper > 0) {
            throw new SecurityAccessorTypeWrapperInUseException(getThesaurus(), securityAccessorTypeStream.findAny().get().getName());
        }

        SecurityAccessorTypeOnDeviceType toBeRemoved = null;
        for (SecurityAccessorTypeOnDeviceType securityAccessorTypeOnDeviceType: securityAccessorTypes) {
            if (securityAccessorTypeOnDeviceType.getDeviceSecurityAccessorType().equals(securityAccessorType)) {
                toBeRemoved = securityAccessorTypeOnDeviceType;
                break;
            }
        }

        if (toBeRemoved != null) {
            validateSecurityAccessorTypeRemoval(toBeRemoved);
            securityAccessorTypes.remove(toBeRemoved);
        }

        return toBeRemoved != null;
    }

    /**
     * Only used for JSON serializing
     */
    @XmlElement(name = "type")
    public String getXmlType() {
        return this.getClass().getName();
    }

    public void setXmlType(String ignore) {
        // For xml unmarshalling purposes only
    }

    private void validateSecurityAccessorTypeRemoval(SecurityAccessorTypeOnDeviceType securityAccessorTypeOnDeviceType) {
        try(QueryStream<ConfigurationSecurityProperty> confSecurityPropStream = getDataModel().stream(ConfigurationSecurityProperty.class)){
            if (confSecurityPropStream
                    .join(SecurityPropertySet.class)
                    .join(DeviceConfiguration.class)
                    .filter(Where.where("securityPropertySet.deviceConfiguration.deviceType").isEqualTo(this))
                    .filter(Where.where("securityPropertySet.deviceConfiguration.active").isEqualTo(true))
                    .filter(Where.where("keyAccessorType").isEqualTo(securityAccessorTypeOnDeviceType.getSecurityAccessorType()))
                    .findAny()
                    .isPresent()) {
                throw new SecurityAccessorTypeCanNotBeDeletedException(getThesaurus());
            }
        }
        getEventService().postEvent(EventType.SECURITY_ACCESSOR_TYPE_VALIDATE_DELETE.topic(), securityAccessorTypeOnDeviceType);
    }

    @Override
    public Optional<String> getDefaultKeyOfSecurityAccessorType(SecurityAccessorType securityAccessorType) {
        Optional<SecurityAccessorTypeOnDeviceTypeImpl> accessorTypeImpl = securityAccessorTypes.stream()
          .filter(securityAccessorTypeOnDeviceType ->
              securityAccessorTypeOnDeviceType.getSecurityAccessorType().getId() == securityAccessorType.getId()).findAny();
        if (accessorTypeImpl.isPresent()) {
            Optional<String> keyValue = accessorTypeImpl.get().getDefaultKey();
            if (keyValue.isPresent()) {
                byte[] key = Base64.getDecoder().decode(keyValue.get());
                return Optional.of(DatatypeConverter.printHexBinary(key));
            }
        }
        return Optional.ofNullable(null);
    }

    @Override
    public void updateDefaultKeyOfSecurityAccessorType(SecurityAccessorType securityAccessorType, String value) {
         if (securityAccessorType.keyTypeIsHSM()) {
             Optional<SecurityAccessorTypeOnDeviceTypeImpl> accessorTypeImpl = securityAccessorTypes.stream()
                    .filter(securityAccessorTypeOnDeviceType ->
                            securityAccessorTypeOnDeviceType.getSecurityAccessorType().equals(securityAccessorType))
                    .findAny();
                byte[] key = DatatypeConverter.parseHexBinary(value);
                if (accessorTypeImpl.isPresent()) {
                    accessorTypeImpl.get().setDefaultKey(Base64.getEncoder().encodeToString(key));
                } else {
                    throw new SecurityAccessorTypeIsNotFoundException(securityAccessorType, this.getThesaurus());
                }
         } else {
             throw new SecurityAccessorTypeIsNotHSMException(securityAccessorType, this.getThesaurus());
         }
    }

    @Override
    public DeviceConfigConflictMappingImpl newConflictMappingFor(DeviceConfiguration origin, DeviceConfiguration destination) {
        DeviceConfigConflictMappingImpl deviceConfigConflictMapping = getDataModel().getInstance(DeviceConfigConflictMappingImpl.class)
                .initialize(this, origin, destination);
        this.deviceConfigConflictMappings.add(deviceConfigConflictMapping);
        return deviceConfigConflictMapping;
    }

    @Override
    public List<RegisteredCustomPropertySet> getCustomPropertySets() {
        return deviceTypeCustomPropertySetUsages
                .stream()
                .map(DeviceTypeCustomPropertySetUsageImpl::getRegisteredCustomPropertySet)
                .filter(registeredCustomPropertySet -> customPropertySetService.findActiveCustomPropertySets().contains(registeredCustomPropertySet))
                .collect(Collectors.toList());
    }

    @Override
    public void addCustomPropertySet(RegisteredCustomPropertySet registeredCustomPropertySet) {
        DeviceTypeCustomPropertySetUsageImpl deviceTypeCustomPropertySetUsage = getDataModel().getInstance(DeviceTypeCustomPropertySetUsageImpl.class)
                .initialize(this, registeredCustomPropertySet);
        this.deviceTypeCustomPropertySetUsages.add(deviceTypeCustomPropertySetUsage);
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @Override
    public void removeCustomPropertySet(RegisteredCustomPropertySet registeredCustomPropertySet) {
        Optional<DeviceTypeCustomPropertySetUsageImpl> deviceTypeCustomPropertySetUsage = this.deviceTypeCustomPropertySetUsages
                .stream()
                .filter(f -> f.getDeviceType().getId() == this.getId())
                .filter(f -> f.getRegisteredCustomPropertySet().getId() == registeredCustomPropertySet.getId())
                .findAny();
        if (deviceTypeCustomPropertySetUsage.isPresent()) {
            deviceTypeCustomPropertySetUsages.remove(deviceTypeCustomPropertySetUsage.get());
        }
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @Override
    public void removeDeviceConfigConflictMappings(List<DeviceConfigConflictMapping> deviceConfigConflictMappings) {
        this.deviceConfigConflictMappings.removeAll(deviceConfigConflictMappings);
    }

    @Override
    public void removeConflictsFor(PartialConnectionTask partialConnectionTask) {
        this.deviceConfigConflictMappings.stream()
                .filter(deviceConfigConflictMapping -> deviceConfigConflictMapping.getDestinationDeviceConfiguration()
                        .getId() == partialConnectionTask.getConfiguration()
                        .getId() || deviceConfigConflictMapping.getOriginDeviceConfiguration()
                        .getId() == partialConnectionTask.getConfiguration().getId())
                .forEach(deviceConfigConflictMapping -> {
                    List<ConflictingConnectionMethodSolution> conflictsWithGivenConnectionTask = deviceConfigConflictMapping
                            .getConflictingConnectionMethodSolutions()
                            .stream()
                            .filter(conflictingConnectionMethodSolution -> conflictingConnectionMethodSolution.getOriginDataSource()
                                    .getId() == partialConnectionTask.getId()
                                    || (conflictingConnectionMethodSolution.getConflictingMappingAction()
                                    .equals(DeviceConfigConflictMapping.ConflictingMappingAction.MAP) && conflictingConnectionMethodSolution
                                    .getDestinationDataSource()
                                    .getId() == partialConnectionTask.getId()))
                            .collect(Collectors.toList());
                    conflictsWithGivenConnectionTask.stream()
                            .forEach(deviceConfigConflictMapping::removeConnectionMethodSolution);
                });
    }

    private void closeCurrentDeviceLifeCycle(Instant now) {
        DeviceLifeCycleInDeviceType deviceLifeCycleInDeviceType = this.deviceLifeCycle.effective(now).get();
        deviceLifeCycleInDeviceType.close(now);
        this.getDataModel().update(deviceLifeCycleInDeviceType);
    }

    private void setDeviceLifeCycle(DeviceLifeCycle deviceLifeCycle, Instant effective) {
        Interval effectivityInterval = Interval.of(Range.atLeast(effective));
        if (deviceLifeCycle != null) {
            this.deviceLifeCycle.add(
                    this.getDataModel()
                            .getInstance(DeviceLifeCycleInDeviceTypeImpl.class)
                            .initialize(effectivityInterval, this, deviceLifeCycle));
        }
    }

    @Override
    public List<DeviceLifeCycleChangeEvent> getDeviceLifeCycleChangeEvents() {
        return this.deviceLifeCycle
                .effective(Range.atLeast(Instant.EPOCH))
                .stream()
                .map(DeviceLifeCycleChangeEventImpl::new)
                .collect(Collectors.toList());
    }

    @Override
    public DeviceUsageType getDeviceUsageType() {
        if (this.deviceUsageType == null) {
            this.deviceUsageType = PersistentDeviceUsageType.fromDb(this.deviceUsageTypeId).toActualType();
        }
        return this.deviceUsageType;
    }

    @Override
    public void setDeviceUsageType(DeviceUsageType deviceUsageType) {
        this.deviceUsageType = deviceUsageType;
        this.deviceUsageTypeId = PersistentDeviceUsageType.fromActual(deviceUsageType).getCode();
    }

    @Override
    public boolean canActAsGateway() {
        if (!getDeviceProtocolPluggableClass().isPresent() || getDeviceProtocolPluggableClass().get().getDeviceProtocol() == null) {
            return false;
        }
        List<DeviceProtocolCapabilities> deviceProtocolCapabilities = getDeviceProtocolPluggableClass()
                .get().getDeviceProtocol().getDeviceProtocolCapabilities();
        return deviceProtocolCapabilities.contains(DeviceProtocolCapabilities.PROTOCOL_MASTER);
    }

    @Override
    public boolean isDirectlyAddressable() {
        if (!getDeviceProtocolPluggableClass().isPresent() || getDeviceProtocolPluggableClass().get().getDeviceProtocol() == null) {
            return false;
        }
        List<DeviceProtocolCapabilities> deviceProtocolCapabilities = getDeviceProtocolPluggableClass()
                .get().getDeviceProtocol().getDeviceProtocolCapabilities();
        return deviceProtocolCapabilities.contains(DeviceProtocolCapabilities.PROTOCOL_SESSION);
    }

    @Override
    public boolean isDataloggerSlave() {
        return deviceTypePurpose.equals(DeviceTypePurpose.DATALOGGER_SLAVE);
    }

    @Override
    public boolean isMultiElementSlave() {
        return deviceTypePurpose.equals(DeviceTypePurpose.MULTI_ELEMENT_SLAVE);
    }

    @Override
    public void setDeviceTypePurpose(DeviceTypePurpose deviceTypePurpose) {
        if (!this.deviceTypePurpose.equals(deviceTypePurpose)) {
            deviceTypePurposeChanged = true;
            getLogBookTypeBehavior().purposeChangedTo(deviceTypePurpose);
        }
        this.deviceTypePurpose = deviceTypePurpose;
    }

    @Override
    public long getVersion() {
        return this.version;
    }

    @Override
    public List<DeviceConfigConflictMapping> getDeviceConfigConflictMappings() {
        return deviceConfigConflictMappings.stream()
                .map(deviceConfigConflictMapping -> ((DeviceConfigConflictMapping) deviceConfigConflictMapping))
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public Optional<DeviceProtocolPluggableClass> getDeviceProtocolPluggableClass() {
        return getProtocolBehavior().getDeviceProtocolPluggableClass();
    }

    @Override
    public void setDeviceProtocolPluggableClass(String deviceProtocolPluggableClassName) {
        getProtocolBehavior().setDeviceProtocolPluggableClass(deviceProtocolPluggableClassName);
    }

    @Override
    public void setDeviceProtocolPluggableClass(DeviceProtocolPluggableClass deviceProtocolPluggableClass) {
        getProtocolBehavior().setDeviceProtocolPluggableClass(deviceProtocolPluggableClass);
    }

    boolean deviceProtocolPluggableClassChanged() {
        return deviceProtocolPluggableClassChanged;
    }

    boolean isDeviceTypePurposeChanged() {
        return deviceTypePurposeChanged;
    }

    public List<RegisterSpec> getRegisterSpecs() {
        List<RegisterSpec> registerSpecs = new ArrayList<>();
        for (DeviceConfiguration deviceConfiguration : this.getNonObsoleteConfigurations()) {
            registerSpecs.addAll(deviceConfiguration.getRegisterSpecs());
        }
        return registerSpecs;
    }

    @Override
    public List<LogBookType> getLogBookTypes() {
        return getLogBookTypeBehavior().getLogBookTypes();
    }

    @Override
    public List<RegisterType> getRegisterTypes() {
        return this.registerTypeUsages
                .stream()
                .map(DeviceTypeRegisterTypeUsage::getRegisterType)
                .collect(Collectors.toList());
    }

    @Override
    public List<AllowedCalendar> getAllowedCalendars() {
        return Collections.unmodifiableList(
                this.allowedCalendars.stream()
                        .filter(allowedCalendar -> !allowedCalendar.isObsolete())
                        .collect(Collectors.toList()));
    }

    @Override
    public AllowedCalendar addCalendar(Calendar calendar) {
        if (!calendar.getCategory().getName().equals(OutOfTheBoxCategory.TOU.name())) {
            throw new TimeOfUseCalendarOnly(this.getThesaurus());
        }
        Optional<AllowedCalendar> existingAllowedCalendar = this.allowedCalendars.stream()
                .filter(allowedCalendar -> !allowedCalendar.isGhost())
                .filter(allowedCalendar -> allowedCalendar.getCalendar().get().getId() == calendar.getId())
                .findFirst();
        if(existingAllowedCalendar.isPresent()) {
            existingAllowedCalendar.get().setObsolete(null);
            return existingAllowedCalendar.get();
        }
        return this.addCalendar(this.getDataModel().getInstance(AllowedCalendarImpl.class).initialize(calendar, this));
    }

    @Override
    public AllowedCalendar addGhostCalendar(String name) {
        return this.addCalendar(this.getDataModel().getInstance(AllowedCalendarImpl.class).initialize(name, this));
    }

    private AllowedCalendar addCalendar(AllowedCalendar calendar) {
        this.allowedCalendars.add(calendar);
        this.touch();
        return calendar;
    }

    @Override
    public void removeCalendar(AllowedCalendar allowedCalendar) {
        this.getEventService().postEvent(EventType.ALLOWED_CALENDAR_VALIDATE_DELETE.topic(), allowedCalendar);
        this.allowedCalendars.remove(allowedCalendar);
        save();
    }

    @Override
    public List<LoadProfileType> getLoadProfileTypes() {
        return this.loadProfileTypeUsages
                .stream()
                .map(DeviceTypeLoadProfileTypeUsage::getLoadProfileType)
                .collect(Collectors.toList());
    }

    @Override
    public void update() {
        super.save();
        this.deviceProtocolPluggableClassChanged = false;
    }

    @Override
    public List<SecurityAccessorType> getSecurityAccessorTypes() {
        List<DeviceSecurityAccessorType> deviceSecurityAccessorType = getDeviceSecurityAccessorType();
        return deviceSecurityAccessorType.stream().map(DeviceSecurityAccessorType::getSecurityAccessor).collect(Collectors.toList());
    }

    private void addSingleLoadProfileType(LoadProfileType loadProfileType) {
        for (DeviceTypeLoadProfileTypeUsage loadProfileTypeUsage : this.loadProfileTypeUsages) {
            if (loadProfileTypeUsage.sameLoadProfileType(loadProfileType)) {
                throw new LoadProfileTypeAlreadyInDeviceTypeException(this, loadProfileType, this.getThesaurus());
            }
        }
        DeviceTypeLoadProfileTypeUsage loadProfileTypeOnDeviceTypeUsage = getDataModel().getInstance(DeviceTypeLoadProfileTypeUsage.class)
                .initialize(this, loadProfileType);
        Save.UPDATE.validate(getDataModel(), loadProfileTypeOnDeviceTypeUsage);
        this.loadProfileTypeUsages.add(loadProfileTypeOnDeviceTypeUsage);
    }

    @Override
    public void addLoadProfileType(LoadProfileType loadProfileType) {
        addSingleLoadProfileType(loadProfileType);
        if (getId() > 0) {
            getDataModel().touch(this);
        }
    }

    private void addLoadProfileTypes(List<LoadProfileType> loadProfileTypes) {
        loadProfileTypes.stream().forEach(this::addSingleLoadProfileType);
    }

    @Override
    public void addLoadProfileTypeCustomPropertySet(LoadProfileType loadProfileType, RegisteredCustomPropertySet registeredCustomPropertySet) {
        DeviceTypeLoadProfileTypeUsage deviceTypeLoadProfileTypeUsage = this.loadProfileTypeUsages.stream()
                .filter(f -> f.sameLoadProfileType(loadProfileType))
                .findAny()
                .get();
        deviceTypeLoadProfileTypeUsage.setCustomPropertySet(registeredCustomPropertySet);
    }

    @Override
    public Optional<RegisteredCustomPropertySet> getLoadProfileTypeCustomPropertySet(LoadProfileType loadProfileType) {
        return this.loadProfileTypeUsages.stream().filter(f -> f.sameLoadProfileType(loadProfileType))
                .map(DeviceTypeLoadProfileTypeUsage::getRegisteredCustomPropertySet)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(registeredCustomPropertySet -> customPropertySetService.findActiveCustomPropertySets().contains(registeredCustomPropertySet))
                .findAny();
    }

    @Override
    public void removeLoadProfileType(LoadProfileType loadProfileType) {
        Iterator<DeviceTypeLoadProfileTypeUsage> loadProfileTypeUsageIterator = this.loadProfileTypeUsages.iterator();
        while (loadProfileTypeUsageIterator.hasNext()) {
            DeviceTypeLoadProfileTypeUsage loadProfileTypeUsage = loadProfileTypeUsageIterator.next();
            if (loadProfileTypeUsage.sameLoadProfileType(loadProfileType)) {
                this.validateLoadProfileTypeNotUsedByLoadProfileSpec(loadProfileType);
                loadProfileTypeUsageIterator.remove();
                getDataModel().touch(this);
            }
        }
    }

    private void validateLoadProfileTypeNotUsedByLoadProfileSpec(LoadProfileType loadProfileType) {
        List<LoadProfileSpec> loadProfileSpecs = this.getLoadProfileSpecsForLoadProfileType(loadProfileType);
        if (!loadProfileSpecs.isEmpty()) {
            throw CannotDeleteBecauseStillInUseException.loadProfileTypeIsStillInUseByLoadProfileSpec(loadProfileType, loadProfileSpecs, this
                    .getThesaurus(), MessageSeeds.LOAD_PROFILE_TYPE_STILL_IN_USE_BY_LOAD_PROFILE_SPECS);
        }
    }

    /**
     * Finds all {@link LoadProfileSpec}s from all {@link DeviceConfiguration}s
     * that are using the specified {@link LoadProfileType}.
     *
     * @param loadProfileType The LoadProfileType
     * @return The List of LoadProfileSpec
     */
    private List<LoadProfileSpec> getLoadProfileSpecsForLoadProfileType(LoadProfileType loadProfileType) {
        List<LoadProfileSpec> loadProfileSpecs = new ArrayList<>(0);
        this.collectLoadProfileSpecsForLoadProfileType(loadProfileType, loadProfileSpecs);
        return loadProfileSpecs;
    }

    private void collectLoadProfileSpecsForLoadProfileType(LoadProfileType loadProfileType, List<LoadProfileSpec> loadProfileSpecs) {
        for (DeviceConfiguration deviceConfiguration : this.getConfigurations()) {
            this.collectLoadProfileSpecsForLoadProfileType(loadProfileType, deviceConfiguration, loadProfileSpecs);
        }
    }

    private void collectLoadProfileSpecsForLoadProfileType(LoadProfileType loadProfileType, DeviceConfiguration deviceConfiguration, List<LoadProfileSpec> loadProfileSpecs) {
        loadProfileSpecs.addAll(
                deviceConfiguration
                        .getLoadProfileSpecs()
                        .stream()
                        .filter(loadProfileSpec -> loadProfileSpec.getLoadProfileType()
                                .getId() == loadProfileType.getId())
                        .collect(Collectors.toList()));
    }


    @Override
    public void addLogBookType(LogBookType logBookType) {
        getLogBookTypeBehavior().addLogBookType(logBookType);
    }

    private void addLogBookTypes(List<LogBookType> logBookTypes) {
        getLogBookTypeBehavior().addLogBookTypes(logBookTypes);
    }

    private void addSingleRegisterType(RegisterType registerType) {
        for (DeviceTypeRegisterTypeUsage registerTypeUsage : this.registerTypeUsages) {
            if (registerTypeUsage.sameRegisterType(registerType)) {
                throw new RegisterTypeAlreadyInDeviceTypeException(this, registerType, this.getThesaurus());
            }
        }
        DeviceTypeRegisterTypeUsage registerTypeOnDeviceTypeUsage = getDataModel().getInstance(DeviceTypeRegisterTypeUsage.class)
                .initialize(this, registerType);
        Save.UPDATE.validate(getDataModel(), registerTypeOnDeviceTypeUsage);
        this.registerTypeUsages.add(registerTypeOnDeviceTypeUsage);
    }

    @Override
    public void addRegisterType(RegisterType registerType) {
        addSingleRegisterType(registerType);
        if (getId() > 0) {
            getDataModel().touch(this);
        }
    }

    private void addRegisterTypes(List<RegisterType> registerTypes) {
        registerTypes.stream().forEach(this::addSingleRegisterType);
    }

    public void addRegisterTypeCustomPropertySet(RegisterType registerType, RegisteredCustomPropertySet registeredCustomPropertySet) {
        DeviceTypeRegisterTypeUsage registerTypeOnDeviceTypeUsage = this.registerTypeUsages.stream()
                .filter(f -> f.sameRegisterType(registerType))
                .findAny()
                .get();
        registerTypeOnDeviceTypeUsage.setCustomPropertySet(registeredCustomPropertySet);
    }

    public Optional<RegisteredCustomPropertySet> getRegisterTypeTypeCustomPropertySet(RegisterType registerType) {
        return this.registerTypeUsages.stream().filter(f -> f.sameRegisterType(registerType))
                .map(DeviceTypeRegisterTypeUsage::getRegisteredCustomPropertySet)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(registeredCustomPropertySet -> customPropertySetService.findActiveCustomPropertySets().contains(registeredCustomPropertySet))
                .findAny();
    }

    @Override
    public void removeRegisterType(RegisterType registerType) {
        Iterator<DeviceTypeRegisterTypeUsage> iterator = this.registerTypeUsages.iterator();
        while (iterator.hasNext()) {
            DeviceTypeRegisterTypeUsage registerTypeUsage = iterator.next();
            if (registerTypeUsage.sameRegisterType(registerType)) {
                this.validateRegisterTypeNotUsedByRegisterSpec(registerType);
                this.validateRegisterTypeNotUsedByChannelSpec(registerType);
                iterator.remove();
                getDataModel().touch(this);
                break;
            }
        }
    }

    private void validateRegisterTypeNotUsedByChannelSpec(MeasurementType measurementType) {
        List<ChannelSpec> channelSpecs = this.getChannelSpecsForChannelType(measurementType);
        if (!channelSpecs.isEmpty()) {
            throw CannotDeleteBecauseStillInUseException.channelTypeIsStillInUseByChannelSpecs(this.getThesaurus(), measurementType, channelSpecs, MessageSeeds.CHANNEL_TYPE_STILL_USED_BY_CHANNEL_SPEC);
        }
    }

    private List<ChannelSpec> getChannelSpecsForChannelType(MeasurementType measurementType) {
        List<ChannelSpec> channelSpecs = new ArrayList<>();
        this.collectChannelSpecsForChannelType(measurementType, channelSpecs);
        return channelSpecs;
    }

    private void collectChannelSpecsForChannelType(MeasurementType measurementType, List<ChannelSpec> channelSpecs) {
        for (DeviceConfiguration deviceConfiguration : this.getConfigurations()) {
            this.collectChannelSpecsForChannelType(measurementType, deviceConfiguration, channelSpecs);
        }
    }

    private void collectChannelSpecsForChannelType(MeasurementType measurementType, DeviceConfiguration deviceConfiguration, List<ChannelSpec> channelSpecs) {
        channelSpecs.addAll(
                deviceConfiguration.
                        getChannelSpecs()
                        .stream()
                        .filter(channelSpec -> channelSpec.getChannelType().getId() == measurementType.getId())
                        .collect(Collectors.toList()));
    }

    private void validateRegisterTypeNotUsedByRegisterSpec(MeasurementType measurementType) {
        List<RegisterSpec> registerSpecs = this.getRegisterSpecsForRegisterType(measurementType);
        if (!registerSpecs.isEmpty()) {
            throw CannotDeleteBecauseStillInUseException.registerTypeIsStillInUseByRegisterSpecs(this.getThesaurus(), measurementType, registerSpecs, MessageSeeds.REGISTER_TYPE_STILL_USED_BY_REGISTER_SPEC);
        }
    }

    private List<RegisterSpec> getRegisterSpecsForRegisterType(MeasurementType measurementType) {
        List<RegisterSpec> registerSpecs = new ArrayList<>();
        this.collectRegisterSpecsForRegisterType(measurementType, registerSpecs);
        return registerSpecs;
    }

    private void collectRegisterSpecsForRegisterType(MeasurementType measurementType, List<RegisterSpec> registerSpecs) {
        for (DeviceConfiguration deviceConfiguration : this.getConfigurations()) {
            this.collectRegisterSpecsForRegisterType(measurementType, deviceConfiguration, registerSpecs);
        }
    }

    private void collectRegisterSpecsForRegisterType(MeasurementType measurementType, DeviceConfiguration deviceConfiguration, List<RegisterSpec> registerSpecs) {
        registerSpecs.addAll(
                deviceConfiguration
                        .getRegisterSpecs()
                        .stream()
                        .filter(registerSpec -> registerSpec.getRegisterType().getId() == measurementType.getId())
                        .collect(Collectors.toList()));
    }

    @Override
    public void removeLogBookType(LogBookType logBookType) {
        getLogBookTypeBehavior().removeLogBookType(logBookType);
    }

    public boolean supportsMessaging() {
        return this.getDeviceProtocolPluggableClass().isPresent();
    }

    public boolean isLogicalSlave() {
        if (getProtocolBehavior() != null) {
            logicalSlave = getProtocolBehavior().isLogicalSlave();
        }
        return logicalSlave;
    }

    @Override
    public List<DeviceConfiguration> getConfigurations() {
        return ImmutableList.copyOf(getNonObsoleteConfigurations());
    }

    @Override
    public List<DeviceConfiguration> getConfigurationsWithObsolete() {
        return ImmutableList.copyOf(getAllDeviceConfigurations());
    }

    private List<ServerDeviceConfiguration> getNonObsoleteConfigurations(){
        return this.deviceConfigurations
                .stream()
                .filter(dc -> !dc.isObsolete())
                .collect(Collectors.toList());
    }

    /**
     * Method to get DeviceConfigurations irrespective of OBSOLETE_DATE value
     * from DTC_DEVICECONFIG table Fix for CXO-12489
     * @return
     */
    private List<ServerDeviceConfiguration> getAllDeviceConfigurations(){
        return this.deviceConfigurations
                .stream()
                .collect(Collectors.toList());
    }

    private void addConfiguration(ServerDeviceConfiguration deviceConfiguration) {
        this.deviceConfigurations.add(deviceConfiguration);
    }

    @Override
    public void removeConfiguration(DeviceConfiguration deviceConfigurationToDelete) {
        Iterator<ServerDeviceConfiguration> iterator = this.getNonObsoleteConfigurations().iterator();
        while (iterator.hasNext()) {
            ServerDeviceConfiguration configuration = iterator.next();
            if (configuration.getId() == deviceConfigurationToDelete.getId()) {
                configuration.notifyDelete();
                configuration.prepareDelete();
                configuration.makeObsolete();
                //iterator.remove();
                getDataModel().touch(this);
                break;
            }
        }
    }

    @Override
    public DeviceConfigurationBuilder newConfiguration(String name) {
        return new ConfigurationBuilder(this.getDataModel()
                .getInstance(DeviceConfigurationImpl.class)
                .initialize(this, name));
    }

    @Override
    public boolean isFileManagementEnabled() {
        return this.fileManagementEnabled;
    }

    @Override
    public void enableFileManagement() {
        if (!this.fileManagementEnabled && getDeviceProtocolPluggableClass().isPresent() && getDeviceProtocolPluggableClass().get().supportsFileManagement()) {
            this.fileManagementEnabled = true;
            this.update();
        }
    }

    @Override
    public void disableFileManagement() {
        if (this.fileManagementEnabled) {
            this.fileManagementEnabled = false;
            this.fileManagementDisabled();
            this.update();
        }
    }

    @Override
    public byte[] getDeviceIcon() {
        if(deviceIcon.isPresent()) {
            return deviceIcon.get().getBlob();
        } else {
            return new byte[0];
        }
    }

    private void fileManagementDisabled() {
        Instant now = this.clock.instant();
        this.deviceMessageFiles
            .stream()
            .filter(deviceMessageFile -> !deviceMessageFile.isObsolete())
            .forEach(deviceMessageFile -> {
                deviceMessageFile.setObsolete(now);
                this.getEventService().postEvent(EventType.DEVICE_MESSAGE_FILE_OBSOLETE.topic(), deviceMessageFile);
            });
        this.touch();
        this.getNonObsoleteConfigurations().forEach(ServerDeviceConfiguration::fileManagementDisabled);
    }

    @Override
    public List<DeviceMessageFile> getDeviceMessageFiles() {
        return this.deviceMessageFiles
            .stream()
            .filter(deviceMessageFile -> !deviceMessageFile.isObsolete())
            .collect(Collectors.toList());
    }

    @Override
    public DeviceMessageFile addDeviceMessageFile(Path path) {
        DeviceMessageFileImpl file = this.getDataModel().getInstance(DeviceMessageFileImpl.class).init(this, path);
        return createFile(file);
    }

    @Override
    public DeviceMessageFile addDeviceMessageFile(InputStream inputStream, String fileName) {
        ServerDeviceMessageFile file = this.getDataModel().getInstance(DeviceMessageFileImpl.class).init(this, inputStream, fileName);
        return createFile(file);
    }

    private DeviceMessageFile createFile(ServerDeviceMessageFile file) {
        if (this.deviceMessageFiles.stream().anyMatch(other -> other.getName().equals(file.getName()) && !other.isObsolete())) {
            throw new DuplicateDeviceMessageFileException(this, file.getName(), this.getThesaurus());
        }
        Save.CREATE.validate(this.getDataModel(), file);
        this.deviceMessageFiles.add(file);
        this.touch();
        return file;
    }

    @Override
    public void removeDeviceMessageFile(DeviceMessageFile obsolete) {
        this.removeDeviceMessageFile((ServerDeviceMessageFile) obsolete);
    }

    private void removeDeviceMessageFile(ServerDeviceMessageFile obsolete) {
        if (this.deviceMessageFiles.contains(obsolete)) {
            Instant now = this.clock.instant();
            this.deviceMessageFiles
                    .stream()
                    .filter(deviceMessageFile -> deviceMessageFile.equals(obsolete))
                    .findFirst()
                    .ifPresent(deviceMessageFile -> deviceMessageFile.setObsolete(now));
            this.touch();
            this.getEventService().postEvent(EventType.DEVICE_MESSAGE_FILE_OBSOLETE.topic(), obsolete);
        }
    }

    private enum BuildingMode {
        UNDERCONSTRUCTION {
            @Override
            protected void verify() {
                // All calls are fine as long as we are under construction
            }
        },
        COMPLETE {
            @Override
            protected void verify() {
                throw new IllegalStateException("The device configuration building process is already complete");
            }
        };

        protected abstract void verify();
    }

    private interface NestedBuilder {
        void add();
    }

    private class ChannelSpecBuilder implements NestedBuilder {

        private final ChannelSpec.ChannelSpecBuilder builder;

        private ChannelSpecBuilder(ChannelSpec.ChannelSpecBuilder builder) {
            super();
            this.builder = builder;
        }

        @Override
        public void add() {
            this.builder.add();
        }
    }

    private class NumericalRegisterSpecBuilder implements NestedBuilder {

        private final NumericalRegisterSpec.Builder builder;

        private NumericalRegisterSpecBuilder(NumericalRegisterSpec.Builder builder) {
            super();
            this.builder = builder;
        }

        @Override
        public void add() {
            this.builder.add();
        }
    }

    private class TextualRegisterSpecBuilder implements NestedBuilder {

        private final TextualRegisterSpec.Builder builder;

        private TextualRegisterSpecBuilder(TextualRegisterSpec.Builder builder) {
            super();
            this.builder = builder;
        }

        @Override
        public void add() {
            this.builder.add();
        }
    }

    private class LoadProfileSpecBuilder implements NestedBuilder {

        private final LoadProfileSpec.LoadProfileSpecBuilder builder;

        private LoadProfileSpecBuilder(LoadProfileSpec.LoadProfileSpecBuilder builder) {
            super();
            this.builder = builder;
        }

        @Override
        public void add() {
            this.builder.add();
        }
    }

    private class LogBookSpecBuilder implements NestedBuilder {

        private final LogBookSpec.LogBookSpecBuilder builder;

        private LogBookSpecBuilder(LogBookSpec.LogBookSpecBuilder builder) {
            super();
            this.builder = builder;
        }

        @Override
        public void add() {
            this.builder.add();
        }
    }

    private ProtocolBehavior getProtocolBehavior() {
        return isDataloggerSlave() || isMultiElementSlave() ? new LackingProtocolBehavior() : new RegularProtocolBehavior();
    }

    interface ProtocolBehavior {
        void setDeviceProtocolPluggableClass(DeviceProtocolPluggableClass deviceProtocolPluggableClass);

        void setDeviceProtocolPluggableClass(String deviceProtocolPluggableClassName);

        Optional<DeviceProtocolPluggableClass> getDeviceProtocolPluggableClass();

        Optional<DeviceProtocol> getDeviceProtocol();

        boolean isLogicalSlave();
    }

    private class RegularProtocolBehavior implements ProtocolBehavior {

        @Override
        public void setDeviceProtocolPluggableClass(DeviceProtocolPluggableClass deviceProtocolPluggableClass) {
            // Test for null because javax.validation only kicks @ save time
            if (deviceProtocolPluggableClass != null) {
                DeviceTypeImpl.this.deviceProtocolPluggableClassChanged = (DeviceTypeImpl.this.deviceProtocolPluggableClassId != deviceProtocolPluggableClass
                        .getId());
                DeviceTypeImpl.this.deviceProtocolPluggableClassId = deviceProtocolPluggableClass.getId();
                DeviceTypeImpl.this.deviceProtocolPluggableClass = deviceProtocolPluggableClass;
            } else {
                DeviceTypeImpl.this.deviceProtocolPluggableClassChanged = (DeviceTypeImpl.this.deviceProtocolPluggableClassId != 0);
                DeviceTypeImpl.this.deviceProtocolPluggableClassId = 0;
                DeviceTypeImpl.this.deviceProtocolPluggableClass = null;
            }
        }

        @Override
        public void setDeviceProtocolPluggableClass(String deviceProtocolPluggableClassName) {
            this.setDeviceProtocolPluggableClass(DeviceTypeImpl.this.protocolPluggableService.findDeviceProtocolPluggableClassByName(deviceProtocolPluggableClassName)
                    .orElse(null));
        }

        @Override
        public Optional<DeviceProtocolPluggableClass> getDeviceProtocolPluggableClass() {
            if (DeviceTypeImpl.this.deviceProtocolPluggableClass == null && !isDataloggerSlave() && !isMultiElementSlave()) {
                Optional<DeviceProtocolPluggableClass> optionalDeviceProtocolPluggableClass = this.findDeviceProtocolPluggableClass(DeviceTypeImpl.this.deviceProtocolPluggableClassId);
                optionalDeviceProtocolPluggableClass.ifPresent(consumer -> DeviceTypeImpl.this.deviceProtocolPluggableClass = consumer);
            }
            return Optional.ofNullable(DeviceTypeImpl.this.deviceProtocolPluggableClass);
        }

        @Override
        public Optional<DeviceProtocol> getDeviceProtocol() {
            Optional<DeviceProtocolPluggableClass> deviceProtocolPluggableClass = getDeviceProtocolPluggableClass();
            if (DeviceTypeImpl.this.localDeviceProtocol == null && deviceProtocolPluggableClass.isPresent()) {
                DeviceTypeImpl.this.localDeviceProtocol = deviceProtocolPluggableClass.get().getDeviceProtocol();
            }
            return Optional.ofNullable(DeviceTypeImpl.this.localDeviceProtocol);
        }

        @Override
        public boolean isLogicalSlave() {
            if (getDeviceProtocol().isPresent()) {
                List<DeviceProtocolCapabilities> deviceProtocolCapabilities = this.getDeviceProtocol().get()
                        .getDeviceProtocolCapabilities();
                return deviceProtocolCapabilities.contains(DeviceProtocolCapabilities.PROTOCOL_SLAVE) && deviceProtocolCapabilities
                        .size() == 1;
            } else {
                return false;
            }
        }

        private Optional<DeviceProtocolPluggableClass> findDeviceProtocolPluggableClass(long deviceProtocolPluggableClassId) {
            if (DeviceTypeImpl.this.protocolPluggableService != null) {
                return DeviceTypeImpl.this.protocolPluggableService.findDeviceProtocolPluggableClass(deviceProtocolPluggableClassId);
            }
            return Optional.empty();
        }
    }
    // Specific behaviour for device types lacking a protocol = no communication  (data logger slaves, multi-element submeter)
    private class LackingProtocolBehavior implements ProtocolBehavior {

        @Override
        public void setDeviceProtocolPluggableClass(DeviceProtocolPluggableClass deviceProtocolPluggableClass) {
            throw DataloggerSlaveException.deviceProtocolPluggableClassIsNoSupported(getThesaurus(), DeviceTypeImpl.this);
        }

        @Override
        public void setDeviceProtocolPluggableClass(String deviceProtocolPluggableClassName) {
            throw DataloggerSlaveException.deviceProtocolPluggableClassIsNoSupported(getThesaurus(), DeviceTypeImpl.this);
        }

        @Override
        public Optional<DeviceProtocolPluggableClass> getDeviceProtocolPluggableClass() {
            return Optional.empty();
        }

        @Override
        public Optional<DeviceProtocol> getDeviceProtocol() {
            return Optional.empty();
        }

        @Override
        public boolean isLogicalSlave() {
            return false;
        }
    }

    private LogBookBehavior getLogBookTypeBehavior() {
        return isDataloggerSlave() || isMultiElementSlave() ? new LackingLogBookBehavior() : new RegularLogBookBehavior();
    }

    /**
     * Models different behavior for logbooks on the different 'types' of a DeviceType
     */
    interface LogBookBehavior {
        void addLogBookTypes(List<LogBookType> logBookTypes);

        void addLogBookType(LogBookType logBookType);

        List<LogBookType> getLogBookTypes();

        void removeLogBookType(LogBookType logBookType);

        void purposeChangedTo(DeviceTypePurpose deviceTypePurpose);
    }

    private class RegularLogBookBehavior implements LogBookBehavior {
        @Override
        public void addLogBookTypes(List<LogBookType> logBookTypes) {
            logBookTypes.forEach(this::addSingleLogBookType);
        }

        @Override
        public void addLogBookType(LogBookType logBookType) {
            addSingleLogBookType(logBookType);
            if (getId() > 0) {
                getDataModel().touch(DeviceTypeImpl.this);
            }
        }

        @Override
        public List<LogBookType> getLogBookTypes() {
            return DeviceTypeImpl.this.logBookTypeUsages
                    .stream()
                    .map(DeviceTypeLogBookTypeUsage::getLogBookType)
                    .collect(Collectors.toList());
        }

        @Override
        public void removeLogBookType(LogBookType logBookType) {
            Iterator<DeviceTypeLogBookTypeUsage> logBookTypeUsageIterator = DeviceTypeImpl.this.logBookTypeUsages.iterator();
            while (logBookTypeUsageIterator.hasNext()) {
                DeviceTypeLogBookTypeUsage logBookTypeUsage = logBookTypeUsageIterator.next();
                if (logBookTypeUsage.sameLogBookType(logBookType)) {
                    this.validateLogBookTypeNotUsedByLogBookSpec(logBookType);
                    logBookTypeUsageIterator.remove();
                    getDataModel().touch(DeviceTypeImpl.this);
                    break;
                }
            }
        }

        @Override
        public void purposeChangedTo(DeviceTypePurpose deviceTypePurpose) {
            if (!deviceTypePurpose.equals(DeviceTypePurpose.REGULAR)) {
                getLogBookTypes().stream().forEach(logBookType -> {
                    List<LogBookSpec> logBookSpecs = this.getLogBookSpecsForLogBookType(logBookType);
                    if (!logBookSpecs.isEmpty()) {
                        throw DataloggerSlaveException.cannotChangeLogBookTypeWhenConfigsExistWithLogBookSpecs(DeviceTypeImpl.this
                                .getThesaurus(), DeviceTypeImpl.this);
                    }
                });
                logBookTypeUsages.clear();
            }
        }

        private void validateLogBookTypeNotUsedByLogBookSpec(LogBookType logBookType) {
            List<LogBookSpec> logBookSpecs = this.getLogBookSpecsForLogBookType(logBookType);
            if (!logBookSpecs.isEmpty()) {
                throw CannotDeleteBecauseStillInUseException.logBookTypeIsStillInUseByLogBookSpec(DeviceTypeImpl.this.getThesaurus(), logBookType, logBookSpecs, MessageSeeds.LOG_BOOK_TYPE_STILL_IN_USE_BY_LOG_BOOK_SPECS);
            }
        }

        /**
         * Finds all {@link LogBookSpec}s from all {@link DeviceConfiguration}s
         * that are using the specified {@link LogBookType}.
         *
         * @param logBookType The LogBookType
         * @return The List of LogBookSpec
         */
        private List<LogBookSpec> getLogBookSpecsForLogBookType(LogBookType logBookType) {
            List<LogBookSpec> logBookSpecs = new ArrayList<>(0);
            this.collectLogBookSpecsForLogBookType(logBookType, logBookSpecs);
            return logBookSpecs;
        }

        private void collectLogBookSpecsForLogBookType(LogBookType logBookType, List<LogBookSpec> logBookSpecs) {
            for (DeviceConfiguration deviceConfiguration : DeviceTypeImpl.this.getConfigurations()) {
                this.collectLogBookSpecsForLogBookType(logBookType, deviceConfiguration, logBookSpecs);
            }
        }

        private void collectLogBookSpecsForLogBookType(LogBookType logBookType, DeviceConfiguration deviceConfiguration, List<LogBookSpec> logBookSpecs) {
            logBookSpecs.addAll(
                    deviceConfiguration
                            .getLogBookSpecs()
                            .stream()
                            .filter(logBookSpec -> logBookSpec.getLogBookType().getId() == logBookType.getId())
                            .collect(Collectors.toList()));
        }

        private void addSingleLogBookType(LogBookType logBookType) {
            for (DeviceTypeLogBookTypeUsage logBookTypeUsage : DeviceTypeImpl.this.logBookTypeUsages) {
                if (logBookTypeUsage.sameLogBookType(logBookType)) {
                    throw new LogBookTypeAlreadyInDeviceTypeException(DeviceTypeImpl.this, logBookType, DeviceTypeImpl.this
                            .getThesaurus());
                }
            }
            DeviceTypeImpl.this.logBookTypeUsages.add(new DeviceTypeLogBookTypeUsage(DeviceTypeImpl.this, logBookType));
        }
    }
    // Specific behaviour for device types lacking logbooks (data logger slaves, multi-element submeter)
    private class LackingLogBookBehavior implements LogBookBehavior {
        @Override
        public void addLogBookTypes(List<LogBookType> logBookTypes) {
            if (!logBookTypes.isEmpty()) {
                throw DataloggerSlaveException.logbookTypesAreNotSupported(getThesaurus(), DeviceTypeImpl.this);
            }
        }

        @Override
        public void addLogBookType(LogBookType logBookType) {
            throw DataloggerSlaveException.logbookTypesAreNotSupported(getThesaurus(), DeviceTypeImpl.this);
        }

        @Override
        public List<LogBookType> getLogBookTypes() {
            return Collections.emptyList();
        }

        @Override
        public void removeLogBookType(LogBookType logBookType) {
            throw DataloggerSlaveException.logbookTypesAreNotSupported(getThesaurus(), DeviceTypeImpl.this);
        }

        @Override
        public void purposeChangedTo(DeviceTypePurpose deviceTypePurpose) {
            // nothing to do
        }
    }

    static class DeviceTypeBuilderImpl implements DeviceTypeBuilder {

        private final DeviceTypeImpl underConstruction;

        DeviceTypeBuilderImpl(DeviceTypeImpl underConstruction, String name, DeviceProtocolPluggableClass deviceProtocolPluggableClass, DeviceLifeCycle deviceLifeCycle, DeviceTypePurpose purpose, String deviceSharedSchedule) {
            this.underConstruction = underConstruction;
            switch (purpose){
                case REGULAR:
                    this.underConstruction.initializeRegular(name, deviceProtocolPluggableClass, deviceLifeCycle, deviceSharedSchedule);
                    break;
                case DATALOGGER_SLAVE:
                    this.underConstruction.initializeDataloggerSlave(name, deviceLifeCycle);
                    break;
                case MULTI_ELEMENT_SLAVE:
                    this.underConstruction.initializeMultiElementSlave(name, deviceLifeCycle);
                    break;
            }
        }

        @Override
        public DeviceTypeBuilder withRegisterTypes(List<RegisterType> registerTypes) {
            underConstruction.addRegisterTypes(registerTypes);
            return this;
        }

        @Override
        public DeviceTypeBuilder withLoadProfileTypes(List<LoadProfileType> loadProfileTypes) {
            underConstruction.addLoadProfileTypes(loadProfileTypes);
            return this;
        }

        @Override
        public DeviceTypeBuilder withLogBookTypes(List<LogBookType> logBookTypes) {
            if (!logBookTypes.isEmpty()) {
                underConstruction.addLogBookTypes(logBookTypes);
            }
            return this;
        }

        @Override
        public DeviceTypeBuilder setDescription(String description) {
            underConstruction.setDescription(description);
            return this;
        }

        @Override
        public DeviceTypeBuilder setSharedSchedule(String deviceSharedSchedule) {
            underConstruction.setSharedSchedule(deviceSharedSchedule);
            return this;
        }


        @Override
        public DeviceTypeBuilder enableFileManagement() {
            underConstruction.enableFileManagement();
            return this;
        }

        @Override
        public DeviceType create() {
            underConstruction.save();
            return underConstruction;
        }
    }

    private class ConfigurationBuilder implements DeviceConfigurationBuilder {

        private BuildingMode mode;
        private final DeviceConfigurationImpl underConstruction;
        private final List<NestedBuilder> nestedBuilders = new ArrayList<>();

        private ConfigurationBuilder(DeviceConfigurationImpl underConstruction) {
            super();
            this.mode = BuildingMode.UNDERCONSTRUCTION;
            this.underConstruction = underConstruction;
        }

        @Override
        public DeviceConfigurationBuilder description(String description) {
            underConstruction.setDescription(description);
            return this;
        }

        @Override
        public DeviceConfigurationBuilder isDirectlyAddressable(boolean isDirectlyAddressable) {
            underConstruction.setDirectlyAddressable(isDirectlyAddressable);
            return this;
        }

        @Override
        public DeviceConfigurationBuilder validateOnStore(boolean validateOnStore) {
            underConstruction.setValidateOnStore(validateOnStore);
            return this;
        }

        @Override
        public DeviceConfigurationBuilder gatewayType(GatewayType gatewayType) {
            if (gatewayType != null && !GatewayType.NONE.equals(gatewayType)) {
                canActAsGateway(true);
            }
            underConstruction.setGatewayType(gatewayType);
            return this;
        }

        @Override
        public DeviceConfigurationBuilder dataloggerEnabled(boolean dataloggerEnabled) {
            underConstruction.setDataloggerEnabled(dataloggerEnabled);
            return this;
        }

        @Override
        public DeviceConfigurationBuilder multiElementEnabled(boolean multiElementEnabled) {
            underConstruction.setMultiElementEnabled(multiElementEnabled);
            return this;
        }

        @Override
        public DeviceConfigurationBuilder canActAsGateway(boolean canActAsGateway) {
            underConstruction.setCanActAsGateway(canActAsGateway);
            return this;
        }

        @Override
        public ChannelSpec.ChannelSpecBuilder newChannelSpec(ChannelType channelType, LoadProfileSpec loadProfileSpec) {
            ChannelSpec.ChannelSpecBuilder builder = this.underConstruction.createChannelSpec(channelType, loadProfileSpec);
            this.nestedBuilders.add(new ChannelSpecBuilder(builder));
            return builder;
        }

        @Override
        public ChannelSpec.ChannelSpecBuilder newChannelSpec(ChannelType channelType, LoadProfileSpec.LoadProfileSpecBuilder loadProfileSpecBuilder) {
            ChannelSpec.ChannelSpecBuilder builder = this.underConstruction.createChannelSpec(channelType, loadProfileSpecBuilder);
            this.nestedBuilders.add(new ChannelSpecBuilder(builder));
            return builder;
        }

        @Override
        public NumericalRegisterSpec.Builder newNumericalRegisterSpec(RegisterType registerType) {
            NumericalRegisterSpec.Builder builder = this.underConstruction.createNumericalRegisterSpec(registerType);
            this.nestedBuilders.add(new NumericalRegisterSpecBuilder(builder));
            return builder;
        }

        @Override
        public TextualRegisterSpec.Builder newTextualRegisterSpec(RegisterType registerType) {
            TextualRegisterSpec.Builder builder = this.underConstruction.createTextualRegisterSpec(registerType);
            this.nestedBuilders.add(new TextualRegisterSpecBuilder(builder));
            return builder;
        }

        @Override
        public LoadProfileSpec.LoadProfileSpecBuilder newLoadProfileSpec(LoadProfileType loadProfileType) {
            LoadProfileSpec.LoadProfileSpecBuilder builder = this.underConstruction.createLoadProfileSpec(loadProfileType);
            this.nestedBuilders.add(new LoadProfileSpecBuilder(builder));
            return builder;
        }

        @Override
        public LogBookSpec.LogBookSpecBuilder newLogBookSpec(LogBookType logBookType) {
            LogBookSpec.LogBookSpecBuilder builder = this.underConstruction.createLogBookSpec(logBookType);
            this.nestedBuilders.add(new LogBookSpecBuilder(builder));
            return builder;
        }

        @Override
        public DeviceConfiguration add() {
            this.mode.verify();
            this.doNestedBuilders();
            Save.CREATE.validate(getDataModel(), this.underConstruction);
            addConfiguration(this.underConstruction);
            this.mode = BuildingMode.COMPLETE;
            createDefaultMessageEnablements();
            return this.underConstruction;
        }

        private void createDefaultMessageEnablements() {
            ((DeviceTypeImpl) this.underConstruction.getDeviceType()).getProtocolBehavior()
                    .getDeviceProtocolPluggableClass()
                    .ifPresent(deviceProtocolPluggableClass -> deviceProtocolPluggableClass
                            .getDeviceProtocol().getSupportedMessages().stream().forEach(
                                    deviceMessageSpec -> {
                                        DeviceMessageEnablementBuilder deviceMessageEnablement = underConstruction.createDeviceMessageEnablement(DeviceMessageId.from(deviceMessageSpec.getId()));
                                        deviceMessageEnablement.addUserAction(DeviceMessageUserAction.EXECUTEDEVICEMESSAGE1);
                                        deviceMessageEnablement.addUserAction(DeviceMessageUserAction.EXECUTEDEVICEMESSAGE2);
                                        deviceMessageEnablement.addUserAction(DeviceMessageUserAction.EXECUTEDEVICEMESSAGE3);
                                        deviceMessageEnablement.build();
                                    }));
        }

        private void doNestedBuilders() {
            this.nestedBuilders.forEach(DeviceTypeImpl.NestedBuilder::add);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DeviceTypeImpl that = (DeviceTypeImpl) o;

        return id == that.id;

    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    //***** Deprecated methods, we should delete these one day ...

    @Override
    public void save() {
        update();
    }

    @Override
    public List<SecurityAccessorTypeOnDeviceType> getSecurityAccessors() {
        return securityAccessorTypes
                .stream()
                .collect(Collectors.toList());
    }

}
