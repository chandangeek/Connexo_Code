package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.streams.Functions;
import com.energyict.mdc.common.CanFindByLongPrimaryKey;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.*;
import com.energyict.mdc.device.data.exceptions.DeviceConfigurationChangeException;
import com.energyict.mdc.device.data.exceptions.NoDestinationSpecFound;
import com.energyict.mdc.device.data.impl.configchange.*;
import com.energyict.mdc.device.data.ComTaskExecutionFields;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.DeviceFields;
import com.energyict.mdc.device.data.DeviceProtocolProperty;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.finders.DeviceFinder;
import com.energyict.mdc.device.data.impl.finders.DeviceGroupFinder;
import com.energyict.mdc.device.data.impl.finders.ProtocolDialectPropertiesFinder;
import com.energyict.mdc.device.data.impl.finders.SecuritySetFinder;
import com.energyict.mdc.device.data.impl.finders.ServiceCategoryFinder;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecution;
import com.energyict.mdc.pluggable.PluggableClass;
import com.energyict.mdc.protocol.api.CommonDeviceProtocolDialectProperties;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolDialectPropertyProvider;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.scheduling.model.ComSchedule;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.util.HasId;
import org.osgi.service.event.EventConstants;

import javax.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;

import static com.elster.jupiter.util.conditions.Where.where;

/**
 * Provides an implementation for the {@link DeviceService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-10 (16:27)
 */
public class DeviceServiceImpl implements ServerDeviceService {

    private final DeviceDataModelService deviceDataModelService;
    private final ProtocolPluggableService protocolPluggableService;
    private final MeteringGroupsService meteringGroupsService;
    private final MeteringService meteringService;
    private final QueryService queryService;
    private final Thesaurus thesaurus;

    @Inject
    public DeviceServiceImpl(DeviceDataModelService deviceDataModelService, ProtocolPluggableService protocolPluggableService, QueryService queryService, NlsService nlsService, MeteringGroupsService meteringGroupsService, MeteringService meteringService) {
        this(deviceDataModelService, protocolPluggableService, queryService, nlsService.getThesaurus(DeviceDataServices.COMPONENT_NAME, Layer.DOMAIN), meteringGroupsService, meteringService);
    }

    DeviceServiceImpl(DeviceDataModelService deviceDataModelService, ProtocolPluggableService protocolPluggableService, QueryService queryService, Thesaurus thesaurus, MeteringGroupsService meteringGroupsService, MeteringService meteringService) {
        super();
        this.deviceDataModelService = deviceDataModelService;
        this.protocolPluggableService = protocolPluggableService;
        this.queryService = queryService;
        this.thesaurus = thesaurus;
        this.meteringGroupsService = meteringGroupsService;
        this.meteringService = meteringService;
    }

    @Override
    public List<CanFindByLongPrimaryKey<? extends HasId>> finders() {
        List<CanFindByLongPrimaryKey<? extends HasId>> finders = new ArrayList<>();
        finders.add(new DeviceFinder(this.deviceDataModelService.dataModel()));
        finders.add(new ProtocolDialectPropertiesFinder(this.deviceDataModelService.dataModel()));
        finders.add(new SecuritySetFinder(this.deviceDataModelService.deviceConfigurationService()));
        finders.add(new DeviceGroupFinder(this.meteringGroupsService));
        finders.add(new ServiceCategoryFinder(this.meteringService));
        return finders;
    }

    @Override
    public boolean hasDevices(DeviceConfiguration deviceConfiguration) {
        Condition condition = where(DeviceFields.DEVICECONFIGURATION.fieldName()).isEqualTo(deviceConfiguration);
        Finder<Device> page =
                DefaultFinder.
                        of(Device.class, condition, this.deviceDataModelService.dataModel()).
                        paged(0, 1);
        List<Device> allDevices = page.find();
        return !allDevices.isEmpty();
    }

    @Override
    public boolean hasDevices(ProtocolDialectConfigurationProperties configurationProperties) {
        return this.count(this.hasDevicesSqlBuilder(configurationProperties)) > 0;
    }

