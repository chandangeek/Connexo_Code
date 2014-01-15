package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.energyict.cpo.PersistentObject;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Copyrights EnergyICT
 * Date: 11/04/13
 * Time: 15:42
 */
public class SecuritySupportAdapterMappingImpl extends PersistentObject implements SecuritySupportAdapterMapping {

    private String deviceProtocolJavaClassName;
    private String securitySupportJavaClassName;

    SecuritySupportAdapterMappingImpl(ResultSet resultSet) throws SQLException {
        super();
        doLoad(resultSet);
    }

    protected void doLoad(ResultSet resultSet) throws SQLException {
        this.deviceProtocolJavaClassName = resultSet.getString(1);
        this.securitySupportJavaClassName = resultSet.getString(2);
    }

    @Override
    protected String[] getColumns() {
        return SecuritySupportAdapterMappingFactoryImpl.COLUMNS;
    }

    @Override
    protected String getTableName() {
        return SecuritySupportAdapterMappingFactoryImpl.TABLENAME;
    }

    @Override
    protected int getNumberOfPrimaryKeyColumns() {
        return SecuritySupportMappingPrimaryKey.getNumberOfPrimaryKeyColumns();
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
        return new SecuritySupportMappingPrimaryKey(deviceProtocolJavaClassName, securitySupportJavaClassName);
    }

    @Override
    public String getSecuritySupportJavaClassName() {
        return securitySupportJavaClassName;
    }

    @Override
    public String getDeviceProtocolJavaClassName() {
        return deviceProtocolJavaClassName;
    }

    static final class SecuritySupportMappingPrimaryKey implements Serializable {

        private final String deviceProtocolJavaClassName;
        private final String securitySupportJavaClassName;

        public SecuritySupportMappingPrimaryKey(String deviceProtocolJavaClassName, String securitySupportJavaClassName) {
            this.deviceProtocolJavaClassName = deviceProtocolJavaClassName;
            this.securitySupportJavaClassName = securitySupportJavaClassName;
        }

        private static int getNumberOfPrimaryKeyColumns() {
            return 2;
        }

        public String getDeviceProtocolJavaClassName() {
            return deviceProtocolJavaClassName;
        }

        public String getSecuritySupportJavaClassName() {
            return securitySupportJavaClassName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            SecuritySupportMappingPrimaryKey that = (SecuritySupportMappingPrimaryKey) o;
            return deviceProtocolJavaClassName.equals(that.deviceProtocolJavaClassName) && securitySupportJavaClassName.equals(that.securitySupportJavaClassName);

        }

        @Override
        public int hashCode() {
            int result = deviceProtocolJavaClassName.hashCode();
            result = 31 * result + securitySupportJavaClassName.hashCode();
            return result;
        }
    }
}
