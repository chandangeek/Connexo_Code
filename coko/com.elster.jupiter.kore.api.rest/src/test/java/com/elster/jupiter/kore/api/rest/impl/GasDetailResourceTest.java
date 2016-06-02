package com.elster.jupiter.kore.api.rest.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.GasDetail;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.rest.util.hypermedia.Relation;

import com.google.common.collect.Range;
import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GasDetailResourceTest extends PlatformPublicApiJerseyTest {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        Finder<UsagePoint> finder = mockFinder(Collections.emptyList());
        when(meteringService.getUsagePoints(any())).thenReturn(finder);
    }

    @Test
    public void testGetDetailsWithTemporalLinks() throws Exception {
        clock = Clock.fixed(Instant.ofEpochMilli(200), ZoneId.systemDefault());
        UsagePoint usagePoint = mockUsagePoint(31L, "usage point", 2L, ServiceKind.GAS);
        GasDetail gasDetail1 = mock(GasDetail.class);
        when(gasDetail1.getUsagePoint()).thenReturn(usagePoint);
        when(gasDetail1.getRange()).thenReturn(Range.closedOpen(Instant.ofEpochMilli(100), Instant.ofEpochMilli(200)));
        GasDetail gasDetail2 = mock(GasDetail.class);
        when(gasDetail2.getUsagePoint()).thenReturn(usagePoint);
        when(gasDetail2.getRange()).thenReturn(Range.closedOpen(Instant.ofEpochMilli(200), Instant.ofEpochMilli(300)));
        GasDetail gasDetail3 = mock(GasDetail.class);
        when(gasDetail3.getUsagePoint()).thenReturn(usagePoint);
        when(gasDetail3.getRange()).thenReturn(Range.closedOpen(Instant.ofEpochMilli(300), Instant.ofEpochMilli(400)));
        doReturn(Arrays.asList(gasDetail1, gasDetail2, gasDetail3)).when(usagePoint).getDetail(any(Range.class));
        doReturn(Optional.of(gasDetail2)).when(usagePoint).getDetail(any(Instant.class));
        Response response = target("/usagepoints/31/details/1").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<String>get("$.link[0].href")).isEqualTo("http://localhost:9998/usagepoints/31/details/300");
        assertThat(model.<String>get("$.link[0].params.rel")).isEqualTo(Relation.REF_PREVIOUS.rel());
        assertThat(model.<String>get("$.link[1].href")).isEqualTo("http://localhost:9998/usagepoints/31/details/200");
        assertThat(model.<String>get("$.link[1].params.rel")).isEqualTo(Relation.REF_SELF.rel());
        assertThat(model.<String>get("$.link[2].href")).isEqualTo("http://localhost:9998/usagepoints/31/details/100");
        assertThat(model.<String>get("$.link[2].params.rel")).isEqualTo(Relation.REF_NEXT.rel());

    }

}
