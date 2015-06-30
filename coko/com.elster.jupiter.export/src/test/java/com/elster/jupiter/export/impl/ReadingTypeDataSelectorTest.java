package com.elster.jupiter.export.impl;

import com.elster.jupiter.devtools.persistence.test.TransactionVerifier;
import com.elster.jupiter.devtools.tests.fakes.LogRecorder;
import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.export.DataExportProperty;
import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.export.DataExportStrategy;
import com.elster.jupiter.export.DataFormatter;
import com.elster.jupiter.export.DataFormatterFactory;
import com.elster.jupiter.export.FormattedData;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingContainer;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.EndDeviceMembership;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.RefAny;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.tasks.TaskLogHandler;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.validation.ValidationService;
import com.google.common.collect.Range;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ReadingTypeDataSelectorTest {

    @Rule
    public TestRule timeZone = Using.timeZoneOfMcMurdo();

    private ZonedDateTime exportPeriodStart;
    private ZonedDateTime exportPeriodEnd;
    private ZonedDateTime triggerTime;
    private ZonedDateTime lastExported;
    private Range<Instant> exportPeriod;
    private LogRecorder logRecorder;
    private TransactionVerifier transactionService;
    private Clock clock = Clock.system(ZoneId.systemDefault());

    @Mock
    private TaskService taskService;
    @Mock
    private IDataExportService dataExportService;
    @Mock
    private TaskOccurrence occurrence;
    @Mock
    private IDataExportOccurrence dataExportOccurrence;
    @Mock
    private IExportTask task;
    @Mock
    private EndDeviceGroup group;
    @Mock
    private EndDeviceMembership endDeviceMembership1, endDeviceMembership2;
    @Mock
    private Meter meter1, meter2, meter3;
    @Mock
    private IReadingTypeDataExportItem existingItem, newItem, obsoleteItem;
    @Mock
    private ReadingType readingType1, readingType2;
    @Mock
    private DataFormatterFactory dataFormatterFactory;
    @Mock
    private DataExportProperty dataExportProperty;
    @Mock
    private DataFormatter dataFormatter;
    @Mock
    private TaskLogHandler taskLogHandler;
    @Mock
    private DataExportStrategy strategy;
    @Mock(extraInterfaces = {IntervalReadingRecord.class})
    private ReadingRecord reading1, reading2;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Thesaurus thesaurus;
    @Mock
    private ReadingContainer readingContainer;
    @Mock
    private PropertySpec propertySpec;
    @Mock
    private MeteringService meteringService;
    @Mock
    private DataModel dataModel;
    @Mock
    private RelativePeriod exportRelativePeriod;
    @Mock
    private ValidatorFactory validatorFactory;
    @Mock
    private Validator validator;
    @Mock
    private FormattedData formattedData;
    @Mock
    private ValidationService validationService;

    @Before
    public void setUp() {
        exportPeriodStart = ZonedDateTime.of(2012, 11, 10, 6, 0, 0, 0, ZoneId.systemDefault());
        lastExported = ZonedDateTime.of(2012, 11, 5, 6, 0, 0, 0, ZoneId.systemDefault());
        exportPeriodEnd = ZonedDateTime.of(2012, 11, 11, 6, 0, 0, 0, ZoneId.systemDefault());
        triggerTime = ZonedDateTime.of(2012, 11, 20, 0, 0, 0, 0, ZoneId.systemDefault());
        exportPeriod = Range.openClosed(exportPeriodStart.toInstant(), exportPeriodEnd.toInstant());
        logRecorder = new LogRecorder(Level.ALL);

        transactionService = new TransactionVerifier(dataFormatter, newItem, existingItem);

        when(dataModel.getInstance(ReadingTypeDataSelectorImpl.class)).thenAnswer(invocation -> spy(new ReadingTypeDataSelectorImpl(dataModel, transactionService, meteringService, validationService, clock)));
        when(dataModel.getInstance(ReadingTypeInDataSelector.class)).thenAnswer(invocation -> spy(new ReadingTypeInDataSelector(meteringService)));
        when(dataModel.getInstance(ReadingTypeDataExportItemImpl.class)).thenAnswer(invocation -> spy(new ReadingTypeDataExportItemImpl(meteringService, dataExportService, dataModel)));
        when(dataModel.asRefAny(any())).thenAnswer(invocation -> new MyRefAny(invocation.getArguments()[0]));
        when(dataModel.getValidatorFactory()).thenReturn(validatorFactory);
        when(validatorFactory.getValidator()).thenReturn(validator);
        when(validator.validate(any(), anyVararg())).thenReturn(Collections.emptySet());
        when(occurrence.createTaskLogHandler()).thenReturn(taskLogHandler);
        when(taskLogHandler.asHandler()).thenReturn(logRecorder);
        when(dataExportService.createExportOccurrence(occurrence)).thenReturn(dataExportOccurrence);
        when(dataExportService.findDataExportOccurrence(occurrence)).thenReturn(Optional.of(dataExportOccurrence));
        when(dataExportService.getDataFormatterFactory("CSV")).thenReturn(Optional.of(dataFormatterFactory));
        when(dataExportService.getDataSelectorFactory(DataExportService.STANDARD_DATA_SELECTOR)).thenReturn(Optional.of(new StandardDataSelectorFactory(transactionService, meteringService, thesaurus)));
        when(dataExportOccurrence.getTask()).thenReturn(task);
        when(dataExportOccurrence.getExportedDataInterval()).thenReturn(exportPeriod);
        when(dataExportOccurrence.getTriggerTime()).thenReturn(triggerTime.toInstant());
        when(task.getDataFormatter()).thenReturn("CSV");
        when(task.getDataSelector()).thenReturn(DataExportService.STANDARD_DATA_SELECTOR);
        when(task.getDataExportProperties()).thenReturn(Arrays.asList(dataExportProperty));
        when(dataExportProperty.getName()).thenReturn("name");
        when(dataExportProperty.getValue()).thenReturn("CSV");
        when(meter1.is(meter1)).thenReturn(true);
        when(meter2.is(meter2)).thenReturn(true);
        when(meter3.is(meter3)).thenReturn(true);
        when(meter1.getMeter(any())).thenReturn(Optional.of(meter1));
        when(meter2.getMeter(any())).thenReturn(Optional.of(meter2));
        when(meter3.getMeter(any())).thenReturn(Optional.of(meter3));
        when(existingItem.getReadingType()).thenReturn(readingType1);
        when(existingItem.getReadingContainer()).thenReturn(meter2);
        when(meter2.getMeter(any())).thenReturn(Optional.of(meter2));
        when(meter2.getUsagePoint(any())).thenReturn(Optional.<UsagePoint>empty());
        when(existingItem.getLastExportedDate()).thenReturn(Optional.of(lastExported.toInstant()));
        when(newItem.getLastExportedDate()).thenReturn(Optional.<Instant>empty());
        when(newItem.getReadingContainer()).thenReturn(meter1);
        when(meter1.getMeter(any())).thenReturn(Optional.of(meter1));
        when(meter1.getUsagePoint(any())).thenReturn(Optional.<UsagePoint>empty());
        when(newItem.getReadingType()).thenReturn(readingType1);
        when(obsoleteItem.getReadingType()).thenReturn(readingType1);
        when(obsoleteItem.getReadingContainer()).thenReturn(meter3);
        when(meter3.getMeter(any())).thenReturn(Optional.of(meter3));
        when(meter3.getUsagePoint(any())).thenReturn(Optional.<UsagePoint>empty());
        when(group.getMembers(exportPeriod)).thenReturn(Arrays.asList(endDeviceMembership1, endDeviceMembership2));
        when(endDeviceMembership1.getEndDevice()).thenReturn(meter1);
        when(endDeviceMembership2.getEndDevice()).thenReturn(meter2);
        Map<String, Object> propertyMap = new HashMap<>();
        propertyMap.put(dataExportProperty.getName(), dataExportProperty.getValue());
        when(dataFormatterFactory.createDataFormatter(propertyMap)).thenReturn(dataFormatter);
        when(dataFormatterFactory.getPropertySpec("name")).thenReturn(propertySpec);
        when(strategy.isExportContinuousData()).thenReturn(false);
        doReturn(Arrays.asList(reading1)).when(meter1).getReadings(exportPeriod, readingType1);
        doReturn(Arrays.asList(reading2)).when(meter2).getReadings(exportPeriod, readingType1);
        when(dataFormatter.processData(any())).thenReturn(formattedData);
        when(reading1.getSource()).thenReturn("reading1");
        when(reading2.getSource()).thenReturn("reading2");
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testExecuteObsoleteItemIsDeactivated() {
        ReadingTypeDataSelectorImpl selector = ReadingTypeDataSelectorImpl.from(dataModel, task, exportRelativePeriod, group);
        selector.addReadingType(readingType1);
        selector.setEndDeviceGroup(group);
        existingItem = selector.addExportItem(meter2, readingType1);
        obsoleteItem = selector.addExportItem(meter3, readingType1);
        when(task.getReadingTypeDataSelector()).thenReturn(Optional.of(selector));

        selector.selectData(dataExportOccurrence);

        InOrder inOrder = inOrder(obsoleteItem);
        inOrder.verify(obsoleteItem).deactivate();
        inOrder.verify(obsoleteItem).update();
    }

    @Test
    public void testExecuteExistingItemIsUpdated() {
        ReadingTypeDataSelectorImpl selector = ReadingTypeDataSelectorImpl.from(dataModel, task, exportRelativePeriod, group);
        selector.addReadingType(readingType1);
        selector.setEndDeviceGroup(group);
        existingItem = selector.addExportItem(meter2, readingType1);
        obsoleteItem = selector.addExportItem(meter3, readingType1);
        when(task.getReadingTypeDataSelector()).thenReturn(Optional.of(selector));

        selector.selectData(dataExportOccurrence);

        InOrder inOrder = inOrder(existingItem);
        inOrder.verify(existingItem).activate();
        inOrder.verify(existingItem).update();
    }

    @Test
    public void testNewItemIsUpdated() {
        ReadingTypeDataSelectorImpl selector = ReadingTypeDataSelectorImpl.from(dataModel, task, exportRelativePeriod, group);
        selector.addReadingType(readingType1);
        selector.setEndDeviceGroup(group);
        existingItem = selector.addExportItem(meter2, readingType1);
        obsoleteItem = selector.addExportItem(meter3, readingType1);
        when(task.getReadingTypeDataSelector()).thenReturn(Optional.of(selector));

        selector.selectData(dataExportOccurrence);

        assertThat(selector.getExportItems())
                .hasSize(3)
                .contains(existingItem)
                .contains(obsoleteItem);

        assertThat(obsoleteItem.isActive()).isFalse();

        assertThat(selector.getExportItems().stream()
                .filter(IReadingTypeDataExportItem::isActive)
                .count()).isEqualTo(2);
    }


    private class MyRefAny implements RefAny {

        private Object value;

        public MyRefAny() {
        }

        public MyRefAny(Object value) {
            this.value = value;
        }

        @Override
        public boolean isPresent() {
            return value != null;
        }

        @Override
        public Object get() {
            return value;
        }

        @Override
        public String getComponent() {
            return "DES";
        }

        @Override
        public String getTableName() {
            return "";
        }

        @Override
        public Object[] getPrimaryKey() {
            return new Object[0];
        }
    }
}