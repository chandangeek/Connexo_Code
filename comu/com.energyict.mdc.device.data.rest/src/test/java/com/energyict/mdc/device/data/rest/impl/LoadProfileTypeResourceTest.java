package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.ProfileStatus;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.elster.jupiter.validation.ValidationResult;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.impl.DataValidationStatusImpl;
import com.elster.jupiter.validation.impl.IValidationRule;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceValidation;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.LoadProfileReading;
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
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class LoadProfileTypeResourceTest extends DeviceDataRestApplicationJerseyTest {

    public static final String BATTERY_LOW = "BATTERY_LOW";
    public static final Instant NOW = Instant.ofEpochMilli(1410786205000L);
    public static final Instant LAST_READING = Instant.ofEpochMilli(1410786196000L);
    public static final Date LAST_CHECKED = new Date(1409570229000L);
    public static final long CHANNEL_ID1 = 151521354L;
    public static final long CHANNEL_ID2 = 7487921005L;
    private static long intervalStart = 1410774630000L;
    private static long intervalEnd = 1410828630000L;

    @Mock
    private Device device;
    @Mock
    private LoadProfile loadProfile;
    @Mock
    private LoadProfileReading loadProfileReading;
    @Mock
    private ChannelSpec channelSpec;
    @Mock
    private Channel channel1, channel2;
    @Mock
    private IntervalReadingRecord readingRecord1, readingRecord2;
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

    public LoadProfileTypeResourceTest() {
    }

    @Before
    public void setUpStubs() {
        when(deviceService.findByUniqueMrid("1")).thenReturn(Optional.of(device));
        when(device.getLoadProfiles()).thenReturn(Arrays.asList(loadProfile));
        when(loadProfile.getId()).thenReturn(1L);
        Range<Instant> interval = Ranges.openClosed(Instant.ofEpochMilli(intervalStart), Instant.ofEpochMilli(intervalEnd));
        when(loadProfile.getChannelData(interval)).thenReturn(asList(loadProfileReading));
        when(loadProfileReading.getRange()).thenReturn(interval);
        when(loadProfileReading.getFlags()).thenReturn(Arrays.asList(ProfileStatus.Flag.BATTERY_LOW));
        when(thesaurus.getString(BATTERY_LOW, BATTERY_LOW)).thenReturn(BATTERY_LOW);
        when(loadProfileReading.getChannelValues()).thenReturn(ImmutableMap.of(channel1, readingRecord1, channel2, readingRecord2));
        when(clock.instant()).thenReturn(NOW);
        when(readingRecord1.getValue()).thenReturn(BigDecimal.valueOf(200, 0));
        when(readingRecord2.getValue()).thenReturn(BigDecimal.valueOf(250, 0));
        ReadingType rt = mock(ReadingType.class);
        when(rt.isCumulative()).thenReturn(false);
        when(channel1.getDevice()).thenReturn(device);
        when(channel1.getReadingType()).thenReturn(rt);
        when(channel1.getId()).thenReturn(CHANNEL_ID1);
        when(channel1.getChannelSpec()).thenReturn(channelSpec);
        when(channel2.getDevice()).thenReturn(device);
        when(channel2.getReadingType()).thenReturn(rt);
        when(channel2.getId()).thenReturn(CHANNEL_ID2);
        when(channel2.getChannelSpec()).thenReturn(channelSpec);
        when(device.forValidation()).thenReturn(deviceValidation);
        when(deviceValidation.isValidationActive(channel1, NOW)).thenReturn(true);
        when(deviceValidation.isValidationActive(channel2, NOW)).thenReturn(true);
        DataValidationStatusImpl state1 = new DataValidationStatusImpl(Instant.ofEpochMilli(intervalEnd), true);
        state1.addReadingQuality(quality1, asList(rule1));
        when(rule1.getRuleSet()).thenReturn(ruleSet);
        when(ruleSet.getName()).thenReturn("ruleSetName");
        doReturn(Arrays.asList(rule1)).when(ruleSet).getRules();
        when(rule1.isActive()).thenReturn(true);
        when(loadProfileReading.getChannelValidationStates()).thenReturn(ImmutableMap.of(channel1, state1));
        when(validationService.getEvaluator()).thenReturn(evaluator);
        when(evaluator.getValidationResult(any())).thenReturn(ValidationResult.SUSPECT);
        when(rule1.getImplementation()).thenReturn("isPrime");
        when(rule1.getDisplayName()).thenReturn("Primes only");
        when(channelSpec.getNbrOfFractionDigits()).thenReturn(3);
        when(deviceValidation.getValidationResult(any())).thenReturn(ValidationResult.SUSPECT);
    }

    @Test
    public void testLoadProfileData() {
        String json = target("devices/1/loadprofiles/1/data")
                .queryParam("intervalStart", "1410774630000")
                .queryParam("intervalEnd", "1410828630000")
                .request().get(String.class);

        System.out.println(json);

        JsonModel jsonModel = JsonModel.create(json);

        assertThat(jsonModel.<List<?>>get("$.data")).hasSize(1);
        assertThat(jsonModel.<Long>get("$.data[0].interval.start")).isEqualTo(1410774630000L);
        assertThat(jsonModel.<Long>get("$.data[0].interval.end")).isEqualTo(1410828630000L);
        assertThat(jsonModel.<List<?>>get("$.data[0].intervalFlags")).hasSize(1);
        assertThat(jsonModel.<String>get("$.data[0].intervalFlags[0]")).isEqualTo(BATTERY_LOW);
        Map values = jsonModel.<Map>get("$.data[0].channelData");
        assertThat(values).contains(entry(String.valueOf(CHANNEL_ID1), "200.000"));
        assertThat(values).contains(entry(String.valueOf(CHANNEL_ID2), "250.000"));
        Map validations = jsonModel.<Map>get("$.data[0].channelValidationData");
        assertThat(validations).hasSize(1).containsKey(String.valueOf(CHANNEL_ID1));
        assertThat(jsonModel.<Boolean>get("$.data[0].channelValidationData." + CHANNEL_ID1 + ".dataValidated")).isTrue();
        assertThat(jsonModel.<String>get("$.data[0].channelValidationData." + CHANNEL_ID1 + ".mainValidationInfo.validationResult")).isEqualTo("validationStatus.suspect");
        assertThat(jsonModel.<List<?>>get("$.data[0].channelValidationData." + CHANNEL_ID1 + ".mainValidationInfo.validationRules")).hasSize(1);
        assertThat(jsonModel.<Boolean>get("$.data[0].channelValidationData." + CHANNEL_ID1 + ".mainValidationInfo.validationRules[0].active")).isTrue();
        assertThat(jsonModel.<String>get("$.data[0].channelValidationData." + CHANNEL_ID1 + ".mainValidationInfo.validationRules[0].implementation")).isEqualTo("isPrime");
        assertThat(jsonModel.<String>get("$.data[0].channelValidationData." + CHANNEL_ID1 + ".mainValidationInfo.validationRules[0].displayName")).isEqualTo("Primes only");
    }

    @Test
    public void testLoadProfileDataFiltered() {
        when(evaluator.getValidationResult(any())).thenReturn(ValidationResult.VALID);
        when(deviceValidation.getValidationResult(any())).thenReturn(ValidationResult.VALID);

        String json = target("devices/1/loadprofiles/1/data")
                .queryParam("intervalStart", "1410774630000")
                .queryParam("intervalEnd", "1410828630000")
                .queryParam("onlySuspect", "true")
                .request().get(String.class);

        System.out.println(json);

        JsonModel jsonModel = JsonModel.create(json);

        assertThat(jsonModel.<List<?>>get("$.data")).isEmpty();
    }

    @Test
    public void testLoadProfileDataFilteredMatches() {
        String json = target("devices/1/loadprofiles/1/data")
                .queryParam("intervalStart", "1410774630000")
                .queryParam("intervalEnd", "1410828630000")
                .queryParam("onlySuspect", "true")
                .request().get(String.class);

        System.out.println(json);

        JsonModel jsonModel = JsonModel.create(json);

        assertThat(jsonModel.<List<?>>get("$.data")).hasSize(1);
    }

    @Test
    public void testValidate() {
        when(loadProfile.getDevice()).thenReturn(device);
        when(device.forValidation()).thenReturn(deviceValidation);
        when(loadProfile.getLastReading()).thenReturn(Optional.of(LAST_READING));

        Response response = target("devices/1/loadprofiles/1/validate")
                .request()
                .put(Entity.json(new TriggerValidationInfo()));

        assertThat(response.getEntity()).isNotNull();
        verify(deviceValidation).validateLoadProfile(loadProfile);
    }

    @Test
    public void testValidateWithDate() {
        when(loadProfile.getDevice()).thenReturn(device);
        when(device.forValidation()).thenReturn(deviceValidation);
        when(loadProfile.getLastReading()).thenReturn(Optional.of(LAST_READING));

        TriggerValidationInfo triggerValidationInfo = new TriggerValidationInfo();
        triggerValidationInfo.lastChecked = LAST_CHECKED.getTime();
        Response response = target("devices/1/loadprofiles/1/validate")
                .request()
                .put(Entity.json(triggerValidationInfo));

        assertThat(response.getEntity()).isNotNull();
        verify(deviceValidation).validateLoadProfile(loadProfile);
    }

}