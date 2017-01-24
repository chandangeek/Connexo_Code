package com.elster.jupiter.metering.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.metering.BypassStatus;
import com.elster.jupiter.metering.GasDetail;
import com.elster.jupiter.metering.HeatDetail;
import com.elster.jupiter.metering.HeatDetailBuilder;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointDetail;
import com.elster.jupiter.metering.UsagePointFilter;
import com.elster.jupiter.metering.WaterDetail;
import com.elster.jupiter.util.YesNoAnswer;
import com.elster.jupiter.util.units.Unit;

import javax.validation.ConstraintViolationException;
import java.math.BigDecimal;
import java.time.Instant;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import static junit.framework.TestCase.fail;
import static org.assertj.core.api.Assertions.assertThat;

public class UsagePointDetailsPersistentTestIT {
    static MeteringInMemoryBootstrapModule inMemoryPersistentModule = new MeteringInMemoryBootstrapModule("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0");

    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(inMemoryPersistentModule.getTransactionService());

    @BeforeClass
    public static void setUp() {
        inMemoryPersistentModule.activate();
    }

    @AfterClass
    public static void afterClass() {
        inMemoryPersistentModule.deactivate();
    }

    @Transactional
    @Test
    public void testSaveEmpty() {
        inMemoryPersistentModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY)
                .get().newUsagePoint("test", Instant.EPOCH).create();

        UsagePointFilter usagePointFilter = new UsagePointFilter();
        usagePointFilter.setName("*");

        UsagePoint usagePoint = inMemoryPersistentModule.getMeteringService().getUsagePoints(usagePointFilter).find().get(0);

