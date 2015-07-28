package com.energyict.mdc.device.lifecycle.impl;

import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.NlsService;
import com.energyict.mdc.device.lifecycle.config.MicroAction;
import com.energyict.mdc.device.topology.TopologyService;

import com.elster.jupiter.estimation.EstimationService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.validation.ValidationService;

import org.junit.*;
import org.junit.runner.*;

import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests the {@link MicroActionFactoryImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-05-06 (09:10)
 */
@RunWith(MockitoJUnitRunner.class)
public class MicroActionFactoryImplTest {

    @Mock
    private MeteringService meteringService;
    @Mock
    private MeteringGroupsService meteringGroupsService;
    @Mock
    private TopologyService topologyService;
    @Mock
    private ThreadPrincipalService threadPrincipalService;
    @Mock
    private TransactionService transactionService;
    @Mock
    private ValidationService validationService;
    @Mock
    private EstimationService estimationService;
    @Mock
    private IssueService issueService;
    @Mock
    private NlsService nlsService;


    @Test
    public void allMicroActionsAreCovered() {
        MicroActionFactoryImpl factory = this.getTestInstance();

        for (MicroAction microAction : MicroAction.values()) {
            // Business method
            ServerMicroAction serverMicroAction = factory.from(microAction);

            // Asserts
            assertThat(serverMicroAction).as("MicroActionFactoryImpl returns null for " + microAction).isNotNull();
        }

    }

    private MicroActionFactoryImpl getTestInstance() {
        return new MicroActionFactoryImpl(this.nlsService, this.meteringService, this.meteringGroupsService, this.topologyService, this.transactionService, this.threadPrincipalService, this.validationService, this.estimationService, this.issueService);
    }

}