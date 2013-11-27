package com.elster.jupiter.metering.impl;

import com.elster.jupiter.bootstrap.BootstrapService;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.domain.util.impl.QueryServiceImpl;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.impl.EventServiceImpl;
import com.elster.jupiter.ids.IdsService;
import com.elster.jupiter.ids.impl.IdsServiceImpl;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.metering.BaseReadingRecord;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQuality;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingStorer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.cache.CacheService;
import com.elster.jupiter.orm.cache.impl.CacheServiceImpl;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.orm.impl.OrmServiceImpl;
import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.parties.impl.PartyServiceImpl;
import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.pubsub.impl.PublisherImpl;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.security.thread.impl.ThreadPrincipalServiceImpl;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.transaction.impl.TransactionServiceImpl;
import com.elster.jupiter.transaction.impl.TransactionalDataSource;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.beans.impl.BeanServiceImpl;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.json.impl.JsonServiceImpl;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.impl.DefaultClock;
import com.google.common.base.Optional;
import org.joda.time.DateMidnight;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.log.LogService;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.security.Principal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ReadingQualityImplTest {

    private final BeanServiceImpl beanService = new BeanServiceImpl();
    private BootstrapService bootstrapService;
    private Publisher publisher;
    private ThreadPrincipalService threadPrincipalService;
    private DataSource dataSource;
    private OrmService ormService;
    private JsonService jsonService;
    private TransactionServiceImpl transactionService;
    private CacheService cacheService;
    private QueryService queryService;
    private IdsService idsService;
    private Connection lifeLineConnection;
    private PartyService partyService;
    private EventService eventService;
    private MeteringService meteringService;
    private Clock clock = new DefaultClock();


    @Mock
    private LogService logService;
    @Mock
    private ComponentContext componentContext;
    @Mock
    private BundleContext bundleContext;

    @Mock
    private UserService userService;
    @Mock
    private Principal principal;
    @Mock
    private EventAdmin eventAdmin;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MessageService messageService;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private QueueTableSpec queueTableSpec;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DestinationSpec destinationSpec;


    @Before
    public void setUp() throws SQLException {
        when(messageService.getQueueTableSpec(anyString())).thenReturn(Optional.of(queueTableSpec));
        when(messageService.getDestinationSpec(anyString())).thenReturn(Optional.of(destinationSpec));
        when(principal.getName()).thenReturn("Test");
        bootstrapService = new H2BootStrapService();
        publisher = initPublisher();
        threadPrincipalService = new ThreadPrincipalServiceImpl();
        threadPrincipalService.set(principal);
        jsonService = new JsonServiceImpl();

        transactionService = initTransactionService();
        dataSource = initDataSource();
        ormService = initOrmService();
        cacheService = initCacheService();
        queryService = initQueryService();
    }

    private QueryService initQueryService() {
        return new QueryServiceImpl();
    }

    private CacheService initCacheService() {
        CacheServiceImpl cacheService = new CacheServiceImpl();
        cacheService.setPublisher(publisher);
        return cacheService;
    }

    private void install(final InstallService installService) {
        transactionService.execute(new VoidTransaction() {
            @Override
            protected void doPerform() {
                installService.install();
            }
        });
    }

    private OrmServiceImpl initOrmService() {
        OrmServiceImpl ormService = new OrmServiceImpl();
        ormService.setClock(clock);
        ormService.setThreadPrincipalService(threadPrincipalService);
        ormService.setDataSource(dataSource);
        ormService.setJsonService(jsonService);
        ormService.activate();
        install(ormService);
        return ormService;
    }

    private PublisherImpl initPublisher() {
        PublisherImpl publisher = new PublisherImpl();
        publisher.setLogService(logService);
        return publisher;
    }

    private TransactionalDataSource initDataSource() throws SQLException {
        TransactionalDataSource dataSource = new TransactionalDataSource();
        dataSource.setTransactionService(transactionService);
        lifeLineConnection = dataSource.getConnection();
        return dataSource;
    }

    private TransactionServiceImpl initTransactionService() {
        TransactionServiceImpl transactionService = new TransactionServiceImpl();
        transactionService.setPublisher(publisher);
        transactionService.setThreadPrincipalService(threadPrincipalService);
        transactionService.setBootstrapService(bootstrapService);
        transactionService.activate();
        return transactionService;
    }

    @After
    public void tearDown() throws SQLException {
        lifeLineConnection.close();
    }

    @Test
    public void test() throws SQLException {

        eventService = initEventService();
        idsService = initIdsService();
        partyService = initPartyService();
        meteringService = initMeteringService();


        transactionService.execute(new VoidTransaction() {
            @Override
            protected void doPerform() {
                Date date = new DateMidnight(2001, 1, 1).toDate();
                doTest(meteringService, date);

            }
        });

    }

    private void doTest(MeteringService meteringService, Date date) {
        ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY).get();
        UsagePoint usagePoint = serviceCategory.newUsagePoint("mrID");
        usagePoint.save();

        ReadingType readingType = meteringService.getReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.3.72.0").get();

        MeterActivation meterActivation = usagePoint.activate(date);
        Channel channel = meterActivation.createChannel(readingType);

        ReadingStorer regularStorer = meteringService.createNonOverrulingStorer();
        regularStorer.addIntervalReading(channel, date, 0L, BigDecimal.valueOf(561561, 2));
        regularStorer.execute();

        BaseReadingRecord reading = channel.getReading(date).get();
        ReadingQuality readingQuality = channel.createReadingQuality(new ReadingQualityType("6.1"), reading);
        readingQuality.save();
    }

    private EventService initEventService() {
        EventServiceImpl eventService = new EventServiceImpl();
        eventService.setOrmService(ormService);
        eventService.setCacheService(cacheService);
        eventService.setEventAdmin(eventAdmin);
        eventService.setClock(clock);
        eventService.setPublisher(publisher);
        eventService.setBeanService(beanService);
        eventService.setJsonService(jsonService);
        eventService.setMessageService(messageService);
        eventService.activate(bundleContext);
        install(eventService);
        return eventService;
    }

    private PartyServiceImpl initPartyService() {
        PartyServiceImpl partyService = new PartyServiceImpl();
        partyService.setOrmService(ormService);
        partyService.setClock(clock);
        partyService.setQueryService(queryService);
        partyService.setUserService(userService);
        partyService.setCacheService(cacheService);
        partyService.setEventService(eventService);
        partyService.setThreadPrincipalService(threadPrincipalService);
        partyService.activate(componentContext);
        install(partyService);
        return partyService;
    }

    private IdsServiceImpl initIdsService() {
        IdsServiceImpl idsService = new IdsServiceImpl();
        idsService.setClock(clock);
        idsService.setOrmService(ormService);
        idsService.activate(componentContext);
        install(idsService);
        return idsService;
    }

    private MeteringService initMeteringService() {
        MeteringServiceImpl meteringService = new MeteringServiceImpl();
        meteringService.setClock(clock);
        meteringService.setOrmService(ormService);
        meteringService.setIdsService(idsService);
        meteringService.setCacheService(cacheService);
        meteringService.setEventService(eventService);
        meteringService.setPartyService(partyService);
        meteringService.setQueryService(queryService);
        meteringService.setUserService(userService);
        meteringService.activate();
        install(meteringService);
        return meteringService;
    }

}
