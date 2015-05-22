package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.metering.*;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.*;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.data.DeviceValidation;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.device.data.NumericalRegister;
import com.energyict.mdc.device.data.impl.DeviceImpl;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.google.common.collect.Range;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mock;

import java.net.URLEncoder;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class DeviceValidationResourceTest extends DeviceDataRestApplicationJerseyTest {

    @Rule
    public TestRule timeZoneNeutral = Using.timeZoneOfMcMurdo();

    public static final long DEVICE_ID = 56854L;
    public static final Instant NOW = ZonedDateTime.of(2014, 6, 14, 10, 43, 13, 0, ZoneId.systemDefault()).toInstant();

    @Mock
    private MdcPropertyUtils mdcPropertyUtils;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private AmrSystem mdcAmrSystem;
    @Mock
    private ValidationEvaluator evaluator;

    @Mock
    private DeviceImpl device;
    @Mock
    private Meter meter;
    @Mock
    private NumericalRegister register1;
    @Mock
    private RegisterSpec registerSpec;
    @Mock
    private RegisterType registerType;
    @Mock
    private ReadingType regReadingType, channelReadingType1, channelReadingType2;
    @Mock
    private MeterActivation meterActivation1, meterActivation2, meterActivation3;
    @Mock
    private Channel channel1, channel2, channel3, channel4, channel5, channel6, channel7, channel8, channel9;
    @Mock
    private DataValidationStatus validationStatus1, validationStatus2, validationStatus3, validationStatus4, validationStatus5, validationStatus6;
    @Mock
    private ReadingQuality suspect, notSuspect;
    @Mock
    private LoadProfile loadProfile1;
    @Mock
    private LoadProfileSpec loadProfileSpec1;
    @Mock
    private LoadProfileType loadProfileType1;
    @Mock
    private com.energyict.mdc.device.data.Channel ch1, ch2;
    @Mock
    private DeviceValidation deviceValidation;


    @Before
    public void setUp1() {
        when(deviceService.findByUniqueMrid("MRID")).thenReturn(Optional.of(device));
        when(meteringService.findAmrSystem(1)).thenReturn(Optional.of(mdcAmrSystem));
        when(device.getId()).thenReturn(DEVICE_ID);
        when(mdcAmrSystem.findMeter("" + DEVICE_ID)).thenReturn(Optional.of(meter));
        when(clock.instant()).thenReturn(NOW);
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        when(device.forValidation()).thenReturn(deviceValidation);
        when(ch1.getDevice()).thenReturn(device);
        when(ch2.getDevice()).thenReturn(device);
        when(register1.getDevice()).thenReturn(device);

        doModelStubbing();

    }

    @Test
    public void testGetValidationFeatureStatusCheckRegisterCount() {

        DeviceValidationStatusInfo response = target("devices/MRID/validationrulesets/validationstatus").request().get(DeviceValidationStatusInfo.class);

        assertThat(response.registerSuspectCount).isEqualTo(5);
    }

    @Test
    public void testGetValidationFeatureStatusCheckLoadProfileCount() {

        DeviceValidationStatusInfo response = target("devices/MRID/validationrulesets/validationstatus").request().get(DeviceValidationStatusInfo.class);

        assertThat(response.loadProfileSuspectCount).isEqualTo(4);
    }

    @Test
    public void testGetValidationMonitoringConfigurationView() {

        String loadProfilePeriodsInfo =
                "[{\"id\":1," +
                        "\"intervalStart\":" + ZonedDateTime.ofInstant(NOW, ZoneId.systemDefault()).minusMonths(1).truncatedTo(ChronoUnit.DAYS).plusDays(1).toInstant().toEpochMilli() + "," +
                        "\"intervalEnd\":" + ZonedDateTime.ofInstant(NOW, ZoneId.systemDefault()).truncatedTo(ChronoUnit.DAYS).plusDays(1).toInstant().toEpochMilli() + "}," +
                "{\"id\":2," +
                        "\"intervalStart\":" + ZonedDateTime.ofInstant(NOW, ZoneId.systemDefault()).minusMonths(1).truncatedTo(ChronoUnit.DAYS).plusDays(1).toInstant().toEpochMilli() + "," +
                        "\"intervalEnd\":" + ZonedDateTime.ofInstant(NOW, ZoneId.systemDefault()).truncatedTo(ChronoUnit.DAYS).plusDays(1).toInstant().toEpochMilli() + "}," +
                "{\"id\":3," +
                        "\"intervalStart\":" + ZonedDateTime.ofInstant(NOW, ZoneId.systemDefault()).minusMonths(1).truncatedTo(ChronoUnit.DAYS).plusDays(1).toInstant().toEpochMilli() + "," +
                        "\"intervalEnd\":" + ZonedDateTime.ofInstant(NOW, ZoneId.systemDefault()).truncatedTo(ChronoUnit.DAYS).plusDays(1).toInstant().toEpochMilli() + "}]";

        MonitorValidationInfo response = target("devices/MRID/validationrulesets/validationmonitoring/configurationview")
                .queryParam("intervalRegisterStart", ZonedDateTime.ofInstant(NOW, ZoneId.systemDefault()).minusMonths(1).truncatedTo(ChronoUnit.DAYS).plusDays(1).toInstant().toEpochMilli())
                .queryParam("intervalRegisterEnd", ZonedDateTime.ofInstant(NOW, ZoneId.systemDefault()).truncatedTo(ChronoUnit.DAYS).plusDays(1).toInstant().toEpochMilli())
                .queryParam("intervalLoadProfile", URLEncoder.encode(loadProfilePeriodsInfo))
                .request().get(MonitorValidationInfo.class);

        assertThat(response.detailedRuleSets.size()).isEqualTo(1);
        assertThat(response.detailedRuleSets.get(0).id).isEqualTo(1);
        assertThat(response.detailedRuleSets.get(0).detailedRuleSetVersions.get(0).id).isEqualTo(11);
        assertThat(response.detailedRuleSets.get(0).detailedRuleSetVersions.get(0).detailedRules.get(0).id).isEqualTo(20);
    }

    @Test
    public void testGetValidationMonitoringDataView() {

        String loadProfilePeriodsInfo =
                "[{\"id\":1," +
                        "\"intervalStart\":" + ZonedDateTime.ofInstant(NOW, ZoneId.systemDefault()).minusMonths(1).truncatedTo(ChronoUnit.DAYS).plusDays(1).toInstant().toEpochMilli() + "," +
                        "\"intervalEnd\":" + ZonedDateTime.ofInstant(NOW, ZoneId.systemDefault()).truncatedTo(ChronoUnit.DAYS).plusDays(1).toInstant().toEpochMilli() + "}," +
                        "{\"id\":2," +
                        "\"intervalStart\":" + ZonedDateTime.ofInstant(NOW, ZoneId.systemDefault()).minusMonths(1).truncatedTo(ChronoUnit.DAYS).plusDays(1).toInstant().toEpochMilli() + "," +
                        "\"intervalEnd\":" + ZonedDateTime.ofInstant(NOW, ZoneId.systemDefault()).truncatedTo(ChronoUnit.DAYS).plusDays(1).toInstant().toEpochMilli() + "}," +
                        "{\"id\":3," +
                        "\"intervalStart\":" + ZonedDateTime.ofInstant(NOW, ZoneId.systemDefault()).minusMonths(1).truncatedTo(ChronoUnit.DAYS).plusDays(1).toInstant().toEpochMilli() + "," +
                        "\"intervalEnd\":" + ZonedDateTime.ofInstant(NOW, ZoneId.systemDefault()).truncatedTo(ChronoUnit.DAYS).plusDays(1).toInstant().toEpochMilli() + "}]";

        MonitorValidationInfo response = target("devices/MRID/validationrulesets/validationmonitoring/dataview")
                .queryParam("intervalRegisterStart", ZonedDateTime.ofInstant(NOW, ZoneId.systemDefault()).minusMonths(1).truncatedTo(ChronoUnit.DAYS).plusDays(1).toInstant().toEpochMilli())
                .queryParam("intervalRegisterEnd", ZonedDateTime.ofInstant(NOW, ZoneId.systemDefault()).truncatedTo(ChronoUnit.DAYS).plusDays(1).toInstant().toEpochMilli())
                .queryParam("intervalLoadProfile", URLEncoder.encode(loadProfilePeriodsInfo))
                .request().get(MonitorValidationInfo.class);

        assertThat(response.detailedValidationLoadProfile.size()).isEqualTo(1);
        assertThat(response.detailedValidationLoadProfile.get(0).total).isEqualTo(3);
        assertThat(response.detailedValidationLoadProfile.get(0).name).isEqualTo("Profile1");
    }

    private void doModelStubbing() {
        when(device.getRegisters()).thenReturn(Arrays.asList(register1));
        when(register1.getReadingType()).thenReturn(regReadingType);
        when(register1.getRegisterSpec()).thenReturn(registerSpec);
        when(registerSpec.getReadingType()).thenReturn(regReadingType);
        when(regReadingType.getMRID()).thenReturn("REG1");
        when(regReadingType.getAliasName()).thenReturn("Sum+");
        when(channelReadingType1.getMRID()).thenReturn("CH1");
        when(channelReadingType2.getMRID()).thenReturn("CH2");

        when(device.getLoadProfiles()).thenReturn(Arrays.asList(loadProfile1));
        when(loadProfile1.getChannels()).thenReturn(Arrays.asList(ch1, ch2));
        when(loadProfile1.getId()).thenReturn(1L);
        when(loadProfile1.getLoadProfileSpec()).thenReturn(loadProfileSpec1);
        when(loadProfileSpec1.getLoadProfileType()).thenReturn(loadProfileType1);
        when(loadProfileType1.getName()).thenReturn("Profile1");

        when(ch1.getReadingType()).thenReturn(channelReadingType1);
        when(ch2.getReadingType()).thenReturn(channelReadingType2);

        doReturn(Arrays.asList(meterActivation1, meterActivation2, meterActivation3)).when(meter).getMeterActivations();
        ZonedDateTime fromReg = ZonedDateTime.ofInstant(NOW, ZoneId.systemDefault()).minusYears(1).truncatedTo(ChronoUnit.DAYS).plusDays(1);
        ZonedDateTime from = ZonedDateTime.ofInstant(NOW, ZoneId.systemDefault()).minusYears(2);
        ZonedDateTime to = ZonedDateTime.ofInstant(NOW, ZoneId.systemDefault()).minusDays(10);
        when(meterActivation1.getInterval()).thenReturn(Interval.endAt(from.toInstant()));
        when(meterActivation2.getInterval()).thenReturn(new Interval(Date.from(from.toInstant()), Date.from(to.toInstant())));
        when(meterActivation3.getInterval()).thenReturn(Interval.startAt(to.toInstant()));
        when(meterActivation1.getChannels()).thenReturn(Arrays.asList(channel1, channel2, channel3));
        when(channel1.getMeterActivation()).thenReturn(meterActivation1);
        when(channel2.getMeterActivation()).thenReturn(meterActivation1);
        when(channel3.getMeterActivation()).thenReturn(meterActivation1);
        when(meterActivation2.getChannels()).thenReturn(Arrays.asList(channel4, channel5, channel6));
        when(channel4.getMeterActivation()).thenReturn(meterActivation2);
        when(channel5.getMeterActivation()).thenReturn(meterActivation2);
        when(channel6.getMeterActivation()).thenReturn(meterActivation2);
        when(meterActivation3.getChannels()).thenReturn(Arrays.asList(channel7, channel8, channel9));
        when(channel7.getMeterActivation()).thenReturn(meterActivation3);
        when(channel8.getMeterActivation()).thenReturn(meterActivation3);
        when(channel9.getMeterActivation()).thenReturn(meterActivation3);
        when(validationService.getLastChecked(meterActivation1)).thenReturn(Optional.of(NOW));
        when(validationService.getLastChecked(meterActivation2)).thenReturn(Optional.of(NOW));
        when(validationService.getLastChecked(meterActivation3)).thenReturn(Optional.of(NOW));
        when(channel1.getMainReadingType()).thenReturn(regReadingType);
        when(channel2.getMainReadingType()).thenReturn(channelReadingType1);
        when(channel3.getMainReadingType()).thenReturn(channelReadingType2);
        when(channel4.getMainReadingType()).thenReturn(regReadingType);
        when(channel5.getMainReadingType()).thenReturn(channelReadingType1);
        when(channel6.getMainReadingType()).thenReturn(channelReadingType2);
        when(channel7.getMainReadingType()).thenReturn(regReadingType);
        when(channel8.getMainReadingType()).thenReturn(channelReadingType1);
        when(channel9.getMainReadingType()).thenReturn(channelReadingType2);
        doReturn(Arrays.asList(regReadingType)).when(channel1).getReadingTypes();
        doReturn(Arrays.asList(channelReadingType1)).when(channel2).getReadingTypes();
        doReturn(Arrays.asList(channelReadingType2)).when(channel3).getReadingTypes();
        doReturn(Arrays.asList(regReadingType)).when(channel4).getReadingTypes();
        doReturn(Arrays.asList(channelReadingType1)).when(channel5).getReadingTypes();
        doReturn(Arrays.asList(channelReadingType2)).when(channel6).getReadingTypes();
        doReturn(Arrays.asList(regReadingType)).when(channel7).getReadingTypes();
        doReturn(Arrays.asList(channelReadingType1)).when(channel8).getReadingTypes();
        doReturn(Arrays.asList(channelReadingType2)).when(channel9).getReadingTypes();
        when(validationService.getEvaluator()).thenReturn(evaluator);
        when(validationService.getEvaluator(eq(meter), any(Range.class))).thenReturn(evaluator);
        when(suspect.getTypeCode()).thenReturn("3.5.258");
        when(notSuspect.getTypeCode()).thenReturn("0.0.0");
        when(suspect.getType()).thenReturn(new ReadingQualityType("3.5.258"));
        when(notSuspect.getType()).thenReturn(new ReadingQualityType("0.0.0"));

        Interval regInterval1 = new Interval(Date.from(fromReg.toInstant()), Date.from(to.toInstant()));
        Instant toNow = ZonedDateTime.ofInstant(NOW, ZoneId.systemDefault()).truncatedTo(ChronoUnit.DAYS).plusDays(1).toInstant();
        Range<Instant> wholeRegInterval = Range.openClosed(fromReg.toInstant(), toNow);
        when(deviceValidation.getValidationStatus(eq(register1), anyList(), eq(wholeRegInterval))).thenReturn(Arrays.asList(validationStatus1, validationStatus2, validationStatus3));
        doReturn(Arrays.asList(suspect, suspect)).when(validationStatus1).getReadingQualities();
        doReturn(Arrays.asList(suspect, suspect)).when(validationStatus2).getReadingQualities();
        doReturn(Arrays.asList(notSuspect, suspect)).when(validationStatus3).getReadingQualities();

        ZonedDateTime fromCh = ZonedDateTime.ofInstant(NOW, ZoneId.systemDefault()).minusMonths(1).truncatedTo(ChronoUnit.DAYS).plusDays(1);
        Range wholeInterval = Range.openClosed(fromCh.toInstant(), toNow);
        when(deviceValidation.getValidationStatus(eq(ch1), anyList(), eq(wholeInterval))).thenReturn(Arrays.asList(validationStatus4));
        when(deviceValidation.getValidationStatus(eq(ch2), anyList(), eq(wholeInterval))).thenReturn(Arrays.asList(validationStatus5, validationStatus6));
        doReturn(Arrays.asList(suspect, suspect)).when(validationStatus4).getReadingQualities();
        doReturn(Arrays.asList(suspect, notSuspect)).when(validationStatus5).getReadingQualities();
        doReturn(Arrays.asList(notSuspect, suspect)).when(validationStatus6).getReadingQualities();

        when(deviceValidation.getLastChecked()).thenReturn(Optional.<Instant>empty());

        ValidationRuleSet ruleSet = mockValidationRuleSet(1,true);
        doReturn(ruleSet.getRules()).when(validationStatus4).getOffendedRules();
        doReturn(ruleSet.getRules()).when(validationStatus2).getOffendedRules();
    }

    private ValidationRuleSet mockValidationRuleSet(int id, boolean version) {
        ValidationRuleSet ruleSet = mock(ValidationRuleSet.class);
        when(ruleSet.getId()).thenReturn(Long.valueOf(id));
        when(ruleSet.getName()).thenReturn("MyName");
        when(ruleSet.getDescription()).thenReturn("MyDescription");
        if (version) {
            ValidationRuleSetVersion ruleSetVersion = mockValidationRuleSetVersion(11, ruleSet);
            List versions = Arrays.asList(ruleSetVersion);
            when(ruleSet.getRuleSetVersions()).thenReturn(versions);

            List rules = Arrays.asList(mockValidationRuleInRuleSetVersion(20, ruleSet, ruleSetVersion));
            when(ruleSetVersion.getRules()).thenReturn(rules);
            when(ruleSet.getRules()).thenReturn(rules);
        }

        doReturn(Optional.of(ruleSet)).when(validationService).getValidationRuleSet(id);

        return ruleSet;
    }

    private ValidationRuleSetVersion mockValidationRuleSetVersion(long id, ValidationRuleSet ruleSet){
        ValidationRuleSetVersion ruleSetVersion = mock(ValidationRuleSetVersion.class);
        when(ruleSetVersion.getDescription()).thenReturn("descriptionOfVersion");
        when(ruleSetVersion.getId()).thenReturn(id);
        when(ruleSetVersion.getStartDate()).thenReturn(ZonedDateTime.ofInstant(NOW, ZoneId.systemDefault()).minusMonths(1).truncatedTo(ChronoUnit.DAYS).plusDays(1).toInstant());
        when(ruleSetVersion.getRuleSet()).thenReturn(ruleSet);
        return ruleSetVersion;
    }

    private ValidationRule mockValidationRuleInRuleSetVersion(long id, ValidationRuleSet ruleSet, ValidationRuleSetVersion ruleSetVersion) {
        ValidationRule rule = mock(ValidationRule.class);
        when(rule.getName()).thenReturn("MyRule");
        when(rule.getId()).thenReturn(id);
        when(rule.getAction()).thenReturn(ValidationAction.FAIL);
        when(rule.getImplementation()).thenReturn("com.blablabla.Validator");
        when(rule.getDisplayName()).thenReturn("My rule");
        when(rule.isActive()).thenReturn(true);
        when(rule.getRuleSetVersion()).thenReturn(ruleSetVersion);
        when(rule.getRuleSet()).thenReturn(ruleSet);


        Map<String, Object> props = new HashMap<>();
        props.put("number", 13);
        props.put("nullableboolean", true);
        props.put("boolean", false);
        props.put("text", "string");
        when(rule.getProps()).thenReturn(props);

        return rule;
    }
}