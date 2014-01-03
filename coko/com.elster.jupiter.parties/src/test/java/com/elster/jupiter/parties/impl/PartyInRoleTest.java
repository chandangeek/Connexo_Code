package com.elster.jupiter.parties.impl;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.elster.jupiter.parties.PartyInRole;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.Interval;

@RunWith(MockitoJUnitRunner.class)
public class PartyInRoleTest {
    
    @Mock
    PartyImpl party;
    @Mock
    PartyRoleImpl role;
    @Mock
    User user;
    @Mock
    Clock clock;
    
    
    @Test
    public void testCreation() {
    	PartyInRole partyInRole =  new PartyInRoleImpl(clock).init(party , role, Interval.startAt(new Date()));
    	assertThat(partyInRole.getParty()).isEqualTo(party);
    	assertThat(partyInRole.getRole()).isEqualTo(role);
    }
}
