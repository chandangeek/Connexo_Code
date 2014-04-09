import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.license.InvalidLicenseException;
import com.elster.jupiter.license.License;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.license.impl.LicenseServiceImpl;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.util.time.UtcInstant;
import com.google.common.base.Optional;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Scopes;
import org.joda.time.DateMidnight;
import org.joda.time.DateTimeZone;
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
import java.util.Set;

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
            //  bind(Clock.class).toInstance(new ProgrammableClock().frozenAt(new DateTime(2014, 4, 14, 12, 32, 13, 200).toDate()));
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
        getLicenseService().addLicense(getResource("validLicense.lic"));

        List<String> licensedApplicationKeys = getLicenseService().getLicensedApplicationKeys();
        assertThat(licensedApplicationKeys).hasSize(2);
        assertThat(licensedApplicationKeys).containsExactly("MTR", "ISU");

        Optional<License> mtrLicense = getLicenseService().getLicenseForApplication("MTR");
        assertThat(mtrLicense.isPresent()).isTrue();
        assertThat(mtrLicense.get().getApplicationKey()).isEqualTo("MTR");
        assertThat(mtrLicense.get().getDescription()).isEqualTo("MTR application license example");
        assertThat(mtrLicense.get().getStatus()).isEqualTo(License.Status.ACTIVE);
        assertThat(mtrLicense.get().getType()).isEqualTo(License.Type.EVALUATION);
        assertThat(mtrLicense.get().getGracePeriodInDays()).isEqualTo(5);
        assertThat(mtrLicense.get().getExpiration()).isEqualTo(new UtcInstant(new DateMidnight(9999, 12, 31, DateTimeZone.UTC).toDate()));
        assertThat(mtrLicense.get().getLicensedValues()).hasSize(2);

        Optional<Properties> mtrLicensedValues = getLicenseService().getLicensedValuesForApplication("MTR");
        assertThat(mtrLicensedValues.isPresent()).isTrue();
        assertThat(mtrLicensedValues.get()).hasSize(2);
        assertThat(mtrLicensedValues.get()).containsKeys("key1", "key2");
        assertThat(mtrLicensedValues.get()).containsEntry("key1", "test").containsEntry("key2", "1");

        Optional<License> isuLicense = getLicenseService().getLicenseForApplication("ISU");
        assertThat(isuLicense.isPresent()).isTrue();
        assertThat(isuLicense.get().getApplicationKey()).isEqualTo("ISU");
        assertThat(isuLicense.get().getDescription()).isEqualTo("ISU application license example");
        assertThat(isuLicense.get().getStatus()).isEqualTo(License.Status.ACTIVE);
        assertThat(isuLicense.get().getType()).isEqualTo(License.Type.EVALUATION);
        assertThat(isuLicense.get().getGracePeriodInDays()).isEqualTo(5);
        assertThat(isuLicense.get().getExpiration()).isEqualTo(new UtcInstant(new DateMidnight(9999, 12, 31, DateTimeZone.UTC).toDate()));
        assertThat(isuLicense.get().getLicensedValues()).hasSize(0);

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
        UtcInstant firstActivationDate = getLicenseService().getLicenseForApplication("MTR").get().getActivation();
        Set<String> updatedLicenses = getLicenseService().addLicense(getResource("updatedMTRLicense.lic"));
        assertThat(updatedLicenses).hasSize(1);
        assertThat(updatedLicenses).containsExactly("MTR");
        licensedValue = getLicenseService().getLicensedValue("MTR", "key1");
        assertThat(licensedValue.isPresent()).isTrue();
        assertThat(licensedValue.get()).isEqualTo("test2");
        assertThat(getLicenseService().getLicenseForApplication("MTR").get().getActivation().after(firstActivationDate));
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
