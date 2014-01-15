package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.energyict.cpo.BatchStatement;
import com.energyict.mdc.common.Environment;
import com.energyict.mdc.InMemoryPersistence;
import com.energyict.mdc.meta.persistence.SecuritySupportAdapterMappingTableDescription;
import com.energyict.mdc.meta.persistence.model.DatabaseModel;
import com.energyict.mdc.meta.persistence.model.DatabaseModelImpl;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.DefaultSecuritySupportAdapterMappingFactoryProvider;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.SecuritySupportAdapterMappingFactory;
import com.energyict.mdc.protocol.pluggable.impl.adapters.common.SecuritySupportAdapterMappingFactoryProvider;
import org.junit.*;
import org.junit.runner.*;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Test the SecuritySupportAdapterMappingFactoryImpl component
 * <p/>
 * Copyrights EnergyICT
 * Date: 15/04/13
 * Time: 9:42
 */
@RunWith(MockitoJUnitRunner.class)
public class SecuritySupportAdapterMappingFactoryImplTest {

    private static Properties defaultProperties = new Properties();

    private static final String firstKey = "com.eict.deviceprotocol1";
    private static final String secondKey = "com.eict.deviceprotocol2";
    private static final String thirdKey = "com.eict.deviceprotocol3";
    private static final String firstSecuritySupport = "com.eict.securitysupport1";
    private static final String secondSecuritySupport = "com.eict.securitysupport2";

    static{
        defaultProperties.setProperty(firstKey, firstSecuritySupport);
        defaultProperties.setProperty(secondKey, firstSecuritySupport);
        defaultProperties.setProperty(thirdKey, secondSecuritySupport);
    }

    @BeforeClass
    public static void initialize() throws IOException, SQLException {
        SecuritySupportAdapterMappingFactoryProvider.INSTANCE.set(new DefaultSecuritySupportAdapterMappingFactoryProvider());
        InMemoryPersistence.initializeDatabase();
        createTableModel();
    }

    private static void createTableModel() throws SQLException {
        getDatabaseModel().create(Environment.DEFAULT.get().getConnection());
    }

    private static DatabaseModel getDatabaseModel() {
        DatabaseModel databaseModel = new DatabaseModelImpl();
        databaseModel.add(new SecuritySupportAdapterMappingTableDescription());
        return databaseModel;
    }

    @AfterClass
    public static void cleanupDatabase () throws SQLException {
        InMemoryPersistence.cleanUpDataBase();
    }

    @Test
    public void cachingTest() throws SQLException {
        insertDefaultMappings();

        // asserts (all results should be null, the findAll is already called and the mapping is currently an empty list
        assertThat(getSecuritySupportAdapterMappingFactory().getSecuritySupportJavaClassNameForDeviceProtocol(firstKey)).isNull();
        assertThat(getSecuritySupportAdapterMappingFactory().getSecuritySupportJavaClassNameForDeviceProtocol(secondKey)).isNull();
        assertThat(getSecuritySupportAdapterMappingFactory().getSecuritySupportJavaClassNameForDeviceProtocol(thirdKey)).isNull();

        // business method
        getSecuritySupportAdapterMappingFactory().clearCache();

        assertThat(getSecuritySupportAdapterMappingFactory().getSecuritySupportJavaClassNameForDeviceProtocol(firstKey)).isEqualTo(firstSecuritySupport);
        assertThat(getSecuritySupportAdapterMappingFactory().getSecuritySupportJavaClassNameForDeviceProtocol(secondKey)).isEqualTo(firstSecuritySupport);
        assertThat(getSecuritySupportAdapterMappingFactory().getSecuritySupportJavaClassNameForDeviceProtocol(thirdKey)).isEqualTo(secondSecuritySupport);
    }

    private void insertDefaultMappings() throws SQLException {
        insertMappings(defaultProperties);
    }

    private void insertMappings(Properties properties) throws SQLException {
        BatchStatement batchStatement = new BatchStatement(getSecuritySupportAdapterMappingFactory().getInsertStatement(), 1000);
        bindInserts(batchStatement, properties);
        batchStatement.executeBatch();
    }

    private SecuritySupportAdapterMappingFactory getSecuritySupportAdapterMappingFactory() {
        return SecuritySupportAdapterMappingFactoryProvider.INSTANCE.get().getSecuritySupportAdapterMappingFactory();
    }

    private void bindInserts(BatchStatement batchStatement, Properties mappings) throws SQLException {
        for (String key : mappings.stringPropertyNames()) {
            if(getSecuritySupportAdapterMappingFactory().getSecuritySupportJavaClassNameForDeviceProtocol(key) == null){
                batchStatement.setString(1, key);
                batchStatement.setString(2, mappings.getProperty(key));
                batchStatement.addBatch();
            }
        }
    }
}
