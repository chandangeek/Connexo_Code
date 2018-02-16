/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.command.impl;

import com.energyict.mdc.device.command.CommandRule;
import com.energyict.mdc.device.command.ICommandRuleCounter;
import com.energyict.mdc.device.command.impl.exceptions.BulkExceededCommandRule;
import com.energyict.mdc.device.command.impl.exceptions.ExceededCommandRule;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mock;

import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExceededCommandRuleTest {

    private CommandRule commandRule;
    private static final long devices = 5;
    private static final long countUsedCommands = 5;

    @Before
    public void before() {
        commandRule = mock(CommandRule.class);
        when(commandRule.getWeekLimit()).thenReturn(0L);
        when(commandRule.getMonthLimit()).thenReturn(0L);
    }

    @Test
    public void usedCommandsEqualsDailyLimit(){
        when(commandRule.getDayLimit()).thenReturn(5L);
        ExceededCommandRule exceededCommandRule = this.createExceededCommandRule();
        assertThat(exceededCommandRule.isLimitExceeded()).isEqualTo(true);
    }

    @Test
    public void bulkUsedCommandsEqualsDailyLimit(){
        when(commandRule.getDayLimit()).thenReturn(5L);
        ExceededCommandRule exceededCommandRule = this.createBulkExceededCommandRule();
        assertThat(exceededCommandRule.isLimitExceeded()).isEqualTo(true);
    }

    @Test
    public void bulkAllowedCommandsLowerThanAvailableDevices(){
        when(commandRule.getDayLimit()).thenReturn(1L);
        ExceededCommandRule exceededCommandRule = this.createBulkExceededCommandRule();
        assertThat(exceededCommandRule.isLimitExceeded()).isEqualTo(true);
    }

    @Test
    public void bulkAllowedCommandsHigherThanAvailableDevices(){
        when(commandRule.getDayLimit()).thenReturn(10L);
        ExceededCommandRule exceededCommandRule = this.createBulkExceededCommandRule();
        assertThat(exceededCommandRule.isLimitExceeded()).isEqualTo(false);
    }

    @Test
    public void bulkAllowedCommandsEqualToAvailableDevices(){
        when(commandRule.getDayLimit()).thenReturn(10L);
        ExceededCommandRule exceededCommandRule = this.createBulkExceededCommandRule();
        assertThat(exceededCommandRule.isLimitExceeded()).isEqualTo(false);
    }

    private ExceededCommandRule createExceededCommandRule(){
        ExceededCommandRule exceededCommandRule = new ExceededCommandRule(commandRule);
        exceededCommandRule.setStatus(CommandRuleCounter.CounterType.DAY, countUsedCommands);
        return exceededCommandRule;
    }

    private ExceededCommandRule createBulkExceededCommandRule(){
        ExceededCommandRule exceededCommandRule = new BulkExceededCommandRule(commandRule, devices);
        exceededCommandRule.setStatus(CommandRuleCounter.CounterType.DAY, countUsedCommands);
        return exceededCommandRule;
    }
}
