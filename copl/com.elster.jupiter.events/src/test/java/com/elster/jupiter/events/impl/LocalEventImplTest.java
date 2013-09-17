package com.elster.jupiter.events.impl;

import com.elster.jupiter.events.EventPropertyType;
import com.elster.jupiter.events.EventType;
import com.elster.jupiter.events.ValueType;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.util.beans.BeanService;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.time.Clock;
import com.google.common.base.Optional;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.osgi.service.event.Event;

import java.util.Arrays;
import java.util.Date;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LocalEventImplTest {

    private static final Date NOW = new DateTime(2013, 9, 13, 4, 56, 14).toDate();
    private static final String LEE_DUNCAN = "Lee Duncan";
    private static final String DOG_OWNER = "dogOwner";
    private static final String TOPIC = "topic";
    private static final String DESTINATION = "destiny";
    private static final String SERIALIZED = "serialized";
    @Mock
    private ServiceLocator serviceLocator;
    @Mock
    private Clock clock;
    @Mock
    private Dog source;
    @Mock
    private EventType eventType;
    @Mock
    private EventPropertyType propertyType;
    @Mock
    private Owner owner;
    @Mock
    private BeanService beanService;
    @Mock
    private JsonService jsonService;
    @Mock
    private EventConfiguration eventConfiguration;
    @Mock
    private MessageService messageService;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DestinationSpec destination;

    @Before
    public void setUp() {
        when(serviceLocator.getClock()).thenReturn(clock);
        when(serviceLocator.getBeanService()).thenReturn(beanService);
        when(serviceLocator.getJsonService()).thenReturn(jsonService);
        when(serviceLocator.getEventConfiguration()).thenReturn(eventConfiguration);
        when(serviceLocator.getMessageService()).thenReturn(messageService);
        when(clock.now()).thenReturn(NOW);
        when(eventType.getPropertyTypes()).thenReturn(Arrays.asList(propertyType));
        when(eventType.getTopic()).thenReturn(TOPIC);
        when(propertyType.getValueType()).thenReturn(ValueType.STRING);
        when(propertyType.getAccessPath()).thenReturn("owner.name");
        when(propertyType.getName()).thenReturn(DOG_OWNER);
        when(source.getOwner()).thenReturn(owner);
        when(owner.getName()).thenReturn(LEE_DUNCAN);
        when(beanService.get(any(Object.class), anyString())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object entity = invocationOnMock.getArguments()[0];
                if ("owner".equals(invocationOnMock.getArguments()[1])) {
                    return ((Dog) entity).getOwner();
                }
                if ("name".equals(invocationOnMock.getArguments()[1])) {
                    return ((Owner) entity).getName();
                }
                return null;
            }
        });
        when(jsonService.serialize(any())).thenReturn(SERIALIZED);
        when(eventConfiguration.getEventDestinationName()).thenReturn(DESTINATION);
        when(messageService.getDestinationSpec(DESTINATION)).thenReturn(Optional.of(destination));

        Bus.setServiceLocator(serviceLocator);
    }

    @After
    public void tearDown() {
        Bus.setServiceLocator(null);
    }

    @Test
    public void testCreation() {
        LocalEventImpl localEvent = new LocalEventImpl(eventType, source);

        assertThat(localEvent.getDateTime()).isEqualTo(NOW);
        assertThat(localEvent.getSource()).isEqualTo(source);
        assertThat(localEvent.getType()).isEqualTo(eventType);
    }

    @Test
    public void testToOsgiEvent() {
        LocalEventImpl localEvent = new LocalEventImpl(eventType, source);

        Event event = localEvent.toOsgiEvent();

        assertThat(event.containsProperty(DOG_OWNER));
        assertThat(event.getProperty(DOG_OWNER)).isEqualTo(LEE_DUNCAN);
        assertThat(event.getTopic()).isEqualTo(TOPIC);
    }

    @Test
    public void testPublish() {
        LocalEventImpl localEvent = new LocalEventImpl(eventType, source);

        localEvent.publish();

        verify(destination.message(SERIALIZED)).send();
    }


    private interface Dog {
        Owner getOwner();
    }

    private interface Owner {
        String getName();
    }
}
