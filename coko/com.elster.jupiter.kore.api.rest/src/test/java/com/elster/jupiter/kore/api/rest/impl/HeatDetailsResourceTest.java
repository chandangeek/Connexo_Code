package com.elster.jupiter.kore.api.rest.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.BypassStatus;
import com.elster.jupiter.metering.HeatDetail;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.rest.util.hypermedia.Relation;
import com.elster.jupiter.util.YesNoAnswer;
import com.elster.jupiter.util.units.Quantity;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import org.assertj.core.api.Assertions;
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
        UsagePoint usagePoint = mockUsagePoint(31L, "usage point", 2L, ServiceKind.HEAT);
        Response response = target("/usagepoints/31/details/1").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

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
        UsagePoint usagePoint = mockUsagePoint(31L, "usage point", 2L, ServiceKind.HEAT, heatDetail);

        Response response = target("/usagepoints/31/details").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        Assertions.assertThat(model.<Long>get("$.id")).isEqualTo(clock.millis());
        Assertions.assertThat(model.<Integer>get("$.version")).isEqualTo(2);
        Assertions.assertThat(model.<String>get("$.valve")).isEqualTo("YES");
        Assertions.assertThat(model.<String>get("$.collar")).isEqualTo("YES");
        Assertions.assertThat(model.<Boolean>get("$.current")).isTrue();
        Assertions.assertThat(model.<String>get("$.bypass")).isEqualTo("YES");
        Assertions.assertThat(model.<String>get("$.bypassStatus")).isEqualTo("CLOSED");
        Assertions.assertThat(model.<Integer>get("$.physicalCapacity.value")).isEqualTo(302);
        Assertions.assertThat(model.<Integer>get("$.physicalCapacity.multiplier")).isEqualTo(0);
        Assertions.assertThat(model.<String>get("$.physicalCapacity.unit")).isEqualTo("m");
        Assertions.assertThat(model.<Integer>get("$.pressure.value")).isEqualTo(301);
        Assertions.assertThat(model.<Integer>get("$.pressure.multiplier")).isEqualTo(0);
        Assertions.assertThat(model.<String>get("$.pressure.unit")).isEqualTo("kg");
        Assertions.assertThat(model.<Long>get("$.effectivity.lowerEnd")).isEqualTo(clock.millis());
        Assertions.assertThat(model.<Long>get("$.effectivity.upperEnd")).isNull();
        Assertions.assertThat(model.<List>get("$.link")).hasSize(1);
        Assertions.assertThat(model.<String>get("$.link[0].params.rel")).isEqualTo(Relation.REF_SELF.rel());
        Assertions.assertThat(model.<String>get("$.link[0].href"))
                .isEqualTo("http://localhost:9998/usagepoints/31/details/1462104000000");
    }

    @Test
    public void testUsagePointFields() throws Exception {
        mockUsagePoint(1, "test", 1L, ServiceKind.HEAT);
        Response response = target("/usagepoints/1/details").request("application/json")
                .method("PROPFIND", Response.class);
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        Assertions.assertThat(model.<List>get("$")).hasSize(11);
        Assertions.assertThat(model.<List<String>>get("$")).containsOnly(
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
