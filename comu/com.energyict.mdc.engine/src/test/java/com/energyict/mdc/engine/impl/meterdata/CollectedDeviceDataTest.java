/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.meterdata.CollectedData;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Tests for the abstract {@link CollectedData}
 *
 * @author gna
 * @since 4/04/12 - 14:46
 */
public class CollectedDeviceDataTest {

    private static RegisterIdentifier getMockedRegisterIdentifier() {
        return mock(RegisterIdentifier.class);
    }

    private static CollectedData getSimpleCollectedData(){
        return new BillingDeviceRegisters(getMockedRegisterIdentifier());
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
        Issue issue = null;
        simpleCollectedData.setFailureInformation(ResultType.ConfigurationMisMatch, issue);
    }

    @Test(expected = CodingException.class)
    public void setFailureInformationNullResultTypeTest(){
        CollectedData simpleCollectedData = getSimpleCollectedData();
        Issue issue = mock(Issue.class);
        simpleCollectedData.setFailureInformation(null, issue);
    }

}