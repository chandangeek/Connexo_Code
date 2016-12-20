package com.energyict.mdc.engine.impl.events.datastorage;

import com.energyict.mdc.device.data.impl.identifiers.DeviceMessageIdentifierForAlreadyKnownMessage;
import com.energyict.mdc.engine.events.Category;
import com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Clock;
import java.util.Arrays;
import java.util.Collections;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Copyrights EnergyICT
 * Date: 24/02/2016
 * Time: 16:19
 */
@RunWith(MockitoJUnitRunner.class)
public class CollectedMessageListEventTest {

    @Mock
    private AbstractComServerEventImpl.ServiceProvider serviceProvider;

    @Before
    public void initMocks(){
        when(serviceProvider.clock()).thenReturn(Clock.systemDefaultZone());
    }

    @Test
    public void testCategory(){
        CollectedMessageList messageList = mock(CollectedMessageList.class);

        // Business method
        CollectedMessageListEvent event = new CollectedMessageListEvent(serviceProvider, messageList);
        assertThat(event.getCategory()).isEqualTo(Category.COLLECTED_DATA_PROCESSING);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoPayload(){
        // Business method
        new CollectedMessageListEvent(serviceProvider, null);
    }

    @Test
    public void testToString(){
        DeviceMessageIdentifierForAlreadyKnownMessage firstIdentifier = mock(DeviceMessageIdentifierForAlreadyKnownMessage.class);
        when(firstIdentifier.toString()).thenReturn( "messageId = 1");
        DeviceMessageIdentifierForAlreadyKnownMessage secondIdentifier = mock(DeviceMessageIdentifierForAlreadyKnownMessage.class);
        when(secondIdentifier.toString()).thenReturn( "messageId = 2");

        CollectedMessage firstMessage = mock(CollectedMessage.class);
        when(firstMessage.getDeviceProtocolInformation()).thenReturn("Protocol Information for first message");
        when(firstMessage.getMessageIdentifier()).thenReturn(firstIdentifier);
        CollectedMessage secondMessage = mock(CollectedMessage.class);
        when(secondMessage.getDeviceProtocolInformation()).thenReturn("Protocol Information for second message");
        when(secondMessage.getMessageIdentifier()).thenReturn(secondIdentifier);

        CollectedMessageList messageList = mock(CollectedMessageList.class);
        when(messageList.getCollectedMessages()).thenReturn(Arrays.asList(firstMessage, secondMessage));

        // Business method
        CollectedMessageListEvent event = new CollectedMessageListEvent(serviceProvider, messageList);

        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).matches("\\{.*\\}");
    }

    @Test
    public void testToStringWithoutMessages(){
        CollectedMessageList messageList = mock(CollectedMessageList.class);
        when(messageList.getCollectedMessages()).thenReturn(Collections.emptyList());

        // Business method
        CollectedMessageListEvent event = new CollectedMessageListEvent(serviceProvider, messageList);

        // Business method
        String eventString = event.toString();

        // Asserts
        assertThat(eventString).matches("\\{.*\\}");
    }
}
