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
public class MessageAdapterMappingFactoryImpl extends PersistentObjectFactory<MessageAdapterMapping> implements MessageAdapterMappingFactory {

    public static final String TABLENAME = "mdcmessageadaptermapping";

    static final String[] COLUMNS = {
            "deviceprotocoljavaclassname",
            "messageadapterjavaclassname"
    };

    private static final String NOT_NULL = "not null";

    private List<MessageAdapterMapping> cachedMessageAdapterMappings;

    @Override
    protected MessageAdapterMapping construct(ResultSet resultSet) throws SQLException {
        return new MessageAdapterMappingImpl(resultSet);
    }


    @Override
    public String getMessageMappingJavaClassNameForDeviceProtocol(String deviceProtocolJavaClassName) {
        // we will try to cache all mappings
        if (this.cachedMessageAdapterMappings == null) {
            this.cachedMessageAdapterMappings = findAll();
        }
        return getMessageAdapterFromCachedMapping(deviceProtocolJavaClassName, cachedMessageAdapterMappings);
    }

    private String getMessageAdapterFromCachedMapping(String deviceProtocolJavaClassName, List<MessageAdapterMapping> cachedMessageAdapterMappings) {
        for (MessageAdapterMapping messageAdapterMapping : cachedMessageAdapterMappings) {
            if (messageAdapterMapping.getDeviceProtocolJavaClassName().equals(deviceProtocolJavaClassName)) {
                return messageAdapterMapping.getMessageAdapterJavaClassName();
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
    public MessageAdapterMapping findByPrimaryKey(Serializable key) {
        return this.findByPrimaryKey((MessageAdapterMappingImpl.MessageAdapterMappingPrimaryKey) key);
    }

    private MessageAdapterMapping findByPrimaryKey(MessageAdapterMappingImpl.MessageAdapterMappingPrimaryKey key) {
        SqlBuilder builder = new SqlBuilder(selectClause());
        builder.append(" where deviceprotocoljavaclassname = ? and messageadapterjavaclassname = ? ");
        builder.bindString(key.getDeviceProtocolJavaClassName());
        builder.bindString(key.getMessageAdapterJavaClassName());
        return fetchFirst(builder);
    }

    @Override
    public String getInsertStatement() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("insert into ");
        stringBuilder.append(TABLENAME);
        stringBuilder.append(" (deviceprotocoljavaclassname, messageadapterjavaclassname) values (?,?) ");
        return stringBuilder.toString();
    }

    @Override
    public void clearCache() {
        this.cachedMessageAdapterMappings = null;
    }


    public int[] getColumnTypes() {
        return new int[]{
                Types.VARCHAR,      // deviceprotocoljavaclassname
                Types.VARCHAR       // messageadapterjavaclassname
        };
    }

    public String[] getColumnConstraints() {
        return new String[]{
                NOT_NULL,          // deviceprotocoljavaclassname
                NOT_NULL           // messageadapterjavaclassname
        };
    }
}
