package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.validation.ValidationResult;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceValidation;
import com.energyict.mdc.device.data.NumericalRegister;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.masterdata.RegisterType;
import com.jayway.jsonpath.JsonModel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Copyrights EnergyICT
 * Date: 08.12.15
 * Time: 15:44
 */
public class RegisterResourceTest extends DeviceDataRestApplicationJerseyTest {

    public static final Instant NOW = Instant.ofEpochMilli(1410786205000L);
    public static final long deviceConfigurationId = 4465L;

    @Mock
    private Device device;
    @Mock
    private DeviceValidation deviceValidation;
    @Mock
    private DeviceConfiguration deviceConfiguration;
    private ObisCode registerSpecObisCode = ObisCode.fromString("1.0.1.8.0.255");

    @Before
    public void setUpStubs() {
        when(deviceService.findByUniqueMrid("1")).thenReturn(Optional.of(device));
        when(deviceService.findAndLockDeviceBymRIDAndVersion("1", 1L)).thenReturn(Optional.of(device));
        when(device.getVersion()).thenReturn(1L);
        when(device.getmRID()).thenReturn("1");
        when(device.forValidation()).thenReturn(deviceValidation);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfiguration.getId()).thenReturn(deviceConfigurationId);
        when(deviceValidation.getValidationResult(any())).thenReturn(ValidationResult.VALID);
        when(deviceValidation.getLastChecked(any(Register.class))).thenReturn(Optional.empty());
        when(clock.instant()).thenReturn(NOW);
    }

    @Test
    public void getBulkSecondaryMeteredRegisterTest() {
        Long registerId = 123L;
        String collectedReadingTypeMrid = "0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0";
        String calculatedReadingTypeMrid = "0.0.0.4.1.1.12.0.0.0.0.0.0.0.0.0.72.0";
        mockRegisterWithCalculatedReadingType(registerId, collectedReadingTypeMrid, calculatedReadingTypeMrid, Optional.empty());
        String json = target("devices/1/registers/" + registerId).request().get(String.class);
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.id").longValue()).isEqualTo(registerId);
        assertThat(jsonModel.<Number>get("$readingType.mRID")).isEqualTo(collectedReadingTypeMrid);
        assertThat(jsonModel.<Number>get("calculatedReadingType.mRID")).isEqualTo(calculatedReadingTypeMrid);
        assertThat(jsonModel.hasPath("multiplier")).isFalse();
    }

    @Test
    public void getBulkSecondaryMeteredWithMultiplierConfiguredRegisterTest() {
        Long registerId = 123L;
        String collectedReadingTypeMrid = "0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0";
        String calculatedReadingTypeMrid = "0.0.0.4.1.2.12.0.0.0.0.0.0.0.0.0.72.0";
        BigDecimal multiplier = BigDecimal.valueOf(74L);
        mockRegisterWithCalculatedReadingType(registerId, collectedReadingTypeMrid, calculatedReadingTypeMrid, Optional.of(multiplier));
        String json = target("devices/1/registers/" + registerId).request().get(String.class);
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.id").longValue()).isEqualTo(registerId);
        assertThat(jsonModel.<Number>get("$readingType.mRID")).isEqualTo(collectedReadingTypeMrid);
        assertThat(jsonModel.<Number>get("calculatedReadingType.mRID")).isEqualTo(calculatedReadingTypeMrid);
        assertThat(jsonModel.<Number>get("multiplier")).isEqualTo(multiplier.intValue());
    }

    private void mockRegisterWithCalculatedReadingType(Long registerSpecId, String collectedReadingTypeMrid, String calculatedReadingTypeMrid, Optional<BigDecimal> multiplier) {
        ReadingType collectedReadingType = ReadingTypeMockBuilder.from(collectedReadingTypeMrid).getMock();
        ReadingType calculatedReadingType = ReadingTypeMockBuilder.from(calculatedReadingTypeMrid).getMock();
        RegisterType registerType = mock(RegisterType.class);
        when(registerType.getId()).thenReturn(978978978L);
        NumericalRegisterSpec registerSpec = mock(NumericalRegisterSpec.class);
        when(registerSpec.getObisCode()).thenReturn(registerSpecObisCode);
        when(registerSpec.getRegisterType()).thenReturn(registerType);
        when(registerSpec.getReadingType()).thenReturn(collectedReadingType);
        when(registerSpec.getId()).thenReturn(registerSpecId);
        NumericalRegister numericalRegister = mock(NumericalRegister.class);
        when(numericalRegister.getRegisterSpec()).thenReturn(registerSpec);
        when(numericalRegister.getReadingType()).thenReturn(collectedReadingType);
        when(numericalRegister.getCalculatedReadingType()).thenReturn(Optional.of(calculatedReadingType));
        when(numericalRegister.getMultiplier()).thenReturn(multiplier);
        when(numericalRegister.getLastReading()).thenReturn(Optional.empty());
        when(numericalRegister.getDevice()).thenReturn(device);
        multiplier.ifPresent(multiplierValue -> when(device.getMultiplier()).thenReturn(multiplierValue));
        when(device.getRegisters()).thenReturn(Collections.singletonList(numericalRegister));
    }
}
