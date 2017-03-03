package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.cbo.ElectronicAddress;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.impl.IMeterActivation;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Copyrights EnergyICT
 * Date: 31/01/2017
 * Time: 8:56
 */
@RunWith(MockitoJUnitRunner.class)
public class MeterInfoTest {

    private final static long ID = 965433;
    private final static String ALIASNAME = "The aliasName";
    private final static String DESCRIPTION = "The description";
    private final static String MRID = "The MRID";
    private final static String NAME = "The meter's name";
    private final static String SERIAL = "The meter's serial";
    private final static String MANUFACTURER = "The manufacturer";
    private final static String MODELNBR = "The model number";
    private final static String MODELVERSION = "The model version";
    private final static String UTC_NUMBER = "The utc number";
    private final static String ELECTRONIC_ADDRESS1 = "The meter's electronic address part 1";
    private final static String ELECTRONIC_ADDRESS2 = "The meter's electronic address part 2";
    private final static String AMRSYSTEM_NAME = "AmrSystem name";
    private final static long METER_VERSION = 32;
    private final static String USAGEPOINT_NAME = "The usage point name";
    private final static String USAGEPOINT_MRID = "The usage point MRID";

    @Mock
    private Meter meter;

    private ElectronicAddress electronicAddress;
    @Mock
    private AmrSystem amrSystem;

    private MeterActivation meterActivation;
    @Mock
    private UsagePoint usagePoint;

    @Before
    public void initMocks(){
        electronicAddress = new ElectronicAddress();
        electronicAddress.setEmail1(ELECTRONIC_ADDRESS1);
        electronicAddress.setEmail2(ELECTRONIC_ADDRESS2);

        when(amrSystem.getName()).thenReturn(AMRSYSTEM_NAME);

        when(usagePoint.getName()).thenReturn(USAGEPOINT_NAME);
        when(usagePoint.getMRID()).thenReturn(USAGEPOINT_MRID);
        when(usagePoint.getMeterActivations(any(Instant.class))).thenReturn(Collections.singletonList(meterActivation));

        MeterActivation meterActivation = mock(MeterActivation.class);
        //when(meterActivation.getMeter()).thenReturn(Optional.of(meter));
        when(meterActivation.getUsagePoint()).thenReturn(Optional.of(usagePoint));

        when(meter.getId()).thenReturn(ID);
        when(meter.getAliasName()).thenReturn(ALIASNAME);
        when(meter.getDescription()).thenReturn(DESCRIPTION);
        when(meter.getMRID()).thenReturn(MRID);
        when(meter.getName()).thenReturn(NAME);
        when(meter.getSerialNumber()).thenReturn(SERIAL);
        when(meter.getManufacturer()).thenReturn(MANUFACTURER);
        when(meter.getModelNumber()).thenReturn(MODELNBR);
        when(meter.getModelVersion()).thenReturn(MODELVERSION);
        when(meter.getUtcNumber()).thenReturn(UTC_NUMBER);
        when(meter.getElectronicAddress()).thenReturn(electronicAddress);
        when(meter.getAmrSystem()).thenReturn(amrSystem);
        when(meter.getVersion()).thenReturn(METER_VERSION);

        doReturn(Optional.of(meterActivation)).when(meter).getCurrentMeterActivation();
    }

    @Test
    public void testConstructor(){
         MeterInfo info = new MeterInfo(meter);
        assertThat(info.id).isEqualTo(ID);
        assertThat(info.aliasName).isEqualTo(ALIASNAME);
        assertThat(info.description).isEqualTo(DESCRIPTION);
        assertThat(info.mRID).isEqualTo(MRID);
        assertThat(info.name).isEqualTo(NAME);
        assertThat(info.serialNumber).isEqualTo(SERIAL);
        assertThat(info.manufacturer).isEqualTo(MANUFACTURER);
        assertThat(info.modelNbr).isEqualTo(MODELNBR);
        assertThat(info.modelVersion).isEqualTo(MODELVERSION);
        assertThat(info.utcNumber).isEqualTo(UTC_NUMBER);
        assertThat(info.eMail1).isEqualTo(ELECTRONIC_ADDRESS1);
        assertThat(info.eMail2).isEqualTo(ELECTRONIC_ADDRESS2);
        assertThat(info.amrSystemName).isEqualTo(AMRSYSTEM_NAME);
        assertThat(info.version).isEqualTo(METER_VERSION);
        assertThat(info.usagePointMRId).isEqualTo(USAGEPOINT_MRID);
        assertThat(info.usagePointName).isEqualTo(USAGEPOINT_NAME);
    }
}
