/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.events;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.Message;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.json.JsonService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ComScheduleUpdatedMessageHandlerTest {

    @Mock
    DataModel dataModel;
    @Mock
    JsonService jsonService;
    @Mock
    EventService eventService;
    @Mock
    Connection connection;
    @Mock
    PreparedStatement preparedStatement;
    @Mock
    ResultSet resultSet;
    @Mock
    Message message;

    Map<String,Object> map = new HashMap<>();

    @Test
    public void testRecalculateBigSet() throws Exception {
        when(dataModel.getConnection(anyBoolean())).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.getResultSet()).thenReturn(resultSet);
        when(resultSet.getLong(0)).thenReturn(100L);
        when(resultSet.getLong(1)).thenReturn(9000L);
        doReturn(map).when(jsonService).deserialize((byte[]) anyObject(), (Class<?>) anyObject());
        map.put("id", 7);
        map.put("event.topics", "com/energyict/mdc/scheduling/comschedules/UPDATED");
        ComScheduleUpdatedMessageHandler messageHandler = new ComScheduleUpdatedMessageHandler(jsonService, eventService, dataModel);
        messageHandler.process(message);

        ArgumentCaptor<ComScheduleUpdatedMessageHandler.IdRange> idRangeArgumentCaptor = ArgumentCaptor.forClass(ComScheduleUpdatedMessageHandler.IdRange.class);
        verify(eventService, times(9)).postEvent(anyString(), idRangeArgumentCaptor.capture());
        assertThat(idRangeArgumentCaptor.getAllValues().get(0).minId).isEqualTo(100L);
        assertThat(idRangeArgumentCaptor.getAllValues().get(0).maxId).isEqualTo(1099L);
        assertThat(idRangeArgumentCaptor.getAllValues().get(1).minId).isEqualTo(1100L);
        assertThat(idRangeArgumentCaptor.getAllValues().get(1).maxId).isEqualTo(2099L);
        assertThat(idRangeArgumentCaptor.getAllValues().get(2).minId).isEqualTo(2100L);
        assertThat(idRangeArgumentCaptor.getAllValues().get(2).maxId).isEqualTo(3099L);

        assertThat(idRangeArgumentCaptor.getAllValues().get(8).minId).isEqualTo(8100L);
        assertThat(idRangeArgumentCaptor.getAllValues().get(8).maxId).isEqualTo(9000L);

    }

    @Test
    public void testRecalculateSingleSet() throws Exception {
        when(dataModel.getConnection(anyBoolean())).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.getResultSet()).thenReturn(resultSet);
        when(resultSet.getLong(0)).thenReturn(100L);
        when(resultSet.getLong(1)).thenReturn(200L);
        doReturn(map).when(jsonService).deserialize((byte[]) anyObject(), (Class<?>) anyObject());
        map.put("id", 7);
        map.put("event.topics", "com/energyict/mdc/scheduling/comschedules/UPDATED");
        ComScheduleUpdatedMessageHandler messageHandler = new ComScheduleUpdatedMessageHandler(jsonService, eventService, dataModel);
        messageHandler.process(message);

        ArgumentCaptor<ComScheduleUpdatedMessageHandler.IdRange> idRangeArgumentCaptor = ArgumentCaptor.forClass(ComScheduleUpdatedMessageHandler.IdRange.class);
        verify(eventService, times(1)).postEvent(anyString(), idRangeArgumentCaptor.capture());
        assertThat(idRangeArgumentCaptor.getAllValues().get(0).minId).isEqualTo(100L);
        assertThat(idRangeArgumentCaptor.getAllValues().get(0).maxId).isEqualTo(200L);
    }

    @Test
    public void testRecalculateFullSet() throws Exception {
        when(dataModel.getConnection(anyBoolean())).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.getResultSet()).thenReturn(resultSet);
        when(resultSet.getLong(0)).thenReturn(1L);
        when(resultSet.getLong(1)).thenReturn(1000L);
        doReturn(map).when(jsonService).deserialize((byte[]) anyObject(), (Class<?>) anyObject());
        map.put("id", 7);
        map.put("event.topics", "com/energyict/mdc/scheduling/comschedules/UPDATED");
        ComScheduleUpdatedMessageHandler messageHandler = new ComScheduleUpdatedMessageHandler(jsonService, eventService, dataModel);
        messageHandler.process(message);

        ArgumentCaptor<ComScheduleUpdatedMessageHandler.IdRange> idRangeArgumentCaptor = ArgumentCaptor.forClass(ComScheduleUpdatedMessageHandler.IdRange.class);
        verify(eventService, times(1)).postEvent(anyString(), idRangeArgumentCaptor.capture());
        assertThat(idRangeArgumentCaptor.getAllValues().get(0).minId).isEqualTo(1L);
        assertThat(idRangeArgumentCaptor.getAllValues().get(0).maxId).isEqualTo(1000L);
    }
}
