package com.elster.jupiter.mdm.usagepoint.config.rest.impl;

import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.validation.ValidationRule;
import com.elster.jupiter.validation.ValidationRuleSet;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    private final long validationRuleSetId = 555;
    private final long metrologyContractId = 12;

    @Test
    public void testGetLinkedMetrologyConfigurationPurposes() throws IOException {
        initMocks();
        doReturn(Optional.of(validationRuleSet)).when(validationService).getValidationRuleSet(validationRuleSetId);
        when(usagePointConfigurationService.getMetrologyContractsLinkedToValidationRuleSet(validationRuleSet))
                .thenReturn(Collections.singletonList(metrologyContract));

        Response response = target("/validationruleset/" + validationRuleSetId + "/purposes").request().get();

        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(model.<Integer>get("$.total")).isEqualTo(1);
        assertThat(model.<String>get("$.purposes[0].purpose")).isEqualTo("Billing");
        assertThat(model.<Boolean>get("$.purposes[0].active")).isEqualTo(true);
        assertThat(model.<Integer>get("$.purposes[0].metrologyContractId")).isEqualTo(12);
        assertThat(model.<String>get("$.purposes[0].outputs[0].outputName")).isEqualTo("Monthly A+");
        assertThat(model.<Boolean>get("$.purposes[0].outputs[0].isMatched")).isEqualTo(true);
        assertThat(model.<Integer>get("$.purposes[0].metrologyConfigurationInfo.id")).isEqualTo(13);
        assertThat(model.<String>get("$.purposes[0].metrologyConfigurationInfo.name")).isEqualTo("Residential prosumer with 1 meter");

        verifyMocks();
    }

    @Test
    public void testRemoveMetrologyConfigurationPurpose() {
        doReturn(Optional.of(validationRuleSet)).when(validationService).getValidationRuleSet(validationRuleSetId);
        doReturn(Optional.of(metrologyContract)).when(metrologyConfigurationService)
                .findMetrologyContract(metrologyContractId);

        Response response = target("/validationruleset/" + validationRuleSetId + "/purposes/" + metrologyContractId).request()
                .delete();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        verify(validationService).getValidationRuleSet(validationRuleSetId);
        verify(metrologyConfigurationService).findMetrologyContract(metrologyContractId);
    }

    @Test
    public void testGetLinkablePurposes() throws Exception {
        initMocks();
        doReturn(Optional.of(validationRuleSet)).when(validationService).getValidationRuleSet(validationRuleSetId);
        when(metrologyConfigurationService.findAllMetrologyConfigurations()).thenReturn(Collections.singletonList(metrologyConfiguration));
        when(metrologyConfiguration.getContracts()).thenReturn(Collections.singletonList(metrologyContract));
        when(usagePointConfigurationService.getValidationRuleSets(metrologyContract)).thenReturn(Collections.singletonList(validationRuleSet));
        when(usagePointConfigurationService.isLinkableValidationRuleSet(metrologyContract, validationRuleSet, Collections.singletonList(validationRuleSet))).thenReturn(true);

        Response response = target("/validationruleset/" + validationRuleSetId + "/purposes/overview").request().get();

        JsonModel model = JsonModel.model((InputStream) response.getEntity());
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(model.<Integer>get("$.total")).isEqualTo(1);
        assertThat(model.<String>get("$.purposes[0].outputs[0].outputName")).isEqualTo("Monthly A+");
        assertThat(model.<Boolean>get("$..purposes[0].outputs[0].isMatched")).isEqualTo(true);
        assertThat(model.<String>get("$.purposes[0].purpose")).isEqualTo("Billing");
        assertThat(model.<Boolean>get("$.purposes[0].active")).isEqualTo(true);
        assertThat(model.<String>get("$.purposes[0].metrologyConfigurationInfo.name")).isEqualTo("Residential prosumer with 1 meter");
        assertThat(model.<Integer>get("$.purposes[0].metrologyConfigurationInfo.id")).isEqualTo(13);
        assertThat(model.<Integer>get("$.purposes[0].metrologyContractId")).isEqualTo(12);

        verifyMocks();
        verify(usagePointConfigurationService).getValidationRuleSets(metrologyContract);
        verify(usagePointConfigurationService).isLinkableValidationRuleSet(metrologyContract, validationRuleSet, Collections.singletonList(validationRuleSet));
    }

    @Test
    public void testLinkMetrologyPurposeToValidationRuleSet() {
        doReturn(Optional.of(validationRuleSet)).when(validationService).getValidationRuleSet(validationRuleSetId);
        when(metrologyConfigurationService.findMetrologyContract(1)).thenReturn(Optional.of(metrologyContract));
        doNothing().when(usagePointConfigurationService).addValidationRuleSet(metrologyContract, validationRuleSet);
        long metrologyContractIds[] = {1};

        Response response = target("/validationruleset/" + validationRuleSetId + "/purposes/add").request().put(Entity.entity(metrologyContractIds, MediaType.APPLICATION_JSON_TYPE));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        verify(validationService).getValidationRuleSet(validationRuleSetId);
        verify(metrologyConfigurationService).findMetrologyContract(1);
        verify(usagePointConfigurationService).addValidationRuleSet(metrologyContract, validationRuleSet);
    }

    private void initMocks() {
        Set<ReadingType> readingTypeSet = new HashSet<>(1);
        readingTypeSet.add(readingType);

        when(metrologyContract.getMetrologyConfiguration()).thenReturn(metrologyConfiguration);
        when(metrologyContract.getId()).thenReturn(metrologyContractId);
        when(metrologyConfiguration.getId()).thenReturn(13L);
        when(metrologyConfiguration.isActive()).thenReturn(true);
        when(metrologyConfiguration.getName()).thenReturn("Residential prosumer with 1 meter");
        when(metrologyContract.getMetrologyPurpose()).thenReturn(metrologyPurpose);
        when(metrologyPurpose.getName()).thenReturn("Billing");
        when(metrologyContract.getDeliverables()).thenReturn(Collections.singletonList(readingTypeDeliverable));
        doReturn(Collections.singletonList(validationRule)).when(validationRuleSet).getRules();
        doReturn(readingTypeSet).when(validationRule).getReadingTypes();
        when(readingTypeDeliverable.getName()).thenReturn("Monthly A+");
        when(readingTypeDeliverable.getReadingType()).thenReturn(readingType);
    }

    private void verifyMocks() {
        verify(validationService).getValidationRuleSet(validationRuleSetId);
        verify(metrologyContract).getDeliverables();
        verify(metrologyContract).getMetrologyPurpose();
        verify(metrologyContract).getId();
        verify(metrologyContract, times(3)).getMetrologyConfiguration();
        verify(metrologyConfiguration).getName();
        verify(metrologyConfiguration).getId();
        verify(metrologyConfiguration).isActive();
        verify(metrologyPurpose).getName();
        verify(validationRule).getReadingTypes();
        verify(validationRuleSet).getRules();
        verify(readingTypeDeliverable).getName();
        verify(readingTypeDeliverable).getReadingType();
    }
}
