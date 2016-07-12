package com.energyict.mdc.device.data.impl.ami;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.EndDeviceControlType;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ami.CommandFactory;
import com.elster.jupiter.metering.ami.CompletionOptions;
import com.elster.jupiter.metering.ami.EndDeviceCapabilities;
import com.elster.jupiter.metering.ami.EndDeviceCommand;
import com.elster.jupiter.metering.ami.HeadEndInterface;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallBuilder;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.users.User;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.ami.EndDeviceCommandFactory;
import com.energyict.mdc.device.data.ami.MultiSenseHeadEndInterface;
import com.energyict.mdc.device.data.exceptions.NoSuchElementException;
import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.CommandServiceCallDomainExtension;
import com.energyict.mdc.device.data.impl.ami.servicecall.CompletionOptionsCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.CompletionOptionsServiceCallDomainExtension;
import com.energyict.mdc.device.data.impl.ami.servicecall.OnDemandReadServiceCallCustomPropertySet;
import com.energyict.mdc.device.data.impl.ami.servicecall.OnDemandReadServiceCallDomainExtension;
import com.energyict.mdc.device.data.impl.ami.servicecall.ServiceCallCommands;
import com.energyict.mdc.device.data.impl.ami.servicecall.handlers.OnDemandReadServiceCallHandler;
import com.energyict.mdc.device.data.security.Privileges;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionBuilder;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecution;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.tasks.LoadProfilesTask;
import com.energyict.mdc.tasks.MessagesTask;
import com.energyict.mdc.tasks.RegistersTask;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component(name = "com.energyict.mdc.device.data.impl.ami.MultiSenseHeadEndInterface",
        service = {HeadEndInterface.class},
        property = "name=MultiSenseHeadEndInterface", immediate = true)
public class MultiSenseHeadEndInterfaceImpl implements MultiSenseHeadEndInterface {

    private static final String AMR_SYSTEM = KnownAmrSystem.MDC.getName();
    private static final Logger LOGGER = Logger.getLogger(MultiSenseHeadEndInterface.class.getName());

    private volatile DeviceService deviceService;
    private volatile MeteringService meteringService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile Thesaurus thesaurus;
    private volatile ServiceCallService serviceCallService;
    private volatile CustomPropertySetService customPropertySetService;
    private volatile EndDeviceCommandFactory endDeviceCommandFactory;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile Clock clock;

    //For OSGI purposes
    public MultiSenseHeadEndInterfaceImpl() {
    }

