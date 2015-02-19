package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.sql.SqlBuilder;

import com.energyict.mdc.common.CanFindByLongPrimaryKey;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.common.services.DefaultFinder;
import com.energyict.mdc.common.services.Finder;
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
import com.energyict.mdc.dynamic.relation.RelationType;
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
import java.util.ArrayList;
import java.util.List;
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

    @Inject
    public DeviceServiceImpl(DeviceDataModelService deviceDataModelService, ProtocolPluggableService protocolPluggableService) {
        super();
        this.deviceDataModelService = deviceDataModelService;
        this.protocolPluggableService = protocolPluggableService;
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
        return this.count(this.hasDevicesSqlBuilder(configurationProperties));
    }

    @Override
    public boolean hasDevices(ProtocolDialectConfigurationProperties configurationProperties, PropertySpec propertySpec) {
        SqlBuilder sqlBuilder = this.hasDevicesSqlBuilder(configurationProperties);
        sqlBuilder.append("and ");
        sqlBuilder.append(propertySpec.getName());
        sqlBuilder.append(" is not null");
        return this.count(sqlBuilder);
    }

    private SqlBuilder hasDevicesSqlBuilder(ProtocolDialectConfigurationProperties configurationProperties) {
        DeviceProtocolPluggableClass deviceProtocolPluggableClass = configurationProperties.getDeviceConfiguration().getDeviceType().getDeviceProtocolPluggableClass();
        DeviceProtocolDialectUsagePluggableClass deviceProtocolDialectUsagePluggableClass =
                this.protocolPluggableService
                        .getDeviceProtocolDialectUsagePluggableClass(
                                deviceProtocolPluggableClass,
                                configurationProperties.getDeviceProtocolDialectName());
        String propertiesTable = deviceProtocolDialectUsagePluggableClass.findRelationType().getDynamicAttributeTableName();
        SqlBuilder sqlBuilder = new SqlBuilder("select count(*) from ");
        sqlBuilder.append(propertiesTable);
        sqlBuilder.append(" dru join ");
        sqlBuilder.append(TableSpecs.DDC_PROTOCOLDIALECTPROPS.name());
        sqlBuilder.append(" props on dru.");
        sqlBuilder.append(DeviceProtocolDialectPropertyRelationAttributeTypeNames.DEVICE_PROTOCOL_DIALECT_ATTRIBUTE_NAME);
        sqlBuilder.append(" = props.id where props.configurationpropertiesid =");
        sqlBuilder.addLong(configurationProperties.getId());
        return sqlBuilder;
    }

    private boolean count(SqlBuilder sqlBuilder) {
        try (PreparedStatement statement = sqlBuilder.prepare(this.deviceDataModelService.dataModel().getConnection(false))) {
            try (ResultSet counter = statement.executeQuery()) {
                counter.next();
                return counter.getLong(1) > 0;
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
    public Device findDeviceById(long id) {
        return this.deviceDataModelService.dataModel().mapper(Device.class).getUnique("id", id).orElse(null);
    }

    @Override
    public Device findByUniqueMrid(String mrId) {
        return this.deviceDataModelService.dataModel().mapper(Device.class).getUnique(DeviceFields.MRID.fieldName(), mrId).orElse(null);
    }

    @Override
    public List<Device> findDevicesBySerialNumber(String serialNumber) {
        return this.deviceDataModelService.dataModel().mapper(Device.class).find("serialNumber", serialNumber);
    }

    @Override
    public Finder<Device> findAllDevices(Condition condition) {
        return DefaultFinder.of(Device.class, condition, this.deviceDataModelService.dataModel(), DeviceConfiguration.class, DeviceType.class);
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
}