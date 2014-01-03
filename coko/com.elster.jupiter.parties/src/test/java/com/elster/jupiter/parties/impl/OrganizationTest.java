package com.elster.jupiter.parties.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.ModuleCreator;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyRole;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.Interval;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

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
    
	@Before
    public void setUp() {
    	when(dataModel.getInstance(OrganizationImpl.class)).thenReturn(new OrganizationImpl(dataModel, eventService));
    	when(dataModel.getInstance(PartyRepresentationImpl.class)).thenReturn(new PartyRepresentationImpl(clock, userService));
    	when(dataModel.getInstance(PartyInRoleImpl.class)).thenReturn(new PartyInRoleImpl(clock));
    }
   
	@Test
    public void testCreation() {
		Party party =  OrganizationImpl.from(dataModel,"EICT");
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
