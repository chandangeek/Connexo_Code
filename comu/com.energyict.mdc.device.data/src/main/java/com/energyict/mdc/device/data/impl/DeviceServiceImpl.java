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
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.streams.Functions;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.config.AllowedCalendar;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.data.ActivatedBreakerStatus;
import com.energyict.mdc.device.data.ActiveEffectiveCalendar;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.DeviceFields;
import com.energyict.mdc.device.data.DeviceProtocolProperty;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.DevicesForConfigChangeSearch;
import com.energyict.mdc.device.data.ItemizeConfigChangeQueueMessage;
import com.energyict.mdc.device.data.PassiveCalendar;
import com.energyict.mdc.device.data.ReadingTypeObisCodeUsage;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.device.data.exceptions.DeviceConfigurationChangeException;
import com.energyict.mdc.device.data.exceptions.NoDestinationSpecFound;
import com.energyict.mdc.device.data.impl.configchange.DeviceConfigChangeExecutor;
import com.energyict.mdc.device.data.impl.configchange.DeviceConfigChangeInAction;
import com.energyict.mdc.device.data.impl.configchange.DeviceConfigChangeRequest;
import com.energyict.mdc.device.data.impl.configchange.DeviceConfigChangeRequestImpl;
import com.energyict.mdc.device.data.impl.configchange.ServerDeviceForConfigChange;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.ComTaskExecutionFields;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.pluggable.PluggableClass;
import com.energyict.mdc.protocol.api.CommonDeviceProtocolDialectProperties;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.DeviceProtocolDialectPropertyProvider;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.device.data.BreakerStatus;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.scheduling.model.ComSchedule;

import org.osgi.service.event.EventConstants;

