/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.kore.api.v1;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.BypassStatus;
import com.elster.jupiter.metering.HeatDetail;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
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

public class HeatDetailsResourceTest extends PlatformPublicApiJerseyTest {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        Finder<UsagePoint> finder = mockFinder(Collections.emptyList());
        when(meteringService.getUsagePoints(any())).thenReturn(finder);
    }

    @Test
    public void testGetDetails() throws Exception {
        mockUsagePoint(MRID, 2L, ServiceKind.HEAT);

        // Business method
        Response response = target("/usagepoints/" + MRID + "/details/1").request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testUpdateHeatUsagePointMissingEffectivity() throws Exception {
        Instant now = Instant.now(clock);
        HeatDetailInfo info = new HeatDetailInfo();
        info.id = now;
        info.version = 2L;
        info.effectivity = null;

        UsagePoint usagePoint = mockUsagePoint(MRID, 2L, ServiceKind.HEAT);
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
    public void testUpdateHeatUsagePointMissingVersion() throws Exception {
        Instant now = Instant.now(clock);
        HeatDetailInfo info = new HeatDetailInfo();
        info.id = now;
        info.version = null;

        mockUsagePoint(MRID, 2L, ServiceKind.HEAT);

        // Business method
        Response response = target("/usagepoints/" + MRID + "/details").request().post(Entity.json(info));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        JsonModel jsonModel = JsonModel.create((InputStream) response.getEntity());
        assertThat(jsonModel.<String>get("$.errors[0].id")).isEqualTo("version");
    }

    @Test
    public void testGetSingleHeatUsagePointAllFields() throws Exception {
        HeatDetail heatDetail = mock(HeatDetail.class);
        BigDecimal value1 = BigDecimal.valueOf(301);
        BigDecimal value2 = BigDecimal.valueOf(302);
        when(heatDetail.getBypassStatus()).thenReturn(BypassStatus.CLOSED);
        when(heatDetail.getPressure()).thenReturn(Quantity.create(value1, "kg"));
        when(heatDetail.getPhysicalCapacity()).thenReturn(Quantity.create(value2, "m"));
        when(heatDetail.isBypassInstalled()).thenReturn(YesNoAnswer.YES);
        when(heatDetail.isCollarInstalled()).thenReturn(YesNoAnswer.YES);
        when(heatDetail.isCurrent()).thenReturn(Boolean.TRUE);
        when(heatDetail.isValveInstalled()).thenReturn(YesNoAnswer.YES);
        when(heatDetail.getRange()).thenReturn(Range.downTo(clock.instant(), BoundType.CLOSED));
        mockUsagePoint(MRID, 2L, ServiceKind.HEAT, heatDetail);

        // Business method
        Response response = target("/usagepoints/" + MRID + "/details").request().get();

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Long>get("$.id")).isEqualTo(clock.millis());
        assertThat(model.<Integer>get("$.version")).isEqualTo(2);
        assertThat(model.<String>get("$.valve")).isEqualTo("YES");
        assertThat(model.<String>get("$.collar")).isEqualTo("YES");
        assertThat(model.<Boolean>get("$.current")).isTrue();
        assertThat(model.<String>get("$.bypass")).isEqualTo("YES");
        assertThat(model.<String>get("$.bypassStatus")).isEqualTo("CLOSED");
        assertThat(model.<Integer>get("$.physicalCapacity.value")).isEqualTo(302);
        assertThat(model.<Integer>get("$.physicalCapacity.multiplier")).isEqualTo(0);
        assertThat(model.<String>get("$.physicalCapacity.unit")).isEqualTo("m");
        assertThat(model.<Integer>get("$.pressure.value")).isEqualTo(301);
        assertThat(model.<Integer>get("$.pressure.multiplier")).isEqualTo(0);
        assertThat(model.<String>get("$.pressure.unit")).isEqualTo("kg");
        assertThat(model.<Long>get("$.effectivity.lowerEnd")).isEqualTo(clock.millis());
        assertThat(model.<Long>get("$.effectivity.upperEnd")).isNull();
        assertThat(model.<List>get("$.link")).hasSize(1);
        assertThat(model.<String>get("$.link[0].params.rel")).isEqualTo(Relation.REF_SELF.rel());
        assertThat(model.<String>get("$.link[0].href")).isEqualTo("http://localhost:9998/usagepoints/" + MRID + "/details/1462104000000");
    }

    @Test
    public void testUsagePointFields() throws Exception {
        mockUsagePoint(MRID, 1L, ServiceKind.HEAT);

        // Business method
        Response response = target("/usagepoints/" + MRID + "/details").request("application/json").method("PROPFIND", Response.class);

        // Asserts
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("$")).hasSize(11);
        assertThat(model.<List<String>>get("$")).containsOnly(
                "bypass",
                "bypassStatus",
                "collar",
                "id",
                "link",
                "physicalCapacity",
                "pressure",
                "valve",
                "version",
                "effectivity",
                "current"
        );
    }
}
