/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.validation.rest.impl;

import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.energyict.mdc.device.dataquality.DataQualityOverview;
import com.energyict.mdc.device.dataquality.DataQualityOverviews;
import com.energyict.mdc.device.dataquality.DeviceDataQualityKpiResults;
import com.energyict.mdc.device.dataquality.DeviceDataQualityService;

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
        DataQualityOverview o1 = this.mockValidationOverview("ABC123451", "123451", "DT1", "DC1", this.mockResults(2, 1, 1, true, Instant.now(), true, true, true, true));
        DataQualityOverview o2 = this.mockValidationOverview("ABC123452", "123452", "DT2", "DC2", this.mockResults(2, 1, 1, true, Instant.now(), true, true, true, true));

        DeviceDataQualityService.DataQualityOverviewBuilder builder = mock(DeviceDataQualityService.DataQualityOverviewBuilder.class);
        DeviceDataQualityService.MetricSpecificationBuilder suspectsBuilder = mock(DeviceDataQualityService.MetricSpecificationBuilder.class);
        when(suspectsBuilder.equalTo(anyInt())).thenReturn(builder);
        when(suspectsBuilder.inRange(any(Range.class))).thenReturn(builder);
//        when(builder.excludeAllValidators()).thenReturn(builder);
//        when(builder.includeAllValidators()).thenReturn(builder);
//        when(builder.includeThresholdValidator()).thenReturn(builder);
//        when(builder.includeMissingValuesValidator()).thenReturn(builder);
//        when(builder.includeReadingQualitiesValidator()).thenReturn(builder);
//        when(builder.includeRegisterIncreaseValidator()).thenReturn(builder);
        when(builder.in(any(Range.class))).thenReturn(builder);
        when(builder.suspects()).thenReturn(suspectsBuilder);
        DataQualityOverviews overviews = mock(DataQualityOverviews.class);
        when(overviews.allOverviews()).thenReturn(Arrays.asList(o1, o2));
        when(builder.paged(anyInt(), anyInt())).thenReturn(overviews);

//        when(deviceDataQualityService.forAllGroups(anyList())).thenReturn(builder);

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

    private DataQualityOverview mockValidationOverview(String deviceName, String serialNumber, String deviceTypeName, String deviceConfigurationName, DeviceDataQualityKpiResults kpiResults) {
        DataQualityOverview dataQualityOverview = mock(DataQualityOverview.class);
//        when(dataQualityOverview.getDeviceName()).thenReturn(deviceName);
//        when(dataQualityOverview.getDeviceSerialNumber()).thenReturn(serialNumber);
//        when(dataQualityOverview.getDeviceTypeName()).thenReturn(deviceTypeName);
//        when(dataQualityOverview.getDeviceConfigurationName()).thenReturn(deviceConfigurationName);
        when(dataQualityOverview.getDataQualityKpiResults()).thenReturn(kpiResults);
        return dataQualityOverview;
    }

    private DeviceDataQualityKpiResults mockResults(long amountOfSuspects, long channelSuspects, long registerSuspects, boolean allDataValidated, Instant lastSuspect,
                                                    boolean thresholdValidator, boolean missingValuesValidator, boolean readingQualitiesValidator, boolean registerIncreaseValidator) {
        DeviceDataQualityKpiResults kpiResults = mock(DeviceDataQualityKpiResults.class);
        when(kpiResults.getAmountOfSuspects()).thenReturn(amountOfSuspects);
        when(kpiResults.getChannelSuspects()).thenReturn(channelSuspects);
        when(kpiResults.getRegisterSuspects()).thenReturn(registerSuspects);
//        when(kpiResults.isAllDataValidated()).thenReturn(allDataValidated);
//        when(kpiResults.getLastSuspect()).thenReturn(lastSuspect);
//        when(kpiResults.isThresholdValidator()).thenReturn(thresholdValidator);
//        when(kpiResults.isMissingValuesValidator()).thenReturn(missingValuesValidator);
//        when(kpiResults.isReadingQualitiesValidator()).thenReturn(readingQualitiesValidator);
//        when(kpiResults.isRegisterIncreaseValidator()).thenReturn(registerIncreaseValidator);
        return kpiResults;
    }

}