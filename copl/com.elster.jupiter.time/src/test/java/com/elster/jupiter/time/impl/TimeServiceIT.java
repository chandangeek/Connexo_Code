package com.elster.jupiter.time.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.time.DefaultRelativePeriodDefinition;
import com.elster.jupiter.time.RelativeDate;
import com.elster.jupiter.time.RelativeField;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.RelativePeriodCategory;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.conditions.Where;
import com.google.common.collect.Range;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.log.LogService;

import javax.validation.ValidatorFactory;
import java.sql.SQLException;
import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import static com.elster.jupiter.time.RelativeField.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TimeServiceIT {

    @Rule
    public TestRule neutralLocale = Using.localeOfMalta();
    @Rule
    public TestRule timeZoneNeutral = Using.timeZoneOfMcMurdo();

    private class MockModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(UserService.class).toInstance(userService);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(EventAdmin.class).toInstance(eventAdmin);
            bind(LogService.class).toInstance(logService);

        }
    }

    public static final String NAME = "NAME";

    public static final String FORMATTER = "formatter";

    private static final ZonedDateTime NOW = ZonedDateTime.of(2012, 10, 12, 9, 46, 12, 241615214, TimeZoneNeutral.getMcMurdo());

    @Rule
    public TestRule veryColdHere = Using.timeZoneOfMcMurdo();
    private Injector injector;

    @Mock
    private BundleContext bundleContext;
    @Mock
    private UserService userService;
    @Mock
    private EventAdmin eventAdmin;
    @Mock
    private ValidatorFactory validatorFactory;
    @Mock
    private LogService logService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private User user;

    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();
    private TransactionService transactionService;
    private TimeService timeService;
    private final RelativeDate startOfTheYearBeforeLastYear = new RelativeDate(
            YEAR.minus(2),
            MONTH.equalTo(1),
            DAY.equalTo(1),
            HOUR.equalTo(0),
            MINUTES.equalTo(0)
    );
    private final RelativeDate startOfLastYear = new RelativeDate(
            YEAR.minus(1),
            MONTH.equalTo(1),
            DAY.equalTo(1),
            HOUR.equalTo(0),
            MINUTES.equalTo(0)
    );
    private final RelativeDate startOfThisYear = new RelativeDate(
            MONTH.equalTo(1),
            DAY.equalTo(1),
            HOUR.equalTo(0),
            MINUTES.equalTo(0)
    );

    @Before
    public void setUp() throws SQLException {
        when(userService.createUser(any(), any())).thenReturn(user);
        try {
            injector = Guice.createInjector(
                    new MockModule(),
                    inMemoryBootstrapModule,
                    new InMemoryMessagingModule(),
                    new EventsModule(),
                    new DomainUtilModule(),
                    new OrmModule(),
                    new UtilModule(Clock.fixed(NOW.toInstant(), ZoneId.systemDefault())),
                    new ThreadSecurityModule(),
                    new PubSubModule(),
                    new TransactionModule(),
                    new NlsModule(),
                    new TimeModule()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        transactionService = injector.getInstance(TransactionService.class);
        transactionService.execute(() -> {
            timeService = injector.getInstance(TimeService.class);
            return null;
        });
    }

    @After
    public void tearDown() throws SQLException {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void testQuery() throws Exception {
        RelativePeriod inA1 = null;
        RelativePeriod inA2 = null;
        RelativePeriod inB1 = null;
        RelativePeriod inB2 = null;
        RelativePeriod both = null;
        RelativePeriod neither = null;

        try (TransactionContext context = transactionService.getContext()) {
            RelativePeriodCategory categoryA = timeService.createRelativePeriodCategory("A");
            categoryA.save();
            RelativePeriodCategory categoryB = timeService.createRelativePeriodCategory("B");
            categoryB.save();
            RelativePeriodCategory categoryC = timeService.createRelativePeriodCategory("C");
            categoryC.save();
            inA1 = timeService.createRelativePeriod("inA1", new RelativeDate(RelativeField.YEAR.minus(2)), new RelativeDate(RelativeField.YEAR.minus(1)), Arrays.asList(categoryA));
            inA2 = timeService.createRelativePeriod("inA2", new RelativeDate(RelativeField.YEAR.minus(2)), new RelativeDate(RelativeField.YEAR.minus(1)), Arrays.asList(categoryA));
            inB1 = timeService.createRelativePeriod("inB1", new RelativeDate(RelativeField.YEAR.minus(2)), new RelativeDate(RelativeField.YEAR.minus(1)), Arrays.asList(categoryB));
            inB2 = timeService.createRelativePeriod("inB2", new RelativeDate(RelativeField.YEAR.minus(2)), new RelativeDate(RelativeField.YEAR.minus(1)), Arrays.asList(categoryB));
            both = timeService.createRelativePeriod("both", new RelativeDate(RelativeField.YEAR.minus(2)), new RelativeDate(RelativeField.YEAR.minus(1)), Arrays.asList(categoryA, categoryB));
            neither = timeService.createRelativePeriod("neither", new RelativeDate(RelativeField.YEAR.minus(2)), new RelativeDate(RelativeField.YEAR.minus(1)), Arrays.asList(categoryC));

            context.commit();
        }

        Condition hasA = Where.where("relativePeriodCategoryUsages.relativePeriodCategory.name").isEqualTo("A");
        Query<? extends RelativePeriod> relativePeriodQuery = timeService.getRelativePeriodQuery();
        List<RelativePeriod> relativePeriods = ((Query<RelativePeriod>) relativePeriodQuery).select(hasA, Order.ascending("name"));
        assertThat(relativePeriods).hasSize(3);
        assertThat(relativePeriods.get(0).getName()).isEqualTo("both");
        assertThat(relativePeriods.get(1).getName()).isEqualTo("inA1");
        assertThat(relativePeriods.get(2).getName()).isEqualTo("inA2");

        Condition hasB = Where.where("relativePeriodCategoryUsages.relativePeriodCategory.name").isEqualTo("B");
        relativePeriodQuery = timeService.getRelativePeriodQuery();
        relativePeriods = ((Query<RelativePeriod>) relativePeriodQuery).select(hasB, Order.ascending("name"));
        assertThat(relativePeriods).hasSize(3);
        assertThat(relativePeriods.get(0).getName()).isEqualTo("both");
        assertThat(relativePeriods.get(1).getName()).isEqualTo("inB1");
        assertThat(relativePeriods.get(2).getName()).isEqualTo("inB2");

        relativePeriodQuery = timeService.getRelativePeriodQuery();
        relativePeriodQuery.setRestriction(hasB);
        relativePeriods = ((Query<RelativePeriod>) relativePeriodQuery).select(Condition.TRUE);
        assertThat(relativePeriods).hasSize(3);
        assertThat(relativePeriods.get(0).getName()).isEqualTo("both");
        assertThat(relativePeriods.get(1).getName()).isEqualTo("inB1");
        assertThat(relativePeriods.get(2).getName()).isEqualTo("inB2");
    }

    @Test
    public void testThisWeekOfDefaultPeriods() {
        RelativePeriod thisWeek = timeService.getRelativePeriods().stream()
                .filter(relativePeriod -> DefaultRelativePeriodDefinition.THIS_WEEK.getPeriodName().equals(relativePeriod.getName()))
                .findFirst().get();

        ZonedDateTime zonedDateTime = ZonedDateTime.of(2015, 6, 22, 15, 16, 12, 212551252, TimeZoneNeutral.getMcMurdo());

        Range<ZonedDateTime> interval = thisWeek.getInterval(zonedDateTime);
        assertThat(interval.hasLowerBound()).isTrue();
        assertThat(interval.lowerEndpoint()).isEqualTo(ZonedDateTime.of(2015, 6, 21, 0, 0, 0, 0, TimeZoneNeutral.getMcMurdo()));
        assertThat(interval.hasUpperBound()).isTrue();
        assertThat(interval.upperEndpoint()).isEqualTo(ZonedDateTime.of(2015, 6, 23, 0, 0, 0, 0, TimeZoneNeutral.getMcMurdo()));

    }

}