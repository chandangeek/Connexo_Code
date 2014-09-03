package com.elster.jupiter.parties.impl;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.parties.PartyRepresentation;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.Interval;

@RunWith(MockitoJUnitRunner.class)
public class PartyRepresentationTest {
    
    @Mock
    DataModel dataModel;
    @Mock
    PartyImpl party;
    @Mock
    User user;
    @Mock
    Clock clock;
    @Mock
    UserService userService;

    @Test
    public void testCreation() {
    	PartyRepresentation rep =  new PartyRepresentationImpl(clock,userService).init(party, user, Interval.startAt(new Date()));
    	assertThat(rep.getParty()).isEqualTo(party);
    }
}