    @Override
    public long countDevicesThatRelyOnRequiredProperty(ProtocolDialectConfigurationProperties configurationProperties, PropertySpec propertySpec) {
        SqlBuilder nullValueSqlBuilder = this.hasDevicesSqlBuilder(configurationProperties);
        nullValueSqlBuilder.append(" and ");
        nullValueSqlBuilder.append(propertySpec.getName());
        nullValueSqlBuilder.append(" is null ");

        SqlBuilder noPropertiesSqlBuilder = new SqlBuilder("select count(*) from ");
        noPropertiesSqlBuilder.append(TableSpecs.DDC_DEVICE.name());
        noPropertiesSqlBuilder.append(" dev where dev.deviceconfigid =");
        noPropertiesSqlBuilder.addLong(configurationProperties.getDeviceConfiguration().getId());
        noPropertiesSqlBuilder.append("and not exists (select *");
        this.appendCountDevicesSql(configurationProperties, noPropertiesSqlBuilder);
        noPropertiesSqlBuilder.append(" and props.deviceid = dev.id)");
        return this.count(nullValueSqlBuilder) + count(noPropertiesSqlBuilder);
    }

    private SqlBuilder hasDevicesSqlBuilder(ProtocolDialectConfigurationProperties configurationProperties) {
        SqlBuilder sqlBuilder = new SqlBuilder("select count(*) ");
        this.appendCountDevicesSql(configurationProperties, sqlBuilder);
        return sqlBuilder;
    }

    private void appendCountDevicesSql(ProtocolDialectConfigurationProperties configurationProperties, SqlBuilder sqlBuilder) {
        Instant now = this.deviceDataModelService.clock().instant();
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = configurationProperties.getDeviceConfiguration().getDeviceType().getDeviceProtocolPluggableClass();
        Optional<CustomPropertySet<DeviceProtocolDialectPropertyProvider, ? extends PersistentDomainExtension<DeviceProtocolDialectPropertyProvider>>> customPropertySet =
                this.getCustomPropertySet(deviceProtocolPluggableClass.getDeviceProtocol(), configurationProperties.getDeviceProtocolDialectName());
        if (customPropertySet.isPresent()) {
            String propertiesTable = customPropertySet.get().getPersistenceSupport().tableName();
            sqlBuilder.append(" from ");
            sqlBuilder.append(propertiesTable);
            sqlBuilder.append(" cps join ");
            sqlBuilder.append(TableSpecs.DDC_PROTOCOLDIALECTPROPS.name());
            sqlBuilder.append(" props on cps.");
            sqlBuilder.append(CommonDeviceProtocolDialectProperties.Fields.DIALECT_PROPERTY_PROVIDER.databaseName());
            sqlBuilder.append(" = props.id where props.configurationpropertiesid =");
            sqlBuilder.addLong(configurationProperties.getId());
            sqlBuilder.append("and (fromdate <=");
            sqlBuilder.addLong(now.getEpochSecond());
            sqlBuilder.append(" and (todate is null or todate >");
            sqlBuilder.addLong(now.getEpochSecond());
            sqlBuilder.append("))");
        }
        else {
            sqlBuilder.append(" from dual where 1 = 0");
        }
    }

    private Optional<CustomPropertySet<DeviceProtocolDialectPropertyProvider, ? extends PersistentDomainExtension<DeviceProtocolDialectPropertyProvider>>> getCustomPropertySet(DeviceProtocol deviceProtocol, String name) {
        return deviceProtocol
                    .getDeviceProtocolDialects()
                    .stream()
                    .filter(dialect -> dialect.getDeviceProtocolDialectName().equals(name))
                    .map(DeviceProtocolDialect::getCustomPropertySet)
                    .flatMap(Functions.asStream())
                    .findAny();
    }

