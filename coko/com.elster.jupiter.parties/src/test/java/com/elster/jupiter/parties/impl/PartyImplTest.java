package com.elster.jupiter.parties.impl;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.parties.PartyInRole;
import com.elster.jupiter.parties.PartyRepresentation;
import com.elster.jupiter.parties.PartyRole;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.time.Interval;
import com.google.common.base.Optional;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.fest.reflect.core.Reflection.field;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public abstract class PartyImplTest extends EqualsContractTest {

    private static final Date START = new DateTime(2013, 5, 30, 14, 55, 0).toDate();
    private static final String USER_NAME = "userName";
    private static final long ID = 6316351L;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ServiceLocator serviceLocator;
    @Mock
    private User user;
    @Mock
    private PartyRole role;

    protected abstract PartyImpl getInstanceToTest();

    @Before
    public void setUpParty() {
        Bus.setServiceLocator(serviceLocator);

        when(user.getName()).thenReturn(USER_NAME);
        when(serviceLocator.getUserService().findUser(USER_NAME)).thenReturn(Optional.of(user));
        when(serviceLocator.getOrmClient().getPartyInRoleFactory().find(eq("party"), any(PartyImpl.class))).thenReturn(Collections.<PartyInRole>emptyList());
    }

    @After
    public void tearDown() {
        Bus.clearServiceLocator(serviceLocator);
    }

    @Test
    public void testAppointDelegate() {
        PartyImpl party = getInstanceToTest();

        PartyRepresentation representation = party.appointDelegate(user, START);

        assertThat(representation.getDelegate()).isEqualTo(user);
        assertThat(representation.getInterval()).isEqualTo(Interval.startAt(START));
        verify(serviceLocator.getOrmClient().getPartyRepresentationFactory()).persist(representation);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAppointDelegateAgain() {
        PartyImpl party = getInstanceToTest();
        PartyRepresentation preExistingRepresentation = new PartyRepresentationImpl(party, user, Interval.startAt(START));
        when(serviceLocator.getOrmClient().getPartyRepresentationFactory().find("party", party)).thenReturn(Arrays.asList(preExistingRepresentation));

        PartyRepresentation representation = party.appointDelegate(user, START);

        assertThat(representation.getDelegate()).isEqualTo(user);
        assertThat(representation.getInterval()).isEqualTo(Interval.startAt(START));
        verify(serviceLocator.getOrmClient().getPartyRepresentationFactory()).persist(representation);
    }

    @Test
    public void testAssumeRole() {
        PartyImpl party = getInstanceToTest();
        when(serviceLocator.getOrmClient().getPartyInRoleFactory().find("party", party)).thenReturn(new ArrayList<PartyInRole>());

        PartyInRole partyInRole = party.assumeRole(role, START);

        assertThat(partyInRole.getInterval()).isEqualTo(Interval.startAt(START));
        assertThat(partyInRole.getRole()).isEqualTo(role);
        verify(serviceLocator.getOrmClient().getPartyInRoleFactory()).persist(partyInRole);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAssumeRoleAgain() {
        PartyImpl party = getInstanceToTest();
        PartyInRole preExists = new PartyInRoleImpl(party, role, Interval.startAt(START));
        when(serviceLocator.getOrmClient().getPartyInRoleFactory().find("party", party)).thenReturn(Arrays.asList(preExists));

        PartyInRole partyInRole = party.assumeRole(role, START);

        assertThat(partyInRole.getInterval()).isEqualTo(Interval.startAt(START));
        assertThat(partyInRole.getRole()).isEqualTo(role);
        verify(serviceLocator.getOrmClient().getPartyInRoleFactory()).persist(partyInRole);
    }

    @Test
    public void testDelete() {
        PartyImpl party = getInstanceToTest();

        party.delete();

        verify(serviceLocator.getOrmClient().getPartyFactory()).remove(party);
        verify(serviceLocator.getEventService()).postEvent(EventType.PARTY_DELETED.topic(), party);
    }

    @Test
    public void testGetAliasName() {
        PartyImpl party = getInstanceToTest();

        String alias = "alias";
        party.setAliasName(alias);

        assertThat(party.getAliasName()).isEqualTo(alias);
    }

    @Test
    public void testGetDescription() {
        PartyImpl party = getInstanceToTest();

        String description = "description";
        party.setDescription(description);

        assertThat(party.getDescription()).isEqualTo(description);
    }

    @Test
    public void testSaveToUpdate() {
        PartyImpl party = getInstanceToTest();

        field("id").ofType(Long.TYPE).in(party).set(ID); // simulate saved

        party.save();

        verify(serviceLocator.getOrmClient().getPartyFactory()).update(party);
        verify(serviceLocator.getEventService()).postEvent(EventType.PARTY_UPDATED.topic(), party);
    }

    @Test
    public void testSaveToCreate() {
        PartyImpl party = getInstanceToTest();

        field("id").ofType(Long.TYPE).in(party).set(0L); // simulate saved

        party.save();

        verify(serviceLocator.getOrmClient().getPartyFactory()).persist(party);
        verify(serviceLocator.getEventService()).postEvent(EventType.PARTY_CREATED.topic(), party);
    }

}
