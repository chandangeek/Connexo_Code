package com.elster.jupiter.parties.impl;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.time.Interval;
import com.google.common.collect.ImmutableList;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Date;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PartyRepresentationImplTest extends EqualsContractTest {

    @Mock
    private Party party;
    @Mock
    private User user, user1;
    PartyRepresentationImpl representation;

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {

    }

    @Override
    protected Object getInstanceA() {
        if (representation == null) {
            when(party.getId()).thenReturn(51L);
            when(user.getName()).thenReturn("user");
            when(user1.getName()).thenReturn("user1");
            representation = new PartyRepresentationImpl(party, user, Interval.startAt(new Date(0)));
        }
        return representation;
    }

    @Override
    protected Object getInstanceEqualToA() {
        return new PartyRepresentationImpl(party, user, Interval.startAt(new Date(15)));
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        return ImmutableList.of(new PartyRepresentationImpl(party, user1, Interval.startAt(new Date(0))));
    }

    @Override
    protected boolean canBeSubclassed() {
        return false;
    }

    @Override
    protected Object getInstanceOfSubclassEqualToA() {
        return null;
    }
}
