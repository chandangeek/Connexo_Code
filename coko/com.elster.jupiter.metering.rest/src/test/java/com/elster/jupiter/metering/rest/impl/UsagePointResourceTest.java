package com.elster.jupiter.metering.rest.impl;


import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointBuilder;
import com.elster.jupiter.users.User;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class UsagePointResourceTest extends MeteringApplicationJerseyTest {

    public static final Instant NOW = ZonedDateTime.of(2015, 12, 10, 10, 43, 13, 0, ZoneId.systemDefault()).toInstant();


    @Mock
    private User principal;
    @Mock
    private UsagePoint usagePoint;
    @Mock
    private ServiceCategory serviceCategory;
    @Mock
    private UsagePointBuilder usagePointBuilder;


    @Before
    public void setUp1() {
        when(meteringService.findUsagePoint("MRID")).thenReturn(Optional.of(usagePoint));
        when(meteringService.getServiceCategory(ServiceKind.ELECTRICITY)).thenReturn(Optional.of(serviceCategory));
        when(serviceCategory.newUsagePoint(anyString(), any(Instant.class))).thenReturn(usagePointBuilder);
        when(serviceCategory.getKind()).thenReturn(ServiceKind.ELECTRICITY);
        when(usagePointBuilder.withName(anyString())).thenReturn(usagePointBuilder);
        when(usagePointBuilder.create()).thenReturn(usagePoint);
        when(clock.instant()).thenReturn(NOW);
        when(usagePoint.getServiceCategory()).thenReturn(serviceCategory);
        when(usagePoint.getMRID()).thenReturn("MRID");
        when(usagePoint.getInstallationTime()).thenReturn(Instant.EPOCH);
        when(usagePoint.getId()).thenReturn(1L);
        when(usagePoint.getVersion()).thenReturn(1L);
        when(usagePoint.getCreateDate()).thenReturn(Instant.EPOCH);
        when(usagePoint.getModificationDate()).thenReturn(Instant.EPOCH);


    }

    @Test
    public void testUsagePointCreating() {

        UsagePointInfo info = new UsagePointInfo();
        info.mRID = "test";
        info.installationTime = Instant.EPOCH.toEpochMilli();
        info.serviceCategory = ServiceKind.ELECTRICITY;
        Response response = target("usagepoints").request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(201);
    }

    @Test
    public void testUsagePointUpdate() {
        when(meteringService.findUsagePoint(1L)).thenReturn(Optional.of(usagePoint));
        when(meteringService.findAndLockUsagePointByIdAndVersion(1L, 1L)).thenReturn(Optional.of(usagePoint));
        UsagePointInfo info = new UsagePointInfo();
        info.id = 1L;
        info.mRID = "upd";
        info.name = "upd";
        info.installationTime = Instant.EPOCH.toEpochMilli();
        info.version = 1L;

        Response response = target("usagepoints/1").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(200);
        verify(usagePoint, never()).setMRID(anyString());
        verify(usagePoint, times(1)).setName("upd");
        verify(usagePoint, times(1)).setInstallationTime(any(Instant.class));
        verify(usagePoint, times(1)).update();
    }

}
