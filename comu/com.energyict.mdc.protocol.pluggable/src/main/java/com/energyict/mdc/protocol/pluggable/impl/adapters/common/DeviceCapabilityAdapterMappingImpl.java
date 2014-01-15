package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.energyict.cpo.PersistentObject;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Copyrights EnergyICT
 * Date: 15/04/13
 * Time: 11:23
 */
public class DeviceCapabilityAdapterMappingImpl extends PersistentObject implements DeviceCapabilityMapping {

    private String deviceProtocolJavaClassName;
    private int deviceProtocolCapability;

    public DeviceCapabilityAdapterMappingImpl(ResultSet resultSet) throws SQLException {
        super();
        doLoad(resultSet);
    }

    protected void doLoad(ResultSet resultSet) throws SQLException {
        this.deviceProtocolJavaClassName = resultSet.getString(1);
        this.deviceProtocolCapability = resultSet.getInt(2);
    }

    @Override
    protected String[] getColumns() {
        return CapabilityAdapterMappingFactoryImpl.COLUMNS;
    }

    @Override
    protected String getTableName() {
        return CapabilityAdapterMappingFactoryImpl.TABLENAME;
    }

    @Override
    protected int getNumberOfPrimaryKeyColumns() {
        return ProtocolCapabilityAdapterMappingPrimaryKey.getNumberOfPrimaryKeyColumns();
    }

    @Override
    protected int bindBody(PreparedStatement preparedStatement, int offset) throws SQLException {
        // Do not need binding because creation is done by factory with BatchStatement
        return offset;
    }

    @Override
    protected int bindWhere(PreparedStatement preparedStatement, int offset) throws SQLException {
        // Do not need binding because creation is done by factory with BatchStatement
        return offset;
    }

    @Override
    public Serializable getPrimaryKey() {
        return new ProtocolCapabilityAdapterMappingPrimaryKey(deviceProtocolJavaClassName, deviceProtocolCapability);
    }

    @Override
    public String getDeviceProtocolJavaClassName() {
        return this.deviceProtocolJavaClassName;
    }

    @Override
    public int getDeviceProtocolCapabilities() {
        return deviceProtocolCapability;
    }

    static final class ProtocolCapabilityAdapterMappingPrimaryKey implements Serializable {

        private final String deviceProtocolJavaClassName;
        private final int deviceProtocolCapability;

        public ProtocolCapabilityAdapterMappingPrimaryKey(String deviceProtocolJavaClassName, int deviceProtocolCapability) {
            this.deviceProtocolJavaClassName = deviceProtocolJavaClassName;
            this.deviceProtocolCapability = deviceProtocolCapability;
        }

        private static int getNumberOfPrimaryKeyColumns() {
            return 2;
        }

        public String getDeviceProtocolJavaClassName() {
            return deviceProtocolJavaClassName;
        }

        int getDeviceProtocolCapability() {
            return deviceProtocolCapability;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            ProtocolCapabilityAdapterMappingPrimaryKey that = (ProtocolCapabilityAdapterMappingPrimaryKey) o;
            return deviceProtocolJavaClassName.equals(that.deviceProtocolJavaClassName) && deviceProtocolCapability == that.deviceProtocolCapability;
        }

        @Override
        public int hashCode() {
            int result = deviceProtocolJavaClassName.hashCode();
            result = 31 * result + deviceProtocolCapability;
            return result;
        }
    }
}