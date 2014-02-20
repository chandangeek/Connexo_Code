package com.elster.jupiter.metering.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.cbo.PhaseCode;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.*;
import com.elster.jupiter.metering.events.EndDeviceEventRecord;
import com.elster.jupiter.metering.events.EndDeviceEventType;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.VoidTransaction;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.util.units.Unit;
import com.google.common.base.Optional;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventAdmin;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class UsagePointDetailImplIT {

    private Injector injector;

    @Mock
    private BundleContext bundleContext;
    @Mock
    private UserService userService;
    @Mock
    private EventAdmin eventAdmin;


    private InMemoryBootstrapModule inMemoryBootstrapModule = new InMemoryBootstrapModule();


    private class MockModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(UserService.class).toInstance(userService);
            bind(BundleContext.class).toInstance(bundleContext);
            bind(EventAdmin.class).toInstance(eventAdmin);
        }
    }

    @Before
    public void setUp() throws SQLException {
        injector = Guice.createInjector(
                new MockModule(),
                inMemoryBootstrapModule,
                new InMemoryMessagingModule(),
                new IdsModule(),
                new MeteringModule(),
                new PartyModule(),
                new EventsModule(),
                new DomainUtilModule(),
                new OrmModule(),
                new UtilModule(),
                new ThreadSecurityModule(),
                new PubSubModule(),
                new TransactionModule(true),
                new NlsModule()
        );
        injector.getInstance(TransactionService.class).execute(new Transaction<Void>() {
            @Override
            public Void perform() {
                injector.getInstance(MeteringService.class);
                return null;
            }
        });
    }

    @After
    public void tearDown() throws SQLException {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void testUsagePointDetails() {
        TransactionService transactionService = injector.getInstance(TransactionService.class);
        try (TransactionContext context = transactionService.getContext()) {
            MeteringService meteringService = injector.getInstance(MeteringService.class);
            DataModel dataModel = ((MeteringServiceImpl) meteringService).getDataModel();
            ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY).get();
            UsagePoint usagePoint = serviceCategory.newUsagePoint("mrID");
            usagePoint.save();
            long id = usagePoint.getId();
            assertThat(dataModel.mapper(UsagePoint.class).find()).hasSize(1);

            //add details valid from 1 january 2014
            Date january2014 = new DateTime(2014, 1, 1, 0, 0, 0, 0).toDate();

            ElectricityDetail elecDetail = newElectricityDetail(usagePoint, january2014);
            usagePoint.addDetail(elecDetail);

            //add details valid from 1 february 2014 (this closes the previous detail on this date)
            Date february2014 = new DateTime(2014, 2, 1, 0, 0, 0, 0).toDate();
            elecDetail = newElectricityDetail(usagePoint, february2014);
            usagePoint.addDetail(elecDetail);

            context.commit();

            //get details valid from 1 january 2014
            Optional optional =  usagePoint.getDetail(january2014);
            assertThat(optional.isPresent()).isTrue();
            ElectricityDetail foundElecDetail = (ElectricityDetail) optional.get();
            //verify interval is closed because a second was added!
            assertThat(foundElecDetail.getInterval().equals(new Interval(january2014, february2014))).isTrue();
            foundElecDetail.getAmiBillingReady();


            //get details valid from 1 february 2014 (finds same details as from 1 february 2014)
            optional =  usagePoint.getDetail(february2014);
            assertThat(optional.isPresent()).isTrue();
            foundElecDetail = (ElectricityDetail) optional.get();
            assertThat(foundElecDetail.getInterval().equals(new Interval(february2014, null))).isTrue();
            foundElecDetail.getAmiBillingReady();

            System.out.println(foundElecDetail.toString());

            Date january2013 = new DateTime(2013, 1, 1, 0, 0, 0, 0).toDate();
            optional =  usagePoint.getDetail(january2013);
            assertThat(optional.isPresent()).isFalse();


            Date march2014 = new DateTime(2014, 3, 1, 0, 0, 0, 0).toDate();
            Interval interval = new Interval(january2014, march2014);
            List<UsagePointDetailImpl> details = usagePoint.getDetail(interval);
            assertThat(details.size() == 2).isTrue();




        }

    }

    protected ElectricityDetail newElectricityDetail(UsagePoint usagePoint, Date date) {
        ElectricityDetail elecDetail = (ElectricityDetail) usagePoint.getServiceCategory().newUsagePointDetail(usagePoint, date);
        fillElectricityDetail(elecDetail);
        return elecDetail;
    }

    protected void fillElectricityDetail(ElectricityDetail elecDetail) {
        //general properties
        elecDetail.setAmiBillingReady(AmiBillingReadyKind.AMIDISABLED);
        elecDetail.setCheckBilling(true);
        elecDetail.setConnectionState(UsagePointConnectedKind.CONNECTED);
        elecDetail.setMinimalUsageExpected(true);
        elecDetail.setServiceDeliveryRemark("remark");

        //electriciy specific properties
        elecDetail.setGrounded(true);
        Quantity voltage = Unit.VOLT.amount(BigDecimal.valueOf(220));
        elecDetail.setNominalServiceVoltage(voltage);
        elecDetail.setPhaseCode(PhaseCode.ABCN);
        Quantity ratedCurrent = Unit.AMPERE.amount(BigDecimal.valueOf(14));
        elecDetail.setRatedCurrent(ratedCurrent);
        Quantity ratedPower = Unit.WATT.amount(BigDecimal.valueOf(156156));
        elecDetail.setRatedPower(ratedPower);
        Quantity load = Unit.WATT_HOUR.amount(BigDecimal.ONE);
        elecDetail.setEstimatedLoad(load);

    }

}