    @Inject
    public MultiSenseHeadEndInterfaceImpl(DeviceService deviceService, DeviceConfigurationService deviceConfigurationService, MeteringService meteringService, Thesaurus thesaurus, ServiceCallService serviceCallService, CustomPropertySetService customPropertySetService, EndDeviceCommandFactory endDeviceCommandFactory, ThreadPrincipalService threadPrincipalService, Clock clock) {
        this.deviceService = deviceService;
        this.meteringService = meteringService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.thesaurus = thesaurus;
        this.serviceCallService = serviceCallService;
        this.customPropertySetService = customPropertySetService;
        this.endDeviceCommandFactory = endDeviceCommandFactory;
        this.threadPrincipalService = threadPrincipalService;
        this.clock = clock;
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }


    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    public final void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(DeviceDataServices.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    public void setServiceCallService(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

    @Reference
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    @Reference
    public void setEndDeviceCommandFactory(EndDeviceCommandFactory endDeviceCommandFactory) {
        this.endDeviceCommandFactory = endDeviceCommandFactory;
    }

    @Activate
    public void activate() {
    }

    @Deactivate
    public void deactivate() {
    }

    @Override
    public Optional<URL> getURLForEndDevice(EndDevice endDevice) {
        if (!((User) threadPrincipalService.getPrincipal()).hasPrivilege(KnownAmrSystem.MDC.getName(), Privileges.Constants.VIEW_DEVICE)) {
            return Optional.empty();
        } else {
            if (endDevice.getAmrSystem().is(KnownAmrSystem.MDC)) {
                Device device = findDeviceForEndDevice(endDevice);
                Map<KnownAmrSystem, String> urls = meteringService.getSupportedApplicationsUrls();
                if (urls.containsKey(KnownAmrSystem.MDC)) {
                    String urlText = urls.get(KnownAmrSystem.MDC).trim() + "/devices/" + device.getmRID();
                    try {
                        return Optional.of(new URL(urlText));
                    } catch (MalformedURLException e) {
                        LOGGER.log(Level.SEVERE, "Unable to parse [url= " + urlText + " ]", e);
                    }
                }
            }
            return Optional.empty();
        }
    }

    @Override
    public EndDeviceCapabilities getCapabilities(EndDevice endDevice) {
        List<ReadingType> readingTypes = deviceConfigurationService.getReadingTypesRelatedToConfiguration(findDeviceForEndDevice(endDevice).getDeviceConfiguration());
        Set<DeviceMessageId> supportedMessages = findDeviceForEndDevice(endDevice).getDeviceProtocolPluggableClass().getDeviceProtocol().getSupportedMessages();
        List<EndDeviceControlType> controlTypes = Arrays.asList(EndDeviceControlTypeMapping.values()).stream()
                .filter(mapping -> mapping.getPossibleDeviceMessageIdGroups().stream().anyMatch(supportedMessages::containsAll))
                .map(this::findEndDeviceControlType)
                .collect(Collectors.toList());
        return new EndDeviceCapabilities(readingTypes, controlTypes);
    }

    public CommandFactory getCommandFactory() {
        return endDeviceCommandFactory;
    }

    @Override
    public CompletionOptions scheduleMeterRead(Meter meter, List<ReadingType> readingTypes, Instant instant) {
        return scheduleMeterRead(meter, readingTypes,instant,null);
    }

    @Override
    public CompletionOptions scheduleMeterRead(Meter meter, List<ReadingType> readingTypes, Instant instant, ServiceCall parentServiceCall) {
        Device multiSenseDevice = findDeviceForEndDevice(meter);
        Set<ComTaskExecution> comTaskExecutions = getComTaskExecutionsForReadingTypes(multiSenseDevice, readingTypes);

        ServiceCall serviceCall = getOnDemandReadServiceCall(multiSenseDevice, multiSenseDevice.getComTaskExecutions().stream().map(cte -> cte.getComTasks().size()).reduce(0, (a,b) -> a+b), Optional.ofNullable(parentServiceCall));
        serviceCall.requestTransition(DefaultState.ONGOING);

        comTaskExecutions.forEach(comTaskExecution -> this.scheduleComTaskExecution(comTaskExecution, instant));

        return new CompletionOptionsImpl(serviceCall);
    }

    @Override
    public CompletionOptions readMeter(Meter meter, List<ReadingType> readingTypes) {
        return scheduleMeterRead(meter, readingTypes, clock.instant(), null);
    }

    @Override
    public CompletionOptions readMeter(Meter meter, List<ReadingType> readingTypes, ServiceCall parentServiceCall) {
        return scheduleMeterRead(meter, readingTypes, clock.instant(), parentServiceCall);
    }

    private Set<ComTaskExecution> getComTaskExecutionsForReadingTypes(Device multiSenseDevice, List<ReadingType> readingTypes) {
        List<ComTaskExecution> comTaskExecutions = getComTaskExecutions(multiSenseDevice);
        Set<ComTaskExecution> filteredComTaskExecutions = new HashSet<>();

        for (ReadingType readingType : readingTypes) {
            if (readingType.isRegular()) {
                comTaskExecutions.stream()
                        .filter(comTaskExecution -> comTaskExecution.getProtocolTasks()
                                .stream()
                                .anyMatch(protocolTask -> protocolTask instanceof LoadProfilesTask))
                        .forEach(filteredComTaskExecutions::add);
            } else {
                comTaskExecutions.stream()
                        .filter(comTaskExecution -> comTaskExecution.getProtocolTasks()
                                .stream()
                                .anyMatch(protocolTask -> protocolTask instanceof RegistersTask))
                        .forEach(filteredComTaskExecutions::add);
            }
        }
        return filteredComTaskExecutions;
    }

    private List<ComTaskExecution> getComTaskExecutions(Device device) {

        List<ComTaskExecution> comTaskExecutions = new ArrayList<>();

        for (ComTaskEnablement comTaskEnablement : device.getDeviceConfiguration().getComTaskEnablements()) {

            Optional<ComTaskExecution> existingComTaskExecution = device.getComTaskExecutions().stream()
                    .filter(cte -> cte.getComTasks()
                            .stream()
                            .anyMatch(comTask -> comTask.getId() == comTaskEnablement.getComTask().getId()))
                    .findFirst();
            comTaskExecutions.add(existingComTaskExecution.orElseGet(() -> createAdHocComTaskExecution(device, comTaskEnablement)));
        }

        return comTaskExecutions;
    }

    private void scheduleComTaskExecution(ComTaskExecution comTaskExecution, Instant instant) {
        comTaskExecution.addNewComTaskExecutionTrigger(instant);
        comTaskExecution.updateNextExecutionTimestamp();
    }


    private ServiceCall getOnDemandReadServiceCall(Device device, int estimatedTasks, Optional<ServiceCall> parentServiceCall) {
        CompletionOptionsServiceCallDomainExtension completionOptionsServiceCallDomainExtension = new CompletionOptionsServiceCallDomainExtension();
        OnDemandReadServiceCallDomainExtension onDemandReadServiceCallDomainExtension = new OnDemandReadServiceCallDomainExtension();
        onDemandReadServiceCallDomainExtension.setExpectedTasks(new BigDecimal(estimatedTasks));

        ServiceCallType serviceCallType = getOnDemandReadServiceCallType();

        ServiceCallBuilder serviceCallBuilder = parentServiceCall.isPresent() ? parentServiceCall.get()
                .newChildCall(serviceCallType) : serviceCallType.newServiceCall();

        ServiceCall serviceCall = serviceCallBuilder
                .extendedWith(onDemandReadServiceCallDomainExtension)
                .extendedWith(completionOptionsServiceCallDomainExtension)
                .targetObject(device)
                .create();
        serviceCall.requestTransition(DefaultState.PENDING);
        return serviceCall;
    }

    private ServiceCallType getOnDemandReadServiceCallType() {
        RegisteredCustomPropertySet customPropertySet = customPropertySetService.findActiveCustomPropertySets(ServiceCall.class)
                .stream()
                .filter(cps -> cps.getCustomPropertySet()
                        .getId()
                        .equals(OnDemandReadServiceCallDomainExtension.class.getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(MessageFormat.format("Could not find custom property set ''{0}''", OnDemandReadServiceCallCustomPropertySet.class
                        .getSimpleName())));
        RegisteredCustomPropertySet completionOptionsCustomPropertySet = customPropertySetService.findActiveCustomPropertySets(ServiceCall.class)
                .stream()
                .filter(cps -> cps.getCustomPropertySet()
                        .getName()
                        .equals(CompletionOptionsCustomPropertySet.class.getSimpleName()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(MessageFormat.format("Could not find custom property set ''{0}''", CompletionOptionsCustomPropertySet.class
                        .getSimpleName())));


        return serviceCallService.findServiceCallType(OnDemandReadServiceCallHandler.SERVICE_CALL_HANDLER_NAME, OnDemandReadServiceCallHandler.VERSION)
                .orElseGet(() -> serviceCallService.createServiceCallType(OnDemandReadServiceCallHandler.SERVICE_CALL_HANDLER_NAME, OnDemandReadServiceCallHandler.VERSION)
                        .handler(OnDemandReadServiceCallHandler.SERVICE_CALL_HANDLER_NAME)
                        .customPropertySet(customPropertySet)
                        .customPropertySet(completionOptionsCustomPropertySet)
                        .create());
    }

    @Override
    public CompletionOptions sendCommand(EndDeviceCommand endDeviceCommand, Instant releaseDate) {
        return sendCommand(endDeviceCommand, releaseDate, null);
    }

    @Override
    public CompletionOptions sendCommand(EndDeviceCommand endDeviceCommand, Instant releaseDate, ServiceCall parentServiceCall) {
        Device multiSenseDevice = findDeviceForEndDevice(endDeviceCommand.getEndDevice());
        ServiceCall serviceCall = getServiceCallCommands().createOperationServiceCall(Optional.ofNullable(parentServiceCall), multiSenseDevice, endDeviceCommand.getEndDeviceControlType(), releaseDate);
        serviceCall.requestTransition(DefaultState.PENDING);
        serviceCall.requestTransition(DefaultState.ONGOING);
        serviceCall.log(LogLevel.INFO, "Handling command " + endDeviceCommand.getEndDeviceControlType());

        try {
            List<DeviceMessage<Device>> deviceMessages = ((EndDeviceCommandImpl) endDeviceCommand).createCorrespondingMultiSenseDeviceMessages(serviceCall, releaseDate);
            scheduleDeviceCommandsComTaskEnablement(findDeviceForEndDevice(endDeviceCommand.getEndDevice()), deviceMessages);  // Intentionally reload the device here
            updateCommandServiceCallDomainExtension(serviceCall, deviceMessages);
            serviceCall.log(LogLevel.INFO, MessageFormat.format("Scheduled {0} device command(s).", deviceMessages.size()));
            serviceCall.requestTransition(DefaultState.WAITING);
            return new CompletionOptionsImpl(serviceCall);
        } catch (RuntimeException e) {
            serviceCall.log("Encountered an exception when trying to create/schedule the device command(s)", e);
            serviceCall.requestTransition(DefaultState.FAILED);
            throw e;
        }
    }

    private void scheduleDeviceCommandsComTaskEnablement(Device device, List<DeviceMessage<Device>> deviceMessages) {
        List<DeviceMessageId> deviceMessageIds = new ArrayList<>();
        deviceMessages.forEach(msg -> deviceMessageIds.add(msg.getDeviceMessageId()));
        getComTaskEnablementsForDeviceMessages(device, deviceMessageIds).forEach(comTaskEnablement -> {
            Optional<ComTaskExecution> existingComTaskExecution = device.getComTaskExecutions().stream()
                    .filter(cte -> cte.getComTasks().stream().anyMatch(comTask -> comTask.getId() == comTaskEnablement.getComTask().getId()))
                    .findFirst();
            ComTaskExecution comTaskExecution = existingComTaskExecution.orElseGet(() -> createAdHocComTaskExecution(device, comTaskEnablement));
            deviceMessages.stream()
                    .map(DeviceMessage::getReleaseDate)
                    .distinct()
                    .forEach(comTaskExecution::addNewComTaskExecutionTrigger);
            comTaskExecution.updateNextExecutionTimestamp();
        });
    }

    private ManuallyScheduledComTaskExecution createAdHocComTaskExecution(Device device, ComTaskEnablement comTaskEnablement) {
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        if (comTaskEnablement.hasPartialConnectionTask()) {
            device.getConnectionTasks().stream()
                    .filter(connectionTask -> connectionTask.getPartialConnectionTask().getId() == comTaskEnablement.getPartialConnectionTask().get().getId())
                    .findFirst()
                    .ifPresent(comTaskExecutionBuilder::connectionTask);
        }
        ManuallyScheduledComTaskExecution manuallyScheduledComTaskExecution = comTaskExecutionBuilder.add();
        device.save();
        return manuallyScheduledComTaskExecution;
    }

    private Stream<ComTaskEnablement> getComTaskEnablementsForDeviceMessages(Device device, List<DeviceMessageId> deviceMessageIds) {
        List<ComTaskEnablement> comTaskEnablements = new ArrayList<>();
        deviceMessageIds.stream()
                .forEach(deviceMessageId -> comTaskEnablements.add(device.getDeviceConfiguration()
                        .getComTaskEnablements()
                        .stream()
                        .filter(cte -> cte.getComTask().getProtocolTasks().stream().
                                filter(task -> task instanceof MessagesTask).
                                flatMap(task -> ((MessagesTask) task).getDeviceMessageCategories().stream()).
                                flatMap(category -> category.getMessageSpecifications().stream()).
                                filter(dms -> dms.getId().equals(deviceMessageId)).
                                findFirst().
                                isPresent())
                        .findAny()
                        .orElseThrow(() -> new IllegalStateException(thesaurus.getFormat(MessageSeeds.NO_COMTASK_FOR_COMMAND).format()))));
        return comTaskEnablements.stream().distinct();
    }

    private void updateCommandServiceCallDomainExtension(ServiceCall serviceCall, List<DeviceMessage<Device>> deviceMessages) {
        CommandServiceCallDomainExtension domainExtension = serviceCall.getExtensionFor(new CommandCustomPropertySet()).get();
        domainExtension.setDeviceMessages(deviceMessages);
        domainExtension.setNrOfUnconfirmedDeviceCommands(deviceMessages.size());
        serviceCall.update(domainExtension);
    }

    protected ServiceCallCommands getServiceCallCommands() {
        return new ServiceCallCommands(serviceCallService, thesaurus);
    }

    private Device findDeviceForEndDevice(EndDevice endDevice) {
        return deviceService.findByUniqueMrid(endDevice.getMRID()).orElseThrow(NoSuchElementException.deviceWithMRIDNotFound(thesaurus, endDevice.getMRID()));
    }

    private EndDeviceControlType findEndDeviceControlType(EndDeviceControlTypeMapping controlTypeMapping) {
        String mrid = controlTypeMapping.getEndDeviceControlTypeMRID();
        return meteringService.getEndDeviceControlType(mrid).orElseThrow(NoSuchElementException.endDeviceControlTypeWithMRIDNotFound(thesaurus, mrid));
    }

    @Override
    public String getAmrSystem() {
        return AMR_SYSTEM;
    }
}