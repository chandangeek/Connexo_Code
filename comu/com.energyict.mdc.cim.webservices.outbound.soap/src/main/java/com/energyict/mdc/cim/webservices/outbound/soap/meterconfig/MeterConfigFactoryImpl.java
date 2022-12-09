/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.cim.webservices.outbound.soap.meterconfig;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.DefaultState;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.cim.webservices.outbound.soap.MeterConfigFactory;
import com.energyict.mdc.cim.webservices.outbound.soap.PingResult;
import com.energyict.mdc.cim.webservices.outbound.soap.impl.DeviceSchedulesInfo;
import com.energyict.mdc.cim.webservices.outbound.soap.impl.TranslationInstaller;
import com.energyict.mdc.cim.webservices.outbound.soap.impl.TranslationKeys;
import com.energyict.mdc.common.device.config.ComTaskEnablement;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.data.ActiveEffectiveCalendar;
import com.energyict.mdc.common.device.data.Batch;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.protocol.DeviceMessage;
import com.energyict.mdc.common.protocol.DeviceMessageCategory;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.common.tasks.StatusInformationTask;
import com.energyict.mdc.common.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.device.data.ActivatedBreakerStatus;
import com.energyict.mdc.device.data.DeviceMessageQueryFilterImpl;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.firmware.ActivatedFirmwareVersion;
import com.energyict.mdc.firmware.FirmwareManagementDeviceUtils;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.upl.messages.DeviceMessageAttribute;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;

import ch.iec.tc57._2011.meterconfig.CalendarStatus;
import ch.iec.tc57._2011.meterconfig.ConnectionAttributes;
import ch.iec.tc57._2011.meterconfig.ContactorStatus;
import ch.iec.tc57._2011.meterconfig.EndDeviceInfo;
import ch.iec.tc57._2011.meterconfig.FirmwareStatus;
import ch.iec.tc57._2011.meterconfig.FirmwareStatusType;
import ch.iec.tc57._2011.meterconfig.Manufacturer;
import ch.iec.tc57._2011.meterconfig.Meter;
import ch.iec.tc57._2011.meterconfig.MeterConfig;
import ch.iec.tc57._2011.meterconfig.MeterMultiplier;
import ch.iec.tc57._2011.meterconfig.MeterStatus;
import ch.iec.tc57._2011.meterconfig.Name;
import ch.iec.tc57._2011.meterconfig.ProductAssetModel;
import ch.iec.tc57._2011.meterconfig.SharedCommunicationSchedule;
import ch.iec.tc57._2011.meterconfig.SimpleEndDeviceFunction;
import ch.iec.tc57._2011.meterconfig.Status;
import ch.iec.tc57._2011.meterconfig.Zones;
import com.elster.connexo._2017.schema.customattributes.Attribute;
import com.elster.connexo._2017.schema.customattributes.CustomAttributeSet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarActivationDateAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.activityCalendarNameAttributeName;
import static com.energyict.protocolimplv2.messages.DeviceMessageConstants.contactorActivationDateAttributeName;
import static java.util.Comparator.comparing;

@Component(name = "com.energyict.mdc.cim.webservices.outbound.soap.meterconfig.MeterConfigFactory", service = MeterConfigFactory.class)
public class MeterConfigFactoryImpl implements MeterConfigFactory {

    private volatile CustomPropertySetService customPropertySetService;
    private volatile DeviceService deviceService;
    private volatile FirmwareService firmwareService;
    private volatile DeviceMessageService deviceMessageService;
    private volatile DeviceMessageSpecificationService deviceMessageSpecificationService;
    private volatile Clock clock;
    private volatile Thesaurus thesaurus;

    public MeterConfigFactoryImpl() {
        // for OSGI purposes
    }

