/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.v1;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.BypassStatus;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.WaterDetail;
import com.elster.jupiter.rest.api.util.v1.hypermedia.Relation;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WaterDetailResourceTest extends PlatformPublicApiJerseyTest {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        Finder<UsagePoint> finder = mockFinder(Collections.emptyList());
        when(meteringService.getUsagePoints(any())).thenReturn(finder);
    }

    @Test
    public void testGetDetails() throws Exception {
        mockUsagePoint(MRID, 2L, ServiceKind.WATER);

        // Business method
        Response response = target("/usagepoints/" + MRID + "/details/1").request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testUpdateWaterUsagePointMissingEffectivity() throws Exception {
        Instant now = Instant.now(clock);
        WaterDetailInfo info = new WaterDetailInfo();
        info.id = now;
        info.version = 2L;
        info.effectivity = null;

        UsagePoint usagePoint = mockUsagePoint(MRID, 2L, ServiceKind.WATER);
        when(usagePoint.getId()).thenReturn(13L);
        when(meteringService.findAndLockUsagePointByIdAndVersion(13L, 2L)).thenReturn(Optional.of(usagePoint));

        // Business method
        Response response = target("/usagepoints/" + MRID + "/details").request().post(Entity.json(info));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.create((InputStream) response.getEntity());
        assertThat(jsonModel.<String>get("$.errors[0].id")).isEqualTo("effectivity.lowerEnd");
    }

    @Test
    public void testUpdateWaterUsagePointMissingVersion() throws Exception {
        Instant now = Instant.now(clock);
        WaterDetailInfo info = new WaterDetailInfo();
        info.id = now;
        info.version = null;

        mockUsagePoint(MRID, 2L, ServiceKind.WATER);

        // Business method
        Response response = target("/usagepoints/" + MRID + "/details").request().post(Entity.json(info));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.create((InputStream) response.getEntity());
        assertThat(jsonModel.<String>get("$.errors[0].id")).isEqualTo("version");
    }

    @Test
    public void testGetSingleWaterUsagePointAllFields() throws Exception {
        WaterDetail waterDetail = mock(WaterDetail.class);
        BigDecimal value1 = BigDecimal.valueOf(201);
        BigDecimal value2 = BigDecimal.valueOf(202);
        BigDecimal value3 = BigDecimal.valueOf(203);
        when(waterDetail.getBypassStatus()).thenReturn(BypassStatus.CLOSED);
        when(waterDetail.getLoadLimit()).thenReturn(Quantity.create(value1, "W"));
        when(waterDetail.getLoadLimiterType()).thenReturn("LLT");
        when(waterDetail.getPhysicalCapacity()).thenReturn(Quantity.create(value2, "m"));
        when(waterDetail.getPressure()).thenReturn(Quantity.create(value3, "kg"));
        when(waterDetail.isBypassInstalled()).thenReturn(YesNoAnswer.YES);
        when(waterDetail.isCapped()).thenReturn(YesNoAnswer.YES);
        when(waterDetail.isCurrent()).thenReturn(Boolean.TRUE);
        when(waterDetail.isClamped()).thenReturn(YesNoAnswer.YES);
        when(waterDetail.isGrounded()).thenReturn(YesNoAnswer.YES);
        when(waterDetail.isLimiter()).thenReturn(YesNoAnswer.YES);
        when(waterDetail.isValveInstalled()).thenReturn(YesNoAnswer.YES);
        when(waterDetail.isCollarInstalled()).thenReturn(YesNoAnswer.YES);
        when(waterDetail.getRange()).thenReturn(Range.downTo(clock.instant(), BoundType.CLOSED));
        mockUsagePoint(MRID, 2L, ServiceKind.WATER, waterDetail);

        // Business method
        Response response = target("/usagepoints/" + MRID + "/details").request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Long>get("$.id")).isEqualTo(clock.millis());
        assertThat(model.<Integer>get("$.version")).isEqualTo(2);
        assertThat(model.<Long>get("$.effectivity.lowerEnd")).isEqualTo(clock.millis());
        assertThat(model.<Long>get("$.effectivity.upperEnd")).isNull();
        assertThat(model.<String>get("$.bypass")).isEqualTo("YES");
        assertThat(model.<String>get("$.collar")).isEqualTo("YES");
        assertThat(model.<String>get("$.capped")).isEqualTo("YES");
        assertThat(model.<String>get("$.clamped")).isEqualTo("YES");
        assertThat(model.<Boolean>get("$.current")).isTrue();
        assertThat(model.<String>get("$.grounded")).isEqualTo("YES");
        assertThat(model.<String>get("$.limiter")).isEqualTo("YES");
        assertThat(model.<String>get("$.valve")).isEqualTo("YES");
        assertThat(model.<String>get("$.bypassStatus")).isEqualTo("CLOSED");
        assertThat(model.<Integer>get("$.loadLimit.value")).isEqualTo(201);
        assertThat(model.<Integer>get("$.loadLimit.multiplier")).isEqualTo(0);
        assertThat(model.<String>get("$.loadLimit.unit")).isEqualTo("W");
        assertThat(model.<String>get("$.loadLimiterType")).isEqualTo("LLT");
        assertThat(model.<Integer>get("$.physicalCapacity.value")).isEqualTo(202);
        assertThat(model.<Integer>get("$.physicalCapacity.multiplier")).isEqualTo(0);
        assertThat(model.<String>get("$.physicalCapacity.unit")).isEqualTo("m");
        assertThat(model.<Integer>get("$.pressure.value")).isEqualTo(203);
        assertThat(model.<Integer>get("$.pressure.multiplier")).isEqualTo(0);
        assertThat(model.<String>get("$.pressure.unit")).isEqualTo("kg");
        assertThat(model.<List>get("$.link")).hasSize(1);
        assertThat(model.<String>get("$.link[0].params.rel")).isEqualTo(Relation.REF_SELF.rel());
        assertThat(model.<String>get("$.link[0].href")).isEqualTo("http://localhost:9998/usagepoints/" + MRID + "/details/1462104000000");
    }

    @Test
    public void testUsagePointFields() throws Exception {
        mockUsagePoint(MRID, 1L, ServiceKind.WATER);

        // Business method
        Response response = target("/usagepoints/" + MRID + "/details").request("application/json").method("PROPFIND", Response.class);

        // Asserts
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("$")).hasSize(17);
        assertThat(model.<List<String>>get("$")).containsOnly(
                "bypass",
                "bypassStatus",
                "capped",
                "clamped",
                "collar",
                "grounded",
                "id",
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
