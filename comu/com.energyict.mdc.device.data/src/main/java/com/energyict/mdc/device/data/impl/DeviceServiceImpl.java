package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.energyict.mdc.common.CanFindByLongPrimaryKey;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.data.ComTaskExecutionFields;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceFields;
import com.energyict.mdc.device.data.DeviceProtocolProperty;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.finders.DeviceFinder;
import com.energyict.mdc.device.data.impl.finders.ProtocolDialectPropertiesFinder;
import com.energyict.mdc.device.data.impl.finders.SecuritySetFinder;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.ScheduledComTaskExecution;
import com.energyict.mdc.pluggable.PluggableClass;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.DeviceProtocolDialectPropertyRelationAttributeTypeNames;
import com.energyict.mdc.protocol.pluggable.DeviceProtocolDialectUsagePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.scheduling.model.ComSchedule;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;

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
    private final QueryService queryService;
    private final Thesaurus thesaurus;

    @Inject
    public DeviceServiceImpl(DeviceDataModelService deviceDataModelService, ProtocolPluggableService protocolPluggableService, QueryService queryService, Thesaurus thesaurus) {
        super();
        this.deviceDataModelService = deviceDataModelService;
        this.protocolPluggableService = protocolPluggableService;
        this.queryService = queryService;
        this.thesaurus = thesaurus;
    }

    @Override
    public List<CanFindByLongPrimaryKey<? extends HasId>> finders() {
        List<CanFindByLongPrimaryKey<? extends HasId>> finders = new ArrayList<>();
        finders.add(new DeviceFinder(this.deviceDataModelService.dataModel()));
        finders.add(new ProtocolDialectPropertiesFinder(this.deviceDataModelService.dataModel()));
        finders.add(new SecuritySetFinder(this.deviceDataModelService.deviceConfigurationService()));
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
        DeviceProtocolDialectUsagePluggableClass deviceProtocolDialectUsagePluggableClass =
                this.protocolPluggableService
                        .getDeviceProtocolDialectUsagePluggableClass(
                                deviceProtocolPluggableClass,
                                configurationProperties.getDeviceProtocolDialectName());
        String propertiesTable = deviceProtocolDialectUsagePluggableClass.findRelationType().getDynamicAttributeTableName();
        sqlBuilder.append(" from ");
        sqlBuilder.append(propertiesTable);
        sqlBuilder.append(" dru join ");
        sqlBuilder.append(TableSpecs.DDC_PROTOCOLDIALECTPROPS.name());
        sqlBuilder.append(" props on dru.");
        sqlBuilder.append(DeviceProtocolDialectPropertyRelationAttributeTypeNames.DEVICE_PROTOCOL_DIALECT_ATTRIBUTE_NAME);
        sqlBuilder.append(" = props.id where props.configurationpropertiesid =");
        sqlBuilder.addLong(configurationProperties.getId());
        sqlBuilder.append("and (fromdate <=");
        sqlBuilder.addLong(now.getEpochSecond());
        sqlBuilder.append(" and (todate is null or todate >");
        sqlBuilder.addLong(now.getEpochSecond());
        sqlBuilder.append("))");
    }

    private long count(SqlBuilder sqlBuilder) {
        try (PreparedStatement statement = sqlBuilder.prepare(this.deviceDataModelService.dataModel().getConnection(false))) {
            try (ResultSet counter = statement.executeQuery()) {
                counter.next();
                return counter.getLong(1);
            }
        }
        catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    @Override
    public Device newDevice(DeviceConfiguration deviceConfiguration, String name, String mRID) {
        return this.deviceDataModelService.dataModel().getInstance(DeviceImpl.class).initialize(deviceConfiguration, name, mRID);
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
    public Optional<Device> findAndLockDeviceBymRIDAndVersion(String mrid, long version) {
        Optional<Device> deviceOptional = this.deviceDataModelService.dataModel().mapper(Device.class).getUnique(DeviceFields.MRID.fieldName(), mrid);
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
                maxPageSize(thesaurus, 10000);
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
}