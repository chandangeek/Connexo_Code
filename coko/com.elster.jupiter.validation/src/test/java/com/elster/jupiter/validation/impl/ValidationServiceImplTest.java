package com.elster.jupiter.validation.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.metering.readings.BaseReading;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.pubsub.Publisher;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.validation.DataValidationOccurrence;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.DataValidationTask;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleProperties;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetResolver;
import com.elster.jupiter.validation.Validator;
import com.elster.jupiter.validation.ValidatorFactory;
import com.elster.jupiter.validation.ValidatorNotFoundException;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;

import javax.inject.Provider;
import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ValidationServiceImplTest {

    private static final String NAME = "name";
    private static final QualityCodeSystem SYSTEM = QualityCodeSystem.MDC;
    private static final long ID = 561651L;
    private ValidationServiceImpl validationService;

    @Mock
    private volatile MessageService messageService;
    @Mock
    private EventService eventService;
    @Mock(name = "TestFactory")
    private ValidatorFactory factory;
    @Mock(name = "TestValidator")
    private Validator validator1;
    @Mock(name = "YetAnotherValidator")
    private Validator validator2;
    @Mock
    private DataValidationTask iDataTask;
    @Mock
    private DataMapper<IValidationRuleSet> validationRuleSetFactory;
    @Mock
    private DataMapper<IValidationRuleSetVersion> validationRuleSetVersionFactory;
    @Mock
    private DataMapper<IValidationRule> validationRuleFactory;
    @Mock
    private DataMapper<MeterValidationImpl> meterValidationFactory;
    @Mock
    private DataMapper<DataValidationTaskImpl> dataValidationTaskFactory;
    @Mock
    private DataMapper<DataValidationTask> dataValidationTaskFactory2;
    @Mock
    private MeterActivation meterActivation;
    @Mock
    private TaskService taskService;
    @Mock
    private TaskOccurrence taskOccurrence;
    @Mock
    private Meter meter;
    @Mock
    private Channel channel1, channel2;
    @Mock
    private CimChannel cimChannel1, cimChannel2;
    @Mock
    private DataMapper<IChannelValidation> channelValidationFactory;
    @Mock
    private MeterValidationImpl meterValidation;
    @Mock
    private DataModel dataModel;
    @Mock
    private OrmService ormService;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Table table;
    private Clock clock = Clock.systemDefaultZone();
    @Mock
    private MeteringService meteringService;
    @Mock
    private MeteringGroupsService meteringGroupsService;
    @Mock
    private NlsService nlsService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private NlsMessageFormat nlsMessageFormat;
    @Mock
    private javax.validation.ValidatorFactory validatorFactory;
    @Mock
    private javax.validation.Validator javaxValidator;
    @Mock
    private QueryExecutor<IValidationRule> validationRuleQueryExecutor;
    @Mock
    private QueryExecutor<DataValidationOccurrence> taskOccurrenceQueryExecutor;
    @Mock
    private ValidationRuleSetResolver validationRuleSetResolver;
    @Mock
    private QueryExecutor<IMeterActivationValidation> queryExecutor;
    @Mock
    private QueryService queryService;
    @Mock
    private Query<IValidationRule> allValidationRuleQuery;
    @Mock
    private IMeterActivationValidation meterActivationValidation;
    @Mock
    private IChannelValidation channelValidation1, channelValidation2;
    @Mock
    private ReadingQualityRecord readingQuality1, readingQuality2, readingQuality3;
    @Mock
    private QueryExecutor<IChannelValidation> channelValidationQuery;
    @Mock
    private ValidatorCreator validatorCreator;
    @Mock
    private QueueTableSpec queueTableSpec;
    @Mock
    private DestinationSpec destinationSpec;
    @Mock
    private SubscriberSpec subscriberSpec;
    @Mock
    private RecurrentTask recurrentTask;
    @Mock
    private Query<DataValidationOccurrence> dataValidationOccurrenceQuery;
    @Mock
    private DataValidationOccurrence dataValidationOccurrence;
    @Mock
    private QueryExecutor<DataValidationTask> dataValidationTaskQueryExecutor;
    @Mock
    private Query<DataValidationTask> dataValidationTaskQuery;
    @Mock
    private UpgradeService upgradeService;

    @Before
    public void setUp() {
        when(ormService.newDataModel(anyString(), anyString())).thenReturn(dataModel);
        when(dataModel.isInstalled()).thenReturn(true);
        when(dataModel.addTable(anyString(), any())).thenReturn(table);
        when(dataModel.<DataValidationTask>mapper(any())).thenReturn(dataValidationTaskFactory2);
        when(dataModel.mapper(IValidationRuleSet.class)).thenReturn(validationRuleSetFactory);
        when(dataModel.mapper(IValidationRuleSetVersion.class)).thenReturn(validationRuleSetVersionFactory);
        when(dataModel.mapper(IValidationRule.class)).thenReturn(validationRuleFactory);
        when(dataModel.mapper(IChannelValidation.class)).thenReturn(channelValidationFactory);
        when(dataModel.mapper(MeterValidationImpl.class)).thenReturn(meterValidationFactory);
        when(dataModel.mapper(DataValidationTaskImpl.class)).thenReturn(dataValidationTaskFactory);
        when(dataModel.query(IChannelValidation.class, IMeterActivationValidation.class)).thenReturn(channelValidationQuery);
        when(channelValidationQuery.select(any())).thenReturn(Collections.emptyList());
        when(nlsService.getThesaurus(anyString(), any(Layer.class))).thenReturn(thesaurus);
        when(dataModel.query(IValidationRule.class, IValidationRuleSet.class, ValidationRuleProperties.class)).thenReturn(validationRuleQueryExecutor);
        when(dataModel.query(IValidationRule.class, IValidationRuleSetVersion.class, IValidationRuleSet.class)).thenReturn(validationRuleQueryExecutor);
        when(queryService.wrap(eq(validationRuleQueryExecutor))).thenReturn(allValidationRuleQuery);
        when(messageService.getQueueTableSpec(any(String.class))).thenReturn(Optional.of(queueTableSpec));
        when(queueTableSpec.createDestinationSpec(any(String.class), any(Integer.class))).thenReturn(destinationSpec);
        doNothing().when(destinationSpec).save();
        doNothing().when(destinationSpec).activate();
        when(destinationSpec.subscribe(any(String.class))).thenReturn(subscriberSpec);
        doReturn(Optional.of(cimChannel1)).when(channel1).getCimChannel(any());
        doReturn(Optional.of(cimChannel2)).when(channel2).getCimChannel(any());
        doReturn(channel1).when(cimChannel1).getChannel();
        doReturn(channel2).when(cimChannel2).getChannel();

        validationService = new ValidationServiceImpl(clock, messageService , eventService, taskService, meteringService, meteringGroupsService, ormService, queryService, nlsService, mock(UserService.class), mock(Publisher.class), upgradeService);
        validationService.addValidationRuleSetResolver(validationRuleSetResolver);

        DataValidationTaskImpl newDataValidationTask = new DataValidationTaskImpl(dataModel, taskService, validationService, thesaurus, () -> destinationSpec);
        newDataValidationTask.setRecurrentTask(recurrentTask);

        String validatorName = validator1.toString();
        String anotherName = validator2.toString();
        when(factory.available()).thenReturn(Arrays.asList(validatorName, anotherName));
        when(factory.create(validatorName, null)).thenReturn(validator1);
        when(factory.createTemplate(validatorName)).thenReturn(validator1);
        when(factory.create(anotherName, null)).thenReturn(validator2);
        when(factory.createTemplate(anotherName)).thenReturn(validator2);
        validationService.addResource(factory);

        Provider<ValidationRuleImpl> ruleProvider = () -> new ValidationRuleImpl(dataModel, validatorCreator, thesaurus, meteringService, eventService, () -> new ReadingTypeInValidationRuleImpl(meteringService));
        Provider<ValidationRuleSetVersionImpl> versionProvider = () -> new ValidationRuleSetVersionImpl(dataModel, eventService, ruleProvider);
        when(dataModel.getInstance(ValidationRuleSetImpl.class)).thenAnswer(invocationOnMock -> new ValidationRuleSetImpl(dataModel, eventService, versionProvider));
        when(dataModel.getInstance(ValidationRuleSetVersionImpl.class)).thenAnswer(invocationOnMock -> new ValidationRuleSetVersionImpl(dataModel, eventService, ruleProvider));
        when(dataModel.getInstance(DataValidationTaskImpl.class)).thenAnswer(invocationOnMock -> newDataValidationTask);
        when(dataModel.query(IMeterActivationValidation.class, IChannelValidation.class)).thenReturn(queryExecutor);
        when(queryExecutor.select(any())).thenReturn(Collections.emptyList());
        when(thesaurus.getFormat(any(MessageSeeds.class))).thenReturn(nlsMessageFormat);
        when(thesaurus.getFormat(any(TranslationKey.class))).thenReturn(nlsMessageFormat);
        when(dataModel.getValidatorFactory()).thenReturn(validatorFactory);
        when(dataModel.getValidatorFactory().getValidator()).thenReturn(javaxValidator);
    }

    private void setupValidationRuleSet(IChannelValidation channelValidation, Channel channel, boolean activeRules, ReadingQualityType... qualities) {
        IMeterActivationValidation meterActivationValidation = mock(IMeterActivationValidation.class);
        when(channelValidation.getMeterActivationValidation()).thenReturn(meterActivationValidation);
        ValidationRuleSet validationRuleSet = mock(ValidationRuleSet.class);
        when(validationRuleSet.getQualityCodeSystem()).thenReturn(SYSTEM);
        when(meterActivationValidation.getRuleSet()).thenReturn(validationRuleSet);
        when(channelValidation.getChannel()).thenReturn(channel);

        ReadingType readingType = channel.getMainReadingType();
        List<IValidationRule> validationRules = new ArrayList<>();
        for (ReadingQualityType quality : qualities) {
            IValidationRule rule = mock(IValidationRule.class);
            when(rule.getReadingQualityType()).thenReturn(quality);
            when(rule.getReadingTypes()).thenReturn(Collections.singleton(readingType));
            validationRules.add(rule);
        }
        if (!validationRules.isEmpty()) {
            when(allValidationRuleQuery.select(any(Condition.class))).thenReturn(validationRules);
            doReturn(validationRules).when(validationRuleSet).getRules();
        }
        when(channelValidation.hasActiveRules()).thenReturn(activeRules);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testGetAvailableValidators() {
        when(validator1.getSupportedApplications()).thenReturn(ImmutableSet.of("HERO", "BORING"));
        when(validator2.getSupportedApplications()).thenReturn(Collections.singleton("HERO"));
        assertThat(validationService.getAvailableValidators())
                .as("There must be 2 validators in sum")
                .containsExactly(validator1, validator2);
        assertThat(validationService.getAvailableValidators("HERO"))
                .as("Application HERO must be supported by both validators")
                .containsExactly(validator1, validator2);
        assertThat(validationService.getAvailableValidators("BADDIE"))
                .as("Application BADDIE is supported by some validator but must not be...")
                .isEmpty();
        assertThat(validationService.getAvailableValidators("BORING"))
                .as("Application BORING must be supported by only one validator1")
                .containsExactly(validator1);
    }

    @Test
    public void testGetImplementation() {
        ValidationServiceImpl.DefaultValidatorCreator creator = validationService.new DefaultValidatorCreator();
        assertThat(creator.getValidator(validator1.toString(), null))
                .isNotNull().isEqualTo(validator1);
        assertThat(creator.getValidator(validator2.toString(), null))
                .isNotNull().isEqualTo(validator2);
        assertThat(creator.getTemplateValidator(validator1.toString()))
                .isNotNull().isEqualTo(validator1);
        assertThat(creator.getTemplateValidator(validator2.toString()))
                .isNotNull().isEqualTo(validator2);
    }

    @Test(expected = ValidatorNotFoundException.class)
    public void testGetValidatorThrowsNotFoundExceptionIfNoFactoryProvidesImplementation() {
        validationService.removeResource(factory);
        validationService.new DefaultValidatorCreator().getValidator(validator1.toString(), null);
    }

    @Test
    public void testCreateRuleSet() {
        ValidationRuleSet validationRuleSet = validationService.createValidationRuleSet(NAME, SYSTEM);
        assertThat(validationRuleSet.getName()).isEqualTo(NAME);
        assertThat(validationRuleSet.getQualityCodeSystem()).isEqualTo(SYSTEM);
    }

    @Test
    public void testApplyRuleSetWithChannels() {
        when(meterActivation.getId()).thenReturn(ID);
        when(meterActivation.getMeter()).thenReturn(Optional.of(meter));
        when(meter.getId()).thenReturn(ID);
        when(meterActivation.getChannels()).thenReturn(Arrays.asList(channel1, channel2));
        when(channel1.getId()).thenReturn(1001L);
        ReadingType readingType = mock(ReadingType.class);
        doReturn(Arrays.asList(readingType)).when(channel1).getReadingTypes();
        when(channel2.getId()).thenReturn(1002L);
        when(channel1.getMeterActivation()).thenReturn(meterActivation);
        when(channel2.getMeterActivation()).thenReturn(meterActivation);
        when(meterValidationFactory.getOptional(ID)).thenReturn(Optional.of(meterValidation));
        when(meterValidation.getActivationStatus()).thenReturn(true);
        doAnswer((invocation) -> {
            Object meterActivationValidation = invocation.getArguments()[0];
            Field field = meterActivationValidation.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(meterActivationValidation, 1L);
            return null;
        }).when(dataModel).persist(any(IMeterActivationValidation.class));

        ValidationRuleSet validationRuleSet = mock(IValidationRuleSet.class);
        ValidationRule validationRule = mock(IValidationRule.class);
        doReturn(Collections.singleton(readingType)).when(validationRule).getReadingTypes();
        doReturn(Arrays.asList(validationRule)).when(validationRuleSet).getRules(anyList());
        when(validationRuleSetResolver.resolve(eq(meterActivation))).thenReturn(Arrays.asList(validationRuleSet));
        validationService.validate(meterActivation);

        ArgumentCaptor<IMeterActivationValidation> meterActivationValidationCapture = ArgumentCaptor.forClass(IMeterActivationValidation.class);
        verify(dataModel).persist(meterActivationValidationCapture.capture());

        final IMeterActivationValidation meterActivationValidationCaptureValue = meterActivationValidationCapture.getValue();
        final Set<IChannelValidation> channelValidations = meterActivationValidationCaptureValue.getChannelValidations();
        assertThat(channelValidations.stream().allMatch(input -> input.getMeterActivationValidation().equals(meterActivationValidationCaptureValue))).isTrue();
        assertThat(channelValidations.stream().map(IChannelValidation::getChannel).collect(Collectors.toSet())).contains(channel1);
    }

    @Test
    public void testApplyRuleSetWithChannelsAndOverwrite() {
        when(meterActivation.getId()).thenReturn(ID);
        when(meterActivation.getMeter()).thenReturn(Optional.of(meter));
        when(meter.getId()).thenReturn(ID);
        when(meterActivation.getChannels()).thenReturn(Arrays.asList(channel1, channel2));
        when(channel1.getId()).thenReturn(1001L);
        ReadingType readingType = mock(ReadingType.class);
        doReturn(Collections.singletonList(readingType)).when(channel1).getReadingTypes();
        doReturn(readingType).when(channel1).getMainReadingType();
        when(channel2.getId()).thenReturn(1002L);
        when(channel1.getMeterActivation()).thenReturn(meterActivation);
        when(channel2.getMeterActivation()).thenReturn(meterActivation);
        when(meterValidationFactory.getOptional(ID)).thenReturn(Optional.of(meterValidation));
        when(meterValidation.getActivationStatus()).thenReturn(false);
        when(queryExecutor.select(any(Condition.class))).thenReturn(Collections.singletonList(meterActivationValidation));
        when(meterActivationValidation.getMaxLastChecked()).thenReturn(Instant.ofEpochMilli(5000L));
        doReturn(Arrays.asList(channel1, channel2)).when(meterActivationValidation).getChannels();
        when(meterActivationValidation.getChannelValidation(channel1)).thenReturn(Optional.of(channelValidation1));
        when(meterActivationValidation.getChannelValidation(channel2)).thenReturn(Optional.of(channelValidation2));
        // TODO: what is the purpose of these stabbings? the test never uses them
//        when(channelValidation1.getLastChecked()).thenReturn(Instant.ofEpochMilli(-5000));
//        when(channelValidation2.getLastChecked()).thenReturn(Instant.ofEpochMilli(5000L));
//        when(channel1.findReadingQualities(anySet(), any(QualityCodeIndex.class), eq(Range.atLeast(Instant.EPOCH)), anyBoolean(), anyBoolean()))
//                .thenReturn(Arrays.asList(readingQuality1));
//        when(channel2.findReadingQualities(anySet(), any(QualityCodeIndex.class), eq(Range.atLeast(Instant.EPOCH)), anyBoolean(), anyBoolean()))
//                .thenReturn(Arrays.asList(readingQuality2, readingQuality3));
//        when(readingQuality1.getTypeCode()).thenReturn("2.6.1");
//        when(readingQuality2.getTypeCode()).thenReturn("1.0.0");
//        when(readingQuality3.getTypeCode()).thenReturn("2.6.2");
//        when(readingQuality1.getType()).thenReturn(new ReadingQualityType("2.6.1"));
//        when(readingQuality2.getType()).thenReturn(new ReadingQualityType("1.0.0"));
//        when(readingQuality3.getType()).thenReturn(new ReadingQualityType("2.6.2"));

        ValidationRuleSet validationRuleSet = mock(IValidationRuleSet.class);

        when(meterActivationValidation.getRuleSet()).thenReturn(validationRuleSet);

        ValidationRule validationRule = mock(IValidationRule.class);
        doReturn(Collections.singleton(readingType)).when(validationRule).getReadingTypes();
        doReturn(Collections.singletonList(validationRule)).when(validationRuleSet).getRules(anyList());
        when(validationRuleSetResolver.resolve(eq(meterActivation))).thenReturn(Collections.singletonList(validationRuleSet));
        Map<Channel, Range<Instant>> changeScope = ImmutableMap.of(channel1, Range.atLeast(Instant.EPOCH));
        validationService.validate(meterActivation, changeScope);
        verify(meterActivationValidation).moveLastCheckedBefore(changeScope);
    }

    @Test
    public void testManageValidationActivations() {
        when(meterActivation.getId()).thenReturn(ID);
        when(meterActivation.getMeter()).thenReturn(Optional.of(meter));
        when(meter.getId()).thenReturn(ID);
        when(meterActivation.getChannels()).thenReturn(Arrays.asList(channel1, channel2));
        when(channel1.getId()).thenReturn(1001L);
        when(channel2.getId()).thenReturn(1002L);
        when(channel1.getMeterActivation()).thenReturn(meterActivation);
        when(channel2.getMeterActivation()).thenReturn(meterActivation);
        when(meterValidationFactory.getOptional(ID)).thenReturn(Optional.of(meterValidation));
        when(meterValidation.getActivationStatus()).thenReturn(true);

        ValidationRuleSet validationRuleSet = validationService.createValidationRuleSet(NAME, SYSTEM);
        validationRuleSet.save();

        when(validationRuleSetResolver.resolve(eq(meterActivation))).thenReturn(Arrays.asList(validationRuleSet));
        validationService.validate(meterActivation);

        List<IMeterActivationValidation> meterActivationValidations = validationService.getUpdatedMeterActivationValidations(meterActivation);
        assertThat(meterActivationValidations).hasSize(1);
        assertThat(meterActivationValidations.get(0).getMeterActivation()).isEqualTo(meterActivation);
        assertThat(meterActivationValidations.get(0).getRuleSet()).isEqualTo(validationRuleSet);
        IMeterActivationValidation activationRuleSet1 = meterActivationValidations.get(0);

        ValidationRuleSet validationRuleSet2 = validationService.createValidationRuleSet(NAME, SYSTEM);
        validationRuleSet2.save();

        when(validationRuleSetResolver.resolve(eq(meterActivation))).thenReturn(Arrays.asList(validationRuleSet, validationRuleSet2));
        validationService.validate(meterActivation);
        meterActivationValidations = validationService.getUpdatedMeterActivationValidations(meterActivation);
        assertThat(meterActivationValidations).hasSize(2);
        assertThat(meterActivationValidations.get(0).getMeterActivation()).isEqualTo(meterActivation);
        assertThat(meterActivationValidations.get(1).getMeterActivation()).isEqualTo(meterActivation);
        assertThat(FluentIterable.from(meterActivationValidations).transform(IMeterActivationValidation::getRuleSet).toSet()).contains(validationRuleSet, validationRuleSet2);

        when(validationRuleSetResolver.resolve(eq(meterActivation))).thenReturn(Arrays.asList(validationRuleSet2));
        validationService.validate(meterActivation);
        meterActivationValidations = validationService.getUpdatedMeterActivationValidations(meterActivation);
        assertThat(meterActivationValidations).hasSize(1);
        assertThat(meterActivationValidations.get(0).getMeterActivation()).isEqualTo(meterActivation);
        assertThat(meterActivationValidations.get(0).getRuleSet()).isEqualTo(validationRuleSet2);
        assertThat(activationRuleSet1.isObsolete());
    }

    @Test
    public void testGetValidationStatusOnEmptyList() {
        when(channel1.getBulkQuantityReadingType()).thenReturn(Optional.empty());
        assertThat(validationService.getEvaluator().getValidationStatus(Collections.singleton(SYSTEM), channel1, Collections.<BaseReading>emptyList())).isEmpty();
    }

    @Test
    public void testGetValidationStatusOnNonValidated() {
        Instant readingDate = Instant.now();
        BaseReading reading = mock(BaseReading.class);
        when(reading.getTimeStamp()).thenReturn(readingDate);

        when(channel1.createReadingQuality(any(ReadingQualityType.class), any(ReadingType.class), any(Instant.class))).thenAnswer(invocationOnMock -> {
            ReadingQualityRecord record = mock(ReadingQualityRecord.class);
            ReadingQualityType readingQualityType = (ReadingQualityType) invocationOnMock.getArguments()[0];
            when(record.getType()).thenReturn(readingQualityType);
            when(record.getTypeCode()).thenReturn(readingQualityType.getCode());
            return record;
        });

        when(channelValidationFactory.find(eq("channel"), eq(channel1))).thenReturn(Collections.emptyList());
        when(channel1.getBulkQuantityReadingType()).thenReturn(Optional.empty());

        List<DataValidationStatus> validationStatus = validationService.getEvaluator().getValidationStatus(Collections.singleton(SYSTEM), channel1, Arrays.asList(reading));
        assertThat(validationStatus).hasSize(1);
        assertThat(validationStatus.get(0).getReadingTimestamp()).isEqualTo(readingDate);
        assertThat(validationStatus.get(0).completelyValidated()).isFalse();
        assertThat(validationStatus.get(0).getReadingQualities()).hasSize(0);

        IChannelValidation channelValidation = mock(IChannelValidation.class);
        when(channelValidationFactory.find(eq("channel"), eq(channel1))).thenReturn(Arrays.asList(channelValidation));
        when(channelValidation.getLastChecked()).thenReturn(Instant.ofEpochMilli(0));
        setupValidationRuleSet(channelValidation, channel1, true);

        validationStatus = validationService.getEvaluator().getValidationStatus(Collections.singleton(SYSTEM), channel1, Arrays.asList(reading));
        assertThat(validationStatus).hasSize(1);
        assertThat(validationStatus.get(0).getReadingTimestamp()).isEqualTo(readingDate);
        assertThat(validationStatus.get(0).completelyValidated()).isFalse();
        assertThat(validationStatus.get(0).getReadingQualities()).isEmpty();
    }

    @Test
    public void testGetValidationStatus() {
        Instant readingDate1 = ZonedDateTime.of(2012, 1, 2, 3, 0, 0, 0, ZoneId.systemDefault()).toInstant();
        Instant readingDate2 = ZonedDateTime.of(2012, 1, 2, 5, 0, 0, 0, ZoneId.systemDefault()).toInstant();
        BaseReading reading1 = mock(BaseReading.class);
        when(reading1.getTimeStamp()).thenReturn(readingDate1);
        BaseReading reading2 = mock(BaseReading.class);
        when(reading2.getTimeStamp()).thenReturn(readingDate2);


        IChannelValidation channelValidation = mock(IChannelValidation.class);
        when(channelValidationFactory.find(eq("channel"), eq(channel1))).thenReturn(Arrays.asList(channelValidation));
        when(channelValidation.getLastChecked()).thenReturn(readingDate1);
        when(cimChannel1.findReadingQualities(Collections.singleton(SYSTEM), null, Range.closed(readingDate1, readingDate2), false))
                .thenReturn(Collections.<ReadingQualityRecord>emptyList());
        ReadingQualityRecord readingQualityRecord = mock(ReadingQualityRecord.class);
        when(cimChannel1.createReadingQuality(any(ReadingQualityType.class), eq(readingDate1))).thenReturn(readingQualityRecord);

        setupValidationRuleSet(channelValidation, channel1, true);
        when(channel1.getBulkQuantityReadingType()).thenReturn(Optional.empty());
// !! remark that the order of the reading is by purpose not chronological !!
        List<DataValidationStatus> validationStatus = validationService.getEvaluator().getValidationStatus(Collections.singleton(SYSTEM),
                channel1, Arrays.asList(reading2, reading1));
        assertThat(validationStatus).hasSize(2);
        // reading2 has not be validated yet
        assertThat(validationStatus.get(0).getReadingTimestamp()).isEqualTo(readingDate2);
        assertThat(validationStatus.get(0).completelyValidated()).isFalse();
        assertThat(validationStatus.get(0).getReadingQualities()).isEmpty();
        // reading1 is ok
        assertThat(validationStatus.get(1).getReadingTimestamp()).isEqualTo(readingDate1);
        assertThat(validationStatus.get(1).completelyValidated()).isTrue();
        assertThat(validationStatus.get(1).getReadingQualities()).hasSize(1);
        assertThat(validationStatus.get(1).getReadingQualities().iterator().next().getTypeCode()).isEqualTo("2.0.1");
        assertThat(validationStatus.get(1).getOffendedValidationRule(readingQualityRecord)).isEmpty();
    }

    @Test
    public void testGetValidationStatusMultipleChannelValidations() {
        Instant readingDate1 = ZonedDateTime.of(2012, 1, 2, 3, 0, 0, 0, ZoneId.systemDefault()).toInstant();
        Instant readingDate2 = ZonedDateTime.of(2012, 1, 2, 5, 0, 0, 0, ZoneId.systemDefault()).toInstant();
        BaseReading reading1 = mock(BaseReading.class);
        when(reading1.getTimeStamp()).thenReturn(readingDate1);
        BaseReading reading2 = mock(BaseReading.class);
        when(reading2.getTimeStamp()).thenReturn(readingDate2);
        when(channel1.getBulkQuantityReadingType()).thenReturn(Optional.empty());

        IChannelValidation channelValidation1 = mock(IChannelValidation.class);
        when(channelValidation1.getLastChecked()).thenReturn(readingDate1);
        IChannelValidation channelValidation2 = mock(IChannelValidation.class);
        when(channelValidation2.getLastChecked()).thenReturn(readingDate2);
        when(channelValidationFactory.find(eq("channel"), eq(channel1))).thenReturn(Arrays.asList(channelValidation1, channelValidation2));
        ReadingQualityRecord readingQualityRecord = mock(ReadingQualityRecord.class);
        when(cimChannel1.createReadingQuality(any(ReadingQualityType.class), eq(readingDate1))).thenReturn(readingQualityRecord);
        ReadingType readingType = mock(ReadingType.class);
        doReturn(Arrays.asList(readingType)).when(channel1).getReadingTypes();
        doReturn(readingType).when(channel1).getMainReadingType();
        setupValidationRuleSet(channelValidation1, channel1, true);
        setupValidationRuleSet(channelValidation2, channel1, true);

// !! remark that the order of the reading is by purpose not chronological !!
        List<DataValidationStatus> validationStatus = validationService.getEvaluator().getValidationStatus(Collections.singleton(SYSTEM), channel1, Arrays.asList(reading2, reading1));
        assertThat(validationStatus).hasSize(2);
        // reading2 has not be validated yet
        assertThat(validationStatus.get(0).getReadingTimestamp()).isEqualTo(readingDate2);
        assertThat(validationStatus.get(0).completelyValidated()).isFalse();
        assertThat(validationStatus.get(0).getReadingQualities()).isEmpty();
        // reading1 is ok
        assertThat(validationStatus.get(1).getReadingTimestamp()).isEqualTo(readingDate1);
        assertThat(validationStatus.get(1).completelyValidated()).isTrue();
        assertThat(validationStatus.get(1).getReadingQualities()).hasSize(1);
        assertThat(validationStatus.get(1).getReadingQualities().iterator().next().getTypeCode()).isEqualTo("2.0.1");
        assertThat(validationStatus.get(1).getOffendedValidationRule(readingQualityRecord)).isEmpty();
    }

    @Test
    public void testGetValidationStatusNewChannelValidations() {
        Instant readingDate1 = ZonedDateTime.of(2012, 1, 2, 3, 0, 0, 0, ZoneId.systemDefault()).toInstant();
        Instant readingDate2 = ZonedDateTime.of(2012, 1, 2, 5, 0, 0, 0, ZoneId.systemDefault()).toInstant();
        BaseReading reading1 = mock(BaseReading.class);
        when(reading1.getTimeStamp()).thenReturn(readingDate1);
        BaseReading reading2 = mock(BaseReading.class);
        when(reading2.getTimeStamp()).thenReturn(readingDate2);
        when(channel1.getBulkQuantityReadingType()).thenReturn(Optional.empty());

        IChannelValidation channelValidation1 = mock(IChannelValidation.class);
        when(channelValidation1.getLastChecked()).thenReturn(readingDate1);
        IChannelValidation channelValidation2 = mock(IChannelValidation.class);
        when(channelValidation2.getLastChecked()).thenReturn(null);
        when(channelValidationFactory.find(eq("channel"), eq(channel1))).thenReturn(Arrays.asList(channelValidation1, channelValidation2));
        ReadingQualityRecord readingQuality = mock(ReadingQualityRecord.class);
        when(readingQuality.getReadingTimestamp()).thenReturn(readingDate1);
        ReadingQualityType readingQualityType = new ReadingQualityType("2.6.32131");
        when(readingQuality.getType()).thenReturn(readingQualityType);
        when(readingQuality.getTypeCode()).thenReturn("2.6.32131");
        when(cimChannel1.findReadingQualities(Collections.singleton(SYSTEM), null, Range.closed(readingDate1, readingDate2), true))
                .thenReturn(Collections.singletonList(readingQuality));
        when(cimChannel1.createReadingQuality(any(ReadingQualityType.class), eq(readingDate1))).thenReturn(mock(ReadingQualityRecord.class));
        setupValidationRuleSet(channelValidation1, channel1, true, readingQualityType);
        setupValidationRuleSet(channelValidation2, channel1, true);

// !! remark that the order of the reading is by purpose not chronological !!
        List<DataValidationStatus> validationStatus = validationService.getEvaluator().getValidationStatus(Collections.singleton(SYSTEM), channel1, Arrays.asList(reading2, reading1));
        assertThat(validationStatus).hasSize(2);
        // reading2 has not be validated yet
        assertThat(validationStatus.get(0).getReadingTimestamp()).isEqualTo(readingDate2);
        assertThat(validationStatus.get(0).completelyValidated()).isFalse();
        assertThat(validationStatus.get(0).getReadingQualities()).isEmpty();
        // reading1 is not completely validated, but has already suspects
        assertThat(validationStatus.get(1).getReadingTimestamp()).isEqualTo(readingDate1);
        assertThat(validationStatus.get(1).completelyValidated()).isFalse();
        assertThat((Collection<ReadingQuality>) validationStatus.get(1).getReadingQualities()).containsOnly(readingQuality);
        assertThat(validationStatus.get(1).getOffendedValidationRule(readingQuality)).isNotEmpty();
    }

    @Test
    public void testGetValidationStatusWithSuspects() {
        Instant readingDate1 = ZonedDateTime.of(2012, 1, 2, 3, 0, 0, 0, ZoneId.systemDefault()).toInstant();
        Instant readingDate2 = ZonedDateTime.of(2012, 1, 2, 5, 0, 0, 0, ZoneId.systemDefault()).toInstant();
        BaseReading reading1 = mock(BaseReading.class);
        when(reading1.getTimeStamp()).thenReturn(readingDate1);
        BaseReading reading2 = mock(BaseReading.class);
        when(reading2.getTimeStamp()).thenReturn(readingDate2);

        IChannelValidation channelValidation1 = mock(IChannelValidation.class);
        when(channelValidation1.getLastChecked()).thenReturn(readingDate2);
        when(channelValidationFactory.find(eq("channel"), eq(channel1))).thenReturn(Arrays.asList(channelValidation1));
        ReadingQualityRecord readingQuality1 = mock(ReadingQualityRecord.class);
        when(readingQuality1.getReadingTimestamp()).thenReturn(readingDate1);
        ReadingQualityType readingQualityType1 = new ReadingQualityType("2.6.5164");
        when(readingQuality1.getType()).thenReturn(readingQualityType1);
        when(readingQuality1.getTypeCode()).thenReturn("2.6.5164");
        ReadingQualityRecord readingQuality2 = mock(ReadingQualityRecord.class);
        when(readingQuality2.getReadingTimestamp()).thenReturn(readingDate1);
        ReadingQualityType readingQualityType2 = new ReadingQualityType("2.6.9856");
        when(readingQuality2.getType()).thenReturn(readingQualityType2);
        when(readingQuality2.getTypeCode()).thenReturn("2.6.9856");
        when(readingQuality2.isSuspect()).thenReturn(true);
        when(cimChannel1.findReadingQualities(Collections.singleton(SYSTEM), null, Range.closed(readingDate1, readingDate2), true))
                .thenReturn(Arrays.asList(readingQuality1, readingQuality2));
        ReadingQualityRecord readingDate2ReadingQuality = mock(ReadingQualityRecord.class);
        when(cimChannel1.createReadingQuality(any(ReadingQualityType.class), any(Instant.class))).thenReturn(readingDate2ReadingQuality);
        when(channel1.getMainReadingType()).thenReturn(mock(ReadingType.class));
        when(channel1.getBulkQuantityReadingType()).thenReturn(Optional.empty());
        setupValidationRuleSet(channelValidation1, channel1, true, readingQualityType1, readingQualityType2);

// !! remark that the order of the reading is by purpose not chronological !!
        List<DataValidationStatus> validationStatus = validationService.getEvaluator()
                .getValidationStatus(Collections.singleton(SYSTEM), channel1, Arrays.asList(reading2, reading1));
        assertThat(validationStatus).hasSize(2);
        // reading2 is OK
        assertThat(validationStatus.get(0).getReadingTimestamp()).isEqualTo(readingDate2);
        assertThat(validationStatus.get(0).completelyValidated()).isTrue();
        assertThat(validationStatus.get(0).getReadingQualities()).hasSize(1);
        assertThat(validationStatus.get(0).getReadingQualities().iterator().next().getTypeCode()).isEqualTo("2.0.1");
        // reading1 has suspects
        assertThat(validationStatus.get(1).getReadingTimestamp()).isEqualTo(readingDate1);
        assertThat(validationStatus.get(1).completelyValidated()).isTrue();
        assertThat((Collection<ReadingQuality>) validationStatus.get(1).getReadingQualities()).containsOnly(readingQuality1, readingQuality2);
        assertThat(validationStatus.get(1).getOffendedValidationRule(readingQuality1)).isNotEmpty();
        assertThat(validationStatus.get(1).getOffendedValidationRule(readingQuality2)).isNotEmpty();
    }

    @Test
    public void testMeterValidationActivation() {
        Meter meter = mock(Meter.class);
        when(meter.getId()).thenReturn(ID);
        doReturn(Optional.of(meterActivation)).when(meter).getCurrentMeterActivation();
        when(meterValidationFactory.getOptional(ID)).thenReturn(Optional.<MeterValidationImpl>empty());
        when(validationRuleSetResolver.resolve(eq(meterActivation))).thenReturn(Collections.<ValidationRuleSet>emptyList());


        validationService.activateValidation(meter);
        validationService.enableValidationOnStorage(meter);

        //Check that a MeterValidation object is made
        verify(dataModel).persist(any(MeterValidationImpl.class));

        // verify that the MeterActivationValidations are managed for the current MeterActivation
        verify(meter).getCurrentMeterActivation();
        verify(validationRuleSetResolver).resolve(meterActivation);
    }

    @Test
    public void testDeactivateMeterValidation() {
        Meter meter = mock(Meter.class);
        when(meter.getId()).thenReturn(ID);
        when(meterValidationFactory.getOptional(ID)).thenReturn(Optional.of(meterValidation));
        when(meterValidation.getActivationStatus()).thenReturn(true);
        when(meterValidation.getValidateOnStorage()).thenReturn(true);

        validationService.deactivateValidation(meter);

        //Check that a MeterValidation object is made
        verify(meterValidation).setActivationStatus(false);
        verify(meterValidation).save();
    }

    @Test
    public void testEnableValidationOnStorage() {
        Meter meter = mock(Meter.class);
        when(meter.getId()).thenReturn(ID);
        when(meterValidationFactory.getOptional(ID)).thenReturn(Optional.of(meterValidation));
        when(meterValidation.getActivationStatus()).thenReturn(true);
        when(meterValidation.getValidateOnStorage()).thenReturn(false);

        validationService.enableValidationOnStorage(meter);

        //Check that a MeterValidation object is made
        verify(meterValidation).setValidateOnStorage(true);
        verify(meterValidation).save();
    }

    @Test
    public void testDisableValidationOnStorage() {
        Meter meter = mock(Meter.class);
        when(meter.getId()).thenReturn(ID);
        when(meterValidationFactory.getOptional(ID)).thenReturn(Optional.of(meterValidation));
        when(meterValidation.getActivationStatus()).thenReturn(true);
        when(meterValidation.getValidateOnStorage()).thenReturn(true);

        validationService.disableValidationOnStorage(meter);

        //Check that a MeterValidation object is made
        verify(meterValidation).setValidateOnStorage(false);
        verify(meterValidation).save();
    }

    @Test
    public void testValidationOnStorageStatusOnValidationDeactivation() {
        Meter meter = mock(Meter.class);
        when(meter.getId()).thenReturn(ID);
        when(meterValidationFactory.getOptional(ID)).thenReturn(Optional.of(meterValidation));
        when(meterValidation.getActivationStatus()).thenReturn(true);
        when(meterValidation.getValidateOnStorage()).thenReturn(false);

        validationService.deactivateValidation(meter);

        //Check that a MeterValidation object is made
        verify(meterValidation).setActivationStatus(false);
        verify(meterValidation).save();
    }

    @Test
    public void testDeactivateMeterValidationNoObject() {
        Meter meter = mock(Meter.class);
        when(meter.getId()).thenReturn(ID);
        when(meterValidationFactory.getOptional(ID)).thenReturn(Optional.<MeterValidationImpl>empty());
        validationService.deactivateValidation(meter);

        verify(meterValidation, never()).setActivationStatus(anyBoolean());
        verify(dataModel, never()).getInstance(MeterValidationImpl.class);
    }

    @Test
    public void testFindDataValidationTaskById() {
        when(dataValidationTaskFactory2.getOptional(ID)).thenReturn(Optional.of(iDataTask));
        assertThat(validationService.findValidationTask(ID).get()).isEqualTo(iDataTask);
    }

    @Test
    public void testFindDataValidationTaskByIdNotFound() {
        when(dataValidationTaskFactory2.getOptional(ID)).thenReturn(Optional.<DataValidationTask>empty());
        assertThat(validationService.findValidationTask(ID).isPresent()).isFalse();
    }

    @Test
    public void testGetDataValidationTaskForRecurrentTask() {
        when(dataModel.query(DataValidationOccurrence.class, DataValidationTask.class)).thenReturn(taskOccurrenceQueryExecutor);
        when(taskOccurrenceQueryExecutor.select(any())).thenReturn(new ArrayList<DataValidationOccurrence>());
        assertThat(validationService.findDataValidationOccurrence(taskOccurrence).isPresent());
    }

    @Test
    public void testFindValidationTasks() {
        when(dataModel.query(DataValidationTask.class)).thenReturn(dataValidationTaskQueryExecutor);
        when(queryService.wrap(eq(dataValidationTaskQueryExecutor))).thenReturn(dataValidationTaskQuery);
        when(dataValidationTaskQueryExecutor.select(any())).thenReturn(new ArrayList<DataValidationTask>());
        assertThat(validationService.findValidationTasks());
    }

    @Test
    public void testCreateValidationOccurrence() {
        when(taskOccurrence.getRecurrentTask()).thenReturn(recurrentTask);
        when(dataValidationTaskFactory2.getUnique("recurrentTask", recurrentTask)).thenReturn(Optional.of(iDataTask));
        DataValidationOccurrenceImpl dataValidationOcc = new DataValidationOccurrenceImpl(dataModel, clock);
        when(dataModel.getInstance(DataValidationOccurrenceImpl.class)).thenReturn(dataValidationOcc);
        when(taskOccurrence.getTriggerTime()).thenReturn(ZonedDateTime.of(2013, 9, 10, 14, 47, 24, 0, ZoneId.of("Europe/Paris")).toInstant());
        assertThat(validationService.createValidationOccurrence(taskOccurrence)).isEqualTo(dataValidationOcc);
        assertThat(dataValidationOcc.getTask()).isEqualTo(iDataTask);
        assertThat(dataValidationOcc.getTaskOccurrence()).isEqualTo(taskOccurrence);
    }

    @Test
    public void testGetDataValidationStatusBulkChannel() {
        Channel channel = mock(Channel.class);
        CimChannel mainChannel = mock(CimChannel.class);
        CimChannel bulkChannel = mock(CimChannel.class);
        when(mainChannel.getChannel()).thenReturn(channel);
        when(bulkChannel.getChannel()).thenReturn(channel);
        Instant start = ZonedDateTime.of(2015, 9, 1, 0, 0, 0, 0, ZoneOffset.UTC).toInstant();
        Instant end = ZonedDateTime.of(2015, 9, 5, 0, 0, 0, 0, ZoneOffset.UTC).toInstant();
        Range<Instant> range = Range.openClosed(start, end);

        IChannelValidation channelValidation = mock(IChannelValidation.class);
        when(channelValidationFactory.find(eq("channel"), eq(channel))).thenReturn(Arrays.asList(channelValidation));
        when(channelValidation.getLastChecked()).thenReturn(end);
        Answer<ReadingQualityRecord> answer = invocationOnMock -> {
            ReadingQualityType rqType = (ReadingQualityType) invocationOnMock.getArguments()[0];
            Instant instant = (Instant) invocationOnMock.getArguments()[1];
            return mockReadingQualityRecord(rqType.getCode(), instant);
        };
        doAnswer(answer).when(mainChannel).createReadingQuality(any(), any(Instant.class));
        doAnswer(answer).when(bulkChannel).createReadingQuality(any(), any(Instant.class));

        setupValidationRuleSet(channelValidation, channel, true);

        List<ReadingQualityRecord> bulkRQs = Arrays.asList(
                mockReadingQualityRecord("2.5.258", start.plus(2, ChronoUnit.DAYS)), mockReadingQualityRecord("2.5.259", start.plus(2, ChronoUnit.DAYS)),//missing
                mockReadingQualityRecord("2.5.258", start.plus(3, ChronoUnit.DAYS)), mockReadingQualityRecord("2.5.259", start.plus(3, ChronoUnit.DAYS)));//missing
        when(bulkChannel.findReadingQualities(Collections.singleton(SYSTEM), null, range, true)).thenReturn(bulkRQs);
        List<ReadingQualityRecord> mainRQs = Arrays.asList(
                mockReadingQualityRecord("2.5.258", start.plus(4, ChronoUnit.DAYS)), mockReadingQualityRecord("2.6.1003", start.plus(4, ChronoUnit.DAYS)));//rule violation
        when(mainChannel.findReadingQualities(Collections.singleton(SYSTEM), null, range, true)).thenReturn(mainRQs);
        List<DataValidationStatus> validationStatus = validationService.getEvaluator().getValidationStatus(
                Collections.singleton(SYSTEM), ImmutableList.of(mainChannel, bulkChannel), Collections.emptyList(), range);
        assertThat(validationStatus).hasSize(3);
        Map<Instant, DataValidationStatus> map = validationStatus.stream()
                .collect(Collectors.toMap(DataValidationStatus::getReadingTimestamp, java.util.function.Function.<DataValidationStatus>identity()));
        assertThat(map.get(start.plus(2, ChronoUnit.DAYS)).getValidationResult()).isEqualTo(ValidationResult.VALID);
        assertThat(map.get(start.plus(3, ChronoUnit.DAYS)).getValidationResult()).isEqualTo(ValidationResult.VALID);
        assertThat(map.get(start.plus(4, ChronoUnit.DAYS)).getValidationResult()).isEqualTo(ValidationResult.SUSPECT);
        assertThat(map.get(start.plus(2, ChronoUnit.DAYS)).getBulkValidationResult()).isEqualTo(ValidationResult.SUSPECT);
        assertThat(map.get(start.plus(3, ChronoUnit.DAYS)).getBulkValidationResult()).isEqualTo(ValidationResult.SUSPECT);
        assertThat(map.get(start.plus(4, ChronoUnit.DAYS)).getBulkValidationResult()).isEqualTo(ValidationResult.VALID);
    }

    private ReadingQualityRecord mockReadingQualityRecord(String code, Instant readingTimeStamp) {
        ReadingQualityRecord readingQualityRecord = mock(ReadingQualityRecord.class);
        ReadingQualityType readingQualityType = new ReadingQualityType(code);
        when(readingQualityRecord.getType()).thenReturn(readingQualityType);
        when(readingQualityRecord.getReadingTimestamp()).thenReturn(readingTimeStamp);
        if (code.equals("2.5.258") || code.equals("2.5.259")) {
            when(readingQualityRecord.isSuspect()).thenReturn(true);
        }
        return readingQualityRecord;
    }
}
