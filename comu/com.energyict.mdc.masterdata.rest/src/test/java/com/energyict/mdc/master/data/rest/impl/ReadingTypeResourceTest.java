package com.energyict.mdc.master.data.rest.impl;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.ReadingTypeFilter;

import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.masterdata.rest.impl.MessageSeeds;

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
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ReadingTypeResourceTest extends MasterDataApplicationJerseyTest {

    private final static String obis = "1.0.61.8.0.255";
    private final static String invalidObis = "1.0.61.8.500.255";
    private final static String mrid = "0.0.0.0.1.1.12.0.0.0.0.0.0.0.32.0.72.0";
    private final static String invalidMRID = "0.0.0.0.1.999.12.0.0.0.0.0.0.0.32.0.72.0";
    private final static String readingTypeFilter = "0\\.0\\.0\\.(1|2)\\.1\\.1\\.12\\.0\\.0\\.0\\.0\\.0\\.0\\.0\\.32\\.0\\.72\\.0";

    @Before
    public void setupNoRegistersInUse() {
        Finder<RegisterType> registerTypeFinder = mockFinder(Collections.emptyList());
        when(masterDataService.findAllRegisterTypes()).thenReturn(registerTypeFinder);
    }

    @Before
    public void setupFindTwoReadingTypes() {
        this.setupValidReadingTypeFilter();
        ReadingType rt1 = mockReadingType1();
        ReadingType rt2 = mockReadingType2();
        Finder<ReadingType> readingTypeFinder = mockFinder(Arrays.asList(rt1, rt2));
        ArgumentCaptor<ReadingTypeFilter> readingTypeFilterArgumentCaptor = ArgumentCaptor.forClass(ReadingTypeFilter.class);
        when(meteringService.findReadingTypes(readingTypeFilterArgumentCaptor.capture())).thenReturn(readingTypeFinder);
    }


    @Test
    public void whenNoQueryParamsThenNoErrorAndDisplayAllReadingTypes(){
        String response = target("/unusedreadingtypes").request().get(String.class);

        JsonModel model = JsonModel.model(response);
        assertThat(model.<Integer>get("$.total")).isEqualTo(2);
        assertThat(model.<Boolean>get("$.mappingError")).isNull();
    }

    @Test
    public void whenValidObisThenNoErrorAndDisplayAllMappedReadingTypes() {
        String response = target("/unusedreadingtypes").queryParam("obisCode", obis).request().get(String.class);

        JsonModel model = JsonModel.model(response);
        assertThat(model.<Integer>get("$.total")).isEqualTo(2);
        assertThat(model.<Boolean>get("$.mappingError")).isNull();
    }

    @Test
    public void whenLikeParamThenNoErrorAndDisplayAllMappedReadingTypes() {
        String response = target("/unusedreadingtypes").queryParam("like", "reading").request().get(String.class);

        JsonModel model = JsonModel.model(response);
        assertThat(model.<Integer>get("$.total")).isEqualTo(2);
        assertThat(model.<Boolean>get("$.mappingError")).isNull();
    }

    @Test
    public void whenValidMRIDParamThenDisplaySingleMappedReadingType() {
        ReadingType readingType = mockReadingType1();
        when(meteringService.getReadingType(mrid)).thenReturn(Optional.of(readingType));

        String response = target("/unusedreadingtypes").queryParam("mRID", mrid).request().get(String.class);

        JsonModel model = JsonModel.model(response);
        assertThat(model.<Integer>get("$.total")).isEqualTo(1);
        assertThat(model.<Boolean>get("$.mappingError")).isNull();
        assertThat(model.<String>get("$.readingTypes[0].aliasName")).isEqualTo("first reading type");
    }

    @Test
    public void whenInvalidMRIDParamThenNoErrorAndEmptyResponse() {
        when(meteringService.getReadingType(invalidMRID)).thenReturn(Optional.empty());

        String response = target("/unusedreadingtypes").queryParam("mRID", invalidMRID).request().get(String.class);

        JsonModel model = JsonModel.model(response);
        assertThat(model.<Integer>get("$.total")).isEqualTo(0);
        assertThat(model.<Boolean>get("$.mappingError")).isNull();
    }

    @Test
    public void whenInvalidObisThenErrorAndDisplayAllReadingTypes() {
        String response = target("/unusedreadingtypes").queryParam("obisCode", invalidObis).request().get(String.class);

        JsonModel model = JsonModel.model(response);
        assertThat(model.<Integer>get("$.total")).isEqualTo(2);
        assertThat(model.<String>get("$.mappingError")).isEqualTo(MessageSeeds.NO_OBIS_TO_READING_TYPE_MAPPING_POSSIBLE.getDefaultFormat());
    }


    @Test
    public void whenObisMapsToOneReadingTypeAndTheOtherOneInUseThenDisplaySingleMappedReadingType() {
        this.setupOneRegisterInUse();

        String response = target("/unusedreadingtypes").queryParam("obisCode", obis).request().get(String.class);

        JsonModel model = JsonModel.model(response);
        assertThat(model.<Integer>get("$.total")).isEqualTo(1);
        assertThat(model.<Boolean>get("$.mappingError")).isNull();
    }


    @Test
    public void whenValidObisAndAllReadingTypesAreInUseThenDisplayNoneAndShowError() {
        this.setupAllRegisterInUse();

        String response = target("/unusedreadingtypes").queryParam("obisCode", obis).request().get(String.class);

        JsonModel model = JsonModel.model(response);
        assertThat(model.<Integer>get("$.total")).isEqualTo(0);
        assertThat(model.<String>get("$.mappingError")).isEqualTo(MessageSeeds.MAPPED_READING_TYPE_IS_IN_USE.getDefaultFormat());
    }


    @Test
    public void whenValidObisThenReturnNonEmptyStringResponse() {
        ObisCode code = ObisCode.fromString(obis);
        when(mdcReadingTypeUtilService.getReadingTypeFilterFrom(code)).thenReturn(readingTypeFilter);

        String response = target("/mappedReadingType/"+obis).request().get(String.class);

        JsonModel model = JsonModel.model(response);
        assertThat(model.<String>get("$.response")).isEqualTo(mrid);
    }

    @Test
    public void whenInvalidObisThenReturnEmptyStringResponse() {
        this.setupInvalidReadingTypeFilter();

        String response = target("/mappedReadingType/"+invalidObis).request().get(String.class);

        JsonModel model = JsonModel.model(response);
        assertThat(model.<String>get("$.response")).isEmpty();
    }

    @Test
    public void whenValidMridThenReturnNonEmptyStringResponse() {
        ReadingTypeInformation info = new ReadingTypeInformation(ObisCode.fromString(obis), null, null);
        when(mdcReadingTypeUtilService.getReadingTypeInformationFrom(mrid)).thenReturn(Optional.of(info));

        String response = target("/mappedObisCode/"+mrid).request().get(String.class);

        JsonModel model = JsonModel.model(response);
        assertThat(model.<String>get("$.response")).isEqualTo(obis);
    }

    @Test
    public void whenInvalidMridThenReturnEmptyStringResponse() {
        when(mdcReadingTypeUtilService.getReadingTypeInformationFrom(invalidMRID)).thenReturn(Optional.empty());

        String response = target("/mappedObisCode/"+invalidMRID).request().get(String.class);

        JsonModel model = JsonModel.model(response);
        assertThat(model.<String>get("$.response")).isEmpty();
    }

    private void setupValidReadingTypeFilter(){
        when(mdcReadingTypeUtilService.getReadingTypeFilterFrom(anyObject())).thenReturn(readingTypeFilter);
    }

    private void setupInvalidReadingTypeFilter(){
        when(mdcReadingTypeUtilService.getReadingTypeFilterFrom(anyObject())).thenReturn("");
    }


    private void setupAllRegisterInUse() {
        RegisterType registerType1 = this.mockRegisterType1();
        RegisterType registerType2 = this.mockRegisterType2();
        Finder<RegisterType> registerTypeFinder = mockFinder(Arrays.asList(registerType1,registerType2));
        when(masterDataService.findAllRegisterTypes()).thenReturn(registerTypeFinder);
    }

    private void setupOneRegisterInUse() {
        RegisterType registerType = this.mockRegisterType1();
        Finder<RegisterType> registerTypeFinder = mockFinder(Collections.singletonList(registerType));
        when(masterDataService.findAllRegisterTypes()).thenReturn(registerTypeFinder);
    }


    private ReadingType mockReadingType1(){
        ReadingType readingType = mockReadingType();
        when(readingType.getAliasName()).thenReturn("first reading type");
        when(readingType.getMRID()).thenReturn("mrid1");
        return readingType;
    }


    private ReadingType mockReadingType2(){
        ReadingType readingType = mockReadingType();
        when(readingType.getAliasName()).thenReturn("second reading type");
        when(readingType.getMRID()).thenReturn("mrid2");
        return readingType;
    }

    private RegisterType mockRegisterType1() {
        RegisterType registerType = mock(RegisterType.class);
        ReadingType rt = this.mockReadingType1();
        when(registerType.getReadingType()).thenReturn(rt);
        return registerType;
    }

    private RegisterType mockRegisterType2() {
        RegisterType registerType = mock(RegisterType.class);
        ReadingType rt = this.mockReadingType2();
        when(registerType.getReadingType()).thenReturn(rt);
        return registerType;
    }

}
