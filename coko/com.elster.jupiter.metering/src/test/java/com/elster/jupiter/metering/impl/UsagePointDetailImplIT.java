package com.elster.jupiter.metering.impl;

import com.elster.jupiter.bootstrap.h2.impl.InMemoryBootstrapModule;
import com.elster.jupiter.bpm.impl.BpmModule;
import com.elster.jupiter.cbo.PhaseCode;
import com.elster.jupiter.domain.util.impl.DomainUtilModule;
import com.elster.jupiter.events.impl.EventsModule;
import com.elster.jupiter.fsm.FiniteStateMachineService;
import com.elster.jupiter.fsm.impl.FiniteStateMachineModule;
import com.elster.jupiter.ids.impl.IdsModule;
import com.elster.jupiter.messaging.h2.impl.InMemoryMessagingModule;
import com.elster.jupiter.metering.AmiBillingReadyKind;
import com.elster.jupiter.metering.ElectricityDetail;
import com.elster.jupiter.metering.GasDetail;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointConnectedKind;
import com.elster.jupiter.metering.UsagePointDetail;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.impl.OrmModule;
import com.elster.jupiter.parties.impl.PartyModule;
import com.elster.jupiter.pubsub.impl.PubSubModule;
import com.elster.jupiter.security.thread.impl.ThreadSecurityModule;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.transaction.impl.TransactionModule;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.UtilModule;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.util.units.Unit;
import com.google.common.collect.Range;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
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
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class UsagePointDetailImplIT {

    private static final Quantity VOLTAGE = Unit.VOLT.amount(BigDecimal.valueOf(220));
    private static final Quantity RATED_CURRENT = Unit.AMPERE.amount(BigDecimal.valueOf(14));
    private static final Quantity RATED_POWER = Unit.WATT.amount(BigDecimal.valueOf(156156));
    private static final Quantity RATED_POWER2 = Unit.WATT.amount(BigDecimal.valueOf(156157));
    private static final Quantity LOAD = Unit.WATT_HOUR.amount(BigDecimal.ONE);

    private static final Instant JANUARY_2014 = ZonedDateTime.of(2014, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
    private static final Instant FEBRUARY_2014 = ZonedDateTime.of(2014, 2, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
    private static final Instant JANUARY_2013 = ZonedDateTime.of(2013, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();
    private static final Instant MARCH_2014 = ZonedDateTime.of(2014, 3, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant();

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
        try {
            try {
                injector = Guice.createInjector(
                        new MockModule(),
                        inMemoryBootstrapModule,
                        new InMemoryMessagingModule(),
                        new IdsModule(),
                        new MeteringModule(false),
                        new PartyModule(),
                        new EventsModule(),
                        new DomainUtilModule(),
                        new OrmModule(),
                        new UtilModule(),
                        new ThreadSecurityModule(),
                        new PubSubModule(),
                        new TransactionModule(false),
                        new BpmModule(),
                        new FiniteStateMachineModule(),
                        new NlsModule()
                );
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        injector.getInstance(TransactionService.class).execute(() -> {
            injector.getInstance(FiniteStateMachineService.class);
            injector.getInstance(MeteringService.class);
            return null;
        });
    }

    @After
    public void tearDown() throws SQLException {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void testElectricityUsagePointDetails() {
        TransactionService transactionService = injector.getInstance(TransactionService.class);

        try (TransactionContext context = transactionService.getContext()) {
            ServerMeteringService meteringService = injector.getInstance(ServerMeteringService.class);
            DataModel dataModel = meteringService.getDataModel();
            ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.ELECTRICITY).get();
            UsagePoint usagePoint = serviceCategory.newUsagePoint("mrID");
            usagePoint.save();
            assertThat(dataModel.mapper(UsagePoint.class).find()).hasSize(1);

            //add details valid from 1 january 2014
            ElectricityDetail elecDetail = newElectricityDetail(usagePoint, JANUARY_2014);
            usagePoint.addDetail(elecDetail);

            //add details valid from 1 february 2014 (this closes the previous detail on this date)
            elecDetail = newElectricityDetail(usagePoint, FEBRUARY_2014);
            usagePoint.addDetail(elecDetail);


            //get details valid from 1 january 2014
            Optional optional =  usagePoint.getDetail(JANUARY_2014);
            assertThat(optional.isPresent()).isTrue();
            ElectricityDetail foundElecDetail = (ElectricityDetail) optional.get();
            //verify interval is closed because a second was added!
            assertThat(foundElecDetail.getInterval().equals(Interval.of(JANUARY_2014, FEBRUARY_2014))).isTrue();
            //check content
            checkElectricityDetailContent(foundElecDetail);

            //update the detail rated power and check
            foundElecDetail.setRatedPower(RATED_POWER2);
            foundElecDetail.update();

            context.commit();

            optional =  usagePoint.getDetail(JANUARY_2014);
            assertThat(optional.isPresent()).isTrue();
            ElectricityDetail updatedElecDetail = (ElectricityDetail) optional.get();
            assertThat(updatedElecDetail.getRatedPower().equals(RATED_POWER2)).isTrue();

            //get details valid from 1 february 2014 (finds same details as from 1 february 2014)
            optional =  usagePoint.getDetail(FEBRUARY_2014);
            assertThat(optional.isPresent()).isTrue();
            foundElecDetail = (ElectricityDetail) optional.get();
            assertThat(foundElecDetail.getInterval().equals(Interval.of(FEBRUARY_2014, null))).isTrue();
            foundElecDetail.getAmiBillingReady();

            //no details to be found valid on 1 january 2013
            optional =  usagePoint.getDetail(JANUARY_2013);
            assertThat(optional.isPresent()).isFalse();


            //2 details to be found in the period from 1 january 2014 to 1 march 2014
            Range<Instant> range = Range.closedOpen(JANUARY_2014, MARCH_2014);
            List details = usagePoint.getDetail(range);
            assertThat(details.size() == 2).isTrue();
        }

    }

    @Test
    public void testGasUsagePointDetails() {
        TransactionService transactionService = injector.getInstance(TransactionService.class);

        try (TransactionContext context = transactionService.getContext()) {
            ServerMeteringService meteringService = injector.getInstance(ServerMeteringService.class);
            DataModel dataModel = meteringService.getDataModel();
            ServiceCategory serviceCategory = meteringService.getServiceCategory(ServiceKind.GAS).get();
            UsagePoint usagePoint = serviceCategory.newUsagePoint("mrID");
            usagePoint.save();
            assertThat(dataModel.mapper(UsagePoint.class).find()).hasSize(1);

            //add details valid from 1 january 2014
            GasDetail gasDetail = newGasDetail(usagePoint, JANUARY_2014);
            usagePoint.addDetail(gasDetail);

            //add details valid from 1 february 2014 (this closes the previous detail on this date)
            gasDetail = newGasDetail(usagePoint, FEBRUARY_2014);
            usagePoint.addDetail(gasDetail);


            //get details valid from 1 january 2014
            Optional optional =  usagePoint.getDetail(JANUARY_2014);
            assertThat(optional.isPresent()).isTrue();
            GasDetail foundGasDetail = (GasDetail) optional.get();
            //verify interval is closed because a second was added!
            assertThat(foundGasDetail.getInterval().equals(Interval.of(JANUARY_2014, FEBRUARY_2014))).isTrue();
            //check content
            checkGasDetailContent(foundGasDetail);

            //update "check billing" and check
            foundGasDetail.setCheckBilling(false);
            foundGasDetail.update();

            context.commit();

            optional =  usagePoint.getDetail(JANUARY_2014);
            assertThat(optional.isPresent()).isTrue();
            GasDetail updatedGasDetail = (GasDetail) optional.get();
            assertThat(updatedGasDetail.isCheckBilling() == false).isTrue();

            //get details valid from 1 february 2014 (finds same details as from 1 february 2014)
            optional =  usagePoint.getDetail(FEBRUARY_2014);
            assertThat(optional.isPresent()).isTrue();
            foundGasDetail = (GasDetail) optional.get();
            assertThat(foundGasDetail.getInterval().equals(Interval.of(FEBRUARY_2014, null))).isTrue();
            foundGasDetail.getAmiBillingReady();

            //no details to be found valid on 1 january 2013
            optional =  usagePoint.getDetail(JANUARY_2013);
            assertThat(optional.isPresent()).isFalse();


            //2 details to be found in the period from 1 january 2014 to 1 march 2014
            Range<Instant> range = Range.closedOpen(JANUARY_2014, MARCH_2014);
            List<? extends UsagePointDetail> details = usagePoint.getDetail(range);
            assertThat(details).hasSize(2);
        }

    }


    protected ElectricityDetail newElectricityDetail(UsagePoint usagePoint, Instant date) {
        ElectricityDetail elecDetail = (ElectricityDetail) usagePoint.getServiceCategory().newUsagePointDetail(usagePoint, date);
        fillElectricityDetail(elecDetail);
        return elecDetail;
    }

    protected GasDetail newGasDetail(UsagePoint usagePoint, Instant instant) {
        GasDetail gasDetail = (GasDetail) usagePoint.getServiceCategory().newUsagePointDetail(usagePoint, instant);
        fillGasDetail(gasDetail);
        return gasDetail;
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
        elecDetail.setNominalServiceVoltage(VOLTAGE);
        elecDetail.setPhaseCode(PhaseCode.ABCN);
        elecDetail.setRatedCurrent(RATED_CURRENT);
        elecDetail.setRatedPower(RATED_POWER);
        elecDetail.setEstimatedLoad(LOAD);

    }

    protected void fillGasDetail(GasDetail gasDetail) {
        //general properties
        gasDetail.setAmiBillingReady(AmiBillingReadyKind.AMIDISABLED);
        gasDetail.setCheckBilling(true);
        gasDetail.setConnectionState(UsagePointConnectedKind.CONNECTED);
        gasDetail.setMinimalUsageExpected(true);
        gasDetail.setServiceDeliveryRemark("remark");

        //gas specific properties: none defined yet
    }

    protected void checkElectricityDetailContent(ElectricityDetail elecDetail) {
        //general properties
        assertThat(elecDetail.getAmiBillingReady().equals(AmiBillingReadyKind.AMIDISABLED)).isTrue();
        assertThat(elecDetail.isCheckBilling() == true).isTrue();
        assertThat(elecDetail.getConnectionState().equals(UsagePointConnectedKind.CONNECTED)).isTrue();
        assertThat(elecDetail.isMinimalUsageExpected() == true).isTrue();
        assertThat(elecDetail.getServiceDeliveryRemark().equals("remark")).isTrue();

        //electriciy specific properties
        assertThat(elecDetail.isGrounded() == true).isTrue();
        assertThat(elecDetail.getNominalServiceVoltage().equals(VOLTAGE)).isTrue();
        assertThat(elecDetail.getPhaseCode().equals(PhaseCode.ABCN)).isTrue();
        assertThat(elecDetail.getRatedCurrent().equals(RATED_CURRENT)).isTrue();
        assertThat(elecDetail.getRatedPower().equals(RATED_POWER)).isTrue();
        assertThat(elecDetail.getEstimatedLoad().equals(LOAD)).isTrue();
    }

    protected void checkGasDetailContent(GasDetail gasDetail) {
        //general properties
        assertThat(gasDetail.getAmiBillingReady().equals(AmiBillingReadyKind.AMIDISABLED)).isTrue();
        assertThat(gasDetail.isCheckBilling() == true).isTrue();
        assertThat(gasDetail.getConnectionState().equals(UsagePointConnectedKind.CONNECTED)).isTrue();
        assertThat(gasDetail.isMinimalUsageExpected() == true).isTrue();
        assertThat(gasDetail.getServiceDeliveryRemark().equals("remark")).isTrue();

        //gas specific properties: none defined yet
    }

}

