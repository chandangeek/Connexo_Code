package com.elster.jupiter.metering.impl;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyRole;
import com.elster.jupiter.util.time.Interval;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UsagePointAccountabilityImplTest {

    private static final long USAGEPOINT_ID = 635154L;
    private static final long PARTY_ID = 56840L;
    private static final String ROLE_MRID = "Zeven";
    private static final Date START = new DateTime(2013, 4, 14, 17, 20, 4).toDate();

    private UsagePointAccountabilityImpl usagePointAccountability;

    @Mock
    private UsagePoint usagePoint;
    @Mock
    private Party party;
    @Mock
    private PartyRole role;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ServiceLocator serviceLocator;

    @Before
    public void setUp() {
        when(usagePoint.getId()).thenReturn(USAGEPOINT_ID);
        when(party.getId()).thenReturn(PARTY_ID);
        when(role.getMRID()).thenReturn(ROLE_MRID);

        usagePointAccountability = new UsagePointAccountabilityImpl(usagePoint, party, role, START);

        Bus.setServiceLocator(serviceLocator);
    }

    @After
    public void tearDown() {
        Bus.clearServiceLocator(serviceLocator);
    }

    @Test
    public void testCreationRemembersUsagePoint() {
        assertThat(usagePointAccountability.getUsagePoint()).isEqualTo(usagePoint);
    }

    @Test
    public void testGetUsagePointId() {
        assertThat(usagePointAccountability.getUsagePointId()).isEqualTo(USAGEPOINT_ID);
    }

    @Test
    public void testGetPartyId() {
        assertThat(usagePointAccountability.getPartyId()).isEqualTo(PARTY_ID);
    }

    @Test
    public void testGetRoleMRID() {
        assertThat(usagePointAccountability.getRoleMRID()).isEqualTo(ROLE_MRID);
    }

    @Test
    public void testGetInterval() {
        assertThat(usagePointAccountability.getInterval()).isEqualTo(Interval.startAt(START));
    }

    @Test
    public void testGetParty() {
        assertThat(usagePointAccountability.getParty()).isEqualTo(party);
    }

    @Test
    public void testGetRole() {
        assertThat(usagePointAccountability.getRole()).isEqualTo(role);
    }

    @Test
    public void testgetUsagePoint() {
        assertThat(usagePointAccountability.getUsagePoint()).isEqualTo(usagePoint);
    }

    @Test
    public void testGetStart() {
        assertThat(usagePointAccountability.getStart()).isEqualTo(START);
    }

    @Test
    public void testGetEnd() {
        assertThat(usagePointAccountability.getEnd()).isNull();
    }

    @Test
    public void testIsCurrentFalse() {
        when(serviceLocator.getClock().now()).thenReturn(new Date(START.getTime() - 1));

        assertThat(usagePointAccountability.isCurrent()).isFalse();
    }

    @Test
    public void testIsCurrentTrue() {
        when(serviceLocator.getClock().now()).thenReturn(new Date(START.getTime()));

        assertThat(usagePointAccountability.isCurrent()).isTrue();
    }


}
