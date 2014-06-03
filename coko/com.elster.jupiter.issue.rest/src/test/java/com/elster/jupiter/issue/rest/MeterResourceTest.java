package com.elster.jupiter.issue.rest;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.google.common.base.Optional;
import org.junit.Test;
import org.mockito.Matchers;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.elster.jupiter.issue.rest.request.RequestHelper.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MeterResourceTest extends Mocks {

    @Test
    public void testGetMetersWithoutParams(){
        Response response = target("/meters").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetMetersWithoutLike(){
        Map<String, Object> map = target("/meters")
                .queryParam(START, 0)
                .queryParam(LIMIT, 10).request().get(Map.class);
        assertThat((String) map.get("data")).isEqualTo("");
    }

    @Test
    public void testGetMeters(){
        List<Meter> meters = new ArrayList<>();
        meters.add(mockMeter(1, "0.0.1.2"));
        meters.add(mockMeter(2, "0.0.1.8"));

        Query<Meter> query = mock(Query.class);
        when(query.select(Matchers.any(Condition.class), Matchers.anyInt(), Matchers.anyInt(), Matchers.any(Order[].class))).thenReturn(meters);
        when(meteringService.getMeterQuery()).thenReturn(query);

        Map<String, Object> map = target("/meters")
                .queryParam(START, 0)
                .queryParam(LIMIT, 10)
                .queryParam(LIKE, "0.0.").request().get(Map.class);
        assertThat(map.get("total")).isEqualTo(2);
        List data = (List) map.get("data");
        assertThat(data).hasSize(2);
        assertThat(((Map) data.get(1)).get("name")).isEqualTo("0.0.1.8");
    }

    @Test
    public void testGetMeterUnexisting(){
        when(meteringService.findMeter(9999)).thenReturn(Optional.<Meter>absent());
        Response response = target("/meters/9999").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetMeter(){
        Meter meter = mockMeter(1, "0.0.0.1");
        when(meteringService.findMeter(1)).thenReturn(Optional.of(meter));
        Map<String, Object> map = target("/meters/1").request().get(Map.class);
        assertThat(((Map)map.get("data")).get("id")).isEqualTo(1);
    }
}
