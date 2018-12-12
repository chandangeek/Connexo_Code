/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.parties.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyRole;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;

import com.google.common.collect.Range;
import com.google.inject.util.Providers;

import java.time.Clock;
import java.time.Instant;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

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
		Party party =  new OrganizationImpl(dataModel, eventService, Providers.of(new PartyInRoleImpl(dataModel, clock)),Providers.of(new PartyRepresentationImpl(userService, clock))).init("EICT");
    	assertThat(party.getPartyInRoles(Range.atLeast(Instant.EPOCH))).isEmpty();
    	assertThat(party.getCurrentDelegates()).isEmpty();
    	Instant now = Instant.now();
    	when(clock.instant()).thenReturn(now);
    	assertThat(party.appointDelegate(user, now).isCurrent()).isTrue();
    	assertThat(party.getCurrentDelegates()).isNotEmpty();
    	assertThat(party.assumeRole(role, now).getParty()).isEqualTo(party);
    	assertThat(party.getPartyInRoles(now)).hasSize(1);
    }

}
