package com.elster.jupiter.kore.api.rest.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.kore.api.impl.GasDetailInfo;
import com.elster.jupiter.metering.BypassStatus;
import com.elster.jupiter.metering.GasDetail;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.rest.util.hypermedia.Relation;
import com.elster.jupiter.util.YesNoAnswer;
import com.elster.jupiter.util.units.Quantity;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;
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
    public void testUpdateGasUsagePointMissingEffectivity() throws Exception {
        Instant now = Instant.now(clock);
        GasDetailInfo info = new GasDetailInfo();
        info.id = now;
        info.version = 2L;

        info.effectivity = null;

        UsagePoint usagePoint = mockUsagePoint(11L, "usage point", 2L, ServiceKind.GAS);

        Response response = target("/usagepoints/11/details").request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.create((InputStream) response.getEntity());
        assertThat(jsonModel.<String>get("$.errors[0].id")).isEqualTo("effectivity.lowerEnd");
    }

    @Test
    public void testUpdateGasUsagePointMissingVersion() throws Exception {
        Instant now = Instant.now(clock);
        GasDetailInfo info = new GasDetailInfo();
        info.id = now;
        info.version = null;

        UsagePoint usagePoint = mockUsagePoint(11L, "usage point", 2L, ServiceKind.GAS);

        Response response = target("/usagepoints/11/details").request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.create((InputStream) response.getEntity());
        assertThat(jsonModel.<String>get("$.errors[0].id")).isEqualTo("version");
    }


    @Test
    public void testGetDetailsWithTemporalLinks() throws Exception {
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
        doReturn(Arrays.asList(gasDetail3, gasDetail2, gasDetail1)).when(usagePoint).getDetail(any(Range.class));
        doReturn(Optional.of(gasDetail2)).when(usagePoint).getDetail(any(Instant.class));
        Response response = target("/usagepoints/31/details/1").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<String>get("$.link[0].href")).isEqualTo("http://localhost:9998/usagepoints/31/details/100");
        assertThat(model.<String>get("$.link[0].params.rel")).isEqualTo(Relation.REF_PREVIOUS.rel());
        assertThat(model.<String>get("$.link[1].href")).isEqualTo("http://localhost:9998/usagepoints/31/details/200");
        assertThat(model.<String>get("$.link[1].params.rel")).isEqualTo(Relation.REF_SELF.rel());
        assertThat(model.<String>get("$.link[2].href")).isEqualTo("http://localhost:9998/usagepoints/31/details/300");
        assertThat(model.<String>get("$.link[2].params.rel")).isEqualTo(Relation.REF_NEXT.rel());
    }

    @Test
    public void testGetSingleGasUsagePointAllFields() throws Exception {
        GasDetail gasDetail = mock(GasDetail.class);
        when(gasDetail.getBypassStatus()).thenReturn(BypassStatus.CLOSED);
        BigDecimal value1 = BigDecimal.valueOf(101);
        BigDecimal value2 = BigDecimal.valueOf(102);
        BigDecimal value3 = BigDecimal.valueOf(103);
        when(gasDetail.getLoadLimit()).thenReturn(Quantity.create(value1, "W"));
        when(gasDetail.getLoadLimiterType()).thenReturn("LLT");
        when(gasDetail.getPhysicalCapacity()).thenReturn(Quantity.create(value2, "m"));
        when(gasDetail.getPressure()).thenReturn(Quantity.create(value3, "kg"));
        when(gasDetail.isBypassInstalled()).thenReturn(YesNoAnswer.YES);
        when(gasDetail.isCapped()).thenReturn(YesNoAnswer.YES);
        when(gasDetail.isClamped()).thenReturn(YesNoAnswer.YES);
        when(gasDetail.isCurrent()).thenReturn(Boolean.TRUE);
        when(gasDetail.isGrounded()).thenReturn(YesNoAnswer.YES);
        when(gasDetail.isLimiter()).thenReturn(YesNoAnswer.YES);
        when(gasDetail.isValveInstalled()).thenReturn(YesNoAnswer.YES);
        when(gasDetail.isInterruptible()).thenReturn(YesNoAnswer.YES);
        when(gasDetail.isCollarInstalled()).thenReturn(YesNoAnswer.YES);
        when(gasDetail.getRange()).thenReturn(Range.downTo(clock.instant(), BoundType.CLOSED));
        UsagePoint usagePoint = mockUsagePoint(31L, "usage point", 2L, ServiceKind.GAS, gasDetail);
        UsagePointMetrologyConfiguration metrologyConfiguration = mockMetrologyConfiguration(13L, "metro", 1);
        when(usagePoint.getMetrologyConfiguration()).thenReturn(Optional.of(metrologyConfiguration));
        Response response = target("/usagepoints/31/details").request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        Assertions.assertThat(model.<Long>get("$.id")).isEqualTo(clock.millis());
        Assertions.assertThat(model.<Integer>get("$.version")).isEqualTo(2);
        Assertions.assertThat(model.<Long>get("$.effectivity.lowerEnd")).isEqualTo(clock.millis());
        Assertions.assertThat(model.<Long>get("$.effectivity.upperEnd")).isNull();
        Assertions.assertThat(model.<String>get("$.bypass")).isEqualTo("YES");
        Assertions.assertThat(model.<String>get("$.capped")).isEqualTo("YES");
        Assertions.assertThat(model.<String>get("$.collar")).isEqualTo("YES");
        Assertions.assertThat(model.<Boolean>get("$.current")).isTrue();
        Assertions.assertThat(model.<String>get("$.interruptible")).isEqualTo("YES");
        Assertions.assertThat(model.<String>get("$.clamped")).isEqualTo("YES");
        Assertions.assertThat(model.<String>get("$.grounded")).isEqualTo("YES");
        Assertions.assertThat(model.<String>get("$.limiter")).isEqualTo("YES");
        Assertions.assertThat(model.<String>get("$.valve")).isEqualTo("YES");
        Assertions.assertThat(model.<String>get("$.bypassStatus")).isEqualTo("CLOSED");
        Assertions.assertThat(model.<Integer>get("$.loadLimit.value")).isEqualTo(101);
        Assertions.assertThat(model.<Integer>get("$.loadLimit.multiplier")).isEqualTo(0);
        Assertions.assertThat(model.<String>get("$.loadLimit.unit")).isEqualTo("W");
        Assertions.assertThat(model.<String>get("$.loadLimiterType")).isEqualTo("LLT");
        Assertions.assertThat(model.<Integer>get("$.physicalCapacity.value")).isEqualTo(102);
        Assertions.assertThat(model.<Integer>get("$.physicalCapacity.multiplier")).isEqualTo(0);
        Assertions.assertThat(model.<String>get("$.physicalCapacity.unit")).isEqualTo("m");
        Assertions.assertThat(model.<Integer>get("$.pressure.value")).isEqualTo(103);
        Assertions.assertThat(model.<Integer>get("$.pressure.multiplier")).isEqualTo(0);
        Assertions.assertThat(model.<String>get("$.pressure.unit")).isEqualTo("kg");
        Assertions.assertThat(model.<List>get("$.link")).hasSize(1);
        Assertions.assertThat(model.<String>get("$.link[0].params.rel")).isEqualTo(Relation.REF_SELF.rel());
        Assertions.assertThat(model.<String>get("$.link[0].href"))
                .isEqualTo("http://localhost:9998/usagepoints/31/details/1462104000000");
    }


    @Test
    public void testUsagePointFields() throws Exception {
        mockUsagePoint(1, "test", 1L, ServiceKind.GAS);
        Response response = target("/usagepoints/1/details").request("application/json")
                .method("PROPFIND", Response.class);
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        Assertions.assertThat(model.<List>get("$")).hasSize(18);
        Assertions.assertThat(model.<List<String>>get("$")).containsOnly(
                "bypass",
                "bypassStatus",
                "capped",
                "clamped",
                "collar",
                "grounded",
                "id",
                "interruptible",
                "limiter",
                "link",
                "loadLimit",
                "loadLimiterType",
                "physicalCapacity",
                "pressure",
                "valve",
                "version",
                "effectivity",
                "current"
        );
    }


}
