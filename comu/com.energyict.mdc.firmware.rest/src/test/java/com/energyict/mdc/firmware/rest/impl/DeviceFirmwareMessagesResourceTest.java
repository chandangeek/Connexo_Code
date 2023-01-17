/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.properties.BooleanFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyTypeInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfo;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.protocol.DeviceMessage;
import com.energyict.mdc.common.protocol.DeviceMessageId;
import com.energyict.mdc.common.protocol.DeviceMessageSpec;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.firmware.FirmwareCheck;
import com.energyict.mdc.firmware.FirmwareManagementDeviceUtils;
import com.energyict.mdc.firmware.FirmwareManagementOptions;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.upl.messages.ProtocolSupportedFirmwareOptions;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static com.elster.jupiter.devtools.tests.MatchersExtension.anyNumberWithValue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class DeviceFirmwareMessagesResourceTest extends BaseFirmwareTest {
    private static final long FIRMWARE_VERSION_ID = 135;
    private static final String FIRMWARE_VERSION = "firmwareVersion";
    private static final String DEVICE_NAME = ":";
    private static final ProtocolSupportedFirmwareOptions UPLOAD_OPTION = ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE;
    private static final DeviceMessageId UPLOAD_MESSAGE_ID = DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_AND_RESUME_OPTION_ACTIVATE_IMMEDIATE;
    private static final Instant NOW = Instant.ofEpochSecond(1556290844);
    private static final String CHECK1 = "Chk1";
    private static final String CHECK2 = "Chk2";
    private static final String CHECK_PREFIX = "Check: ";

    @Mock
    private Device device;
    @Mock
    private DeviceType deviceType;
    @Mock
    private FirmwareVersion firmwareVersion;
    @Mock
    private DeviceMessageSpec uploadMessageSpec;
    @Mock
    private PropertySpec firmwareSpec, resumeSpec, imageIdSpec;
    @Mock
    private FirmwareCheck check1, check2;
    @Mock
    private FirmwareManagementDeviceUtils firmwareManagementDeviceUtils;
    @Mock
    private ComTaskExecution uploadComTaskExecution;
    private Device.DeviceMessageBuilder deviceMessageBuilder;
    @Mock
    private DeviceMessage deviceMessage;
    @Mock
    private FirmwareManagementOptions firmwareManagementOptions;

    @Before
    public void setUpStubs() {
        when(firmwareVersion.getFirmwareVersion()).thenReturn(FIRMWARE_VERSION);
        when(firmwareVersion.getImageIdentifier()).thenReturn(FIRMWARE_VERSION);
        when(firmwareVersion.getFirmwareType()).thenReturn(FirmwareType.METER);
        when(firmwareVersion.getId()).thenReturn(FIRMWARE_VERSION_ID);
        when(firmwareService.getFirmwareVersionById(anyLong())).thenReturn(Optional.empty());
        when(firmwareService.getFirmwareVersionById(FIRMWARE_VERSION_ID)).thenReturn(Optional.of(firmwareVersion));
        when(deviceService.findDeviceByName(DEVICE_NAME)).thenReturn(Optional.of(device));
        when(device.getName()).thenReturn(DEVICE_NAME);
        when(device.getDeviceType()).thenReturn(deviceType);
        when(firmwareService.findFirmwareManagementOptions(device.getDeviceType())).thenReturn(Optional.of(firmwareManagementOptions));
        when(firmwareService.getAllowedFirmwareManagementOptionsFor(deviceType)).thenReturn(EnumSet.of(UPLOAD_OPTION));
        when(firmwareService.bestSuitableFirmwareUpgradeMessageId(deviceType, UPLOAD_OPTION, firmwareVersion)).thenReturn(Optional.of(UPLOAD_MESSAGE_ID));
        when(deviceMessageSpecificationService.findMessageSpecById(UPLOAD_MESSAGE_ID.dbValue())).thenReturn(Optional.of(uploadMessageSpec));
        when(uploadMessageSpec.getPropertySpecs()).thenReturn(Arrays.asList(firmwareSpec, imageIdSpec, resumeSpec));
        when(firmwareSpec.getName()).thenReturn(FirmwareMessageInfoFactory.PROPERTY_KEY_FIRMWARE_VERSION);
        when(firmwareSpec.getValueFactory()).thenReturn(new ValueFactory<FirmwareVersion>() {
            @Override
            public FirmwareVersion fromStringValue(String stringValue) {
                return firmwareService.getFirmwareVersionById(Long.parseLong(stringValue)).get();
            }

            @Override
            public String toStringValue(FirmwareVersion object) {
                return Long.toString(object.getId());
            }

            @Override
            public Class<FirmwareVersion> getValueType() {
                return FirmwareVersion.class;
            }

            @Override
            public FirmwareVersion valueFromDatabase(Object object) {
                return fromStringValue(object.toString());
            }

            @Override
            public Object valueToDatabase(FirmwareVersion object) {
                return toStringValue(object);
            }

            @Override
            public void bind(PreparedStatement statement, int offset, FirmwareVersion value) throws SQLException {
            }

            @Override
            public void bind(SqlBuilder builder, FirmwareVersion value) {
            }
        });
        when(imageIdSpec.getName()).thenReturn(FirmwareMessageInfoFactory.PROPERTY_KEY_IMAGE_IDENTIFIER);
        when(imageIdSpec.getValueFactory()).thenReturn(new StringFactory());
        when(resumeSpec.getName()).thenReturn(FirmwareMessageInfoFactory.PROPERTY_KEY_RESUME);
        when(resumeSpec.getValueFactory()).thenReturn(new BooleanFactory());
        when(mdcPropertyUtils.findPropertyValue(any(PropertySpec.class), anyCollectionOf(PropertyInfo.class))).thenAnswer(invocation -> {
            String name = invocation.getArgumentAt(0, PropertySpec.class).getName();
            Collection<PropertyInfo> propertyInfoCollection = invocation.getArgumentAt(1, Collection.class);
            return propertyInfoCollection.stream()
                    .filter(property -> name.equals(property.key))
                    .findAny()
                    .map(PropertyInfo::getPropertyValueInfo)
                    .map(PropertyValueInfo::getValue)
                    .orElse(null);
        });
        when(firmwareService.getFirmwareChecks()).thenAnswer(invocation -> Stream.of(check1, check2));
        when(check1.getKey()).thenReturn(CHECK1);
        when(check1.getTitle(thesaurus)).thenReturn(CHECK_PREFIX + CHECK1);
        when(check2.getKey()).thenReturn(CHECK2);
        when(check2.getTitle(thesaurus)).thenReturn(CHECK_PREFIX + CHECK2);
        when(firmwareService.getFirmwareManagementDeviceUtilsFor(device)).thenReturn(firmwareManagementDeviceUtils);
        when(firmwareManagementDeviceUtils.getFirmwareComTaskExecution()).thenReturn(Optional.of(uploadComTaskExecution));
        when(firmwareManagementDeviceUtils.lockFirmwareComTaskExecution()).thenReturn(Optional.of(uploadComTaskExecution));
        when(device.newDeviceMessage(UPLOAD_MESSAGE_ID)).thenAnswer(invocation -> deviceMessageBuilder);
        deviceMessageBuilder = FakeBuilder.initBuilderStub(deviceMessage, Device.DeviceMessageBuilder.class);
    }

    @Test
    public void testUploadFirmware() {
        FirmwareMessageInfo uploadInfo = createUploadInfo(UPLOAD_OPTION, firmwareVersion, NOW, false);

        Response response = target("devices/" + DEVICE_NAME + "/firmwaremessages").request().post(Entity.json(uploadInfo));

        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());

        verify(check1).execute(firmwareManagementOptions,firmwareManagementDeviceUtils, firmwareVersion);
        verify(check2).execute(firmwareManagementOptions,firmwareManagementDeviceUtils, firmwareVersion);

        verify(device).newDeviceMessage(UPLOAD_MESSAGE_ID);
        verify(deviceMessageBuilder).setReleaseDate(NOW);
        verify(deviceMessageBuilder).addProperty(eq(firmwareSpec.getName()), anyNumberWithValue(FIRMWARE_VERSION_ID));
        verify(deviceMessageBuilder).addProperty(imageIdSpec.getName(), FIRMWARE_VERSION);
        verify(deviceMessageBuilder).addProperty(resumeSpec.getName(), false);
        verify(deviceMessageBuilder).add();
    }

    @Test
    public void testBothFirmwareChecksFail() throws Exception {
        doThrow(new FirmwareCheck.FirmwareCheckException(thesaurus, MessageSeeds.VERSION_IS_DEPRECATED)).when(check1).execute(firmwareManagementOptions,firmwareManagementDeviceUtils, firmwareVersion);
        doThrow(new FirmwareCheck.FirmwareCheckException(thesaurus, MessageSeeds.VERSION_IN_USE)).when(check2).execute(firmwareManagementOptions,firmwareManagementDeviceUtils, firmwareVersion);
        FirmwareMessageInfo uploadInfo = createUploadInfo(UPLOAD_OPTION, firmwareVersion, NOW, false);

        Response response = target("devices/" + DEVICE_NAME + "/firmwaremessages").request().post(Entity.json(uploadInfo));

        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
        verify(device, never()).newDeviceMessage(UPLOAD_MESSAGE_ID);
        verifyZeroInteractions(deviceMessageBuilder);

        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Boolean>get("$.success")).isFalse();
        assertThat(jsonModel.<Boolean>get("$.confirmation")).isTrue();
        assertThat(jsonModel.<List<String>>get("$.errors[*].id")).containsOnly(CHECK1, CHECK2);
        assertThat(jsonModel.<List<String>>get("$.errors[?(@.id==" + CHECK1 + ")].title")).containsOnly(CHECK_PREFIX + CHECK1);
        assertThat(jsonModel.<List<String>>get("$.errors[?(@.id==" + CHECK1 + ")].msg")).containsOnly(MessageFormat.format(MessageSeeds.VERSION_IS_DEPRECATED.getDefaultFormat(), new Object[0]));
        assertThat(jsonModel.<List<String>>get("$.errors[?(@.id==" + CHECK2 + ")].title")).containsOnly(CHECK_PREFIX + CHECK2);
        assertThat(jsonModel.<List<String>>get("$.errors[?(@.id==" + CHECK2 + ")].msg")).containsOnly(MessageFormat.format(MessageSeeds.VERSION_IN_USE.getDefaultFormat(), new Object[0]));
    }

    @Test
    public void testForceUpload() {
        doThrow(new FirmwareCheck.FirmwareCheckException(thesaurus, MessageSeeds.VERSION_IS_DEPRECATED)).when(check1).execute(firmwareManagementOptions,firmwareManagementDeviceUtils, firmwareVersion);
        doThrow(new FirmwareCheck.FirmwareCheckException(thesaurus, MessageSeeds.VERSION_IN_USE)).when(check2).execute(firmwareManagementOptions,firmwareManagementDeviceUtils, firmwareVersion);
        FirmwareMessageInfo uploadInfo = createUploadInfo(UPLOAD_OPTION, firmwareVersion, NOW, false);

        Response response = target("devices/" + DEVICE_NAME + "/firmwaremessages").queryParam("force", true).request().post(Entity.json(uploadInfo));

        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());

        verifyZeroInteractions(check1, check2);

        verify(device).newDeviceMessage(UPLOAD_MESSAGE_ID);
        verify(deviceMessageBuilder).setReleaseDate(NOW);
        verify(deviceMessageBuilder).addProperty(eq(firmwareSpec.getName()), anyNumberWithValue(FIRMWARE_VERSION_ID));
        verify(deviceMessageBuilder).addProperty(imageIdSpec.getName(), FIRMWARE_VERSION);
        verify(deviceMessageBuilder).addProperty(resumeSpec.getName(), false);
        verify(deviceMessageBuilder).add();
    }

    @Test
    public void testNoChecksDuringUploadOfCaConfig() {
        doThrow(new FirmwareCheck.FirmwareCheckException(thesaurus, MessageSeeds.VERSION_IS_DEPRECATED)).when(check1).execute(firmwareManagementOptions,firmwareManagementDeviceUtils, firmwareVersion);
        doThrow(new FirmwareCheck.FirmwareCheckException(thesaurus, MessageSeeds.VERSION_IN_USE)).when(check2).execute(firmwareManagementOptions,firmwareManagementDeviceUtils, firmwareVersion);
        when(firmwareVersion.getFirmwareType()).thenReturn(FirmwareType.CA_CONFIG_IMAGE);
        FirmwareMessageInfo uploadInfo = createUploadInfo(UPLOAD_OPTION, firmwareVersion, NOW, false);

        Response response = target("devices/" + DEVICE_NAME + "/firmwaremessages").request().post(Entity.json(uploadInfo));

        assertThat(response.getStatus()).isEqualTo(Response.Status.CREATED.getStatusCode());

        verifyZeroInteractions(check1, check2);

        verify(device).newDeviceMessage(UPLOAD_MESSAGE_ID);
        verify(deviceMessageBuilder).setReleaseDate(NOW);
        verify(deviceMessageBuilder).addProperty(eq(firmwareSpec.getName()), anyNumberWithValue(FIRMWARE_VERSION_ID));
        verify(deviceMessageBuilder).addProperty(imageIdSpec.getName(), FIRMWARE_VERSION);
        verify(deviceMessageBuilder).addProperty(resumeSpec.getName(), false);
        verify(deviceMessageBuilder).add();
    }

    private static FirmwareMessageInfo createUploadInfo(ProtocolSupportedFirmwareOptions option, FirmwareVersion firmwareVersion, Instant releaseDate, Boolean resume) {
        FirmwareMessageInfo uploadInfo = new FirmwareMessageInfo();
        uploadInfo.uploadOption = option.getId();
        List<PropertyInfo> propertyInfoList = new ArrayList<>(3);
        propertyInfoList.add(createPropertyInfo(FirmwareMessageInfoFactory.PROPERTY_KEY_FIRMWARE_VERSION, firmwareVersion.getId()));
        Optional.ofNullable(firmwareVersion.getImageIdentifier()).ifPresent(imageId ->
                propertyInfoList.add(createPropertyInfo(FirmwareMessageInfoFactory.PROPERTY_KEY_IMAGE_IDENTIFIER, imageId)));
        Optional.ofNullable(resume).ifPresent(resumeValue ->
                propertyInfoList.add(createPropertyInfo(FirmwareMessageInfoFactory.PROPERTY_KEY_RESUME, resumeValue)));
        uploadInfo.setProperties(propertyInfoList);
        uploadInfo.getWrappedProperties().forEach(wrappedProperty -> wrappedProperty.canBeOverridden = null);
        uploadInfo.releaseDate = releaseDate;
        return uploadInfo;
    }

    private static PropertyInfo createPropertyInfo(String key, Object value) {
        return new PropertyInfo(key, key, new PropertyValueInfo<>(value, null), new PropertyTypeInfo(), true);
    }
}
