/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.command.impl;

import com.elster.jupiter.devtools.tests.ProgrammableClock;
import com.elster.jupiter.devtools.tests.rules.TimeZoneNeutral;
import com.elster.jupiter.dualcontrol.DualControlService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.device.command.CommandRuleService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;

import java.lang.reflect.Field;
import java.time.Clock;
import java.time.Instant;
import java.time.ZonedDateTime;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;
import static org.fest.reflect.core.Reflection.field;


@RunWith(MockitoJUnitRunner.class)
public class CommandRuleImplTest {

    private static final ZonedDateTime TIME_BASE = ZonedDateTime.of(1990, 2, 27, 16, 12, 30, 0, TimeZoneNeutral.getMcMurdo());

    private Clock clock;

    @Mock
    private DataModel dataModel;
    @Mock
    private CommandRuleService commandRuleService;
    @Mock
    private DeviceMessageSpecificationService deviceMessageSpecificationService;
    @Mock
    private DualControlService dualControlService;
    @Mock
    private EventService eventService;

    @Before
    public void setUp() {
        clock = new ProgrammableClock(TimeZoneNeutral.getMcMurdo(), TIME_BASE::toInstant);
    }

    private void simulateSavedInDB(CommandRuleImpl commandRule) {
        field("id").ofType(Long.TYPE).in(commandRule).set(489216L); // simulate saved in db
        Instant now = clock.instant();
        field("createTime").ofType(Instant.class).in(commandRule).set(now);
        field("modTime").ofType(Instant.class).in(commandRule).set(now);
    }

    @Test
    public void commandRuleCreationTimeIsTimeAtConstruction() throws Exception {
        CommandRuleImpl commandRule = new CommandRuleImpl(dataModel, deviceMessageSpecificationService, dualControlService, commandRuleService);
        simulateSavedInDB(commandRule);

        Field createTime = CommandRuleImpl.class.getDeclaredField("createTime");
        createTime.setAccessible(true);
        assertThat(createTime.get(commandRule)).isEqualTo(TIME_BASE.toInstant());
    }

    @Test
    public void commandRuleModificationTimeIsTimeAtConstruction() throws Exception {
        CommandRuleImpl commandRule = new CommandRuleImpl(dataModel, deviceMessageSpecificationService, dualControlService, commandRuleService);
        simulateSavedInDB(commandRule);

        Field modTime = CommandRuleImpl.class.getDeclaredField("modTime");
        modTime.setAccessible(true);
        assertThat(modTime.get(commandRule)).isEqualTo(TIME_BASE.toInstant());
    }

    @Test
    public void inactiveAtConstruction() {
        CommandRuleImpl commandRule = new CommandRuleImpl(dataModel, deviceMessageSpecificationService, dualControlService, commandRuleService);

        assertThat(commandRule.isActive()).isFalse();
    }

    @Test
    public void noPendingChangesAtConstruction() {
        CommandRuleImpl commandRule = new CommandRuleImpl(dataModel, deviceMessageSpecificationService, dualControlService, commandRuleService);

        assertThat(commandRule.getCommandRulePendingUpdate()).isEmpty();
    }

    @Test
    public void noLimitsAtConstruction() {
        CommandRuleImpl commandRule = new CommandRuleImpl(dataModel, deviceMessageSpecificationService, dualControlService, commandRuleService);
        assertThat(commandRule.getDayLimit()).isEqualTo(0);
        assertThat(commandRule.getWeekLimit()).isEqualTo(0);
        assertThat(commandRule.getMonthLimit()).isEqualTo(0);
    }
}
