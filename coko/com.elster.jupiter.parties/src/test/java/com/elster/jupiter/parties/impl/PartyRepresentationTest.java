/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.parties.impl;


import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.parties.PartyRepresentation;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;

import com.google.common.collect.Range;

import java.time.Clock;
import java.time.Instant;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

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
    	PartyRepresentation rep =  new PartyRepresentationImpl(userService, clock).init(party, user, Range.atLeast(Instant.now()));
    	assertThat(rep.getParty()).isEqualTo(party);
    }
}
