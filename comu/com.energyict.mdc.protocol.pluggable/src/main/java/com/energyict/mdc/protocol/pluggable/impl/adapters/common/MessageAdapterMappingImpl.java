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
public class MessageAdapterMappingImpl extends PersistentObject implements MessageAdapterMapping {

    private String deviceProtocolJavaClassName;
    private String messageAdapterJavaClassName;

    public MessageAdapterMappingImpl(ResultSet resultSet) throws SQLException {
        super();
        doLoad(resultSet);
    }

    protected void doLoad(ResultSet resultSet) throws SQLException {
        this.deviceProtocolJavaClassName = resultSet.getString(1);
        this.messageAdapterJavaClassName = resultSet.getString(2);
    }

    @Override
    protected String[] getColumns() {
        return MessageAdapterMappingFactoryImpl.COLUMNS;
    }

    @Override
    protected String getTableName() {
        return MessageAdapterMappingFactoryImpl.TABLENAME;
    }

    @Override
    protected int getNumberOfPrimaryKeyColumns() {
        return MessageAdapterMappingPrimaryKey.getNumberOfPrimaryKeyColumns();
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
        return new MessageAdapterMappingPrimaryKey(deviceProtocolJavaClassName, messageAdapterJavaClassName);
    }

    @Override
    public String getDeviceProtocolJavaClassName() {
        return this.deviceProtocolJavaClassName;
    }

    @Override
    public String getMessageAdapterJavaClassName() {
        return this.messageAdapterJavaClassName;
    }

    static final class MessageAdapterMappingPrimaryKey implements Serializable {

        private final String deviceProtocolJavaClassName;
        private final String messageAdapterJavaClassName;

        public MessageAdapterMappingPrimaryKey(String deviceProtocolJavaClassName, String messageAdapterJavaClassName) {
            this.deviceProtocolJavaClassName = deviceProtocolJavaClassName;
            this.messageAdapterJavaClassName = messageAdapterJavaClassName;
        }

        private static int getNumberOfPrimaryKeyColumns() {
            return 2;
        }

        public String getDeviceProtocolJavaClassName() {
            return deviceProtocolJavaClassName;
        }

        public String getMessageAdapterJavaClassName() {
            return messageAdapterJavaClassName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            MessageAdapterMappingPrimaryKey that = (MessageAdapterMappingPrimaryKey) o;
            return deviceProtocolJavaClassName.equals(that.deviceProtocolJavaClassName) && messageAdapterJavaClassName.equals(that.messageAdapterJavaClassName);

        }

        @Override
        public int hashCode() {
            int result = deviceProtocolJavaClassName.hashCode();
            result = 31 * result + messageAdapterJavaClassName.hashCode();
            return result;
        }
    }
}
