package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.util.cron.CronExpression;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.time.Clock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Date;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DefaultRecurrentTaskBuilderTest {

    private static final String NAME = "name";
    private static final String PAYLOAD = "PAYLOAD";
    private static final Date NOW = new Date(123456);
    private static final Date FIRST = new Date(124000);
    private static final String CRON_STRING = "0 * * * * ? *";
    private DefaultRecurrentTaskBuilder defaultRecurrentTaskBuilder;

    @Mock
    private CronExpressionParser cronExpressionParser;
    @Mock
    private DestinationSpec destination;
    @Mock
    private Clock clock;
    @Mock
    private ServiceLocator serviceLocator;
    @Mock
    private CronExpression cronExpression;

    @Before
    public void setUp() {
        defaultRecurrentTaskBuilder = new DefaultRecurrentTaskBuilder(cronExpressionParser);

        when(serviceLocator.getClock()).thenReturn(clock);
        when(clock.now()).thenReturn(NOW);
        when(cronExpressionParser.parse(CRON_STRING)).thenReturn(cronExpression);
        when(cronExpression.nextAfter(NOW)).thenReturn(FIRST);

        Bus.setServiceLocator(serviceLocator);
    }

    @After
    public void tearDown() {
        Bus.setServiceLocator(null);
    }

    @Test
    public void testCronStringAndScheduleImmediately() {
        RecurrentTask recurrentTask = defaultRecurrentTaskBuilder.setDestination(destination)
                .scheduleImmediately()
                .setCronExpression(CRON_STRING)
                .setName(NAME)
                .setPayLoad(PAYLOAD)
                .build();

        assertThat(recurrentTask.getNextExecution()).isEqualTo(FIRST);
    }

    @Test
    public void testCronString() {
        RecurrentTask recurrentTask = defaultRecurrentTaskBuilder.setDestination(destination)
                .scheduleImmediately()
                .setCronExpression(CRON_STRING)
                .setName(NAME)
                .setPayLoad(PAYLOAD)
                .build();

        recurrentTask.updateNextExecution();
        assertThat(recurrentTask.getNextExecution()).isEqualTo(FIRST);
    }

    @Test
    public void testName() {
        RecurrentTask recurrentTask = defaultRecurrentTaskBuilder.setDestination(destination)
                .scheduleImmediately()
                .setCronExpression(CRON_STRING)
                .setName(NAME)
                .setPayLoad(PAYLOAD)
                .build();

        assertThat(recurrentTask.getName()).isEqualTo(NAME);
    }

    @Test
    public void testPayload() {
        RecurrentTask recurrentTask = defaultRecurrentTaskBuilder.setDestination(destination)
                .scheduleImmediately()
                .setCronExpression(CRON_STRING)
                .setName(NAME)
                .setPayLoad(PAYLOAD)
                .build();

        assertThat(recurrentTask.getPayLoad()).isEqualTo(PAYLOAD);
    }

    @Test
    public void testDestination() {
        RecurrentTask recurrentTask = defaultRecurrentTaskBuilder.setDestination(destination)
                .scheduleImmediately()
                .setCronExpression(CRON_STRING)
                .setName(NAME)
                .setPayLoad(PAYLOAD)
                .build();

        assertThat(recurrentTask.getDestination()).isEqualTo(destination);
    }


}
