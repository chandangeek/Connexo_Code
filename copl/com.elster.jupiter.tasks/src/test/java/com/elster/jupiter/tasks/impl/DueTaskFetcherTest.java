/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.tasks.impl;

import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.util.cron.CronExpression;
import com.elster.jupiter.util.cron.CronExpressionParser;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.sql.Fetcher;
import com.elster.jupiter.util.sql.SqlBuilder;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DueTaskFetcherTest {

    private static final Instant NOW = Instant.ofEpochMilli(65464161);
    private static final long ID = 1631;
    private static final String NAME = "name";
    private static final String CRON = "0 * * * * ? *";
    private static final String PAYLOAD = "PAYLOAD";
    private static final String DESTINATION = "destination";

    @Mock
    private Clock clock;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private MessageService messageService;
    @Mock
    private DestinationSpec destination;
    @Mock
    private CronExpressionParser cronExpressionParser;
    @Mock
    private CronExpression cronExpression;
    private DueTaskFetcher dueTaskFetcher;
    @Mock
    private DataModel dataModel;
    @Mock
    private JsonService jsonService;
    @Mock
    private DataMapper<RecurrentTaskImpl> dataMapper;
    @Mock
    private Fetcher<RecurrentTaskImpl> fetcher;
    @Mock
    private RecurrentTaskImpl task1, task2, task3;

    @Before
    public void setUp() throws SQLException {
        when(clock.instant()).thenReturn(NOW);
        when(messageService.getDestinationSpec(DESTINATION)).thenReturn(Optional.of(destination));
        when(cronExpressionParser.parse(CRON)).thenReturn(java.util.Optional.of(cronExpression));
        when(dataModel.getInstance(RecurrentTaskImpl.class)).thenAnswer(invocationOnMock -> new RecurrentTaskImpl(dataModel, cronExpressionParser, messageService, jsonService, clock));
        when(dataModel.mapper(RecurrentTaskImpl.class)).thenReturn(dataMapper);
        when(dataMapper.builder(anyString(), anyVararg())).thenAnswer(invocation -> new SqlBuilder());
        when(dataMapper.fetcher(any())).thenReturn(fetcher);

        dueTaskFetcher = new DueTaskFetcher(dataModel, messageService, cronExpressionParser, clock);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testNoTasks() throws SQLException {
        when(fetcher.iterator()).thenReturn(Collections.<RecurrentTaskImpl>emptySet().iterator());

        assertThat(new DueTaskFetcher(dataModel, messageService, cronExpressionParser, clock).dueTasks()).isEmpty();
    }

    @Test
    public void testOneTasks() throws SQLException {
        when(fetcher.iterator()).thenReturn(singletonList(task1).iterator());

        Iterable<RecurrentTaskImpl> recurrentTasks = dueTaskFetcher.dueTasks();

        assertThat(recurrentTasks).hasSize(1);
    }

    @Test(expected = UnderlyingSQLFailedException.class)
    public void testProperWrappingOfSQLException() throws SQLException {
        when(fetcher.iterator()).thenThrow(SQLException.class);

        dueTaskFetcher.dueTasks();
    }

}
