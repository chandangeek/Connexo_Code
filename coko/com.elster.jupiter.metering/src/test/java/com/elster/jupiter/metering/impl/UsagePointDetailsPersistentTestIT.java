package com.elster.jupiter.metering.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.metering.BypassStatus;
import com.elster.jupiter.metering.GasDetail;
import com.elster.jupiter.metering.HeatDetail;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointFilter;
import com.elster.jupiter.metering.WaterDetail;
import com.elster.jupiter.util.units.Unit;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by antfom on 17.02.2016.
 */
public class UsagePointDetailsPersistentTestIT {
    static MeteringInMemoryPersistentModule inMemoryPersistentModule = new MeteringInMemoryPersistentModule("0.0.0.1.1.1.12.0.0.0.0.0.0.0.0.0.72.0");

    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(inMemoryPersistentModule.getTransactionService());


    @AfterClass
    public static void afterClass() {
        inMemoryPersistentModule.deactivate();
    }

    @Transactional
    @Test
    public void testSaveEmpty(){

        UsagePoint up = inMemoryPersistentModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY)
                .get().newUsagePoint("test").withInstallationTime(Instant.EPOCH).create();

        UsagePointFilter usagePointFilter = new UsagePointFilter();
            usagePointFilter.setMrid("*");

        UsagePoint usagePoint = inMemoryPersistentModule.getMeteringService().getUsagePoints(usagePointFilter).find().get(0);

