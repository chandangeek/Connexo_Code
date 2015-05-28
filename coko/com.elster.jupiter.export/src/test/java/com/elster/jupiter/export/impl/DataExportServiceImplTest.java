package com.elster.jupiter.export.impl;

import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.export.*;
import com.elster.jupiter.messaging.MessageService;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;

import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.properties.PropertySpec;

import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.tasks.TaskService;

import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TimeService;

import com.elster.jupiter.transaction.Transaction;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.google.common.collect.Range;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.osgi.framework.BundleContext;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DataExportServiceImplTest {
    private static final long ID = 15;
    private DataExportServiceImpl dataExportService;
    private static final Instant NOW = ZonedDateTime.of(2013, 9, 10, 14, 47, 24, 0, ZoneId.of("Europe/Paris")).toInstant();

    @Mock
    private DataModel dataModel;
    @Mock
    private OrmService ormService;
    @Mock
    private MessageService messageService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private Clock clock;
    @Mock
    private UserService userService;
    @Mock
    private BundleContext bundleContext;
    @Mock
    private DataMapper<ExportTask> readingTypeDataExportTaskFactory;
    @Mock
    private DataMapper<IExportTask> iReadingTypeDataExportTaskFactory;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Table table;
    @Mock
    private DataProcessorFactory dataProcessorFactory;
    @Mock
    private TaskService taskService;
    @Mock
    private TimeService timeService;
    @Mock
    private MeteringService meteringService;
    @Mock
    private MeteringGroupsService meteringGroupsService;
    @Mock
    private NlsService nlsService;
    @Mock
    private QueryService queryService;
    @Mock
    private TransactionService transactionService;
    @Mock
    private ExportTask exportTask;
    @Mock
    private IExportTask iExportTask;
    @Mock
    private RecurrentTask recurrentTask;
    @Mock
    private TaskOccurrence taskOccurrence;
    @Mock
    private DataExportOccurrenceImpl dataExportOccurrence;
    @Mock
    private RelativePeriod relativePeriod;
    @Mock
    private PropertySpec propertySpec1, propertySpec2, propertySpec3;
    @Mock
    private ReadingTypeDataSelector readingTypeDataSelector;
    @Mock
    private EndDeviceGroup endDeviceGroup;

    private static final Instant nextExecution = ZonedDateTime.of(2013, 9, 10, 14, 47, 24, 0, ZoneId.of("Europe/Paris")).toInstant();
    private static String NAME = "task";
    private static String DATA_PROCESSOR = "factory";

    @Before
    public void setUp() throws SQLException {
        when(iExportTask.getReadingTypeDataSelector()).thenReturn(Optional.of(readingTypeDataSelector));
        when(ormService.newDataModel(anyString(), anyString())).thenReturn(dataModel);
        when(dataModel.addTable(anyString(), any())).thenReturn(table);
        when(dataModel.<ExportTask>mapper(any())).thenReturn(readingTypeDataExportTaskFactory);
        when(dataModel.<IExportTask>mapper(any())).thenReturn(iReadingTypeDataExportTaskFactory);
        when(transactionService.execute(any())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                return ((Transaction<?>) invocationOnMock.getArguments()[0]).perform();
            }
        });
        when(clock.instant()).thenReturn(NOW);
        dataExportService = new DataExportServiceImpl();
        dataExportService.setOrmService(ormService);
        dataExportService.setTimeService(timeService);
        dataExportService.setTaskService(taskService);
        dataExportService.setMeteringGroupsService(meteringGroupsService);
        dataExportService.setMessageService(messageService);
        dataExportService.setNlsService(nlsService);
        dataExportService.setMeteringService(meteringService);
        dataExportService.setQueryService(queryService);
        dataExportService.setClock(clock);
        dataExportService.setUserService(userService);
        dataExportService.addProcessor(dataProcessorFactory);
    }

    @Test
    public void testNewBuilder() {
        ExportTaskImpl readingTypeDataExportTaskImpl = new ExportTaskImpl(dataModel, dataExportService, taskService, thesaurus);
        when(dataModel.getInstance(ExportTaskImpl.class)).thenReturn(readingTypeDataExportTaskImpl);
        DataExportTaskBuilderImpl dataExportTaskBuilder = new DataExportTaskBuilderImpl(dataModel)
                .setName(NAME)
                .setDataProcessorName(DATA_PROCESSOR)
                .setNextExecution(nextExecution)
                .selectingStandard()
                .fromEndDeviceGroup(endDeviceGroup)
                .fromExportPeriod(relativePeriod)
                .endSelection();
        assertThat(dataExportService.newBuilder()).isInstanceOf(DataExportTaskBuilder.class);
        assertThat(dataExportTaskBuilder.build()).isEqualTo(readingTypeDataExportTaskImpl);
        assertThat(readingTypeDataExportTaskImpl.getName()).isEqualTo(NAME);
    }

    @Test
    public void testCreateExportOccurrence() {
        when(taskOccurrence.getRecurrentTask()).thenReturn(recurrentTask);
        when(iReadingTypeDataExportTaskFactory.getUnique("recurrentTask", recurrentTask)).thenReturn(Optional.of(iExportTask));

        DataExportOccurrenceImpl dataExportOccurrence1 = new DataExportOccurrenceImpl(dataModel, clock);
        when(dataModel.getInstance(DataExportOccurrenceImpl.class)).thenReturn(dataExportOccurrence1);
        when(taskOccurrence.getTriggerTime()).thenReturn(NOW);
        when(readingTypeDataSelector.getExportPeriod()).thenReturn(relativePeriod);
        when(relativePeriod.getInterval(any())).thenReturn(Range.openClosed(ZonedDateTime.ofInstant(NOW, ZoneId.of("Europe/Paris")), ZonedDateTime.ofInstant(NOW, ZoneId.of("Europe/Paris"))));

        assertThat(dataExportService.createExportOccurrence(taskOccurrence)).isEqualTo(dataExportOccurrence1);
        assertThat(dataExportOccurrence1.getTask()).isEqualTo(iExportTask);
        assertThat(dataExportOccurrence1.getTaskOccurrence()).isEqualTo(taskOccurrence);
    }

    @Test
    public void testFindExportTaskById() {
        when(iReadingTypeDataExportTaskFactory.getOptional(ID)).thenReturn(Optional.of(iExportTask));

        assertThat(dataExportService.findExportTask(ID).get()).isEqualTo(iExportTask);
    }

    @Test
    public void testGetExportTaskByIdNotFound() {
        when(iReadingTypeDataExportTaskFactory.getOptional(ID)).thenReturn(Optional.<IExportTask>empty());

        assertThat(dataExportService.findExportTask(ID).isPresent()).isFalse();
    }

    @Test
    public void testGetAvailableProcessors() {
        assertThat(dataExportService.getAvailableProcessors().size()).isEqualTo(1);
    }

    @Test
    public void testGetDataFormatterFactory() {
        when(dataProcessorFactory.getName()).thenReturn("factory");

        assertThat(dataExportService.getDataFormatterFactory("factory")).isEqualTo(Optional.of(dataProcessorFactory));
    }

    @Test
    public void testGetPropertiesSpecsForProcessor() {
        when(dataProcessorFactory.getName()).thenReturn("factory");
        when(dataProcessorFactory.getPropertySpecs()).thenReturn(Arrays.asList(propertySpec1, propertySpec2, propertySpec3));

        assertThat(dataExportService.getPropertiesSpecsForProcessor("factory")).isEqualTo(Arrays.asList(propertySpec1, propertySpec2, propertySpec3));
    }
}
