package com.energyict.protocolimplv2.ace4000.objects;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.mdc.identifiers.DeviceIdentifierBySerialNumber;
import com.energyict.mdc.protocol.inbound.g3.DummyComChannel;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalStateBits;
import com.energyict.protocol.IntervalValue;
import com.energyict.protocol.ProfileData;
import com.energyict.protocolimplv2.ace4000.ACE4000Outbound;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 29/06/2016 - 11:37
 */
@RunWith(MockitoJUnitRunner.class)
public class ObjectFactoryTest {

    @Mock
    private CollectedDataFactory collectedDataFactory;
    @Mock
    private IssueFactory issueFactory;
    @Mock
    private PropertySpecService propertySpecService;
    @Mock
    private NlsService nlsService;
    @Mock
    private Converter converter;
    @Mock
    private TariffCalendarExtractor calendarExtractor;

    private static final String LOAD_PROFILE_DATA = "<MPush><MD><M>0505514284386660</M><LPA>V3MPgA8nAAACrwAAAM8AAAAAwAABAAACrwAAAM8AAAAAwAABAAACrwAAAM8AAAAAwAABAAACrwAAAM8AAAAAwAABAAACrwAAAM8AAAAAwAABAAACrwAAAM8AAAAAwAABAAACrwAAAM8AAAAAwAABAAACrwAAAM8AAAAAwAABAAACrwAAAM8AAAAAwAABAAACrwAAAM8AAAAAwAABAAACrwAAAM8AAAAAwAABAAACrwAAAM8AAAAAwAABAAACrwAAAM8AAAAAwAABAAACrwAAAM8AAAAAwAABAAACrwAAAM8AAAAAwAABAAACrwAAAM8AAAAAwAABAAACrwAAAM8AAAAAwAABAAACrwAAAM8AAAAAwAABAAACrwAAAM8AAAAAwAABAAACrwAAAM8AAAAAwAABAAACrwAAAM8AAAAAwAABAAACrwAAAM8AAAAAwAABAAACrwAAAM8AAAAAwAABAAACrwAAAM8AAAAAwAABAAACrwAAAM8AAAAAwAABAAACrwAAAM8AAAAAwAACAAACrwAAAM8AAAAAwAACAAACrwAAAM8AAAAAwAACAAACrwAAAM8AAAAAwAACAAACrwAAAM8AAAAAwAACAAACrwAAAM8AAAAAwAACAAACrwAAAM8AAAAAwAACAAACrwAAAM8AAAAAwAACAAACrwAAAM8AAAAAwAACAAACrwAAAM8AAAAAwAACAAACrwAAAM8AAAAAwAACAAACrwAAAM8AAAAAwAACAAACrwAAAM8AAAAAwAACAAACrwAAAM8AAAAAwAAC</LPA></MD></MPush>";
    private static final String LOAD_PROFILE_DATA2 = "<MPush><MD><M>0505514284386913</M><T>610e</T><LPA>WecwrA8rAAnd7AABfSMAAAAAwAACAAnd7AABfSMAAAAAwAACAAnd7AABfSMAAAAAwAACAAnd7AABfSMAAAAAwAACAAnd7AABfSMAAAAAwAACAAnd7AABfSMAAAAAwAACAAnd7AABfSMAAAAAwAADAAnd7AABfSMAAAAAwAADAAnd7AABfSMAAAAAwAADAAnd7AABfSMAAAAAwAADAAnd7AABfSMAAAAAwAADAAnd7AABfSMAAAAAwAADAAnd7AABfSMAAAAAwAADAAnd7AABfSMAAAAAwAADAAnd7AABfSMAAAAAwAADAAnd7AABfSMAAAAAwAADAAnd7AABfSMAAAAAwAADAAnd7AABfSMAAAAAwAADAAnd7AABfSMAAAAAwAADAAnd7AABfSMAAAAAwAADAAnd7AABfSMAAAAAwAADAAnd7AABfSMAAAAAwAADAAnd7AABfSMAAAAAwAADAAnd7AABfSMAAAAAwAADAAnd7AABfSMAAAAAwAADAAnd7AABfSMAAAAAwAADAAnd7AABfSMAAAAAwAADAAnd7AABfSMAAAAAwAADAAnd7AABfSMAAAAAwAADAAnd7AABfSMAAAAAwAADAAnd7AABfSMAAAAAwAADAAnd7AABfSMAAAAAwAADAAnd7AABfSMAAAAAwAACAAnd7AABfSMAAAAAwAACAAnd7AABfSMAAAAAwAACAAnd7AABfSMAAAAAwAACAAnd7AABfSMAAAAAwAACAAnd7AABfSMAAAAAwAACAAnd7AABfSMAAAAAwAADAAnd7AABfSMAAAAAwAADAAnd7AABfSMAAAAAwAADAAnd7AABfSMAAAAAwAADAAnd7AABfSMAAAAAwAAB</LPA></MD></MPush>";
    private static final String SERIAL_NUMBER = "0505514284386660";

    @Test
    public void testProfileData() {
        ACE4000Outbound ace4000 = spy(new ACE4000Outbound(propertySpecService, nlsService, converter, collectedDataFactory, issueFactory, calendarExtractor));
        doReturn(TimeZone.getTimeZone("Europe/Athens")).when(ace4000).getTimeZone();
        doReturn(true).when(ace4000).isDst();
        OfflineDevice offlineDevice = mock(OfflineDevice.class);
        when(offlineDevice.getSerialNumber()).thenReturn(SERIAL_NUMBER);
        when(offlineDevice.getId()).thenReturn(1L);
        when(offlineDevice.getAllProperties()).thenReturn(TypedProperties.empty());
        ace4000.init(offlineDevice, new DummyComChannel());
        ObjectFactory objectFactory = new ObjectFactory(ace4000, collectedDataFactory);

        objectFactory.parseXML(LOAD_PROFILE_DATA);
        objectFactory.parseXML(LOAD_PROFILE_DATA2);

        ProfileData profileData = objectFactory.getLoadProfile().getProfileDataMap().get(new DeviceIdentifierBySerialNumber(SERIAL_NUMBER));

        assertEquals(profileData.getIntervalDatas().size(), 39);
        assertEquals(profileData.getMeterEvents().size(), 0);
        IntervalData firstInterval = profileData.getIntervalDatas().get(0);
        assertEquals(firstInterval.getEndTime().getTime(), 1467151200000L);
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
        assertEquals(lastInterval.getEndTime().getTime(), 1467185400000L);
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