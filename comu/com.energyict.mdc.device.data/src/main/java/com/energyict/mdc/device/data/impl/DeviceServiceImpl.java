/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.MultiplierType;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.orm.NotUniqueException;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityValueWrapper;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.streams.Functions;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.device.config.AllowedCalendar;
import com.energyict.mdc.common.device.config.ChannelSpec;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.config.PartialInboundConnectionTask;
import com.energyict.mdc.common.device.config.PartialScheduledConnectionTask;
import com.energyict.mdc.common.device.config.RegisterSpec;
import com.energyict.mdc.common.device.data.ActiveEffectiveCalendar;
import com.energyict.mdc.common.device.data.Channel;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.DeviceProtocolProperty;
import com.energyict.mdc.common.device.data.PassiveCalendar;
import com.energyict.mdc.common.device.data.ReadingTypeObisCodeUsage;
import com.energyict.mdc.common.device.data.Register;
import com.energyict.mdc.common.device.data.SecurityAccessor;
import com.energyict.mdc.common.pluggable.PluggableClass;
import com.energyict.mdc.common.protocol.ConnectionType;
import com.energyict.mdc.common.protocol.ConnectionTypePluggableClass;
import com.energyict.mdc.common.protocol.DeviceProtocolDialect;
import com.energyict.mdc.common.protocol.DeviceProtocolDialectPropertyProvider;
import com.energyict.mdc.common.protocol.DeviceProtocolPluggableClass;
import com.energyict.mdc.common.protocol.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.common.scheduling.ComSchedule;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.device.data.ActivatedBreakerStatus;
import com.energyict.mdc.device.data.CreditAmount;
import com.energyict.mdc.device.data.DeviceBuilder;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.DeviceFields;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.DevicesForConfigChangeSearch;
import com.energyict.mdc.device.data.ItemizeConfigChangeQueueMessage;
import com.energyict.mdc.device.data.exceptions.DeviceConfigurationChangeException;
import com.energyict.mdc.device.data.exceptions.NoDestinationSpecFound;
import com.energyict.mdc.device.data.impl.configchange.DeviceConfigChangeExecutor;
import com.energyict.mdc.device.data.impl.configchange.DeviceConfigChangeInAction;
import com.energyict.mdc.device.data.impl.configchange.DeviceConfigChangeRequest;
import com.energyict.mdc.device.data.impl.configchange.DeviceConfigChangeRequestImpl;
import com.energyict.mdc.device.data.impl.configchange.ServerDeviceForConfigChange;
import com.energyict.mdc.device.data.impl.pki.AbstractDeviceSecurityAccessorImpl;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionFields;
import com.energyict.mdc.protocol.LegacyProtocolProperties;
import com.energyict.mdc.protocol.api.CommonDeviceProtocolDialectProperties;
import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.meterdata.BreakerStatus;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.Introspector;

import org.osgi.service.event.EventConstants;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;
import static com.energyict.mdc.device.data.impl.SyncDeviceWithKoreMeter.MULTIPLIER_TYPE;

/**
 * Provides an implementation for the {@link DeviceService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-03-10 (16:27)
 */
@LiteralSql
class DeviceServiceImpl implements ServerDeviceService {

    /**
     * Enum listing up all different Introspector types that can be used in method DeviceServiceImpl#find(com.energyict.mdc.upl.meterdata.identifiers.Introspector)
     */
    public enum IntrospectorTypes {
        SerialNumber("serialNumber"),
        LikeSerialNumber("serialNumberGrepPattern"),
        DatabaseId("databaseValue"),
        CallHomeId("callHomeId"),
        SystemTitle("systemTitle"),
        PropertyBased("propertyName", "propertyValue"),
        ConnectionTypePropertyBased("connectionTypeClass", "propertyName", "propertyValue"),
        mRID("databaseValue"),
        Actual("actual", "mRID"),
        Name("databaseValue"),
        Null();

        private final String[] roles;

        IntrospectorTypes(String... roles) {
            this.roles = roles;
        }

        public Set<String> getRoles() {
            return new HashSet<>(Arrays.asList(roles));
        }

        public static Optional<IntrospectorTypes> forName(String name) {
            return Arrays.stream(values()).filter(type -> type.name().equals(name)).findFirst();
        }
    }

    private final MeteringService meteringService;
    private final DeviceDataModelService deviceDataModelService;
    private final QueryService queryService;
    private final Thesaurus thesaurus;
    private final Clock clock;
    private MultiplierType defaultMultiplierType;

