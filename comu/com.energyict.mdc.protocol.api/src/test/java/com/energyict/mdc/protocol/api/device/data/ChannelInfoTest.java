package com.energyict.mdc.protocol.api.device.data;

import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.Unit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * Copyrights EnergyICT
 * Date: 04.08.15
 * Time: 11:06
 */
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
        ChannelInfo channelInfo = new ChannelInfo(0, "0.x.24.2.1.255", Unit.get(BaseUnit.AMPERE, 3), "serialNumber", readingType);
        ChannelInfo undefinedUnit = new ChannelInfo(0, "0.x.24.2.1.255", Unit.get(BaseUnit.UNITLESS, 0), "serialNumber", readingType);
        ChannelInfo wrongScale = new ChannelInfo(0, "0.x.24.2.1.255", Unit.get(BaseUnit.AMPERE, -1), "serialNumber", readingType);
        ChannelInfo wrongObisCode = new ChannelInfo(0, "1.2.3.4.5.6", Unit.get(BaseUnit.AMPERE, 3), "serialNumber", readingType);
        ChannelInfo wrongObisCode2 = new ChannelInfo(0, "1.2.3.4.5.6", Unit.get(BaseUnit.AMPERE, 3), "serialNumber", readingType);
        ChannelInfo wrongSerialNumber = new ChannelInfo(0, "0.x.24.2.1.255", Unit.get(BaseUnit.AMPERE, 3), "sdfsdfsdf", readingType);
        ChannelInfo wrongBaseUnit = new ChannelInfo(0, "0.x.24.2.1.255", Unit.get(BaseUnit.VOLT, 3), "serialNumber", readingType);
        ChannelInfo wrongName = new ChannelInfo(0, "sdfsdf", Unit.get(BaseUnit.AMPERE, 3), "serialNumber", readingType);
        ChannelInfo wrongName2 = new ChannelInfo(0, "sdfsdf", Unit.get(BaseUnit.AMPERE, 3), "serialNumber", readingType);
        ChannelInfo nullName = new ChannelInfo(0, null, Unit.get(BaseUnit.AMPERE, 3), "serialNumber", readingType);
        ChannelInfo noMeterIdentifier = new ChannelInfo(0, "A+", Unit.get(BaseUnit.AMPERE, 3));
        ChannelInfo noMeterIdentifier2 = new ChannelInfo(0, "A+", Unit.get(BaseUnit.AMPERE, 3));
        ChannelInfo meterIdentifier = new ChannelInfo(0, "A+", Unit.get(BaseUnit.AMPERE, 3), "serialNumber", readingType);
        ChannelInfo bFieldNoWildCard = new ChannelInfo(0, "0.1.24.2.1.255", Unit.get(BaseUnit.AMPERE, 3), "serialNumber", readingType);
        ChannelInfo bFieldNoWildCardWrongSerialNumber = new ChannelInfo(0, "0.1.24.2.1.255", Unit.get(BaseUnit.AMPERE, 3), "sdsdfsdf", readingType);
        ChannelInfo otherReadingTypeMrid = new ChannelInfo(0, "0.x.24.2.1.255", Unit.get(BaseUnit.AMPERE, 3), "serialNumber", otherReadingType);
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
        assertFalse(channelInfo.equals(nullReadingType));
        assertFalse(channelInfo.equals(otherReadingTypeMrid));
    }
}