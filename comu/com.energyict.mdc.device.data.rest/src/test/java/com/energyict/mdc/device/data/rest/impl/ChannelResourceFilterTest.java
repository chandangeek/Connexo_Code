package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.cbo.Accumulation;
import com.elster.jupiter.cbo.Aggregate;
import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.FlowDirection;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.MeasurementKind;
import com.elster.jupiter.cbo.MetricMultiplier;
import com.elster.jupiter.cbo.Phase;
import com.elster.jupiter.cbo.RationalNumber;
import com.elster.jupiter.cbo.ReadingTypeUnit;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.validation.ValidationResult;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.data.Channel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceValidation;
import com.energyict.mdc.device.data.LoadProfile;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.jayway.jsonpath.JsonModel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.net.URLEncoder;
import java.time.Instant;
import java.util.Arrays;
import java.util.Currency;
import java.util.Optional;
import java.util.PrimitiveIterator.OfInt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class ChannelResourceFilterTest extends DeviceDataRestApplicationJerseyTest {

    private static final String DEIVICE_MRID = "mrid1";
    private static final Instant NOW = Instant.ofEpochMilli(1410786205000L);

    @Mock
    private Device device;
    @Mock
    private DeviceValidation deviceValidation;

    @Before
    public void setupStubs(){
        when(deviceService.findByUniqueMrid(DEIVICE_MRID)).thenReturn(Optional.of(device));
        when(clock.instant()).thenReturn(NOW);
        LoadProfile loadProfile1 = mock(LoadProfile.class);
        LoadProfile loadProfile2 = mock(LoadProfile.class);
        when(loadProfile1.getId()).thenReturn(1L);
        when(loadProfile2.getId()).thenReturn(2L);
        LoadProfileSpec lpSpec1 = mock(LoadProfileSpec.class);
        LoadProfileSpec lpSpec2 = mock(LoadProfileSpec.class);
        LoadProfileType lpType1 = mock(LoadProfileType.class);
        LoadProfileType lpType2 = mock(LoadProfileType.class);
        when(loadProfile1.getLoadProfileSpec()).thenReturn(lpSpec1);
        when(loadProfile2.getLoadProfileSpec()).thenReturn(lpSpec2);
        when(lpSpec1.getLoadProfileType()).thenReturn(lpType1);
        when(lpSpec2.getLoadProfileType()).thenReturn(lpType2);
        when(lpType1.getName()).thenReturn("Load Profile Name 1");
        when(lpType2.getName()).thenReturn("Active Energy Import");
        Channel channel1 = mockChannel(1, mockReadingType("Bulk A+ all phases", "0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0"));//Bulk A+ all phases (Wh)
        Channel channel2 = mockChannel(2, mockReadingType("Bulk A+ all phases", "11.0.0.1.1.1.12.0.0.0.0.1.0.0.0.0.72.0"));//Bulk A+ all phases ToU 1 (Wh)
        Channel channel3 = mockChannel(3, mockReadingType("Bulk A- all phases", "13.0.0.1.19.1.12.0.0.0.0.1.0.0.0.0.72.0"));//Bulk A- all phases ToU 1 (Wh)
        Channel channel4 = mockChannel(4, mockReadingType("Bulk A+ all phases", "13.0.0.1.1.1.12.0.0.0.0.2.0.0.0.0.72.0"));//Bulk A+ all phases ToU 2 (Wh)
        when(loadProfile1.getChannels()).thenReturn(Arrays.asList(channel1, channel2));
        when(channel1.getLoadProfile()).thenReturn(loadProfile1);
        when(channel2.getLoadProfile()).thenReturn(loadProfile1);
        when(loadProfile2.getChannels()).thenReturn(Arrays.asList(channel3, channel4));
        when(channel3.getLoadProfile()).thenReturn(loadProfile2);
        when(channel4.getLoadProfile()).thenReturn(loadProfile2);
        when(device.getLoadProfiles()).thenReturn(Arrays.asList(loadProfile1, loadProfile2));
        when(device.getChannels()).thenReturn(Arrays.asList(channel1,channel2,channel3,channel4));
        when(deviceValidation.getValidationResult(any())).thenReturn(ValidationResult.SUSPECT);
        when(device.forValidation()).thenReturn(deviceValidation);
        doReturn(false).when(deviceValidation).isValidationActive(any(Channel.class), eq(NOW));
        when(deviceValidation.getLastChecked(any(Channel.class))).thenReturn(Optional.<Instant>empty());
    }

    private Channel mockChannel(long id, ReadingType readingType){
        Channel channel = mock(Channel.class);
        Unit unit = Unit.get("kWh");
        ChannelSpec channelSpec = mock(ChannelSpec.class);
        when(channel.getInterval()).thenReturn(TimeDuration.minutes(15));
        when(channel.getLastReading()).thenReturn(Optional.<Instant>empty());
        when(channel.getName()).thenReturn("Channel: " + id);
        when(channel.getId()).thenReturn(id);
        when(channel.getDevice()).thenReturn(device);
        when(channel.getReadingType()).thenReturn(readingType);
        when(channel.getChannelSpec()).thenReturn(channelSpec);
        when(channel.getLastDateTime()).thenReturn(Optional.<Instant>empty());
        when(channel.getUnit()).thenReturn(unit);
        return channel;
    }

    @Test
    public void testGetAllChannelsOnDevice(){
        String json = target("devices/" + DEIVICE_MRID + "/channels")
                .request().get(String.class);
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(4);
    }

    @Test
    public void testGetChannelsFilterByLoadProfileName() throws Exception{
        String json = target("devices/" + DEIVICE_MRID + "/channels")
                .queryParam("filter", URLEncoder.encode("[{\"property\":\"loadProfileName\",\"value\":[\"*nergy*\"]}]", "UTF-8"))
                .request().get(String.class);
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(2);
        assertThat(jsonModel.<Number>get("$.channels[0].id")).isEqualTo(3);
        assertThat(jsonModel.<Number>get("$.channels[1].id")).isEqualTo(4);
    }

    @Test
    public void testGetChannelsFilterByLoadProfileNames() throws Exception{
        String json = target("devices/" + DEIVICE_MRID + "/channels")
            .queryParam("filter", URLEncoder.encode("[{\"property\":\"loadProfileName\",\"value\":[\"*nergy*\",\"*Profile*\"]}]", "UTF-8"))
            .request().get(String.class);
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(4);
    }

    @Test
    public void testGetChannelsFilterByChannelName() throws Exception{
        String json = target("devices/" + DEIVICE_MRID + "/channels")
                .queryParam("filter", URLEncoder.encode("[{\"property\":\"channelName\",\"value\":\"*2*\"}]", "UTF-8"))
                .request().get(String.class);
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<Number>get("$.channels[0].id")).isEqualTo(4);
    }

    @Test
    public void testGetChannelsFilterByLoadProfileAndChannelName() throws Exception{
        String json = target("devices/" + DEIVICE_MRID + "/channels")
                .queryParam("filter", URLEncoder.encode(
                        "[{\"property\":\"channelName\",\"value\":\"*A+*\"}," +
                         "{\"property\":\"loadProfileName\",\"value\":[\"*nergy*\"]}]", "UTF-8"))
                .request().get(String.class);
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<Number>get("$.channels[0].id")).isEqualTo(4);
    }

    public ReadingType mockReadingType(String alias, String mrid) {
        ReadingType readingType = mock(ReadingType.class);
        OfInt iterator = Arrays.asList(mrid.split("\\.")).stream().mapToInt(Integer::parseInt).iterator();
        when(readingType.getAliasName()).thenReturn(alias);
        when(readingType.getMRID()).thenReturn(mrid);
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.get(iterator.next()));
        when(readingType.getAggregate()).thenReturn(Aggregate.get(iterator.next()));
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.get(iterator.next()));
        when(readingType.getAccumulation()).thenReturn(Accumulation.get(iterator.next()));
        when(readingType.getFlowDirection()).thenReturn(FlowDirection.get(iterator.next()));
        when(readingType.getCommodity()).thenReturn(Commodity.get(iterator.next()));
        when(readingType.getMeasurementKind()).thenReturn(MeasurementKind.get(iterator.next()));
        when(readingType.getInterharmonic()).thenReturn(new RationalNumber(iterator.next(), 1 + iterator.next()));
        when(readingType.getArgument()).thenReturn(new RationalNumber(iterator.next(), 1 + iterator.next()));
        when(readingType.getTou()).thenReturn(iterator.next());
        when(readingType.getCpp()).thenReturn(iterator.next());
        when(readingType.getConsumptionTier()).thenReturn(iterator.next());
        when(readingType.getPhases()).thenReturn(Phase.get(iterator.next()));
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.get(iterator.next()));
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.get(iterator.next()));
        when(readingType.getCurrency()).thenReturn(Currency.getInstance("EUR"));
        when(readingType.getCalculatedReadingType()).thenReturn(Optional.<ReadingType>empty());
        when(readingType.isCumulative()).thenReturn(true);
        return readingType;
    }
}
