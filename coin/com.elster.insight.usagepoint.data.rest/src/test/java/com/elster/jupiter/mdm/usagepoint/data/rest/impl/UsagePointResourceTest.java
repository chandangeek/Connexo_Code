package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.devtools.tests.rules.Using;
import com.elster.jupiter.metering.Channel;
import com.elster.jupiter.metering.ElectricityDetailBuilder;
import com.elster.jupiter.metering.IntervalReadingRecord;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ReadingQualityRecord;
import com.elster.jupiter.metering.ReadingQualityType;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointBuilder;
import com.elster.jupiter.metering.UsagePointCustomPropertySetExtension;
import com.elster.jupiter.metering.readings.ReadingQuality;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.Ranges;
import com.elster.jupiter.util.units.Quantity;
import com.elster.jupiter.validation.DataValidationStatus;
import com.elster.jupiter.validation.ValidationEvaluator;
import com.google.common.collect.Range;
import com.jayway.jsonpath.JsonModel;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mock;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UsagePointResourceTest extends UsagePointDataRestApplicationJerseyTest {

    @Rule
    public TestRule timeZoneNeutral = Using.timeZoneOfMcMurdo();

    public static final Instant NOW = ZonedDateTime.of(2015, 12, 10, 10, 43, 13, 0, ZoneId.systemDefault()).toInstant();
    public static final Instant LAST_READING = ZonedDateTime.of(2015, 12, 9, 10, 43, 13, 0, ZoneId.systemDefault()).toInstant();
    private static long intervalStart = 1410774630000L;

    @Mock
    private ValidationEvaluator evaluator;

    @Mock
    private User principal;
    @Mock
    private UsagePoint usagePoint;
    @Mock
    private Meter meter;
    @Mock
    private ServiceCategory serviceCategory;
    @Mock
    private MeterActivation meterActivation;
    @Mock
    private Channel channel, register;
    @Mock
    private ReadingQuality suspect, notSuspect;
    @Mock
    private IntervalReadingRecord irr1, irr2, irr3, irr4;
    @Mock
    private UsagePointBuilder usagePointBuilder;
    @Mock
    private ElectricityDetailBuilder electricityDetailBuilder;

    @Before
    public void setUp1() {
        when(meteringService.findUsagePoint("MRID")).thenReturn(Optional.of(usagePoint));
        when(meteringService.getServiceCategory(ServiceKind.ELECTRICITY)).thenReturn(Optional.of(serviceCategory));

        when(serviceCategory.newUsagePoint(anyString())).thenReturn(usagePointBuilder);
        when(usagePointBuilder.withIsSdp(anyBoolean())).thenReturn(usagePointBuilder);
        when(usagePointBuilder.withIsVirtual(anyBoolean())).thenReturn(usagePointBuilder);
        when(usagePointBuilder.withName(anyString())).thenReturn(usagePointBuilder);
        when(usagePointBuilder.withReadRoute(anyString())).thenReturn(usagePointBuilder);
        when(usagePointBuilder.withServiceDeliveryRemark(anyString())).thenReturn(usagePointBuilder);
        when(usagePointBuilder.withServicePriority(anyString())).thenReturn(usagePointBuilder);
        when(usagePointBuilder.withInstallationTime(Instant.EPOCH)).thenReturn(usagePointBuilder);
        when(usagePointBuilder.withServiceLocationString(anyString())).thenReturn(usagePointBuilder);
        when(usagePointBuilder.create()).thenReturn(usagePoint);
        when(usagePointBuilder.validate()).thenReturn(usagePoint);

        when(usagePoint.newElectricityDetailBuilder(any(Instant.class))).thenReturn(electricityDetailBuilder);
        when(electricityDetailBuilder.withCollar(any())).thenReturn(electricityDetailBuilder);
        when(electricityDetailBuilder.withEstimatedLoad(any(Quantity.class))).thenReturn(electricityDetailBuilder);
        when(electricityDetailBuilder.withGrounded(anyBoolean())).thenReturn(electricityDetailBuilder);
        when(electricityDetailBuilder.withInterruptible(anyBoolean())).thenReturn(electricityDetailBuilder);
        when(electricityDetailBuilder.withLimiter(anyBoolean())).thenReturn(electricityDetailBuilder);
        when(electricityDetailBuilder.withLoadLimiterType(any())).thenReturn(electricityDetailBuilder);
        when(electricityDetailBuilder.withLoadLimit(any(Quantity.class))).thenReturn(electricityDetailBuilder);
        when(electricityDetailBuilder.withRatedPower(any(Quantity.class))).thenReturn(electricityDetailBuilder);
        when(electricityDetailBuilder.withRatedCurrent(any(Quantity.class))).thenReturn(electricityDetailBuilder);
        when(electricityDetailBuilder.withPhaseCode(any())).thenReturn(electricityDetailBuilder);
        when(electricityDetailBuilder.withNominalServiceVoltage(any(Quantity.class))).thenReturn(electricityDetailBuilder);

        when(clock.instant()).thenReturn(NOW);
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        
        when(usagePoint.getMeter(any())).thenReturn(Optional.of(meter));
        when(usagePoint.getServiceCategory()).thenReturn(serviceCategory);
        when(usagePoint.getCreateDate()).thenReturn(Instant.now().minusSeconds(60*60*24));
        when(usagePoint.getModificationDate()).thenReturn(Instant.now().minusSeconds(60*60*5));
        when(usagePoint.getDetail(any(Instant.class))).thenReturn(Optional.empty());
        when(usagePoint.getCurrentMeterActivation()).thenReturn(Optional.of(meterActivation));
        when(usagePoint.getServiceLocation()).thenReturn(Optional.empty());
        when(usagePoint.getMRID()).thenReturn("MRID");
        when(usagePoint.getInstallationTime()).thenReturn(Instant.EPOCH);
        when(usagePoint.getMetrologyConfiguration()).thenReturn(Optional.empty());
        when(usagePoint.getServiceLocationString()).thenReturn("serviceLocation");

        UsagePointCustomPropertySetExtension extension = mock(UsagePointCustomPropertySetExtension.class);
        when(extension.getAllPropertySets()).thenReturn(Collections.emptyList());
        when(usagePoint.forCustomProperties()).thenReturn(extension);
      
        doReturn(Arrays.asList(meterActivation)).when(meter).getMeterActivations();
        
        Range<Instant> intervalToNow = Ranges.openClosed(Instant.ofEpochMilli(intervalStart), Instant.now());
        when(meterActivation.getRange()).thenReturn(intervalToNow);
        when(meterActivation.getChannels()).thenReturn(Arrays.asList(channel, register));
        
        when(register.isRegular()).thenReturn(false);
        when(channel.isRegular()).thenReturn(true);
        when(register.getMeterActivation()).thenReturn(meterActivation);
        when(channel.getMeterActivation()).thenReturn(meterActivation);
        when(channel.getReadings(any())).thenReturn(Arrays.asList(irr1, irr2, irr3, irr4));
        
        when(usagePointConfigurationService.findMetrologyConfigurationForUsagePoint(any())).thenReturn(Optional.empty());
        
        when(validationService.getEvaluator()).thenReturn(evaluator);
        when(validationService.getEvaluator(eq(meter), any(Range.class))).thenReturn(evaluator);
        when(validationService.getLastChecked(any(MeterActivation.class))).thenReturn(Optional.of(NOW));
        when(suspect.getTypeCode()).thenReturn("3.5.258");
        when(notSuspect.getTypeCode()).thenReturn("0.0.0");
        when(suspect.getType()).thenReturn(new ReadingQualityType("3.5.258"));
        when(notSuspect.getType()).thenReturn(new ReadingQualityType("0.0.0"));
        
        ReadingQualityType readingQualitySuspect = new ReadingQualityType("3.5.258");
        DataValidationStatus statusForSuspect = mockDataValidationStatus(readingQualitySuspect, false);
        when(evaluator.getValidationStatus(eq(channel), any(), any())).thenReturn(Arrays.asList(statusForSuspect, statusForSuspect, statusForSuspect, statusForSuspect));
        when(evaluator.getValidationStatus(eq(register), any(), any())).thenReturn(Arrays.asList(statusForSuspect, statusForSuspect, statusForSuspect, statusForSuspect, statusForSuspect));
    }

    @Test
    public void testGetUsagePointInfo() {
        
        when(securityContext.getUserPrincipal()).thenReturn(principal);
        when(principal.hasPrivilege(any(String.class), any(String.class))).thenReturn(true);
        UsagePointInfo response = target("usagepoints/MRID").request().get(UsagePointInfo.class);

        assertThat(response.mRID).isEqualTo("MRID");
    }

    @Test
    public void testValidateUsagePointGeneralBeforeCreating() {

        UsagePointInfo info = new UsagePointInfo();
        info.mRID = "test";
        info.installationTime = Instant.EPOCH.toEpochMilli();
        info.isSdp = true;
        info.isVirtual = true;
        info.techInfo = new ElectricityUsagePointDetailsInfo();

        Response response = target("usagepoints").queryParam("validate",true).queryParam("step",1).request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(202);
    }

    @Test
    public void testValidateUsagePointGeneralBeforeCreatingFailed() throws Exception {

        UsagePointInfo info = new UsagePointInfo();
        info.isVirtual = true;

        Response response = target("usagepoints").queryParam("validate",true).queryParam("step",1).request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(400);
        JsonModel jsonModel = JsonModel.create((ByteArrayInputStream) response.getEntity());
        assertThat(jsonModel.<Boolean>get("$.success")).isFalse();
        assertThat(jsonModel.<String >get("$.errors[0].id")).isEqualTo("mRID");
        assertThat(jsonModel.<String >get("$.errors[1].id")).isEqualTo("serviceCategory");
        assertThat(jsonModel.<String >get("$.errors[2].id")).isEqualTo("typeOfUsagePoint");
    }


    @Test
    public void testValidateUsagePointTechnicalBeforeCreating() {

        UsagePointInfo info = new UsagePointInfo();
        info.mRID = "test";
        info.installationTime = Instant.EPOCH.toEpochMilli();
        info.isSdp = true;
        info.isVirtual = true;
        info.techInfo = new ElectricityUsagePointDetailsInfo();

        Response response = target("usagepoints").queryParam("validate",true).queryParam("step",2).request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(202);
    }

    @Test
    public void testUsagePointCreating() {

        UsagePointInfo info = new UsagePointInfo();
        info.mRID = "test";
        info.installationTime = Instant.EPOCH.toEpochMilli();
        info.isSdp = true;
        info.isVirtual = true;
        info.techInfo = new ElectricityUsagePointDetailsInfo();

        Response response = target("usagepoints").request().post(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(201);
    }

    @Test
    public void testUpadateUsagePoint() {
        when(meteringService.findUsagePoint(1L)).thenReturn(Optional.of(usagePoint));
        when(meteringService.findAndLockUsagePointByIdAndVersion(1L, 1L)).thenReturn(Optional.of(usagePoint));
        UsagePointInfo info = new UsagePointInfo();
        info.id = 1L;
        info.mRID = "upd";
        info.name = "upd";
        info.location = "upd";
        info.installationTime = Instant.EPOCH.toEpochMilli();
        info.isSdp = true;
        info.isVirtual = true;
        info.readRoute = "upd";
        info.serviceDeliveryRemark = "upd";
        info.version = 1L;
        info.techInfo = new ElectricityUsagePointDetailsInfo();

        Response response = target("usagepoints/1").request().put(Entity.json(info));
        assertThat(response.getStatus()).isEqualTo(200);
        verify(usagePoint, never()).setMRID(anyString());
        verify(usagePoint, times(1)).setName("upd");
        verify(usagePoint, times(1)).setServiceLocationString("upd");
        verify(usagePoint, never()).setInstallationTime(any(Instant.class));
        verify(usagePoint, never()).setSdp(anyBoolean());
        verify(usagePoint, never()).setVirtual(anyBoolean());
        verify(usagePoint, times(1)).setReadRoute("upd");
        verify(usagePoint, times(1)).setServiceDeliveryRemark("upd");
        verify(usagePoint, times(1)).update();
        verify(usagePoint, times(1)).newElectricityDetailBuilder(any(Instant.class));
    }

    private DataValidationStatus mockDataValidationStatus(ReadingQualityType readingQualityType, boolean isBulk) {
        DataValidationStatus status = mock(DataValidationStatus.class);
        ReadingQualityRecord readingQualityRecord = mock(ReadingQualityRecord.class);
        when(readingQualityRecord.getType()).thenReturn(readingQualityType);
        List<? extends ReadingQualityRecord> readingQualities = Arrays.asList(readingQualityRecord);
        if (isBulk) {
            doReturn(readingQualities).when(status).getBulkReadingQualities();
        } else {
            doReturn(readingQualities).when(status).getReadingQualities();
        }
        return status;
    }
}