package com.elster.jupiter.export.impl;

import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataExportTaskBuilder;
import com.elster.jupiter.export.DataFormatterFactory;
import com.elster.jupiter.export.DataSelector;
import com.elster.jupiter.export.DataSelectorFactory;
import com.elster.jupiter.export.ExportTask;
import com.elster.jupiter.messaging.DestinationSpec;
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
import com.elster.jupiter.validation.ValidationService;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import org.osgi.framework.BundleContext;

import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.elster.jupiter.export.DataExportService.DATA_TYPE_PROPERTY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DataExportServiceImplTest {
    private static final long ID = 15;
    private static final Instant NOW = ZonedDateTime.of(2013, 9, 10, 14, 47, 24, 0, ZoneId.of("Europe/Paris")).toInstant();
    private static final Instant nextExecution = ZonedDateTime.of(2013, 9, 10, 14, 47, 24, 0, ZoneId.of("Europe/Paris")).toInstant();
    private static String DATA_FORMATTER = "factory";

    private DataExportServiceImpl dataExportService;

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
    private DataFormatterFactory dataFormatterFactory;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
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
    private IStandardDataSelector standardDataSelector;
    @Mock
    private EndDeviceGroup endDeviceGroup;
    @Mock
    private ValidationService validationService;
    @Mock
    private DestinationSpec destinationSpec;
    @Mock
    private ValidatorFactory validatorFactory;
    @Mock
    private Validator validator;

    @Before
    public void setUp() throws SQLException {
        when(dataFormatterFactory.getName()).thenReturn(DATA_FORMATTER);
        when(iExportTask.getReadingTypeDataSelector()).thenReturn(Optional.of(standardDataSelector));
        when(iExportTask.getEventDataSelector()).thenReturn(Optional.empty());
        when(ormService.newDataModel(anyString(), anyString())).thenReturn(dataModel);
        when(dataModel.addTable(anyString(), any())).thenReturn(table);
        when(dataModel.<ExportTask>mapper(any())).thenReturn(readingTypeDataExportTaskFactory);
        when(dataModel.<IExportTask>mapper(any())).thenReturn(iReadingTypeDataExportTaskFactory);
        when(dataModel.getInstance(DataExportOccurrenceImpl.class)).thenReturn(dataExportOccurrence);
        when(dataExportOccurrence.init(any(), any())).thenReturn(dataExportOccurrence);
        when(transactionService.execute(any())).thenAnswer(invocationOnMock -> ((Transaction<?>) invocationOnMock.getArguments()[0]).perform());
        when(messageService.getDestinationSpec("DataExport")).thenReturn(Optional.of(destinationSpec));
        when(dataModel.getValidatorFactory()).thenReturn(validatorFactory);
        when(validatorFactory.getValidator()).thenReturn(validator);
        when(validator.validate(any(), anyVararg())).thenReturn(Collections.emptySet());
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
        dataExportService.addFormatter(dataFormatterFactory, ImmutableMap.of(DATA_TYPE_PROPERTY, DataExportService.STANDARD_READING_DATA_TYPE));
    }

    @Test
    public void testNewBuilder() {
        ExportTaskImpl readingTypeDataExportTaskImpl = new ExportTaskImpl(dataModel, dataExportService, taskService, thesaurus);
        when(dataModel.getInstance(ExportTaskImpl.class)).thenReturn(readingTypeDataExportTaskImpl);
        StandardDataSelectorImpl selectorImpl = new StandardDataSelectorImpl(dataModel, meteringService);
        when(dataModel.getInstance(StandardDataSelectorImpl.class)).thenReturn(selectorImpl);
        DataSelectorFactory dataSelectorFactory = mock(DataSelectorFactory.class);
        when(dataSelectorFactory.getName()).thenReturn("Standard Data Selector");
        DataSelector dataSelector = mock(DataSelector.class);
        when(dataSelectorFactory.createDataSelector(anyMap(), any(Logger.class))).thenReturn(dataSelector);
        dataExportService.addSelector(dataSelectorFactory, ImmutableMap.of(DATA_TYPE_PROPERTY, "Standard Data Selector"));
        DataExportTaskBuilderImpl dataExportTaskBuilder = new DataExportTaskBuilderImpl(dataModel)
                .setName("task")
                .setDataFormatterFactoryName(DATA_FORMATTER)
                .setNextExecution(nextExecution)
                .selectingReadingTypes()
                .fromEndDeviceGroup(endDeviceGroup)
                .fromExportPeriod(relativePeriod)
                .endSelection();
        assertThat(dataExportService.newBuilder()).isInstanceOf(DataExportTaskBuilder.class);
        assertThat(dataExportTaskBuilder.create()).isEqualTo(readingTypeDataExportTaskImpl);
    }

    @Test
    public void testCreateExportOccurrence() {
        when(taskOccurrence.getRecurrentTask()).thenReturn(recurrentTask);
        when(iReadingTypeDataExportTaskFactory.getUnique("recurrentTask", recurrentTask)).thenReturn(Optional.of(iExportTask));

        DataExportOccurrenceImpl dataExportOccurrence1 = new DataExportOccurrenceImpl(dataModel, taskService, transactionService, thesaurus, clock);
        when(dataModel.getInstance(DataExportOccurrenceImpl.class)).thenReturn(dataExportOccurrence1);
        when(taskOccurrence.getTriggerTime()).thenReturn(NOW);
        when(standardDataSelector.getExportPeriod()).thenReturn(relativePeriod);
        when(relativePeriod.getOpenClosedInterval(any())).thenReturn(Range.openClosed(NOW, NOW));

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
        when(iReadingTypeDataExportTaskFactory.getOptional(ID)).thenReturn(Optional.empty());

        assertThat(dataExportService.findExportTask(ID).isPresent()).isFalse();
    }

    @Test
    public void testGetAvailableFormatters() {
        assertThat(dataExportService.getAvailableFormatters().size()).isEqualTo(1);
    }

    @Test
    public void testGetDataFormatterFactory() {
        when(dataFormatterFactory.getName()).thenReturn("factory");

        assertThat(dataExportService.getDataFormatterFactory("factory")).isEqualTo(Optional.of(dataFormatterFactory));
    }

    @Test
    public void testGetPropertiesSpecsForFormatter() {
        when(dataFormatterFactory.getName()).thenReturn("factory");
        when(dataFormatterFactory.getPropertySpecs()).thenReturn(Arrays.asList(propertySpec1, propertySpec2, propertySpec3));

        assertThat(dataExportService.getPropertiesSpecsForFormatter("factory")).isEqualTo(Arrays.asList(propertySpec1, propertySpec2, propertySpec3));
    }

    @Test
    public void testFormattersMatchingSelector() {
        String requiredType = "requiredType";
        DataSelectorFactory dataSelector = mock(DataSelectorFactory.class);
        dataExportService.addSelector(dataSelector, ImmutableMap.of(DATA_TYPE_PROPERTY, requiredType));

        DataFormatterFactory nonMatch = mock(DataFormatterFactory.class);
        DataFormatterFactory trivialMatch = mock(DataFormatterFactory.class);
        DataFormatterFactory complexMatch = mock(DataFormatterFactory.class);
        DataFormatterFactory noDataTypes = mock(DataFormatterFactory.class);
        DataFormatterFactory complexNonMatch = mock(DataFormatterFactory.class);

        dataExportService.addFormatter(nonMatch, ImmutableMap.of(DATA_TYPE_PROPERTY, "nope"));
        dataExportService.addFormatter(trivialMatch, ImmutableMap.of(DATA_TYPE_PROPERTY, requiredType));
        dataExportService.addFormatter(complexMatch, ImmutableMap.of(DATA_TYPE_PROPERTY, new String[]{"nope", requiredType, "anotherNope"}));
        dataExportService.addFormatter(noDataTypes, Collections.emptyMap());
        dataExportService.addFormatter(complexNonMatch, ImmutableMap.of(DATA_TYPE_PROPERTY, new String[]{"nope", "anotherNope", "andYetAnother"}));

        assertThat(dataExportService.formatterFactoriesMatching(dataSelector)).hasSize(2).containsOnly(complexMatch, trivialMatch);
    }

}
