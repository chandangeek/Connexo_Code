/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.OverlapCalculatorBuilder;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.ValuesRangeConflict;
import com.elster.jupiter.cps.ValuesRangeConflictType;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfo;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.validation.ValidationResult;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.NumericalRegisterSpec;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceValidation;
import com.energyict.mdc.device.data.NumericalRegister;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;

import com.google.common.collect.Range;
import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RegisterResourceTest extends DeviceDataRestApplicationJerseyTest {

    public static final long startTimeFirst = 1416403197000L;
    public static final long endTimeFirst = 1479561597000L;
    public static final long endTimeSecond = 1489561597000L;
    public static final long startTimeNew = 1469561597000L;
    public static final long endTimeNew = 1499561597000L;
    public static final Instant NOW = Instant.ofEpochMilli(1410786205000L);
    public static final long deviceConfigurationId = 4465L;

    @Mock
    private Device device;
    @Mock
    private DeviceType deviceType;
    @Mock
    private DeviceValidation deviceValidation;
    @Mock
    private DeviceConfiguration deviceConfiguration;
    private ObisCode registerSpecObisCode = ObisCode.fromString("1.0.1.8.0.255");


    @Before
    public void setUpStubs() {
        when(deviceService.findDeviceByName("1")).thenReturn(Optional.of(device));
        when(deviceService.findAndLockDeviceByNameAndVersion("1", 1L)).thenReturn(Optional.of(device));
        when(device.getVersion()).thenReturn(1L);
        when(device.getmRID()).thenReturn("1");
        when(device.forValidation()).thenReturn(deviceValidation);
        when(device.getDeviceConfiguration()).thenReturn(deviceConfiguration);
        when(deviceConfiguration.getId()).thenReturn(deviceConfigurationId);
        when(deviceValidation.getValidationResult(any())).thenReturn(ValidationResult.VALID);
        when(deviceValidation.getLastChecked(any(Register.class))).thenReturn(Optional.empty());
        when(clock.instant()).thenReturn(NOW);
        when(device.getDeviceType()).thenReturn(deviceType);
        when(deviceType.isDataloggerSlave()).thenReturn(false);
        when(topologyService.getSlaveRegister(any(Register.class), any(Instant.class))).thenReturn(Optional.empty());
    }

    public CustomPropertySet mockCustomPropertySet() {
        when(clock.instant()).thenReturn(Instant.ofEpochMilli(1448191220000L));
        Device device = mock(Device.class);
        DeviceType deviceType = mock(DeviceType.class);
        Register register = mock(Register.class);
        RegisterSpec registerSpec = mock(RegisterSpec.class);
        RegisterType registerType = mock(RegisterType.class);
        RegisteredCustomPropertySet registeredCustomPropertySet = mock(RegisteredCustomPropertySet.class);
        when(deviceService.findDeviceByName(anyString())).thenReturn(Optional.of(device));
        when(deviceConfigurationService.findAndLockRegisterSpecByIdAndVersion(anyLong(), anyLong())).thenReturn(Optional.of(registerSpec));
        when(masterDataService.findRegisterType(anyLong())).thenReturn(Optional.of(registerType));
        when(masterDataService.findAndLockRegisterTypeByIdAndVersion(anyLong(), anyLong())).thenReturn(Optional.of(registerType));
        when(device.getRegisters()).thenReturn(Collections.singletonList(register));
        when(register.getDevice()).thenReturn(device);
        when(register.getRegisterSpec()).thenReturn(registerSpec);
        when(registerSpec.getId()).thenReturn(1L);
        when(registerSpec.getVersion()).thenReturn(1L);
        when(registerSpec.getRegisterType()).thenReturn(registerType);
        when(registerType.getId()).thenReturn(1L);
        when(registerType.getVersion()).thenReturn(1L);
        CustomPropertySet customPropertySet = mock(CustomPropertySet.class);
        when(customPropertySet.getName()).thenReturn("testCps");
        when(customPropertySet.isVersioned()).thenReturn(true);
        when(device.getDeviceType()).thenReturn(deviceType);
        when(deviceType.getRegisterTypeTypeCustomPropertySet(any(RegisterType.class))).thenReturn(Optional.of(registeredCustomPropertySet));
        when(registeredCustomPropertySet.isViewableByCurrentUser()).thenReturn(true);
        when(registeredCustomPropertySet.isEditableByCurrentUser()).thenReturn(true);
        when(registeredCustomPropertySet.getId()).thenReturn(1L);
        when(registeredCustomPropertySet.getCustomPropertySet()).thenReturn(customPropertySet);
        when(customPropertySet.getId()).thenReturn("provider.class");
        MdcPropertyUtils mdcPropertyUtils = mock(MdcPropertyUtils.class);
        PropertyInfo propertyInfo = mock(PropertyInfo.class);
        PropertyValueInfo propertyValueInfo = mock(PropertyValueInfo.class);
        when(propertyValueInfo.getValue()).thenReturn("testValue");
        when(propertyInfo.getPropertyValueInfo()).thenReturn(propertyValueInfo);
        when(mdcPropertyUtils.convertPropertySpecsToPropertyInfos(anyObject(), anyObject())).thenReturn(Arrays.asList(propertyInfo));
        CustomPropertySetValues customPropertySetValuesNoTimesliced = CustomPropertySetValues.empty();
        customPropertySetValuesNoTimesliced.setProperty("testnameNoTimesliced", "testValueNoTimesliced");
        CustomPropertySetValues customPropertySetValues = CustomPropertySetValues.emptyDuring(Interval.of(Range.closedOpen(Instant.ofEpochMilli(startTimeFirst), Instant.ofEpochMilli(endTimeFirst))));
        customPropertySetValues.setProperty("testname", "testValue1");
        CustomPropertySetValues customPropertySetValues2 = CustomPropertySetValues.emptyDuring(Interval.of(Range.closedOpen(Instant.ofEpochMilli(endTimeFirst), Instant.ofEpochMilli(endTimeSecond))));
        customPropertySetValues2.setProperty("testname2", "testValue2");
        when(customPropertySetService.getUniqueValuesFor(eq(customPropertySet), eq(registerSpec), anyObject())).thenReturn(customPropertySetValues);
        when(customPropertySetService.getUniqueValuesFor(eq(customPropertySet), eq(registerSpec), any(Instant.class), anyObject())).thenReturn(customPropertySetValuesNoTimesliced);
        when(customPropertySetService.getAllVersionedValuesFor(eq(customPropertySet), eq(registerSpec), anyObject())).thenReturn(Arrays.asList(customPropertySetValues, customPropertySetValues2));
        ValuesRangeConflict conflict1 = mock(ValuesRangeConflict.class);
        when(conflict1.getConflictingRange()).thenReturn(Range.closedOpen(Instant.ofEpochMilli(startTimeFirst), Instant.ofEpochMilli(endTimeFirst)));
        when(conflict1.getMessage()).thenReturn("testMessage");
        when(conflict1.getType()).thenReturn(ValuesRangeConflictType.RANGE_OVERLAP_UPDATE_END);
        when(conflict1.getValues()).thenReturn(customPropertySetValues);
        ValuesRangeConflict conflict2 = mock(ValuesRangeConflict.class);
        when(conflict2.getConflictingRange()).thenReturn(Range.closedOpen(Instant.ofEpochMilli(startTimeNew), Instant.ofEpochMilli(endTimeNew)));
        when(conflict2.getMessage()).thenReturn("testMessage");
        when(conflict2.getType()).thenReturn(ValuesRangeConflictType.RANGE_INSERTED);
        when(conflict2.getValues()).thenReturn(CustomPropertySetValues.emptyDuring(Interval.of(Range.closedOpen(Instant.ofEpochMilli(startTimeNew), Instant.ofEpochMilli(endTimeNew)))));
        ValuesRangeConflict conflict3 = mock(ValuesRangeConflict.class);
        when(conflict3.getConflictingRange()).thenReturn(Range.closedOpen(Instant.ofEpochMilli(endTimeFirst), Instant.ofEpochMilli(endTimeSecond)));
        when(conflict3.getMessage()).thenReturn("testMessage");
        when(conflict3.getType()).thenReturn(ValuesRangeConflictType.RANGE_OVERLAP_DELETE);
        when(conflict3.getValues()).thenReturn(customPropertySetValues2);
        OverlapCalculatorBuilder overlapCalculatorBuilder = mock(OverlapCalculatorBuilder.class);
        when(overlapCalculatorBuilder.whenCreating(any(Range.class))).thenReturn(Arrays.asList(conflict1, conflict2, conflict3));
        when(overlapCalculatorBuilder.whenUpdating(any(Instant.class), any(Range.class))).thenReturn(Arrays.asList(conflict1, conflict2, conflict3));
        when(customPropertySetService.calculateOverlapsFor(anyObject(), anyObject(), anyObject())).thenReturn(overlapCalculatorBuilder);
        return customPropertySet;
    }

    @Test
    public void testGetRegisterCustomProperties() throws Exception {
        CustomPropertySet customPropertySet = mockCustomPropertySet();
        when(customPropertySet.isVersioned()).thenReturn(false);

        String response = target("devices/1/registers/1/customproperties").request().get(String.class);
        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(1);
        assertThat(jsonModel.<List<?>>get("$.customproperties")).hasSize(1);
        assertThat(jsonModel.<Integer>get("$.customproperties[0].id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.customproperties[0].name")).isEqualTo("testCps");
        assertThat(jsonModel.<Boolean>get("$.customproperties[0].timesliced")).isEqualTo(false);
    }

    @Test
    public void testGetRegisterCustomPropertiesVersioned() throws Exception {
        mockCustomPropertySet();
        String response = target("devices/1/registers/1/customproperties/1/versions").request().get(String.class);
        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(2);
        assertThat(jsonModel.<List<?>>get("$.versions")).hasSize(2);
        assertThat(jsonModel.<Integer>get("$.versions[0].id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.versions[0].name")).isEqualTo("testCps");
        assertThat(jsonModel.<Boolean>get("$.versions[0].timesliced")).isEqualTo(true);
        assertThat(jsonModel.<Long>get("$.versions[0].versionId")).isEqualTo(startTimeFirst);
        assertThat(jsonModel.<Long>get("$.versions[0].startTime")).isEqualTo(startTimeFirst);
        assertThat(jsonModel.<Long>get("$.versions[0].endTime")).isEqualTo(endTimeFirst);
        assertThat(jsonModel.<Integer>get("$.versions[1].id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.versions[1].name")).isEqualTo("testCps");
        assertThat(jsonModel.<Boolean>get("$.versions[1].timesliced")).isEqualTo(true);
        assertThat(jsonModel.<Long>get("$.versions[0].versionId")).isEqualTo(startTimeFirst);
        assertThat(jsonModel.<Long>get("$.versions[1].startTime")).isEqualTo(endTimeFirst);
        assertThat(jsonModel.<Long>get("$.versions[1].endTime")).isEqualTo(endTimeSecond);
    }

    @Test
    public void testGetCurrentTimeInterval() throws Exception {
        mockCustomPropertySet();
        String response = target("devices/1/registers/1/customproperties/1/currentinterval").request().get(String.class);
        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Long>get("$.start")).isGreaterThan(startTimeFirst);
        assertThat(jsonModel.<Long>get("$.end")).isEqualTo(endTimeFirst);
    }

    @Test
    public void testGetConflictsCreate() throws Exception {
        mockCustomPropertySet();
        String response = target("devices/1/registers/1/customproperties/1/conflicts").queryParam("startTime", startTimeNew).queryParam("endTime", endTimeNew).request().get(String.class);
        JsonModel jsonModel = JsonModel.model(response);
        assertThat(jsonModel.<Integer>get("$.total")).isEqualTo(3);
        assertThat(jsonModel.<List<?>>get("$.conflicts")).hasSize(3);
        assertThat(jsonModel.<Integer>get("$.conflicts[0].customPropertySet.id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.conflicts[0].customPropertySet.name")).isEqualTo("testCps");
        assertThat(jsonModel.<String>get("$.conflicts[0].conflictType")).isEqualTo(ValuesRangeConflictType.RANGE_OVERLAP_UPDATE_END.name());
        assertThat(jsonModel.<Boolean>get("$.conflicts[0].customPropertySet.timesliced")).isEqualTo(true);
        assertThat(jsonModel.<Boolean>get("$.conflicts[0].conflictAtStart")).isEqualTo(false);
        assertThat(jsonModel.<Boolean>get("$.conflicts[0].conflictAtEnd")).isEqualTo(true);
        assertThat(jsonModel.<Long>get("$.conflicts[0].customPropertySet.versionId")).isEqualTo(startTimeFirst);
        assertThat(jsonModel.<Long>get("$.conflicts[0].customPropertySet.startTime")).isEqualTo(startTimeFirst);
        assertThat(jsonModel.<Long>get("$.conflicts[0].customPropertySet.endTime")).isEqualTo(endTimeFirst);
        assertThat(jsonModel.<Long>get("$.conflicts[1].customPropertySet.startTime")).isEqualTo(startTimeNew);
        assertThat(jsonModel.<Long>get("$.conflicts[1].customPropertySet.endTime")).isEqualTo(endTimeNew);
        assertThat(jsonModel.<Integer>get("$.conflicts[2].customPropertySet.id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.conflicts[2].customPropertySet.name")).isEqualTo("testCps");
        assertThat(jsonModel.<String>get("$.conflicts[2].conflictType")).isEqualTo(ValuesRangeConflictType.RANGE_OVERLAP_DELETE.name());
        assertThat(jsonModel.<Boolean>get("$.conflicts[2].customPropertySet.timesliced")).isEqualTo(true);
        assertThat(jsonModel.<Long>get("$.conflicts[2].customPropertySet.versionId")).isEqualTo(endTimeFirst);
        assertThat(jsonModel.<Long>get("$.conflicts[2].customPropertySet.startTime")).isEqualTo(endTimeFirst);
        assertThat(jsonModel.<Long>get("$.conflicts[2].customPropertySet.endTime")).isEqualTo(endTimeSecond);
        assertThat(jsonModel.<Boolean>get("$.conflicts[2].conflictAtStart")).isEqualTo(true);
        assertThat(jsonModel.<Boolean>get("$.conflicts[2].conflictAtEnd")).isEqualTo(true);
    }

    @Test
    public void testEditRegisterCustomAttribute() throws Exception {
        CustomPropertySet customPropertySet = mockCustomPropertySet();
        when(customPropertySet.isVersioned()).thenReturn(false);
        CustomPropertySetInfo info = new CustomPropertySetInfo();
        info.id = 1L;
        info.isActive = true;
        info.parent = 1L;
        info.version = 5L;
        info.objectTypeId = 1L;
        info.objectTypeVersion = 1L;
        info.timesliced = false;
        info.properties = new ArrayList<>();
        info.customPropertySetId = "provider.class";
        Response response = target("devices/1/registers/1/customproperties/1").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void testEditRegisterCustomAttributeVersioned() throws Exception {
        mockCustomPropertySet();
        CustomPropertySetInfo info = new CustomPropertySetInfo();
        info.id = 1L;
        info.isActive = true;
        info.startTime = endTimeFirst;
        info.endTime = startTimeFirst;
        info.parent = 1L;
        info.version = 5L;
        info.objectTypeId = 1L;
        info.objectTypeVersion = 1L;
        info.timesliced = true;
        info.versionId = info.startTime;
        info.properties = new ArrayList<>();
        Response response = target("devices/1/registers/1/customproperties/1/versions/1416403197000").queryParam("forced", true).request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(400);
        info.startTime = startTimeNew;
        info.endTime = endTimeFirst;
        info.versionId = info.startTime;
        info.objectTypeId = 1L;
        info.objectTypeVersion = 1L;
        response = target("devices/1/registers/1/customproperties/1/versions/1416403197000").queryParam("forced", true).request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(200);
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

    @Test
    public void getRegistersTest() throws Exception{
        Long firstRegisterId = 123L;
        Long secondRegisterId = 125L;
        String collectedReadingTypeMrid = "0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0";
        String calculatedReadingTypeMrid = "0.0.0.4.1.2.12.0.0.0.0.0.0.0.0.0.72.0";
        BigDecimal multiplier = BigDecimal.valueOf(74L);
        NumericalRegister firstNumericalRegister = mockRegisterWithCalculatedReadingType(firstRegisterId, collectedReadingTypeMrid, calculatedReadingTypeMrid, Optional.of(multiplier));
        NumericalRegister secondNumericalRegister = mockRegisterWithCalculatedReadingType(secondRegisterId, collectedReadingTypeMrid, calculatedReadingTypeMrid, Optional.of(multiplier));
        mockRegisterWithCalculatedReadingType(secondRegisterId, collectedReadingTypeMrid, calculatedReadingTypeMrid, Optional.of(multiplier));
        when(device.getRegisters()).thenReturn(Arrays.asList(firstNumericalRegister, secondNumericalRegister));
        String json = target("devices/1/registers").request().get(String.class);
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total").longValue()).isEqualTo(2);

    }

    @Test
    public void getFilteredRegistersTest() throws Exception{
        Long firstRegisterId = 123L;
        Long secondRegisterId = 125L;
        String collectedReadingTypeMrid = "0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0";
        String calculatedReadingTypeMrid = "0.0.0.4.1.2.12.0.0.0.0.0.0.0.0.0.72.0";
        BigDecimal multiplier = BigDecimal.valueOf(74L);
        NumericalRegister firstNumericalRegister = mockRegisterWithCalculatedReadingType(firstRegisterId, collectedReadingTypeMrid, calculatedReadingTypeMrid, Optional.of(multiplier));
        NumericalRegister secondNumericalRegister = mockRegisterWithCalculatedReadingType(secondRegisterId, collectedReadingTypeMrid, calculatedReadingTypeMrid, Optional.of(multiplier));
        mockRegisterWithCalculatedReadingType(secondRegisterId, collectedReadingTypeMrid, calculatedReadingTypeMrid, Optional.of(multiplier));
        when(device.getRegisters()).thenReturn(Arrays.asList(firstNumericalRegister, secondNumericalRegister));
        String json = target("devices/1/registers").queryParam("filter", URLEncoder.encode("[{\"property\":\"registers\",\"value\":[123]}]", "UTF-8")).request().get(String.class);
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total").longValue()).isEqualTo(1);

    }

    private NumericalRegister mockRegisterWithCalculatedReadingType(Long registerSpecId, String collectedReadingTypeMrid, String calculatedReadingTypeMrid, Optional<BigDecimal> multiplier) {
        ReadingType collectedReadingType = ReadingTypeMockBuilder.from(collectedReadingTypeMrid).getMock();
        when(collectedReadingType.getAliasName()).thenReturn("CollectedReadingType" + registerSpecId);
        ReadingType calculatedReadingType = ReadingTypeMockBuilder.from(calculatedReadingTypeMrid).getMock();
        when(calculatedReadingType.getAliasName()).thenReturn("CalculatedReadingType" + registerSpecId);
        RegisterType registerType = mock(RegisterType.class);
        when(registerType.getId()).thenReturn(978978978L);
        NumericalRegisterSpec registerSpec = mock(NumericalRegisterSpec.class);
        when(registerSpec.getObisCode()).thenReturn(registerSpecObisCode);
        when(registerSpec.getRegisterType()).thenReturn(registerType);
        when(registerSpec.getRegisterType().getReadingType()).thenReturn(collectedReadingType);
        when(registerSpec.getReadingType()).thenReturn(collectedReadingType);
        when(registerSpec.getOverflowValue()).thenReturn(Optional.empty());
        when(registerSpec.getId()).thenReturn(registerSpecId);
        NumericalRegister numericalRegister = mock(NumericalRegister.class);
        when(numericalRegister.getRegisterSpec()).thenReturn(registerSpec);
        when(numericalRegister.getReadingType()).thenReturn(collectedReadingType);
        when(numericalRegister.getCalculatedReadingType(any())).thenReturn(Optional.of(calculatedReadingType));
        when(numericalRegister.getMultiplier(any(Instant.class))).thenReturn(multiplier);
        when(numericalRegister.getLastReading()).thenReturn(Optional.empty());
        when(numericalRegister.getDevice()).thenReturn(device);
        when(numericalRegister.getLastReadingDate()).thenReturn(Optional.empty());
        when(numericalRegister.getDeviceObisCode()).thenReturn(registerSpecObisCode);
        when(numericalRegister.getOverflow()).thenReturn(Optional.empty());
        when(numericalRegister.getRegisterSpecId()).thenReturn(registerSpecId);
        multiplier.ifPresent(multiplierValue -> when(device.getMultiplier()).thenReturn(multiplierValue));
        when(device.getRegisters()).thenReturn(Collections.singletonList(numericalRegister));

        return numericalRegister;
    }
}
