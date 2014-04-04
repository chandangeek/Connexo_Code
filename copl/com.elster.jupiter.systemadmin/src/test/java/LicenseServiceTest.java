import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.license.InvalidLicenseException;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.license.impl.LicenseServiceImpl;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.util.UtilModule;
import com.google.common.base.Optional;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Scopes;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.osgi.framework.BundleContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.security.SignedObject;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import static junit.framework.TestCase.fail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Copyrights EnergyICT
 * Date: 2/04/2014
 * Time: 13:23
 */
public class LicenseServiceTest {
    private static InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private static Injector injector;

    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(injector.getInstance(TransactionService.class));


    private static Module licenseModule = new AbstractModule() {
        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(mock(BundleContext.class));
            bind(LicenseService.class).to(LicenseServiceImpl.class).in(Scopes.SINGLETON);
        }
    };

    @BeforeClass
    public static void setUp() {
        injector = Guice.createInjector(inMemoryBootstrapModule, licenseModule,
                new OrmModule(), new TransactionModule(), new PubSubModule(), new ThreadSecurityModule(), new UtilModule());
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            getLicenseService();
            ctx.commit();
        }
    }

    private static LicenseService getLicenseService() {
        return injector.getInstance(LicenseService.class);
    }

    private TransactionService getTransactionService() {
        return injector.getInstance(TransactionService.class);
    }

    @AfterClass
    public static void tearDown() throws SQLException {
        inMemoryBootstrapModule.deactivate();
    }

    @After
    public void clearCache() {
        ((LicenseServiceImpl) injector.getInstance(LicenseService.class)).clearCache();
    }

    private SignedObject getResource(String resource) throws IOException, ClassNotFoundException {
        try (InputStream baseStream = getClass().getClassLoader().getResourceAsStream(resource)) {
            ObjectInputStream stream = new ObjectInputStream(baseStream);
            return (SignedObject) stream.readObject();
        }

    }

    @Test
    @Transactional
    public void testAddLicense() throws Exception {
            /*
            Properties props = new Properties();
        props.setProperty("key1", "test");
        props.setProperty("key2", Integer.toString(1));

        Hashtable<String, SignedObject> license = new Hashtable<>();
        license.put("MTR", sign(props));
        license.put("ISU", sign(new Properties()));
             */
        getLicenseService().addLicense(getResource("validLicense.lic"));

        List<String> licensedApplicationKeys = getLicenseService().getLicensedApplicationKeys();
        assertThat(licensedApplicationKeys).hasSize(2);
        assertThat(licensedApplicationKeys).containsExactly("MTR", "ISU");

        Optional<Properties> mtrLicensedValues = getLicenseService().getLicensedValuesForApplication("MTR");
        assertThat(mtrLicensedValues.isPresent()).isTrue();
        assertThat(mtrLicensedValues.get()).hasSize(3);
        assertThat(mtrLicensedValues.get()).containsKeys("key1", "key2", LicenseService.LICENSE_CREATION_DATE_KEY);
        assertThat(mtrLicensedValues.get()).containsEntry("key1", "test").containsEntry("key2", "1");

        Optional<Properties> isuLicensedValues = getLicenseService().getLicensedValuesForApplication("ISU");
        assertThat(isuLicensedValues.isPresent()).isTrue();
        assertThat(isuLicensedValues.get()).hasSize(1);
        assertThat(isuLicensedValues.get()).containsKey(LicenseService.LICENSE_CREATION_DATE_KEY);

        Optional<Properties> otherLicensedValues = getLicenseService().getLicensedValuesForApplication("OTH");
        assertThat(otherLicensedValues.isPresent()).isFalse();
    }

    @Test
    @Transactional
    public void testLicensedValue() throws Exception {
        getLicenseService().addLicense(getResource("validLicense.lic"));

        Optional<String> licensedValue = getLicenseService().getLicensedValue("MTR", "key1");
        assertThat(licensedValue.isPresent()).isTrue();
        assertThat(licensedValue.get()).isEqualTo("test");

        licensedValue = getLicenseService().getLicensedValue("MTR", "key3");
        assertThat(licensedValue.isPresent()).isFalse();

        licensedValue = getLicenseService().getLicensedValue("ISU", "key1");
        assertThat(licensedValue.isPresent()).isFalse();


    }

    @Test
    @Transactional
    public void testUpdateLicense() throws Exception {
        getLicenseService().addLicense(getResource("validLicense.lic"));
        Optional<String> licensedValue = getLicenseService().getLicensedValue("MTR", "key1");
        assertThat(licensedValue.isPresent()).isTrue();
        assertThat(licensedValue.get()).isEqualTo("test");
        getLicenseService().addLicense(getResource("updatedMTRLicense.lic"));
        licensedValue = getLicenseService().getLicensedValue("MTR", "key1");
        assertThat(licensedValue.isPresent()).isTrue();
        assertThat(licensedValue.get()).isEqualTo("test2");
    }

    @Test(expected = InvalidLicenseException.class)
    public void testAddInvalidLicense() throws Exception {
        getLicenseService().addLicense(getResource("invalidLicense.lic"));
    }

    @Transactional
    public void testUpdateLicenseWithSameFile() throws Exception {
        getLicenseService().addLicense(getResource("validLicense.lic"));
        try {
            getLicenseService().addLicense(getResource("validLicense.lic"));
            fail("expected an exception");
        } catch (InvalidLicenseException ile) {
            //fine
        }
    }

    @Test
    @Transactional
    public void testLicenseTampering() throws Exception {
        getLicenseService().addLicense(getResource("validLicense.lic"));

        Optional<Properties> mtrLicensedValues = getLicenseService().getLicensedValuesForApplication("MTR");
        assertThat(mtrLicensedValues.isPresent()).isTrue();
        mtrLicensedValues.get().setProperty("key2", "5000");

        mtrLicensedValues = getLicenseService().getLicensedValuesForApplication("MTR");
        assertThat(mtrLicensedValues.isPresent()).isTrue();
        assertThat(mtrLicensedValues.get()).containsEntry("key2", "1");
    }


}
