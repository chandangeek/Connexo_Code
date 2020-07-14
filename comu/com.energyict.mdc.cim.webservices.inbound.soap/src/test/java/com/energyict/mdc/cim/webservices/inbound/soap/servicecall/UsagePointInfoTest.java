package com.elster.jupiter.cim.webservices.inbound.soap.servicecall;

import com.energyict.mdc.cim.webservices.inbound.soap.servicecall.masterdatalinkageconfig.bean.UsagePointInfo;

import ch.iec.tc57._2011.masterdatalinkageconfig.Name;
import ch.iec.tc57._2011.masterdatalinkageconfig.UsagePoint;
import com.google.common.collect.ImmutableList;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UsagePointInfoTest {

    @Test
    public void testConstructorWithoutName() {
        String mrid = "my mrid";

        UsagePoint usagePoint = mockUsagePoint(mrid);

        UsagePointInfo usagePointInfo = new UsagePointInfo(usagePoint);

        verifyUsagePoint(mrid, usagePointInfo);
        assertNull(usagePointInfo.getName());
    }

    @Test
    public void testConstructorWithName() {
        String mrid = "my mrid";

        UsagePoint meter = mockUsagePoint(mrid);

        Name nameObj = mock(Name.class);
        String name = "my name";
        when(nameObj.getName()).thenReturn(name);
        when(meter.getNames()).thenReturn(ImmutableList.of(nameObj));

        UsagePointInfo usagePointInfo = new UsagePointInfo(meter);

        verifyUsagePoint(mrid, usagePointInfo);
        assertEquals(name, usagePointInfo.getName());
    }

    private UsagePoint mockUsagePoint(String mrid) {
        UsagePoint usagePoint = mock(UsagePoint.class);
        when(usagePoint.getMRID()).thenReturn(mrid);
        return usagePoint;
    }

    private void verifyUsagePoint(String mrid, UsagePointInfo meterInfo) {
        assertEquals(mrid, meterInfo.getMrid());
    }

}
