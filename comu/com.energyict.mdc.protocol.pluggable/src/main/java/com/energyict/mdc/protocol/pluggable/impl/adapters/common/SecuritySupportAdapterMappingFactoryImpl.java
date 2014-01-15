package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.energyict.cpo.PersistentObjectFactory;
import com.energyict.mdc.common.SqlBuilder;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

/**
 * This factory will load and cache a list of
 * {@link SecuritySupportAdapterMapping securitySupportAdapterMappings}.
 * The factory is not foreseen to create mappings,
 * only for fetching them. These mappings
 * are created during migration.
 *
 * Copyrights EnergyICT
 * Date: 12/04/13
 * Time: 11:03
 */
public class SecuritySupportAdapterMappingFactoryImpl extends PersistentObjectFactory<SecuritySupportAdapterMapping> implements SecuritySupportAdapterMappingFactory {

    public static final String TABLENAME = "mdcssadaptermapping";

    static final String[] COLUMNS = {
            "deviceprotocoljavaclassname",
            "securitysupportclassname"
    };

    private static final String NOT_NULL = "not null";

    private List<SecuritySupportAdapterMapping> cachedSecuritySupportMappings;

    @Override
    protected SecuritySupportAdapterMapping construct(ResultSet resultSet) throws SQLException {
        return new SecuritySupportAdapterMappingImpl(resultSet);
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
    public SecuritySupportAdapterMapping findByPrimaryKey(Serializable key) {
        return this.findByPrimaryKey((SecuritySupportAdapterMappingImpl.SecuritySupportMappingPrimaryKey) key);
    }

    public SecuritySupportAdapterMapping findByPrimaryKey(SecuritySupportAdapterMappingImpl.SecuritySupportMappingPrimaryKey key) {
        SqlBuilder builder = new SqlBuilder(selectClause());
        builder.append(" where deviceprotocoljavaclassname = ? and securitysupportclassname = ? ");
        builder.bindString(key.getDeviceProtocolJavaClassName());
        builder.bindString(key.getSecuritySupportJavaClassName());
        return fetchFirst(builder);
    }

    @Override
    public String getSecuritySupportJavaClassNameForDeviceProtocol(String deviceProtocolJavaClassName) {
        // we will try to cache all mappings
        if (this.cachedSecuritySupportMappings == null) {
            this.cachedSecuritySupportMappings = findAll();
        }
        return getSecuritySupportFromCachedMapping(deviceProtocolJavaClassName, cachedSecuritySupportMappings);
    }

    @Override
    public String getInsertStatement() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("insert into ");
        stringBuilder.append(TABLENAME);
        stringBuilder.append(" (deviceprotocoljavaclassname, securitysupportclassname) values (?,?) ");
        return stringBuilder.toString();
    }

    @Override
    public void clearCache() {
        this.cachedSecuritySupportMappings = null;
    }

    private String getSecuritySupportFromCachedMapping(String deviceProtocolJavaClassName, List<SecuritySupportAdapterMapping> cachedSecuritySupportMappings) {
        for (SecuritySupportAdapterMapping cachedSecuritySupportMapping : cachedSecuritySupportMappings) {
            if (cachedSecuritySupportMapping.getDeviceProtocolJavaClassName().equals(deviceProtocolJavaClassName)) {
                return cachedSecuritySupportMapping.getSecuritySupportJavaClassName();
            }
        }
        return null;
    }

    public int[] getColumnTypes() {
        return new int[]{
                Types.VARCHAR,      // deviceprotocoljavaclassname
                Types.VARCHAR       // securitysupportclassname
        };
    }

    public String[] getColumnConstraints() {
        return new String[]{
                NOT_NULL,          // deviceprotocoljavaclassname
                NOT_NULL           // securitysupportclassname
        };
    }
}
