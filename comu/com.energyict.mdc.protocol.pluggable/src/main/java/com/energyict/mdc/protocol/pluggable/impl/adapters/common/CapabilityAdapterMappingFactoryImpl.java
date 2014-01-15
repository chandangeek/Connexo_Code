package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.energyict.cpo.PersistentObjectFactory;
import com.energyict.mdc.common.SqlBuilder;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 15/04/13
 * Time: 11:19
 */
public class CapabilityAdapterMappingFactoryImpl extends PersistentObjectFactory<DeviceCapabilityMapping> implements CapabilityAdapterMappingFactory {

    public static final String TABLENAME = "mdccapabilitiesadaptermapping";

    static final String[] COLUMNS = {
            "deviceprotocoljavaclassname",
            "deviceprotocolcapabilities"
    };

    private static final String NOT_NULL = "not null";

    private List<DeviceCapabilityMapping> cachedCapabilityMappings;

    @Override
    protected DeviceCapabilityMapping construct(ResultSet resultSet) throws SQLException {
        return new DeviceCapabilityAdapterMappingImpl(resultSet);
    }

    @Override
    public Integer getCapabilitiesMappingForDeviceProtocol(String deviceProtocolJavaClassName) {
        // we will try to cache all mappings
        if (this.cachedCapabilityMappings == null) {
            this.cachedCapabilityMappings = findAll();
        }
        return getFromCachedMapping(deviceProtocolJavaClassName, cachedCapabilityMappings);
    }

    private Integer getFromCachedMapping(String deviceProtocolJavaClassName, List<DeviceCapabilityMapping> cachedCapabilityMappings) {
        for (DeviceCapabilityMapping deviceCapabilityMapping : cachedCapabilityMappings) {
            if (deviceCapabilityMapping.getDeviceProtocolJavaClassName().equals(deviceProtocolJavaClassName)) {
                return deviceCapabilityMapping.getDeviceProtocolCapabilities();
            }
        }
        return null;
    }

    @Override
    public String[] getColumns() {
        return COLUMNS;
    }

    @Override
    protected String getTableName() {
        return TABLENAME;
    }

    @Override
    public DeviceCapabilityMapping findByPrimaryKey(Serializable key) {
        return this.findByPrimaryKey((DeviceCapabilityAdapterMappingImpl.ProtocolCapabilityAdapterMappingPrimaryKey) key);
    }

    private DeviceCapabilityMapping findByPrimaryKey(DeviceCapabilityAdapterMappingImpl.ProtocolCapabilityAdapterMappingPrimaryKey key) {
        SqlBuilder builder = new SqlBuilder(selectClause());
        builder.append(" where deviceprotocoljavaclassname = ? and deviceprotocolcapabilities = ? ");
        builder.bindString(key.getDeviceProtocolJavaClassName());
        builder.bindString(String.valueOf(key.getDeviceProtocolCapability()));
        return fetchFirst(builder);
    }

    @Override
    public String getInsertStatement() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("insert into ");
        stringBuilder.append(TABLENAME);
        stringBuilder.append(" (deviceprotocoljavaclassname, deviceprotocolcapabilities) values (?,?) ");
        return stringBuilder.toString();
    }

    @Override
    public void clearCache() {
        this.cachedCapabilityMappings = null;
    }

    public int[] getColumnTypes() {
        return new int[]{
                Types.VARCHAR,
                Types.INTEGER   //Value is only 3 bits long ==> maximum value is 7
        };
    }

    public String[] getColumnConstraints() {
        return new String[]{
                NOT_NULL,
                NOT_NULL
        };
    }
}