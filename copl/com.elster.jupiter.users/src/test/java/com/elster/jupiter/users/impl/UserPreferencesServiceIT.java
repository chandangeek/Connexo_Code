/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.datavault.impl.DataVaultModule;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.h2.H2OrmModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.impl.UpgradeModule;
import com.elster.jupiter.users.MessageSeeds;
import com.elster.jupiter.users.PreferenceType;
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
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserPreferencesServiceIT {
    private static InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private static Injector injector;
    private static UserPreferencesService userPrefsService;

    @Rule
    public TestRule expectedErrorRule = new ExpectedConstraintViolationRule();
    @Rule
    public TestRule transactional = new TransactionalRule(injector.getInstance(TransactionService.class));

    private static class MockModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(BundleContext.class).toInstance(mock(BundleContext.class));
            bind(EventAdmin.class).toInstance(mock(EventAdmin.class));
            bind(UpgradeService.class).toInstance(UpgradeModule.FakeUpgradeService.getInstance());
        }
    }

    @BeforeClass
    public static void setUp() throws Exception {
        injector = Guice.createInjector(
                new MockModule(),
                inMemoryBootstrapModule,
                new UserModule(),
                //new EventsModule(),
                new InMemoryMessagingModule(),
                new DomainUtilModule(),
                new H2OrmModule(),
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

    @AfterClass
    public static void tearDown() throws SQLException {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void testGetInstalledPreferences_CheckLocales() {
        assertThat(userPrefsService.getSupportedLocales()).containsExactly(Locale.ENGLISH, Locale.US);
    }

    @Test
    @Transactional
    public void testGetInstalledPreferences_CheckPreferences() {
        User user = mock(User.class);
        when(user.getLocale()).thenReturn(Optional.of(Locale.US));

        assertThat(userPrefsService.getPreferences(user)).hasSize(PreferenceType.values().length);
    }

    @Test
    @Transactional
    public void testGetInstalledPreferences_CheckPreferencesByKey() {
        User user = mock(User.class);
        when(user.getLocale()).thenReturn(Optional.of(Locale.ENGLISH));
        userPrefsService.createUserPreference(Locale.ENGLISH, PreferenceType.LONG_DATE, "test", "test", false);

        Optional<UserPreference> preference = userPrefsService.getPreferenceByKey(user, PreferenceType.LONG_DATE);

        assertThat(preference.isPresent()).isTrue();
        assertThat(preference.get().getLocale()).isEqualTo(Locale.ENGLISH);
        assertThat(preference.get().getType()).isEqualTo(PreferenceType.LONG_DATE);
        assertThat(preference.get().getFormat()).isNotEmpty();
        assertThat(preference.get().getDisplayFormat()).isNotEmpty();
        assertThat(preference.get().isDefault()).isTrue();
    }

    @Test
    @Transactional
    public void testCreateNewUserPreferences() {
        userPrefsService.createUserPreference(Locale.FRANCE, PreferenceType.LONG_DATE, "fr_be", "fr_fe", true);

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
    @Transactional
    public void testGetDateTimeFormatter() {
        User principal = mock(User.class);
        when(principal.getLocale()).thenReturn(Optional.of(Locale.FRANCE));
        DateTimeFormatter dateTimeFormatter = userPrefsService.getDateTimeFormatter(principal, PreferenceType.LONG_DATE, PreferenceType.LONG_TIME);

        assertThat(dateTimeFormatter.getLocale()).isEqualTo(Locale.FRANCE);
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
