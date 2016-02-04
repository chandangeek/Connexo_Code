package com.elster.insight.usagepoint.data.impl;

import com.elster.insight.usagepoint.data.UsagePointPropertySetValuesExtension;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.metering.ServiceKind;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class UsagePointDataServiceImplTest {
    private static UsagePointDataInMemoryBootstrapModule inMemoryBootstrapModule = new UsagePointDataInMemoryBootstrapModule();

    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(inMemoryBootstrapModule.getTransactionService());

    @BeforeClass
    public static void beforeClass(){
        inMemoryBootstrapModule.activate();
    }

    @AfterClass
    public static void afterClass(){
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    @Transactional
    public void testGetWrappedUsagePoint(){
        String usagePointMrid = "Wrapped";
        inMemoryBootstrapModule.getMeteringService()
                .getServiceCategory(ServiceKind.ELECTRICITY)
                .get()
                .newUsagePoint(usagePointMrid)
                .create();
        Optional<UsagePointPropertySetValuesExtension> valuesExtension = inMemoryBootstrapModule.getUsagePointDataService()
                .findUsagePointExtensionByMrid(usagePointMrid);
        assertThat(valuesExtension.isPresent()).isTrue();
        assertThat(valuesExtension.get().getUsagePoint().getMRID()).isEqualTo(usagePointMrid);
    }

}