    private long count(SqlBuilder sqlBuilder) {
        try (PreparedStatement statement = sqlBuilder.prepare(this.deviceDataModelService.dataModel().getConnection(false))) {
            try (ResultSet counter = statement.executeQuery()) {
                counter.next();
                return counter.getLong(1);
            }
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    @Override
    public Device newDevice(DeviceConfiguration deviceConfiguration, String name, String mRID) {
        Device device = this.deviceDataModelService.dataModel().getInstance(DeviceImpl.class).initialize(deviceConfiguration, name, mRID);
        device.save(); // always returns a persisted device
        return device;
    }

    @Override
    public Device newDevice(DeviceConfiguration deviceConfiguration, String name, String mRID, String batch) {
        Device device = newDevice(deviceConfiguration, name, mRID);
        this.deviceDataModelService.batchService().findOrCreateBatch(batch).addDevice(device);
        return device;
    }

    @Override
    public Optional<Device> findDeviceById(long id) {
        return this.deviceDataModelService.dataModel().mapper(Device.class).getUnique("id", id);
    }

    @Override
    public Optional<Device> findAndLockDeviceByIdAndVersion(long id, long version) {
        return this.deviceDataModelService.dataModel().mapper(Device.class).lockObjectIfVersion(version, id);
    }

    @Override
    public Optional<Device> findAndLockDeviceBymRIDAndVersion(String mRID, long version) {
        Optional<Device> deviceOptional = this.deviceDataModelService.dataModel().mapper(Device.class).getUnique(DeviceFields.MRID.fieldName(), mRID);
        if (deviceOptional.isPresent()) {
            return this.deviceDataModelService.dataModel().mapper(Device.class).lockObjectIfVersion(version, deviceOptional.get().getId());
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Device> findByUniqueMrid(String mrId) {
        return this.deviceDataModelService.dataModel().mapper(Device.class).getUnique(DeviceFields.MRID.fieldName(), mrId);
    }

    @Override
    public List<Device> findDevicesBySerialNumber(String serialNumber) {
        return this.deviceDataModelService.dataModel().mapper(Device.class).find("serialNumber", serialNumber);
    }

    @Override
    public Finder<Device> findAllDevices(Condition condition) {
        return DefaultFinder.of(Device.class, condition, this.deviceDataModelService.dataModel(), DeviceConfiguration.class, DeviceType.class).
                defaultSortColumn("name").
                maxPageSize(thesaurus, 1000);
    }

    @Override
    public boolean isLinkedToDevices(ComSchedule comSchedule) {
        Condition condition = where(ComTaskExecutionFields.COM_SCHEDULE.fieldName()).isEqualTo(comSchedule).and(where(ComTaskExecutionFields.OBSOLETEDATE.fieldName()).isNull());
        List<ScheduledComTaskExecution> scheduledComTaskExecutions = this.deviceDataModelService.dataModel().query(ScheduledComTaskExecution.class).
                select(condition, new Order[0], false, new String[0], 1, 1);
        return !scheduledComTaskExecutions.isEmpty();
    }

    @Override
    public Finder<Device> findDevicesByDeviceConfiguration(DeviceConfiguration deviceConfiguration) {
        return DefaultFinder.of(Device.class, where("deviceConfiguration").isEqualTo(deviceConfiguration), this.deviceDataModelService.dataModel()).defaultSortColumn("lower(name)");
    }

    @Override
    public List<Device> findDevicesByPropertySpecValue(String propertySpecName, String propertySpecValue) {
        Condition condition = where("deviceProperties.propertySpec").isEqualTo(propertySpecName).and(where("deviceProperties.propertyValue").isEqualTo(propertySpecValue));
        return this.deviceDataModelService.dataModel().query(Device.class, DeviceProtocolProperty.class).select(condition);
    }

    @Override
    public List<Device> findDevicesByConnectionTypeAndProperty(Class<? extends ConnectionType> connectionTypeClass, String propertyName, String propertyValue) {
//        Condition condition = where("connectionTasks.pluggableClass.pluggableClass.javaClassName").isEqualTo(connectionTypeClass.getClass().getName());
        //TODO complete it!
        Condition condition = where("connectionTasks.pluggableClass.pluggableClass.javaClassName").isEqualTo("com.energyict.protocols.impl.channels.ip.socket.OutboundTcpIpConnectionType");
        return this.deviceDataModelService.dataModel().query(Device.class, ConnectionTask.class, ConnectionTypePluggableClass.class, PluggableClass.class).select(condition);
    }

    @Override
    public Query<Device> deviceQuery() {
        return queryService.wrap(deviceDataModelService.dataModel().query(Device.class, DeviceConfiguration.class, DeviceType.class));
    }

    @Override
    public Device changeDeviceConfigurationForSingleDevice(long deviceId, long deviceVersion, long destinationDeviceConfigId, long destinationDeviceConfigVersion) {
        Pair<Device, DeviceConfigChangeRequestImpl> lockResult = deviceDataModelService.getTransactionService().execute(() -> {
            Device device = findAndLockDeviceByIdAndVersion(deviceId, deviceVersion).orElseThrow(DeviceConfigurationChangeException.noDeviceFoundForVersion(thesaurus, deviceId, deviceVersion));
            final DeviceConfiguration deviceConfiguration = deviceDataModelService.deviceConfigurationService()
                    .findAndLockDeviceConfigurationByIdAndVersion(destinationDeviceConfigId, destinationDeviceConfigVersion)
                    .orElseThrow(DeviceConfigurationChangeException.noDestinationConfigFoundForVersion(thesaurus, destinationDeviceConfigId, destinationDeviceConfigVersion));
            final DeviceConfigChangeRequestImpl deviceConfigChangeRequest = deviceDataModelService.dataModel().getInstance(DeviceConfigChangeRequestImpl.class).init(deviceConfiguration);
            deviceConfigChangeRequest.save();
            return Pair.of(device, deviceConfigChangeRequest);
        });

        Device modifiedDevice = null;
        try {
            modifiedDevice = deviceDataModelService.getTransactionService().execute(() -> new DeviceConfigChangeExecutor(this).execute((DeviceImpl) lockResult.getFirst(), deviceDataModelService.deviceConfigurationService().findDeviceConfiguration(destinationDeviceConfigId).get()));
        } finally {
            deviceDataModelService.getTransactionService().execute(VoidTransaction.of(lockResult.getLast()::notifyDeviceInActionIsRemoved));
        }
        return modifiedDevice;
    }

    @Override
    public void changeDeviceConfigurationForDevices(DeviceConfiguration destinationDeviceConfiguration, DevicesForConfigChangeSearch devicesForConfigChangeSearch, String... deviceMRIDs) {
        final DeviceConfigChangeRequestImpl deviceConfigChangeRequest = deviceDataModelService.dataModel().getInstance(DeviceConfigChangeRequestImpl.class).init(destinationDeviceConfiguration);
        deviceConfigChangeRequest.save();
        ItemizeConfigChangeQueueMessage itemizeConfigChangeQueueMessage = new ItemizeConfigChangeQueueMessage(destinationDeviceConfiguration.getId(), Arrays.asList(deviceMRIDs), devicesForConfigChangeSearch, deviceConfigChangeRequest.getId());

        DestinationSpec destinationSpec = deviceDataModelService.messageService().getDestinationSpec(ServerDeviceForConfigChange.CONFIG_CHANGE_BULK_QUEUE_DESTINATION).orElseThrow(new NoDestinationSpecFound(thesaurus, ServerDeviceForConfigChange.CONFIG_CHANGE_BULK_QUEUE_DESTINATION));
        Map<String, Object> message = createConfigChangeQueueMessage(itemizeConfigChangeQueueMessage);
        destinationSpec.message(deviceDataModelService.jsonService().serialize(message)).send();
    }

    private Map<String, Object> createConfigChangeQueueMessage(ItemizeConfigChangeQueueMessage itemizeConfigChangeQueueMessage) {
        Map<String, Object> message = new HashMap<>(2);
        message.put(EventConstants.EVENT_TOPIC, ServerDeviceForConfigChange.DEVICE_CONFIG_CHANGE_BULK_SETUP_ACTION);
        message.put(ServerDeviceForConfigChange.CONFIG_CHANGE_MESSAGE_VALUE, deviceDataModelService.jsonService().serialize(itemizeConfigChangeQueueMessage));
        return message;
    }

    @Override
    public boolean hasActiveDeviceConfigChangesFor(DeviceConfiguration originDeviceConfiguration, DeviceConfiguration destinationDeviceConfiguration) {
        return this.deviceDataModelService.dataModel()
                .stream(DeviceConfigChangeRequest.class)
                .filter(Where.where(DeviceConfigChangeRequestImpl.Fields.DEVICE_CONFIG_REFERENCE.fieldName()).in(Arrays.asList(originDeviceConfiguration, destinationDeviceConfiguration)))
                .findAny().isPresent();
    }

    @Override
    public Optional<DeviceConfigChangeRequest> findDeviceConfigChangeRequestById(long id) {
        return deviceDataModelService.dataModel().mapper(DeviceConfigChangeRequest.class).getUnique("id", id);
    }

    @Override
    public Optional<DeviceConfigChangeInAction> findDeviceConfigChangeInActionById(long id) {
        return deviceDataModelService.dataModel().mapper(DeviceConfigChangeInAction.class).getUnique("id", id);
    }
}