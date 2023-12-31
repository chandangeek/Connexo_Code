/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.master.data.rest.impl;

import com.elster.jupiter.cbo.Commodity;
import com.elster.jupiter.cbo.MacroPeriod;
import com.elster.jupiter.cbo.TimeAttribute;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.masterdata.ChannelType;
import com.energyict.mdc.common.masterdata.LoadProfileType;
import com.energyict.mdc.common.masterdata.RegisterType;
import com.energyict.mdc.masterdata.rest.LoadProfileTypeInfo;

import com.energyict.obis.ObisCode;
import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LoadProfileTypeResourceTest extends MasterDataApplicationJerseyTest {
    public static final long OK_VERSION = 11;
    public static final long BAD_VERSION = 8;
    private static final long LOADPROFILE_ID = 1L;

    @Test
    public void testIntervalsList() throws Exception {
        List<Object> intervals = target("/loadprofiles/intervals").request().get(List.class);
        assertThat(intervals).hasSize(16);
        assertThat(((Map) intervals.get(0)).get("name")).isEqualTo("1 minute");
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

        Response response = target("/loadprofiles/9999").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetLoadProfileType() throws Exception {
        TimeDuration interval = getTimeDuration();
        LoadProfileType loadProfileType = mockLoadProfileType(1, String.format("Load Profile Type %04d", 1), interval,
                new ObisCode(10, 20, 30, 40, 50, 60), getChannelTypes(2, interval));
        when(masterDataService.findLoadProfileType(1)).thenReturn(Optional.of(loadProfileType));

        Map<String, Object> map = target("/loadprofiles/1").request().get(Map.class);
        assertThat(map.get("id")).isEqualTo(1);
        assertThat((String) map.get("name")).isEqualTo("Load Profile Type 0001");
        assertThat(map.get("obisCode")).isEqualTo("10.20.30.40.50.60");
        assertThat((List) map.get("registerTypes")).hasSize(2);
        assertThat((Integer) ((Map) map.get("timeDuration")).get("id")).isBetween(0, 11);
    }

    @Test
    public void testGetAvailableRegisterTypes() {
        Finder<RegisterType> finder = mock(Finder.class);
        when(masterDataService.findAllRegisterTypes()).thenReturn(finder);
        RegisterType registerType = mockRegisterType(1, "Bulk A+", ObisCode.fromString("0.0.0.0.0.0."));
        ReadingType readingType = registerType.getReadingType();
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.NOTAPPLICABLE);
        when(readingType.getCommodity()).thenReturn(Commodity.ELECTRICITY_PRIMARY_METERED);
        List<RegisterType> registerTypes = Arrays.asList(registerType);
        doAnswer(invocationOnMock -> registerTypes.stream()).when(finder).stream();

        String response = target("loadprofiles/measurementtypes").queryParam("start", 0).queryParam("limit", 10).request().get(String.class);

        JsonModel model = JsonModel.model(response);
        assertThat(model.<Number>get("$.total")).isEqualTo(1);
        assertThat(model.<List<?>>get("$.registerTypes")).hasSize(1);
        assertThat(model.<Number>get("$.registerTypes[0].id")).isEqualTo(1);
        assertThat(model.<String>get("$.registerTypes[0].obisCode")).isEqualTo("0.0.0.0.0.0");
        assertThat(model.<Boolean>get("$registerTypes[0].isLinkedByDeviceType")).isFalse();
        assertThat(model.<String>get("$registerTypes[0].readingType.aliasName")).isEqualTo("Bulk A+");
    }

    @Test
    public void testGetAvailableRegisterTypesWithExcludedId() throws Exception {
        Finder<RegisterType> finder = mock(Finder.class);
        when(masterDataService.findAllRegisterTypes()).thenReturn(finder);
        RegisterType registerType = mockRegisterType(13, "Bulk A+", ObisCode.fromString("0.0.0.0.0.0."));
        ReadingType readingType = registerType.getReadingType();
        when(readingType.getMacroPeriod()).thenReturn(MacroPeriod.NOTAPPLICABLE);
        when(readingType.getMeasuringPeriod()).thenReturn(TimeAttribute.NOTAPPLICABLE);
        when(readingType.getCommodity()).thenReturn(Commodity.ELECTRICITY_PRIMARY_METERED);
        List<RegisterType> registerTypes = Arrays.asList(registerType);
        doAnswer(invocationOnMock -> registerTypes.stream()).when(finder).stream();

        String response = target("loadprofiles/measurementtypes")
                .queryParam("start", 0)
                .queryParam("limit", 10)
                .queryParam("filter", URLEncoder.encode("[{\"property\":\"ids\",\"value\":[13]}]", "UTF-8"))
                .request().get(String.class);

        JsonModel model = JsonModel.model(response);
        assertThat(model.<Number>get("$.total")).isEqualTo(0);
        assertThat(model.<List<?>>get("$.registerTypes")).isEmpty();
    }

    private String getServerAnswer(Response response) {
        ByteArrayInputStream entity = (ByteArrayInputStream) response.getEntity();
        byte[] bytes = new byte[entity.available()];
        entity.read(bytes, 0, entity.available());
        return new String(bytes);
    }


    private List<LoadProfileType> getLoadProfileTypes(int count) {
        List<LoadProfileType> loadProfileTypes = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) {
            TimeDuration interval = getTimeDuration();
            loadProfileTypes.add(mockLoadProfileType(1000 + i, String.format("Load Profile Type %04d", i), interval,
                    new ObisCode(i, i, i, i, i, i), getChannelTypes(getRandomInt(4), interval)));
        }
        return loadProfileTypes;
    }

    private List<ChannelType> getChannelTypes(int count, TimeDuration interval) {
        List<ChannelType> channelTypes = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) {
            RegisterType registerType = mockRegisterType(1000 + i, String.format("Register type %04d", i), new ObisCode(i, i, i, i, i, i));
            channelTypes.add(mockChannelType(1000 + i, String.format("Channel type %04d", i), new ObisCode(i, i, i, i, i, i), interval, registerType));
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

    private TimeDuration getTimeDuration() {
        return new TimeDuration(5, TimeDuration.TimeUnit.MINUTES);
    }

    private ObisCode mockObisCode(String code) {
        ObisCode obisCode = mock(ObisCode.class);
        when(obisCode.toString()).thenReturn(code);
        return obisCode;
    }

    private LoadProfileType mockLoadProfileType(long id, String name, TimeDuration interval, ObisCode obisCode, List<ChannelType> channelTypes) {
        LoadProfileType loadProfileType = mock(LoadProfileType.class);
        when(loadProfileType.getId()).thenReturn(id);
        when(loadProfileType.getName()).thenReturn(name);
        when(loadProfileType.interval()).thenReturn(interval.asTemporalAmount());
        when(loadProfileType.getObisCode()).thenReturn(obisCode);
        when(loadProfileType.getChannelTypes()).thenReturn(channelTypes);
        when(loadProfileType.getVersion()).thenReturn(OK_VERSION);


        when(masterDataService.findLoadProfileType(id)).thenReturn(Optional.of(loadProfileType));
        when(masterDataService.findAndLockLoadProfileTypeByIdAndVersion(id, OK_VERSION)).thenReturn(Optional.of(loadProfileType));
        when(masterDataService.findAndLockLoadProfileTypeByIdAndVersion(id, BAD_VERSION)).thenReturn(Optional.empty());
        return loadProfileType;
    }

    private RegisterType mockRegisterType(long id, String name, ObisCode obisCode) {
        RegisterType registerType = mock(RegisterType.class);
        when(registerType.getId()).thenReturn(id);
        when(registerType.getObisCode()).thenReturn(obisCode);
        when(registerType.getTimeOfUse()).thenReturn(0);
        ReadingType readingType = mockReadingType();
        when(registerType.getReadingType()).thenReturn(readingType);
        when(readingType.getAliasName()).thenReturn(name);
        when(masterDataService.findRegisterType(1001)).thenReturn(Optional.of(registerType));
        when(masterDataService.findRegisterType(1002)).thenReturn(Optional.of(registerType));
        return registerType;
    }

    private ChannelType mockChannelType(long id, String name, ObisCode obisCode, TimeDuration interval, RegisterType templateRegister) {
        ChannelType channelType = mock(ChannelType.class);
        when(channelType.getId()).thenReturn(id);
        when(channelType.getObisCode()).thenReturn(obisCode);
        when(channelType.getTimeOfUse()).thenReturn(0);
        ReadingType readingType = mockReadingType();
        when(readingType.getAliasName()).thenReturn(name);
        when(channelType.getReadingType()).thenReturn(readingType);
        when(channelType.getInterval()).thenReturn(interval);
        when(channelType.getTemplateRegister()).thenReturn(templateRegister);
        return channelType;
    }

    @Test
    public void testUpdateLoadProfileTypeOkVersion() {
        TimeDuration interval = getTimeDuration();
        LoadProfileType loadProfile = mockLoadProfileType(LOADPROFILE_ID, String.format("Load Profile Type %04d", 1), interval,
                new ObisCode(10, 20, 30, 40, 50, 60), getChannelTypes(2, interval));
        LoadProfileTypeInfo info = loadProfileTypeInfoFactory.from(loadProfile, false);
        info.name = "new name";
        info.version = OK_VERSION;
        Response response = target("/loadprofiles/" + LOADPROFILE_ID).request().build(HttpMethod.PUT, Entity.json(info)).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(loadProfile, times(1)).setName("new name");
    }

    @Test
    public void testUpdateLoadPrifileTypeBadVersion() {
        TimeDuration interval = getTimeDuration();
        LoadProfileType loadProfile = mockLoadProfileType(1, String.format("Load Profile Type %04d", 1), interval,
                new ObisCode(10, 20, 30, 40, 50, 60), getChannelTypes(2, interval));
        LoadProfileTypeInfo info = loadProfileTypeInfoFactory.from(loadProfile, false);
        info.name = "new name";
        info.version = BAD_VERSION;
        Response response = target("/loadprofiles/" + LOADPROFILE_ID).request().build(HttpMethod.PUT, Entity.json(info)).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        verify(loadProfile, never()).setName("new name");
    }

    @Test
    public void testDeleteLoadProfileTypeOkVersion() {
        TimeDuration interval = getTimeDuration();
        LoadProfileType loadProfile = mockLoadProfileType(1, String.format("Load Profile Type %04d", 1), interval,
                new ObisCode(10, 20, 30, 40, 50, 60), getChannelTypes(2, interval));
        LoadProfileTypeInfo info = loadProfileTypeInfoFactory.from(loadProfile, false);
        info.name = "new name";
        Response response = target("/loadprofiles/" + LOADPROFILE_ID).request().build(HttpMethod.DELETE, Entity.json(info)).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(loadProfile, times(1)).delete();
    }

    @Test
    public void testDeleteLogBookTypeBadVersion() {
        TimeDuration interval = getTimeDuration();
        LoadProfileType loadProfile = mockLoadProfileType(1, String.format("Load Profile Type %04d", 1), interval,
                new ObisCode(10, 20, 30, 40, 50, 60), getChannelTypes(2, interval));
        LoadProfileTypeInfo info = loadProfileTypeInfoFactory.from(loadProfile, false);
        info.name = "new name";
        info.version = BAD_VERSION;
        Response response = target("/loadprofiles/" + LOADPROFILE_ID).request().build(HttpMethod.DELETE, Entity.json(info)).invoke();
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
        verify(loadProfile, never()).delete();
    }

}