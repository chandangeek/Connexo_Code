package com.energyict.protocolimplv2.messages.convertor.utils;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.device.Channel;
import com.energyict.mdc.protocol.api.device.BaseDevice;
import com.energyict.mdc.protocol.api.device.LoadProfile;
import org.junit.*;

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
        BaseDevice device = createdMockedDevice();
        Channel channel1 = createdMockedChannel(device, OBISCODE1);
        Channel channel2 = createdMockedChannel(device, OBISCODE2);
        LoadProfile loadProfile = createMockedLoadProfile();
        when(loadProfile.getDevice()).thenReturn(device);
        when(loadProfile.getAllChannels()).thenReturn(Arrays.asList(channel1, channel2));

        final String format = LoadProfileMessageUtils.formatLoadProfile(loadProfile);

        assertThat(format).isNotNull();
        assertThat(format).isEqualTo(expectedXml);
    }

    private LoadProfile createMockedLoadProfile() {
        LoadProfile loadProfile = mock(LoadProfile.class);
        when(loadProfile.getDeviceObisCode()).thenReturn(LOAD_PROFILE_OBISCODE);
        return loadProfile;
    }

    private Channel createdMockedChannel(BaseDevice device, ObisCode obisCode) {
        Channel channel = mock(Channel.class);
        when(channel.getDevice()).thenReturn(device);
        when(channel.getRegisterTypeObisCode()).thenReturn(obisCode);
        when(channel.getDeviceRegisterMappingUnit()).thenReturn(UNIT);
        return channel;
    }

    private BaseDevice createdMockedDevice() {
        BaseDevice device = mock(BaseDevice.class);
        when(device.getSerialNumber()).thenReturn(METER_SERIAL_NUMBER);
        return device;
    }

}
