package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.devtools.ExtjsFilter;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.ReadingRecord;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;

import com.google.common.collect.Range;
import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UsagePointOutputResourceRegisterDataTest extends UsagePointDataRestApplicationJerseyTest {

    private static final Instant readingTimeStamp1 = Instant.ofEpochMilli(1410774620100L);
    private static final Instant readingTimeStamp2 = readingTimeStamp1.plus(5, ChronoUnit.MINUTES);
    private static final Instant readingTimeStamp3 = readingTimeStamp2.plus(10, ChronoUnit.MINUTES);

    @Mock
    private UsagePoint usagePoint;
    @Mock
    EffectiveMetrologyConfigurationOnUsagePoint effectiveMC;
    @Mock
    private ReadingRecord readingRecord1, readingRecord2, readingRecord3;

    @Before
    public void before() {
        when(meteringService.findUsagePoint(any())).thenReturn(Optional.empty());
        when(meteringService.findUsagePoint("MRID")).thenReturn(Optional.of(usagePoint));
        UsagePointMetrologyConfiguration metrologyConfiguration = mockMetrologyConfigurationWithContract(1, "mc");
        when(usagePoint.getMetrologyConfiguration()).thenReturn(Optional.of(metrologyConfiguration));
        when(usagePoint.getEffectiveMetrologyConfiguration()).thenReturn(Optional.of(effectiveMC));
        when(effectiveMC.getMetrologyConfiguration()).thenReturn(metrologyConfiguration);
        when(effectiveMC.getUsagePoint()).thenReturn(usagePoint);

        when(readingRecord1.getValue()).thenReturn(BigDecimal.valueOf(200, 0));
        when(readingRecord1.getTimeStamp()).thenReturn(readingTimeStamp1);
        when(readingRecord2.getValue()).thenReturn(BigDecimal.valueOf(206, 0));
        when(readingRecord2.getTimeStamp()).thenReturn(readingTimeStamp2);
        when(readingRecord3.getValue()).thenReturn(BigDecimal.valueOf(250, 0));
        when(readingRecord3.getTimeStamp()).thenReturn(readingTimeStamp3);
    }

    private String buildFilter() throws UnsupportedEncodingException {
        return ExtjsFilter.filter()
                .property("intervalStart", readingTimeStamp1.toEpochMilli())
                .property("intervalEnd", readingTimeStamp3.toEpochMilli())
                .create();
    }

    @Test
    public void testGetRegisterDataNoSuchUsagePoint() throws Exception {
        // Business method
        Response response = target("/usagepoints/xxx/purposes/1/outputs/1/registerData").queryParam("filter", buildFilter()).request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetRegisterDataNoMetrologyConfigurationOnUsagePoint() throws Exception {
        when(usagePoint.getEffectiveMetrologyConfiguration()).thenReturn(Optional.empty());

        // Business method
        Response response = target("/usagepoints/MRID/purposes/1/outputs/1/registerData").queryParam("filter", buildFilter()).request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetRegisterDataNoSuchContract() throws Exception {
        // Business method
        Response response = target("/usagepoints/MRID/purposes/90030004443343/outputs/1/registerData").queryParam("filter", buildFilter()).request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetRegisterDataOnRegularReadingTypeDeliverable() throws Exception {
        // Business method
        Response response = target("/usagepoints/MRID/purposes/1/outputs/1/registerData").queryParam("filter", buildFilter()).request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetRegisterOutputData() throws Exception {
        ChannelsContainer channelsContainer = mock(ChannelsContainer.class);
        when(effectiveMC.getChannelsContainer(any())).thenReturn(Optional.of(channelsContainer));
        Channel channel = mock(Channel.class);
        when(channelsContainer.getChannel(any())).thenReturn(Optional.of(channel));
        when(channel.getRegisterReadings(Range.openClosed(readingTimeStamp1, readingTimeStamp3))).thenReturn(Arrays.asList(readingRecord1, readingRecord2, readingRecord3));

        // Business method
        String json = target("/usagepoints/MRID/purposes/1/outputs/2/registerData").queryParam("filter", buildFilter()).request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(3);
        assertThat(jsonModel.<List<Number>>get("$registerData[*].timeStamp")).containsExactly(
                readingTimeStamp3.toEpochMilli(), readingTimeStamp2.toEpochMilli(), readingTimeStamp1.toEpochMilli());
        assertThat(jsonModel.<List<String>>get("$registerData[*].value")).containsExactly("250", "206", "200");
    }
}
