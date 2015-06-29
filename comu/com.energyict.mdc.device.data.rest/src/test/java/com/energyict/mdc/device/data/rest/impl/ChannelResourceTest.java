package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.ProfileStatus;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.impl.DataValidationStatusImpl;
import com.elster.jupiter.validation.impl.IValidationRule;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.common.rest.IntervalInfo;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.data.*;
import com.energyict.mdc.issue.datavalidation.IssueDataValidation;
import com.energyict.mdc.issue.datavalidation.NotEstimatedBlock;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import com.jayway.jsonpath.JsonModel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class ChannelResourceTest extends DeviceDataRestApplicationJerseyTest {

    public static final String BATTERY_LOW = "BATTERY_LOW";
    public static final Instant NOW = Instant.ofEpochMilli(1410786205000L);
    public static final Date LAST_CHECKED = new Date(1409570229000L);
    public static final Instant LAST_READING = Instant.ofEpochMilli(1410786196000L);
    public static final long CHANNEL_ID1 = 151521354L;
    private static long intervalStart = 1410774630000L;
    private static long intervalEnd = 1410828630000L;

    @Mock
    private Device device;
    @Mock
    private LoadProfile loadProfile;
    @Mock
    private LoadProfileReading loadProfileReading, addedloadProfileReading, editedProfileReading, removedProfileReading;
    @Mock
    private ChannelSpec channelSpec;
    @Mock
    private Channel channel;
    @Mock
    private DeviceValidation deviceValidation;
    @Mock
    private IValidationRule rule1;
    @Mock
    private ReadingQuality quality1;
    @Mock
    private ValidationRuleSet ruleSet;
    @Mock
    private ValidationEvaluator evaluator;
    @Mock
    private IntervalReadingRecord readingRecord, addedReadingRecord, editedReadingRecord;

    public ChannelResourceTest() {
    }

    @Before
    public void setUpStubs() {
        when(deviceService.findByUniqueMrid("1")).thenReturn(Optional.of(device));
        when(device.getLoadProfiles()).thenReturn(Arrays.asList(loadProfile));
        when(loadProfile.getId()).thenReturn(1L);
        when(loadProfile.getChannels()).thenReturn(Arrays.asList(channel));

        Range<Instant> interval = Ranges.openClosed(Instant.ofEpochMilli(intervalStart), Instant.ofEpochMilli(intervalEnd));
        when(channel.getChannelData(interval)).thenReturn(asList(loadProfileReading, addedloadProfileReading, editedProfileReading, removedProfileReading));
        when(loadProfileReading.getRange()).thenReturn(interval);
        when(loadProfileReading.getFlags()).thenReturn(Arrays.asList(ProfileStatus.Flag.BATTERY_LOW));
        doReturn(BATTERY_LOW).when(thesaurus).getString(BATTERY_LOW, BATTERY_LOW);
        when(loadProfileReading.getChannelValues()).thenReturn(ImmutableMap.of(channel, readingRecord));
        when(readingRecord.getValue()).thenReturn(BigDecimal.valueOf(200, 0));
        when(readingRecord.getReportedDateTime()).thenReturn(LAST_READING);

        when(addedloadProfileReading.getRange()).thenReturn(interval);
        when(addedloadProfileReading.getChannelValues()).thenReturn(ImmutableMap.of(channel, addedReadingRecord));
        when(addedReadingRecord.getValue()).thenReturn(BigDecimal.valueOf(201, 0));
        when(addedReadingRecord.wasAdded()).thenReturn(true);
        when(addedReadingRecord.getReportedDateTime()).thenReturn(LAST_READING);

        when(editedProfileReading.getRange()).thenReturn(interval);
        when(editedProfileReading.getChannelValues()).thenReturn(ImmutableMap.of(channel, editedReadingRecord));
        when(editedReadingRecord.getValue()).thenReturn(BigDecimal.valueOf(202, 0));
        when(editedReadingRecord.wasAdded()).thenReturn(false);
        when(editedReadingRecord.edited()).thenReturn(true);
        when(editedReadingRecord.getReportedDateTime()).thenReturn(LAST_READING);

        when(removedProfileReading.getRange()).thenReturn(interval);
        when(removedProfileReading.getReadingTime()).thenReturn(LAST_READING);

        when(clock.instant()).thenReturn(NOW);
        when(channel.getDevice()).thenReturn(device);
        when(channel.getId()).thenReturn(CHANNEL_ID1);
        when(channel.getChannelSpec()).thenReturn(channelSpec);
        when(channelSpec.getId()).thenReturn(CHANNEL_ID1);
        when(device.forValidation()).thenReturn(deviceValidation);
        when(deviceValidation.isValidationActive(channel, NOW)).thenReturn(true);
        DataValidationStatusImpl state1 = new DataValidationStatusImpl(Instant.ofEpochMilli(intervalEnd), true);
        state1.addReadingQuality(quality1, asList(rule1));
        when(rule1.getRuleSet()).thenReturn(ruleSet);
        when(ruleSet.getName()).thenReturn("ruleSetName");
        doReturn(Arrays.asList(rule1)).when(ruleSet).getRules();
        when(rule1.isActive()).thenReturn(true);
        when(loadProfileReading.getChannelValidationStates()).thenReturn(ImmutableMap.of(channel, state1));
        when(addedloadProfileReading.getChannelValidationStates()).thenReturn(ImmutableMap.of(channel, state1));
        when(editedProfileReading.getChannelValidationStates()).thenReturn(ImmutableMap.of(channel, state1));
        when(validationService.getEvaluator()).thenReturn(evaluator);
        when(evaluator.getValidationResult(any())).thenReturn(ValidationResult.SUSPECT);
        when(rule1.getImplementation()).thenReturn("isPrime");
        when(rule1.getDisplayName()).thenReturn("Primes only");
        when(channelSpec.getNbrOfFractionDigits()).thenReturn(3);
        when(deviceValidation.getValidationResult(any())).thenReturn(ValidationResult.SUSPECT);
        when(channel.getLastDateTime()).thenReturn(Optional.of(NOW));
        ReadingType readingType = mockReadingType("1.2.3.4.5.6.7.8.9.10.11.12.13.14.15.16.17.18");
        when(channel.getReadingType()).thenReturn(readingType);
        when(channel.getInterval()).thenReturn(TimeDuration.minutes(15));
        Unit unit = Unit.get("kWh");
        when(channel.getLastReading()).thenReturn(Optional.<Instant>empty());
        when(channel.getLoadProfile()).thenReturn(loadProfile);
        when(channel.getLastDateTime()).thenReturn(Optional.of(NOW));
        when(channel.getUnit()).thenReturn(unit);
        when(deviceValidation.getLastChecked(channel)).thenReturn(Optional.of(NOW));
    }

    @Test
    public void testChannelData() {
        String json = target("devices/1/channels/" + CHANNEL_ID1 + "/data")
                .queryParam("intervalStart", "1410774630000")
                .queryParam("intervalEnd", "1410828630000")
                .request().get(String.class);

        System.out.println(json);

        JsonModel jsonModel = JsonModel.create(json);

        assertThat(jsonModel.<List<?>>get("$.data")).hasSize(4);
        assertThat(jsonModel.<Long>get("$.data[0].interval.start")).isEqualTo(1410774630000L);
        assertThat(jsonModel.<Long>get("$.data[0].interval.end")).isEqualTo(1410828630000L);
        assertThat(jsonModel.<List<?>>get("$.data[0].intervalFlags")).hasSize(1);
        assertThat(jsonModel.<String>get("$.data[0].intervalFlags[0]")).isEqualTo(BATTERY_LOW);
        String value = jsonModel.<String>get("$.data[0].value");
        assertThat(value).isEqualTo("200.000");
        assertThat(jsonModel.<Boolean>get("$.data[0].validationInfo.dataValidated")).isTrue();
        assertThat(jsonModel.<String>get("$.data[0].validationInfo.mainValidationInfo.validationResult")).isEqualTo("validationStatus.suspect");
        assertThat(jsonModel.<List<?>>get("$.data[0].validationInfo.mainValidationInfo.validationRules")).hasSize(1);
        assertThat(jsonModel.<Boolean>get("$.data[0].validationInfo.mainValidationInfo.validationRules[0].active")).isTrue();
        assertThat(jsonModel.<String>get("$.data[0].validationInfo.mainValidationInfo.validationRules[0].implementation")).isEqualTo("isPrime");
        assertThat(jsonModel.<String>get("$.data[0].validationInfo.mainValidationInfo.validationRules[0].displayName")).isEqualTo("Primes only");
        assertThat(jsonModel.<String>get("$.data[0].modificationFlag")).isNull();
        assertThat(jsonModel.<Long>get("$.data[0].reportedDateTime")).isEqualTo(LAST_READING.toEpochMilli());

        assertThat(jsonModel.<String>get("$.data[1].value")).isEqualTo("201.000");
        assertThat(jsonModel.<String>get("$.data[1].modificationFlag")).isEqualTo("ADDED");
        assertThat(jsonModel.<Long>get("$.data[1].reportedDateTime")).isEqualTo(LAST_READING.toEpochMilli());

        assertThat(jsonModel.<String>get("$.data[2].value")).isEqualTo("202.000");
        assertThat(jsonModel.<String>get("$.data[2].modificationFlag")).isEqualTo("EDITED");
        assertThat(jsonModel.<Long>get("$.data[2].reportedDateTime")).isEqualTo(LAST_READING.toEpochMilli());

        assertThat(jsonModel.<String>get("$.data[3].value")).isNull();
        assertThat(jsonModel.<String>get("$.data[3].modificationFlag")).isEqualTo("REMOVED");
        assertThat(jsonModel.<Long>get("$.data[3].reportedDateTime")).isEqualTo(LAST_READING.toEpochMilli());
    }

    @Test
    public void testPutChannelData() {
        MeterActivation meterActivation = mock(MeterActivation.class);
        com.elster.jupiter.metering.Channel meteringChannel = mock(com.elster.jupiter.metering.Channel.class);
        ReadingType readingType = mock(ReadingType.class);
        List list = mock(List.class);
        when(channel.getReadingType()).thenReturn(readingType);
        ChannelDataUpdater channelDataUpdater = mock(ChannelDataUpdater.class);
        when(channelDataUpdater.editChannelData(anyList())).thenReturn(channelDataUpdater);
        when(channelDataUpdater.removeChannelData(anyList())).thenReturn(channelDataUpdater);
        when(channel.startEditingData()).thenReturn(channelDataUpdater);
        when(device.getId()).thenReturn(1L);
        when(meterActivation.getChannels()).thenReturn(Arrays.asList(meteringChannel));
        doReturn(Arrays.asList(readingType)).when(meteringChannel).getReadingTypes();
        when(list.contains(readingType)).thenReturn(true);

        ChannelDataInfo channelDataInfo = new ChannelDataInfo();
        channelDataInfo.value = BigDecimal.TEN;
        channelDataInfo.interval = new IntervalInfo();
        channelDataInfo.interval.start = intervalStart;
        channelDataInfo.interval.end = intervalEnd;

        List<ChannelDataInfo> infos = new ArrayList<>();
        infos.add(channelDataInfo);

        Response response = target("devices/1/channels/" + CHANNEL_ID1 + "/data").request().put(Entity.json(infos));
        verify(channelDataUpdater).complete();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testChannelDataFiltered() {
        when(evaluator.getValidationResult(any())).thenReturn(ValidationResult.VALID);
        when(deviceValidation.getValidationResult(any())).thenReturn(ValidationResult.VALID);

        String json = target("devices/1/channels/" + CHANNEL_ID1 + "/data")
                .queryParam("intervalStart", "1410774630000")
                .queryParam("intervalEnd", "1410828630000")
                .queryParam("onlySuspect", "true")
                .request().get(String.class);

        System.out.println(json);

        JsonModel jsonModel = JsonModel.create(json);

        assertThat(jsonModel.<List<?>>get("$.data")).isEmpty();
    }

    @Test
    public void testChannelDataFilteredMatches() {
        String json = target("devices/1/channels/" + CHANNEL_ID1 + "/data")
                .queryParam("intervalStart", "1410774630000")
                .queryParam("intervalEnd", "1410828630000")
                .queryParam("onlySuspect", "true")
                .request().get(String.class);

        System.out.println(json);

        JsonModel jsonModel = JsonModel.create(json);

        assertThat(jsonModel.<List<?>>get("$.data")).hasSize(3);
    }

    @Test
    public void testValidate() {
        when(loadProfile.getDevice()).thenReturn(device);
        when(device.forValidation()).thenReturn(deviceValidation);
        when(channel.getLastReading()).thenReturn(Optional.of(LAST_READING));

        Response response = target("devices/1/channels/" + CHANNEL_ID1 + "/validate")
                .request()
                .put(Entity.json(new TriggerValidationInfo()));

        assertThat(response.getEntity()).isNotNull();
        verify(deviceValidation).validateChannel(channel);
    }

    @Test
    public void testValidateWithDate() {
        when(loadProfile.getDevice()).thenReturn(device);
        when(device.forValidation()).thenReturn(deviceValidation);
        when(channel.getLastReading()).thenReturn(Optional.of(LAST_READING));

        TriggerValidationInfo triggerValidationInfo = new TriggerValidationInfo();
        triggerValidationInfo.lastChecked = LAST_CHECKED.getTime();
        Response response = target("devices/1/channels/" + CHANNEL_ID1 + "/validate")
                .request()
                .put(Entity.json(triggerValidationInfo));

        assertThat(response.getEntity()).isNotNull();
        verify(deviceValidation).setLastChecked(channel, LAST_CHECKED.toInstant());
        verify(deviceValidation).validateChannel(channel);
    }

    @Test
    public void testChannelInfo(){
        String json = target("devices/1/channels/" + CHANNEL_ID1).request().get(String.class);
        JsonModel jsonModel = JsonModel.create(json);
        // TODO add items
        assertThat(jsonModel.<Number>get("$.id").longValue()).isEqualTo(CHANNEL_ID1);
        assertThat(jsonModel.<Number>get("$.lastValueTimestamp")).isEqualTo(NOW.toEpochMilli());
    }

    @Test
    public void testGetValidationBlocksOnIssueNoBlocks() {
        IssueDataValidation issue = mock(IssueDataValidation.class);
        doReturn(Optional.of(issue)).when(issueDataValidationService).findIssue(12L);
        List<NotEstimatedBlock> blocks = new ArrayList<>();
        when(issue.getNotEstimatedBlocks()).thenReturn(blocks);

        String response = target("devices/1/channels/" + CHANNEL_ID1 + "/datavalidationissues/12/validationblocks").request().get(String.class);

        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(0);
        assertThat(jsonModel.<List<?>>get("$.validationBlocks")).isEmpty();
    }

    @Test
    public void testGetValidationBlocksOnIssue() {
        IssueDataValidation issue = mock(IssueDataValidation.class);
        doReturn(Optional.of(issue)).when(issueDataValidationService).findIssue(12L);
        List<NotEstimatedBlock> blocks = new ArrayList<>();
        when(issue.getNotEstimatedBlocks()).thenReturn(blocks);

        Instant now = Instant.now();
        ReadingType bulkReadingType = mockReadingType("0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0");
        ReadingType deltaReadingType = mockReadingType("0.0.2.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0");
        when(bulkReadingType.getCalculatedReadingType()).thenReturn(Optional.of(deltaReadingType));
        when(channel.getReadingType()).thenReturn(bulkReadingType);

        blocks.add(mockNotEstimatedBlock(now, now.plus(30, ChronoUnit.MINUTES), bulkReadingType));
        blocks.add(mockNotEstimatedBlock(now.plus(60, ChronoUnit.MINUTES), now.plus(90, ChronoUnit.MINUTES), bulkReadingType));
        blocks.add(mockNotEstimatedBlock(now, now.plus(15, ChronoUnit.MINUTES), deltaReadingType));
        blocks.add(mockNotEstimatedBlock(now.plus(30, ChronoUnit.MINUTES), now.plus(75, ChronoUnit.MINUTES), deltaReadingType));

        String response = target("devices/1/channels/" + CHANNEL_ID1 + "/datavalidationissues/12/validationblocks").request().get(String.class);

        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<List<?>>get("$.validationBlocks")).hasSize(1);
        assertThat(jsonModel.<Number>get("$.validationBlocks[0].startTime")).isEqualTo(now.toEpochMilli());
        assertThat(jsonModel.<Number>get("$.validationBlocks[0].endTime")).isEqualTo(now.plus(90, ChronoUnit.MINUTES).toEpochMilli());
    }

    private NotEstimatedBlock mockNotEstimatedBlock(Instant from, Instant to, ReadingType readingType) {
        NotEstimatedBlock block = mock(NotEstimatedBlock.class);
        when(block.getStartTime()).thenReturn(from);
        when(block.getEndTime()).thenReturn(to);
        when(block.getReadingType()).thenReturn(readingType);
        return block;
    }
}