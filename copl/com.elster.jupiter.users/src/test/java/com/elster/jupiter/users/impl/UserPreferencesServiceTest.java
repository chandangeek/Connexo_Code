/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.users.PreferenceType;
import com.elster.jupiter.users.MessageSeeds;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserPreference;
import com.elster.jupiter.users.UserPreferencesService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.UtilModule;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.sql.SQLException;
import java.util.Locale;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserPreferencesServiceTest {

    @Rule
    public TestRule expectedErrorRule = new ExpectedConstraintViolationRule();

    private Injector injector;
    private UserPreferencesService userPrefsService;
    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();

    private static class MockModule extends AbstractModule {
        @Override
        protected void configure() {
           bind(BundleContext.class).toInstance(mock(BundleContext.class));  
           bind(EventAdmin.class).toInstance(mock(EventAdmin.class));
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
        }
    }
    
    @Before
    public void setUp() throws Exception {
        injector = Guice.createInjector(
                new MockModule(),
                inMemoryBootstrapModule,
                new UserModule(),
                //new EventsModule(),
                new InMemoryMessagingModule(),
                new DomainUtilModule(),
                new OrmModule(),
                new UtilModule(),
                new ThreadSecurityModule(),
                new PubSubModule(),
                new TransactionModule(true),
                new NlsModule(),
                new DataVaultModule());
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            userPrefsService = injector.getInstance(UserService.class).getUserPreferencesService();
            ctx.commit();
        }
    }
    
    @After
    public void tearDown() throws SQLException {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void testGetInstalledPreferences_CheckLocales() {
        assertThat(userPrefsService.getSupportedLocales()).containsExactly(Locale.ENGLISH, Locale.US);
    }
    
    @Test
    public void testGetInstalledPreferences_CheckPreferences() {
        User user = mock(User.class);
        when(user.getLocale()).thenReturn(Optional.of(Locale.US));

        assertThat(userPrefsService.getPreferences(user)).hasSize(PreferenceType.values().length);
    }
    
    @Test
    public void testGetInstalledPreferences_CheckPreferencesByKey() {
        User user = mock(User.class);
        when(user.getLocale()).thenReturn(Optional.of(Locale.ENGLISH));
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            userPrefsService.createUserPreference(Locale.ENGLISH, PreferenceType.LONG_DATE, "test", "test", false);
            ctx.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Optional<UserPreference> preference = userPrefsService.getPreferenceByKey(user, PreferenceType.LONG_DATE);

        assertThat(preference.isPresent()).isTrue();
        assertThat(preference.get().getLocale()).isEqualTo(Locale.ENGLISH);
        assertThat(preference.get().getType()).isEqualTo(PreferenceType.LONG_DATE);
        assertThat(preference.get().getFormat()).isNotEmpty();
        assertThat(preference.get().getDisplayFormat()).isNotEmpty();
        assertThat(preference.get().isDefault()).isTrue();
    }
    
    @Test
    public void testCreateNewUserPreferences() {
        try (TransactionContext ctx = injector.getInstance(TransactionService.class).getContext()) {
            userPrefsService.createUserPreference(Locale.FRANCE, PreferenceType.LONG_DATE, "fr_be", "fr_fe", true);
            ctx.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }

        User user = mock(User.class);
        when(user.getLocale()).thenReturn(Optional.of(Locale.FRANCE));
        Optional<UserPreference> preference = userPrefsService.getPreferenceByKey(user, PreferenceType.LONG_DATE);

        assertThat(preference.isPresent()).isTrue();
        assertThat(preference.get().getLocale()).isEqualTo(Locale.FRANCE);
        assertThat(preference.get().getType()).isEqualTo(PreferenceType.LONG_DATE);
        assertThat(preference.get().getFormat()).isEqualTo("fr_be");
        assertThat(preference.get().getDisplayFormat()).isEqualTo("fr_fe");
        assertThat(preference.get().isDefault()).isTrue();
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}", property = "locale", strict = false)
    public void testCreateUserPreferencesWithoutLocale() {
        userPrefsService.createUserPreference(null, PreferenceType.DECIMAL_PRECISION, "fr_be", "fr_fe", true);
    }

    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}", property = "key", strict = false)
    public void testCreateUserPreferencesWithoutKey() {
        userPrefsService.createUserPreference(Locale.US, null, "fr_be", "fr_fe", true);
    }
    
    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}", property = "formatBE", strict = false)
    public void testCreateUserPreferencesWithoutFormatBE() {
        userPrefsService.createUserPreference(Locale.US, PreferenceType.CURRENCY, null, "fr_fe", true);
    }
    
    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}", property = "formatFE", strict = false)
    public void testCreateUserPreferencesWithoutFormatFE() {
        userPrefsService.createUserPreference(Locale.US, PreferenceType.CURRENCY, "be", null, true);
    }
    
    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_80 + "}", property = "formatBE", strict = false)
    public void testCreateUserPreferencesFormatBEWrongSize() {
        userPrefsService.createUserPreference(Locale.US, PreferenceType.CURRENCY, "", "fe", true);
    }
    
    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_80 + "}", property = "formatFE", strict = false)
    public void testCreateUserPreferencesFormatFEWrongSize() {
        userPrefsService.createUserPreference(Locale.US, PreferenceType.CURRENCY, "be", "", true);
    }
    
    @Test
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.ONLY_ONE_DEFAULT_KEY_PER_LOCALE_ALLOWED + "}", strict = false)
    public void testCreateUserPreferenceButDefaultAlreadyInstalled() {
        userPrefsService.createUserPreference(Locale.US, PreferenceType.CURRENCY, "be", "fe", true);
    }

}