import javax.inject.Inject;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
        Condition condition = where(ActiveEffectiveCalendarImpl.Fields.CALENDAR.fieldName()).isEqualTo(allowedCalendar)
                .and(Where.where(ActiveEffectiveCalendarImpl.Fields.INTERVAL.fieldName()).isEffective(this.clock.instant()));
        Finder<ActiveEffectiveCalendar> page =
                DefaultFinder.
                        of(ActiveEffectiveCalendar.class, condition, this.deviceDataModelService.dataModel()).
                        paged(0, 1);
        List<ActiveEffectiveCalendar> allActiveCalendars = page.find();
        if (!allActiveCalendars.isEmpty()) {
            return true;
        }

        condition = where(PassiveCalendarImpl.Fields.CALENDAR.fieldName()).isEqualTo(allowedCalendar);
        Finder<PassiveCalendar> pagedPassive =
                DefaultFinder.of(PassiveCalendar.class, condition, this.deviceDataModelService.dataModel())
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
                this.getCustomPropertySet(configurationProperties.getDeviceProtocolDialectName(),
                        deviceProtocolPluggableClass.map(deviceProtocolPluggableClass1 -> deviceProtocolPluggableClass1.getDeviceProtocol()
                                .getDeviceProtocolDialects())
                                .orElse(Collections.emptyList()));
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

    private Optional<CustomPropertySet<DeviceProtocolDialectPropertyProvider, ? extends PersistentDomainExtension<DeviceProtocolDialectPropertyProvider>>> getCustomPropertySet(String name, List<DeviceProtocolDialect> deviceProtocolDialects) {
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
    public Device newDevice(DeviceConfiguration deviceConfiguration, String name, Instant startDate) {
        Device device = this.deviceDataModelService.dataModel()
                .getInstance(DeviceImpl.class)
                .initialize(deviceConfiguration, name, startDate);
        device.save(); // always returns a persisted device
        return device;
    }

    @Override
    public Device newDevice(DeviceConfiguration deviceConfiguration, String name, String batch, Instant startDate) {
        Device device = newDevice(deviceConfiguration, name, startDate);
        this.deviceDataModelService.batchService().findOrCreateBatch(batch).addDevice(device);
        return device;
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
    public Optional<Device> findDeviceByMrid(String mrId) {
        return getDeviceMapper().getUnique(DeviceFields.MRID.fieldName(), mrId);
    }

    @Override
    public Optional<Device> findDeviceByName(String name) {
        return getDeviceMapper().getUnique(DeviceFields.NAME.fieldName(), name);
    }

    @Override
    public List<Device> findDevicesBySerialNumber(String serialNumber) {
        return getDeviceMapper().find(DeviceFields.SERIALNUMBER.fieldName(), serialNumber);
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
                select(condition, new Order[0], false, new String[0], 1, 1).stream().filter(ComTaskExecution::usesSharedSchedule).collect(Collectors.toList());
        return !scheduledComTaskExecutions.isEmpty();
    }

    @Override
    public Finder<Device> findDevicesByDeviceConfiguration(DeviceConfiguration deviceConfiguration) {
        return DefaultFinder.of(Device.class, where("deviceConfiguration").isEqualTo(deviceConfiguration), this.deviceDataModelService.dataModel())
                .defaultSortColumn("lower(name)");
    }

    @Override
    public List<Device> findDevicesByPropertySpecValue(String propertySpecName, String propertySpecValue) {
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
            Device device = findAndLockDeviceByIdAndVersion(deviceId, deviceVersion).orElseThrow(DeviceConfigurationChangeException.noDeviceFoundForVersion(thesaurus, deviceId, deviceVersion));
            final DeviceConfiguration deviceConfiguration = deviceDataModelService.deviceConfigurationService()
                    .findAndLockDeviceConfigurationByIdAndVersion(destinationDeviceConfigId, destinationDeviceConfigVersion)
                    .orElseThrow(DeviceConfigurationChangeException.noDestinationConfigFoundForVersion(thesaurus, destinationDeviceConfigId, destinationDeviceConfigVersion));
            final DeviceConfigChangeRequestImpl deviceConfigChangeRequest = deviceDataModelService.dataModel()
                    .getInstance(DeviceConfigChangeRequestImpl.class)
                    .init(deviceConfiguration);
            deviceConfigChangeRequest.save();
            return Pair.of(device, deviceConfigChangeRequest);
        });

        Device modifiedDevice = null;
        try {
            modifiedDevice = deviceDataModelService.getTransactionService()
                    .execute(() -> new DeviceConfigChangeExecutor(this, deviceDataModelService.clock()).execute((DeviceImpl) lockResult.getFirst(), deviceDataModelService.deviceConfigurationService()
                            .findDeviceConfiguration(destinationDeviceConfigId)
                            .get()));
        } finally {
            deviceDataModelService.getTransactionService().execute(VoidTransaction.of(lockResult.getLast()::notifyDeviceInActionIsRemoved));
        }
        return modifiedDevice;
    }

    @Override
    public void changeDeviceConfigurationForDevices(DeviceConfiguration destinationDeviceConfiguration, DevicesForConfigChangeSearch devicesForConfigChangeSearch, Long... deviceIds) {
        final DeviceConfigChangeRequestImpl deviceConfigChangeRequest = deviceDataModelService.dataModel()
                .getInstance(DeviceConfigChangeRequestImpl.class)
                .init(destinationDeviceConfiguration);
        deviceConfigChangeRequest.save();
        ItemizeConfigChangeQueueMessage itemizeConfigChangeQueueMessage = new ItemizeConfigChangeQueueMessage(destinationDeviceConfiguration.getId(), Arrays.asList(deviceIds), devicesForConfigChangeSearch, deviceConfigChangeRequest
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
                .select(where("device").isEqualTo(device).and(Where.where("interval").isEffective()))
                .stream()
                .findFirst();
    }

    @Override
    public ActivatedBreakerStatus newActivatedBreakerStatusFrom(Device device, BreakerStatus collectedBreakerStatus, Interval interval) {
        return ActivatedBreakerStatusImpl.from(deviceDataModelService.dataModel(), device, collectedBreakerStatus, interval);
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
}