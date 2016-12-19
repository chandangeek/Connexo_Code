package com.energyict.mdc.device.alarms.rest;


import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.issue.rest.response.device.MeterShortInfo;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterFilter;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MeterResourceTest extends DeviceAlarmApplicationTest {

    @Test
    public void testGetMetersWithoutLike() {
        List<Meter> meters = new ArrayList<>();
        meters.add(mockMeter(1, "0.0.0.2"));
        meters.add(mockMeter(2, "0.0.0.8"));
        meters.add(mockMeter(2, "0.1.0.8"));

        Finder<Meter> finder = mock(Finder.class);
        when(meteringService.findMeters(any(MeterFilter.class))).thenReturn(finder);
        when(finder.paged(0, 10)).thenReturn(finder);
        when(finder.find()).thenReturn(meters);

        List<MeterShortInfo> meterShortInfos = target("/meters")
                .queryParam("start", 0)
                .queryParam("limit", 10).request().get(List.class);

        // Asserts
        assertThat(meterShortInfos.size()).isEqualTo(3);
        ArgumentCaptor<MeterFilter> argument = ArgumentCaptor.forClass(MeterFilter.class);
        verify(meteringService).findMeters(argument.capture());
        assertThat(argument.getValue().getName()).isEqualTo("*");
        assertThat(argument.getValue().getExcludedStates()).isEmpty();
    }

    @Test
    public void testGetMeters() {
        List<Meter> meters = new ArrayList<>();
        meters.add(mockMeter(1, "0.0.1.2"));
        meters.add(mockMeter(2, "0.0.1.8"));

        Finder<Meter> finder = mock(Finder.class);
        when(meteringService.findMeters(any(MeterFilter.class))).thenReturn(finder);
        when(finder.paged(0, 10)).thenReturn(finder);
        when(finder.find()).thenReturn(meters);

        // Business method
        List<MeterShortInfo> meterShortInfos = target("/meters")
                .queryParam("start", 0)
                .queryParam("limit", 10)
                .queryParam("like", "0.0.").request().get(List.class);

        // Asserts
        ArgumentCaptor<MeterFilter> argument = ArgumentCaptor.forClass(MeterFilter.class);
        verify(meteringService).findMeters(argument.capture());
        assertThat(argument.getValue().getName()).isEqualTo("*0.0.*");
        assertThat(argument.getValue().getExcludedStates()).isEmpty();

        assertThat(meterShortInfos.size()).isEqualTo(2);
    }

    @Test
    public void testGetMeterUnexisting() {
        when(meteringService.findMeterByName("test_meter")).thenReturn(Optional.empty());
        Response response = target("/meters/test_meter").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }
}
