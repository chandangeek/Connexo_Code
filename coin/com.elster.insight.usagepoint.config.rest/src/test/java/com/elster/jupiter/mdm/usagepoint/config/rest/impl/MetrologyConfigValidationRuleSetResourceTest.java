/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.config.rest.impl;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;


public class MetrologyConfigValidationRuleSetResourceTest extends UsagePointConfigurationRestApplicationJerseyTest {

    @Mock
    private ValidationRuleSet validationRuleSet;
    @Mock
    private MetrologyContract metrologyContract;
    @Mock
    private MetrologyConfiguration metrologyConfiguration;
    @Mock
    private MetrologyPurpose metrologyPurpose;
    @Mock
    private ValidationRule validationRule;
    @Mock
    private ReadingType readingType;
    @Mock
    private ReadingTypeDeliverable readingTypeDeliverable;

    private static final long VALIDATION_RULE_SET_ID = 555;
    private static final long METROLOGY_CONTRACT_ID = 12;
    private static final long METORLOGY_CONTRACT_VERSION = 27;

    @Test
    public void testGetLinkedMetrologyConfigurationPurposes() throws IOException {
        initMocks();
        when(usagePointConfigurationService.getMetrologyContractsLinkedToValidationRuleSet(validationRuleSet)).thenReturn(Collections.singletonList(metrologyContract));
        when(usagePointConfigurationService.getMatchingDeliverablesOnValidationRuleSet(metrologyContract, validationRuleSet)).thenReturn(Collections.singletonList(readingTypeDeliverable));

        Response response = target("/validationrulesets/" + VALIDATION_RULE_SET_ID + "/purposes").request().get();

        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(model.<Integer>get("$.total")).isEqualTo(1);
        assertThat(model.<String>get("$.purposes[0].name")).isEqualTo("Billing");
        assertThat(model.<Boolean>get("$.purposes[0].active")).isEqualTo(true);
        assertThat(model.<String>get("$.purposes[0].outputs[0].outputName")).isEqualTo("Monthly A+");
        assertThat(model.<Boolean>get("$.purposes[0].outputs[0].isMatched")).isEqualTo(true);
        assertThat(model.<Integer>get("$.purposes[0].metrologyConfigurationInfo.id")).isEqualTo(13);
        assertThat(model.<String>get("$.purposes[0].metrologyConfigurationInfo.name")).isEqualTo("Residential prosumer with 1 meter");
        assertThat(model.<Integer>get("$.purposes[0].id")).isEqualTo(12);
        assertThat(model.<Integer> get("$.purposes[0].version")).isEqualTo(15);
    }

    @Test
    public void testRemoveMetrologyConfigurationPurpose() {
        initMocks();
        doReturn(Optional.of(metrologyContract)).when(metrologyConfigurationService)
                .findAndLockMetrologyContract(METROLOGY_CONTRACT_ID, METORLOGY_CONTRACT_VERSION);
        when(metrologyContract.getVersion()).thenReturn(METORLOGY_CONTRACT_VERSION);
        when(metrologyContract.getId()).thenReturn(METROLOGY_CONTRACT_ID);
        when(metrologyContract.getMetrologyPurpose()).thenReturn(metrologyPurpose);
        when(metrologyConfigurationService.findAndLockMetrologyContract(METROLOGY_CONTRACT_ID, METORLOGY_CONTRACT_VERSION)).thenReturn(Optional.of(metrologyContract));

        LinkableMetrologyContractInfo metrologyContractInfo = new LinkableMetrologyContractInfo();
        metrologyContractInfo.setVersion(metrologyContract.getVersion());
        metrologyContractInfo.setId(metrologyContract.getId());

        Response response = target("/validationrulesets/" + VALIDATION_RULE_SET_ID + "/purposes/" + METROLOGY_CONTRACT_ID)
                .request().build(HttpMethod.DELETE, Entity.json(metrologyContractInfo)).invoke();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(usagePointConfigurationService).removeValidationRuleSet(metrologyContract, validationRuleSet);
    }

