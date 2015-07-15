package com.elster.jupiter.events.impl;

import com.elster.jupiter.events.EventPropertyType;
import com.elster.jupiter.events.EventType;
import com.elster.jupiter.events.ValueType;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.beans.BeanService;
import com.elster.jupiter.util.json.JsonService;
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

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LocalEventImplTest {

    private static final Instant NOW = ZonedDateTime.of(2013, 9, 13, 4, 56, 14, 0, ZoneId.systemDefault()).toInstant();
    private static final String LEE_DUNCAN = "Lee Duncan";
    private static final String DOG_OWNER = "dogOwner";
    private static final String TOPIC = "topic";
    private static final String DESTINATION = "destiny";
    private static final String SERIALIZED = "serialized";
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
    @Mock
    private Thesaurus thesaurus;

    @Before
    public void setUp() {
        when(clock.instant()).thenReturn(NOW);
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
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testCreation() {
        LocalEventImpl localEvent = new LocalEventImpl(NOW, jsonService, eventConfiguration, messageService, beanService, eventType, source, thesaurus);

        assertThat(localEvent.getDateTime()).isEqualTo(NOW);
        assertThat(localEvent.getSource()).isEqualTo(source);
        assertThat(localEvent.getType()).isEqualTo(eventType);
    }

    @Test
    public void testToOsgiEvent() {
        LocalEventImpl localEvent = new LocalEventImpl(NOW, jsonService, eventConfiguration, messageService, beanService, eventType, source, thesaurus);

        Event event = localEvent.toOsgiEvent();

        assertThat(event.containsProperty(DOG_OWNER));
        assertThat(event.getProperty(DOG_OWNER)).isEqualTo(LEE_DUNCAN);
        assertThat(event.getTopic()).isEqualTo(TOPIC);
    }

    @Test
    public void testPublish() {
        LocalEventImpl localEvent = new LocalEventImpl(NOW, jsonService, eventConfiguration, messageService, beanService, eventType, source, thesaurus);

        localEvent.publish();

        verify(destination).message(SERIALIZED);
        verify(destination.message(SERIALIZED)).withCorrelationId(TOPIC);
        verify(destination.message(SERIALIZED).withCorrelationId(TOPIC)).send();

    }


    private interface Dog {
        Owner getOwner();
    }

    private interface Owner {
        String getName();
    }

    @Test
    public void testLocales() {
        Locale[] availableLocales = Locale.getAvailableLocales();
        System.out.println(availableLocales.length);
        for (Locale availableLocale : availableLocales) {
            System.out.println(availableLocale + ": " + availableLocale.getDisplayName() + " - " + availableLocale.getDisplayCountry() + " - " + availableLocale.getDisplayLanguage());
        }
    }
}
