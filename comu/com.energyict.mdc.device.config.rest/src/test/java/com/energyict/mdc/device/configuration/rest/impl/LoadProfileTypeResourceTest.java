/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.rest.ReadingTypeInfoFactory;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.rest.RegisterTypeInfoFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LoadProfileTypeResourceTest {

    public static final long DEVICETYPE_ID = 156L;
    public static final long LPT_ID1 = 1L;
    public static final long LPT_ID2 = 2L;

    private LoadProfileTypeResource loadProfileTypeResource;

    @Mock
    private Thesaurus thesaurus;
    @Mock
    private MasterDataService masterDataService;
    @Mock
    private ResourceHelper resourceHelper;
    @Mock
    private DeviceType deviceType;
    @Mock
    private LoadProfileType loadProfileType1, loadProfileType2;

    private ReadingTypeInfoFactory readingTypeInfoFactory;
    private RegisterTypeInfoFactory registerTypeInfoFactory;
    private LoadProfileTypeOnDeviceTypeInfoFactory loadProfileTypeOnDeviceTypeInfoFactory;

    @Before
    public void setUp() {

        readingTypeInfoFactory = new ReadingTypeInfoFactory(thesaurus);
        registerTypeInfoFactory = new RegisterTypeInfoFactory(readingTypeInfoFactory);
        loadProfileTypeOnDeviceTypeInfoFactory = new LoadProfileTypeOnDeviceTypeInfoFactory(registerTypeInfoFactory);
        loadProfileTypeResource = new LoadProfileTypeResource(resourceHelper, masterDataService, thesaurus, loadProfileTypeOnDeviceTypeInfoFactory);

        when(resourceHelper.findDeviceTypeByIdOrThrowException(DEVICETYPE_ID)).thenReturn(deviceType);
        when(masterDataService.findLoadProfileType(LPT_ID1)).thenReturn(Optional.of(loadProfileType1));
        when(masterDataService.findLoadProfileType(LPT_ID2)).thenReturn(Optional.of(loadProfileType2));
        when(resourceHelper.findLoadProfileTypeByIdOrThrowException(eq(LPT_ID1))).thenReturn(loadProfileType1);
        when(resourceHelper.findLoadProfileTypeByIdOrThrowException(eq(LPT_ID2))).thenReturn(loadProfileType2);
        when(resourceHelper.findLoadProfileTypeByIdOrThrowException(eq(new Long(LPT_ID1)))).thenReturn(loadProfileType1);
        when(resourceHelper.findLoadProfileTypeByIdOrThrowException(eq(new Long(LPT_ID2)))).thenReturn(loadProfileType2);
        when(deviceType.getLoadProfileTypeCustomPropertySet(loadProfileType1)).thenReturn(Optional.<RegisteredCustomPropertySet>empty());
        when(deviceType.getLoadProfileTypeCustomPropertySet(loadProfileType2)).thenReturn(Optional.<RegisteredCustomPropertySet>empty());
    }

    @Test
    public void testAddLoadProfileTypesForDeviceType() throws Exception {
        loadProfileTypeResource.addLoadProfileTypesForDeviceType(DEVICETYPE_ID, asList(LPT_ID1, LPT_ID2), false);

        verify(deviceType).addLoadProfileType(loadProfileType1);
        verify(deviceType).addLoadProfileType(loadProfileType2);
    }

    @Test
    public void testAddAllLoadProfileTypesForDeviceType() throws Exception {
        when(masterDataService.findAllLoadProfileTypes()).thenReturn(Arrays.asList(loadProfileType1, loadProfileType2));

        loadProfileTypeResource.addLoadProfileTypesForDeviceType(DEVICETYPE_ID, Collections.<Long>emptyList(), true);

        verify(deviceType).addLoadProfileType(loadProfileType1);
        verify(deviceType).addLoadProfileType(loadProfileType2);
    }

}