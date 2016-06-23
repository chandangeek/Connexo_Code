package com.energyict.mdc.device.data.impl.ami;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.EndDevice;
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
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.users.User;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.energyict.mdc.device.data.security.Privileges.Constants.VIEW_DEVICE;

@Component(name = "com.energyict.mdc.device.data.impl.ami.MultiSenseHeadEndInterface",
        service = {HeadEndInterface.class, TranslationKeyProvider.class},
        property = "name=MultiSenseHeadEndInterface", immediate = true)
public class MultiSenseHeadEndInterface implements HeadEndInterface, TranslationKeyProvider {

    private static final String AMR_SYSTEM = KnownAmrSystem.MDC.getName();
    private static final Logger LOGGER = Logger.getLogger(MultiSenseHeadEndInterface.class.getName());

    private volatile DeviceService deviceService;
    private volatile MeteringService meteringService;
    private volatile DeviceMessageSpecificationService deviceMessageSpecificationService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile NlsService nlsService;
    private volatile Thesaurus thesaurus;
    private volatile PropertySpecService propertySpecService;
    private volatile ThreadPrincipalService threadPrincipalService;

    //For OSGI purposes
    public MultiSenseHeadEndInterface() {
    }

    @Inject
    public MultiSenseHeadEndInterface(DeviceService deviceService, MeteringService meteringService, DeviceMessageSpecificationService deviceMessageSpecificationService, DeviceConfigurationService deviceConfigurationService, NlsService nlsService, Thesaurus thesaurus, PropertySpecService propertySpecService, ThreadPrincipalService threadPrincipalService) {
        this.deviceService = deviceService;
        this.meteringService = meteringService;
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
        this.deviceConfigurationService = deviceConfigurationService;
        this.nlsService = nlsService;
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
        this.threadPrincipalService = threadPrincipalService;
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
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference
    public final void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
        this.thesaurus = nlsService.getThesaurus(DeviceDataServices.COMPONENT_NAME, Layer.DOMAIN);
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

    private Device findDeviceForEndDevice(EndDevice endDevice) {
        return deviceService.findByUniqueMrid(endDevice.getMRID())
                .orElseThrow(() -> new IllegalArgumentException(MessageSeeds.NO_SUCH_DEVICE.getDefaultFormat() + " " + endDevice
                        .getMRID()));
    }

    @Override
    public Optional<URL> getURLForEndDevice(EndDevice endDevice) {
        if (!((User) threadPrincipalService.getPrincipal())
                .hasPrivilege(KnownAmrSystem.MDC.getName(), VIEW_DEVICE)) {
            return Optional.empty();
        } else {
            if (endDevice.getAmrSystem().is(KnownAmrSystem.MDC)) {
                Device device = findDeviceForEndDevice(endDevice);
                URL url = null;
                Map<KnownAmrSystem, String> urls = meteringService.getSupportedApplicationsUrls();
                try {
                    if (urls != null && !urls.isEmpty()) {
                        url = new URL(urls
                                .get(KnownAmrSystem.MDC).trim() + "/devices/" + device.getmRID());
                    } else {
                        throw new MalformedURLException();
                    }

                } catch (MalformedURLException e) {
                    LOGGER.log(Level.SEVERE, "Unable to parse [url= " + urls.get(KnownAmrSystem.MDC) + " ]", e);
                }
                return Optional.of(url);
            } else {
                return Optional.empty();
            }
        }
    }

    @Override
    public EndDeviceCapabilities getCapabilities(EndDevice endDevice) {
        return new EndDeviceCapabilities(deviceConfigurationService.getReadingTypesRelatedToConfiguration(findDeviceForEndDevice(endDevice)
                        .getDeviceConfiguration()), Collections.emptyList());
    }

    @Override
    public CommandFactory getCommandFactory() {
        return new EndDeviceCommandFactoryImpl(meteringService, deviceService, deviceMessageSpecificationService, nlsService, thesaurus, propertySpecService);
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
        //ALL CODE RELATED TO SERVICECALLS TEMPORARILY MOVED TO demo bundle under ami_scsexamples
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


   @Override
    public String getAmrSystem() {
        return AMR_SYSTEM;
    }

    @Override
    public String getComponentName() {
        return DeviceDataServices.COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Collections.emptyList();
    }
}