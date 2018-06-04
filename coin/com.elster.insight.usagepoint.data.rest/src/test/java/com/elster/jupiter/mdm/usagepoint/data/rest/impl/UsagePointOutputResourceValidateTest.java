/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.cbo.QualityCodeSystem;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.ValidationContextImpl;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;
import com.elster.jupiter.validation.ValidationRuleSetVersion;

import com.google.common.collect.Range;
import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UsagePointOutputResourceValidateTest extends UsagePointOutputResourceTest {

    private Range<Instant> range1;
    private Range<Instant> range2;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        when(meteringService.findAndLockUsagePointByIdAndVersion(usagePoint.getId(), usagePoint.getVersion())).thenReturn(Optional.of(usagePoint));
        when(validationService.getLastChecked(channelsContainer1)).thenReturn(Optional.of(INSTANT));
        when(validationService.getLastChecked(channelsContainer2)).thenReturn(Optional.of(PREVIOUS_INSTANT));
        when(validationService.isValidationActive(channelsContainer1)).thenReturn(true);
        when(validationService.isValidationActive(channelsContainer2)).thenReturn(true);
        range1 = Range.atLeast(PREVIOUS_INSTANT);
        when(channelsContainer1.getInterval()).thenReturn(Interval.of(range1));
        range2 = Range.closedOpen(OLD_INSTANT, PREVIOUS_INSTANT);
        when(channelsContainer2.getInterval()).thenReturn(Interval.of(range2));
    }

    @Test
    public void testValidatePurposeOnRequest() {
        mockValidationRuleSet(mandatoryContract1, channelsContainer1, range1);
        mockValidationRuleSet(mandatoryContract2, channelsContainer2, range1);
        PurposeInfo purposeInfo = createPurposeInfo(mandatoryContract1, OLD_INSTANT);
        // Business method
        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/purposes/100/validate").request().put(Entity.json(purposeInfo));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(validationService).getLastChecked(channelsContainer1);
        verify(validationService).validate(
                refEq(new ValidationContextImpl(EnumSet.of(QualityCodeSystem.MDM), channelsContainer1, mandatoryContract1)),
                eq(OLD_INSTANT.plusMillis(1)));
        verify(validationService).getLastChecked(channelsContainer2);
        verify(validationService).validate(
                refEq(new ValidationContextImpl(EnumSet.of(QualityCodeSystem.MDM), channelsContainer2, mandatoryContract2)),
                eq(OLD_INSTANT.plusMillis(1)));
    }

    @Test
    public void testOnlyOneContractValidatedDueToLastChecked() {
        mockValidationRuleSet(mandatoryContract1, channelsContainer1, range1);
        mockValidationRuleSet(mandatoryContract2, channelsContainer2, range1);
        PurposeInfo purposeInfo = createPurposeInfo(mandatoryContract1, NEXT_INSTANT);
        // Business method
        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/purposes/100/validate").request().put(Entity.json(purposeInfo));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(validationService).getLastChecked(channelsContainer1);
        verify(validationService).getLastChecked(channelsContainer2);
        verify(validationService).validate(
                refEq(new ValidationContextImpl(EnumSet.of(QualityCodeSystem.MDM), channelsContainer1, mandatoryContract1)),
                eq(INSTANT.plusMillis(1)));
    }

    @Test
    public void testOnlyOneContractValidatedDueToRuleSetVersions() {
        mockValidationRuleSet(mandatoryContract1, channelsContainer1, range2);
        mockValidationRuleSet(mandatoryContract2, channelsContainer2, range2);
        PurposeInfo purposeInfo = createPurposeInfo(mandatoryContract1, OLD_INSTANT);
        // Business method
        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/purposes/100/validate").request().put(Entity.json(purposeInfo));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(validationService).getLastChecked(channelsContainer1);
        verify(validationService).getLastChecked(channelsContainer2);
        verify(validationService).validate(
                refEq(new ValidationContextImpl(EnumSet.of(QualityCodeSystem.MDM), channelsContainer2, mandatoryContract2)),
                eq(OLD_INSTANT.plusMillis(1)));
    }

    @Test
    public void whenNoActiveRuleSetVersions_thenNothingToValidate() throws IOException {
        range1 = Range.greaterThan(PREVIOUS_INSTANT);
        when(channelsContainer1.getInterval()).thenReturn(Interval.of(range1));
        range2 = Range.openClosed(OLD_INSTANT, PREVIOUS_INSTANT);
        when(channelsContainer2.getInterval()).thenReturn(Interval.of(range2));
        mockValidationRuleSet(mandatoryContract1, channelsContainer1, range2);
        mockValidationRuleSet(mandatoryContract2, channelsContainer2, range1);
        PurposeInfo purposeInfo = createPurposeInfo(mandatoryContract1, OLD_INSTANT);
        // Business method
        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/purposes/100/validate").request().put(Entity.json(purposeInfo));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        verify(validationService).getLastChecked(channelsContainer1);
        verify(validationService).getLastChecked(channelsContainer2);

        JsonModel model = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(model.<Boolean>get("$.success")).isFalse();
        assertThat(model.<List<String>>get("$.errors[*].id")).containsExactly("validationInfo.lastChecked");
        assertThat(model.<List<String>>get("$.errors[*].msg")).containsExactly(MessageSeeds.NOTHING_TO_VALIDATE.getDefaultFormat());
    }

    @Test
    public void whenNoRuleSetsConfigured_thenNothingToValidate() throws IOException {
        when(usagePointConfigurationService.getValidationRuleSets(any(MetrologyContract.class))).thenReturn(Collections.emptyList());
        PurposeInfo purposeInfo = createPurposeInfo(mandatoryContract1, OLD_INSTANT);
        // Business method
        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/purposes/100/validate").request().put(Entity.json(purposeInfo));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        verify(validationService).getLastChecked(channelsContainer1);
        verify(validationService).getLastChecked(channelsContainer2);

        JsonModel model = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(model.<Boolean>get("$.success")).isFalse();
        assertThat(model.<List<String>>get("$.errors[*].id")).containsExactly("validationInfo.lastChecked");
        assertThat(model.<List<String>>get("$.errors[*].msg")).containsExactly(MessageSeeds.NOTHING_TO_VALIDATE.getDefaultFormat());
    }

    @Test
    public void whenInactiveConfiguration_thenNothingToValidate() throws IOException {
        mockValidationRuleSet(mandatoryContract1, channelsContainer1, range2);
        mockValidationRuleSet(mandatoryContract2, channelsContainer2, range2);
        when(validationService.isValidationActive(channelsContainer1)).thenReturn(false);
        when(validationService.isValidationActive(channelsContainer2)).thenReturn(false);
        PurposeInfo purposeInfo = createPurposeInfo(mandatoryContract1, OLD_INSTANT);

        // Business method
        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/purposes/100/validate").request().put(Entity.json(purposeInfo));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

        JsonModel model = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(model.<Boolean>get("$.success")).isFalse();
        assertThat(model.<List<String>>get("$.errors[*].id")).containsExactly("validationInfo.lastChecked");
        assertThat(model.<List<String>>get("$.errors[*].msg")).containsExactly(MessageSeeds.NOTHING_TO_VALIDATE.getDefaultFormat());

    }

    @Test
    public void whenNoActiveRuleSets_thenNothingToValidate() throws IOException {
        mockValidationRuleSet(mandatoryContract1, channelsContainer1, range2);
        mockValidationRuleSet(mandatoryContract2, channelsContainer2, range2);
        when(usagePointConfigurationService.getActiveValidationRuleSets(mandatoryContract1, channelsContainer1)).thenReturn(Collections.emptyList());
        when(usagePointConfigurationService.getActiveValidationRuleSets(mandatoryContract2, channelsContainer2)).thenReturn(Collections.emptyList());

        PurposeInfo purposeInfo = createPurposeInfo(mandatoryContract1, OLD_INSTANT);

        // Business method
        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/purposes/100/validate").request().put(Entity.json(purposeInfo));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());

        JsonModel model = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(model.<Boolean>get("$.success")).isFalse();
        assertThat(model.<List<String>>get("$.errors[*].id")).containsExactly("validationInfo.lastChecked");
        assertThat(model.<List<String>>get("$.errors[*].msg")).containsExactly(MessageSeeds.NOTHING_TO_VALIDATE.getDefaultFormat());
    }

    private void mockValidationRuleSet(MetrologyContract metrologyContract, ChannelsContainer channelsContainer, Range<Instant> active) {
        ValidationRuleSet validationRuleSet = mock(ValidationRuleSet.class);
        when(usagePointConfigurationService.getValidationRuleSets(metrologyContract)).thenReturn(Collections.singletonList(validationRuleSet));
        when(usagePointConfigurationService.getActiveValidationRuleSets(metrologyContract, channelsContainer)).thenReturn(Collections.singletonList(validationRuleSet));
        mockValidationRuleSetVersion(validationRuleSet, metrologyContract, active);
    }

    private void mockValidationRuleSetVersion(ValidationRuleSet validationRuleSet, MetrologyContract metrologyContract, Range<Instant> active) {
        ValidationRuleSetVersion validationRuleSetVersion = mock(ValidationRuleSetVersion.class);
        doReturn(Collections.singletonList(validationRuleSetVersion)).when(validationRuleSet).getRuleSetVersions();
        when(validationRuleSetVersion.getRange()).thenReturn(active);
        mockValidationRule(validationRuleSetVersion, metrologyContract);
    }

    private void mockValidationRule(ValidationRuleSetVersion validationRuleSetVersion, MetrologyContract metrologyContract) {
        ValidationRule validationRule = mock(ValidationRule.class);
        doReturn(Collections.singletonList(validationRule)).when(validationRuleSetVersion).getRules();
        Set<ReadingType> readingTypes = metrologyContract.getDeliverables().stream()
                .map(ReadingTypeDeliverable::getReadingType)
                .collect(Collectors.toSet());
        doReturn(Collections.singletonList(validationRule)).when(validationRuleSetVersion).getRules(readingTypes);
        when(validationRule.isActive()).thenReturn(true);
    }
}