    @Inject
    DeviceServiceImpl(DeviceDataModelService deviceDataModelService, MeteringService meteringService, QueryService queryService, NlsService nlsService, Clock clock) {
        this(deviceDataModelService, meteringService, queryService, nlsService.getThesaurus(DeviceDataServices.COMPONENT_NAME, Layer.DOMAIN), clock);
    }

    DeviceServiceImpl(DeviceDataModelService deviceDataModelService, MeteringService meteringService, QueryService queryService, Thesaurus thesaurus, Clock clock) {
        super();
        this.meteringService = meteringService;
        this.deviceDataModelService = deviceDataModelService;
        this.queryService = queryService;
        this.thesaurus = thesaurus;
        this.clock = clock;
        Services.deviceFinder(this);
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
    public boolean hasDevices(AllowedCalendar allowedCalendar) {
        if (allowedCalendar.isGhost()) {
            return false;
        }
        Condition activeCondition = where(ActiveEffectiveCalendarImpl.Fields.CALENDAR.fieldName()).isEqualTo(allowedCalendar)
                .and(where(ActiveEffectiveCalendarImpl.Fields.INTERVAL.fieldName()).isEffective(this.clock.instant()));
        Finder<ActiveEffectiveCalendar> page =
                DefaultFinder.
                        of(ActiveEffectiveCalendar.class, activeCondition, this.deviceDataModelService.dataModel()).
                        paged(0, 1);
        List<ActiveEffectiveCalendar> allActiveCalendars = page.find();
        if (!allActiveCalendars.isEmpty()) {
            return true;
        }

        Condition passiveCondition = where(PassiveCalendarImpl.Fields.CALENDAR.fieldName()).isEqualTo(allowedCalendar);
        Finder<PassiveCalendar> pagedPassive =
                DefaultFinder.of(PassiveCalendar.class, passiveCondition, this.deviceDataModelService.dataModel())
                        .paged(0, 1);
        List<PassiveCalendar> allPassiveCalendars = pagedPassive.find();
        return !allPassiveCalendars.isEmpty();
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
        Optional<DeviceProtocolPluggableClass> deviceProtocolPluggableClass = configurationProperties.getDeviceConfiguration()
                .getDeviceType()
                .getDeviceProtocolPluggableClass();
        Optional<CustomPropertySet<DeviceProtocolDialectPropertyProvider, ? extends PersistentDomainExtension<DeviceProtocolDialectPropertyProvider>>> customPropertySet =
                this.getCustomPropertySet(
                        configurationProperties.getDeviceProtocolDialectName(),
                        deviceProtocolPluggableClass
                                .map(this::getDeviceProtocolDialects)
                                .orElseGet(Collections::emptyList));
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
        } else {
            sqlBuilder.append(" from dual where 1 = 0");
        }
    }

    private List<? extends DeviceProtocolDialect> getDeviceProtocolDialects(DeviceProtocolPluggableClass pluggableClass) {
        return pluggableClass.getDeviceProtocol().getDeviceProtocolDialects();
    }

    private Optional<CustomPropertySet<DeviceProtocolDialectPropertyProvider, ? extends PersistentDomainExtension<DeviceProtocolDialectPropertyProvider>>> getCustomPropertySet(String name, List<? extends DeviceProtocolDialect> deviceProtocolDialects) {
        return deviceProtocolDialects
                .stream()
                .filter(dialect -> dialect.getDeviceProtocolDialectName().equals(name))
                .map(DeviceProtocolDialect::getCustomPropertySet)
                .flatMap(Functions.asStream())
                .findAny();
    }

