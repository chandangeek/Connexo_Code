package com.energyict.mdc.engine.impl;

import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.device.config.ChannelSpec;
import com.energyict.mdc.common.device.config.LoadProfileSpec;
import com.energyict.mdc.common.device.data.Channel;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.LoadProfile;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.messages.legacy.LoadProfileExtractor;

import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;

import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link LoadProfileExtractorImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-19 (09:48)
 */
@RunWith(MockitoJUnitRunner.class)
public class LoadProfileExtractorImplTest {

    private static final long ID = 97;
    public static final String DEVICE_OBIS_CODE = "1.2.3.4.5.6";
    public static final String DEVICE_SERIAL_NUMBER = "LoadProfileExtractorImplTest";

    @Mock
    private MdcReadingTypeUtilService readingTypeUtilService;
    @Mock
    private LoadProfile loadProfile;
    @Mock
    private LoadProfileSpec loadProfileSpec;
    @Mock
    private Device device;

    @Before
    public void initializeMocks() {
        when(this.readingTypeUtilService.getMdcUnitsFor(any(ReadingType.class))).thenReturn(Collections.singletonList(Unit.get("kWh")));
        when(this.loadProfile.getId()).thenReturn(ID);
        when(this.loadProfile.getLoadProfileSpec()).thenReturn(this.loadProfileSpec);
        when(this.loadProfileSpec.getDeviceObisCode()).thenReturn(ObisCode.fromString(DEVICE_OBIS_CODE));
        when(this.loadProfile.getDevice()).thenReturn(this.device);
        when(this.device.getSerialNumber()).thenReturn(DEVICE_SERIAL_NUMBER);
    }

    @After
    public void cleanServices() {
        Services.loadProfileExtractor(null);
    }

    @Test
    public void activate() {
        // Make sure there is no LoadProfileExtractor service
        Services.loadProfileExtractor(null);

        // Business method
        this.getInstance().activate();

        // Asserts
        assertThat(Services.loadProfileExtractor()).isNotNull();
    }

    @Test
    public void extractId() {
        LoadProfileExtractor extractor = this.getInstance();

        // Business method
        String id = extractor.id(this.loadProfile);

        // Asserts
        assertThat(id).isEqualTo(Long.toString(ID));
    }

    @Test
    public void extractSpecDeviceObisCode() {
        LoadProfileExtractor extractor = this.getInstance();

        // Business method
        String specDeviceObisCode = extractor.specDeviceObisCode(this.loadProfile);

        // Asserts
        assertThat(specDeviceObisCode).isEqualTo(DEVICE_OBIS_CODE);
    }

    @Test
    public void extractDeviceSerialNumber() {
        LoadProfileExtractor extractor = this.getInstance();

        // Business method
        String serialNumber = extractor.deviceSerialNumber(this.loadProfile);

        // Asserts
        assertThat(serialNumber).isEqualTo(DEVICE_SERIAL_NUMBER);
    }

    @Test
    public void noChannels() {
        LoadProfileExtractor extractor = this.getInstance();
        when(this.loadProfile.getChannels()).thenReturn(Collections.emptyList());

        // Business method
        List<LoadProfileExtractor.Channel> channels = extractor.channels(this.loadProfile);

        // Asserts
        assertThat(channels).isEmpty();
    }

    @Test
    public void oneChannel() {
        LoadProfileExtractor extractor = this.getInstance();
        ChannelSpec channelSpec = mock(ChannelSpec.class);
        when(channelSpec.getDeviceObisCode()).thenReturn(ObisCode.fromString(DEVICE_OBIS_CODE));
        Channel channel = mock(Channel.class);
        when(channel.getChannelSpec()).thenReturn(channelSpec);
        when(this.loadProfile.getChannels()).thenReturn(Collections.singletonList(channel));

        // Business method
        List<LoadProfileExtractor.Channel> channels = extractor.channels(this.loadProfile);

        // Asserts
        assertThat(channels).hasSize(1);
        LoadProfileExtractor.Channel extractedChannel = channels.get(0);
        assertThat(extractedChannel.deviceSerialNumber()).isEqualTo(DEVICE_SERIAL_NUMBER);
        assertThat(extractedChannel.obisCode()).isEqualTo(DEVICE_OBIS_CODE);
        assertThat(extractedChannel.unit()).isEqualTo("kWh");
    }

    @Test
    public void noRegisters() {
        LoadProfileExtractor extractor = this.getInstance();
        when(this.loadProfile.getChannels()).thenReturn(Collections.emptyList());

        // Business method
        List<LoadProfileExtractor.Register> registers = extractor.registers(this.loadProfile);

        // Asserts
        assertThat(registers).isEmpty();
    }

    @Test
    public void oneRegister() {
        LoadProfileExtractor extractor = this.getInstance();
        ChannelSpec channelSpec = mock(ChannelSpec.class);
        when(channelSpec.getDeviceObisCode()).thenReturn(ObisCode.fromString(DEVICE_OBIS_CODE));
        Channel channel = mock(Channel.class);
        when(channel.getChannelSpec()).thenReturn(channelSpec);
        when(this.loadProfile.getChannels()).thenReturn(Collections.singletonList(channel));

        // Business method
        List<LoadProfileExtractor.Register> registers = extractor.registers(this.loadProfile);

        // Asserts
        assertThat(registers).hasSize(1);
        LoadProfileExtractor.Register extractedRegister = registers.get(0);
        assertThat(extractedRegister.deviceSerialNumber()).isEqualTo(DEVICE_SERIAL_NUMBER);
        assertThat(extractedRegister.obisCode()).isEqualTo(DEVICE_OBIS_CODE);
    }

    private LoadProfileExtractorImpl getInstance() {
        return new LoadProfileExtractorImpl(this.readingTypeUtilService);
    }

}