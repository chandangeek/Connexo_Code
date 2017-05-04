/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.device.data;

import com.elster.jupiter.metering.ReadingType;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.protocol.ChannelInfo;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ChannelInfoTest {

    @Mock
    private ReadingType readingType;
    @Mock
    private ReadingType otherReadingType;

    @Before
    public void setup() {
        when(readingType.getMRID()).thenReturn("MyMrid");
        when(otherReadingType.getMRID()).thenReturn("OtherReadingTypeMrid");
    }

    /**
     * 2 channel infos are considered equal if they have the same ObisCode (or text name), BaseUnit and serial number.
     * - Scale of the unit is ignored, only BaseUnit is compared
     * - Wild card at the B-field of the ObisCode is equal to any b-field value
     */
    @Test
    public void testEquals() {
        ChannelInfo channelInfo = new ChannelInfo(0, "0.x.24.2.1.255", Unit.get(BaseUnit.AMPERE, 3), "serialNumber", readingType.getMRID());
        ChannelInfo undefinedUnit = new ChannelInfo(0, "0.x.24.2.1.255", Unit.get(BaseUnit.UNITLESS, 0), "serialNumber", readingType.getMRID());
        ChannelInfo wrongScale = new ChannelInfo(0, "0.x.24.2.1.255", Unit.get(BaseUnit.AMPERE, -1), "serialNumber", readingType.getMRID());
        ChannelInfo wrongObisCode = new ChannelInfo(0, "1.2.3.4.5.6", Unit.get(BaseUnit.AMPERE, 3), "serialNumber", readingType.getMRID());
        ChannelInfo wrongObisCode2 = new ChannelInfo(0, "1.2.3.4.5.6", Unit.get(BaseUnit.AMPERE, 3), "serialNumber", readingType.getMRID());
        ChannelInfo wrongSerialNumber = new ChannelInfo(0, "0.x.24.2.1.255", Unit.get(BaseUnit.AMPERE, 3), "sdfsdfsdf", readingType.getMRID());
        ChannelInfo wrongBaseUnit = new ChannelInfo(0, "0.x.24.2.1.255", Unit.get(BaseUnit.VOLT, 3), "serialNumber", readingType.getMRID());
        ChannelInfo wrongName = new ChannelInfo(0, "sdfsdf", Unit.get(BaseUnit.AMPERE, 3), "serialNumber", readingType.getMRID());
        ChannelInfo wrongName2 = new ChannelInfo(0, "sdfsdf", Unit.get(BaseUnit.AMPERE, 3), "serialNumber", readingType.getMRID());
        ChannelInfo nullName = new ChannelInfo(0, null, Unit.get(BaseUnit.AMPERE, 3), "serialNumber", readingType.getMRID());
        ChannelInfo noMeterIdentifier = new ChannelInfo(0, "A+", Unit.get(BaseUnit.AMPERE, 3));
        ChannelInfo noMeterIdentifier2 = new ChannelInfo(0, "A+", Unit.get(BaseUnit.AMPERE, 3));
        ChannelInfo meterIdentifier = new ChannelInfo(0, "A+", Unit.get(BaseUnit.AMPERE, 3), "serialNumber", readingType.getMRID());
        ChannelInfo bFieldNoWildCard = new ChannelInfo(0, "0.1.24.2.1.255", Unit.get(BaseUnit.AMPERE, 3), "serialNumber", readingType.getMRID());
        ChannelInfo bFieldNoWildCardWrongSerialNumber = new ChannelInfo(0, "0.1.24.2.1.255", Unit.get(BaseUnit.AMPERE, 3), "sdsdfsdf", readingType.getMRID());
        ChannelInfo otherReadingTypeMrid = new ChannelInfo(0, "0.x.24.2.1.255", Unit.get(BaseUnit.AMPERE, 3), "serialNumber", otherReadingType.getMRID());
        ChannelInfo nullReadingType = new ChannelInfo(0, "0.x.24.2.1.255", Unit.get(BaseUnit.AMPERE, 3), "serialNumber", null);

        assertTrue(channelInfo.equals(wrongScale));
        assertFalse(channelInfo.equals(wrongObisCode));
        assertFalse(channelInfo.equals(wrongSerialNumber));
        assertFalse(channelInfo.equals(wrongBaseUnit));
        assertFalse(channelInfo.equals(wrongName));
        assertFalse(channelInfo.equals(null));
        assertFalse(channelInfo.equals(new Integer(1)));
        assertFalse(channelInfo.equals(nullName));
        assertFalse(nullName.equals(channelInfo));
        assertTrue(channelInfo.equals(bFieldNoWildCard));
        assertFalse(channelInfo.equals(bFieldNoWildCardWrongSerialNumber));
        assertTrue(wrongName.equals(wrongName2));
        assertTrue(wrongObisCode.equals(wrongObisCode2));
        assertTrue(noMeterIdentifier.equals(noMeterIdentifier2));
        assertFalse(meterIdentifier.equals(noMeterIdentifier2));
        assertFalse(noMeterIdentifier2.equals(meterIdentifier));
        assertTrue(undefinedUnit.equals(channelInfo));
        assertTrue(channelInfo.equals(undefinedUnit));
        assertTrue(channelInfo.equals(nullReadingType));
        assertFalse(channelInfo.equals(otherReadingTypeMrid));
    }
}