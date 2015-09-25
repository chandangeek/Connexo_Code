package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.MarketRoleKind;
import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceLocation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointAccountability;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyRepresentation;
import com.elster.jupiter.parties.PartyRole;
import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.users.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import javax.inject.Provider;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.fest.reflect.core.Reflection.field;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class UsagePointImplTest extends EqualsContractTest {

    public static final long INSTANCE_A_ID = 546L;
    @Rule
    public TestRule timeZoneNeutral = Using.timeZoneOfMcMurdo();

    private static final String MR_ID = "mrID";
    private static final String ALIAS_NAME = "aliasName";
    private static final String DESCRIPTION = "description";
    private static final String NAME = "name";
    private static final long ID = 1457L;
    public static final ZonedDateTime START_DATE = ZonedDateTime.of(2013, 9, 18, 13, 16, 45, 0, TimeZoneNeutral.getMcMurdo());
    private static final Instant START = START_DATE.toInstant();

    private UsagePointImpl usagePoint, instanceA;

    private Clock clock = Clock.fixed(START, TimeZoneNeutral.getMcMurdo());

    @Mock
    private ServiceCategory serviceCategory;
    @Mock
    private DataMapper<UsagePoint> usagePointFactory;
    @Mock
    private MeterActivation activation1, activation2;
    @Mock
    private UsagePointAccountability acc1, acc2;
    @Mock
    private PartyRole role;
    @Mock
    private Party party, party1, party2;
    @Mock
    private User user1, user2, user3, user4, user5;
    @Mock
    private PartyRepresentation representation1, representation2, representation3, representation4;
    @Mock
    private DataModel dataModel;
    @Mock
    private EventService eventService;
    @Mock
    private PartyService partyService;
    @Mock
    private ChannelBuilder channelBuilder;
    @Mock
    private Provider<MeterActivationImpl> meterActivationProvider;
    @Mock
    private Provider<UsagePointAccountabilityImpl> accountabilityProvider;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private DataMapper<MeterActivation> meterActivationMapper;

    @Before
    public void setUp() {
    	when(role.getMRID()).thenReturn(MarketRoleKind.ENERGYSERVICECONSUMER.name());
        when(dataModel.mapper(UsagePoint.class)).thenReturn(usagePointFactory);
        when(dataModel.getInstance(UsagePointAccountabilityImpl.class)).thenAnswer(invocationOnMock -> new UsagePointAccountabilityImpl(clock));
        final Provider<ChannelBuilder> channelBuilderProvider = () -> channelBuilder;
        when(meterActivationProvider.get()).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return new MeterActivationImpl(dataModel, eventService, clock, channelBuilderProvider, thesaurus);
            }
        });
        when(accountabilityProvider.get()).thenAnswer(invocationOnMock -> new UsagePointAccountabilityImpl(clock));
        when(representation1.getDelegate()).thenReturn(user1);
        when(representation2.getDelegate()).thenReturn(user2);
        when(representation3.getDelegate()).thenReturn(user3);
        when(representation4.getDelegate()).thenReturn(user4);
        when(dataModel.mapper(MeterActivation.class)).thenReturn(meterActivationMapper);

        usagePoint = new UsagePointImpl(dataModel, eventService,meterActivationProvider,accountabilityProvider).init(MR_ID, serviceCategory);
    }

    @After
    public void tearDown() {
    }

    @Override
    protected Object getInstanceA() {
        if (instanceA == null) {
            instanceA = new UsagePointImpl(dataModel, eventService,meterActivationProvider,accountabilityProvider).init(MR_ID, serviceCategory);
            field("id").ofType(Long.TYPE).in(instanceA).set(INSTANCE_A_ID);
        }
        return instanceA;
    }

    @Override
    protected Object getInstanceEqualToA() {
        UsagePointImpl other = new UsagePointImpl(dataModel, eventService,meterActivationProvider,accountabilityProvider).init(MR_ID, serviceCategory);
        field("id").ofType(Long.TYPE).in(other).set(INSTANCE_A_ID);
        return other;
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        UsagePointImpl other = new UsagePointImpl(dataModel, eventService,meterActivationProvider,accountabilityProvider).init(MR_ID, serviceCategory);
        field("id").ofType(Long.TYPE).in(other).set(INSTANCE_A_ID + 1);
        return Collections.singletonList(other);
    }

    @Override
    protected boolean canBeSubclassed() {
        return false;
    }

    @Override
    protected Object getInstanceOfSubclassEqualToA() {
        return null;
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
    public void testGetOutageRegion() {
        String outageRegion = "outageRegion";
        usagePoint.setOutageRegion(outageRegion);

        assertThat(usagePoint.getOutageRegion()).isEqualTo(outageRegion);
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

        assertThat(usagePoint.getServiceLocation()).isEqualTo(Optional.of(serviceLocation));
    }

    @Test
    public void testSaveNew() {
        usagePoint.update();

        verify(dataModel).persist(usagePoint);
    }

    @Test
    public void testSaveUpdate() {
        simulateSavedUsagePoint();
        usagePoint.update();

        verify(dataModel).update(usagePoint);
    }

    @Test
    public void testDelete() {
        usagePoint.delete();
        
        verify(dataModel).remove(usagePoint);
    }

    @Test
    public void testGetMeterActivations() {
        ZonedDateTime dateTime = ZonedDateTime.of(2013, 8, 14, 17, 30, 22, 123456789, TimeZoneNeutral.getMcMurdo());
        activation1 = usagePoint.activate(dateTime.toInstant());
        activation1.endAt(dateTime.plusYears(1).toInstant());
        activation2 = usagePoint.activate(dateTime.plusYears(1).toInstant());

        List<MeterActivation> meterActivations = new ArrayList<>(usagePoint.getMeterActivations());

        assertThat(meterActivations).hasSize(2)
                .contains(this.activation1)
                .contains(activation2);
    }

    @Test
    public void testGetCurrentMeterActivations() {
        clock = Clock.fixed(START_DATE.plusYears(2).toInstant(), ZoneId.systemDefault());
        activation1 = usagePoint.activate(START_DATE.toInstant());
        activation1.endAt(START_DATE.plusYears(1).toInstant());
        activation2 = usagePoint.activate(START_DATE.plusYears(1).toInstant());

        assertThat(usagePoint.getCurrentMeterActivation())
                .contains(activation2);
    }

    @Test
    public void testGetAccountabilities() {
        field("accountabilities").ofType(List.class).in(usagePoint).set(Arrays.asList(acc1,acc2));
        List<UsagePointAccountability> accountabilities = usagePoint.getAccountabilities();

        assertThat(accountabilities).hasSize(2)
                .contains(acc1)
                .contains(acc2);
    }

    @Test
    public void testActivate() {
        simulateSavedUsagePoint();
        MeterActivation meterActivation = usagePoint.activate(START);

        verify(dataModel).persist(meterActivation);

        assertThat(meterActivation.getUsagePoint().isPresent()).isTrue();
        assertThat(meterActivation.getUsagePoint().get()).isEqualTo(usagePoint);
    }

    @Test
    public void testAddAccountability() {
        UsagePointAccountability accountability = usagePoint.addAccountability(role, party, START);
        assertThat(usagePoint.getAccountabilities()).contains(accountability);
    }

    @Test
    public void testGetResponsiblePartyChooseCorrectRole() {
        field("accountabilities").ofType(List.class).in(usagePoint).set(Arrays.asList(acc1,acc2));
        PartyRole wrongRole = mock(PartyRole.class);
        Instant now = Instant.now();
        when(wrongRole.getMRID()).thenReturn(MarketRoleKind.BALANCERESPONSIBLEPARTY.name());
        when(acc1.getRole()).thenReturn(wrongRole);
        when(acc1.isEffectiveAt(now)).thenReturn(true);
        when(acc2.getRole()).thenReturn(role);
        when(acc2.isEffectiveAt(now)).thenReturn(true);
        when(acc2.getParty()).thenReturn(party);

        assertThat(usagePoint.getResponsibleParty(now, MarketRoleKind.ENERGYSERVICECONSUMER).get()).isEqualTo(party);
    }

    @Test
    public void testGetResponsiblePartyChooseOnlyCurrent() {
    	field("accountabilities").ofType(List.class).in(usagePoint).set(Arrays.asList(acc1,acc2));
    	Instant now = Instant.now();
        when(acc1.getRole()).thenReturn(role);
        when(acc1.isEffectiveAt(now)).thenReturn(false);
        when(acc2.getRole()).thenReturn(role);
        when(acc2.isEffectiveAt(now)).thenReturn(true);
        when(acc2.getParty()).thenReturn(party);

        assertThat(usagePoint.getResponsibleParty(now, MarketRoleKind.ENERGYSERVICECONSUMER).get()).isEqualTo(party);
    }

    @Test
    public void testHasAccountabilityTrue() {
        field("accountabilities").ofType(List.class).in(usagePoint).set(Arrays.asList(acc1,acc2));
        when(acc1.getParty()).thenReturn(party1);
        when(acc2.getParty()).thenReturn(party2);
        doReturn(Arrays.asList(representation1, representation2)).when(party1).getCurrentDelegates();
        doReturn(Arrays.asList(representation3, representation4)).when(party2).getCurrentDelegates();

        assertThat(usagePoint.hasAccountability(user4)).isTrue();
    }

    @Test
    public void testHasAccountabilityFalse() {
        field("accountabilities").ofType(List.class).in(usagePoint).set(Arrays.asList(acc1,acc2));
        when(acc1.getParty()).thenReturn(party1);
        when(acc2.getParty()).thenReturn(party2);
        doReturn(Arrays.asList(representation1, representation2)).when(party1).getCurrentDelegates();
        doReturn(Arrays.asList(representation3, representation4)).when(party2).getCurrentDelegates();

        assertThat(usagePoint.hasAccountability(user5)).isFalse();
    }


    private void simulateSavedUsagePoint() {
        field("id").ofType(Long.TYPE).in(usagePoint).set(ID);
    }
}
