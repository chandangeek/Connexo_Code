package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.util.cron.CronExpression;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.time.Clock;
import com.google.common.base.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DueTaskFetcherTest {

    private static final Date NOW = new Date(65464161);
    private static final long ID = 1631;
    private static final String NAME = "name";
    private static final String CRON = "0 * * * * ? *";
    private static final String PAYLOAD = "PAYLOAD";
    private static final String DESTINATION = "destination";
    @Mock
    private ServiceLocator serviceLocator;
    @Mock
    private OrmClient ormClient;
    @Mock
    private Connection connection;
    @Mock
    private Clock clock;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSet resultSet;
    @Mock
    private MessageService messageService;
    @Mock
    private DestinationSpec destination;
    @Mock
    private CronExpressionParser cronExpressionParser;
    @Mock
    private CronExpression cronExpression;
    private DueTaskFetcher dueTaskFetcher;

    @Before
    public void setUp() throws SQLException {
        when(serviceLocator.getOrmClient()).thenReturn(ormClient);
        when(ormClient.getConnection()).thenReturn(connection);
        when(serviceLocator.getClock()).thenReturn(clock);
        when(serviceLocator.getCronExpressionParser()).thenReturn(cronExpressionParser);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(clock.now()).thenReturn(NOW);
        when(serviceLocator.getMessageService()).thenReturn(messageService);
        when(messageService.getDestinationSpec(DESTINATION)).thenReturn(Optional.of(destination));
        when(cronExpressionParser.parse(CRON)).thenReturn(cronExpression);

        Bus.setServiceLocator(serviceLocator);
        dueTaskFetcher = new DueTaskFetcher();
    }

    @After
    public void tearDown() {
        Bus.setServiceLocator(null);
    }

    @Test
    public void testNoTasks() throws SQLException {
        when(resultSet.next()).thenReturn(false);

        assertThat(new DueTaskFetcher().dueTasks()).isEmpty();

        verify(resultSet).close();
    }

    @Test
    public void testOneTasks() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);

        when(resultSet.getLong(1)).thenReturn(ID);
        when(resultSet.getString(2)).thenReturn(NAME);
        when(resultSet.getString(3)).thenReturn(CRON);
        when(resultSet.getLong((4))).thenReturn(NOW.getTime());
        when(resultSet.wasNull()).thenReturn(false);
        when(resultSet.getString(5)).thenReturn(PAYLOAD);
        when(resultSet.getString(6)).thenReturn(DESTINATION);

        Iterable<RecurrentTask> recurrentTasks = dueTaskFetcher.dueTasks();

        assertThat(recurrentTasks).hasSize(1);
        RecurrentTask recurrentTask = recurrentTasks.iterator().next();
        assertThat(recurrentTask.getDestination()).isEqualTo(destination);
        assertThat(recurrentTask.getName()).isEqualTo(NAME);
        assertThat(recurrentTask.getNextExecution()).isEqualTo(NOW);
        assertThat(recurrentTask.getPayLoad()).isEqualTo(PAYLOAD);

        verify(resultSet).close();
    }

    @Test(expected = UnderlyingSQLFailedException.class)
    public void testProperWrappingOfSQLException() throws SQLException {
        when(resultSet.next()).thenThrow(SQLException.class);

        dueTaskFetcher.dueTasks();
    }

}
