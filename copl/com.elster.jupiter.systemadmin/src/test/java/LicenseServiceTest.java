import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.license.InvalidLicenseException;
import com.elster.jupiter.license.License;
import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.license.impl.LicenseServiceImpl;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.users.impl.UserModule;
import com.elster.jupiter.util.UtilModule;

import java.util.Optional;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Scopes;

import org.junit.*;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.security.SignedObject;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static junit.framework.TestCase.fail;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
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
            bind(EventAdmin.class).toInstance(mock(EventAdmin.class));
            //  bind(Clock.class).toInstance(new ProgrammableClock().frozenAt(new DateTime(2014, 4, 14, 12, 32, 13, 200).toDate()));
        }
    };

    @BeforeClass
    public static void setUp() {
        injector = Guice.createInjector(inMemoryBootstrapModule, licenseModule,
                new OrmModule(), new TransactionModule(), new PubSubModule(), new ThreadSecurityModule(),
                new UtilModule(), new DomainUtilModule(), new UserModule(), new EventsModule(),
                new InMemoryMessagingModule(), new NlsModule());
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            getLicenseService();
            getUserService();
            ctx.commit();
        }
    }

    private static LicenseService getLicenseService() {
        return injector.getInstance(LicenseService.class);
    }

    private TransactionService getTransactionService() {
        return injector.getInstance(TransactionService.class);
    }

    private static UserService getUserService() {
        return injector.getInstance(UserService.class);
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
        assertThat(licensedApplicationKeys).contains("MTR", "ISU");

        Optional<License> mtrLicense = getLicenseService().getLicenseForApplication("MTR");
        assertThat(mtrLicense.isPresent()).isTrue();
        assertThat(mtrLicense.get().getApplicationKey()).isEqualTo("MTR");
        assertThat(mtrLicense.get().getDescription()).isEqualTo("MTR application license example");
        assertThat(mtrLicense.get().getStatus()).isEqualTo(License.Status.ACTIVE);
        assertThat(mtrLicense.get().getType()).isEqualTo(License.Type.EVALUATION);
        assertThat(mtrLicense.get().getGracePeriodInDays()).isEqualTo(5);
        assertThat(mtrLicense.get().getExpiration()).isEqualTo(LocalDate.of(9999, 12, 31).atStartOfDay(ZoneOffset.UTC).toInstant());
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
        assertThat(isuLicense.get().getExpiration()).isEqualTo(LocalDate.of(9999, 12, 31).atStartOfDay(ZoneOffset.UTC).toInstant());
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
        Instant firstActivationDate = getLicenseService().getLicenseForApplication("MTR").get().getActivation();
        Set<String> updatedLicenses = getLicenseService().addLicense(getResource("updatedMTRLicense.lic"));
        assertThat(updatedLicenses).hasSize(1);
        assertThat(updatedLicenses).containsExactly("MTR");
        licensedValue = getLicenseService().getLicensedValue("MTR", "key1");
        assertThat(licensedValue.isPresent()).isTrue();
        assertThat(licensedValue.get()).isEqualTo("test2");
        assertThat(getLicenseService().getLicenseForApplication("MTR").get().getActivation().isAfter(firstActivationDate));
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

    @Test
    @Transactional
    public void testExpiredLicense() throws Exception {
        Set<String> licenseKey = getLicenseService().addLicense(getResource("expiredlicense.lic"));

        assertThat(licenseKey).hasSize(1);
        Optional<License> licenseForApplication = getLicenseService().getLicenseForApplication(licenseKey.iterator().next());
        assertTrue(licenseForApplication.isPresent());
        assertThat(licenseForApplication.get().getStatus()).isEqualTo(License.Status.EXPIRED);
        assertThat(licenseForApplication.get().getGracePeriodInDays()).isEqualTo(0);
        assertThat(licenseForApplication.get().getLicensedValues()).isEmpty();
    }


}
