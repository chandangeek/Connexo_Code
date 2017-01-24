package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.mdm.usagepoint.config.rest.FormulaInfo;
import com.elster.jupiter.mdm.usagepoint.config.rest.ReadingTypeDeliverablesInfo;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ChannelsContainer;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.EffectiveMetrologyConfigurationOnUsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.metering.config.MetrologyPurpose;
import com.elster.jupiter.metering.config.ReadingTypeDeliverable;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.rest.util.VersionInfo;
import com.elster.jupiter.validation.ValidationContextImpl;
import com.elster.jupiter.validation.ValidationEvaluator;

import com.jayway.jsonpath.JsonModel;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UsagePointOutputResourceTest extends UsagePointDataRestApplicationJerseyTest {

    private static final String USAGE_POINT_NAME = "Der Name";
    private static final String EXPECTED_FORMULA_DESCRIPTION = "Formula Description";

    @Mock
    private UsagePoint usagePoint;
    @Mock
    private EffectiveMetrologyConfigurationOnUsagePoint effectiveMC;
    @Mock
    private ChannelsContainer channelsContainer;

    @Before
    public void before() {
        when(meteringService.findUsagePointByName(anyString())).thenReturn(Optional.empty());
        when(meteringService.findUsagePointByName(USAGE_POINT_NAME)).thenReturn(Optional.of(usagePoint));
        when(meteringService.findAndLockUsagePointByIdAndVersion(usagePoint.getId(), usagePoint.getVersion())).thenReturn(Optional.of(usagePoint));
        UsagePointMetrologyConfiguration metrologyConfiguration = mockMetrologyConfigurationWithContract(1, "mc");
        when(effectiveMC.getMetrologyConfiguration()).thenReturn(metrologyConfiguration);
        when(usagePoint.getCurrentEffectiveMetrologyConfiguration()).thenReturn(Optional.of(effectiveMC));
        when(effectiveMC.getMetrologyConfiguration()).thenReturn(metrologyConfiguration);
        when(effectiveMC.getChannelsContainer(any())).thenReturn(Optional.of(channelsContainer));
        when(effectiveMC.getChannelsContainer(any(), any(Instant.class))).thenReturn(Optional.empty());
        when(effectiveMC.getUsagePoint()).thenReturn(usagePoint);
        when(channelsContainer.getChannel(any())).thenReturn(Optional.empty());
        ReadingTypeDeliverablesInfo readingTypeDeliverablesInfo = new ReadingTypeDeliverablesInfo();
        readingTypeDeliverablesInfo.formula = new FormulaInfo();
        readingTypeDeliverablesInfo.formula.description = EXPECTED_FORMULA_DESCRIPTION;
        when(readingTypeDeliverableFactory.asInfo(any(ReadingTypeDeliverable.class))).thenReturn(readingTypeDeliverablesInfo);
        when(clock.instant()).thenReturn(Instant.now());
        MetrologyPurpose metrologyPurpose = usagePoint.getCurrentEffectiveMetrologyConfiguration().get().getMetrologyConfiguration().getContracts().get(1).getMetrologyPurpose();
        when(metrologyConfigurationService.findMetrologyPurpose(101L))
                .thenReturn(Optional.of(metrologyPurpose));
    }

    @Test
    public void testGetOutputsOfUsagePointPurpose() {
        // Business method
        String json = target("/usagepoints/" + USAGE_POINT_NAME + "/purposes/100/outputs").request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.total")).isEqualTo(2);
        // channel output
        assertThat(jsonModel.<Number>get("$.outputs[0].id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.outputs[0].outputType")).isEqualTo("channel");
        assertThat(jsonModel.<String>get("$.outputs[0].name")).isEqualTo("1 regular RT");
        assertThat(jsonModel.<Number>get("$.outputs[0].interval.count")).isEqualTo(15);
        assertThat(jsonModel.<String>get("$.outputs[0].interval.timeUnit")).isEqualTo("minutes");
        assertThat(jsonModel.<String>get("$.outputs[0].readingType.mRID")).isEqualTo("0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0");
        assertThat(jsonModel.<String>get("$.outputs[0].formula.description")).isEqualTo(EXPECTED_FORMULA_DESCRIPTION);
        // register output
        assertThat(jsonModel.<Number>get("$.outputs[1].id")).isEqualTo(2);
        assertThat(jsonModel.<String>get("$.outputs[1].outputType")).isEqualTo("register");
        assertThat(jsonModel.<String>get("$.outputs[1].name")).isEqualTo("2 irregular RT");
        assertThat(jsonModel.<String>get("$.outputs[1].readingType.mRID")).isEqualTo("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0");
        assertThat(jsonModel.<String>get("$.outputs[1].formula.description")).isEqualTo(EXPECTED_FORMULA_DESCRIPTION);
    }

    @Test
    public void testGetOutputById() {
        // Business method
        String json = target("/usagepoints/" + USAGE_POINT_NAME + "/purposes/100/outputs/1").request().get(String.class);

        // Asserts
        JsonModel jsonModel = JsonModel.create(json);
        assertThat(jsonModel.<Number>get("$.id")).isEqualTo(1);
        assertThat(jsonModel.<String>get("$.outputType")).isEqualTo("channel");
        assertThat(jsonModel.<String>get("$.name")).isEqualTo("1 regular RT");
        assertThat(jsonModel.<Number>get("$.interval.count")).isEqualTo(15);
        assertThat(jsonModel.<String>get("$.interval.timeUnit")).isEqualTo("minutes");
        assertThat(jsonModel.<String>get("$.readingType.mRID")).isEqualTo("0.0.2.1.1.1.12.0.0.0.0.0.0.0.0.3.72.0");
        assertThat(jsonModel.<String>get("$.formula.description")).isEqualTo(EXPECTED_FORMULA_DESCRIPTION);
    }

    @Test
    public void testValidatePurposeOnRequest() {
        when(meteringService.findAndLockUsagePointByIdAndVersion(usagePoint.getId(), usagePoint.getVersion())).thenReturn(Optional.of(usagePoint));
        MetrologyContract metrologyContract = usagePoint.getCurrentEffectiveMetrologyConfiguration().get().getMetrologyConfiguration().getContracts().get(0);
        PurposeInfo purposeInfo = createPurposeInfo(metrologyContract);
        // Business method
        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/purposes/100").queryParam("upVersion", usagePoint.getVersion()).request().put(Entity.json(purposeInfo));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(validationService).validate(any(ValidationContextImpl.class), any(Instant.class));
    }

    @Test
    public void testValidatePurposeOnRequestConcurrencyCheck() {
        MetrologyContract metrologyContract = usagePoint.getCurrentEffectiveMetrologyConfiguration().get().getMetrologyConfiguration().getContracts().get(0);
        PurposeInfo purposeInfo = createPurposeInfo(metrologyContract);
        when(meteringService.findAndLockUsagePointByIdAndVersion(usagePoint.getId(), usagePoint.getVersion())).thenReturn(Optional.empty());
        when(meteringService.findUsagePointById(usagePoint.getId())).thenReturn(Optional.of(usagePoint));
        // Business method
        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/purposes/100").queryParam("upVersion", usagePoint.getVersion()).request().put(Entity.json(purposeInfo));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.CONFLICT.getStatusCode());
    }

    @Test
    public void testPurposeActivation(){
        MetrologyContract metrologyContract = usagePoint.getCurrentEffectiveMetrologyConfiguration().get().getMetrologyConfiguration().getContracts().get(1);
        PurposeInfo purposeInfo = createPurposeInfo(metrologyContract);
        when(effectiveMC.getChannelsContainer(metrologyContract)).thenReturn(Optional.empty());
        when(usagePoint.getMeterActivations()).thenReturn(Collections.emptyList());
        // Business method
        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/purposes/101/activate").request().put(Entity.json(purposeInfo));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(effectiveMC).activateOptionalMetrologyContract(eq(metrologyContract), any(Instant.class));
    }

    @Test
    public void testPurposeDeactivation(){
        MetrologyContract metrologyContract = usagePoint.getCurrentEffectiveMetrologyConfiguration().get().getMetrologyConfiguration().getContracts().get(1);
        PurposeInfo purposeInfo = createPurposeInfo(metrologyContract);
        ChannelsContainer channelsContainer = mock(ChannelsContainer.class);
        when(channelsContainer.getChannels()).thenReturn(Collections.emptyList());
        when(effectiveMC.getChannelsContainer(eq(metrologyContract), any(Instant.class))).thenReturn(Optional.of(channelsContainer));
        ValidationEvaluator validationEvaluator = mock(ValidationEvaluator.class);
        when(validationService.getEvaluator()).thenReturn(validationEvaluator);
        doReturn(Collections.emptyList()).when(validationEvaluator).getValidationStatus(any(), any(Channel.class), any(), any());
        // Business method
        Response response = target("usagepoints/" + USAGE_POINT_NAME + "/purposes/101/deactivate").request().put(Entity.json(purposeInfo));

        // Asserts
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        verify(effectiveMC).deactivateOptionalMetrologyContract(eq(metrologyContract), any(Instant.class));
    }

    private PurposeInfo createPurposeInfo(MetrologyContract metrologyContract) {
        PurposeInfo purposeInfo = new PurposeInfo();
        purposeInfo.id = metrologyContract.getId();
        purposeInfo.version = metrologyContract.getVersion();
        purposeInfo.validationInfo = new UsagePointValidationStatusInfo();
        purposeInfo.validationInfo.lastChecked = Instant.ofEpochMilli(1467185935140L);
        purposeInfo.parent = new VersionInfo<>(usagePoint.getId(), usagePoint.getVersion());
        return purposeInfo;
    }
}
