package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.metering.UsagePoint;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

public class UsagePointResourceTest extends MultisensePublicApiJerseyTest {

    @Test
    public void testAllGetUsagePointsPaged() throws Exception {
        Response response = target("/usagepoints").queryParam("start", 0).queryParam("limit", 10).request().get();
        assertThat(response.getStatus()).isGreaterThan(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetSingleUsagePointWithFields() throws Exception {
        UsagePoint usagePoint = mockUsagePoint(31L, "usage point");
        Response response = target("/usagepoints/31").queryParam("fields", "id,name").request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Integer>get("$.id")).isEqualTo(31);
        assertThat(model.<Integer>get("$.version")).isNull();
        assertThat(model.<String>get("$.name")).isEqualTo("usage point");
        assertThat(model.<String>get("$.link")).isNull();
        assertThat(model.<String>get("$.readRoute")).isNull();
    }

    @Test
    public void testGetSingleUsagePointAllFields() throws Exception {
        UsagePoint usagePoint = mockUsagePoint(31L, "usage point");
        Response response = target("/usagepoints/31").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<Integer>get("$.id")).isEqualTo(31);
        assertThat(model.<Integer>get("$.version")).isEqualTo(2);
        assertThat(model.<String>get("$.name")).isEqualTo("usage point");
        assertThat(model.<String>get("$.location")).isEqualTo("location");
        assertThat(model.<String>get("$.mrid")).isEqualTo("MRID");
        assertThat(model.<String>get("$.readRoute")).isEqualTo("read route");
        assertThat(model.<Long>get("$.installationTime")).isEqualTo(LocalDateTime.of(2016, 3, 20, 11, 0)
                .toInstant(ZoneOffset.UTC)
                .toEpochMilli());
        assertThat(model.<String>get("$.description")).isEqualTo("usage point desc");
        assertThat(model.<String>get("$.serviceDeliveryRemark")).isEqualTo("remark");
        assertThat(model.<String>get("$.servicePriority")).isEqualTo("service priority");
        assertThat(model.<String>get("$.link.params.rel")).isEqualTo(Relation.REF_SELF.rel());
        assertThat(model.<String>get("$.link.href")).isEqualTo("http://localhost:9998/usagepoints/31");
    }

    @Test
    public void testUpdateUsagePoint() throws Exception {
        Instant now = Instant.now(clock);
        UsagePointInfo info = new UsagePointInfo();
        info.id = 999L;
        info.version = 2L;
        info.aliasName = "alias";
        info.description = "desc";
        info.installationTime = now;
        info.location = "here";
        info.mrid = "mmmmm";
        info.name = "naam";
        info.outageRegion = "outage";
        info.serviceDeliveryRemark = "remark";
        info.servicePriority = "prio1";

        UsagePoint usagePoint = mockUsagePoint(11L, "usage point");
        Response response = target("/usagepoints/11").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(usagePoint).setName("naam");
        verify(usagePoint).setAliasName("alias");
        verify(usagePoint).setDescription("desc");
        verify(usagePoint).setInstallationTime(now);
        verify(usagePoint).setMRID("mmmmm");
        verify(usagePoint).setOutageRegion("outage");
        verify(usagePoint).setServiceDeliveryRemark("remark");
        verify(usagePoint).setServicePriority("prio1");
        verify(usagePoint).update();
    }

    @Test
    public void testUsagePointFields() throws Exception {
        Response response = target("/usagepoints").request("application/json").method("PROPFIND", Response.class);
        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(model.<List>get("$")).hasSize(13);
        assertThat(model.<List<String>>get("$")).containsOnly("aliasName",
                "description",
                "id",
                "installationTime",
                "link",
                "location",
                "mrid",
                "name",
                "outageRegion",
                "readRoute",
                "serviceDeliveryRemark",
                "servicePriority",
                "version");
    }


}
