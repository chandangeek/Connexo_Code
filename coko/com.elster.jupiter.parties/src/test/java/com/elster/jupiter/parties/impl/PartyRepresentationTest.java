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
import com.elster.jupiter.parties.PartyRepresentation;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.Interval;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

@RunWith(MockitoJUnitRunner.class)
public class PartyRepresentationTest {

    private Injector injector;
    
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
    
    @Before
    public void setUp() {
    	Class<PartyRepresentationImpl> testClass = PartyRepresentationImpl.class;
    	Module myModule = new AbstractModule() {
			@Override
			protected void configure() {
				bind(Clock.class).toInstance(clock);
				bind(UserService.class).toInstance(userService);
			}
		};
    	injector = Guice.createInjector(myModule,ModuleCreator.create(testClass));
    	when(dataModel.getInstance(testClass)).thenReturn((injector.getInstance(testClass)));
    }

    @Test
    public void testCreation() {
    	PartyRepresentation rep =  PartyRepresentationImpl.from(dataModel, party, user, Interval.startAt(new Date()));
    	assertThat(rep.getParty()).isEqualTo(party);
    }
}
