package com.energyict.mdc.device.data.validation.rest.impl;

import com.energyict.mdc.device.data.validation.DeviceValidationKpiResults;
import com.energyict.mdc.device.data.validation.ValidationOverview;
import com.energyict.mdc.device.data.validation.impl.ValidationOverviewImpl;

import com.google.common.collect.Range;
import com.jayway.jsonpath.JsonModel;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Created by dragos on 7/21/2015.
 */
public class ValidationResultsResourceTest extends DeviceDataValidationRestApplicationJerseyTest {

    @Test
    public void testValidationOverview() {

        List<ValidationOverview> list = new ArrayList<>(2);
        list.add(new ValidationOverviewImpl("ABC123451", "123451", "DT1", "DC1", new DeviceValidationKpiResults(2, 1, 1, 1, Instant.now())));
        list.add(new ValidationOverviewImpl("ABC123452", "123452", "DT2", "DC2", new DeviceValidationKpiResults(2, 1, 1, 1, Instant.now())));

        when(deviceDataValidationService.getValidationResultsOfDeviceGroup(1L, Range.all())).thenReturn(list);

        JsonModel jsonModel = JsonModel.model(target("/validationresults/devicegroups/1").queryParam("start", 0).queryParam("limit", 2).request().get(String.class));

        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(2);
        assertThat(jsonModel.<String>get("$.summary[0].mrid")).isEqualTo("ABC123451");
        assertThat(jsonModel.<String>get("$.summary[0].serialNumber")).isEqualTo("123451");
        assertThat(jsonModel.<String>get("$.summary[0].deviceType")).isEqualTo("DT1");
        assertThat(jsonModel.<String>get("$.summary[0].deviceConfig")).isEqualTo("DC1");

        assertThat(jsonModel.<String>get("$.summary[1].mrid")).isEqualTo("ABC123452");
        assertThat(jsonModel.<String>get("$.summary[1].serialNumber")).isEqualTo("123452");
        assertThat(jsonModel.<String>get("$.summary[1].deviceType")).isEqualTo("DT2");
        assertThat(jsonModel.<String>get("$.summary[1].deviceConfig")).isEqualTo("DC2");
    }
}
