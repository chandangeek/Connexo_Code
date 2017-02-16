/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cbo.MarketRoleKind;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.ConnectionState;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceLocation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointAccountability;
import com.elster.jupiter.metering.config.DefaultMeterRole;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.impl.aggregation.ServerDataAggregationService;
import com.elster.jupiter.metering.impl.config.ServerMetrologyConfigurationService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyRepresentation;
import com.elster.jupiter.parties.PartyRole;
import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.usagepoint.lifecycle.config.UsagePointLifeCycleConfigurationService;
import com.elster.jupiter.users.User;

import javax.inject.Provider;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.fest.reflect.core.Reflection.field;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class UsagePointImplTest {

    @Rule
    public TestRule timeZoneNeutral = Using.timeZoneOfMcMurdo();

    private static final String ALIAS_NAME = "aliasName";
    private static final String DESCRIPTION = "description";
    private static final String NAME = "name";
    private static final long ID = 1457L;
    public static final ZonedDateTime START_DATE = ZonedDateTime.of(2013, 9, 18, 13, 16, 45, 0, TimeZoneNeutral.getMcMurdo());
    private static final Instant START = START_DATE.toInstant();

    private UsagePointImpl usagePoint;

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
    private CustomPropertySetService customPropertySetService;
    @Mock
    private ServerMetrologyConfigurationService metrologyConfigurationService;
    @Mock
    private ServerDataAggregationService dataAggregationService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private DataMapper<MeterActivation> meterActivationMapper;
    @Mock
    private ValidatorFactory validatorFactory;
    @Mock
    private Validator validator;
    @Mock
    private ServerMeteringService meteringService;
    @Mock
    private MeterImpl meter;
    @Mock
    private MeterRole meterRole;
    @Mock
    private DataMapper<Meter> meterFactory;
    @Mock
    private UsagePointLifeCycleConfigurationService usagePointLifeCycleConfigurationService;

    @Before
    public void setUp() {
        when(meteringService.getClock()).thenReturn(clock);
        when(meteringService.getDataModel()).thenReturn(dataModel);
        when(role.getMRID()).thenReturn(MarketRoleKind.ENERGYSERVICECONSUMER.name());
        when(dataModel.mapper(UsagePoint.class)).thenReturn(usagePointFactory);
        when(dataModel.mapper(Meter.class)).thenReturn(meterFactory);
        when(meterRole.getKey()).thenReturn(DefaultMeterRole.DEFAULT.getKey());
        when(meterFactory.getExisting(any())).thenReturn(meter);
        when(meter.getHeadEndInterface()).thenReturn(Optional.empty());
        when(dataModel.getInstance(UsagePointAccountabilityImpl.class)).thenAnswer(invocationOnMock -> new UsagePointAccountabilityImpl(clock));
        final Provider<ChannelBuilder> channelBuilderProvider = () -> channelBuilder;
        when(dataModel.getInstance(MeterActivationChannelsContainerImpl.class)).then(invocation -> new MeterActivationChannelsContainerImpl(meteringService, eventService, channelBuilderProvider));
        when(dataModel.getValidatorFactory()).thenReturn(validatorFactory);
        when(dataModel.getInstance(UsagePointConnectionStateImpl.class)).thenReturn(new UsagePointConnectionStateImpl());
        when(validatorFactory.getValidator()).thenReturn(validator);
        when(meterActivationProvider.get()).thenAnswer(invocationOnMock -> new MeterActivationImpl(dataModel, eventService, clock, thesaurus));
        when(accountabilityProvider.get()).thenAnswer(invocationOnMock -> new UsagePointAccountabilityImpl(clock));
        when(representation1.getDelegate()).thenReturn(user1);
        when(representation2.getDelegate()).thenReturn(user2);
        when(representation3.getDelegate()).thenReturn(user3);
        when(representation4.getDelegate()).thenReturn(user4);
        when(dataModel.mapper(MeterActivation.class)).thenReturn(meterActivationMapper);

        usagePoint = new UsagePointImpl(clock, dataModel, eventService, thesaurus, meterActivationProvider, accountabilityProvider,
                customPropertySetService, metrologyConfigurationService, dataAggregationService, usagePointLifeCycleConfigurationService)
                .init(NAME, serviceCategory);
        usagePoint.setInstallationTime(Instant.EPOCH);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testGetMrId() {
        String mRID = usagePoint.getMRID();
        assertThat(mRID).isNotNull().isNotEmpty();
        assertThat(UUID.fromString(mRID).toString()).isEqualTo(mRID);
    }

    @Test
    public void testGetServiceCategory() {
        assertThat(usagePoint.getServiceCategory()).isEqualTo(serviceCategory);
    }

    @Test
    public void testGetSetName() {
        assertThat(usagePoint.getName()).isEqualTo(NAME);
        usagePoint.setName("newname");
        assertThat(usagePoint.getName()).isEqualTo("newname");
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
    public void testGetConnectionState() {
        usagePoint.setConnectionState(ConnectionState.CONNECTED);

        assertThat(usagePoint.getCurrentConnectionState()).contains(ConnectionState.CONNECTED);
    }

    @Test
    public void testSaveNew() {
        usagePoint.doSave();

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

        activation1 = usagePoint.activate(meter, meterRole, dateTime.toInstant());
        activation1.endAt(dateTime.plusYears(1).toInstant());
        activation2 = usagePoint.activate(meter, meterRole, dateTime.plusYears(1).toInstant());

        List<MeterActivation> meterActivations = new ArrayList<>(usagePoint.getMeterActivations());
        assertThat(meterActivations).hasSize(2)
                .contains(this.activation1)
                .contains(activation2);
    }

    @Test
    public void testGetCurrentMeterActivations() {
        clock = Clock.fixed(START_DATE.plusYears(2).toInstant(), ZoneId.systemDefault());
        activation1 = usagePoint.activate(meter, meterRole, START_DATE.toInstant());
        activation1.endAt(START_DATE.plusYears(1).toInstant());
        activation2 = usagePoint.activate(meter, meterRole, START_DATE.plusYears(1).toInstant());
        assertThat(usagePoint.getCurrentMeterActivations()).contains(activation2);
    }

    @Test
    public void testGetAccountabilities() {
        field("accountabilities").ofType(List.class).in(usagePoint).set(Arrays.asList(acc1, acc2));
        List<UsagePointAccountability> accountabilities = usagePoint.getAccountabilities();

        assertThat(accountabilities).hasSize(2)
                .contains(acc1)
                .contains(acc2);
    }

    @Test
    public void testActivate() {
        simulateSavedUsagePoint();
        MeterActivation meterActivation = usagePoint.activate(meter, meterRole, START);

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
        field("accountabilities").ofType(List.class).in(usagePoint).set(Arrays.asList(acc1, acc2));
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
        field("accountabilities").ofType(List.class).in(usagePoint).set(Arrays.asList(acc1, acc2));
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
        field("accountabilities").ofType(List.class).in(usagePoint).set(Arrays.asList(acc1, acc2));
        when(acc1.getParty()).thenReturn(party1);
        when(acc2.getParty()).thenReturn(party2);
        doReturn(Arrays.asList(representation1, representation2)).when(party1).getCurrentDelegates();
        doReturn(Arrays.asList(representation3, representation4)).when(party2).getCurrentDelegates();

        assertThat(usagePoint.hasAccountability(user4)).isTrue();
    }

    @Test
    public void testHasAccountabilityFalse() {
        field("accountabilities").ofType(List.class).in(usagePoint).set(Arrays.asList(acc1, acc2));
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