        assertThat(usagePoint.getInstallationTime()).isEqualTo(Instant.EPOCH);
        assertThat(usagePoint.getName()).isEqualTo("test");
        assertThat(usagePoint.getServiceCategory().getKind()).isEqualTo(ServiceKind.ELECTRICITY);
    }

    @Transactional
    @Test
    public void testSaveWithGasDetails() {
        UsagePoint up = inMemoryPersistentModule.getMeteringService().getServiceCategory(ServiceKind.GAS)
                .get().newUsagePoint("test", inMemoryPersistentModule.getClock().instant().minusSeconds(1000)).create();

        up.newGasDetailBuilder(inMemoryPersistentModule.getClock().instant())
                .withCollar(YesNoAnswer.YES)
                .withCap(YesNoAnswer.YES)
                .withClamp(YesNoAnswer.YES)
                .withValve(YesNoAnswer.YES)
                .withBypass(YesNoAnswer.YES)
                .withBypassStatus(BypassStatus.OPEN)
                .withGrounded(YesNoAnswer.YES)
                .withInterruptible(YesNoAnswer.YES)
                .withLimiter(YesNoAnswer.YES)
                .withLoadLimit(Unit.CUBIC_METER_PER_HOUR.amount(BigDecimal.valueOf(123.45)))
                .withLoadLimiterType("LoadLimit")
                .withPhysicalCapacity(Unit.CUBIC_METER_PER_HOUR.amount(BigDecimal.valueOf(123.45)))
                .withPressure(Unit.PASCAL.amount(BigDecimal.valueOf(34.5)))
                .create();

        up.update();

        UsagePointFilter usagePointFilter = new UsagePointFilter();
        usagePointFilter.setName("*");

        GasDetail gasDetail = (GasDetail) inMemoryPersistentModule.getMeteringService().getUsagePoints(usagePointFilter).find().get(0).getDetail(inMemoryPersistentModule.getClock().instant()).get();

        //general properties
        assertThat(gasDetail.isCollarInstalled()).isEqualTo(YesNoAnswer.YES);

        //gas specific properties
        assertThat(gasDetail.isCapped()).isEqualTo(YesNoAnswer.YES);
        assertThat(gasDetail.isClamped()).isEqualTo(YesNoAnswer.YES);
        assertThat(gasDetail.isBypassInstalled()).isEqualTo(YesNoAnswer.YES);
        assertThat(gasDetail.getBypassStatus().equals(BypassStatus.OPEN)).isTrue();
        assertThat(gasDetail.isGrounded()).isEqualTo(YesNoAnswer.YES);
        assertThat(gasDetail.isInterruptible()).isEqualTo(YesNoAnswer.YES);
        assertThat(gasDetail.isLimiter()).isEqualTo(YesNoAnswer.YES);
        assertThat(gasDetail.getLoadLimit().equals(Unit.CUBIC_METER_PER_HOUR.amount(BigDecimal.valueOf(123.45)))).isTrue();
        assertThat(gasDetail.getLoadLimiterType().equals("LoadLimit")).isTrue();
        assertThat(gasDetail.getPhysicalCapacity().equals(Unit.CUBIC_METER_PER_HOUR.amount(BigDecimal.valueOf(123.45)))).isTrue();
        assertThat(gasDetail.getPressure().equals(Unit.PASCAL.amount(BigDecimal.valueOf(34.5)))).isTrue();
    }

    @Transactional
    @Test
    public void testSaveWithWaterDetails() {
        UsagePoint up = inMemoryPersistentModule.getMeteringService().getServiceCategory(ServiceKind.WATER)
                .get().newUsagePoint("test", inMemoryPersistentModule.getClock().instant().minusSeconds(1000)).create();

        up.newWaterDetailBuilder(inMemoryPersistentModule.getClock().instant())
                .withCollar(YesNoAnswer.YES)
                .withCap(YesNoAnswer.YES)
                .withClamp(YesNoAnswer.YES)
                .withValve(YesNoAnswer.YES)
                .withBypass(YesNoAnswer.YES)
                .withBypassStatus(BypassStatus.OPEN)
                .withGrounded(YesNoAnswer.YES)
                .withLimiter(YesNoAnswer.YES)
                .withLoadLimit(Unit.CUBIC_METER_PER_HOUR.amount(BigDecimal.valueOf(123.45)))
                .withLoadLimiterType("LoadLimit")
                .withPhysicalCapacity(Unit.CUBIC_METER_PER_HOUR.amount(BigDecimal.valueOf(123.45)))
                .withPressure(Unit.PASCAL.amount(BigDecimal.valueOf(34.5)))
                .create();

        up.update();

        UsagePointFilter usagePointFilter = new UsagePointFilter();
        usagePointFilter.setName("*");

        WaterDetail waterDetail = (WaterDetail) inMemoryPersistentModule.getMeteringService().getUsagePoints(usagePointFilter).find().get(0).getDetail(inMemoryPersistentModule.getClock().instant()).get();

        //general properties
        assertThat(waterDetail.isCollarInstalled()).isEqualTo(YesNoAnswer.YES);

        //gas specific properties
        assertThat(waterDetail.isCapped()).isEqualTo(YesNoAnswer.YES);
        assertThat(waterDetail.isClamped()).isEqualTo(YesNoAnswer.YES);
        assertThat(waterDetail.isBypassInstalled()).isEqualTo(YesNoAnswer.YES);
        assertThat(waterDetail.getBypassStatus().equals(BypassStatus.OPEN)).isTrue();
        assertThat(waterDetail.isGrounded()).isEqualTo(YesNoAnswer.YES);
        assertThat(waterDetail.isLimiter()).isEqualTo(YesNoAnswer.YES);
        assertThat(waterDetail.getLoadLimit().equals(Unit.CUBIC_METER_PER_HOUR.amount(BigDecimal.valueOf(123.45)))).isTrue();
        assertThat(waterDetail.getLoadLimiterType().equals("LoadLimit")).isTrue();
        assertThat(waterDetail.getPhysicalCapacity().equals(Unit.CUBIC_METER_PER_HOUR.amount(BigDecimal.valueOf(123.45)))).isTrue();
        assertThat(waterDetail.getPressure().equals(Unit.PASCAL.amount(BigDecimal.valueOf(34.5)))).isTrue();
    }

    @Transactional
    @Test
    public void testSaveWithHeatDetails() {
        UsagePoint up = inMemoryPersistentModule.getMeteringService().getServiceCategory(ServiceKind.HEAT)
                .get().newUsagePoint("test", inMemoryPersistentModule.getClock().instant().minusSeconds(1000)).create();

        up.newHeatDetailBuilder(inMemoryPersistentModule.getClock().instant())
                .withCollar(YesNoAnswer.YES)
                .withValve(YesNoAnswer.YES)
                .withBypass(YesNoAnswer.YES)
                .withBypassStatus(BypassStatus.OPEN)
                .withPhysicalCapacity(Unit.WATT_HOUR.amount(BigDecimal.valueOf(123.45)))
                .withPressure(Unit.PASCAL.amount(BigDecimal.valueOf(34.5)))
                .create();

        up.update();

        UsagePointFilter usagePointFilter = new UsagePointFilter();
        usagePointFilter.setName("*");

        HeatDetail waterDetail = (HeatDetail) inMemoryPersistentModule.getMeteringService().getUsagePoints(usagePointFilter).find().get(0).getDetail(inMemoryPersistentModule.getClock().instant()).get();

        //general properties
        assertThat(waterDetail.isCollarInstalled()).isEqualTo(YesNoAnswer.YES);

        //gas specific properties
        assertThat(waterDetail.isBypassInstalled()).isEqualTo(YesNoAnswer.YES);
        assertThat(waterDetail.getBypassStatus().equals(BypassStatus.OPEN)).isTrue();
        assertThat(waterDetail.getPhysicalCapacity().equals(Unit.WATT_HOUR.amount(BigDecimal.valueOf(123.45)))).isTrue();
        assertThat(waterDetail.getPressure().equals(Unit.PASCAL.amount(BigDecimal.valueOf(34.5)))).isTrue();
    }
}