        assertThat(usagePoint.getInstallationTime()).isEqualTo(Instant.EPOCH);
        assertThat(usagePoint.getMRID()).isEqualTo("test");
        assertThat(usagePoint.getServiceCategory().getKind()).isEqualTo(ServiceKind.ELECTRICITY);
    }

    @Transactional
    @Test
    public void testSaveWithGasDetails(){

        UsagePoint up = inMemoryPersistentModule.getMeteringService().getServiceCategory(ServiceKind.GAS)
                .get().newUsagePoint("test").withInstallationTime(inMemoryPersistentModule.getClock().instant().minusSeconds(1000)).create();

        up.newGasDetailBuilder(inMemoryPersistentModule.getClock().instant())
                .withCollar(true)
                .withCapped(true)
                .withClamped(true)
                .withValve(true)
                .withBypass(true)
                .withBypassStatus(BypassStatus.OPEN)
                .withGrounded(true)
                .withInterruptible(true)
                .withLimiter(true)
                .withLoadLimit(Unit.CUBIC_METER_PER_HOUR.amount(BigDecimal.valueOf(123.45)))
                .withLoadLimiterType("LoadLimit")
                .withPhysicalCapacity(Unit.CUBIC_METER_PER_HOUR.amount(BigDecimal.valueOf(123.45)))
                .withPressure(Unit.PASCAL.amount(BigDecimal.valueOf(34.5)))
                .build();

        up.update();

        UsagePointFilter usagePointFilter = new UsagePointFilter();
        usagePointFilter.setMrid("*");

        GasDetail gasDetail = (GasDetail) inMemoryPersistentModule.getMeteringService().getUsagePoints(usagePointFilter).find().get(0).getDetail(inMemoryPersistentModule.getClock().instant()).get();

        //general properties
        assertThat(gasDetail.getCollar().isPresent()).isTrue();
        assertThat(gasDetail.getCollar().get()).isTrue();

        //gas specific properties
        assertThat(gasDetail.getCapped().isPresent()).isTrue();
        assertThat(gasDetail.getCapped().get()).isTrue();
        assertThat(gasDetail.getClamped().isPresent()).isTrue();
        assertThat(gasDetail.getClamped().get()).isTrue();
        assertThat(gasDetail.getBypass().isPresent()).isTrue();
        assertThat(gasDetail.getBypass().get()).isTrue();
        assertThat(gasDetail.getBypassStatus().equals(BypassStatus.OPEN)).isTrue();
        assertThat(gasDetail.isGrounded()).isTrue();
        assertThat(gasDetail.isInterruptible()).isTrue();
        assertThat(gasDetail.isLimiter()).isTrue();
        assertThat(gasDetail.getLoadLimit().equals(Unit.CUBIC_METER_PER_HOUR.amount(BigDecimal.valueOf(123.45)))).isTrue();
        assertThat(gasDetail.getLoadLimiterType().equals("LoadLimit")).isTrue();
        assertThat(gasDetail.getPhysicalCapacity().equals(Unit.CUBIC_METER_PER_HOUR.amount(BigDecimal.valueOf(123.45)))).isTrue();
        assertThat(gasDetail.getPressure().equals(Unit.PASCAL.amount(BigDecimal.valueOf(34.5)))).isTrue();
    }

    @Transactional
    @Test
    public void testSaveWithWaterDetails(){

        UsagePoint up = inMemoryPersistentModule.getMeteringService().getServiceCategory(ServiceKind.WATER)
                .get().newUsagePoint("test").withInstallationTime(inMemoryPersistentModule.getClock().instant().minusSeconds(1000)).create();

        up.newWaterDetailBuilder(inMemoryPersistentModule.getClock().instant())
                .withCollar(true)
                .withCapped(true)
                .withClamped(true)
                .withValve(true)
                .withBypass(true)
                .withBypassStatus(BypassStatus.OPEN)
                .withGrounded(true)
                .withLimiter(true)
                .withLoadLimit(Unit.CUBIC_METER_PER_HOUR.amount(BigDecimal.valueOf(123.45)))
                .withLoadLimiterType("LoadLimit")
                .withPhysicalCapacity(Unit.CUBIC_METER_PER_HOUR.amount(BigDecimal.valueOf(123.45)))
                .withPressure(Unit.PASCAL.amount(BigDecimal.valueOf(34.5)))
                .build();

        up.update();

        UsagePointFilter usagePointFilter = new UsagePointFilter();
        usagePointFilter.setMrid("*");

        WaterDetail waterDetail = (WaterDetail) inMemoryPersistentModule.getMeteringService().getUsagePoints(usagePointFilter).find().get(0).getDetail(inMemoryPersistentModule.getClock().instant()).get();

        //general properties
        assertThat(waterDetail.getCollar().isPresent()).isTrue();
        assertThat(waterDetail.getCollar().get()).isTrue();

        //gas specific properties
        assertThat(waterDetail.getCapped().isPresent()).isTrue();
        assertThat(waterDetail.getCapped().get()).isTrue();
        assertThat(waterDetail.getClamped().isPresent()).isTrue();
        assertThat(waterDetail.getClamped().get()).isTrue();
        assertThat(waterDetail.getBypass().isPresent()).isTrue();
        assertThat(waterDetail.getBypass().get()).isTrue();
        assertThat(waterDetail.getBypassStatus().equals(BypassStatus.OPEN)).isTrue();
        assertThat(waterDetail.isGrounded()).isTrue();
        assertThat(waterDetail.isLimiter()).isTrue();
        assertThat(waterDetail.getLoadLimit().equals(Unit.CUBIC_METER_PER_HOUR.amount(BigDecimal.valueOf(123.45)))).isTrue();
        assertThat(waterDetail.getLoadLimiterType().equals("LoadLimit")).isTrue();
        assertThat(waterDetail.getPhysicalCapacity().equals(Unit.CUBIC_METER_PER_HOUR.amount(BigDecimal.valueOf(123.45)))).isTrue();
        assertThat(waterDetail.getPressure().equals(Unit.PASCAL.amount(BigDecimal.valueOf(34.5)))).isTrue();
    }

    @Transactional
    @Test
    public void testSaveWithHeatDetails(){

        UsagePoint up = inMemoryPersistentModule.getMeteringService().getServiceCategory(ServiceKind.HEAT)
                .get().newUsagePoint("test").withInstallationTime(inMemoryPersistentModule.getClock().instant().minusSeconds(1000)).create();

        up.newHeatDetailBuilder(inMemoryPersistentModule.getClock().instant())
                .withCollar(true)
                .withValve(true)
                .withBypass(true)
                .withBypassStatus(BypassStatus.OPEN)
                .withPhysicalCapacity(Unit.CUBIC_METER_PER_HOUR.amount(BigDecimal.valueOf(123.45)))
                .withPressure(Unit.PASCAL.amount(BigDecimal.valueOf(34.5)))
                .build();

        up.update();

        UsagePointFilter usagePointFilter = new UsagePointFilter();
        usagePointFilter.setMrid("*");

        HeatDetail waterDetail = (HeatDetail) inMemoryPersistentModule.getMeteringService().getUsagePoints(usagePointFilter).find().get(0).getDetail(inMemoryPersistentModule.getClock().instant()).get();

        //general properties
        assertThat(waterDetail.getCollar().isPresent()).isTrue();
        assertThat(waterDetail.getCollar().get()).isTrue();

        //gas specific properties
        assertThat(waterDetail.getBypass().isPresent()).isTrue();
        assertThat(waterDetail.getBypass().get()).isTrue();
        assertThat(waterDetail.getBypassStatus().equals(BypassStatus.OPEN)).isTrue();
        assertThat(waterDetail.getPhysicalCapacity().equals(Unit.CUBIC_METER_PER_HOUR.amount(BigDecimal.valueOf(123.45)))).isTrue();
        assertThat(waterDetail.getPressure().equals(Unit.PASCAL.amount(BigDecimal.valueOf(34.5)))).isTrue();
    }
}
