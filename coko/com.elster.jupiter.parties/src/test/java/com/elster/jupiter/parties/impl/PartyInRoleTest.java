package com.elster.jupiter.parties.impl;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.ModuleCreator;
import com.elster.jupiter.parties.PartyInRole;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.Interval;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

@RunWith(MockitoJUnitRunner.class)
public class PartyInRoleTest {
    
    @Mock
    DataModel dataModel;
    @Mock
    PartyImpl party;
    @Mock
    PartyRoleImpl role;
    @Mock
    User user;
    @Mock
    Clock clock;
    
    @Before
    public void setUp() {
    	when(dataModel.getInstance(PartyInRoleImpl.class)).thenReturn(new PartyInRoleImpl(clock));
    }
    
    @Test
    public void testCreation() {
    	PartyInRole partyInRole =  PartyInRoleImpl.from(dataModel, party , role, Interval.startAt(new Date()));
    	assertThat(partyInRole.getParty()).isEqualTo(party);
    	assertThat(partyInRole.getRole()).isEqualTo(role);
    }
}
