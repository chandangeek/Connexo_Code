package com.energyict.mdc.device.data.impl.ami;

import com.elster.jupiter.cbo.EndDeviceType;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.EndDeviceControlType;
import com.elster.jupiter.metering.EndDeviceControlTypeMapping;
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
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.energyict.mdc.device.config.ComTaskEnablement;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionBuilder;
import com.energyict.mdc.device.data.tasks.ManuallyScheduledComTaskExecution;
import com.energyict.mdc.protocol.api.TrackingCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.tasks.MessagesTask;
import com.energyict.mdc.tasks.StatusInformationTask;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component(name = "com.energyict.mdc.device.data.impl.ami.MultiSenseHeadEndInterface",
        service = {HeadEndInterface.class, TranslationKeyProvider.class},
        property = "name=MultiSenseHeadEndInterface", immediate = true)
public class MultiSenseHeadEndInterface implements HeadEndInterface {


    private static final String UNDEFINED = "undefined";
    private static final String AMR_SYSTEM = KnownAmrSystem.MDC.getName();

    private volatile Clock clock;
    private volatile DeviceService deviceService;
    private volatile MeteringService meteringService;
    private volatile DeviceMessageSpecificationService deviceMessageSpecificationService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile MessageService messageService;
    private volatile NlsService nlsService;
    private volatile Thesaurus thesaurus;
    private volatile ServiceCallService serviceCallService;
    private volatile CustomPropertySetService customPropertySetService;
    private volatile PropertySpecService propertySpecService;

    //For OSGI purposes
    public MultiSenseHeadEndInterface() {
    }

