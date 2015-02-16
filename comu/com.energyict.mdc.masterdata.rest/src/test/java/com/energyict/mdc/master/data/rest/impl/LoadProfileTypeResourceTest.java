package com.energyict.mdc.master.data.rest.impl;

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
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.util.exception.MessageSeed;
import com.energyict.mdc.common.ObisCode;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.masterdata.ChannelType;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.masterdata.rest.LocalizedTimeDuration;
import java.util.Optional;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.ws.rs.core.Response;
import org.junit.Test;
import org.mockito.Matchers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LoadProfileTypeResourceTest extends MasterDataApplicationJerseyTest {

    @Test
    public void testIntervalsList() throws Exception {
        when(thesaurus.getString(Matchers.<String>anyObject(), Matchers.<String>anyObject())).thenReturn("%s minute");
        List<Object> intervals = target("/loadprofiles/intervals").request().get(List.class);
        assertThat(intervals).hasSize(11);
        assertThat(((Map)intervals.get(0)).get("name")).isEqualTo("1 minute");
    }

    @Test
    public void testGetEmptyLoadProfileTypesList() throws Exception {
        List<LoadProfileType> allLoadProfileTypes = Collections.<LoadProfileType>emptyList();
        when(masterDataService.findAllLoadProfileTypes()).thenReturn(allLoadProfileTypes);

        Map<String, Object> map = target("/loadprofiles").request().get(Map.class);
        assertThat(map.get("total")).isEqualTo(0);
        assertThat((List) map.get("data")).isEmpty();
    }

    @Test
    public void testGetLoadProfileTypesList() throws Exception {
        List<LoadProfileType> allLoadProfileTypes = getLoadProfileTypes(20);
        when(masterDataService.findAllLoadProfileTypes()).thenReturn(allLoadProfileTypes);

        Map<String, Object> map = target("/loadprofiles").request().get(Map.class);
        assertThat(map.get("total")).isEqualTo(20);
        List loadProfiles = (List) map.get("data");
        assertThat(loadProfiles.size()).isEqualTo(20);
    }

    @Test
    public void testGetUnexistingLoadProfileType() throws Exception {
        when(masterDataService.findLoadProfileType(9999)).thenReturn(Optional.empty());

        NlsMessageFormat nlsMessageFormat = mock(NlsMessageFormat.class);
        when(thesaurus.getFormat(Matchers.<MessageSeed>anyObject())).thenReturn(nlsMessageFormat);

        Response response = target("/loadprofiles/9999").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        String answer = getServerAnswer(response);

        assertThat(answer).contains("\"success\":false").contains("\"errors\"");
    }

    @Test
    public void testGetLoadProfileType() throws Exception {
        TimeDuration interval = getRandomTimeDuration();
        LoadProfileType loadProfileType = mockLoadProfileType(1, String.format("Load Profile Type %04d", 1), interval,
                new ObisCode(10, 20, 30, 40, 50, 60), getChannelTypes(2, interval));
        when(masterDataService.findLoadProfileType(1)).thenReturn(Optional.of(loadProfileType));

        Map<String, Object> map = target("/loadprofiles/1").request().get(Map.class);
        assertThat(map.get("id")).isEqualTo(1);
        assertThat((String)map.get("name")).isEqualTo("Load Profile Type 0001");
        assertThat(map.get("obisCode")).isEqualTo("10.20.30.40.50.60");
        assertThat((List)map.get("registerTypes")).hasSize(2);
        assertThat((Integer)((Map)map.get("timeDuration")).get("id")).isBetween(0, 11);
    }

    private String getServerAnswer(Response response) {
        ByteArrayInputStream entity = (ByteArrayInputStream) response.getEntity();
        byte[] bytes = new byte[entity.available()];
        entity.read(bytes,0, entity.available());
        return new String(bytes);
    }


    private List<LoadProfileType> getLoadProfileTypes(int count) {
        List<LoadProfileType> loadProfileTypes = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) {
            TimeDuration interval = getRandomTimeDuration();
            loadProfileTypes.add(mockLoadProfileType(1000 + i, String.format("Load Profile Type %04d", i), interval,
                    new ObisCode(i, i, i, i, i, i), getChannelTypes(getRandomInt(4), interval)));
        }
        return loadProfileTypes;
    }

    private List<ChannelType> getChannelTypes(int count, TimeDuration interval) {
        List<ChannelType> channelTypes = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) {
            RegisterType registerType = mockRegisterType(1000 + i, String.format("Register type %04d", i), new ObisCode(i, i, i, i, i, i));
            channelTypes.add(mockChannelType(1000 + i, String.format("Channel type %04d", i), new ObisCode(i, i, i, i, i, i),interval ,registerType));
        }
        return channelTypes;
    }

    private int getRandomInt(int end) {
        return getRandomInt(0, end);
    }

    private int getRandomInt(int start, int end) {
        int range = end - start;
        return (int) (start + new Random().nextDouble() * range);
    }

    private TimeDuration getRandomTimeDuration(){
        return LocalizedTimeDuration.intervals.get(getRandomInt(10)).getTimeDuration();
    }

    private ObisCode mockObisCode(String code) {
        return mockObisCode(code, null);
    }

    private ObisCode mockObisCode(String code, String description) {
        ObisCode obisCode = mock(ObisCode.class);
        when(obisCode.getDescription()).thenReturn(description);
        when(obisCode.toString()).thenReturn(code);
        return obisCode;
    }

    private LoadProfileType mockLoadProfileType(long id, String name, TimeDuration interval, ObisCode obisCode, List<ChannelType> channelTypes) {
        LoadProfileType loadProfileType = mock(LoadProfileType.class);
        when(loadProfileType.getId()).thenReturn(id);
        when(loadProfileType.getName()).thenReturn(name);
        when(loadProfileType.getInterval()).thenReturn(interval);
        when(loadProfileType.getObisCode()).thenReturn(obisCode);
        when(loadProfileType.getChannelTypes()).thenReturn(channelTypes);
        return loadProfileType;
    }

    private RegisterType mockRegisterType(long id, String name, ObisCode obisCode) {
        RegisterType registerType = mock(RegisterType.class);
        when(registerType.getId()).thenReturn(id);
        when(registerType.getObisCode()).thenReturn(obisCode);
        when(registerType.getTimeOfUse()).thenReturn(0);
        when(registerType.getUnit()).thenReturn(Unit.get("kWh"));
        ReadingType readingType = mockReadingType();
        when(registerType.getReadingType()).thenReturn(readingType);
        when(readingType.getAliasName()).thenReturn(name);
        return registerType;
    }

    private ChannelType mockChannelType(long id, String name, ObisCode obisCode, TimeDuration interval, RegisterType templateRegister) {
        ChannelType channelType = mock(ChannelType.class);
        when(channelType.getId()).thenReturn(id);
        when(channelType.getObisCode()).thenReturn(obisCode);
        when(channelType.getTimeOfUse()).thenReturn(0);
        when(channelType.getUnit()).thenReturn(Unit.get("kWh"));
        ReadingType readingType = mockReadingType();
        when(readingType.getAliasName()).thenReturn(name);
        when(channelType.getReadingType()).thenReturn(readingType);
        when(channelType.getInterval()).thenReturn(interval);
        when(channelType.getTemplateRegister()).thenReturn(templateRegister);
        return channelType;
    }

    private ReadingType mockReadingType() {
        ReadingType readingType = mock(ReadingType.class);
        when(readingType.getMRID()).thenReturn("mrid");
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.DAILY);
        when(readingType.getAggregate()).thenReturn(Aggregate.AVERAGE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.FIXEDBLOCK1MIN);
        when(readingType.getAccumulation()).thenReturn(Accumulation.BULKQUANTITY);
        when(readingType.getFlowDirection()).thenReturn(FlowDirection.FORWARD);
        when(readingType.getCommodity()).thenReturn(Commodity.AIR);
        when(readingType.getMeasurementKind()).thenReturn(MeasurementKind.ACVOLTAGEPEAK);
        when(readingType.getInterharmonic()).thenReturn(new RationalNumber(1, 2));
        when(readingType.getArgument()).thenReturn(new RationalNumber(1, 2));
        when(readingType.getTou()).thenReturn(3);
        when(readingType.getCpp()).thenReturn(4);
        when(readingType.getConsumptionTier()).thenReturn(5);
        when(readingType.getPhases()).thenReturn(Phase.PHASEA);
        when(readingType.getMultiplier()).thenReturn(MetricMultiplier.CENTI);
        when(readingType.getUnit()).thenReturn(ReadingTypeUnit.AMPERE);
        when(readingType.getCurrency()).thenReturn(Currency.getInstance("EUR"));
        return readingType;
    }
}