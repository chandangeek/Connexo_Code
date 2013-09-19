package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.PhaseCode;
import com.elster.jupiter.metering.AmiBillingReadyKind;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceLocation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointAccountability;
import com.elster.jupiter.metering.UsagePointConnectedKind;
import com.elster.jupiter.metering.plumbing.Bus;
import com.elster.jupiter.metering.plumbing.ServiceLocator;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyRole;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.util.units.Unit;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.reflect.core.Reflection.field;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class UsagePointImplTest {

    private static final String MR_ID = "mrID";
    private static final String ALIAS_NAME = "aliasName";
    private static final String DESCRIPTION = "description";
    private static final String NAME = "name";
    private static final long ID = 1457L;
    private static final Date START = new DateTime(2013, 9, 18, 13, 16, 45).toDate();

    private UsagePointImpl usagePoint;

    @Mock
    private ServiceCategory serviceCategory;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ServiceLocator serviceLocator;
    @Mock
    private DataMapper<UsagePoint> usagePointFactory;
    @Mock
    private DataMapper<MeterActivation> meterActivationFactory;
    @Mock
    private MeterActivation activation1, activation2;
    @Mock
    private DataMapper<UsagePointAccountability> usagePointAccountabilityFactory;
    @Mock
    private UsagePointAccountability acc1, acc2;
    @Mock
    private PartyRole role;
    @Mock
    private Party party, party1, party2;
    @Mock
    private User user1, user2, user3, user4, user5;

    @Before
    public void setUp() {
        when(serviceLocator.getOrmClient().getUsagePointFactory()).thenReturn(usagePointFactory);
        when(serviceLocator.getOrmClient().getMeterActivationFactory()).thenReturn(meterActivationFactory);
        when(serviceLocator.getOrmClient().getUsagePointAccountabilityFactory()).thenReturn(usagePointAccountabilityFactory);

        usagePoint = new UsagePointImpl(MR_ID, serviceCategory);

        Bus.setServiceLocator(serviceLocator);
    }

    @After
    public void tearDown() {
        Bus.setServiceLocator(null);
    }

    @Test
    public void testGetMrId() {
        assertThat(usagePoint.getMRID()).isEqualTo(MR_ID);
    }

    @Test
    public void testGetServiceCategory() {
        assertThat(usagePoint.getServiceCategory()).isEqualTo(serviceCategory);
    }


    @Test
    public void testGetAliasName() {
        usagePoint.setAliasName(ALIAS_NAME);

        assertThat(usagePoint.getAliasName()).isEqualTo(ALIAS_NAME);
    }

    @Test
    public void testGetDescription() {
        usagePoint.setDescription(DESCRIPTION);

        assertThat(usagePoint.getDescription()).isEqualTo(DESCRIPTION);
    }

    @Test
    public void testGetName() {
        usagePoint.setName(NAME);

        assertThat(usagePoint.getName()).isEqualTo(NAME);
    }

    @Test
    public void testGetAmiBillingReady() {
        usagePoint.setAmiBillingReady(AmiBillingReadyKind.AMICAPABLE);

        assertThat(usagePoint.getAmiBillingReady()).isEqualTo(AmiBillingReadyKind.AMICAPABLE);
    }

    @Test
    public void testIsCheckBilling() {
        usagePoint.setCheckBilling(true);

        assertThat(usagePoint.isCheckBilling()).isTrue();
    }

    @Test
    public void testGetConnectionState() {
        usagePoint.setConnectionState(UsagePointConnectedKind.CONNECTED);

        assertThat(usagePoint.getConnectionState()).isEqualTo(UsagePointConnectedKind.CONNECTED);
    }

    @Test
    public void testGetEstimatedLoad() {
        Quantity load = Unit.WATT_HOUR.amount(BigDecimal.ONE);
        usagePoint.setEstimatedLoad(load);

        assertThat(usagePoint.getEstimatedLoad()).isEqualTo(load);
    }

    @Test
    public void testIsGrounded() {
        usagePoint.setGrounded(true);

        assertThat(usagePoint.isGrounded()).isTrue();
    }

    @Test
    public void testIsSDP() {
        usagePoint.setSdp(true);

        assertThat(usagePoint.isSdp()).isTrue();
    }

    @Test
    public void testIsVirtual() {
        usagePoint.setVirtual(true);

        assertThat(usagePoint.isVirtual()).isTrue();
    }

    @Test
    public void testIsMinimumUsageExpected() {
        usagePoint.setMinimalUsageExpected(true);

        assertThat(usagePoint.isMinimumUsageExpected()).isTrue();
    }

    @Test
    public void testGetNominalServiceVoltage() {
        Quantity voltage = Unit.VOLT.amount(BigDecimal.valueOf(220));

        usagePoint.setNominalServiceVoltage(voltage);

        assertThat(usagePoint.getNominalServiceVoltage()).isEqualTo(voltage);
    }

    @Test
    public void testGetOutageRegion() {
        String outageRegion = "outageRegion";
        usagePoint.setOutageRegion(outageRegion);

        assertThat(usagePoint.getOutageRegion()).isEqualTo(outageRegion);
    }

    @Test
    public void testGetPahseCode() {
        usagePoint.setPhaseCode(PhaseCode.ABCN);

        assertThat(usagePoint.getPhaseCode()).isEqualTo(PhaseCode.ABCN);
    }

    @Test
    public void testGetRatedCurrent() {
        Quantity ratedCurrent = Unit.AMPERE.amount(BigDecimal.valueOf(14));

        usagePoint.setRatedCurrent(ratedCurrent);

        assertThat(usagePoint.getRatedCurrent()).isEqualTo(ratedCurrent);
    }

    @Test
    public void testGetRatedPower() {
        Quantity ratedPower = Unit.WATT.amount(BigDecimal.valueOf(156156));

        usagePoint.setRatedPower(ratedPower);

        assertThat(usagePoint.getRatedPower()).isEqualTo(ratedPower);
    }

    @Test
    public void testGetReadCycle() {
        String readCycle = "readCycle";
        usagePoint.setReadCycle(readCycle);

        assertThat(usagePoint.getReadCycle()).isEqualTo(readCycle);
    }

    @Test
    public void testGetReadRoute() {
        String readRoute = "readRoute";
        usagePoint.setReadRoute(readRoute);

        assertThat(usagePoint.getReadRoute()).isEqualTo(readRoute);
    }

    @Test
    public void testGetServiceDeliveryRemark() {
        String remark = "remark";
        usagePoint.setServiceDeliveryRemark(remark);

        assertThat(usagePoint.getServiceDeliveryRemark()).isEqualTo(remark);
    }

    @Test
    public void testGetServicePriority() {
        String priority = "priority";
        usagePoint.setServicePriority(priority);

        assertThat(usagePoint.getServicePriority()).isEqualTo(priority);
    }

    @Test
    public void testGetServiceLocation() {
        ServiceLocation serviceLocation = mock(ServiceLocation.class);
        when(serviceLocation.getId()).thenReturn(15L);
        usagePoint.setServiceLocation(serviceLocation);

        assertThat(usagePoint.getServiceLocation()).isEqualTo(serviceLocation);
    }

    @Test
    public void testSaveNew() {
        usagePoint.save();

        verify(usagePointFactory).persist(usagePoint);
    }

    @Test
    public void testSaveUpdate() {
        simulateSavedUsagePoint();
        usagePoint.save();

        verify(usagePointFactory).update(usagePoint);
    }

    @Test
    public void testDelete() {
        usagePoint.delete();

        verify(usagePointFactory).remove(usagePoint);
    }

    @Test
    public void testGetMeterActivations() {
        when(meterActivationFactory.find("usagePoint", usagePoint)).thenReturn(Arrays.asList(activation1, activation2));

        List<MeterActivation> meterActivations = usagePoint.getMeterActivations();

        assertThat(meterActivations).hasSize(2)
                .contains(activation1)
                .contains(activation2);
    }

    @Test
    public void testGetCurrentMeterActivations() {
        when(meterActivationFactory.find("usagePoint", usagePoint)).thenReturn(Arrays.asList(activation1, activation2));
        when(activation1.isCurrent()).thenReturn(false);
        when(activation2.isCurrent()).thenReturn(true);

        MeterActivation meterActivation1 = usagePoint.getCurrentMeterActivation();
        MeterActivation meterActivation2 = usagePoint.getCurrentMeterActivation();

        assertThat(meterActivation1).isEqualTo(activation2);
        assertThat(meterActivation2).isEqualTo(activation2);

        verify(meterActivationFactory, times(1)).find("usagePoint", usagePoint);
    }

    @Test
    public void testGetAccountabilities() {
        when(usagePointAccountabilityFactory.find("usagePoint", usagePoint)).thenReturn(Arrays.asList(acc1, acc2));

        List<UsagePointAccountability> accountabilities = usagePoint.getAccountabilities();

        assertThat(accountabilities).hasSize(2)
                .contains(acc1)
                .contains(acc2);
    }

    @Test
    public void testActivate() {
        simulateSavedUsagePoint();
        MeterActivation meterActivation = usagePoint.activate(START);

        verify(meterActivationFactory).persist(meterActivation);

        assertThat(meterActivation.getUsagePoint().isPresent()).isTrue();
        assertThat(meterActivation.getUsagePoint().get()).isEqualTo(usagePoint);
    }

    @Test
    public void testAddAccountability() {
        UsagePointAccountability accountability = usagePoint.addAccountability(role, party, START);

        verify(usagePointAccountabilityFactory).persist(accountability);
    }

    @Test
    public void testGetResponsiblePartyChooseCorrectRole() {
        when(usagePointAccountabilityFactory.find("usagePoint", usagePoint)).thenReturn(Arrays.asList(acc1, acc2));
        PartyRole wrongRole = mock(PartyRole.class);
        when(acc1.getRole()).thenReturn(wrongRole);
        when(acc1.isCurrent()).thenReturn(true);
        when(acc2.getRole()).thenReturn(role);
        when(acc2.isCurrent()).thenReturn(true);
        when(acc2.getParty()).thenReturn(party);

        assertThat(usagePoint.getResponsibleParty(role).isPresent()).isTrue();
        assertThat(usagePoint.getResponsibleParty(role).get()).isEqualTo(party);
    }

    @Test
    public void testGetResponsiblePartyChooseOnlyCurrent() {
        when(usagePointAccountabilityFactory.find("usagePoint", usagePoint)).thenReturn(Arrays.asList(acc1, acc2));
        when(acc1.getRole()).thenReturn(role);
        when(acc1.isCurrent()).thenReturn(false);
        when(acc2.getRole()).thenReturn(role);
        when(acc2.isCurrent()).thenReturn(true);
        when(acc2.getParty()).thenReturn(party);

        assertThat(usagePoint.getResponsibleParty(role).isPresent()).isTrue();
        assertThat(usagePoint.getResponsibleParty(role).get()).isEqualTo(party);
    }

    @Test
    public void testHasAccountabilityTrue() {
        when(usagePointAccountabilityFactory.find("usagePoint", usagePoint)).thenReturn(Arrays.asList(acc1, acc2));
        when(acc1.getParty()).thenReturn(party1);
        when(acc2.getParty()).thenReturn(party2);
        when(party1.getCurrentDelegates()).thenReturn(Arrays.asList(user1, user2));
        when(party2.getCurrentDelegates()).thenReturn(Arrays.asList(user3, user4));

        assertThat(usagePoint.hasAccountability(user4)).isTrue();
    }

    @Test
    public void testHasAccountabilityFalse() {
        when(usagePointAccountabilityFactory.find("usagePoint", usagePoint)).thenReturn(Arrays.asList(acc1, acc2));
        when(acc1.getParty()).thenReturn(party1);
        when(acc2.getParty()).thenReturn(party2);
        when(party1.getCurrentDelegates()).thenReturn(Arrays.asList(user1, user2));
        when(party2.getCurrentDelegates()).thenReturn(Arrays.asList(user3, user4));

        assertThat(usagePoint.hasAccountability(user5)).isFalse();
    }


    private void simulateSavedUsagePoint() {
        field("id").ofType(Long.TYPE).in(usagePoint).set(ID);
    }
}
