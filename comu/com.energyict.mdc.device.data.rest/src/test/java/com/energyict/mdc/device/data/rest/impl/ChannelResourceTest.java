package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.readings.ProfileStatus;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.util.time.Interval;
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
import com.jayway.jsonpath.JsonModel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

public class ChannelResourceTest extends DeviceDataRestApplicationJerseyTest {

    public static final String BATTERY_LOW = "BATTERY_LOW";
    public static final Date NOW = new Date(1410786205000L);
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
    private DeviceValidation deviceValidation;
    @Mock
    private IValidationRule rule1;
    @Mock
    private ReadingQuality quality1;
    @Mock
    private ValidationRuleSet ruleSet;
    @Mock
    private ValidationEvaluator evaluator;

    public ChannelResourceTest() {
    }

    @Before
    public void setUpStubs() {
        when(deviceDataService.findByUniqueMrid("1")).thenReturn(device);
        when(device.getLoadProfiles()).thenReturn(Arrays.asList(loadProfile));
        when(loadProfile.getId()).thenReturn(1L);
        Interval interval = new Interval(new Date(intervalStart), new Date(intervalEnd));
        when(channel1.getChannelData(interval)).thenReturn(asList(loadProfileReading));
        when(loadProfile.getChannels()).thenReturn(asList(channel1, channel2));
        when(loadProfileReading.getInterval()).thenReturn(interval);
        when(loadProfileReading.getFlags()).thenReturn(Arrays.asList(ProfileStatus.Flag.BATTERY_LOW));
        when(thesaurus.getString(BATTERY_LOW, BATTERY_LOW)).thenReturn(BATTERY_LOW);
        when(loadProfileReading.getChannelValues()).thenReturn(ImmutableMap.of(channel1, BigDecimal.valueOf(200, 0), channel2, BigDecimal.valueOf(250, 0)));
        when(clock.now()).thenReturn(NOW);
        when(channel1.getDevice()).thenReturn(device);
        when(channel1.getId()).thenReturn(CHANNEL_ID1);
        when(channel1.getChannelSpec()).thenReturn(channelSpec);
        when(channelSpec.getId()).thenReturn(CHANNEL_ID1);
        when(channel2.getDevice()).thenReturn(device);
        when(channel2.getId()).thenReturn(CHANNEL_ID2);
        when(channel2.getChannelSpec()).thenReturn(channelSpec);
        when(device.forValidation()).thenReturn(deviceValidation);
        when(deviceValidation.isValidationActive(channel1, NOW)).thenReturn(true);
        when(deviceValidation.isValidationActive(channel2, NOW)).thenReturn(true);
        DataValidationStatusImpl state1 = new DataValidationStatusImpl(new Date(intervalEnd), true);
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
    }

    @Test
    public void testChannelData() {
        String json = target("devices/1/loadprofiles/1/channels/" + CHANNEL_ID1 + "/data")
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
        String value = jsonModel.<String>get("$.data[0].value");
        assertThat(value).isEqualTo("250.000");
        assertThat(jsonModel.<Boolean>get("$.data[0].dataValidated")).isTrue();
        assertThat(jsonModel.<String>get("$.data[0].validationResult")).isEqualTo("validationStatus.suspect");
        assertThat(jsonModel.<List<?>>get("$.data[0].suspectReason")).hasSize(1);
        assertThat(jsonModel.<Boolean>get("$.data[0].suspectReason[0].active")).isTrue();
        assertThat(jsonModel.<String>get("$.data[0].suspectReason[0].implementation")).isEqualTo("isPrime");
        assertThat(jsonModel.<String>get("$.data[0].suspectReason[0].displayName")).isEqualTo("Primes only");
    }

    @Test
    public void testChannelDataFiltered() {
        when(evaluator.getValidationResult(any())).thenReturn(ValidationResult.VALID);

        String json = target("devices/1/loadprofiles/1/channels/" + CHANNEL_ID1 + "/data")
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
        String json = target("devices/1/loadprofiles/1/channels/" + CHANNEL_ID1 + "/data")
                .queryParam("intervalStart", "1410774630000")
                .queryParam("intervalEnd", "1410828630000")
                .queryParam("onlySuspect", "true")
                .request().get(String.class);

        System.out.println(json);

        JsonModel jsonModel = JsonModel.create(json);

        assertThat(jsonModel.<List<?>>get("$.data")).hasSize(1);
    }


}