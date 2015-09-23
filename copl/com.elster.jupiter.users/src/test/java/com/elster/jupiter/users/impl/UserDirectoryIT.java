package com.elster.jupiter.users.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.*;
import com.elster.jupiter.util.UtilModule;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class UserDirectoryIT {

    private Injector injector;

    @Mock
    private BundleContext bundleContext;
    @Mock
    private EventAdmin eventAdmin;


    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();


    private class MockModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(bundleContext);
            bind(EventAdmin.class).toInstance(eventAdmin);
        }
    }

    @Before
    public void setUp() throws SQLException {
        try {
            injector = Guice.createInjector(
                    new MockModule(),
                    inMemoryBootstrapModule,
                    new DomainUtilModule(),
                    new OrmModule(),
                    new UtilModule(),
                    new ThreadSecurityModule(),
                    new PubSubModule(),
                    new TransactionModule(),
                    new UserModule(),
                    new NlsModule());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        injector.getInstance(TransactionService.class).execute(new Transaction<Void>() {
            @Override
            public Void perform() {
                injector.getInstance(UserService.class);
                return null;
            }
        });
    }

    @After
    public void tearDown() {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void testPersistenceInternalDirectory() {
        TransactionService transactionService = injector.getInstance(TransactionService.class);
        UserService userService = injector.getInstance(UserService.class);
        try (TransactionContext context = transactionService.getContext()) {
            UserDirectory internalDirectory = userService.createInternalDirectory("MyDomain");
            internalDirectory.setDefault(true);
            internalDirectory.save();
            context.commit();
        }
        Optional<UserDirectory> found = userService.findUserDirectory("MyDomain");

        assertThat(found.isPresent()).isTrue();

        UserDirectory userDirectory = found.get();
        assertThat(userDirectory).isInstanceOf(InternalDirectoryImpl.class);
        assertThat(userDirectory.isDefault()).isTrue();
        assertThat(userDirectory.getDomain()).isEqualTo("MyDomain");
    }

    @Test
    public void testPersistenceActiveDirectory() {
        TransactionService transactionService = injector.getInstance(TransactionService.class);
        UserService userService = injector.getInstance(UserService.class);
        try (TransactionContext context = transactionService.getContext()) {
            LdapUserDirectory activeDirectory = userService.createActiveDirectory("MyDomain");
            activeDirectory.setDefault(true);
            activeDirectory.setDirectoryUser("MyUser");
            activeDirectory.setPassword("MyPassword");
            activeDirectory.setUrl("MyUrl");
            activeDirectory.setBaseUser("BaseUser");
            activeDirectory.setBackupUrl("backupUrl");
            activeDirectory.setSecurity("NONE");
            activeDirectory.save();
            context.commit();
        }
        Optional<UserDirectory> found = userService.findUserDirectory("MyDomain");

        assertThat(found.isPresent()).isTrue();

        UserDirectory userDirectory = found.get();
        assertThat(userDirectory).isInstanceOf(ActiveDirectoryImpl.class);
        assertThat(userDirectory.isDefault()).isTrue();
        assertThat(userDirectory.getDomain()).isEqualTo("MyDomain");
        assertThat(((LdapUserDirectory)userDirectory).getDirectoryUser()).isEqualTo("MyUser");
        assertThat(((LdapUserDirectory)userDirectory).getPassword()).isEqualTo("MyPassword");
        assertThat(((LdapUserDirectory)userDirectory).getUrl()).isEqualTo("MyUrl");
    }

    @Test
    public void testPersistenceApacheDirectory() {
        TransactionService transactionService = injector.getInstance(TransactionService.class);
        UserService userService = injector.getInstance(UserService.class);
        try (TransactionContext context = transactionService.getContext()) {
            LdapUserDirectory apacheDirectory = userService.createApacheDirectory("MyDomain");
            apacheDirectory.setDefault(true);
            apacheDirectory.setDirectoryUser("MyUser");
            apacheDirectory.setPassword("MyPassword");
            apacheDirectory.setUrl("MyUrl");
            apacheDirectory.setBaseUser("BaseUser");
            apacheDirectory.setBackupUrl("backupUrl");
            apacheDirectory.setSecurity("NONE");
            apacheDirectory.save();
            context.commit();
        }
        Optional<UserDirectory> found = userService.findUserDirectory("MyDomain");

        assertThat(found.isPresent()).isTrue();

        UserDirectory userDirectory = found.get();
        assertThat(userDirectory).isInstanceOf(ApacheDirectoryImpl.class);
        assertThat(userDirectory.isDefault()).isTrue();
        assertThat(userDirectory.getDomain()).isEqualTo("MyDomain");
        assertThat(((LdapUserDirectory)userDirectory).getDirectoryUser()).isEqualTo("MyUser");
        assertThat(((LdapUserDirectory)userDirectory).getPassword()).isEqualTo("MyPassword");
        assertThat(((LdapUserDirectory)userDirectory).getUrl()).isEqualTo("MyUrl");
    }



    @Ignore
    @Test
    public void testAuthenticateActiveDirectory() {
        //TODO: change credentials according to a valid account
        TransactionService transactionService = injector.getInstance(TransactionService.class);
        UserService userService = injector.getInstance(UserService.class);
        try (TransactionContext context = transactionService.getContext()) {
            LdapUserDirectory activeDirectory = userService.createActiveDirectory("LDAP Active Directory");
            activeDirectory.setDefault(true);
            activeDirectory.setDirectoryUser("iancud@rimroe.elster-group.com");
            activeDirectory.setPassword("xxxxxx");
            activeDirectory.setUrl("ldap://localhost:389");
            activeDirectory.setBaseUser("OU=Users,OU=ROGHI01,DC=rimroe,DC=elster-group,DC=com");
            activeDirectory.setBackupUrl("ldap://localhost:12389");
            activeDirectory.setSecurity("NONE");
            activeDirectory.save();
            Optional<User> user = activeDirectory.authenticate("iancud","xxxxxx");
            Optional<User> user1 = activeDirectory.authenticate("iancud","xxxxxx");
            List<Group> groups = activeDirectory.getGroups(user1.get());
            List<Group> groups1 = activeDirectory.getGroups(user1.get());
            context.commit();
        }

        Optional<UserDirectory> found = userService.findUserDirectory("LDAP Active Directory");

        assertThat(found.isPresent()).isTrue();

        UserDirectory userDirectory = found.get();
        assertThat(userDirectory).isInstanceOf(ActiveDirectoryImpl.class);
        assertThat(userDirectory.isDefault()).isTrue();
        assertThat(userDirectory.getDomain()).isEqualTo("LDAP Active Directory");
        assertThat(((LdapUserDirectory)userDirectory).getDirectoryUser()).isEqualTo("iancud@rimroe.elster-group.com");
        assertThat(((LdapUserDirectory)userDirectory).getUrl()).isEqualTo("ldap://10.29.131.222:389");
    }


    @Ignore
    @Test
    public void testAuthenticateApacheDirectory() {
        //TODO: change credentials according to a valid account
        TransactionService transactionService = injector.getInstance(TransactionService.class);
        UserService userService = injector.getInstance(UserService.class);
        try (TransactionContext context = transactionService.getContext()) {
            LdapUserDirectory apacheDirectory = userService.createApacheDirectory("LDAP Apache Directory");
            apacheDirectory.setDefault(true);
            apacheDirectory.setDirectoryUser("uid=admin,ou=system");
            apacheDirectory.setPassword("admin");
            apacheDirectory.setUrl("ldap://localhost:10389");
            apacheDirectory.setBaseUser("ou=users,dc=WSO2,dc=ORG");
            apacheDirectory.setBaseGroup("ou=Groups,dc=WSO2,dc=ORG");
            apacheDirectory.setBackupUrl("ldap://localhost:11389");
            apacheDirectory.setSecurity("NONE");
            apacheDirectory.save();
            Optional<User> user = apacheDirectory.authenticate("root","tester");
            Optional<User> user1 = apacheDirectory.authenticate("root","tester");
            List<Group> groups = apacheDirectory.getGroups(user1.get());
            List<Group> groups1 = apacheDirectory.getGroups(user1.get());
            context.commit();
        }

        Optional<UserDirectory> found = userService.findUserDirectory("LDAP Apache Directory");

        assertThat(found.isPresent()).isTrue();

        UserDirectory userDirectory = found.get();
        assertThat(userDirectory).isInstanceOf(ApacheDirectoryImpl.class);
        assertThat(userDirectory.isDefault()).isTrue();
        assertThat(userDirectory.getDomain()).isEqualTo("LDAP Apache Directory");
        assertThat(((LdapUserDirectory)userDirectory).getDirectoryUser()).isEqualTo("uid=admin,ou=system");
        assertThat(((LdapUserDirectory)userDirectory).getUrl()).isEqualTo("ldap://localhost:10389");
    }


    @Test
    public void testEditApacheDirectory() {
        TransactionService transactionService = injector.getInstance(TransactionService.class);
        UserService userService = injector.getInstance(UserService.class);
        try (TransactionContext context = transactionService.getContext()) {
            LdapUserDirectory apacheDirectory = userService.createApacheDirectory("MyDomain");
            apacheDirectory.setDefault(true);
            apacheDirectory.setDirectoryUser("MyUser");
            apacheDirectory.setPassword("MyPassword");
            apacheDirectory.setUrl("MyUrl");
            apacheDirectory.setBaseUser("BaseUser");
            apacheDirectory.setBackupUrl("BackupUrl");
            apacheDirectory.setSecurity("NONE");
            apacheDirectory.save();
            context.commit();
        }
        Optional<UserDirectory> found = userService.findUserDirectory("MyDomain");
        assertThat(found.isPresent()).isTrue();
        try (TransactionContext context = transactionService.getContext()) {
            LdapUserDirectory apacheDirectory = (LdapUserDirectory) found.get();
            apacheDirectory.setDirectoryUser("MyUser2");
            apacheDirectory.setUrl("MyUrl2");
            apacheDirectory.setBackupUrl("BackupUrl2");
            apacheDirectory.setSecurity("SSL");
            apacheDirectory.save();
            context.commit();
        }
        Optional<UserDirectory> foundEdited = userService.findUserDirectory("MyDomain");
        LdapUserDirectory userDirectory = (LdapUserDirectory) foundEdited.get();

        assertThat(userDirectory).isInstanceOf(ApacheDirectoryImpl.class);
        assertThat(userDirectory.getDirectoryUser()).isEqualTo("MyUser2");
        assertThat(userDirectory.getUrl()).isEqualTo("MyUrl2");
        assertThat(userDirectory.getBackupUrl()).isEqualTo("BackupUrl2");
        assertThat(userDirectory.getSecurity()).isNotEqualTo("NONE");
    }

    public void testEditActiveDirectory() {
        TransactionService transactionService = injector.getInstance(TransactionService.class);
        UserService userService = injector.getInstance(UserService.class);
        try (TransactionContext context = transactionService.getContext()) {
            LdapUserDirectory activeDirectory = userService.createActiveDirectory("MyDomain");
            activeDirectory.setDefault(true);
            activeDirectory.setDirectoryUser("MyUser");
            activeDirectory.setPassword("MyPassword");
            activeDirectory.setUrl("MyUrl");
            activeDirectory.setBaseUser("BaseUser");
            activeDirectory.setBackupUrl("backupUrl");
            activeDirectory.setSecurity("NONE");
            activeDirectory.save();
            context.commit();
        }
        Optional<UserDirectory> found = userService.findUserDirectory("MyDomain");
        assertThat(found.isPresent()).isTrue();

        try (TransactionContext context = transactionService.getContext()) {
            LdapUserDirectory activeDirectory = (LdapUserDirectory) found.get();
            activeDirectory.setDirectoryUser("MyUser2");
            activeDirectory.setUrl("MyUrl2");
            activeDirectory.setBackupUrl("BackupUrl2");
            activeDirectory.setSecurity("SSL");
            activeDirectory.save();
            context.commit();
        }
        Optional<UserDirectory> foundEdited = userService.findUserDirectory("MyDomain");
        LdapUserDirectory userDirectory = (LdapUserDirectory) foundEdited.get();

        assertThat(userDirectory).isInstanceOf(ApacheDirectoryImpl.class);
        assertThat(userDirectory.getDirectoryUser()).isEqualTo("MyUser2");
        assertThat(userDirectory.getUrl()).isEqualTo("MyUrl2");
        assertThat(userDirectory.getBackupUrl()).isEqualTo("BackupUrl2");
        assertThat(userDirectory.getSecurity()).isNotEqualTo("NONE");
    }

}
