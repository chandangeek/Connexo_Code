package com.elster.jupiter.kore.api.rest.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;

import javax.ws.rs.core.Response;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 6/2/16.
 */
public class ElectricityDetailResourceTest extends PlatformPublicApiJerseyTest {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        Finder<UsagePoint> finder = mockFinder(Collections.emptyList());
        when(meteringService.getUsagePoints(any())).thenReturn(finder);
    }

    @Test
    public void testGetDetails() throws Exception {
        UsagePoint usagePoint = mockUsagePoint(31L, "usage point", 2L, ServiceKind.ELECTRICITY);
        Response response = target("/usagepoints/31/details/1").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

    }
}