    // for testing purposes
    @Inject
    public MeterConfigFactoryImpl(CustomPropertySetService customPropertySetService, DeviceService deviceService,
                                  FirmwareService firmwareService, DeviceMessageService deviceMessageService,
                                  DeviceMessageSpecificationService deviceMessageSpecificationService,
                                  Clock clock, Thesaurus thesaurus) {
        setCustomPropertySetService(customPropertySetService);
        setDeviceService(deviceService);
        setFirmwareService(firmwareService);
        setDeviceMessageService(deviceMessageService);
        setDeviceMessageSpecificationService(deviceMessageSpecificationService);
        setClock(clock);
        this.thesaurus = thesaurus;
    }

    @Reference
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setFirmwareService(FirmwareService firmwareService) {
        this.firmwareService = firmwareService;
    }

    @Reference
    public void setDeviceMessageService(DeviceMessageService deviceMessageService) {
        this.deviceMessageService = deviceMessageService;
    }

    @Reference
    public void setDeviceMessageSpecificationService(DeviceMessageSpecificationService deviceMessageSpecificationService) {
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setThesaurus(TranslationInstaller translationInstaller) {
        this.thesaurus = translationInstaller.getThesaurus();
    }

    @Override
    public MeterConfig asMeterConfig(Device device) {
        return asMeterConfig(Arrays.asList(device));
    }

    @Override
    public MeterConfig asGetMeterConfig(Device device, PingResult pingResult, boolean meterStatusRequired) {
        return asGetMeterConfig(Collections.singletonMap(device, pingResult), meterStatusRequired);
    }

    @Override
    public MeterConfig asMeterConfig(Collection<Device> devices) {
        Set<String> deviceConfigRefs = new HashSet<>();
        MeterConfig meterConfig = new MeterConfig();
        devices.forEach(device -> {
            Meter meter = createMeter(device);
            meterConfig.getMeter().add(meter);

            DeviceConfiguration deviceConfiguration = device.getDeviceConfiguration();
            String deviceConfigRef = Long.toString(deviceConfiguration.getId());
            if (!deviceConfigRefs.contains(deviceConfigRef)) {
                meterConfig.getSimpleEndDeviceFunction().add(createSimpleEndDeviceFunction(deviceConfigRef, deviceConfiguration.getName()));
                deviceConfigRefs.add(deviceConfigRef);
            }
            Meter.SimpleEndDeviceFunction endDeviceFunctionRef = createEndDeviceFunctionRef(deviceConfigRef);
            meter.getComFunctionOrConnectDisconnectFunctionOrSimpleEndDeviceFunction().add(endDeviceFunctionRef);
            device.getConnectionTasks().forEach(connTask -> meter.getConnectionAttributes().add(getConnectionAttribute(connTask)));
        });
        return meterConfig;
    }

    @Override
    public MeterConfig asGetMeterConfig(Map<Device, PingResult> devicesAndPingResult, boolean meterStatusRequired) {
        MeterConfig meterConfig = new MeterConfig();
        devicesAndPingResult.forEach((device, pingResult) -> {
            Meter meter = getMeter(device, meterStatusRequired, pingResult);
            meterConfig.getMeter().add(meter);
            SimpleEndDeviceFunction simpleEndDeviceFunction = getSimpleEndDeviceFunction(device, meter);
            meterConfig.getSimpleEndDeviceFunction().add(simpleEndDeviceFunction);
        });
        return meterConfig;
    }

    private Meter createMeter(Device device) {
        Meter meter = new Meter();
        meter.setMRID(device.getmRID());
        meter.setSerialNumber(device.getSerialNumber());
        meter.getNames().add(createName(device.getName()));
        device.getBatch().map(Batch::getName).ifPresent(meter::setLotNumber);
        meter.setEndDeviceInfo(createEndDeviceInfo(device));
        meter.setType(device.getDeviceConfiguration().getDeviceType().getName());
        meter.getMeterMultipliers().add(createMultiplier(device.getMultiplier()));
        meter.setStatus(createStatus(device));
        DeviceConfiguration deviceConfiguration = device.getDeviceConfiguration();
        List<ComTaskExecution> comTaskExecutions = device.getComTaskExecutions().stream()
                .filter(comTaskExecution -> !comTaskExecution.getComTask().isSystemComTask())
                .collect(Collectors.toList());
        List<ComTaskEnablement> comTaskEnablements = deviceConfiguration.getComTaskEnablements().stream()
                .filter(comTaskEnablement -> !comTaskEnablement.getComTask().isSystemComTask())
                .collect(Collectors.toList());
        List<DeviceSchedulesInfo> deviceSchedulesInfos = DeviceSchedulesInfo.from(comTaskExecutions, comTaskEnablements, device);
       List<SharedCommunicationSchedule> sharedCommunicationSchedules = meter.getSharedCommunicationSchedules();
        if (deviceSchedulesInfos.size() != 0) {
            for (DeviceSchedulesInfo deviceScheduleInfo : deviceSchedulesInfos) {
                SharedCommunicationSchedule schedule = new SharedCommunicationSchedule();
                if ((!(deviceScheduleInfo.name == null) && !deviceScheduleInfo.name.isEmpty())) {
                    if (!(sharedCommunicationSchedules.stream()
                            .anyMatch(sharedCommunicationSchedule -> deviceScheduleInfo.name.equals(sharedCommunicationSchedule
                                    .getName())))) {
                        schedule.setName(deviceScheduleInfo.name);
                        sharedCommunicationSchedules.add(schedule);
                    }
                }
            }
        }
         return meter;
    }

    private EndDeviceInfo createEndDeviceInfo(Device device) {
        EndDeviceInfo endDeviceInfo = new EndDeviceInfo();
        ProductAssetModel productAssetModel = createAssetModel(device);
        endDeviceInfo.setAssetModel(productAssetModel);
        return endDeviceInfo;
    }

    private ProductAssetModel createAssetModel(Device device) {
        ProductAssetModel productAssetModel = new ProductAssetModel();
        productAssetModel.setModelNumber(device.getModelNumber());
        productAssetModel.setModelVersion(device.getModelVersion());
        Manufacturer manufacturer = createManufacturer(device.getManufacturer());
        productAssetModel.setManufacturer(manufacturer);
        return productAssetModel;
    }

    private Manufacturer createManufacturer(String manufacturerName) {
        if (manufacturerName == null) {
            return null;
        }
        Manufacturer manufacturer = new Manufacturer();
        Name name = createName(manufacturerName);
        manufacturer.getNames().add(name);
        return manufacturer;
    }

    private SimpleEndDeviceFunction createSimpleEndDeviceFunction(String deviceConfigRef, String deviceConfigName) {
        SimpleEndDeviceFunction simpleEndDeviceFunction = new SimpleEndDeviceFunction();
        simpleEndDeviceFunction.setMRID(deviceConfigRef);
        simpleEndDeviceFunction.setConfigID(deviceConfigName);
        simpleEndDeviceFunction.setZones(new Zones());
        return simpleEndDeviceFunction;
    }

    private Meter.SimpleEndDeviceFunction createEndDeviceFunctionRef(String deviceConfigRef) {
        Meter.SimpleEndDeviceFunction simpleEndDeviceFunctionRef = new Meter.SimpleEndDeviceFunction();
        simpleEndDeviceFunctionRef.setRef(deviceConfigRef);
        return simpleEndDeviceFunctionRef;
    }

    private Name createName(String name) {
        Name nameBean = new Name();
        nameBean.setName(name);
        return nameBean;
    }

    private Status createStatus(Device device) {
        String stateKey = device.getState().getName();
        String stateName = DefaultState.fromKey(stateKey)
                .map(DefaultState::getDefaultFormat)
                .orElse(stateKey);
        Status status = new Status();
        status.setValue(stateName);
        return status;
    }

    private MeterMultiplier createMultiplier(BigDecimal multiplier) {
        MeterMultiplier meterMultiplier = new MeterMultiplier();
        meterMultiplier.setValue(multiplier.floatValue());
        return meterMultiplier;
    }

    private Meter getMeter(Device device, boolean isMeterStatusRequired, PingResult pingResult) {
        Meter meter = new Meter();
        meter.setMRID(device.getmRID());
        meter.setSerialNumber(device.getSerialNumber());
        meter.getNames().add(createName(device.getName()));
        device.getBatch().map(Batch::getName).ifPresent(meter::setLotNumber);
        meter.setEndDeviceInfo(createEndDeviceInfo(device));
        meter.setType(device.getDeviceConfiguration().getDeviceType().getName());
        meter.getMeterMultipliers().add(createMultiplier(device.getMultiplier()));
        String stateKey = device.getState().getName();
        String stateName = DefaultState.fromKey(stateKey)
                .map(DefaultState::getDefaultFormat)
                .orElse(stateKey);
        meter.setStatus(createStatus(stateName));
        if (isMeterStatusRequired) {
            meter.setMeterStatus(getMeterStatus(device));
        }
        if (pingResult != PingResult.NOT_NEEDED) {
            meter.setPingResult(pingResult.getName());
        }
        //general attributes
        List<CustomAttributeSet> generalList = new ArrayList<>();
        TypedProperties deviceProperties = device.getDeviceProtocolProperties();
        if (device.getDeviceType().getDeviceProtocolPluggableClass().isPresent()) {
            List<PropertySpec> propertySpecs = device.getDeviceType().getDeviceProtocolPluggableClass().get().getDeviceProtocol().getPropertySpecs();
            CustomAttributeSet attributeSet = new CustomAttributeSet();
            attributeSet.setId(thesaurus.getFormat(TranslationKeys.GENERAL_ATTRIBUTES).format());
            for (PropertySpec propertySpec : propertySpecs) {
                Attribute attr = new Attribute();
                attr.setName(propertySpec.getName());
                Object propertyValue = getPropertyValue(propertySpec, deviceProperties);
                attr.setValue(convertPropertyValue(propertySpec, propertyValue));
                attributeSet.getAttribute().add(attr);
            }
            generalList.add(attributeSet);
        }
        meter.getMeterCustomAttributeSet().addAll(generalList);
        //custom attributes
        List<CustomAttributeSet> customList = device.getDeviceType().getCustomPropertySets()
                .stream()
                .filter(RegisteredCustomPropertySet::isViewableByCurrentUser)
                .map(registeredCustomPropertySet -> convertToCustomAttributeSet(registeredCustomPropertySet, device))
                .collect(Collectors.toList());
        meter.getMeterCustomAttributeSet().addAll(customList);

        device.getConnectionTasks().forEach(connTask -> meter.getConnectionAttributes().add(getConnectionAttribute(connTask)));
        return meter;
    }

    private ConnectionAttributes getConnectionAttribute(ConnectionTask<?, ?> connTask) {
        TypedProperties typedProperties = connTask.getTypedProperties();
        ConnectionAttributes attr = new ConnectionAttributes();
        attr.setConnectionMethod(connTask.getName());
        for (PropertySpec propertySpec : connTask.getPluggableClass().getPropertySpecs()) {
            ch.iec.tc57._2011.meterconfig.Attribute attribute = new ch.iec.tc57._2011.meterconfig.Attribute();
            attribute.setName(propertySpec.getName());
            Object value = typedProperties.getLocalValue(propertySpec.getName());
            if (value == null) {
                value = typedProperties.getInheritedValue(propertySpec.getName());
            }
            attribute.setValue(convertPropertyValue(propertySpec, value));
            attr.getAttribute().add(attribute);
        }
        return attr;
    }

    private MeterStatus getMeterStatus(Device device) {
        MeterStatus meterStatus = new MeterStatus();
        setCommTaskValues(meterStatus, device);
        setFirmwares(meterStatus, device);
        setCalendar(meterStatus, device);
        setContactorStatus(meterStatus, device);
        return meterStatus;
    }

    private void setContactorStatus(MeterStatus meterStatus, Device device) {
        Optional<ActivatedBreakerStatus> breakerStatus = deviceService.getActiveBreakerStatus(device);
        breakerStatus.ifPresent(activatedBreakerStatus -> meterStatus.setContactorStatus(getContactorStatus(device, activatedBreakerStatus)));
    }

    private Optional<DeviceMessage> getCalendarCommand(Device device, String calendarName) {
        DeviceMessageCategory deviceMessageCategory = deviceMessageSpecificationService.allCategories().stream()
                .filter(m -> m.getName().equals("Activity calendar"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Can't find device message category 'Activity calendar'."));
        DeviceMessageQueryFilterImpl deviceMessageQueryFilter = new DeviceMessageQueryFilterImpl();
        deviceMessageQueryFilter.setDevice(device);
        deviceMessageQueryFilter.setDeviceMessagesStatuses(Collections.singletonList(DeviceMessageStatus.CONFIRMED));
        deviceMessageQueryFilter.setMessageCategories(Collections.singletonList(deviceMessageCategory));
        return deviceMessageService.findDeviceMessagesByFilter(deviceMessageQueryFilter)
                .stream()
                .filter(dm -> dm.getAttributes().stream()
                        .anyMatch(attr -> attr.getName().equals(activityCalendarNameAttributeName) && attr.getValue().equals(calendarName)))
                .filter(dm -> dm.getSentDate().isPresent())
                .max(comparing(dm -> dm.getSentDate().orElse(Instant.MIN)));
    }

    private Optional<DeviceMessage> getContactorCommand(Device device, ActivatedBreakerStatus breakerStatus) {
        DeviceMessageCategory deviceMessageCategory = deviceMessageSpecificationService.allCategories().stream()
                .filter(m -> m.getName().equals("Contactor"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Can't find device message category 'Contactor'."));
        DeviceMessageQueryFilterImpl deviceMessageQueryFilter = new DeviceMessageQueryFilterImpl();
        deviceMessageQueryFilter.setDeviceMessagesStatuses(Collections.singletonList(DeviceMessageStatus.CONFIRMED));
        deviceMessageQueryFilter.setDevice(device);
        deviceMessageQueryFilter.setMessageCategories(Collections.singletonList(deviceMessageCategory));
        return deviceMessageService.findDeviceMessagesByFilter(deviceMessageQueryFilter)
                .stream()
                .filter(dm -> dm.getSentDate().isPresent())
                .filter(dm -> !dm.getSentDate().get().isAfter(breakerStatus.getLastChecked()))
                .max(comparing(dm -> dm.getSentDate().orElse(Instant.MIN)));

    }

    private ContactorStatus getContactorStatus(Device device, ActivatedBreakerStatus breakerStatus) {
        ContactorStatus contactorStatus = new ContactorStatus();
        contactorStatus.setStatus(breakerStatus.getBreakerStatus().toString());
        Optional<DeviceMessage> command = getContactorCommand(device, breakerStatus);
        if (command.isPresent()) {
            Optional<Instant> sentDate = command.get().getSentDate();
            sentDate.ifPresent(contactorStatus::setSentDate);
            Optional<Object> activationDateAttr = command.get().getAttributes().stream()
                    .filter(attr -> attr.getName().equals(contactorActivationDateAttributeName))
                    .map(DeviceMessageAttribute::getValue)
                    .findFirst();
            activationDateAttr.ifPresent(activationDate -> contactorStatus.setActivationDate(((Date) activationDate).toInstant()));
        }
        return contactorStatus;
    }

    private void setCalendar(MeterStatus meterStatus, Device device) {
        device.calendars().getActive().ifPresent(activeEffectiveCalendar -> meterStatus.setCalendarStatus(getCalendarStatus(device, activeEffectiveCalendar)));
    }

    private CalendarStatus getCalendarStatus(Device device, ActiveEffectiveCalendar activeCalendar) {
        CalendarStatus calendarStatus = new CalendarStatus();
        if (!activeCalendar.getAllowedCalendar().isObsolete()) {
            calendarStatus.setCalendar(activeCalendar.getAllowedCalendar().getName());
            if (activeCalendar.getAllowedCalendar().getCalendar().isPresent()) {
                String calendarName = activeCalendar.getAllowedCalendar().getName();
                Optional<DeviceMessage> command = getCalendarCommand(device, calendarName);
                if (command.isPresent()) {
                    Optional<Instant> sentDate = command.get().getSentDate();
                    sentDate.ifPresent(calendarStatus::setUploadDate);
                    Optional<Object> activationDateAttr = command.get().getAttributes().stream()
                            .filter(attr -> attr.getName().equals(activityCalendarActivationDateAttributeName))
                            .map(DeviceMessageAttribute::getValue)
                            .findFirst();
                    activationDateAttr.ifPresent(activationDate -> calendarStatus.setActivationDate(((Date) activationDate).toInstant()));
                }
            }
        }
        return calendarStatus;
    }

    private void setCommTaskValues(MeterStatus meterStatus, Device device) {
        Optional<ComTaskExecution> comTaskExecution = device.getDeviceConfiguration().getComTaskEnablements().stream()
                .filter(cte -> cte.getComTask().isManualSystemTask())
                .filter(cte -> !cte.isSuspended())
                .filter(comTaskEnablement -> comTaskEnablement.getComTask().getProtocolTasks().stream()
                        .anyMatch(task -> task instanceof StatusInformationTask))
                .map(comTaskEnablement -> device.getComTaskExecutions().stream()
                        .filter(cte -> !cte.isOnHold())
                        .filter(cte -> cte.getComTask().equals(comTaskEnablement.getComTask()))
                        .findFirst().orElse(null))
                .filter(Objects::nonNull)
                .findFirst();
        if (comTaskExecution.isPresent()) {
            Optional<ComTaskExecutionSession> comTaskExecutionSession = comTaskExecution.get().getLastSession();
            if (comTaskExecutionSession.isPresent()) {
                meterStatus.setStatusInfoComTaskExecutionStatus(comTaskExecutionSession.get().getSuccessIndicator().toString());
                meterStatus.setStatusInfoComTaskExecutionDate(comTaskExecutionSession.get().getStartDate());
            }
        }
    }

    private void setFirmwares(MeterStatus meterStatus, Device device) {
        Set<FirmwareType> supportedFirmwareTypes = firmwareService.getSupportedFirmwareTypes(device.getDeviceType());
        FirmwareManagementDeviceUtils versionUtils = firmwareService.getFirmwareManagementDeviceUtilsFor(device, true);
        if (supportedFirmwareTypes.isEmpty()) {
            return;
        }
        FirmwareStatus firmwareStatus = new FirmwareStatus();
        for (FirmwareType firmwareType : supportedFirmwareTypes) {
            Optional<ActivatedFirmwareVersion> activatedFirmwareVersion = firmwareService.getActiveFirmwareVersion(device, firmwareType);
            if (activatedFirmwareVersion.isPresent()) {
                List<DeviceMessage> deviceMessageList = versionUtils.getFirmwareMessages();
                Optional<DeviceMessage> deviceMessage = deviceMessageList.stream()
                        .filter(m -> activatedFirmwareVersion.get()
                                .getFirmwareVersion().equals(versionUtils.getFirmwareVersionFromMessage(m).orElse(null)))
                        .filter(dm -> dm.getSentDate().isPresent())
                        .max(comparing(dm -> dm.getSentDate().orElse(Instant.MIN)));
                addFirmwareInfo(firmwareStatus, activatedFirmwareVersion.get(), deviceMessage.orElse(null), versionUtils);
            }
        }
        meterStatus.setFirmwareStatus(firmwareStatus);
    }

    private void addFirmwareInfo(FirmwareStatus firmwareStatus, ActivatedFirmwareVersion activatedFirmwareVersion, DeviceMessage deviceMessage, FirmwareManagementDeviceUtils versionUtils) {
        FirmwareStatusType firmwareStatusType = new FirmwareStatusType();
        firmwareStatusType.setVersion(activatedFirmwareVersion.getFirmwareVersion().getFirmwareVersion());
        firmwareStatusType.setImageIdentifier(activatedFirmwareVersion.getFirmwareVersion().getImageIdentifier());
        if (deviceMessage != null) {
            firmwareStatusType.setUploadDate(deviceMessage.getReleaseDate());
            firmwareStatusType.setActivationDate(versionUtils.getActivationDateFromMessage(deviceMessage).orElse(deviceMessage.getReleaseDate()));
        }
        switch (activatedFirmwareVersion.getFirmwareVersion().getFirmwareType()) {
            case METER:
                firmwareStatus.setMeterFirmware(firmwareStatusType);
                break;
            case COMMUNICATION:
                firmwareStatus.setCommunicationFirmware(firmwareStatusType);
                break;
            case AUXILIARY:
                firmwareStatus.setAuxiliaryFirmware(firmwareStatusType);
                break;
            case CA_CONFIG_IMAGE:
                firmwareStatus.setImage(firmwareStatusType);
                break;
        }
    }

    private Object getPropertyValue(PropertySpec propertySpec, TypedProperties deviceProperties) {
        Object domainValue = deviceProperties.getLocalValue(propertySpec.getName());
        if (domainValue == null) {
            domainValue = deviceProperties == null ? null : deviceProperties.getInheritedValue(propertySpec.getName());
            if (domainValue == null) {
                return getDefaultPropertyValue(propertySpec);
            }
        }
        return domainValue;
    }

    private CustomAttributeSet convertToCustomAttributeSet(RegisteredCustomPropertySet registeredCustomPropertySet, Device device) {
        CustomAttributeSet customAttributeSet = new CustomAttributeSet();
        CustomPropertySetValues values = null;
        CustomPropertySet propertySet = registeredCustomPropertySet.getCustomPropertySet();
        List<PropertySpec> propertySpecs = propertySet.getPropertySpecs();
        customAttributeSet.setId(propertySet.getId());
        if (!propertySet.isVersioned()) {
            values = customPropertySetService.getUniqueValuesFor(propertySet, device);
        } else {
            values = customPropertySetService.getUniqueValuesFor(propertySet, device, clock.instant());
            if (values.isEmpty()) {
                return customAttributeSet; // for versioned CAS empty values means no version
            }
        }
        for (PropertySpec propertySpec : propertySpecs) {
            Attribute attr = new Attribute();
            attr.setName(propertySpec.getName());
            if (values == null) {
                Object propertyValue = getDefaultPropertyValue(propertySpec);
                attr.setValue(convertPropertyValue(propertySpec, propertyValue));
                customAttributeSet.getAttribute().add(attr);
            } else {
                attr.setValue(convertPropertyValue(propertySpec, values.getProperty(propertySpec.getName())));
                customAttributeSet.getAttribute().add(attr);
            }
            if (propertySet.isVersioned() && values != null) {
                if (values.getEffectiveRange().hasLowerBound()) {
                    customAttributeSet.setFromDateTime(values.getEffectiveRange().lowerEndpoint());
                    customAttributeSet.setVersionId(values.getEffectiveRange().lowerEndpoint());
                } else {
                    customAttributeSet.setVersionId(device.getCreateTime());
                }
                if (values.getEffectiveRange().hasUpperBound()) {
                    customAttributeSet.setToDateTime(values.getEffectiveRange().upperEndpoint());
                }
            }
        }
        return customAttributeSet;
    }

    private Object getDefaultPropertyValue(PropertySpec propertySpec) {
        return propertySpec.getPossibleValues() == null ? null : propertySpec.getPossibleValues().getDefault();
    }

    private String convertPropertyValue(PropertySpec spec, Object value) {
        return spec.getValueFactory().toStringValue(value);
    }

    private SimpleEndDeviceFunction getSimpleEndDeviceFunction(Device device, Meter meter) {
        String deviceConfigRef = Long.toString(device.getDeviceConfiguration().getId());
        Meter.SimpleEndDeviceFunction endDeviceFunctionRef = createEndDeviceFunctionRef(deviceConfigRef);
        meter.getComFunctionOrConnectDisconnectFunctionOrSimpleEndDeviceFunction().add(endDeviceFunctionRef);
        return createSimpleEndDeviceFunction(deviceConfigRef, device.getDeviceConfiguration().getName());
    }

    private Status createStatus(String state) {
        Status status = new Status();
        status.setValue(state);
        return status;
    }

}
