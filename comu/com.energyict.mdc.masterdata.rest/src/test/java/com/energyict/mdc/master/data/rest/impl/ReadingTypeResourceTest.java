package com.energyict.mdc.master.data.rest.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ReadingTypeFilter;

import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.metering.ReadingTypeInformation;

import com.energyict.obis.ObisCode;
import com.jayway.jsonpath.JsonModel;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ReadingTypeResourceTest extends MasterDataApplicationJerseyTest {

    private final static String obis = "1.0.61.8.0.255";
    private final static String invalidObis = "1.0.61.8.500.255";

    @Before
    public void setUpFindAllRegisterTypes() {
        Finder<RegisterType> registerTypeFinder = mockFinder(Collections.emptyList());
        when(masterDataService.findAllRegisterTypes()).thenReturn(registerTypeFinder);
    }

    @Before
    public void setUpGetReadingTypeFilterFrom() {
        ObisCode code = ObisCode.fromString(obis);
        when(mdcReadingTypeUtilService.getReadingTypeFilterFrom(code)).thenReturn("");
    }

    @Before
    public void setUpFindReadingTypes() {
        ReadingType rt1 = mockReadingType();
        Finder<ReadingType> readingTypeFinder = mockFinder(Arrays.asList(rt1, rt1));
        ArgumentCaptor<ReadingTypeFilter> readingTypeFilterArgumentCaptor = ArgumentCaptor.forClass(ReadingTypeFilter.class);
        when(meteringService.findReadingTypes(readingTypeFilterArgumentCaptor.capture())).thenReturn(readingTypeFinder);
    }

    @Test
    public void testWithValidObis() {
        String response = target("/unusedreadingtypes").queryParam("obisCode", obis).request().get(String.class);
        JsonModel model = JsonModel.model(response);
        assertThat(model.<Integer>get("$.total")).isEqualTo(2);
        assertThat(model.<Boolean>get("$.mappingError")).isFalse();
    }

    @Test
    public void testWithInvalidObis() {
        String response = target("/unusedreadingtypes").queryParam("obisCode", invalidObis).request().get(String.class);
        JsonModel model = JsonModel.model(response);
        assertThat(model.<Integer>get("$.total")).isEqualTo(2);
        assertThat(model.<Boolean>get("$.mappingError")).isTrue();
    }

    @Test
    public void testWithLikeParam() {
        String response = target("/unusedreadingtypes").queryParam("like", "someText").request().get(String.class);
        JsonModel model = JsonModel.model(response);
        assertThat(model.<Integer>get("$.total")).isEqualTo(2);
        assertThat(model.<Boolean>get("$.mappingError")).isTrue();
    }

    @Test
    public void testWithExistingRegisters() {
        RegisterType registerType = mockRegisterType();
        Finder<RegisterType> registerTypeFinder = mockFinder(Collections.singletonList(registerType));
        when(masterDataService.findAllRegisterTypes()).thenReturn(registerTypeFinder);

        String response = target("/unusedreadingtypes").queryParam("like", "someText").request().get(String.class);
        JsonModel model = JsonModel.model(response);
        assertThat(model.<Integer>get("$.total")).isEqualTo(0);
        assertThat(model.<Boolean>get("$.mappingError")).isTrue();
    }

    private RegisterType mockRegisterType() {
        RegisterType registerType = mock(RegisterType.class);
        ReadingType rt = mockReadingType();
        when(registerType.getReadingType()).thenReturn(rt);
        return registerType;
    }


    @Test
    public void testAddReadingTypeFromObis() {
        ObisCode code = ObisCode.fromString(obis);
        when(mdcReadingTypeUtilService.getReadingTypeFilterFrom(code)).thenReturn("0\\.0\\.0\\.(1|2)\\.1\\.1\\.12\\.0\\.0\\.0\\.0\\.0\\.0\\.0\\.32\\.0\\.72\\.0");
        String response = target("/mappedReadingType/"+obis).request().get(String.class);
        JsonModel model = JsonModel.model(response);
        assertThat(model.<String>get("$.response")).isEqualTo("0.0.0.0.1.1.12.0.0.0.0.0.0.0.32.0.72.0");

    }

    @Test
    public void testMapMridToObisCode() {
        String mrid = "0.0.0.1.1.1.12.0.0.0.0.0.0.0.32.0.72.0";
        ReadingTypeInformation info = new ReadingTypeInformation(ObisCode.fromString(obis), null, null);
        when(mdcReadingTypeUtilService.getReadingTypeInformationFrom(mrid)).thenReturn(Optional.of(info));
        String response = target("/mappedObisCode/"+mrid).request().get(String.class);
        JsonModel model = JsonModel.model(response);
        assertThat(model.<String>get("$.response")).isEqualTo(obis);
    }
}
