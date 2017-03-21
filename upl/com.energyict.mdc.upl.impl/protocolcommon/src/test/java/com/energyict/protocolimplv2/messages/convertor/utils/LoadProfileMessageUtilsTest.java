package com.energyict.protocolimplv2.messages.convertor.utils;

import com.energyict.cbo.Unit;
import com.energyict.mdc.upl.messages.legacy.LoadProfileExtractor;
import com.energyict.mdc.upl.meterdata.LoadProfile;
import com.energyict.obis.ObisCode;
import org.junit.Test;

import java.util.Arrays;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Copyrights EnergyICT
 * Date: 3/05/13
 * Time: 10:53
 */
public class LoadProfileMessageUtilsTest {

    private static final String METER_SERIAL_NUMBER = "SomeSerialNumber";
    private static final ObisCode LOAD_PROFILE_OBISCODE = ObisCode.fromString("1.0.99.1.0.255");
    private static final ObisCode OBISCODE1 = ObisCode.fromString("1.0.1.8.0.255");
    private static final ObisCode OBISCODE2 = ObisCode.fromString("1.0.2.8.0.255");
    private static final Unit UNIT = Unit.get("kWh");

    private static final String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><LoadProfile LPId=\"0\" LPObisCode=\"1.0.99.1.0.255\" MSerial=\"SomeSerialNumber\"><Channels><Ch ID=\"SomeSerialNumber\" Id=\"0\" Name=\"1.0.1.8.0.255\" Unit=\"kWh\"/><Ch ID=\"SomeSerialNumber\" Id=\"1\" Name=\"1.0.2.8.0.255\" Unit=\"kWh\"/></Channels><RtuRegs><Reg ID=\"SomeSerialNumber\" OC=\"1.0.1.8.0.255\"/><Reg ID=\"SomeSerialNumber\" OC=\"1.0.2.8.0.255\"/></RtuRegs></LoadProfile>";

    @Test
    public void loadProfileFormatTest() {
        LoadProfileExtractor.Channel channel1 = createdMockedChannel(OBISCODE1);
        LoadProfileExtractor.Channel channel2 = createdMockedChannel(OBISCODE2);
        LoadProfile loadProfile = mock(LoadProfile.class);
        LoadProfileExtractor loadProfileExtractor = mock(LoadProfileExtractor.class);
        String expectedLoadProfileId = "0";
        when(loadProfileExtractor.specDeviceObisCode(loadProfile)).thenReturn(LOAD_PROFILE_OBISCODE.toString());
        when(loadProfileExtractor.deviceSerialNumber(loadProfile)).thenReturn(METER_SERIAL_NUMBER);
        when(loadProfileExtractor.id(loadProfile)).thenReturn(expectedLoadProfileId);
        when(loadProfileExtractor.channels(loadProfile)).thenReturn(Arrays.asList(channel1, channel2));

        final String format = LoadProfileMessageUtils.formatLoadProfile(loadProfile, loadProfileExtractor);

        assertThat(format).isNotNull();
        assertThat(format).isEqualTo(expectedXml);
    }

    private LoadProfileExtractor.Channel createdMockedChannel(ObisCode obisCode) {
        LoadProfileExtractor.Channel channel = mock(LoadProfileExtractor.Channel.class);
        when(channel.deviceSerialNumber()).thenReturn(METER_SERIAL_NUMBER);
        when(channel.obisCode()).thenReturn(obisCode.toString());
        when(channel.unit()).thenReturn(UNIT.toString());
        return channel;
    }
}