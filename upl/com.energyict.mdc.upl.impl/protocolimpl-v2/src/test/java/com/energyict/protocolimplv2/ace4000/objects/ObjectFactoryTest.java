package com.energyict.protocolimplv2.ace4000.objects;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.protocol.DummyComChannel;
import com.energyict.mdw.offline.OfflineDevice;
import com.energyict.protocol.*;
import com.energyict.protocolimplv2.ace4000.ACE4000Outbound;
import org.junit.Test;

import java.util.List;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 29/06/2016 - 11:37
 */
public class ObjectFactoryTest {

    private static final String LOAD_PROFILE_DATA = "<MPush><MD><M>0505514284386660</M><LPA>V3MPgA8nAAACrwAAAM8AAAAAwAABAAACrwAAAM8AAAAAwAABAAACrwAAAM8AAAAAwAABAAACrwAAAM8AAAAAwAABAAACrwAAAM8AAAAAwAABAAACrwAAAM8AAAAAwAABAAACrwAAAM8AAAAAwAABAAACrwAAAM8AAAAAwAABAAACrwAAAM8AAAAAwAABAAACrwAAAM8AAAAAwAABAAACrwAAAM8AAAAAwAABAAACrwAAAM8AAAAAwAABAAACrwAAAM8AAAAAwAABAAACrwAAAM8AAAAAwAABAAACrwAAAM8AAAAAwAABAAACrwAAAM8AAAAAwAABAAACrwAAAM8AAAAAwAABAAACrwAAAM8AAAAAwAABAAACrwAAAM8AAAAAwAABAAACrwAAAM8AAAAAwAABAAACrwAAAM8AAAAAwAABAAACrwAAAM8AAAAAwAABAAACrwAAAM8AAAAAwAABAAACrwAAAM8AAAAAwAABAAACrwAAAM8AAAAAwAABAAACrwAAAM8AAAAAwAACAAACrwAAAM8AAAAAwAACAAACrwAAAM8AAAAAwAACAAACrwAAAM8AAAAAwAACAAACrwAAAM8AAAAAwAACAAACrwAAAM8AAAAAwAACAAACrwAAAM8AAAAAwAACAAACrwAAAM8AAAAAwAACAAACrwAAAM8AAAAAwAACAAACrwAAAM8AAAAAwAACAAACrwAAAM8AAAAAwAACAAACrwAAAM8AAAAAwAACAAACrwAAAM8AAAAAwAACAAACrwAAAM8AAAAAwAAC</LPA></MD></MPush>";
    private static final String SERIAL_NUMBER = "0505514284386660";

    @Test
    public void testProfileData() {
        ACE4000Outbound ace4000 = spy(new ACE4000Outbound());
        doReturn(TimeZone.getTimeZone("Europe/Athens")).when(ace4000).getTimeZone();
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        when(offlineDevice.getSerialNumber()).thenReturn(SERIAL_NUMBER);
        when(offlineDevice.getAllProperties()).thenReturn(TypedProperties.empty());
        ace4000.init(offlineDevice, new DummyComChannel());

        ObjectFactory objectFactory = new ObjectFactory(ace4000);

        objectFactory.parseXML(LOAD_PROFILE_DATA);

        ProfileData profileData = objectFactory.getLoadProfile().getProfileData();

        assertEquals(profileData.getIntervalDatas().size(), 39);
        assertEquals(profileData.getMeterEvents().size(), 0);
        IntervalData firstInterval = profileData.getIntervalDatas().get(0);
        assertEquals(firstInterval.getEndTime().getTime(), 1467147600000L);
        assertEquals(firstInterval.getEiStatus(), IntervalStateBits.REVERSERUN);
        assertEquals(firstInterval.getProtocolStatus(), 49152);
        assertEquals(firstInterval.getTariffCode(), 1);
        assertEquals(firstInterval.getIntervalValues().size(), 3);

        List<IntervalValue> intervalValues = firstInterval.getIntervalValues();
        assertEquals(intervalValues.get(0).getNumber(), 687);
        assertEquals(intervalValues.get(0).getEiStatus(), IntervalStateBits.REVERSERUN);
        assertEquals(intervalValues.get(0).getProtocolStatus(), 49152);
        assertEquals(intervalValues.get(1).getNumber(), 0);
        assertEquals(intervalValues.get(1).getEiStatus(), IntervalStateBits.REVERSERUN);
        assertEquals(intervalValues.get(1).getProtocolStatus(), 49152);
        assertEquals(intervalValues.get(2).getNumber(), 207);
        assertEquals(intervalValues.get(2).getEiStatus(), IntervalStateBits.REVERSERUN);
        assertEquals(intervalValues.get(2).getProtocolStatus(), 49152);

        assertEquals(profileData.getIntervalDatas().size(), 39);
        assertEquals(profileData.getMeterEvents().size(), 0);
        IntervalData lastInterval = profileData.getIntervalDatas().get(38);
        assertEquals(lastInterval.getEndTime().getTime(), 1467181800000L);
        assertEquals(lastInterval.getEiStatus(), IntervalStateBits.REVERSERUN);
        assertEquals(lastInterval.getProtocolStatus(), 49152);
        assertEquals(lastInterval.getTariffCode(), 2);
        assertEquals(lastInterval.getIntervalValues().size(), 3);

        intervalValues = lastInterval.getIntervalValues();
        assertEquals(intervalValues.get(0).getNumber(), 687);
        assertEquals(intervalValues.get(0).getEiStatus(), IntervalStateBits.REVERSERUN);
        assertEquals(intervalValues.get(0).getProtocolStatus(), 49152);
        assertEquals(intervalValues.get(1).getNumber(), 0);
        assertEquals(intervalValues.get(1).getEiStatus(), IntervalStateBits.REVERSERUN);
        assertEquals(intervalValues.get(1).getProtocolStatus(), 49152);
        assertEquals(intervalValues.get(2).getNumber(), 207);
        assertEquals(intervalValues.get(2).getEiStatus(), IntervalStateBits.REVERSERUN);
        assertEquals(intervalValues.get(2).getProtocolStatus(), 49152);

        List<ChannelInfo> channelInfos = profileData.getChannelInfos();
        assertEquals(channelInfos.size(), 3);
        assertEquals(channelInfos.get(0).getName(), "1.0.1.8.0.255");
        assertEquals(channelInfos.get(0).getUnit(), Unit.get(BaseUnit.WATTHOUR));
        assertEquals(channelInfos.get(0).getId(), 0);
        assertEquals(channelInfos.get(0).getMeterIdentifier(), SERIAL_NUMBER);
        assertEquals(channelInfos.get(1).getName(), "1.0.3.8.0.255");
        assertEquals(channelInfos.get(1).getUnit(), Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR));
        assertEquals(channelInfos.get(1).getId(), 1);
        assertEquals(channelInfos.get(1).getMeterIdentifier(), SERIAL_NUMBER);
        assertEquals(channelInfos.get(2).getName(), "1.0.2.8.0.255");
        assertEquals(channelInfos.get(2).getUnit(), Unit.get(BaseUnit.WATTHOUR));
        assertEquals(channelInfos.get(2).getId(), 2);
        assertEquals(channelInfos.get(2).getMeterIdentifier(), SERIAL_NUMBER);

    }
}