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
import org.mockito.Mock;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    public void testRecalculate() throws Exception {
        when(dataModel.getConnection(anyBoolean())).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.getResultSet()).thenReturn(resultSet);
        when(resultSet.getLong(0)).thenReturn(100L);
        when(resultSet.getLong(1)).thenReturn(9000L);
        when(jsonService.deserialize((byte[]) anyObject(), (Class<?>) anyObject())).thenReturn(map);
        map.put("id", 7);
        ComScheduleUpdatedMessageHandler messageHandler = new ComScheduleUpdatedMessageHandler(jsonService, eventService, dataModel);
        messageHandler.process(message);


        verify(eventService).postEvent("com/energyict/mdc/device/data/comschedule/UPDATED", anyObject());

    }
}
