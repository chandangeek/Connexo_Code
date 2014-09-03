package com.elster.jupiter.parties.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyRole;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.Interval;
import com.google.inject.util.Providers;

@RunWith(MockitoJUnitRunner.class)
public class OrganizationTest {
    @Mock
    private DataModel dataModel;
    @Mock
    private User user;
    @Mock
    private Clock clock;
    @Mock
    private UserService userService;
    @Mock
    private EventService eventService;
    @Mock
    private PartyRole role;
   
	@Test
    public void testCreation() {
		Party party =  new OrganizationImpl(dataModel, eventService, Providers.of(new PartyInRoleImpl(clock)),Providers.of(new PartyRepresentationImpl(clock, userService))).init("EICT");
    	assertThat(party.getPartyInRoles(Interval.sinceEpoch())).isEmpty();
    	assertThat(party.getCurrentDelegates()).isEmpty();
    	Date now = new Date();
    	when(clock.now()).thenReturn(now);
    	assertThat(party.appointDelegate(user, now).isCurrent()).isTrue();
    	assertThat(party.getCurrentDelegates()).isNotEmpty();
    	assertThat(party.assumeRole(role, now).getParty()).isEqualTo(party);
    	assertThat(party.getPartyInRoles(now)).hasSize(1);
    }
    
}
