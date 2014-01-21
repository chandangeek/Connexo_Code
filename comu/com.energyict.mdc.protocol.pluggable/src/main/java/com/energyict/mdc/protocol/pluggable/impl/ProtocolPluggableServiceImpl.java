package com.energyict.mdc.protocol.pluggable.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.NotFoundException;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.relation.RelationAttributeType;
import com.energyict.mdc.dynamic.relation.RelationService;
import com.energyict.mdc.dynamic.relation.RelationType;
import com.energyict.mdc.pluggable.PluggableClass;
import com.energyict.mdc.pluggable.PluggableClassType;
import com.energyict.mdc.pluggable.PluggableService;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.services.ConnectionTypeService;
import com.energyict.mdc.protocol.api.services.DeviceProtocolService;
import com.energyict.mdc.protocol.api.services.InboundDeviceProtocolService;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.DeviceProtocolDialectUsagePluggableClass;
import com.energyict.mdc.protocol.pluggable.InboundDeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.SecuritySupportAdapterMappingFactory;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.SecuritySupportAdapterMappingFactoryImpl;
import com.energyict.mdc.protocol.pluggable.impl.relations.SecurityPropertySetRelationTypeSupport;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides an interface for the {@link ProtocolPluggableService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-23 (13:47)
 */
@Component(name="com.energyict.mdc.protocol.pluggable", service = {ProtocolPluggableService.class, InstallService.class})
public class ProtocolPluggableServiceImpl implements ProtocolPluggableService, InstallService {

    private volatile DataModel dataModel;
    private volatile TransactionService transactionService;
    private volatile EventService eventService;
    private volatile PropertySpecService propertySpecService;
    private volatile PluggableService pluggableService;
    private volatile RelationService relationService;
    private volatile DeviceProtocolService deviceProtocolService;
    private volatile InboundDeviceProtocolService inboundDeviceProtocolService;
    private volatile ConnectionTypeService connectionTypeService;

    public ProtocolPluggableServiceImpl() {
        super();
    }

    @Inject
    public ProtocolPluggableServiceImpl(
            OrmService ormService,
            TransactionService transactionService,
            EventService eventService,
            PropertySpecService propertySpecService,
            PluggableService pluggableService,
            RelationService relationService,
            DeviceProtocolService deviceProtocolService,
            InboundDeviceProtocolService inboundDeviceProtocolService,
            ConnectionTypeService connectionTypeService) {
        this();
        this.setOrmService(ormService);
        this.setTransactionService(transactionService);
        this.setEventService(eventService);
        this.setRelationService(relationService);
        this.setPluggableService(pluggableService);
        this.setDeviceProtocolService(deviceProtocolService);
        this.setInboundDeviceProtocolService(inboundDeviceProtocolService);
        this.setConnectionTypeService(connectionTypeService);
        this.activate();
        if (!this.dataModel.isInstalled()) {
            this.install();
        }
    }

    @Override
    public DeviceProtocolPluggableClass newDeviceProtocolPluggableClass(String name, String className) throws BusinessException {
        final PluggableClass pluggableClass = this.pluggableService.newPluggableClass(PluggableClassType.DeviceProtocol, name, className);
        final DeviceProtocolPluggableClassImpl deviceProtocolPluggableClass = DeviceProtocolPluggableClassImpl.from(this.dataModel, pluggableClass);
        return this.transactionService.execute(new Transaction<DeviceProtocolPluggableClass>() {
            @Override
            public DeviceProtocolPluggableClass perform() {
                try {
                    pluggableClass.save();
                    deviceProtocolPluggableClass.save();
                }
                catch (BusinessException | SQLException e) {
                    throw new ApplicationException(e);
                }
                return deviceProtocolPluggableClass;
            }
        });
    }

    @Override
    public DeviceProtocolPluggableClass newDeviceProtocolPluggableClass(String name, String className, TypedProperties properties) throws BusinessException {
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = this.newDeviceProtocolPluggableClass(name, className);
        for (PropertySpec propertySpec : deviceProtocolPluggableClass.getDeviceProtocol().getPropertySpecs()) {
            if (properties.hasValueFor(propertySpec.getName())) {
                deviceProtocolPluggableClass.setProperty(propertySpec, properties.getProperty(propertySpec.getName()));
            }
        }
        try {
            deviceProtocolPluggableClass.save();
            return deviceProtocolPluggableClass;
        }
        catch (BusinessException | SQLException e) {
            throw new ApplicationException(e);
        }
    }

    @Override
    public List<DeviceProtocolPluggableClass> findAllDeviceProtocolPluggableClasses() {
        List<PluggableClass> pluggableClasses = this.pluggableService.findAllByType(PluggableClassType.DeviceProtocol);
        List<DeviceProtocolPluggableClass> deviceProtocolPluggableClasses = new ArrayList<>(pluggableClasses.size());
        for (PluggableClass pluggableClass : pluggableClasses) {
            deviceProtocolPluggableClasses.add(DeviceProtocolPluggableClassImpl.from(this.dataModel, pluggableClass));
        }
        return deviceProtocolPluggableClasses;
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
    public List<DeviceProtocolPluggableClass> findDeviceProtocolPluggableClass(String className) {
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
            try {
                deviceProtocolPluggableClass.delete();
            }
            catch (BusinessException | SQLException e) {
                throw new ApplicationException(e);
            }
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
            try {
                inboundDeviceProtocolPluggableClass.delete();
            }
            catch (BusinessException | SQLException e) {
                throw new ApplicationException(e);
            }
        }
    }

    @Override
    public List<InboundDeviceProtocolPluggableClass> findAllInboundDeviceProtocolPluggableClass() {
        List<PluggableClass> pluggableClasses = this.pluggableService.findAllByType(PluggableClassType.DiscoveryProtocol);
        List<InboundDeviceProtocolPluggableClass> inboundDeviceProtocolPluggableClasses = new ArrayList<>(pluggableClasses.size());
        for (PluggableClass pluggableClass : pluggableClasses) {
            inboundDeviceProtocolPluggableClasses.add(InboundDeviceProtocolPluggableClassImpl.from(this.dataModel, pluggableClass));
        }
        return inboundDeviceProtocolPluggableClasses;
    }

    @Override
    public InboundDeviceProtocolPluggableClass newInboundDeviceProtocolPluggableClass(String name, String javaClassName) throws BusinessException {
        final PluggableClass pluggableClass = this.pluggableService.newPluggableClass(PluggableClassType.DiscoveryProtocol, name, javaClassName);
        final InboundDeviceProtocolPluggableClassImpl inboundDeviceProtocolPluggableClass = InboundDeviceProtocolPluggableClassImpl.from(this.dataModel, pluggableClass);
        return this.transactionService.execute(new Transaction<InboundDeviceProtocolPluggableClass>() {
            @Override
            public InboundDeviceProtocolPluggableClass perform() {
                try {
                    pluggableClass.save();
                    inboundDeviceProtocolPluggableClass.save();
                }
                catch (BusinessException | SQLException e) {
                    throw new ApplicationException(e);
                }
                return inboundDeviceProtocolPluggableClass;
            }
        });
    }

    @Override
    public InboundDeviceProtocolPluggableClass newInboundDeviceProtocolPluggableClass(String name, String javaClassName, TypedProperties properties) throws BusinessException, SQLException {
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
        List<PluggableClass> pluggableClasses = this.pluggableService.findAllByType(PluggableClassType.ConnectionType);
        List<ConnectionTypePluggableClass> connectionTypePluggableClasses = new ArrayList<>(pluggableClasses.size());
        for (PluggableClass pluggableClass : pluggableClasses) {
            connectionTypePluggableClasses.add(ConnectionTypePluggableClassImpl.from(this.dataModel, pluggableClass));
        }
        return connectionTypePluggableClasses;
    }

    @Override
    public ConnectionTypePluggableClass newConnectionTypePluggableClass(String name, String javaClassName) throws BusinessException {
        final PluggableClass pluggableClass = this.pluggableService.newPluggableClass(PluggableClassType.ConnectionType, name, javaClassName);
        final ConnectionTypePluggableClassImpl connectionTypePluggableClass = ConnectionTypePluggableClassImpl.from(this.dataModel, pluggableClass);
        return this.transactionService.execute(new Transaction<ConnectionTypePluggableClass>() {
            @Override
            public ConnectionTypePluggableClass perform() {
                try {
                    pluggableClass.save();
                    connectionTypePluggableClass.save();
                }
                catch (BusinessException | SQLException e) {
                    throw new ApplicationException(e);
                }
                return connectionTypePluggableClass;
            }
        });
    }

    @Override
    public ConnectionTypePluggableClass newConnectionTypePluggableClass(String name, String javaClassName, TypedProperties properties) throws BusinessException, SQLException {
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
    public String createOriginalAndConformRelationNameBasedOnJavaClassname(Class clazz) {
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

    DataModel getDataModel() {
        return dataModel;
    }

    public TransactionService getTransactionService() {
        return transactionService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    public EventService getEventService() {
        return eventService;
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    public PluggableService getPluggableService() {
        return pluggableService;
    }

    @Reference
    public void setPluggableService(PluggableService pluggableService) {
        this.pluggableService = pluggableService;
    }

    public DeviceProtocolService getDeviceProtocolService() {
        return deviceProtocolService;
    }

    @Reference
    public void setDeviceProtocolService(DeviceProtocolService deviceProtocolService) {
        this.deviceProtocolService = deviceProtocolService;
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

    private Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(EventService.class).toInstance(eventService);
                bind(PropertySpecService.class).toInstance(propertySpecService);
                bind(PluggableService.class).toInstance(pluggableService);
                bind(RelationService.class).toInstance(relationService);
                bind(DeviceProtocolService.class).toInstance(deviceProtocolService);
                bind(InboundDeviceProtocolService.class).toInstance(inboundDeviceProtocolService);
                bind(ConnectionTypeService.class).toInstance(connectionTypeService);
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
        new Installer(this.dataModel, this.eventService).install(true, true, true);
    }

}