    @Test
    public void testGetLinkablePurposes() throws Exception {
        initMocks();
        ReadingTypeDeliverable unmatchedDeliverable = mock(ReadingTypeDeliverable.class);
        List<ReadingTypeDeliverable> contractDeliverables = new ArrayList<>();
        when(unmatchedDeliverable.getName()).thenReturn("Monthly A-");
        when(unmatchedDeliverable.getReadingType()).thenReturn(mock(ReadingType.class));
        contractDeliverables.add(readingTypeDeliverable);
        contractDeliverables.add(unmatchedDeliverable);
        when(metrologyContract.getDeliverables()).thenReturn(contractDeliverables);

        when(metrologyConfigurationService.findAllMetrologyConfigurations()).thenReturn(Collections.singletonList(metrologyConfiguration));
        when(metrologyConfiguration.getContracts()).thenReturn(Collections.singletonList(metrologyContract));
        when(usagePointConfigurationService.getValidationRuleSets(metrologyContract)).thenReturn(Collections.singletonList(validationRuleSet));
        when(usagePointConfigurationService.getMatchingDeliverablesOnValidationRuleSet(metrologyContract, validationRuleSet)).thenReturn(Collections.singletonList(readingTypeDeliverable));

        Response response = target("/validationrulesets/" + VALIDATION_RULE_SET_ID + "/purposes/overview").request().get();

        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(model.<Integer>get("$.total")).isEqualTo(1);
        assertThat(model.<String>get("$.purposes[0].outputs[0].outputName")).isEqualTo("Monthly A+");
        assertThat(model.<Boolean>get("$..purposes[0].outputs[0].isMatched")).isEqualTo(true);
        assertThat(model.<String>get("$.purposes[0].outputs[1].outputName")).isEqualTo("Monthly A-");
        assertThat(model.<Boolean>get("$..purposes[0].outputs[1].isMatched")).isEqualTo(false);
        assertThat(model.<String>get("$.purposes[0].name")).isEqualTo("Billing");
        assertThat(model.<Boolean>get("$.purposes[0].active")).isEqualTo(true);
        assertThat(model.<String>get("$.purposes[0].metrologyConfigurationInfo.name")).isEqualTo("Residential prosumer with 1 meter");
        assertThat(model.<Integer>get("$.purposes[0].metrologyConfigurationInfo.id")).isEqualTo(13);
        assertThat(model.<Integer>get("$.purposes[0].id")).isEqualTo(12);
        assertThat(model.<Integer> get("$.purposes[0].version")).isEqualTo(15);
    }

    @Test
    public void testLinkMetrologyPurposeToValidationRuleSet() {
        initMocks();
        when(metrologyConfigurationService.findMetrologyContract(1)).thenReturn(Optional.of(metrologyContract));
        doNothing().when(usagePointConfigurationService).addValidationRuleSet(metrologyContract, validationRuleSet);
        when(metrologyContract.getVersion()).thenReturn(METORLOGY_CONTRACT_VERSION);
        when(metrologyContract.getId()).thenReturn(METROLOGY_CONTRACT_ID);
        when(metrologyContract.getMetrologyPurpose()).thenReturn(metrologyPurpose);
        when(metrologyConfigurationService.findAndLockMetrologyContract(METROLOGY_CONTRACT_ID, METORLOGY_CONTRACT_VERSION)).thenReturn(Optional.of(metrologyContract));

        MetrologyContractInfos metrologyContractInfos = new MetrologyContractInfos();
        metrologyContractInfos.contracts = Collections.singletonList(new MetrologyContractInfo(metrologyContract));

        Response response = target("/validationrulesets/" + VALIDATION_RULE_SET_ID + "/purposes/add").request().put(Entity.json(metrologyContractInfos));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(usagePointConfigurationService).addValidationRuleSet(metrologyContract, validationRuleSet);
    }

    private void initMocks() {
        doReturn(Optional.of(validationRuleSet)).when(validationService).getValidationRuleSet(VALIDATION_RULE_SET_ID);
        when(metrologyContract.getMetrologyConfiguration()).thenReturn(metrologyConfiguration);
        when(metrologyContract.getId()).thenReturn(METROLOGY_CONTRACT_ID);
        when(metrologyConfiguration.getId()).thenReturn(13L);
        when(metrologyConfiguration.isActive()).thenReturn(true);
        when(metrologyConfiguration.getName()).thenReturn("Residential prosumer with 1 meter");
        when(metrologyContract.getMetrologyPurpose()).thenReturn(metrologyPurpose);
        when(metrologyContract.getVersion()).thenReturn(15L);
        when(metrologyPurpose.getName()).thenReturn("Billing");
        when(metrologyContract.getDeliverables()).thenReturn(Collections.singletonList(readingTypeDeliverable));
        doReturn(Collections.singletonList(validationRule)).when(validationRuleSet).getRules();
        doReturn(Collections.singleton(readingType)).when(validationRule).getReadingTypes();
        when(readingTypeDeliverable.getName()).thenReturn("Monthly A+");
        when(readingTypeDeliverable.getReadingType()).thenReturn(readingType);
    }
}
