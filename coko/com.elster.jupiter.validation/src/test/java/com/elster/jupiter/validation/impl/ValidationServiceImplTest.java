/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validation.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.kpi.KpiService;
import com.elster.jupiter.messaging.DestinationSpec;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.messaging.QueueTableSpec;
import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.CimChannel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ReadingQualityWithTypeFetcher;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
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
import com.elster.jupiter.search.SearchService;
import com.elster.jupiter.tasks.RecurrentTask;
import com.elster.jupiter.tasks.TaskOccurrence;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.validation.DataValidationOccurrence;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.DataValidationTask;
import com.elster.jupiter.validation.ValidationContext;
import com.elster.jupiter.validation.ValidationContextImpl;
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
import org.osgi.framework.BundleContext;

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
import java.util.EnumSet;
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
import static org.mockito.Mockito.times;
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
    private ChannelsContainer channelsContainer;
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
    private DataMapper<ChannelValidation> channelValidationFactory;
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
    private QueryExecutor<ChannelsContainerValidation> queryExecutor;
    @Mock
    private QueryService queryService;
    @Mock
    private Query<IValidationRule> allValidationRuleQuery;
    @Mock
    private ChannelsContainerValidation channelsContainerValidation;
    @Mock
    private ChannelValidation channelValidation1, channelValidation2;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ReadingQualityWithTypeFetcher fetcher;
    @Mock
    private ReadingQualityRecord readingQuality1, readingQuality2, readingQuality3;
    @Mock
    private QueryExecutor<ChannelValidation> channelValidationQuery;
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
    private KpiService kpiService;
    @Mock
    private BundleContext bundleContext;
    @Mock
    private UpgradeService upgradeService;
    @Mock
    private MetrologyConfigurationService metrologyConfigurationService;
    @Mock
    private SearchService searchService;

    @Before
    public void setUp() {
        when(ormService.newDataModel(anyString(), anyString())).thenReturn(dataModel);
        when(dataModel.isInstalled()).thenReturn(true);
        when(dataModel.addTable(anyString(), any())).thenReturn(table);
        when(dataModel.<DataValidationTask>mapper(any())).thenReturn(dataValidationTaskFactory2);
        when(dataModel.mapper(IValidationRuleSet.class)).thenReturn(validationRuleSetFactory);
        when(dataModel.mapper(IValidationRuleSetVersion.class)).thenReturn(validationRuleSetVersionFactory);
        when(dataModel.mapper(IValidationRule.class)).thenReturn(validationRuleFactory);
        when(dataModel.mapper(ChannelValidation.class)).thenReturn(channelValidationFactory);
        when(dataModel.mapper(MeterValidationImpl.class)).thenReturn(meterValidationFactory);
        when(dataModel.mapper(DataValidationTaskImpl.class)).thenReturn(dataValidationTaskFactory);
        when(dataModel.query(ChannelValidation.class, ChannelsContainerValidation.class)).thenReturn(channelValidationQuery);
        when(channelValidationQuery.select(any())).thenReturn(Collections.emptyList());
        when(nlsService.getThesaurus(anyString(), any(Layer.class))).thenReturn(thesaurus);
        when(dataModel.query(IValidationRule.class, IValidationRuleSet.class, ValidationRuleProperties.class)).thenReturn(validationRuleQueryExecutor);
        when(dataModel.query(IValidationRule.class, IValidationRuleSetVersion.class, IValidationRuleSet.class)).thenReturn(validationRuleQueryExecutor);
        when(queryService.wrap(eq(validationRuleQueryExecutor))).thenReturn(allValidationRuleQuery);
        when(messageService.getQueueTableSpec(any(String.class))).thenReturn(Optional.of(queueTableSpec));
        when(queueTableSpec.createDestinationSpec(any(String.class), any(Integer.class))).thenReturn(destinationSpec);
        doNothing().when(destinationSpec).save();
        doNothing().when(destinationSpec).activate();
        when(destinationSpec.subscribe(any(TranslationKey.class), anyString(), any(Layer.class))).thenReturn(subscriberSpec);
        doReturn(Optional.of(cimChannel1)).when(channel1).getCimChannel(any());
        doReturn(Optional.of(cimChannel2)).when(channel2).getCimChannel(any());
        doReturn(channel1).when(cimChannel1).getChannel();
        doReturn(channel2).when(cimChannel2).getChannel();
        when(meterActivation.getChannelsContainer()).thenReturn(channelsContainer);
        doReturn(fetcher).when(cimChannel1).findReadingQualities();

        validationService = new ValidationServiceImpl(bundleContext, clock, messageService, eventService, taskService, meteringService, meteringGroupsService, ormService, queryService, nlsService, mock(UserService.class), mock(Publisher.class), upgradeService, kpiService, metrologyConfigurationService, searchService);
        validationService.addValidationRuleSetResolver(validationRuleSetResolver);

        DataValidationTaskImpl newDataValidationTask = new DataValidationTaskImpl(dataModel, taskService, thesaurus, () -> destinationSpec);
        newDataValidationTask.setRecurrentTask(recurrentTask);

        String validatorName = validator1.toString();
        String anotherName = validator2.toString();
        when(factory.available()).thenReturn(Arrays.asList(validatorName, anotherName));
        when(factory.create(validatorName, null)).thenReturn(validator1);
        when(factory.createTemplate(validatorName)).thenReturn(validator1);
        when(factory.create(anotherName, null)).thenReturn(validator2);
        when(factory.createTemplate(anotherName)).thenReturn(validator2);
        validationService.addResource(factory);

        Provider<ValidationRuleImpl> ruleProvider = () -> new ValidationRuleImpl(dataModel, validatorCreator, thesaurus, meteringService, eventService, () -> new ReadingTypeInValidationRuleImpl(meteringService), clock);
        Provider<ValidationRuleSetVersionImpl> versionProvider = () -> new ValidationRuleSetVersionImpl(dataModel, eventService, ruleProvider, clock);
        when(dataModel.getInstance(ValidationRuleSetImpl.class)).thenAnswer(invocationOnMock -> new ValidationRuleSetImpl(dataModel, eventService, versionProvider, clock));
        when(dataModel.getInstance(ValidationRuleSetVersionImpl.class)).thenAnswer(invocationOnMock -> new ValidationRuleSetVersionImpl(dataModel, eventService, ruleProvider, clock));
        when(dataModel.getInstance(DataValidationTaskImpl.class)).thenAnswer(invocationOnMock -> newDataValidationTask);
        when(dataModel.getInstance(ChannelsContainerValidationImpl.class)).thenReturn(new ChannelsContainerValidationImpl(dataModel, clock));
        when(dataModel.query(ChannelsContainerValidation.class, ChannelValidation.class)).thenReturn(queryExecutor);
        when(queryExecutor.select(any())).thenReturn(Collections.emptyList());
        when(thesaurus.getFormat(any(MessageSeeds.class))).thenReturn(nlsMessageFormat);
        when(thesaurus.getFormat(any(TranslationKey.class))).thenReturn(nlsMessageFormat);
        when(dataModel.getValidatorFactory()).thenReturn(validatorFactory);
        when(dataModel.getValidatorFactory().getValidator()).thenReturn(javaxValidator);
    }

    private void setupValidationRuleSet(ChannelValidation channelValidation, Channel channel, boolean activeRules, ReadingQualityType... qualities) {
        ChannelsContainerValidation channelsContainerValidation = mock(ChannelsContainerValidation.class);
        when(channelValidation.getChannelsContainerValidation()).thenReturn(channelsContainerValidation);
        ValidationRuleSet validationRuleSet = mock(ValidationRuleSet.class);
        when(validationRuleSet.getQualityCodeSystem()).thenReturn(SYSTEM);
        when(channelsContainerValidation.getRuleSet()).thenReturn(validationRuleSet);
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
        when(validator1.getSupportedQualityCodeSystems()).thenReturn(ImmutableSet.of(QualityCodeSystem.ENDDEVICE, QualityCodeSystem.EXTERNAL));
        when(validator2.getSupportedQualityCodeSystems()).thenReturn(Collections.singleton(QualityCodeSystem.ENDDEVICE));
        assertThat(validationService.getAvailableValidators())
                .as("There must be 2 validators in sum")
                .containsExactly(validator1, validator2);
        assertThat(validationService.getAvailableValidators(QualityCodeSystem.ENDDEVICE))
                .as("QualityCodeSystem ENDDEVICE must be supported by both validators")
                .containsExactly(validator1, validator2);
        assertThat(validationService.getAvailableValidators(QualityCodeSystem.OTHER))
                .as("QualityCodeSystem OTHER is supported by some validator but must not be...")
                .isEmpty();
        assertThat(validationService.getAvailableValidators(QualityCodeSystem.EXTERNAL))
                .as("QualityCodeSystem EXTERNAL must be supported by only one validator1")
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
        when(channelsContainer.getId()).thenReturn(ID);
        when(channelsContainer.getMeter()).thenReturn(Optional.of(meter));
        when(meter.getId()).thenReturn(ID);
        when(channelsContainer.getChannels()).thenReturn(Arrays.asList(channel1, channel2));
        when(channel1.getId()).thenReturn(1001L);
        ReadingType readingType = mock(ReadingType.class);
        doReturn(Arrays.asList(readingType)).when(channel1).getReadingTypes();
        when(channel2.getId()).thenReturn(1002L);
        when(channel1.getChannelsContainer()).thenReturn(channelsContainer);
        when(channel2.getChannelsContainer()).thenReturn(channelsContainer);
        when(meterValidationFactory.getOptional(ID)).thenReturn(Optional.of(meterValidation));
        when(meterValidation.getActivationStatus()).thenReturn(true);
        doAnswer((invocation) -> {
            Object channelsContainerValidation = invocation.getArguments()[0];
            Field field = channelsContainerValidation.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(channelsContainerValidation, 1L);
            return null;
        }).when(dataModel).persist(any(ChannelsContainerValidation.class));

        ValidationRuleSet validationRuleSet = mock(IValidationRuleSet.class);
        when(validationRuleSet.getQualityCodeSystem()).thenReturn(QualityCodeSystem.OTHER);
        ValidationRule validationRule = mock(IValidationRule.class);
        doReturn(Collections.singleton(readingType)).when(validationRule).getReadingTypes();
        doReturn(Arrays.asList(validationRule)).when(validationRuleSet).getRules(anyList());
        when(validationRuleSetResolver.resolve(any(ValidationContext.class))).thenReturn(Arrays.asList(validationRuleSet));
        validationService.validate(Collections.emptySet(), channelsContainer);

        ArgumentCaptor<ChannelsContainerValidation> channelsContainerValidationArgumentCaptor = ArgumentCaptor.forClass(ChannelsContainerValidation.class);
        verify(dataModel).persist(channelsContainerValidationArgumentCaptor.capture());

        final ChannelsContainerValidation channelsContainerValidationCaptureValue = channelsContainerValidationArgumentCaptor.getValue();
        final Set<ChannelValidation> channelValidations = channelsContainerValidationCaptureValue.getChannelValidations();
        assertThat(channelValidations.stream().allMatch(input -> input.getChannelsContainerValidation().equals(channelsContainerValidationCaptureValue))).isTrue();
        assertThat(channelValidations.stream().map(ChannelValidation::getChannel).collect(Collectors.toSet())).contains(channel1);
    }

    @Test
    public void testApplyRuleSetWithChannelsAndOverwrite() {
        when(channelsContainer.getId()).thenReturn(ID);
        when(channelsContainer.getMeter()).thenReturn(Optional.of(meter));
        when(meter.getId()).thenReturn(ID);
        when(channelsContainer.getChannels()).thenReturn(Arrays.asList(channel1, channel2));
        when(channel1.getId()).thenReturn(1001L);
        ReadingType readingType = mock(ReadingType.class);
        doReturn(Collections.singletonList(readingType)).when(channel1).getReadingTypes();
        doReturn(readingType).when(channel1).getMainReadingType();
        when(channel2.getId()).thenReturn(1002L);
        when(channel1.getChannelsContainer()).thenReturn(channelsContainer);
        when(channel2.getChannelsContainer()).thenReturn(channelsContainer);
        when(meterValidationFactory.getOptional(ID)).thenReturn(Optional.of(meterValidation));
        when(meterValidation.getActivationStatus()).thenReturn(false);
        when(queryExecutor.select(any(Condition.class))).thenReturn(Arrays.asList(channelsContainerValidation));
        when(channelsContainerValidation.getMaxLastChecked()).thenReturn(Instant.ofEpochMilli(5000L));
        when(channelsContainerValidation.getChannelValidations()).thenReturn(ImmutableSet.of(channelValidation1, channelValidation2));
        when(channelsContainerValidation.getChannelsContainer()).thenReturn(channelsContainer);
        when(channelsContainerValidation.getChannelValidation(channel1)).thenReturn(Optional.of(channelValidation1));
        when(channelsContainerValidation.getChannelValidation(channel2)).thenReturn(Optional.of(channelValidation2));
        when(channelValidation1.getChannel()).thenReturn(channel1);
        when(channelValidation2.getChannel()).thenReturn(channel2);

        ValidationRuleSet validationRuleSet = mock(IValidationRuleSet.class);

        when(channelsContainerValidation.getRuleSet()).thenReturn(validationRuleSet);

        ValidationRule validationRule = mock(IValidationRule.class);
        doReturn(Collections.singleton(readingType)).when(validationRule).getReadingTypes();
        doReturn(Collections.singletonList(validationRule)).when(validationRuleSet).getRules(anyList());
        when(validationRuleSetResolver.resolve(any(ValidationContext.class))).thenReturn(Collections.singletonList(validationRuleSet));
        Map<Channel, Range<Instant>> changeScope = ImmutableMap.of(channel1, Range.atLeast(Instant.EPOCH));
        validationService.validate(channelsContainer, changeScope);
        verify(channelsContainerValidation).moveLastCheckedBefore(changeScope);
    }

    @Test
    public void testManageValidationActivations() {
        when(channelsContainer.getId()).thenReturn(ID);
        when(channelsContainer.getMeter()).thenReturn(Optional.of(meter));
        when(meter.getId()).thenReturn(ID);
        when(channelsContainer.getChannels()).thenReturn(Arrays.asList(channel1, channel2));
        when(channel1.getId()).thenReturn(1001L);
        when(channel2.getId()).thenReturn(1002L);
        when(channel1.getChannelsContainer()).thenReturn(channelsContainer);
        when(channel2.getChannelsContainer()).thenReturn(channelsContainer);
        when(meterValidationFactory.getOptional(ID)).thenReturn(Optional.of(meterValidation));
        when(meterValidation.getActivationStatus()).thenReturn(true);

        ValidationRuleSet validationRuleSet = validationService.createValidationRuleSet(NAME, SYSTEM);
        validationRuleSet.save();

        when(validationRuleSetResolver.resolve(any(ValidationContext.class))).thenReturn(Arrays.asList(validationRuleSet));
        validationService.validate(Collections.emptySet(), channelsContainer);

        List<ChannelsContainerValidation> channelsContainerValidations = validationService.getUpdatedChannelsContainerValidations(new ValidationContextImpl(channelsContainer));
        assertThat(channelsContainerValidations).hasSize(1);
        assertThat(channelsContainerValidations.get(0).getChannelsContainer()).isEqualTo(channelsContainer);
        assertThat(channelsContainerValidations.get(0).getRuleSet()).isEqualTo(validationRuleSet);
        ChannelsContainerValidation activationRuleSet1 = channelsContainerValidations.get(0);

        ValidationRuleSet validationRuleSet2 = validationService.createValidationRuleSet(NAME, SYSTEM);
        validationRuleSet2.save();

        when(validationRuleSetResolver.resolve(any(ValidationContext.class))).thenReturn(Arrays.asList(validationRuleSet, validationRuleSet2));
        validationService.validate(Collections.emptySet(), channelsContainer);
        channelsContainerValidations = validationService.getUpdatedChannelsContainerValidations(new ValidationContextImpl(channelsContainer));
        assertThat(channelsContainerValidations).hasSize(2);
        assertThat(channelsContainerValidations.get(0).getChannelsContainer()).isEqualTo(channelsContainer);
        assertThat(channelsContainerValidations.get(1).getChannelsContainer()).isEqualTo(channelsContainer);
        assertThat(FluentIterable.from(channelsContainerValidations).transform(ChannelsContainerValidation::getRuleSet).toSet()).contains(validationRuleSet, validationRuleSet2);

        when(validationRuleSetResolver.resolve(any(ValidationContext.class))).thenReturn(Arrays.asList(validationRuleSet2));
        validationService.validate(Collections.emptySet(), channelsContainer);
        channelsContainerValidations = validationService.getUpdatedChannelsContainerValidations(new ValidationContextImpl(channelsContainer));
        assertThat(channelsContainerValidations).hasSize(1);
        assertThat(channelsContainerValidations.get(0).getChannelsContainer()).isEqualTo(channelsContainer);
        assertThat(channelsContainerValidations.get(0).getRuleSet()).isEqualTo(validationRuleSet2);
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

        ChannelValidation channelValidation = mock(ChannelValidation.class);
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


        ChannelValidation channelValidation = mock(ChannelValidation.class);
        when(channelValidationFactory.find(eq("channel"), eq(channel1))).thenReturn(Arrays.asList(channelValidation));
        when(channelValidation.getLastChecked()).thenReturn(readingDate1);
        when(fetcher.ofQualitySystems(Collections.singleton(SYSTEM))
                .inTimeInterval(Range.closed(readingDate1, readingDate2))
                .collect())
                .thenReturn(Collections.emptyList());
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

        ChannelValidation channelValidation1 = mock(ChannelValidation.class);
        when(channelValidation1.getLastChecked()).thenReturn(readingDate1);
        ChannelValidation channelValidation2 = mock(ChannelValidation.class);
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

        ChannelValidation channelValidation1 = mock(ChannelValidation.class);
        when(channelValidation1.getLastChecked()).thenReturn(readingDate1);
        ChannelValidation channelValidation2 = mock(ChannelValidation.class);
        when(channelValidation2.getLastChecked()).thenReturn(null);
        when(channelValidationFactory.find(eq("channel"), eq(channel1))).thenReturn(Arrays.asList(channelValidation1, channelValidation2));
        ReadingQualityRecord readingQuality = mock(ReadingQualityRecord.class);
        when(readingQuality.getReadingTimestamp()).thenReturn(readingDate1);
        ReadingQualityType readingQualityType = new ReadingQualityType("2.6.32131");
        when(readingQuality.getType()).thenReturn(readingQualityType);
        when(readingQuality.getTypeCode()).thenReturn("2.6.32131");
        when(fetcher.ofQualitySystems(Collections.singleton(SYSTEM))
                .inTimeInterval(Range.closed(readingDate1, readingDate2))
                .actual()
                .collect())
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

        ChannelValidation channelValidation1 = mock(ChannelValidation.class);
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
        when(fetcher.ofQualitySystems(Collections.singleton(SYSTEM))
                .inTimeInterval(Range.closed(readingDate1, readingDate2))
                .actual()
                .collect())
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
        when(validationRuleSetResolver.resolve(any(ValidationContext.class))).thenReturn(Collections.<ValidationRuleSet>emptyList());


        validationService.activateValidation(meter);
        validationService.enableValidationOnStorage(meter);

        //Check that a MeterValidation object is made
        verify(dataModel).persist(any(MeterValidationImpl.class));

        // verify that the ChannelsContainerValidations are managed for the current channelsContainer
        verify(meter).getCurrentMeterActivation();
        verify(meterActivation).getChannelsContainer();
        verify(validationRuleSetResolver).resolve(any(ValidationContext.class));
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
        when(meterValidationFactory.getOptional(ID)).thenReturn(Optional.empty());
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
        DataValidationOccurrenceImpl dataValidationOcc = new DataValidationOccurrenceImpl(dataModel, thesaurus);
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
        when(mainChannel.findReadingQualities()).thenReturn(fetcher);
        ReadingQualityWithTypeFetcher bulkFetcher = mock(ReadingQualityWithTypeFetcher.class, Answers.RETURNS_DEEP_STUBS.get());
        when(bulkChannel.findReadingQualities()).thenReturn(bulkFetcher);
        Instant start = ZonedDateTime.of(2015, 9, 1, 0, 0, 0, 0, ZoneOffset.UTC).toInstant();
        Instant end = ZonedDateTime.of(2015, 9, 5, 0, 0, 0, 0, ZoneOffset.UTC).toInstant();
        Range<Instant> range = Range.openClosed(start, end);

        ChannelValidation channelValidation = mock(ChannelValidation.class);
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
        when(bulkFetcher
                .ofQualitySystems(Collections.singleton(SYSTEM))
                .inTimeInterval(range)
                .actual()
                .collect()).thenReturn(bulkRQs);
        List<ReadingQualityRecord> mainRQs = Arrays.asList(
                mockReadingQualityRecord("2.5.258", start.plus(4, ChronoUnit.DAYS)), mockReadingQualityRecord("2.6.1003", start.plus(4, ChronoUnit.DAYS)));//rule violation
        when(fetcher
                .ofQualitySystems(Collections.singleton(SYSTEM))
                .inTimeInterval(range)
                .actual()
                .collect()).thenReturn(mainRQs);
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

    @Test
    public void testOnlyRuleSetsWithSpecificQualitySystemsAreExecuted() {
        ChannelsContainer channelsContainer = mock(ChannelsContainer.class);
        when(channelsContainer.getMeter()).thenReturn(Optional.empty());
        IValidationRuleSet mdcValidationRuleSet = mock(IValidationRuleSet.class);
        when(mdcValidationRuleSet.getQualityCodeSystem()).thenReturn(QualityCodeSystem.MDC);
        IValidationRuleSet mdmValidationRuleSet = mock(IValidationRuleSet.class);
        when(mdmValidationRuleSet.getQualityCodeSystem()).thenReturn(QualityCodeSystem.MDM);
        when(validationRuleSetResolver.resolve(any(ValidationContext.class))).thenReturn(Arrays.asList(mdcValidationRuleSet, mdmValidationRuleSet));
        ChannelsContainerValidationImpl channelsContainerValidation = mock(ChannelsContainerValidationImpl.class);
        when(dataModel.getInstance(ChannelsContainerValidationImpl.class)).thenReturn(channelsContainerValidation);
        when(channelsContainerValidation.init(any(ChannelsContainer.class))).thenReturn(channelsContainerValidation);
        when(channelsContainerValidation.getChannelsContainer()).thenReturn(channelsContainer);
        when(channelsContainerValidation.getRuleSet()).thenReturn(mdcValidationRuleSet);

        validationService.validate(EnumSet.of(QualityCodeSystem.MDC), channelsContainer);

        verify(validationRuleSetResolver).resolve(any(ValidationContext.class));
        verify(channelsContainerValidation).init(channelsContainer);
    }

    @Test
    public void testRuleSetsWithAnyQualitySystemsAreExecuted() {
        ChannelsContainer channelsContainer = mock(ChannelsContainer.class);
        when(channelsContainer.getMeter()).thenReturn(Optional.empty());
        IValidationRuleSet mdcValidationRuleSet = mock(IValidationRuleSet.class);
        when(mdcValidationRuleSet.getQualityCodeSystem()).thenReturn(QualityCodeSystem.MDC);
        IValidationRuleSet mdmValidationRuleSet = mock(IValidationRuleSet.class);
        when(mdmValidationRuleSet.getQualityCodeSystem()).thenReturn(QualityCodeSystem.MDM);
        when(validationRuleSetResolver.resolve(any(ValidationContext.class))).thenReturn(Arrays.asList(mdcValidationRuleSet, mdmValidationRuleSet));
        ChannelsContainerValidationImpl channelsContainerValidation = mock(ChannelsContainerValidationImpl.class);
        when(dataModel.getInstance(ChannelsContainerValidationImpl.class)).thenReturn(channelsContainerValidation);
        when(channelsContainerValidation.init(any(ChannelsContainer.class))).thenReturn(channelsContainerValidation);
        when(channelsContainerValidation.getChannelsContainer()).thenReturn(channelsContainer);
        when(channelsContainerValidation.getRuleSet()).thenReturn(mdcValidationRuleSet);

        validationService.validate(Collections.emptySet(), channelsContainer);

        verify(validationRuleSetResolver).resolve(any(ValidationContext.class));
        verify(channelsContainerValidation, times(2)).init(channelsContainer);
    }
}
