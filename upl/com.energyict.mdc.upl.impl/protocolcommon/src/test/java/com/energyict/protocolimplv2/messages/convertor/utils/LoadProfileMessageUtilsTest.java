package com.energyict.protocolimplv2.messages.convertor.utils;

import com.energyict.mdc.upl.messages.legacy.Extractor;

import com.energyict.cbo.Unit;
import com.energyict.mdw.amr.RegisterMapping;
import com.energyict.mdw.core.Channel;
import com.energyict.mdw.core.Device;
import com.energyict.mdw.core.LoadProfile;
import com.energyict.mdw.core.LoadProfileSpec;
import com.energyict.obis.ObisCode;

import java.util.Arrays;

import org.junit.Test;

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
        Device device = createdMockedDevice();
        Channel channel1 = createdMockedChannel(device, OBISCODE1);
        Channel channel2 = createdMockedChannel(device, OBISCODE2);
        LoadProfile loadProfile = createMockedLoadProfile();
        when(loadProfile.getRtu()).thenReturn(device);
        when(loadProfile.getAllChannels()).thenReturn(Arrays.asList(channel1, channel2));
        Extractor extractor = mock(Extractor.class);
        String expectedObisCode = "1.0.99.1.0.255";
        String expectedDeviceSerialNumber = "SomeSerialNumber";
        String expectedLoadProfileId = "0";
        when(extractor.specDeviceObisCode(loadProfile)).thenReturn(expectedObisCode);
        when(extractor.deviceSerialNumber(loadProfile)).thenReturn(expectedDeviceSerialNumber);
        when(extractor.id(loadProfile)).thenReturn(expectedLoadProfileId);

        final String format = LoadProfileMessageUtils.formatLoadProfile(loadProfile, extractor);

        assertThat(format).isNotNull();
        assertThat(format).isEqualTo(expectedXml);
    }

    private LoadProfile createMockedLoadProfile() {
        LoadProfile loadProfile = mock(LoadProfile.class);
        LoadProfileSpec loadProfileSpec = mock(LoadProfileSpec.class);
        when(loadProfileSpec.getDeviceObisCode()).thenReturn(LOAD_PROFILE_OBISCODE);
        when(loadProfile.getLoadProfileSpec()).thenReturn(loadProfileSpec);
        return loadProfile;
    }

    private Channel createdMockedChannel(Device device, ObisCode obisCode) {
        Channel channel = mock(Channel.class);
        RegisterMapping registerMapping = createMockedRegisterMapping(obisCode);
        when(channel.getDevice()).thenReturn(device);
        when(channel.getDeviceRegisterMapping()).thenReturn(registerMapping);
        return channel;
    }

    private RegisterMapping createMockedRegisterMapping(ObisCode obisCode) {
        RegisterMapping registerMapping = mock(RegisterMapping.class);
        when(registerMapping.getObisCode()).thenReturn(obisCode);
        when(registerMapping.getUnit()).thenReturn(UNIT);
        return registerMapping;
    }

    private Device createdMockedDevice() {
        Device device = mock(Device.class);
        when(device.getSerialNumber()).thenReturn(METER_SERIAL_NUMBER);
        return device;
    }

}
