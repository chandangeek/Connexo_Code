package com.energyict.mdc.protocol.pluggable.impl;

import com.energyict.mdc.common.NotFoundException;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.common.services.WrappingFinder;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.ReferencePropertySpecFinderProvider;
import com.energyict.mdc.dynamic.relation.RelationAttributeType;
import com.energyict.mdc.dynamic.relation.RelationParticipant;
import com.energyict.mdc.dynamic.relation.RelationService;
import com.energyict.mdc.dynamic.relation.RelationType;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.pluggable.PluggableClass;
import com.energyict.mdc.pluggable.PluggableClassType;
import com.energyict.mdc.pluggable.PluggableService;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.LicensedProtocol;
import com.energyict.mdc.protocol.api.device.data.CollectedConfigurationInformation;
import com.energyict.mdc.protocol.api.device.data.CollectedData;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedDeviceCache;
import com.energyict.mdc.protocol.api.device.data.CollectedDeviceInfo;
import com.energyict.mdc.protocol.api.device.data.CollectedFirmwareVersion;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfileConfiguration;
import com.energyict.mdc.protocol.api.device.data.CollectedLogBook;
import com.energyict.mdc.protocol.api.device.data.CollectedMessage;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.data.CollectedRegisterList;
import com.energyict.mdc.protocol.api.device.data.CollectedTopology;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.LogBookIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.MessageIdentifier;
import com.energyict.mdc.protocol.api.device.data.identifiers.RegisterIdentifier;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.exceptions.DeviceProtocolAdapterCodingExceptions;
import com.energyict.mdc.protocol.api.exceptions.ProtocolCreationException;
import com.energyict.mdc.protocol.api.inbound.InboundDeviceProtocol;
import com.energyict.mdc.protocol.api.services.ConnectionTypeService;
import com.energyict.mdc.protocol.api.services.DeviceCacheMarshallingException;
import com.energyict.mdc.protocol.api.services.DeviceCacheMarshallingService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolMessageService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolSecurityService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.mdc.protocol.api.services.InboundDeviceProtocolService;
import com.energyict.mdc.protocol.api.services.LicensedProtocolService;
import com.energyict.mdc.protocol.api.services.NotAppropriateDeviceCacheMarshallingTargetException;
import com.energyict.mdc.protocol.api.services.UnableToCreateConnectionType;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.DeviceProtocolDialectUsagePluggableClass;
import com.energyict.mdc.protocol.pluggable.InboundDeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.MessageSeeds;
import com.energyict.mdc.protocol.pluggable.ProtocolDeploymentListener;
import com.energyict.mdc.protocol.pluggable.ProtocolDeploymentListenerRegistration;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.UnknownPluggableClassPropertiesException;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.CapabilityAdapterMappingFactory;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.CapabilityAdapterMappingFactoryImpl;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.MessageAdapterMappingFactory;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.MessageAdapterMappingFactoryImpl;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.SecuritySupportAdapterMappingFactory;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.SecuritySupportAdapterMappingFactoryImpl;
import com.energyict.mdc.protocol.pluggable.impl.relations.SecurityPropertySetRelationSupport;
import com.energyict.mdc.protocol.pluggable.impl.relations.SecurityPropertySetRelationTypeSupport;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.license.License;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.google.inject.Singleton;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Provides an interface for the {@link ProtocolPluggableService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-23 (13:47)
 */
@Component(name = "com.energyict.mdc.protocol.pluggable", service = {ProtocolPluggableService.class, InstallService.class, MessageSeedProvider.class}, property = "name=" + ProtocolPluggableService.COMPONENTNAME)
public class ProtocolPluggableServiceImpl implements ProtocolPluggableService, InstallService, MessageSeedProvider {

    private static final Logger LOGGER = Logger.getLogger(ProtocolPluggableServiceImpl.class.getName());
    private static final String MDC_APPLICATION_KEY = "MDC";

    private volatile DataModel dataModel;
    private volatile EventService eventService;
    private volatile Clock clock;
    private volatile Thesaurus thesaurus;
    private volatile PropertySpecService propertySpecService;
    private volatile PluggableService pluggableService;
    private volatile RelationService relationService;
    private volatile List<DeviceProtocolService> deviceProtocolServices = new CopyOnWriteArrayList<>();
    private volatile List<InboundDeviceProtocolService> inboundDeviceProtocolServices = new CopyOnWriteArrayList<>();
    private volatile List<ConnectionTypeService> connectionTypeServices = new CopyOnWriteArrayList<>();
    private volatile List<DeviceProtocolMessageService> deviceProtocolMessageServices = new CopyOnWriteArrayList<>();
    private volatile List<DeviceProtocolSecurityService> deviceProtocolSecurityServices = new CopyOnWriteArrayList<>();
    private volatile List<LicensedProtocolService> licensedProtocolServices = new CopyOnWriteArrayList<>();
    private volatile List<DeviceCacheMarshallingService> deviceCacheMarshallingServices = new CopyOnWriteArrayList<>();
    private volatile List<CollectedDataFactory> collectedDataFactories = new CopyOnWriteArrayList<>();
    private volatile IssueService issueService;
    private volatile LicenseService licenseService;
    private volatile TransactionService transactionService;
    private volatile UserService userService;
    private volatile MeteringService meteringService;
    private volatile DataVaultService dataVaultService;

    private volatile boolean installed = false;
    private List<ReferencePropertySpecFinderProvider> factoryProviders = new ArrayList<>();
    private volatile List<ProtocolDeploymentListenerRegistrationImpl> registrations = new CopyOnWriteArrayList<>();

    // For OSGi purposes
    public ProtocolPluggableServiceImpl() {
        super();
    }

    // For unit testing purposes
    @Inject
    public ProtocolPluggableServiceImpl(
            OrmService ormService,
            EventService eventService,
            NlsService nlsService,
            IssueService issueService,
            UserService userService,
            MeteringService meteringService,
            PropertySpecService propertySpecService,
            PluggableService pluggableService,
            RelationService relationService,
            LicenseService licenseService,
            DataVaultService dataVaultService) {
        this();
        this.setOrmService(ormService);
        this.setEventService(eventService);
        this.setNlsService(nlsService);
        this.setMeteringService(meteringService);
        this.setIssueService(issueService);
        this.setPropertySpecService(propertySpecService);
        this.setRelationService(relationService);
        this.setPluggableService(pluggableService);
        this.setUserService(userService);
        this.setLicenseService(licenseService);
        this.setDataVaultService(dataVaultService);
        this.activate();
        this.install();
    }

    @Override
    public ProtocolDeploymentListenerRegistration register(ProtocolDeploymentListener listener) {
        ProtocolDeploymentListenerRegistrationImpl registration = new ProtocolDeploymentListenerRegistrationImpl(listener);
        this.registrations.add(registration);
        registration.notifyAlreadyDeployedServices();
        return registration;
    }

    private void unregister(ProtocolDeploymentListenerRegistrationImpl registration) {
        this.registrations.remove(registration);
    }

    @Override
    public Object createProtocol(String javaClassName) {
        for (DeviceProtocolService service : this.deviceProtocolServices) {
            try {
                return service.createProtocol(javaClassName);
            }
            catch (ProtocolCreationException e) {
                // Try the next DeviceProtocolService
            }
        }
        if (this.deviceProtocolServices.isEmpty()) {
            LOGGER.fine("No registered device protocol services");
        }
        else {
            LOGGER.log(Level.FINE, this::allDeviceProtocolServiceClassNames);
        }
        throw new NoServiceFoundThatCanLoadTheJavaClass(javaClassName);
    }

    private String allDeviceProtocolServiceClassNames() {
        return this.deviceProtocolServices
                .stream()
                .map(Object::getClass)
                .map(Class::getName)
                .collect(Collectors.joining(", "));
    }

    @Override
    public Object createDeviceProtocolMessagesFor(String javaClassName) {
        for (DeviceProtocolMessageService service : this.deviceProtocolMessageServices) {
            try {
                return service.createDeviceProtocolMessagesFor(javaClassName);
            }
            catch (ProtocolCreationException e) {
                // Try the next DeviceProtocolMessageService
            }
        }
        throw new NoServiceFoundThatCanLoadTheJavaClass(javaClassName);
    }

    @Override
    public Object createDeviceProtocolSecurityFor(String javaClassName) {
        for (DeviceProtocolSecurityService service : this.deviceProtocolSecurityServices) {
            try {
                return service.createDeviceProtocolSecurityFor(javaClassName);
            }
            catch (DeviceProtocolAdapterCodingExceptions e) {
                // Try the next DeviceProtocolSecurityService
            }
        }
        throw new NoServiceFoundThatCanLoadTheJavaClass(javaClassName);
    }

    @Override
    public DeviceProtocolPluggableClass newDeviceProtocolPluggableClass(String name, String className) {
        final PluggableClass pluggableClass = this.pluggableService.newPluggableClass(PluggableClassType.DeviceProtocol, name, className);
        final DeviceProtocolPluggableClassImpl deviceProtocolPluggableClass = DeviceProtocolPluggableClassImpl.from(this.dataModel, pluggableClass);
        deviceProtocolPluggableClass.save();
        pluggableClass.save();
        return deviceProtocolPluggableClass;
    }

    @Override
    public DeviceProtocolPluggableClass newDeviceProtocolPluggableClass(String name, String className, TypedProperties properties) {
        boolean dirty = false;
        Set<String> unsupportedPropertyNames = properties.propertyNames();
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = this.newDeviceProtocolPluggableClass(name, className);
        for (PropertySpec propertySpec : deviceProtocolPluggableClass.getDeviceProtocol().getPropertySpecs()) {
            unsupportedPropertyNames.remove(propertySpec.getName());
            if (properties.hasValueFor(propertySpec.getName())) {
                deviceProtocolPluggableClass.setProperty(propertySpec, properties.getProperty(propertySpec.getName()));
                dirty = true;
            }
        }
        if (!unsupportedPropertyNames.isEmpty()) {
            throw new UnknownPluggableClassPropertiesException(this.thesaurus, unsupportedPropertyNames, className);
        }
        if (dirty) {
            deviceProtocolPluggableClass.save();
        }
        return deviceProtocolPluggableClass;
    }

    @Override
    public List<LicensedProtocol> getAllLicensedProtocols() {
        Optional<License> mdcLicense = this.getMdcLicense();
        if (mdcLicense.isPresent()) {
            List<LicensedProtocol> licensedProtocols = new ArrayList<>();
            for (LicensedProtocolService licensedProtocolService : this.licensedProtocolServices) {
                licensedProtocols.addAll(licensedProtocolService.getAllLicensedProtocols(mdcLicense.get()));
            }
            return licensedProtocols;
        }
        else {
            return Collections.emptyList();
        }
    }

    @Override
    public boolean isLicensedProtocolClassName(String javaClassName) {
        Optional<License> mdcLicense = this.getMdcLicense();
        if (mdcLicense.isPresent()) {
            for (LicensedProtocolService licensedProtocolService : this.licensedProtocolServices) {
                if (licensedProtocolService.isValidJavaClassName(javaClassName, mdcLicense.get())) {
                    return true;
                }
            }
        }
        return false;
    }

    private Optional<License> getMdcLicense() {
        return this.licenseService.getLicenseForApplication(MDC_APPLICATION_KEY);
    }

    @Override
    public Finder<DeviceProtocolPluggableClass> findAllDeviceProtocolPluggableClasses() {
        return new WrappingFinder<DeviceProtocolPluggableClass, PluggableClass>(this.pluggableService.findAllByType(PluggableClassType.DeviceProtocol)) {
            @Override
            public List<DeviceProtocolPluggableClass> convert(List<PluggableClass> pluggableClasses) {
                List<DeviceProtocolPluggableClass> deviceProtocolPluggableClasses = new ArrayList<>(pluggableClasses.size());
                for (PluggableClass pluggableClass : pluggableClasses) {
                    deviceProtocolPluggableClasses.add(DeviceProtocolPluggableClassImpl.from(ProtocolPluggableServiceImpl.this.dataModel, pluggableClass));
                }
                return deviceProtocolPluggableClasses;
            }
        };
    }

    @Override
    public LicensedProtocol findLicensedProtocolFor(DeviceProtocolPluggableClass deviceProtocolPluggableClass) {
        for (LicensedProtocolService licensedProtocolService : this.licensedProtocolServices) {
            LicensedProtocol licensedProtocol = licensedProtocolService.findLicensedProtocolFor(deviceProtocolPluggableClass);
            if (licensedProtocol != null) {
                return licensedProtocol;
            }
        }
        return null;
    }

    @Override
    public Optional<DeviceProtocolPluggableClass> findDeviceProtocolPluggableClass(long id) {
        Optional<PluggableClass> pluggableClass = this.pluggableService.findByTypeAndId(PluggableClassType.DeviceProtocol, id);
        return pluggableClass.map(pc -> DeviceProtocolPluggableClassImpl.from(this.dataModel, pc));
    }

    @Override
    public Optional<DeviceProtocolPluggableClass> findDeviceProtocolPluggableClassByName(String name) {
        Optional<PluggableClass> pluggableClass = this.pluggableService.findByTypeAndName(PluggableClassType.DeviceProtocol, name);
        if (pluggableClass.isPresent()) {
            DeviceProtocolPluggableClass deviceProtocolPluggableClass = DeviceProtocolPluggableClassImpl.from(this.dataModel, pluggableClass.get());
            return Optional.of(deviceProtocolPluggableClass);
        }
        else {
            return Optional.empty();
        }
    }

    @Override
    public List<DeviceProtocolPluggableClass> findDeviceProtocolPluggableClassesByClassName(String className) {
        List<PluggableClass> pluggableClasses = this.pluggableService.findByTypeAndClassName(PluggableClassType.DeviceProtocol, className);
        List<DeviceProtocolPluggableClass> deviceProtocolPluggableClasses = new ArrayList<>(pluggableClasses.size());
        for (PluggableClass pluggableClass : pluggableClasses) {
            deviceProtocolPluggableClasses.add(DeviceProtocolPluggableClassImpl.from(this.dataModel, pluggableClass));
        }
        return deviceProtocolPluggableClasses;
    }

    @Override
    public void deleteDeviceProtocolPluggableClass(long id) {
        Optional<DeviceProtocolPluggableClass> deviceProtocolPluggableClass = this.findDeviceProtocolPluggableClass(id);
        deviceProtocolPluggableClass
                .orElseThrow(() -> new NotFoundException("DeviceProtocolPluggableClass with id " + id + " cannot be deleted because it does not exist"))
                .delete();
    }

    @Override
    public DeviceProtocolDialectUsagePluggableClass getDeviceProtocolDialectUsagePluggableClass(DeviceProtocolPluggableClass pluggableClass, String dialectName) {
        DeviceProtocolDialect deviceProtocolDialect = this.getDeviceProtocolDialectFor(pluggableClass.getDeviceProtocol(), dialectName);
        return new DeviceProtocolDialectUsagePluggableClassImpl(pluggableClass, deviceProtocolDialect, this.dataModel, this.relationService, this.propertySpecService);
    }

    private DeviceProtocolDialect getDeviceProtocolDialectFor(DeviceProtocol deviceProtocol, String name) {
        for (DeviceProtocolDialect deviceProtocolDialect : deviceProtocol.getDeviceProtocolDialects()) {
            if (deviceProtocolDialect.getDeviceProtocolDialectName().equals(name)) {
                return deviceProtocolDialect;
            }
        }
        return null;
    }

    @Override
    public List<InboundDeviceProtocolPluggableClass> findInboundDeviceProtocolPluggableClassByClassName(String javaClassName) {
        List<PluggableClass> pluggableClasses = this.pluggableService.findByTypeAndClassName(PluggableClassType.DiscoveryProtocol, javaClassName);
        List<InboundDeviceProtocolPluggableClass> inboundDeviceProtocolPluggableClasses = new ArrayList<>(pluggableClasses.size());
        for (PluggableClass pluggableClass : pluggableClasses) {
            inboundDeviceProtocolPluggableClasses.add(InboundDeviceProtocolPluggableClassImpl.from(this.dataModel, pluggableClass));
        }
        return inboundDeviceProtocolPluggableClasses;
    }

    @Override
    public Optional<InboundDeviceProtocolPluggableClass> findInboundDeviceProtocolPluggableClass(long id) {
        Optional<PluggableClass> pluggableClass = this.pluggableService.findByTypeAndId(PluggableClassType.DiscoveryProtocol, id);
        return pluggableClass.map(pc -> InboundDeviceProtocolPluggableClassImpl.from(this.dataModel, pc));
    }

    @Override
    public void deleteInboundDeviceProtocolPluggableClass(long id) {
        Optional<InboundDeviceProtocolPluggableClass> inboundDeviceProtocolPluggableClass = this.findInboundDeviceProtocolPluggableClass(id);
        if (inboundDeviceProtocolPluggableClass.isPresent()) {
            inboundDeviceProtocolPluggableClass.get().delete();
        }
    }

    @Override
    public List<InboundDeviceProtocolPluggableClass> findAllInboundDeviceProtocolPluggableClass() {
        List<PluggableClass> pluggableClasses = this.pluggableService.findAllByType(PluggableClassType.DiscoveryProtocol).find();
        List<InboundDeviceProtocolPluggableClass> inboundDeviceProtocolPluggableClasses = new ArrayList<>(pluggableClasses.size());
        for (PluggableClass pluggableClass : pluggableClasses) {
            inboundDeviceProtocolPluggableClasses.add(InboundDeviceProtocolPluggableClassImpl.from(this.dataModel, pluggableClass));
        }
        return inboundDeviceProtocolPluggableClasses;
    }

    @Override
    public InboundDeviceProtocolPluggableClass newInboundDeviceProtocolPluggableClass(String name, String javaClassName) {
        final PluggableClass pluggableClass = this.pluggableService.newPluggableClass(PluggableClassType.DiscoveryProtocol, name, javaClassName);
        final InboundDeviceProtocolPluggableClassImpl inboundDeviceProtocolPluggableClass = InboundDeviceProtocolPluggableClassImpl.from(this.dataModel, pluggableClass);
        pluggableClass.save();
        inboundDeviceProtocolPluggableClass.save();
        return inboundDeviceProtocolPluggableClass;
    }

    @Override
    public InboundDeviceProtocolPluggableClass newInboundDeviceProtocolPluggableClass(String name, String javaClassName, TypedProperties properties) {
        InboundDeviceProtocolPluggableClass inboundDeviceProtocolPluggableClass = this.newInboundDeviceProtocolPluggableClass(name, javaClassName);
        for (PropertySpec propertySpec : inboundDeviceProtocolPluggableClass.getInboundDeviceProtocol().getPropertySpecs()) {
            if (properties.hasValueFor(propertySpec.getName())) {
                inboundDeviceProtocolPluggableClass.setProperty(propertySpec, properties.getProperty(propertySpec.getName()));
            }
        }
        inboundDeviceProtocolPluggableClass.save();
        return inboundDeviceProtocolPluggableClass;
    }

    @Override
    public List<ConnectionTypePluggableClass> findConnectionTypePluggableClassByClassName(String javaClassName) {
        List<PluggableClass> pluggableClasses = this.pluggableService.findByTypeAndClassName(PluggableClassType.ConnectionType, javaClassName);
        List<ConnectionTypePluggableClass> connectionTypePluggableClasses = new ArrayList<>(pluggableClasses.size());
        for (PluggableClass pluggableClass : pluggableClasses) {
            connectionTypePluggableClasses.add(ConnectionTypePluggableClassImpl.from(this.dataModel, pluggableClass));
        }
        return connectionTypePluggableClasses;
    }

    @Override
    public Optional<ConnectionTypePluggableClass> findConnectionTypePluggableClassByName(String name) {
        Optional<PluggableClass> pluggableClasses = this.pluggableService.findByTypeAndName(PluggableClassType.ConnectionType, name);
        return pluggableClasses.map(pc -> ConnectionTypePluggableClassImpl.from(this.dataModel, pc));
    }

    @Override
    public Optional<ConnectionTypePluggableClass> findConnectionTypePluggableClass(long id) {
        Optional<PluggableClass> pluggableClass = this.pluggableService.findByTypeAndId(PluggableClassType.ConnectionType, id);
        return pluggableClass.map(pc -> ConnectionTypePluggableClassImpl.from(this.dataModel, pc));
    }

    @Override
    public List<ConnectionTypePluggableClass> findAllConnectionTypePluggableClasses() {
        List<PluggableClass> pluggableClasses = this.pluggableService.findAllByType(PluggableClassType.ConnectionType).find();
        return pluggableClasses
                .stream()
                .map(pluggableClass -> ConnectionTypePluggableClassImpl.from(this.dataModel, pluggableClass))
                .collect(Collectors.toList());
    }

    @Override
    public ConnectionTypePluggableClass newConnectionTypePluggableClass(String name, String javaClassName) {
        final PluggableClass pluggableClass = this.pluggableService.newPluggableClass(PluggableClassType.ConnectionType, name, javaClassName);
        final ConnectionTypePluggableClassImpl connectionTypePluggableClass = ConnectionTypePluggableClassImpl.from(this.dataModel, pluggableClass);
        pluggableClass.save();
        connectionTypePluggableClass.save();
        return connectionTypePluggableClass;
    }

    @Override
    public ConnectionTypePluggableClass newConnectionTypePluggableClass(String name, String javaClassName, TypedProperties properties) {
        ConnectionTypePluggableClass connectionTypePluggableClass = this.newConnectionTypePluggableClass(name, javaClassName);
        for (PropertySpec propertySpec : connectionTypePluggableClass.getConnectionType().getPropertySpecs()) {
            if (properties.hasValueFor(propertySpec.getName())) {
                connectionTypePluggableClass.setProperty(propertySpec, properties.getProperty(propertySpec.getName()));
            }
        }
        connectionTypePluggableClass.save();
        return connectionTypePluggableClass;
    }

    @Override
    public String createOriginalAndConformRelationNameBasedOnJavaClassname(Class<?> clazz) {
        return RelationUtils.createOriginalAndConformRelationNameBasedOnJavaClassname(clazz.getCanonicalName());
    }

    @Override
    public String createConformRelationTypeName(String name) {
        return RelationUtils.createConformRelationTypeName(name);
    }

    @Override
    public String createConformRelationAttributeName(String name) {
        return RelationUtils.createConformRelationAttributeName(name);
    }

    @Override
    public boolean isDefaultAttribute(RelationAttributeType attributeType) {
        List<PluggableClassRelationAttributeTypeUsage> usages =
                this.dataModel
                        .mapper(PluggableClassRelationAttributeTypeUsage.class)
                        .find("relationAttributeTypeId", attributeType.getId());
        return !usages.isEmpty();
    }

    @Override
    public RelationType findSecurityPropertyRelationType(DeviceProtocolPluggableClass deviceProtocolPluggableClass) {
        DeviceProtocol deviceProtocol = deviceProtocolPluggableClass.getDeviceProtocol();
        SecurityPropertySetRelationTypeSupport relationTypeSupport =
                new SecurityPropertySetRelationTypeSupport(
                        this.dataModel,
                        this,
                        this.relationService,
                        this.propertySpecService,
                        deviceProtocol,
                        deviceProtocolPluggableClass);
        return relationTypeSupport.findRelationType();
    }

    @Override
    public Optional<Object> unMarshallDeviceProtocolCache(String jsonCache) {
        try {
            for (DeviceCacheMarshallingService service : this.deviceCacheMarshallingServices) {
                try {
                    return service.unMarshallCache(jsonCache);
                }
                catch (NotAppropriateDeviceCacheMarshallingTargetException e) {
                    // Try the next service
                }
            }
        }
        catch (DeviceCacheMarshallingException e) {
            // A service accepted the
            LOGGER.severe(e.getMessage());
            LOGGER.log(Level.FINE, e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public String marshallDeviceProtocolCache(Object legacyCache) {
        if (legacyCache != null) {
            for (DeviceCacheMarshallingService service : this.deviceCacheMarshallingServices) {
                try {
                    return service.marshall(legacyCache);
                }
                catch (NotAppropriateDeviceCacheMarshallingTargetException e) {
                    // Try the next service
                }
                catch (DeviceCacheMarshallingException e) {
                    /* Some services don't distinguish between DeviceCacheMarshallingException and NotAppropriateDeviceCacheMarshallingTargetException.
                     * Log the failure and try the next service. */
                    LOGGER.severe(e.getMessage());
                    LOGGER.log(Level.FINE, e.getMessage(), e);
                }
            }
        }
        return "";
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        DataModel dataModel = ormService.newDataModel(COMPONENTNAME, "Protocol Pluggable Classes");
        for (TableSpecs tableSpecs : TableSpecs.values()) {
            tableSpecs.addTo(dataModel);
        }
        this.dataModel = dataModel;
    }

    public DataModel getDataModel() {
        return dataModel;
    }

    public EventService getEventService() {
        return eventService;
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(COMPONENTNAME, Layer.DOMAIN);
    }

    @Reference
    public void setDataVaultService(DataVaultService dataVaultService) {
        this.dataVaultService = dataVaultService;
    }

    @Reference
    public void setPluggableService(PluggableService pluggableService) {
        this.pluggableService = pluggableService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Override
    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addDeviceProtocolService(DeviceProtocolService deviceProtocolService) {
        this.deviceProtocolServices.add(deviceProtocolService);
        this.registrations.forEach(each -> each.notifyAdded(deviceProtocolService));
        if (installed) {
            registerDeviceProtocolPluggableClasses();
        }
    }

    @SuppressWarnings("unused")
    public void removeDeviceProtocolService(DeviceProtocolService deviceProtocolService) {
        this.deviceProtocolServices.remove(deviceProtocolService);
        this.registrations.forEach(each -> each.notifyRemove(deviceProtocolService));
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addDeviceProtocolMessageService(DeviceProtocolMessageService deviceProtocolMessageService) {
        this.deviceProtocolMessageServices.add(deviceProtocolMessageService);
    }

    @SuppressWarnings("unused")
    public void removeDeviceProtocolMessageService(DeviceProtocolMessageService deviceProtocolMessageService) {
        this.deviceProtocolMessageServices.remove(deviceProtocolMessageService);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addDeviceProtocolSecurityService(DeviceProtocolSecurityService deviceProtocolSecurityService) {
        this.deviceProtocolSecurityServices.add(deviceProtocolSecurityService);
    }

    @SuppressWarnings("unused")
    public void removeDeviceProtocolSecurityService(DeviceProtocolSecurityService deviceProtocolSecurityService) {
        this.deviceProtocolSecurityServices.remove(deviceProtocolSecurityService);
    }

    @Override
    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addLicensedProtocolService(LicensedProtocolService licensedProtocolService) {
        this.licensedProtocolServices.add(licensedProtocolService);
    }

    @SuppressWarnings("unused")
    public void removeLicensedProtocolService(LicensedProtocolService licensedProtocolService) {
        this.licensedProtocolServices.remove(licensedProtocolService);
    }

    @Override
    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addInboundDeviceProtocolService(InboundDeviceProtocolService inboundDeviceProtocolService) {
        this.inboundDeviceProtocolServices.add(inboundDeviceProtocolService);
        this.registrations.forEach(each -> each.notifyAdded(inboundDeviceProtocolService));
        if (installed) {
            registerInboundDeviceProtocolPluggableClasses();
        }
    }

    @SuppressWarnings("unused")
    public void removeInboundDeviceProtocolService(InboundDeviceProtocolService inboundDeviceProtocolService) {
        this.inboundDeviceProtocolServices.remove(inboundDeviceProtocolService);
        this.registrations.forEach(each -> each.notifyRemove(inboundDeviceProtocolService));
    }

    public PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
        if (!this.factoryProviders.isEmpty()) {
            for (ReferencePropertySpecFinderProvider factoryProvider : this.factoryProviders) {
                this.propertySpecService.addFactoryProvider(factoryProvider);
            }
        }
    }

    public RelationService getRelationService() {
        return relationService;
    }

    @Reference
    public void setRelationService(RelationService relationService) {
        this.relationService = relationService;
    }

    public List<ConnectionTypeService> getConnectionTypeServices() {
        return connectionTypeServices;
    }

    @Override
    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addConnectionTypeService(ConnectionTypeService connectionTypeService) {
        this.connectionTypeServices.add(connectionTypeService);
        this.registrations.forEach(each -> each.notifyAdded(connectionTypeService));
        if (installed) {
            registerConnectionTypePluggableClasses();
        }
    }

    @SuppressWarnings("unused")
    public void removeConnectionTypeService(ConnectionTypeService connectionTypeService) {
        this.connectionTypeServices.remove(connectionTypeService);
        this.registrations.forEach(each -> each.notifyRemove(connectionTypeService));
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addDeviceCacheMarshallingService(DeviceCacheMarshallingService deviceCacheMarshallingService) {
        this.deviceCacheMarshallingServices.add(deviceCacheMarshallingService);
    }

    @SuppressWarnings("unused")
    public void removeDeviceCacheMarshallingService(DeviceCacheMarshallingService deviceCacheMarshallingService) {
        this.deviceCacheMarshallingServices.remove(deviceCacheMarshallingService);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addCollectedDataFactory(CollectedDataFactory collectedDataFactory) {
        this.collectedDataFactories.add(collectedDataFactory);
    }

    @SuppressWarnings("unused")
    public void removeCollectedDataFactory(CollectedDataFactory collectedDataFactory) {
        this.collectedDataFactories.remove(collectedDataFactory);
    }

    @Reference
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }

    @Reference
    public void setLicenseService(LicenseService licenseService) {
        this.licenseService = licenseService;
    }

    /**
     * We add all the ReferencePropertySpecFinderProviders so we can try to reregsiter all PluggableClasses
     */
    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addFactoryProvider(ReferencePropertySpecFinderProvider factoryProvider) {
        if (getPropertySpecService() != null) {
            getPropertySpecService().addFactoryProvider(factoryProvider);
        }
        this.factoryProviders.add(factoryProvider);
        if (installed) {
            registerAllPluggableClasses();
        }
    }

    @SuppressWarnings("unused")
    public void removeFactoryProvider(ReferencePropertySpecFinderProvider factoryProvider) {
        this.factoryProviders.remove(factoryProvider);
    }

    @Override
    public ConnectionType createConnectionType(String javaClassName) {
        ConnectionType connectionType = null;
        Throwable throwable = null;
        for (ConnectionTypeService connectionTypeService : this.connectionTypeServices) {
            try {
                connectionType = connectionTypeService.createConnectionType(javaClassName);
            }
            catch (Exception e) {
                throwable = e;
                // silently ignore, will try other service
            }
        }
        if (connectionType != null) {
            return connectionType;
        }
        else {
            if (this.connectionTypeServices.isEmpty()) {
                LOGGER.fine("No registered connection type services");
            }
            else {
                LOGGER.log(Level.FINE, this::allConnectionTypeServiceClassNames);
            }
            throw new UnableToCreateConnectionType(throwable, javaClassName);
        }
    }

    private String allConnectionTypeServiceClassNames() {
        return this.connectionTypeServices
                .stream()
                .map(Object::getClass)
                .map(Class::getName)
                .collect(Collectors.joining(", "));
    }

    @Override
    public InboundDeviceProtocol createInboundDeviceProtocolFor(PluggableClass pluggableClass) {
        InboundDeviceProtocol inboundDeviceProtocol;
        Exception throwable = null;
        for (InboundDeviceProtocolService inboundDeviceProtocolService : this.inboundDeviceProtocolServices) {
            try {
                inboundDeviceProtocol = inboundDeviceProtocolService.createInboundDeviceProtocolFor(pluggableClass);
                if (inboundDeviceProtocol != null) {
                    return inboundDeviceProtocol;
                }
            }
            catch (Exception e) {
                throwable = e;
                // silently ignore, will try other service
            }
        }
        if (this.inboundDeviceProtocolServices.isEmpty()) {
            LOGGER.fine("No registered inbound device protocol services");
        }
        else {
            LOGGER.log(Level.FINE, this::allInboundDeviceProtocolServiceClassNames);
        }
        throw DeviceProtocolAdapterCodingExceptions.genericReflectionError(MessageSeeds.GENERIC_JAVA_REFLECTION_ERROR, throwable, pluggableClass.getJavaClassName());
    }

    private String allInboundDeviceProtocolServiceClassNames() {
        return this.inboundDeviceProtocolServices
                .stream()
                .map(Object::getClass)
                .map(Class::getName)
                .collect(Collectors.joining(", "));
    }

    @Override
    public boolean hasSecurityRelations(RelationParticipant securityPropertySet, DeviceProtocol deviceProtocol) {
        SecurityPropertySetRelationSupport relationSupport = new SecurityPropertySetRelationSupport(securityPropertySet, deviceProtocol, this, this.relationService, this.propertySpecService, this.clock);
        return relationSupport.hasValues();
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(EventService.class).toInstance(eventService);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(PropertySpecService.class).toInstance(propertySpecService);
                bind(PluggableService.class).toInstance(pluggableService);
                bind(RelationService.class).toInstance(relationService);
                bind(IssueService.class).toInstance(issueService);
                bind(LicenseService.class).toInstance(licenseService);
                bind(UserService.class).toInstance(userService);
                bind(MeteringService.class).toInstance(meteringService);
                bind(DataVaultService.class).toInstance(dataVaultService);
                bind(CollectedDataFactory.class).toInstance(new CompositeCollectedDataFactory());
                bind(MessageAdapterMappingFactory.class).to(MessageAdapterMappingFactoryImpl.class).in(Singleton.class);
                bind(SecuritySupportAdapterMappingFactory.class).to(SecuritySupportAdapterMappingFactoryImpl.class).in(Singleton.class);
                bind(CapabilityAdapterMappingFactory.class).to(CapabilityAdapterMappingFactoryImpl.class).in(Singleton.class);
                bind(ProtocolPluggableService.class).toInstance(ProtocolPluggableServiceImpl.this);
            }
        };
    }

    @Activate
    public void activate() {
        //TODO need a proper implementation of the DataVault!
        this.dataModel.register(this.getModule());
        this.installed = this.dataModel.isInstalled();
        this.registerAllPluggableClasses();
    }

    private void registerAllPluggableClasses() {
        if (installed) {
            this.registerInboundDeviceProtocolPluggableClasses();
            this.registerDeviceProtocolPluggableClasses();
            this.registerConnectionTypePluggableClasses();
        }
    }

    private void registerDeviceProtocolPluggableClasses() {
        if (!this.deviceProtocolServices.isEmpty()) {
            new DeviceProtocolPluggableClassRegistrar(this, this.transactionService).registerAll(this.getAllLicensedProtocols());
        }
        else {
            LOGGER.fine("No device protocol services have registered yet, makes no sense to attempt to register all device protocol pluggable classes");
        }
    }

    private void registerInboundDeviceProtocolPluggableClasses() {
        if (!this.inboundDeviceProtocolServices.isEmpty()) {
            new InboundDeviceProtocolPluggableClassRegistrar(this, this.transactionService).
                    registerAll(Collections.unmodifiableList(this.inboundDeviceProtocolServices));
        }
        else {
            LOGGER.fine("No inbound protocol services have registered yet, makes no sense to attempt to register all inboudn device protocol pluggable classes");
        }
    }

    private void registerConnectionTypePluggableClasses() {
        if (!this.connectionTypeServices.isEmpty()) {
            new ConnectionTypePluggableClassRegistrar(this, this.transactionService).
                    registerAll(Collections.unmodifiableList(this.connectionTypeServices));
        }
        else {
            LOGGER.fine("No connection type services have registered yet, makes no sense to attempt to register all connection type pluggable classes");
        }
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Override
    public void install() {
        if (!dataModel.isInstalled()) {
            new Installer(this.dataModel, this.eventService).install(true, true);
        }
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("ORM", "USR", "EVT", "NLS");
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    private class CompositeCollectedDataFactory implements CollectedDataFactory {
        private CollectedDataFactory getCollectedDataFactory() {
            return collectedDataFactories.stream().findFirst().orElseThrow(() -> new IllegalStateException("No suitable CollectedDataFactory found"));
        }

        @Override
        public CollectedLoadProfile createCollectedLoadProfile(LoadProfileIdentifier loadProfileIdentifier) {
            return this.getCollectedDataFactory().createCollectedLoadProfile(loadProfileIdentifier);
        }

        @Override
        public CollectedTopology createCollectedTopology(DeviceIdentifier deviceIdentifier) {
            return this.getCollectedDataFactory().createCollectedTopology(deviceIdentifier);
        }

        @Override
        public CollectedLogBook createCollectedLogBook(LogBookIdentifier logBookIdentifier) {
            return this.getCollectedDataFactory().createCollectedLogBook(logBookIdentifier);
        }

        @Override
        public CollectedRegister createMaximumDemandCollectedRegister(RegisterIdentifier registerIdentifier, ReadingType readingType) {
            return this.getCollectedDataFactory().createMaximumDemandCollectedRegister(registerIdentifier, readingType);
        }

        @Override
        public CollectedRegister createCollectedRegisterForAdapter(RegisterIdentifier registerIdentifier, ReadingType readingType) {
            return this.getCollectedDataFactory().createCollectedRegisterForAdapter(registerIdentifier, readingType);
        }

        @Override
        public CollectedRegister createBillingCollectedRegister(RegisterIdentifier registerIdentifier, ReadingType readingType) {
            return this.getCollectedDataFactory().createBillingCollectedRegister(registerIdentifier, readingType);
        }

        @Override
        public CollectedRegister createDefaultCollectedRegister(RegisterIdentifier registerIdentifier, ReadingType readingType) {
            return this.getCollectedDataFactory().createDefaultCollectedRegister(registerIdentifier, readingType);
        }

        @Override
        public CollectedLogBook createNoLogBookCollectedData(DeviceIdentifier deviceIdentifier) {
            return this.getCollectedDataFactory().createNoLogBookCollectedData(deviceIdentifier);
        }

        @Override
        public CollectedMessage createCollectedMessage(MessageIdentifier messageIdentifier) {
            return this.getCollectedDataFactory().createCollectedMessage(messageIdentifier);
        }

        @Override
        public CollectedMessage createCollectedMessageWithLoadProfileData(MessageIdentifier messageIdentifier, CollectedLoadProfile collectedLoadProfile) {
            return this.getCollectedDataFactory().createCollectedMessageWithLoadProfileData(messageIdentifier, collectedLoadProfile);
        }

        @Override
        public CollectedMessage createCollectedMessageWithRegisterData(DeviceIdentifier deviceIdentifier, MessageIdentifier messageIdentifier, List<CollectedRegister> collectedRegisters) {
            return this.getCollectedDataFactory().createCollectedMessageWithRegisterData(deviceIdentifier, messageIdentifier, collectedRegisters);
        }

        @Override
        public CollectedMessage createCollectedMessageTopology(MessageIdentifier messageIdentifier, CollectedTopology collectedTopology) {
            return this.getCollectedDataFactory().createCollectedMessageTopology(messageIdentifier, collectedTopology);
        }

        @Override
        public CollectedDeviceCache createCollectedDeviceCache(DeviceIdentifier deviceIdentifier) {
            return this.getCollectedDataFactory().createCollectedDeviceCache(deviceIdentifier);
        }

        @Override
        public CollectedMessageList createCollectedMessageList(List<OfflineDeviceMessage> offlineDeviceMessages) {
            return this.getCollectedDataFactory().createCollectedMessageList(offlineDeviceMessages);
        }

        @Override
        public CollectedMessageList createEmptyCollectedMessageList() {
            return this.getCollectedDataFactory().createEmptyCollectedMessageList();
        }

        @Override
        public CollectedRegisterList createCollectedRegisterList(DeviceIdentifier deviceIdentifier) {
            return this.getCollectedDataFactory().createCollectedRegisterList(deviceIdentifier);
        }

        @Override
        public CollectedLoadProfileConfiguration createCollectedLoadProfileConfiguration(ObisCode profileObisCode, DeviceIdentifier<?> deviceIdentifier) {
            return this.getCollectedDataFactory().createCollectedLoadProfileConfiguration(profileObisCode, deviceIdentifier);
        }

        @Override
        public CollectedLoadProfileConfiguration createCollectedLoadProfileConfiguration(ObisCode profileObisCode, DeviceIdentifier<?> deviceIdentifier, boolean supported) {
            return this.getCollectedDataFactory().createCollectedLoadProfileConfiguration(profileObisCode, deviceIdentifier, supported);
        }

        @Override
        public CollectedConfigurationInformation createCollectedConfigurationInformation(DeviceIdentifier deviceIdentifier, String fileExtension, byte[] contents) {
            return this.getCollectedDataFactory().createCollectedConfigurationInformation(deviceIdentifier, fileExtension, contents);
        }

        @Override
        public CollectedData createCollectedAddressProperties(DeviceIdentifier deviceIdentifier, String ipAddress, String ipAddressPropertyName) {
            return this.getCollectedDataFactory().createCollectedAddressProperties(deviceIdentifier, ipAddress, ipAddressPropertyName);
        }

        @Override
        public CollectedDeviceInfo createCollectedDeviceProtocolProperty(DeviceIdentifier deviceIdentifier, PropertySpec propertySpec, Object propertyValue) {
            return this.getCollectedDataFactory().createCollectedDeviceProtocolProperty(deviceIdentifier, propertySpec, propertyValue);
        }

        @Override
        public CollectedFirmwareVersion createFirmwareVersionsCollectedData(DeviceIdentifier deviceIdentifier) {
            return this.getCollectedDataFactory().createFirmwareVersionsCollectedData(deviceIdentifier);
        }
    }

    private class ProtocolDeploymentListenerRegistrationImpl implements ProtocolDeploymentListenerRegistration {
        private final ProtocolDeploymentListener listener;

        private ProtocolDeploymentListenerRegistrationImpl(ProtocolDeploymentListener listener) {
            super();
            this.listener = listener;
        }

        private void notifyAlreadyDeployedServices() {
            deviceProtocolServices.forEach(this::notifyAdded);
            inboundDeviceProtocolServices.forEach(this::notifyAdded);
            connectionTypeServices.forEach(this::notifyAdded);
        }

        private void notifyAdded(DeviceProtocolService service) {
            this.listener.deviceProtocolServiceDeployed(service);
        }

        private void notifyRemove(DeviceProtocolService service) {
            this.listener.deviceProtocolServiceUndeployed(service);
        }

        private void notifyAdded(InboundDeviceProtocolService service) {
            this.listener.inboundDeviceProtocolServiceDeployed(service);
        }

        private void notifyRemove(InboundDeviceProtocolService service) {
            this.listener.inboundDeviceProtocolServiceUndeployed(service);
        }

        private void notifyAdded(ConnectionTypeService service) {
            this.listener.connectionTypeServiceDeployed(service);
        }

        private void notifyRemove(ConnectionTypeService service) {
            this.listener.connectionTypeServiceUndeployed(service);
        }

        @Override
        public void unregister() {
            ProtocolPluggableServiceImpl.this.unregister(this);
        }
    }

}