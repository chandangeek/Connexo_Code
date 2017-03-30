/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.command.rest.impl;

import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.license.License;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.ExceptionFactory;
import com.elster.jupiter.util.json.JsonService;
import com.energyict.mdc.device.command.CommandInRule;
import com.energyict.mdc.device.command.CommandRule;
import com.energyict.mdc.device.command.CommandRuleService;
import com.energyict.mdc.device.command.CommandRuleService.CommandRuleBuilder;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageCategory;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CommandRuleResourceTest extends FelixRestApplicationJerseyTest {
    @Mock
    private CommandRuleService commandRuleService;
    @Mock
    private DeviceMessageSpecificationService deviceMessageSpecificationService;
    @Mock
    private JsonService jsonService;
    @Mock
    private ExceptionFactory exceptionFactory;
    @Mock
    private License license;


    @Override
    protected Application getApplication() {
        when(thesaurus.join(any(Thesaurus.class))).thenReturn(thesaurus);
        CommandApplication application = new CommandApplication();
        application.setCommandRuleService(commandRuleService);
        application.setDeviceMessageSpecificationService(deviceMessageSpecificationService);
        application.setNlsService(nlsService);
        application.setJsonService(jsonService);
        application.setLicense(license);
        return application;
    }

    @Test
    public void getCommandRules() throws Exception {
        CommandRule commandRule = mockCommandRule();
        when(commandRuleService.findAllCommandRules()).thenReturn(Collections.singletonList(commandRule));

        Response response = target("/commandrules").request().get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        JsonModel jsonModel = JsonModel.model((InputStream) response.getEntity());
        assertThat(jsonModel.<Integer>get("total")).isEqualTo(1);
        assertThat(jsonModel.<Integer>get("commandrules[0].id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("commandrules[0].name")).isEqualTo("TestName");
        assertThat(jsonModel.<Integer>get("commandrules[0].dayLimit")).isEqualTo(1);
        assertThat(jsonModel.<Integer>get("commandrules[0].weekLimit")).isEqualTo(2);
        assertThat(jsonModel.<Integer>get("commandrules[0].monthLimit")).isEqualTo(3);
    }

    @Test
    public void getCommandRule() throws Exception {
        CommandRule commandRule = mockCommandRule();
        when(commandRuleService.findCommandRule(1L)).thenReturn(Optional.of(commandRule));
        Response response = target("/commandrules/1").request().get();
        CommandRuleInfo info = response.readEntity(CommandRuleInfo.class);
        assertThat(info.id).isEqualTo(1);
        assertThat(info.name).isEqualTo("TestName");
        assertThat(info.dayLimit).isEqualTo(1);
        assertThat(info.weekLimit).isEqualTo(2);
        assertThat(info.monthLimit).isEqualTo(3);
        assertThat(info.commands).hasSize(2);
        assertThat(info.dualControl).isNull();
        assertThat(info.commands.get(0).category).isEqualTo("Category");
        assertThat(info.commands.get(0).command).isEqualTo("Command");
        assertThat(info.commands.get(0).commandName).isEqualTo("DEVICE_ACTIONS_ALARM_REGISTER_RESET");
    }

    @Test
    public void testCreateCommandRule() {
        CommandRuleInfo commandRuleInfo = new CommandRuleInfo();
        commandRuleInfo.name = "NAME";
        commandRuleInfo.weekLimit = 1L;
        commandRuleInfo.monthLimit = 2L;
        CommandInfo commandInfo = new CommandInfo();
        commandInfo.commandName = "name1";
        commandRuleInfo.commands.add(commandInfo);
        CommandInfo commandInfo2 = new CommandInfo();
        commandInfo.commandName = "name2";
        commandRuleInfo.commands.add(commandInfo2);
        Entity<CommandRuleInfo> json = Entity.json(commandRuleInfo);
        CommandRuleBuilder builder = mock(CommandRuleBuilder.class);
        when(commandRuleService.createRule(any())).thenReturn(builder);
        CommandRule commandRule = mockCommandRule();
        when(builder.add()).thenReturn(commandRule);

        Response response = target("/commandrules").request().post(json);
        verify(commandRuleService).createRule("NAME");
        verify(builder).dayLimit(0L);
        verify(builder).weekLimit(1L);
        verify(builder).monthLimit(2L);
        verify(builder, times(2)).command(any());
        verify(builder).add();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void testActivateCommandRule() {
        CommandRule commandRule = mockCommandRule();
        CommandRuleInfo commandRuleInfo = new CommandRuleInfo();
        commandRuleInfo.active = true;
        commandRuleInfo.version = 1L;
        when(commandRule.isActive()).thenReturn(false);
        when(commandRuleService.findAndLockCommandRule(1L,1L)).thenReturn(Optional.of(commandRule));
        Entity<CommandRuleInfo> json = Entity.json(commandRuleInfo);
        target("/commandrules/1").request().put(json);
        verify(commandRule).activate();
    }

    @Test
    public void testDeactivateCommandRule() {
        CommandRule commandRule = mockCommandRule();
        CommandRuleInfo commandRuleInfo = new CommandRuleInfo();
        commandRuleInfo.active = false;
        commandRuleInfo.version = 1L;
        when(commandRule.isActive()).thenReturn(true);
        when(commandRuleService.findAndLockCommandRule(1L, 1L)).thenReturn(Optional.of(commandRule));
        Entity<CommandRuleInfo> json = Entity.json(commandRuleInfo);
        target("/commandrules/1").request().put(json);
        verify(commandRule).deactivate();
    }

    public CommandRule mockCommandRule() {
        CommandRule commandRule = mock(CommandRule.class);
        when(commandRule.getId()).thenReturn(1L);
        when(commandRule.getName()).thenReturn("TestName");
        when(commandRule.isActive()).thenReturn(true);
        when(commandRule.getDayLimit()).thenReturn(1L);
        when(commandRule.getWeekLimit()).thenReturn(2L);
        when(commandRule.getMonthLimit()).thenReturn(3L);
        CommandInRule commandInRule = mockCommandInRule();
        CommandInRule commandInRule2 = mockCommandInRule();
        when(commandRule.getCommands()).thenReturn(Arrays.asList(commandInRule, commandInRule2));
        when(commandRule.getCommandRulePendingUpdate()).thenReturn(Optional.empty());
        return commandRule;
    }

    public CommandInRule mockCommandInRule() {
        CommandInRule command = mock(CommandInRule.class);
        DeviceMessageSpec spec = mock(DeviceMessageSpec.class);
        DeviceMessageCategory category = mock(DeviceMessageCategory.class);
        when(category.getName()).thenReturn("Category");
        when(spec.getName()).thenReturn("Command");
        when(command.getCommand()).thenReturn(spec);
        when(spec.getId()).thenReturn(DeviceMessageId.DEVICE_ACTIONS_ALARM_REGISTER_RESET);
        when(spec.getCategory()).thenReturn(category);
        return command;
    }
}