    private long count(SqlBuilder sqlBuilder) {
        try (Connection connection = this.deviceDataModelService.dataModel().getConnection(false);
             PreparedStatement statement = sqlBuilder.prepare(connection)) {
            try (ResultSet counter = statement.executeQuery()) {
                counter.next();
                return counter.getLong(1);
            }
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    @Override
    public Device newDevice(DeviceConfiguration deviceConfiguration, String serialNumber, String name, Instant startDate) {
        Device device = this.deviceDataModelService.dataModel()
                .getInstance(DeviceImpl.class)
                .initialize(deviceConfiguration, name, startDate);
        device.setSerialNumber(serialNumber);
        device.save(); // always returns a persisted device
        return device;
    }

    @Override
    public Device newDevice(DeviceConfiguration deviceConfiguration, String serialNumber, String name, String batch, Instant startDate) {
        Device device = newDevice(deviceConfiguration, serialNumber, name, startDate);
        this.deviceDataModelService.batchService().findOrCreateBatch(batch).addDevice(device);
        return device;
    }

    @Override
    public DeviceBuilder newDeviceBuilder(DeviceConfiguration deviceConfiguration, String name, Instant startDate) {
        return new DeviceBuilderImpl(deviceConfiguration, name, startDate, deviceDataModelService, clock);
    }

    private DataMapper<Device> getDeviceMapper() {
        return this.deviceDataModelService.dataModel().mapper(Device.class);
    }

    @Override
    public Optional<Device> findDeviceById(long id) {
        return getDeviceMapper().getOptional(id);
    }

    @Override
    public Optional<Device> findAndLockDeviceByIdAndVersion(long id, long version) {
        return getDeviceMapper().lockObjectIfVersion(version, id);
    }

    @Override
    public Optional<Device> findAndLockDeviceById(long id) {
        return Optional.ofNullable(getDeviceMapper().lock(id));
    }

    @Override
    public Optional<Device> findAndLockDeviceByNameAndVersion(String name, long version) {
        DataMapper<Device> mapper = getDeviceMapper();
        return mapper.getUnique(DeviceFields.NAME.fieldName(), name).flatMap(device -> mapper.lockObjectIfVersion(version, device.getId()));
    }

    @Override
    public Optional<Device> findAndLockDeviceBymRIDAndVersion(String mRID, long version) {
        DataMapper<Device> mapper = getDeviceMapper();
        return mapper.getUnique(DeviceFields.MRID.fieldName(), mRID).flatMap(device -> mapper.lockObjectIfVersion(version, device.getId()));
    }

    @Override
    public Optional<SecurityAccessor<SecurityValueWrapper>> findAndLockSecurityAccessorById(Device device, SecurityAccessorType securityAccessorType) {
        return Optional.ofNullable((SecurityAccessor<SecurityValueWrapper>) deviceDataModelService.dataModel()
                .mapper(SecurityAccessor.class)
                .lock(device.getId(), securityAccessorType.getId()));
    }

    @Override
    public Optional<SecurityAccessor<SecurityValueWrapper>> findAndLockKeyAccessorByIdAndVersion(Device device, SecurityAccessorType securityAccessorType, long version) {
        return deviceDataModelService.dataModel()
                .mapper(SecurityAccessor.class)
                .lockObjectIfVersion(version, device.getId(), securityAccessorType.getId())
                .map(securityAccessor -> (SecurityAccessor<SecurityValueWrapper>) securityAccessor);
    }

    @Override
    public Optional<Device> findDeviceByMrid(String mrId) {
        return getDeviceMapper().getUnique(DeviceFields.MRID.fieldName(), mrId);
    }

    @Override
    public Optional<Device> findDeviceByName(String name) {
        return getDeviceMapper().getUnique(DeviceFields.NAME.fieldName(), name);
    }

    @Override
    public Optional<Device> findDeviceByMeterId(long meterID) {
        return getDeviceMapper().getUnique(DeviceFields.METER_ID.fieldName(), meterID);
    }

    @Override
    public List<Device> findDevicesBySerialNumber(String serialNumber) {
        return getDeviceMapper().find(DeviceFields.SERIALNUMBER.fieldName(), serialNumber);
    }

    protected List<Device> findDevicesBySerialNumberPattern(String serialNumberPattern) {
        return this.deviceDataModelService.dataModel().query(Device.class).select(where("serialNumberPattern").like(serialNumberPattern));
    }

    @Override
    public Optional<com.energyict.mdc.upl.meterdata.Device> find(DeviceIdentifier identifier) {
        return this.findDeviceByIdentifier(identifier).map(com.energyict.mdc.upl.meterdata.Device.class::cast);
    }

    @Override
    public Optional<Device> findDeviceByIdentifier(DeviceIdentifier identifier) {
        try {
            if (identifier == null) {
                return Optional.empty();
            }
            return this.exactlyOne(this.find(identifier.forIntrospection()), identifier);
        } catch (UnsupportedDeviceIdentifierTypeName | IllegalArgumentException | NotUniqueException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Device> findAllDevicesByIdentifier(DeviceIdentifier identifier) {
        try {
            return this.find(identifier.forIntrospection());
        } catch (UnsupportedDeviceIdentifierTypeName | IllegalArgumentException e) {
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    private List<Device> find(Introspector introspector) throws UnsupportedDeviceIdentifierTypeName {
        if (introspector.getTypeName().equals(IntrospectorTypes.SerialNumber.name())) {
            return this.findDevicesBySerialNumber((String) introspector.getValue(IntrospectorTypes.SerialNumber.roles[0]));
        } else if (introspector.getTypeName().equals(IntrospectorTypes.LikeSerialNumber.name())) {
            return this.findDevicesBySerialNumberPattern((String) introspector.getValue(IntrospectorTypes.LikeSerialNumber.roles[0]));
        } else if (introspector.getTypeName().equals(IntrospectorTypes.DatabaseId.name())) {
            return this
                    .findDeviceById(Long.valueOf(introspector.getValue(IntrospectorTypes.DatabaseId.roles[0]).toString()))
                    .map(Collections::singletonList)
                    .orElseGet(Collections::emptyList);
        } else if (introspector.getTypeName().equals(IntrospectorTypes.CallHomeId.name())) {
            String callHomeID = (String) introspector.getValue(IntrospectorTypes.CallHomeId.roles[0]);
            return this.findDevicesByPropertySpecValue(LegacyProtocolProperties.CALL_HOME_ID_PROPERTY_NAME, callHomeID);
        } else if (introspector.getTypeName().equals(IntrospectorTypes.SystemTitle.name())) {
            String systemTitle = (String) introspector.getValue(IntrospectorTypes.SystemTitle.roles[0]);
            return this.findDevicesByPropertySpecValue("DeviceSystemTitle", systemTitle);
        } else if (introspector.getTypeName().equals(IntrospectorTypes.PropertyBased.name())) {
            return this.findDevicesByPropertySpecValue(
                    (String) introspector.getValue(IntrospectorTypes.PropertyBased.roles[0]),
                    (String) introspector.getValue(IntrospectorTypes.PropertyBased.roles[1]));
        } else if (introspector.getTypeName().equals(IntrospectorTypes.ConnectionTypePropertyBased.name())) {
            Class<ConnectionType> connectionTypeClass = (Class) introspector.getValue(IntrospectorTypes.ConnectionTypePropertyBased.roles[0]);
            return this.findDevicesByConnectionTypeAndProperty(
                    connectionTypeClass,
                    (String) introspector.getValue(IntrospectorTypes.ConnectionTypePropertyBased.roles[1]),
                    (String) introspector.getValue(IntrospectorTypes.ConnectionTypePropertyBased.roles[2]));
        } else if (introspector.getTypeName().equals(IntrospectorTypes.mRID.name())) {
            return this
                    .findDeviceByMrid((String) introspector.getValue(IntrospectorTypes.mRID.roles[0]))
                    .map(Collections::singletonList)
                    .orElseGet(Collections::emptyList);
        } else if (introspector.getTypeName().equals(IntrospectorTypes.Actual.name())) {
            return this
                    .findDeviceById((long)introspector.getValue(IntrospectorTypes.Actual.roles[0]))
                    .map(Collections::singletonList)
                    .orElseGet(Collections::emptyList);
        } else if (introspector.getTypeName().equals(IntrospectorTypes.Name.name())) {
            String deviceName = (String) introspector.getValue(IntrospectorTypes.Name.roles[0]);
            return findDeviceByName(deviceName)
                    .map(Collections::singletonList)
                    .orElseGet(Collections::emptyList);
        } else if (introspector.getTypeName().equals(IntrospectorTypes.Null.name())) {
            throw new UnsupportedOperationException("NullDeviceIdentifier is not capable of finding a device because it is a marker for a missing device");
        } else {
            throw new UnsupportedDeviceIdentifierTypeName();
        }
    }

    private Optional<Device> exactlyOne(List<Device> allDevices, DeviceIdentifier identifier) {
        if (allDevices.isEmpty()) {
            return Optional.empty();
        } else {
            if (allDevices.size() > 1) {
                throw new NotUniqueException(identifier.toString());
            } else {
                return Optional.of(allDevices.get(0));
            }
        }
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
        List<ComTaskExecution> scheduledComTaskExecutions = this.deviceDataModelService.dataModel().query(ComTaskExecution.class).
                select(condition, Order.NOORDER, false, new String[0], 1, 1).stream().filter(ComTaskExecution::usesSharedSchedule).collect(Collectors.toList());
        return !scheduledComTaskExecutions.isEmpty();
    }

    @Override
    public Finder<Device> findDevicesByDeviceConfiguration(DeviceConfiguration deviceConfiguration) {
        return DefaultFinder.of(Device.class, where("deviceConfiguration").isEqualTo(deviceConfiguration), this.deviceDataModelService.dataModel())
                .defaultSortColumn("name");
    }

    @Override
    public List<Device> findDevicesByPropertySpecValue(String propertySpecName, String propertySpecValue) { //TODO: warning - we don't take the config level into account!
        Condition condition = where("deviceProperties.propertyName").isEqualTo(propertySpecName).and(where("deviceProperties.propertyValue").isEqualTo(propertySpecValue));
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
            final DeviceConfiguration deviceConfiguration = deviceDataModelService.deviceConfigurationService()
                    .findAndLockDeviceConfigurationByIdAndVersion(destinationDeviceConfigId, destinationDeviceConfigVersion)
                    .orElseThrow(DeviceConfigurationChangeException.noDestinationConfigFoundForVersion(thesaurus, destinationDeviceConfigId, destinationDeviceConfigVersion));
            Device device = findAndLockDeviceByIdAndVersion(deviceId, deviceVersion).orElseThrow(DeviceConfigurationChangeException.noDeviceFoundForVersion(thesaurus, deviceId, deviceVersion));
            final DeviceConfigChangeRequestImpl deviceConfigChangeRequest = deviceDataModelService.dataModel()
                    .getInstance(DeviceConfigChangeRequestImpl.class)
                    .init(deviceConfiguration);
            deviceConfigChangeRequest.save();
            return Pair.of(device, deviceConfigChangeRequest);
        });

        Device modifiedDevice = null;
        try {
            modifiedDevice = deviceDataModelService.getTransactionService()
                    .execute(() -> new DeviceConfigChangeExecutor(this, deviceDataModelService.clock(), ((DeviceImpl) lockResult.getFirst()).getEventService()).execute((DeviceImpl) lockResult.getFirst(), deviceDataModelService
                            .deviceConfigurationService()
                            .findDeviceConfiguration(destinationDeviceConfigId)
                            .get()));
        } finally {
            deviceDataModelService.getTransactionService().execute(VoidTransaction.of(lockResult.getLast()::notifyDeviceInActionIsRemoved));
        }

        addConnectionTasksToDevice(modifiedDevice,modifiedDevice.getDeviceConfiguration());

        return modifiedDevice;
    }

    private void addConnectionTasksToDevice(Device device, DeviceConfiguration deviceConfiguration){
        Set<Long> devPartialConnectionTasksIds = device.getConnectionTasks().stream().map(connectionTask -> connectionTask.getPartialConnectionTask().getId()).collect(Collectors.toSet());
        deviceConfiguration.getPartialConnectionTasks().stream()
                .filter(partialConnectionTask -> !devPartialConnectionTasksIds.contains(partialConnectionTask.getId()))
                .forEach(partialConnectionTask -> {
                    if(partialConnectionTask instanceof PartialInboundConnectionTask) {
                        deviceDataModelService.getTransactionService().execute(() -> device.getInboundConnectionTaskBuilder((PartialInboundConnectionTask)partialConnectionTask).add());
                    } else if(partialConnectionTask instanceof PartialScheduledConnectionTask) {
                        deviceDataModelService.getTransactionService().execute(() -> device.getScheduledConnectionTaskBuilder((PartialScheduledConnectionTask)partialConnectionTask).add());
                    }
                });
    }

    @Override
    public void changeDeviceConfigurationForDevices(DeviceConfiguration destinationDeviceConfiguration, DevicesForConfigChangeSearch devicesForConfigChangeSearch, Long... deviceIds) {
        final DeviceConfigChangeRequestImpl deviceConfigChangeRequest = deviceDataModelService.dataModel()
                .getInstance(DeviceConfigChangeRequestImpl.class)
                .init(destinationDeviceConfiguration);
        deviceConfigChangeRequest.save();

        List<Long> ids = Arrays.asList(deviceIds);
        ids.forEach(id -> findDeviceById(id).ifPresent(device -> addConnectionTasksToDevice(device, destinationDeviceConfiguration)));
        ItemizeConfigChangeQueueMessage itemizeConfigChangeQueueMessage = new ItemizeConfigChangeQueueMessage(destinationDeviceConfiguration.getId(), ids, devicesForConfigChangeSearch, deviceConfigChangeRequest
                .getId());

        DestinationSpec destinationSpec = deviceDataModelService.messageService()
                .getDestinationSpec(ServerDeviceForConfigChange.CONFIG_CHANGE_BULK_QUEUE_DESTINATION)
                .orElseThrow(new NoDestinationSpecFound(thesaurus, ServerDeviceForConfigChange.CONFIG_CHANGE_BULK_QUEUE_DESTINATION));
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
                .filter(where(DeviceConfigChangeRequestImpl.Fields.DEVICE_CONFIG_REFERENCE.fieldName()).in(Arrays.asList(originDeviceConfiguration, destinationDeviceConfiguration)))
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

    @Override
    public Optional<ActivatedBreakerStatus> getActiveBreakerStatus(Device device) {
        QueryExecutor<ActivatedBreakerStatus> activeBreakerStatusQuery = deviceDataModelService.dataModel().query(ActivatedBreakerStatus.class);
        return activeBreakerStatusQuery
                .select(where("device").isEqualTo(device).and(where("interval").isEffective()))
                .stream()
                .findFirst();
    }

    @Override
    public ActivatedBreakerStatus newActivatedBreakerStatusFrom(Device device, BreakerStatus collectedBreakerStatus, Interval interval) {
        return ActivatedBreakerStatusImpl.from(deviceDataModelService.dataModel(), device, collectedBreakerStatus, interval);
    }

    @Override
    public Optional<CreditAmount> getCreditAmount(Device device) {
        return deviceDataModelService.dataModel().stream(CreditAmount.class)
                .filter(where("device").isEqualTo(device))
                .findFirst();
    }

    @Override
    public CreditAmount creditAmountFrom(Device device, String collectedCreditType, BigDecimal collectedCreditAmount) {
        return CreditAmountImpl.from(deviceDataModelService.dataModel(), device, collectedCreditType, collectedCreditAmount);
    }

    @Override
    public List<Device> findDeviceWithOverruledObisCodeForOtherThanRegisterSpec(RegisterSpec registerSpec) {
        Condition condition = where(DeviceFields.READINGTYPEOBISCODEUSAGES.fieldName() + "."
                + ReadingTypeObisCodeUsageImpl.Fields.OBISCODESTRING.fieldName()).isEqualTo(registerSpec.getDeviceObisCode().getValue())
                .and(where(DeviceFields.READINGTYPEOBISCODEUSAGES.fieldName() + "."
                        + ReadingTypeObisCodeUsageImpl.Fields.READINGTYPE.fieldName()).isNotEqual(registerSpec.getReadingType()))
                .and(where(DeviceFields.DEVICECONFIGURATION.fieldName()).isEqualTo(registerSpec.getDeviceConfiguration()));
        return this.deviceDataModelService.dataModel().stream(Device.class).join(ReadingTypeObisCodeUsage.class)
                .filter(condition)
                .filter(onlyMatchRegisters(registerSpec))
                .collect(Collectors.toList());

    }

    private Predicate<Device> onlyMatchRegisters(RegisterSpec registerSpec) {
        return device -> device.getRegisters().stream()
                .filter(register -> !register.getReadingType().getMRID().equals(registerSpec.getReadingType().getMRID()))
                .map(Register::getDeviceObisCode)
                .anyMatch(obisCode -> obisCode.equals(registerSpec.getDeviceObisCode()));
    }

    @Override
    public List<Device> findDeviceWithOverruledObisCodeForOtherThanChannelSpec(ChannelSpec channelSpec) {
        Condition condition = where(DeviceFields.READINGTYPEOBISCODEUSAGES.fieldName() + "."
                + ReadingTypeObisCodeUsageImpl.Fields.OBISCODESTRING.fieldName()).isEqualTo(channelSpec.getDeviceObisCode().getValue())
                .and(where(DeviceFields.READINGTYPEOBISCODEUSAGES.fieldName() + "."
                        + ReadingTypeObisCodeUsageImpl.Fields.READINGTYPE.fieldName()).isNotEqual(channelSpec.getReadingType()))
                .and(where(DeviceFields.DEVICECONFIGURATION.fieldName()).isEqualTo(channelSpec.getDeviceConfiguration()));
        return this.deviceDataModelService.dataModel().stream(Device.class).join(ReadingTypeObisCodeUsage.class)
                .filter(condition)
                .filter(onlyMatchLoadProfileOfChannelSpec(channelSpec))
                .collect(Collectors.toList());
    }

    private Predicate<Device> onlyMatchLoadProfileOfChannelSpec(ChannelSpec channelSpec) {
        return device1 -> device1.getLoadProfiles()
                .stream()
                .filter(loadProfile -> loadProfile.getLoadProfileSpec().getId() == channelSpec.getLoadProfileSpec().getId())
                .anyMatch(loadProfile1 -> loadProfile1.getChannels().stream().anyMatch(otherChannelsThenChannelFromSpec(channelSpec)));
    }

    private Predicate<Channel> otherChannelsThenChannelFromSpec(ChannelSpec channelSpec) {
        return channel -> channel.getObisCode().equals(channelSpec.getDeviceObisCode()) && !channel.getReadingType()
                .getMRID().equals(channelSpec.getReadingType().getMRID());
    }

    @Override
    public List<Device> findActiveValidatedDevices(List<Device> domainObjects) {
        List<Meter> enabledMeters = deviceDataModelService.validationService()
                .validationEnabledMetersIn(domainObjects.stream().map(Device::getName).collect(Collectors.toList()));
        return domainObjects.stream()
                .filter(device -> enabledMeters.stream().anyMatch(meter -> meter.getName().equals(device.getName())))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteOutdatedComTaskExecutionTriggers() {
        try (Connection connection = this.deviceDataModelService.dataModel().getConnection(false);
             PreparedStatement statement = deleteOutdatedComTaskExecutionTriggersSqlBuilder().prepare(connection)) {
            statement.executeQuery();
        } catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    @Override
    public boolean usedByKeyAccessor(CertificateWrapper certificate) {
        return deviceDataModelService.dataModel()
                .stream(SecurityAccessor.class)
                .anyMatch(where(AbstractDeviceSecurityAccessorImpl.Fields.CERTIFICATE_WRAPPER_ACTUAL.fieldName()).isEqualTo(certificate)
                        .or(where(AbstractDeviceSecurityAccessorImpl.Fields.CERTIFICATE_WRAPPER_TEMP.fieldName()).isEqualTo(certificate)));
    }

    @Override
    public List<SecurityAccessor> getAssociatedKeyAccessors(CertificateWrapper certificate) {
        return deviceDataModelService.dataModel()
                .stream(SecurityAccessor.class)
                .filter(where(AbstractDeviceSecurityAccessorImpl.Fields.CERTIFICATE_WRAPPER_ACTUAL.fieldName()).isEqualTo(certificate)
                        .or(where(AbstractDeviceSecurityAccessorImpl.Fields.CERTIFICATE_WRAPPER_TEMP.fieldName()).isEqualTo(certificate)))
                .collect(Collectors.toList());
    }

    private SqlBuilder deleteOutdatedComTaskExecutionTriggersSqlBuilder() {
        Instant outdatedTimeStamp = this.deviceDataModelService.clock().instant().minus(1, ChronoUnit.DAYS);

        SqlBuilder sqlBuilder = new SqlBuilder("delete from ");
        sqlBuilder.append(TableSpecs.DDC_COMTASKEXEC_TRIGGERS.name());
        sqlBuilder.append(" where TIMESTAMP < ");
        sqlBuilder.addLong(outdatedTimeStamp.getEpochSecond());
        return sqlBuilder;
    }

    @Override
    public MultiplierType findDefaultMultiplierType() {
        if (this.defaultMultiplierType == null) {
            Optional<MultiplierType> multiplierType = this.meteringService.getMultiplierType(MULTIPLIER_TYPE);
            if (multiplierType.isPresent()) {
                this.defaultMultiplierType = multiplierType.get();
            } else {
                throw new IllegalStateException("mdc.device.data installer has not run yet!");
            }
        }
        return this.defaultMultiplierType;
    }

    @Override
    public void clearMultiplierTypeCache() {
        this.defaultMultiplierType = null;
    }

    @Override
    public Device lockDevice(long deviceId) {
        return this.deviceDataModelService.dataModel().mapper(Device.class).lock(deviceId);
    }

    private static class UnsupportedDeviceIdentifierTypeName extends RuntimeException {
    }
}
