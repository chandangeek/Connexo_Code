package com.energyict.mdc.protocol.pluggable.impl;

import com.energyict.mdc.common.DataVault;
import com.energyict.mdc.common.DataVaultProvider;
import com.energyict.mdc.common.NotFoundException;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.common.services.Finder;
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
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.UnknownPluggableClassPropertiesException;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.SecuritySupportAdapterMappingFactory;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.SecuritySupportAdapterMappingFactoryImpl;
import com.energyict.mdc.protocol.pluggable.impl.relations.SecurityPropertySetRelationSupport;
import com.energyict.mdc.protocol.pluggable.impl.relations.SecurityPropertySetRelationTypeSupport;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.license.License;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.io.File;
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
@Component(name = "com.energyict.mdc.protocol.pluggable", service = {ProtocolPluggableService.class, InstallService.class}, property = "name=" + ProtocolPluggableService.COMPONENTNAME)
public class ProtocolPluggableServiceImpl implements ProtocolPluggableService, InstallService {

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
    private volatile IssueService issueService;
    private volatile LicenseService licenseService;
    private volatile TransactionService transactionService;
    private volatile UserService userService;

    private volatile boolean active = false;
    private List<ReferencePropertySpecFinderProvider> factoryProviders = new ArrayList<>();

    public ProtocolPluggableServiceImpl() {
        super();
    }

    @Inject
    public ProtocolPluggableServiceImpl(
            OrmService ormService,
            EventService eventService,
            NlsService nlsService,
            IssueService issueService,
            PropertySpecService propertySpecService,
            PluggableService pluggableService,
            RelationService relationService,
            DeviceProtocolService deviceProtocolService,
            DeviceProtocolMessageService deviceProtocolMessageService,
            DeviceProtocolSecurityService deviceProtocolSecurityService,
            InboundDeviceProtocolService inboundDeviceProtocolService,
            ConnectionTypeService connectionTypeService,
            DeviceCacheMarshallingService deviceCacheMarshallingService,
            LicenseService licenseService,
            LicensedProtocolService licensedProtocolService,
            UserService userService) {
        this();
        this.setOrmService(ormService);
        this.setEventService(eventService);
        this.setNlsService(nlsService);
        this.setIssueService(issueService);
        this.setPropertySpecService(propertySpecService);
        this.setRelationService(relationService);
        this.setPluggableService(pluggableService);
        this.setUserService(userService);
        this.addDeviceProtocolService(deviceProtocolService);
        this.addDeviceProtocolMessageService(deviceProtocolMessageService);
        this.addDeviceProtocolSecurityService(deviceProtocolSecurityService);
        this.addInboundDeviceProtocolService(inboundDeviceProtocolService);
        this.addConnectionTypeService(connectionTypeService);
        this.addDeviceCacheMarshallingService(deviceCacheMarshallingService);
        this.setLicenseService(licenseService);
        this.addLicensedProtocolService(licensedProtocolService);
        this.activate();
        this.install();
    }

    @Override
    public Class<?> loadProtocolClass(String javaClassName) {
        for (DeviceProtocolService service : this.deviceProtocolServices) {
            try {
                return service.loadProtocolClass(javaClassName);
            } catch (ProtocolCreationException e) {
                // Try the next DeviceProtocolService
            }
        }
        throw new NoServiceFoundThatCanLoadTheJavaClass(javaClassName);
    }

