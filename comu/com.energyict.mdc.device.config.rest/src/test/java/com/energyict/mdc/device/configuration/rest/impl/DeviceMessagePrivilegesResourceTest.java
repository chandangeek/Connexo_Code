/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.Privilege;
import com.energyict.mdc.common.device.config.DeviceMessageUserAction;

import com.jayway.jsonpath.JsonModel;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DeviceMessagePrivilegesResourceTest extends DeviceConfigurationApplicationJerseyTest {

    @Override
    protected void setupThesaurus() {
        super.setupThesaurus();
        Stream.of(DeviceMessageExecutionLevelTranslationKeys.values()).forEach(this::mockTranslation);
    }

    private void mockTranslation(DeviceMessageExecutionLevelTranslationKeys translationKey) {
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn(translationKey.getDefaultFormat());
        doReturn(messageFormat).when(thesaurus).getFormat(translationKey);
    }

    @Test
    public void testDeviceMessagePrivileges() {
        Privilege level1 = mock(Privilege.class);
        when(level1.getName()).thenReturn(DeviceMessageUserAction.EXECUTEDEVICEMESSAGE1.getPrivilege());
        Privilege level3 = mock(Privilege.class);
        when(level3.getName()).thenReturn(DeviceMessageUserAction.EXECUTEDEVICEMESSAGE3.getPrivilege());
        Group group = mock(Group.class);
        when(group.getName()).thenReturn("Administrators");
        when(group.getPrivileges("MDC")).thenReturn(Arrays.asList(level1, level3));
        when(userService.getGroups()).thenReturn(Arrays.asList(group));

        String response = target("/devicemessageprivileges").request().get(String.class);

        JsonModel jsonModel = JsonModel.create(response);
        assertThat(jsonModel.<Integer> get("$.total")).isEqualTo(4);
        assertThat(jsonModel.<List<?>> get("$.privileges")).hasSize(4);
        assertThat(jsonModel.<String> get("$.privileges[0].privilege")).isEqualTo(DeviceMessageUserAction.EXECUTEDEVICEMESSAGE1.getPrivilege());
        assertThat(jsonModel.<String> get("$.privileges[0].name")).isEqualTo("Level 1");
        assertThat(jsonModel.<List<String>> get("$.privileges[0].roles")).containsExactly("Administrators");

        assertThat(jsonModel.<String> get("$.privileges[1].privilege")).isEqualTo(DeviceMessageUserAction.EXECUTEDEVICEMESSAGE2.getPrivilege());
        assertThat(jsonModel.<String> get("$.privileges[1].name")).isEqualTo("Level 2");
        assertThat(jsonModel.<List<String>> get("$.privileges[1].roles")).isEmpty();

        assertThat(jsonModel.<String> get("$.privileges[2].privilege")).isEqualTo(DeviceMessageUserAction.EXECUTEDEVICEMESSAGE3.getPrivilege());
        assertThat(jsonModel.<String> get("$.privileges[2].name")).isEqualTo("Level 3");
        assertThat(jsonModel.<List<String>> get("$.privileges[2].roles")).containsExactly("Administrators");

        assertThat(jsonModel.<String> get("$.privileges[3].privilege")).isEqualTo(DeviceMessageUserAction.EXECUTEDEVICEMESSAGE4.getPrivilege());
        assertThat(jsonModel.<String> get("$.privileges[3].name")).isEqualTo("Level 4");
        assertThat(jsonModel.<List<String>> get("$.privileges[3].roles")).isEmpty();
    }
}
