package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.util.cron.CronExpression;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.json.JsonService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultRecurrentTaskBuilderTest {

    private static final String NAME = "name";
    private static final String PAYLOAD = "PAYLOAD";
    private static final Instant NOW = Instant.ofEpochMilli(123456);
    private static final Instant FIRST = Instant.ofEpochMilli(124000);
    private static final String CRON_STRING = "0 * * * * ? *";
    private DefaultRecurrentTaskBuilder defaultRecurrentTaskBuilder;

    @Mock
    private CronExpressionParser cronExpressionParser;
    @Mock
    private DestinationSpec destination;
    @Mock
    private Clock clock;
    @Mock
    private CronExpression cronExpression;
    @Mock
    private DataModel dataModel;
    @Mock
    private MessageService messageService;
    @Mock
    private JsonService jsonService;

    @Before
    public void setUp() {
        when(dataModel.getInstance(RecurrentTaskImpl.class)).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return new RecurrentTaskImpl(dataModel, cronExpressionParser, messageService, jsonService, clock);
            }
        });

        defaultRecurrentTaskBuilder = new DefaultRecurrentTaskBuilder(dataModel, cronExpressionParser);
        when(clock.instant()).thenReturn(NOW);
        when(cronExpressionParser.parse(CRON_STRING)).thenReturn(Optional.of(cronExpression));
        when(cronExpression.nextOccurrence(ZonedDateTime.ofInstant(NOW, ZoneId.systemDefault()))).thenReturn(Optional.of(ZonedDateTime.ofInstant(FIRST, ZoneId.systemDefault())));
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testCronStringAndScheduleImmediately() {
        RecurrentTask recurrentTask = defaultRecurrentTaskBuilder.setDestination(destination)
                .scheduleImmediately(true)
                .setScheduleExpressionString(CRON_STRING)
                .setName(NAME)
                .setPayLoad(PAYLOAD)
                .build();

        assertThat(recurrentTask.getNextExecution()).isEqualTo(FIRST);
    }

    @Test
    public void testCronString() {
        RecurrentTask recurrentTask = defaultRecurrentTaskBuilder.setDestination(destination)
                .scheduleImmediately(true)
                .setScheduleExpressionString(CRON_STRING)
                .setName(NAME)
                .setPayLoad(PAYLOAD)
                .build();

        recurrentTask.updateNextExecution();
        assertThat(recurrentTask.getNextExecution()).isEqualTo(FIRST);
    }

    @Test
    public void testName() {
        RecurrentTask recurrentTask = defaultRecurrentTaskBuilder.setDestination(destination)
                .scheduleImmediately(true)
                .setScheduleExpressionString(CRON_STRING)
                .setName(NAME)
                .setPayLoad(PAYLOAD)
                .build();

        assertThat(recurrentTask.getName()).isEqualTo(NAME);
    }

    @Test
    public void testPayload() {
        RecurrentTask recurrentTask = defaultRecurrentTaskBuilder.setDestination(destination)
                .scheduleImmediately(true)
                .setScheduleExpressionString(CRON_STRING)
                .setName(NAME)
                .setPayLoad(PAYLOAD)
                .build();

        assertThat(recurrentTask.getPayLoad()).isEqualTo(PAYLOAD);
    }

    @Test
    public void testDestination() {
        RecurrentTask recurrentTask = defaultRecurrentTaskBuilder.setDestination(destination)
                .scheduleImmediately(true)
                .setScheduleExpressionString(CRON_STRING)
                .setName(NAME)
                .setPayLoad(PAYLOAD)
                .build();

        assertThat(recurrentTask.getDestination()).isEqualTo(destination);
    }


}
