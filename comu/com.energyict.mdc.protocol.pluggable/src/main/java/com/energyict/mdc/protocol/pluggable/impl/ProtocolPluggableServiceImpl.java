package com.energyict.mdc.protocol.pluggable.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.energyict.mdc.common.NotFoundException;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.common.services.WrappingFinder;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.relation.RelationAttributeType;
import com.energyict.mdc.dynamic.relation.RelationService;
import com.energyict.mdc.dynamic.relation.RelationType;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.pluggable.PluggableClass;
import com.energyict.mdc.pluggable.PluggableClassType;
import com.energyict.mdc.pluggable.PluggableService;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.exceptions.DeviceProtocolAdapterCodingExceptions;
import com.energyict.mdc.protocol.api.exceptions.ProtocolCreationException;
import com.energyict.mdc.protocol.api.services.ConnectionTypeService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolMessageService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolSecurityService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.mdc.protocol.api.services.InboundDeviceProtocolService;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.DeviceProtocolDialectUsagePluggableClass;
import com.energyict.mdc.protocol.pluggable.InboundDeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.UnknownPluggableClassPropertiesException;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.SecuritySupportAdapterMappingFactory;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.SecuritySupportAdapterMappingFactoryImpl;
import com.energyict.mdc.protocol.pluggable.impl.relations.SecurityPropertySetRelationTypeSupport;
import com.google.common.base.Optional;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.inject.Inject;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * Provides an interface for the {@link ProtocolPluggableService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-23 (13:47)
 */
@Component(name="com.energyict.mdc.protocol.pluggable", service = {ProtocolPluggableService.class, InstallService.class}, property = "name=" + ProtocolPluggableService.COMPONENTNAME)
public class ProtocolPluggableServiceImpl implements ProtocolPluggableService, InstallService {

    private volatile DataModel dataModel;
    private volatile EventService eventService;
    private volatile Thesaurus thesaurus;
    private volatile PropertySpecService propertySpecService;
    private volatile PluggableService pluggableService;
    private volatile RelationService relationService;
    private volatile List<DeviceProtocolService> deviceProtocolServices = new CopyOnWriteArrayList<>();
    private volatile List<DeviceProtocolMessageService> deviceProtocolMessageServices = new CopyOnWriteArrayList<>();
    private volatile List<DeviceProtocolSecurityService> deviceProtocolSecurityServices = new CopyOnWriteArrayList<>();
    private volatile InboundDeviceProtocolService inboundDeviceProtocolService;
    private volatile ConnectionTypeService connectionTypeService;
    private volatile IssueService issueService;

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
            ConnectionTypeService connectionTypeService) {
        this();
        this.setOrmService(ormService);
        this.setEventService(eventService);
        this.setNlsService(nlsService);
        this.setIssueService(issueService);
        this.setPropertySpecService(propertySpecService);
        this.setRelationService(relationService);
        this.setPluggableService(pluggableService);
        this.addDeviceProtocolService(deviceProtocolService);
        this.addDeviceProtocolMessageService(deviceProtocolMessageService);
        this.addDeviceProtocolSecurityService(deviceProtocolSecurityService);
        this.setInboundDeviceProtocolService(inboundDeviceProtocolService);
        this.setConnectionTypeService(connectionTypeService);
        this.activate();
        if (!this.dataModel.isInstalled()) {
            this.install(true);
        }
    }

    @Override
    public Class<?> loadProtocolClass(String javaClassName) {
        for (DeviceProtocolService service : this.deviceProtocolServices) {
            try {
                return service.loadProtocolClass(javaClassName);
            }
            catch (ProtocolCreationException e) {
                // Try the next DeviceProtocolService
            }
        }
        throw new ProtocolCreationException(javaClassName);
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
        throw new ProtocolCreationException(javaClassName);
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
        throw new ProtocolCreationException(javaClassName);
    }

    @Override
    public DeviceProtocolPluggableClass newDeviceProtocolPluggableClass(String name, String className) {
        final PluggableClass pluggableClass = this.pluggableService.newPluggableClass(PluggableClassType.DeviceProtocol, name, className);
        final DeviceProtocolPluggableClassImpl deviceProtocolPluggableClass = DeviceProtocolPluggableClassImpl.from(this.dataModel, pluggableClass);
        pluggableClass.save();
        deviceProtocolPluggableClass.save();
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
    public DeviceProtocolPluggableClass findDeviceProtocolPluggableClass(long id) {
        PluggableClass pluggableClass = this.pluggableService.findByTypeAndId(PluggableClassType.DeviceProtocol, id);
        if (pluggableClass == null) {
            return null;
        }
        else {
            return DeviceProtocolPluggableClassImpl.from(this.dataModel, pluggableClass);
        }
    }

    @Override
    public Optional<DeviceProtocolPluggableClass> findDeviceProtocolPluggableClassByName(String name) {
        Optional<PluggableClass> pluggableClass = this.pluggableService.findByTypeAndName(PluggableClassType.DeviceProtocol, name);
        if(pluggableClass.isPresent()){
            DeviceProtocolPluggableClass deviceProtocolPluggableClass = DeviceProtocolPluggableClassImpl.from(this.dataModel, pluggableClass.get());
            return Optional.of(deviceProtocolPluggableClass);
        }
        else  {
            return Optional.absent();
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
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = this.findDeviceProtocolPluggableClass(id);
        if (deviceProtocolPluggableClass == null) {
            throw new NotFoundException("DeviceProtocolPluggableClass with id " + id + " cannot be deleted because it does not exist");
        }
        else {
            deviceProtocolPluggableClass.delete();
        }
    }

    @Override
    public DeviceProtocolDialectUsagePluggableClass getDeviceProtocolDialectUsagePluggableClass(DeviceProtocolPluggableClass pluggableClass, String dialectName) {
        DeviceProtocolDialect deviceProtocolDialect = this.getDeviceProtocolDialectFor(pluggableClass.getDeviceProtocol(), dialectName);
        return new DeviceProtocolDialectUsagePluggableClassImpl(pluggableClass, deviceProtocolDialect, this.dataModel, this.relationService);
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
    public InboundDeviceProtocolPluggableClass findInboundDeviceProtocolPluggableClass(long id) {
        PluggableClass pluggableClass = this.pluggableService.findByTypeAndId(PluggableClassType.DiscoveryProtocol, id);
        if (pluggableClass == null) {
            return null;
        }
        else {
            return InboundDeviceProtocolPluggableClassImpl.from(this.dataModel, pluggableClass);
        }
    }

    @Override
    public void deleteInboundDeviceProtocolPluggableClass(long id) {
        InboundDeviceProtocolPluggableClass inboundDeviceProtocolPluggableClass = this.findInboundDeviceProtocolPluggableClass(id);
        if (inboundDeviceProtocolPluggableClass != null) {
            inboundDeviceProtocolPluggableClass.delete();
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
    public ConnectionTypePluggableClass findConnectionTypePluggableClass(long id) {
        PluggableClass pluggableClass = this.pluggableService.findByTypeAndId(PluggableClassType.ConnectionType, id);
        if (pluggableClass == null) {
            return null;
        }
        else {
            return ConnectionTypePluggableClassImpl.from(this.dataModel, pluggableClass);
        }
    }

    @Override
    public List<ConnectionTypePluggableClass> findAllConnectionTypePluggableClasses() {
        List<PluggableClass> pluggableClasses = this.pluggableService.findAllByType(PluggableClassType.ConnectionType).find();
        List<ConnectionTypePluggableClass> connectionTypePluggableClasses = new ArrayList<>(pluggableClasses.size());
        for (PluggableClass pluggableClass : pluggableClasses) {
            connectionTypePluggableClasses.add(ConnectionTypePluggableClassImpl.from(this.dataModel, pluggableClass));
        }
        return connectionTypePluggableClasses;
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
    public String createConformRelationTypeName (String name) {
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
                        deviceProtocol,
                        deviceProtocolPluggableClass);
        return relationTypeSupport.findRelationType();
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
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(COMPONENTNAME, Layer.DOMAIN);
    }

    public PluggableService getPluggableService() {
        return pluggableService;
    }

    @Reference
    public void setPluggableService(PluggableService pluggableService) {
        this.pluggableService = pluggableService;
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addDeviceProtocolService(DeviceProtocolService deviceProtocolService) {
        this.deviceProtocolServices.add(deviceProtocolService);
    }

    public void removeDeviceProtocolService(DeviceProtocolService deviceProtocolService) {
        this.deviceProtocolServices.remove(deviceProtocolService);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addDeviceProtocolMessageService(DeviceProtocolMessageService deviceProtocolMessageService) {
        this.deviceProtocolMessageServices.add(deviceProtocolMessageService);
    }

    public void removeDeviceProtocolMessageService(DeviceProtocolMessageService deviceProtocolMessageService) {
        this.deviceProtocolMessageServices.remove(deviceProtocolMessageService);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addDeviceProtocolSecurityService(DeviceProtocolSecurityService deviceProtocolSecurityService) {
        this.deviceProtocolSecurityServices.add(deviceProtocolSecurityService);
    }

    public void removeDeviceProtocolSecurityService(DeviceProtocolSecurityService deviceProtocolSecurityService) {
        this.deviceProtocolSecurityServices.remove(deviceProtocolSecurityService);
    }

    public InboundDeviceProtocolService getInboundDeviceProtocolService() {
        return inboundDeviceProtocolService;
    }

    @Reference
    public void setInboundDeviceProtocolService(InboundDeviceProtocolService inboundDeviceProtocolService) {
        this.inboundDeviceProtocolService = inboundDeviceProtocolService;
    }

    public PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    public RelationService getRelationService() {
        return relationService;
    }

    @Reference
    public void setRelationService(RelationService relationService) {
        this.relationService = relationService;
    }

    public ConnectionTypeService getConnectionTypeService() {
        return connectionTypeService;
    }

    @Reference
    public void setConnectionTypeService(ConnectionTypeService connectionTypeService) {
        this.connectionTypeService = connectionTypeService;
    }

    @Reference
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(EventService.class).toInstance(eventService);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(PropertySpecService.class).toInstance(propertySpecService);
                bind(PluggableService.class).toInstance(pluggableService);
                bind(RelationService.class).toInstance(relationService);
                bind(InboundDeviceProtocolService.class).toInstance(inboundDeviceProtocolService);
                bind(ConnectionTypeService.class).toInstance(connectionTypeService);
                bind(IssueService.class).toInstance(issueService);
                bind(ProtocolPluggableService.class).toInstance(ProtocolPluggableServiceImpl.this);
                bind(SecuritySupportAdapterMappingFactory.class).to(SecuritySupportAdapterMappingFactoryImpl.class);
            }
        };
    }

    @Activate
    public void activate() {
        this.dataModel.register(this.getModule());
    }

    @Override
    public void install() {
        this.install(false);
    }

    private void install(boolean executeDdl) {
        new Installer(this.dataModel, this.eventService, this.thesaurus).install(executeDdl, false, true);
    }

}