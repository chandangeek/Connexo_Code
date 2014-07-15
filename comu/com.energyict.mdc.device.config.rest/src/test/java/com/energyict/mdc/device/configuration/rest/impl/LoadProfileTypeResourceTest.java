package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.MasterDataService;
import com.google.common.base.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import java.util.Arrays;
import java.util.Collections;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LoadProfileTypeResourceTest {

    public static final long DEVICETYPE_ID = 156L;
    public static final long LPT_ID1 = 1L;
    public static final long LPT_ID2 = 2L;

    private LoadProfileTypeResource loadProfileTypeResource;
    private MultivaluedMap<String, String> map = new MultivaluedHashMap<>();

    @Mock
    private Thesaurus thesaurus;
    @Mock
    private MasterDataService masterDataService;
    @Mock
    private ResourceHelper resourceHelper;
    @Mock
    private UriInfo uriInfo;
    @Mock
    private DeviceType deviceType;
    @Mock
    private LoadProfileType loadProfileType1, loadProfileType2;

    @Before
    public void setUp() {
        loadProfileTypeResource = new LoadProfileTypeResource(resourceHelper, masterDataService, thesaurus);

        when(resourceHelper.findDeviceTypeByIdOrThrowException(DEVICETYPE_ID)).thenReturn(deviceType);
        when(masterDataService.findLoadProfileType(LPT_ID1)).thenReturn(Optional.of(loadProfileType1));
        when(masterDataService.findLoadProfileType(LPT_ID2)).thenReturn(Optional.of(loadProfileType2));
        when(uriInfo.getQueryParameters()).thenReturn(map);
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testAddLoadProfileTypesForDeviceType() throws Exception {
        loadProfileTypeResource.addLoadProfileTypesForDeviceType(DEVICETYPE_ID, asList(LPT_ID1, LPT_ID2), uriInfo);

        verify(deviceType).addLoadProfileType(loadProfileType1);
        verify(deviceType).addLoadProfileType(loadProfileType2);
    }

    @Test
    public void testAddAllLoadProfileTypesForDeviceType() throws Exception {
        map.add("all", Boolean.TRUE.toString());
        when(masterDataService.findAllLoadProfileTypes()).thenReturn(Arrays.asList(loadProfileType1, loadProfileType2));

        loadProfileTypeResource.addLoadProfileTypesForDeviceType(DEVICETYPE_ID, Collections.<Long>emptyList(), uriInfo);

        verify(deviceType).addLoadProfileType(loadProfileType1);
        verify(deviceType).addLoadProfileType(loadProfileType2);
    }

}