    @Override
    public Object createDeviceProtocolMessagesFor(String javaClassName) {
        for (DeviceProtocolMessageService service : this.deviceProtocolMessageServices) {
            try {
                return service.createDeviceProtocolMessagesFor(javaClassName);
            } catch (ProtocolCreationException e) {
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
            } catch (DeviceProtocolAdapterCodingExceptions e) {
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
        for (PropertySpec<?> propertySpec : deviceProtocolPluggableClass.getDeviceProtocol().getPropertySpecs()) {
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
        } else {
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
        return new WrappingFinder<DeviceProtocolPluggableClass, PluggableClass>(this.pluggableService.findAllByType(PluggableClassType.DeviceProtocol).defaultSortColumn("name")) {
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
        } else {
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
        for (PropertySpec<?> propertySpec : inboundDeviceProtocolPluggableClass.getInboundDeviceProtocol().getPropertySpecs()) {
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
        for (PropertySpec<?> propertySpec : connectionTypePluggableClass.getConnectionType().getPropertySpecs()) {
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
    public void setPluggableService(PluggableService pluggableService) {
        this.pluggableService = pluggableService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addDeviceProtocolService(DeviceProtocolService deviceProtocolService) {
        this.deviceProtocolServices.add(deviceProtocolService);
    }

    @SuppressWarnings("unused")
    public void removeDeviceProtocolService(DeviceProtocolService deviceProtocolService) {
        this.deviceProtocolServices.remove(deviceProtocolService);
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

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addLicensedProtocolService(LicensedProtocolService licensedProtocolService) {
        this.licensedProtocolServices.add(licensedProtocolService);
        if (active) {
            registerDeviceProtocolPluggableClasses();
        }
    }

    @SuppressWarnings("unused")
    public void removeLicensedProtocolService(LicensedProtocolService licensedProtocolService) {
        this.licensedProtocolServices.remove(licensedProtocolService);
    }

    public List<InboundDeviceProtocolService> getInboundDeviceProtocolService() {
        return inboundDeviceProtocolServices;
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addInboundDeviceProtocolService(InboundDeviceProtocolService inboundDeviceProtocolService) {
        this.inboundDeviceProtocolServices.add(inboundDeviceProtocolService);
        if (active) {
            registerInboundDeviceProtocolPluggableClasses();
        }
    }

    @SuppressWarnings("unused")
    public void removeInboundDeviceProtocolService(InboundDeviceProtocolService inboundDeviceProtocolService) {
        this.inboundDeviceProtocolServices.remove(inboundDeviceProtocolService);
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

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addConnectionTypeService(ConnectionTypeService connectionTypeService) {
        this.connectionTypeServices.add(connectionTypeService);
        if (active) {
            registerConnectionTypePluggableClasses();
        }
    }

    @SuppressWarnings("unused")
    public void removeConnectionTypeService(ConnectionTypeService connectionTypeService) {
        this.connectionTypeServices.remove(connectionTypeService);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addDeviceCacheMarshallingService(DeviceCacheMarshallingService deviceCacheMarshallingService) {
        this.deviceCacheMarshallingServices.add(deviceCacheMarshallingService);
    }

    @SuppressWarnings("unused")
    public void removeDeviceCacheMarshallingService(DeviceCacheMarshallingService deviceCacheMarshallingService) {
        this.deviceCacheMarshallingServices.remove(deviceCacheMarshallingService);
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
        if (active) {
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
        for (ConnectionTypeService connectionTypeService : getConnectionTypeServices()) {
            try {
                connectionType = connectionTypeService.createConnectionType(javaClassName);
            } catch (Exception e) {
                throwable = e;
                // silently ignore, will try other service
            }
        }
        if (connectionType != null) {
            return connectionType;
        } else {
            throw new UnableToCreateConnectionType(throwable, javaClassName);

        }
    }

    @Override
    public InboundDeviceProtocol createInboundDeviceProtocolFor(PluggableClass pluggableClass) {
        InboundDeviceProtocol inboundDeviceProtocol = null;
        Exception throwable = null;
        for (InboundDeviceProtocolService inboundDeviceProtocolService : getInboundDeviceProtocolService()) {
            try {
                inboundDeviceProtocol = inboundDeviceProtocolService.createInboundDeviceProtocolFor(pluggableClass);
            } catch (Exception e) {
                throwable = e;
                // silently ignore, will try other service
            }
        }
        if (inboundDeviceProtocol != null) {
            return inboundDeviceProtocol;
        } else {
            throw DeviceProtocolAdapterCodingExceptions.genericReflectionError(MessageSeeds.GENERIC_JAVA_REFLECTION_ERROR, throwable, pluggableClass.getJavaClassName());
        }
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
                bind(ProtocolPluggableService.class).toInstance(ProtocolPluggableServiceImpl.this);
                bind(SecuritySupportAdapterMappingFactory.class).to(SecuritySupportAdapterMappingFactoryImpl.class);
                bind(LicenseService.class).toInstance(licenseService);
                bind(UserService.class).toInstance(userService);
            }
        };
    }

    @Activate
    public void activate() {
        //TODO need a proper implementation of the DataVault!
        DataVaultProvider.instance.set(new TemporaryUnSecureDataVaultProvider());
        this.dataModel.register(this.getModule());
        this.active = this.dataModel.isInstalled();
        this.registerAllPluggableClasses();
    }

    private void registerAllPluggableClasses() {
        if (active) {
            this.registerInboundDeviceProtocolPluggableClasses();
            this.registerDeviceProtocolPluggableClasses();
            this.registerConnectionTypePluggableClasses();
        }
    }

    private void registerDeviceProtocolPluggableClasses() {
        new DeviceProtocolPluggableClassRegistrar(this, this.transactionService).registerAll(this.getAllLicensedProtocols());
    }

    private void registerInboundDeviceProtocolPluggableClasses() {
        new InboundDeviceProtocolPluggableClassRegistrar(this, this.transactionService).
                registerAll(Collections.unmodifiableList(this.inboundDeviceProtocolServices));
    }

    private void registerConnectionTypePluggableClasses() {
        new ConnectionTypePluggableClassRegistrar(this, this.transactionService).
                registerAll(Collections.unmodifiableList(this.connectionTypeServices));
    }

    @Deactivate
    public void deactivate() {
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Override
    public void install() {
        new Installer(this.dataModel, this.eventService, this.thesaurus, this.userService).install(true, true);
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("ORM", "USR", "EVT", "NLS");
    }

    private class TemporaryUnSecureDataVaultProvider implements DataVaultProvider {

        private DataVault soleInstance;

        @Override
        public DataVault getKeyVault() {
            if (soleInstance == null) {
                soleInstance = new StraightForwardUnSecureDataVault();
            }
            return soleInstance;
        }
    }

    /**
     * An unsecure DataVault. The encrypt will just make a String from the given bytes and the decrypt will do the reverse!.
     * <b>NOT SECURE FOR IN PRODUCTION</b>
     *
     * @see <a href="http://jira.eict.vpdc/browse/JP-3879">JP-3879</a>
     */
    private class StraightForwardUnSecureDataVault implements DataVault {

        @Override
        public String encrypt(byte[] decrypted) {
            return new String(decrypted);
        }

        @Override
        public byte[] decrypt(String encrypted) {
            if (encrypted != null) {
                return encrypted.getBytes();
            } else {
                return new byte[0];
            }
        }

        @Override
        public void createVault(File file) {

        }
    }
}