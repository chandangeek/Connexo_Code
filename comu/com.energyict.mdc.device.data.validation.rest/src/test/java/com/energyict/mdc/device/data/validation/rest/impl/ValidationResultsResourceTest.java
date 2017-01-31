/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.validation.rest.impl;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.energyict.mdc.device.data.validation.DeviceDataValidationService;
import com.energyict.mdc.device.data.validation.DeviceValidationKpiResults;
import com.energyict.mdc.device.data.validation.ValidationOverview;
import com.energyict.mdc.device.data.validation.ValidationOverviews;

import com.google.common.collect.Range;
import com.jayway.jsonpath.JsonModel;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by dragos on 7/21/2015.
 */
public class ValidationResultsResourceTest extends DeviceDataValidationRestApplicationJerseyTest {

    @Mock
    private EndDeviceGroup endDeviceGroup;

    @Before
    public void initializeEndDeviceGroup() {
        when(this.endDeviceGroup.getId()).thenReturn(97L);
        when(meteringGroupsService.findEndDeviceGroup(97L)).thenReturn(Optional.of(this.endDeviceGroup));
    }

    @Test
    public void testValidationOverview() throws UnsupportedEncodingException {
        ValidationOverview o1 = this.mockValidationOverview("ABC123451", "123451", "DT1", "DC1", this.mockResults(2, 1, 1, true, Instant.now(), true, true, true, true));
        ValidationOverview o2 = this.mockValidationOverview("ABC123452", "123452", "DT2", "DC2", this.mockResults(2, 1, 1, true, Instant.now(), true, true, true, true));

        DeviceDataValidationService.ValidationOverviewBuilder builder = mock(DeviceDataValidationService.ValidationOverviewBuilder.class);
        DeviceDataValidationService.ValidationOverviewSuspectsSpecificationBuilder suspectsBuilder = mock(DeviceDataValidationService.ValidationOverviewSuspectsSpecificationBuilder.class);
        when(suspectsBuilder.equalTo(anyInt())).thenReturn(builder);
        when(suspectsBuilder.inRange(any(Range.class))).thenReturn(builder);
        when(builder.excludeAllValidators()).thenReturn(builder);
        when(builder.includeAllValidators()).thenReturn(builder);
        when(builder.includeThresholdValidator()).thenReturn(builder);
        when(builder.includeMissingValuesValidator()).thenReturn(builder);
        when(builder.includeReadingQualitiesValidator()).thenReturn(builder);
        when(builder.includeRegisterIncreaseValidator()).thenReturn(builder);
        when(builder.in(any(Range.class))).thenReturn(builder);
        when(builder.suspects()).thenReturn(suspectsBuilder);
        ValidationOverviews overviews = mock(ValidationOverviews.class);
        when(overviews.allOverviews()).thenReturn(Arrays.asList(o1, o2));
        when(builder.paged(anyInt(), anyInt())).thenReturn(overviews);

        when(deviceDataValidationService.forAllGroups(anyList())).thenReturn(builder);

        JsonModel jsonModel = JsonModel.model(target("/validationresults/devicegroups")
                .queryParam("filter", URLEncoder.encode("[{\"property\":\"deviceGroups\",\"value\":[97]}]", "UTF-8"))
                .queryParam("start", 0)
                .queryParam("limit", 2).request().get(String.class));

        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(2);
        assertThat(jsonModel.<String>get("$.summary[0].name")).isEqualTo("ABC123451");
        assertThat(jsonModel.<String>get("$.summary[0].serialNumber")).isEqualTo("123451");
        assertThat(jsonModel.<String>get("$.summary[0].deviceType")).isEqualTo("DT1");
        assertThat(jsonModel.<String>get("$.summary[0].deviceConfig")).isEqualTo("DC1");
        assertThat(jsonModel.<Boolean>get("$.summary[0].allDataValidated")).isEqualTo(true);
        assertThat(jsonModel.<Boolean>get("$.summary[0].registerIncreaseValidator")).isEqualTo(true);
        assertThat(jsonModel.<Integer>get("$.summary[0].registerSuspects")).isEqualTo(1);
        assertThat(jsonModel.<Integer>get("$.summary[0].amountOfSuspects")).isEqualTo(2);
        assertThat(jsonModel.<Boolean>get("$.summary[0].thresholdValidator")).isEqualTo(true);

        assertThat(jsonModel.<String>get("$.summary[1].name")).isEqualTo("ABC123452");
        assertThat(jsonModel.<String>get("$.summary[1].serialNumber")).isEqualTo("123452");
        assertThat(jsonModel.<String>get("$.summary[1].deviceType")).isEqualTo("DT2");
        assertThat(jsonModel.<String>get("$.summary[1].deviceConfig")).isEqualTo("DC2");
        assertThat(jsonModel.<Boolean>get("$.summary[0].allDataValidated")).isEqualTo(true);
        assertThat(jsonModel.<Boolean>get("$.summary[0].registerIncreaseValidator")).isEqualTo(true);
        assertThat(jsonModel.<Integer>get("$.summary[0].registerSuspects")).isEqualTo(1);
        assertThat(jsonModel.<Integer>get("$.summary[0].amountOfSuspects")).isEqualTo(2);
        assertThat(jsonModel.<Boolean>get("$.summary[0].thresholdValidator")).isEqualTo(true);
    }

    private ValidationOverview mockValidationOverview(String deviceName, String serialNumber, String deviceTypeName, String deviceConfigurationName, DeviceValidationKpiResults kpiResults) {
        ValidationOverview validationOverview = mock(ValidationOverview.class);
        when(validationOverview.getDeviceName()).thenReturn(deviceName);
        when(validationOverview.getDeviceSerialNumber()).thenReturn(serialNumber);
        when(validationOverview.getDeviceTypeName()).thenReturn(deviceTypeName);
        when(validationOverview.getDeviceConfigurationName()).thenReturn(deviceConfigurationName);
        when(validationOverview.getDeviceValidationKpiResults()).thenReturn(kpiResults);
        return validationOverview;
    }

    private DeviceValidationKpiResults mockResults(long amountOfSuspects, long channelSuspects, long registerSuspects, boolean allDataValidated, Instant lastSuspect,
            boolean thresholdValidator, boolean missingValuesValidator, boolean readingQualitiesValidator, boolean registerIncreaseValidator) {
        DeviceValidationKpiResults kpiResults = mock(DeviceValidationKpiResults.class);
        when(kpiResults.getAmountOfSuspects()).thenReturn(amountOfSuspects);
        when(kpiResults.getChannelSuspects()).thenReturn(channelSuspects);
        when(kpiResults.getRegisterSuspects()).thenReturn(registerSuspects);
        when(kpiResults.isAllDataValidated()).thenReturn(allDataValidated);
        when(kpiResults.getLastSuspect()).thenReturn(lastSuspect);
        when(kpiResults.isThresholdValidator()).thenReturn(thresholdValidator);
        when(kpiResults.isMissingValuesValidator()).thenReturn(missingValuesValidator);
        when(kpiResults.isReadingQualitiesValidator()).thenReturn(readingQualitiesValidator);
        when(kpiResults.isRegisterIncreaseValidator()).thenReturn(registerIncreaseValidator);
        return kpiResults;
    }

}