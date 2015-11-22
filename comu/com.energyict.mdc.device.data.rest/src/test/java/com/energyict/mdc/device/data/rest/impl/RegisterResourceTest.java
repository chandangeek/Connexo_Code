package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.rest.util.properties.PropertyInfo;
import com.elster.jupiter.rest.util.properties.PropertyValueInfo;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.masterdata.RegisterType;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.google.common.collect.Range;
import com.jayway.jsonpath.JsonModel;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RegisterResourceTest extends DeviceDataRestApplicationJerseyTest {

    public static final long startTimeFirst = 1416403197000L;
    public static final long endTimeFirst = 1479561597000L;
    public static final long endTimeSecond = 1489561597000L;
    public static final long startTimeNew = 1469561597000L;
    public static final long endTimeNew = 1499561597000L;

    @Before
    public void setUpStubs() {
    }

    public CustomPropertySet mockCustomPropertySet() {
        Device device = mock(Device.class);
        DeviceType deviceType = mock(DeviceType.class);
        Register register = mock(Register.class);
        RegisterSpec registerSpec = mock(RegisterSpec.class);
        RegisterType registerType = mock(RegisterType.class);
        RegisteredCustomPropertySet registeredCustomPropertySet = mock(RegisteredCustomPropertySet.class);
        when(deviceService.findByUniqueMrid(anyString())).thenReturn(Optional.of(device));
        when(deviceConfigurationService.findAndLockRegisterSpecByIdAndVersion(anyLong(), anyLong())).thenReturn(Optional.of(registerSpec));
        when(device.getRegisters()).thenReturn(Collections.singletonList(register));
        when(register.getDevice()).thenReturn(device);
        when(register.getRegisterSpec()).thenReturn(registerSpec);
        when(registerSpec.getId()).thenReturn(1L);
        when(registerSpec.getVersion()).thenReturn(1L);
        when(registerSpec.getRegisterType()).thenReturn(registerType);
        CustomPropertySet customPropertySet = mock(CustomPropertySet.class);
        when(customPropertySet.getName()).thenReturn("testCps");
        when(customPropertySet.isVersioned()).thenReturn(true);
        when(device.getDeviceType()).thenReturn(deviceType);
        when(deviceType.getRegisterTypeTypeCustomPropertySet(any(RegisterType.class))).thenReturn(Optional.of(registeredCustomPropertySet));
        when(registeredCustomPropertySet.isViewableByCurrentUser()).thenReturn(true);
        when(registeredCustomPropertySet.isEditableByCurrentUser()).thenReturn(true);
        when(registeredCustomPropertySet.getId()).thenReturn(1L);
        when(registeredCustomPropertySet.getCustomPropertySet()).thenReturn(customPropertySet);
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
        when(customPropertySetService.getValuesFor(customPropertySet, registerSpec)).thenReturn(customPropertySetValues);
        when(customPropertySetService.getValuesFor(eq(customPropertySet), eq(registerSpec), any(Instant.class))).thenReturn(customPropertySetValuesNoTimesliced);
        when(customPropertySetService.getValuesHistoryFor(customPropertySet, registerSpec)).thenReturn(Arrays.asList(customPropertySetValues, customPropertySetValues2));
        MessageSeed messageUpdate = mock(MessageSeed.class);
        when(messageUpdate.getKey()).thenReturn("edit.historical.values.overlap.can.update.end");
        when(messageUpdate.getDefaultFormat()).thenReturn("update");
        MessageSeed messageDelete = mock(MessageSeed.class);
        when(messageDelete.getKey()).thenReturn("edit.historical.values.overlap.can.delete");
        when(messageDelete.getDefaultFormat()).thenReturn("delete");
        HashMap<CustomPropertySetValues, MessageSeed> conflicts = new HashMap<>();
        conflicts.put(customPropertySetValues, messageUpdate);
        conflicts.put(customPropertySetValues2, messageDelete);
        when(customPropertySetService.getValuesRangeOverlapFor(eq(customPropertySet), anyObject(), any(Range.class), any(Instant.class), eq(false)))
                .thenReturn(conflicts);
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
        assertThat(jsonModel.<String>get("$.conflicts[0].conflictType")).isEqualTo("edit.historical.values.overlap.can.update.end");
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
        assertThat(jsonModel.<String>get("$.conflicts[2].conflictType")).isEqualTo("edit.historical.values.overlap.can.delete");
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
        info.timesliced = false;
        info.properties = new ArrayList<>();
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
        info.timesliced = true;
        info.versionId = info.startTime;
        info.properties = new ArrayList<>();
        Response response = target("devices/1/registers/1/customproperties/1/versions/1416403197000").queryParam("forced", true).request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(400);
        info.startTime = startTimeNew;
        info.endTime = endTimeFirst;
        info.versionId = info.startTime;
        response = target("devices/1/registers/1/customproperties/1/versions/1416403197000").queryParam("forced", true).request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(200);
    }
}