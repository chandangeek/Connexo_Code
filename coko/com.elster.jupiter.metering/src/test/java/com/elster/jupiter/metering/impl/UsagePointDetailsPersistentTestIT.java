package com.elster.jupiter.metering.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.metering.BypassStatus;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointFilter;
import com.elster.jupiter.metering.WaterDetail;

import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.Test;

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

        UsagePoint up = inMemoryPersistentModule.getMeteringService().getServiceCategory(ServiceKind.WATER)
                .get().newUsagePoint("test").withInstallationTime(inMemoryPersistentModule.getClock().instant().minusSeconds(1000)).create();

        up.newWaterDetailBuilder(inMemoryPersistentModule.getClock().instant())
                .withCollar(true).withCapped(true).withBypassStatus(BypassStatus.CLOSED).build();

        up.update();

        UsagePointFilter usagePointFilter = new UsagePointFilter();
            usagePointFilter.setMrid("*");

        WaterDetail detail = (WaterDetail) inMemoryPersistentModule.getMeteringService().getUsagePoints(usagePointFilter).find().get(0).getDetail(inMemoryPersistentModule.getClock().instant()).get();

        System.out.println(detail.getCapped());
        System.out.println(detail.getClamped());
        System.out.println(detail.getCollar());
        System.out.println(detail.getBypassStatus());
    }
}
