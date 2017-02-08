/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.meterdata;

import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.device.data.Register;
import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.protocol.api.device.data.CollectedData;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.device.data.identifiers.RegisterIdentifier;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the abstract {@link CollectedData}
 *
 * @author gna
 * @since 4/04/12 - 14:46
 */
public class CollectedDeviceDataTest {

    private static RegisterIdentifier getMockedRegisterIdentifier() {
        Register register = mock(Register.class);
        RegisterIdentifier registerIdentifier = mock(RegisterIdentifier.class);
        when(registerIdentifier.findRegister()).thenReturn(register);
        return registerIdentifier;
    }

    private static CollectedData getSimpleCollectedData(){
        return new BillingDeviceRegisters(getMockedRegisterIdentifier(), mock(ReadingType.class));
    }

    @Test
    public void setFailureInformationTest() {
        CollectedData simpleCollectedData = getSimpleCollectedData();
        Issue issue = mock(Issue.class);
        simpleCollectedData.setFailureInformation(ResultType.ConfigurationMisMatch, issue);

        assertThat(simpleCollectedData.getResultType()).isEqualTo(ResultType.ConfigurationMisMatch);
        assertThat(simpleCollectedData.getIssues().get(0)).isEqualTo(issue);
    }

    @Test(expected = CodingException.class)
    public void setFailureInformationNullIssueTest(){
        CollectedData simpleCollectedData = getSimpleCollectedData();
        simpleCollectedData.setFailureInformation(ResultType.ConfigurationMisMatch, null);
    }

    @Test(expected = CodingException.class)
    public void setFailureInformationNullResultTypeTest(){
        CollectedData simpleCollectedData = getSimpleCollectedData();
        Issue issue = mock(Issue.class);
        simpleCollectedData.setFailureInformation(null, issue);
    }

}