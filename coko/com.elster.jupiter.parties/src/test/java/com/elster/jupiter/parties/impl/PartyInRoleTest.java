/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.parties.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.parties.PartyInRole;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.time.Interval;

import java.time.Clock;
import java.time.Instant;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class PartyInRoleTest {

    @Mock
    private PartyImpl party;
    @Mock
    private PartyRoleImpl role;
    @Mock
    private User user;
    @Mock
    private Clock clock;
    @Mock
    private DataModel dataModel;

    @Test
    public void testCreation() {
    	PartyInRole partyInRole =  new PartyInRoleImpl(dataModel, clock).init(party , role, Interval.startAt(Instant.now()));
    	assertThat(partyInRole.getParty()).isEqualTo(party);
    	assertThat(partyInRole.getRole()).isEqualTo(role);
    }

}