    @Inject
    public MultiSenseHeadEndInterface(Clock clock, DeviceService deviceService, MeteringService meteringService, DeviceMessageSpecificationService deviceMessageSpecificationService, DeviceConfigurationService deviceConfigurationService, MessageService messageService, NlsService nlsService, Thesaurus thesaurus, ServiceCallService serviceCallService, CustomPropertySetService customPropertySetService, PropertySpecService propertySpecService) {
        this.clock = clock;
        this.deviceService = deviceService;
        this.meteringService = meteringService;
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.messageService = messageService;
        this.nlsService = nlsService;
        this.thesaurus = thesaurus;
        this.serviceCallService = serviceCallService;
        this.customPropertySetService = customPropertySetService;
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setClock(Clock clock) {
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
    public void setDeviceMessageSpecificationService(DeviceMessageSpecificationService deviceMessageSpecificationService) {
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Reference
    public final void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(MeteringService.COMPONENTNAME, Layer.DOMAIN);
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
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }


    @Activate
    public void activate() {
        System.out.println("Activating MultiSense Head End Interface");
    }

    @Deactivate
    public void deactivate() {
    }

    private void scheduleDeviceCommandsComTaskEnablement(EndDeviceCommand endDeviceCmd) {
        Device device = findDeviceForEndDevice(endDeviceCmd.getEndDevice());
        List<DeviceMessageId> deviceMessageIds = new ArrayList<>();
        endDeviceCmd.getDeviceMessageIds().forEach(id -> deviceMessageIds.add(DeviceMessageId.havingId(id)));
        getComTaskEnablementsForDeviceMessages(device, deviceMessageIds).forEach(comTaskEnablement -> {
            Optional<ComTaskExecution> existingComTaskExecution = device.getComTaskExecutions().stream()
                    .filter(cte -> cte.getComTasks().stream()
                            .anyMatch(comTask -> comTask.getId() == comTaskEnablement.getComTask().getId()))
                    .findFirst();
            existingComTaskExecution.orElseGet(() -> createAdHocComTaskExecution(device, comTaskEnablement)).runNow();
        });
    }

    private ManuallyScheduledComTaskExecution createAdHocComTaskExecution(Device device, ComTaskEnablement comTaskEnablement) {
        ComTaskExecutionBuilder<ManuallyScheduledComTaskExecution> comTaskExecutionBuilder = device.newAdHocComTaskExecution(comTaskEnablement);
        if (comTaskEnablement.hasPartialConnectionTask()) {
            device.getConnectionTasks().stream()
                    .filter(connectionTask -> connectionTask.getPartialConnectionTask()
                            .getId() == comTaskEnablement.getPartialConnectionTask().get().getId())
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
                        .orElseThrow(() -> new IllegalStateException(MessageSeeds.NO_COMTASK_FOR_COMMAND.getDefaultFormat()))));


        return comTaskEnablements.stream().distinct();
    }

    /**
     * @throws javax.validation.ConstraintViolationException in case not all DeviceMessageAttributes have a value
     */
    private List<DeviceMessage<Device>> createDeviceMessagesOnDevice(EndDeviceCommand endDeviceCommand, ServiceCall serviceCall) {
        List<DeviceMessage<Device>> deviceMessages = new ArrayList<>();
        endDeviceCommand.getDeviceMessageIds().forEach(msgId -> {
                    Device.DeviceMessageBuilder deviceMessageBuilder = findDeviceForEndDevice(endDeviceCommand.getEndDevice()).newDeviceMessage(DeviceMessageId
                            .havingId(msgId), TrackingCategory.serviceCall)
                            .setTrackingId("" + serviceCall.getId())
                            .setReleaseDate(clock.instant());
                    for (PropertySpec propertySpec : endDeviceCommand.getCommandArgumentSpecs()) {
                       /* if (endDeviceCommand.getAttributes().containsKey(propertySpec.getName())) {
                            deviceMessageBuilder.addProperty(propertySpec.getName(), endDeviceCommand.getAttributes()
                                    .get(propertySpec.getName()));
                         */
                            deviceMessageBuilder.addProperty(propertySpec.getName(), propertySpec);
                    }
                    deviceMessages.add(deviceMessageBuilder.add());
                }

        );

        return deviceMessages;
    }


    private void scheduleStatusInformationTask(EndDevice endDevice, Instant scheduleTime) {
        Device device = findDeviceForEndDevice(endDevice);
        ComTaskEnablement comTaskEnablement = getStatusInformationComTaskEnablement(device);
        Optional<ComTaskExecution> existingComTaskExecution = device.getComTaskExecutions().stream()
                .filter(cte -> cte.getComTasks()
                        .stream()
                        .anyMatch(comTask -> comTask.getId() == comTaskEnablement.getComTask().getId())).findFirst();
        existingComTaskExecution.orElseGet(() -> createAdHocComTaskExecution(device, comTaskEnablement))
                .schedule(scheduleTime);
    }

    private ComTaskEnablement getStatusInformationComTaskEnablement(Device device) {
        return device.getDeviceConfiguration()
                .getComTaskEnablements()
                .stream()
                .filter(cte -> cte.getComTask().getProtocolTasks().stream().
                        filter(task -> task instanceof StatusInformationTask).
                        findFirst().
                        isPresent())
                .findAny()
                .orElseThrow(() -> new IllegalStateException(MessageSeeds.NO_COMTASK_FOR_STATUS_INFORMATION.getDefaultFormat()));
    }

    private Device findDeviceForEndDevice(EndDevice endDevice) {
        return deviceService.findByUniqueMrid(endDevice.getMRID())
                .orElseThrow(() -> new IllegalArgumentException(MessageSeeds.NO_SUCH_DEVICE.getDefaultFormat() + " " + endDevice
                        .getMRID()));
    }

    @Override
    public Optional<URI> getURIForEndDevice(EndDevice endDevice) {
        if (endDevice.getAmrSystem().is(KnownAmrSystem.MDC)) {
            Device device = findDeviceForEndDevice(endDevice);
            URI uri = null;
            try {
                uri = new URI("/devices/" + device.getmRID());
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            return Optional.of(uri);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public EndDeviceCapabilities getCapabilities(EndDevice endDevice) {
        Device device = findDeviceForEndDevice(endDevice);
        List<ReadingType> readingTypes = deviceConfigurationService.getReadingTypesRelatedToConfiguration(findDeviceForEndDevice(endDevice)
                .getDeviceConfiguration());
        int endDeviceType = EndDeviceType.get(Math.toIntExact(device.getDeviceConfiguration().getDeviceType().getId()))
                .getValue();
        List<EndDeviceControlType> controlTypes = new ArrayList<>();
        Stream.of(EndDeviceControlTypeMapping.values()).forEach(type -> {
            if (type.getEndDeviceControlTypeMRID().startsWith(String.valueOf(endDeviceType) + ".")
                    || type.getEndDeviceControlTypeMRID().startsWith("0.")) {
                meteringService.getEndDeviceControlType(type.getEndDeviceControlTypeMRID())
                        .ifPresent(found -> controlTypes.add(found));
            }
        });

        return new EndDeviceCapabilities(readingTypes, controlTypes);
    }

    @Override
    public CommandFactory getCommandFactory() {
        return new CommandFactoryImpl(meteringService, deviceService, deviceMessageSpecificationService, nlsService, thesaurus, propertySpecService);
    }

    @Override
    public CompletionOptions scheduleMeterRead(Meter meter, List<ReadingType> readingTypes, Instant instant) {
        return null;
    }

    @Override
    public CompletionOptions scheduleMeterRead(Meter meter, List<ReadingType> readingTypes, Instant instant, ServiceCall serviceCall) {
        return null;
    }

    @Override
    public CompletionOptions readMeter(Meter meter, List<ReadingType> readingTypes) {
        return null;
    }

    @Override
    public CompletionOptions readMeter(Meter meter, List<ReadingType> redingTypes, ServiceCall serviceCall) {
        return null;
    }

    @Override
    public CompletionOptions sendCommand(EndDeviceCommand endDeviceCommand, Instant instant) {
      //ALL CODE RELATED TO SERVICECALLS TEMPORARILY MOVED TO demo bundle under amiscs
        /*  String commandName = endDeviceCommand.getName();
        RegisteredCustomPropertySet customPropertySet = customPropertySetService.findActiveCustomPropertySets(ServiceCall.class)
                .stream()
                .filter(cps -> cps.getCustomPropertySet()
                        .getName()
                        .equals(ServiceOperationCustomPropertySet.class.getSimpleName()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Could not find my custom property set"));
        ServiceCallType serviceCallType = serviceCallService.findServiceCallType(ServiceCallCommands.ServiceCallTypes.valueOf(commandName)
                .getTypeName(), ServiceCallCommands.ServiceCallTypes.valueOf(commandName).getTypeVersion())
                .orElseGet(() -> serviceCallService.createServiceCallType(ServiceCallCommands.ServiceCallTypes.valueOf(commandName)
                        .getTypeName(), ServiceCallCommands.ServiceCallTypes.valueOf(commandName).getTypeVersion())
                        .handler("ServiceOperationHandler")
                        .logLevel(LogLevel.FINEST)
                        .customPropertySet(customPropertySet)
                        .create());
        Device device = findDeviceForEndDevice(endDeviceCommand.getEndDevice());
        ServiceOperationDomainExtension domainExtension = new ServiceOperationDomainExtension();
        domainExtension.setmRIDDevice(device.getmRID());
        domainExtension.setActivationDate(clock.instant());
        if (Arrays.asList(EndDeviceCommandImpl.EndDeviceCommandType.ARM.getName(), EndDeviceCommandImpl.EndDeviceCommandType.CONNECT
                .getName(), EndDeviceCommandImpl.EndDeviceCommandType.DISCONNECT.getName()).contains(commandName)) {
            domainExtension.setBreakerStatus(BreakerStatus.valueOf(commandName + "ed"));
        }
        if (commandName.equals(EndDeviceCommandImpl.EndDeviceCommandType.ENABLE_LOAD_LIMIT)) {
            domainExtension.setLoadLimitEnabled(true);
            Optional<Map.Entry<String, Object>> loadLimit = endDeviceCommand.getAttributes()
                    .entrySet()
                    .stream()
                    .filter(attribute -> attribute.getKey().equals(DeviceMessageConstants.normalThresholdAttributeName))
                    .findFirst();
            if (loadLimit.isPresent()) {
                domainExtension.setLoadLimit((Quantity) loadLimit.get().getValue());
            }
        }
        if (commandName.equals(EndDeviceCommandImpl.EndDeviceCommandType.DISABLE_LOAD_LIMIT)) {
            domainExtension.setLoadLimitEnabled(false);
        }
        //TODO: clean up + set loadLimit
        domainExtension.setReadingType(getCapabilities(endDeviceCommand.getEndDevice()).getConfiguredReadingTypes()
                .stream()
                .findFirst()
                .get()
                .getName());
        domainExtension.setCallback(getURLForEndDevice(endDeviceCommand.getEndDevice()).get().toString());
        ServiceCall serviceCall;
        if (device != null) {
            serviceCall = serviceCallType.newServiceCall()
                    .origin("Multisense")
                    .extendedWith(domainExtension)
                    .targetObject(device)
                    .create();
            serviceCall.requestTransition(DefaultState.PENDING);
        } else {
            serviceCall = serviceCallType.newServiceCall()
                    .origin("Multisense")
                    .extendedWith(domainExtension)
                    .create();
            serviceCall.log(LogLevel.SEVERE, "Device could not be found");
            serviceCall.requestTransition(DefaultState.REJECTED);

        }
        return new CompletionOptionsImpl("Newly created parent service call", null, serviceCall);
    */
        return null;
    }

    @Override
    public CompletionOptions sendCommand(EndDeviceCommand endDeviceCommand, Instant instant, ServiceCall parentServiceCall) {
        //ALL CODE RELATED TO SERVICECALLS TEMPORARILY MOVED TO demo bundle under amiscs
       /* List<DeviceMessage<Device>> deviceMessages = createDeviceMessagesOnDevice(endDeviceCommand, parentServiceCall);
        RegisteredCustomPropertySet customPropertySet = customPropertySetService.findActiveCustomPropertySets(ServiceCall.class)
                .stream()
                .filter(cps -> cps.getCustomPropertySet()
                        .getName()
                        .equals(ContactorOperationCustomPropertySet.class.getSimpleName()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Could not find my custom property set"));

        ServiceOperationDomainExtension domainExtension = (ServiceOperationDomainExtension) parentServiceCall.getExtensionFor(customPropertySet
                .getCustomPropertySet()).get();
        Optional<DestinationSpec> destinationSpec = getDestinationSpec(domainExtension.getDestinationSpecName());


        scheduleDeviceCommandsComTaskEnablement(endDeviceCommand);
        parentServiceCall.requestTransition(DefaultState.PENDING);

        return new CompletionOptionsImpl(getFormattedDeviceMessages(deviceMessages), destinationSpec.get(), parentServiceCall);
*/
        return null;
    }


    private Optional<DestinationSpec> getDestinationSpec(String name) {
        // TODO - COMMAND_SCHEDULER_QUEUE_DESTINATION
        return messageService.getDestinationSpec(name);
    }

    private String getFormattedDeviceMessages(List<DeviceMessage<Device>> deviceMessages) {
        List<String> msgList = new ArrayList<>();
        deviceMessages.stream()
                .forEach(msg -> msg.getAttributes()
                        .stream()
                        .forEach(element -> msgList.add(element.getName() + " - " + String.valueOf(element.getValue()))));
        return msgList.stream().collect(Collectors.joining(","));

    }

    @Override
    public String getAmrSystem() {
        return AMR_SYSTEM;
    }
}