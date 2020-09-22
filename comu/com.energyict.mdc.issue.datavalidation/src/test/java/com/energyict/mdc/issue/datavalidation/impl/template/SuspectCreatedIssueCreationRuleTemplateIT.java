package com.energyict.mdc.issue.datavalidation.impl.template;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.readings.Reading;
import com.elster.jupiter.metering.readings.beans.MeterReadingImpl;
import com.elster.jupiter.metering.readings.beans.ReadingImpl;
import com.elster.jupiter.time.DefaultRelativePeriodDefinition;
import com.elster.jupiter.time.RelativePeriod;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.validation.ValidationRule;
import com.energyict.mdc.common.device.config.DeviceConfiguration;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.issue.datavalidation.DataValidationIssueFilter;
import com.energyict.mdc.issue.datavalidation.IssueDataValidationService;
import com.energyict.mdc.issue.datavalidation.OpenIssueDataValidation;
import com.energyict.mdc.issue.datavalidation.impl.InMemoryIntegrationPersistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.Period;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.BeforeClass;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SuspectCreatedIssueCreationRuleTemplateIT extends BaseTemplateIT {
    private static final int THRESHOLD = 2;
    private static final Instant CREATION_DATE = TIME.minus(Period.ofWeeks(3));
    private static final Instant READING_FIRST = CREATION_DATE.plus(Period.ofDays(1));
    private static final Instant SUSPECT_FIRST = TIME.minus(Period.ofDays(8));
    private static final Instant SUSPECT_SECOND = TIME.minus(Period.ofDays(6));
    private static final Instant SUSPECT_THIRD = TIME.minus(Period.ofDays(4));
    private static final Instant SUSPECT_FORTH = TIME.minus(Period.ofDays(2));

    private static DeviceType deviceType;
    private static DeviceConfiguration deviceConfiguration;
    private static Meter meter;
    private static Channel channel;
    private static ReadingType readingType;
    private static ValidationRule validationRule;
    private static RelativePeriod relativePeriod;

    @BeforeClass
    public static void setUp() throws Exception {
        IN_MEMORY_PERSISTENCE.getTransactionService().run(() -> {
            // Creating device types, configuration, reading types and etc.
            readingType = meteringService.getReadingType(InMemoryIntegrationPersistence.READING_TYPE_MRID).get();
            deviceType = createDeviceType(readingType);
            deviceConfiguration = createDeviceConfiguration(deviceType, "Default");
            validationRule = createValidationRule("Test rule");
            deviceConfiguration.addValidationRuleSet(validationRule.getRuleSet());
            meter = createMeter(deviceConfiguration, "Test Meter", CREATION_DATE);
            validationService.activateValidation(meter);
            validationService.enableValidationOnStorage(meter);
            channel = meter.getCurrentMeterActivation().get().getChannelsContainer().getChannel(readingType).get();
            relativePeriod = getService(TimeService.class).findRelativePeriodByName(DefaultRelativePeriodDefinition.LAST_7_DAYS.getPeriodName()).get();

            createRuleForDeviceConfiguration("Default Rule", SuspectCreatedIssueCreationRuleTemplate.NAME, deviceType,
                    Collections.singletonList(deviceConfiguration), Collections.singletonList(validationRule), relativePeriod, THRESHOLD);
        });

        // Verifying that drools rules are compiled without errors
        assertThat(issueCreationService.reReadRules()).as("Drools compilation of the rule: there are errors").isTrue();
    }

    @Test
    public void testReturnIssueTypeDataValidation() {
        final IssueType actualIssueType = suspectCreatedIssueCreationRuleTemplate.getIssueType();
        final IssueType expectedIssueType = issueService.findIssueType(IssueDataValidationService.ISSUE_TYPE_NAME).get();

        assertThat(actualIssueType.getId()).isEqualTo(expectedIssueType.getId());
    }

    @Test
    public void testCreateAndUpdateIssue() {
        assertThat(findOpenIssues()).isEmpty();

        addReading(channel, READING_FIRST, 13L);
        // no suspects => no issue event
        addReading(channel, SUSPECT_FIRST, 12L);
        // first suspect is out of the interval of interest
        waitWhileIssueEventIsBeingProcessed();
        assertThat(findOpenIssues()).isEmpty();

        addReading(channel, SUSPECT_SECOND, 11L);
        waitWhileIssueEventIsBeingProcessed();
        assertThat(findOpenIssues()).isEmpty();

        addReading(channel, SUSPECT_THIRD, 10L);
        waitWhileIssueEventIsBeingProcessed();
        List<OpenIssueDataValidation> issues = findOpenIssues();
        assertThat(issues).hasSize(1);
        OpenIssueDataValidation openIssueDataValidation = issues.get(0);
        assertThat(openIssueDataValidation.getLastSuspectOccurrenceDatetime()).isEqualTo(SUSPECT_THIRD);
        assertThat(openIssueDataValidation.getTotalOccurrencesNumber()).isEqualTo(2);

        addReading(channel, SUSPECT_FORTH, 9L);
        waitWhileIssueEventIsBeingProcessed();
        issues = findOpenIssues();
        assertThat(issues).hasSize(1);
        openIssueDataValidation = issues.get(0);
        assertThat(openIssueDataValidation.getLastSuspectOccurrenceDatetime()).isEqualTo(SUSPECT_FORTH);
        assertThat(openIssueDataValidation.getTotalOccurrencesNumber()).isEqualTo(3);
    }

    private static void addReading(Channel channel, Instant time, Long value) {
        getService(TransactionService.class).run(() -> {
            MeterReadingImpl meterReading = MeterReadingImpl.newInstance();
            Reading reading = ReadingImpl.of(channel.getMainReadingType().getMRID(), BigDecimal.valueOf(value), time);
            meterReading.addReading(reading);
            channel.getChannelsContainer().getMeter().get().store(QualityCodeSystem.MDC, meterReading);
        });
    }

    private static List<OpenIssueDataValidation> findOpenIssues() {
        DataValidationIssueFilter filter = new DataValidationIssueFilter();
        Stream.of(IssueStatus.OPEN, IssueStatus.IN_PROGRESS, IssueStatus.SNOOZED)
                .map(issueService::findStatus)
                .map(Optional::get)
                .forEach(filter::addStatus);
        return issueDataValidationService.findAllDataValidationIssues(filter).stream()
                .filter(OpenIssueDataValidation.class::isInstance)
                .map(OpenIssueDataValidation.class::cast)
                .collect(Collectors.toList());
    }
}
