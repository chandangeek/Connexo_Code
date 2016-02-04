package com.elster.insight.usagepoint.data.impl;

import com.elster.insight.usagepoint.config.MetrologyConfiguration;
import com.elster.insight.usagepoint.data.UsagePointCustomPropertySetExtension;
import com.elster.insight.usagepoint.data.impl.cps.CustomPropertySetAttributes;
import com.elster.insight.usagepoint.data.impl.cps.UsagePointTestCustomPropertySet;
import com.elster.insight.usagepoint.data.impl.exceptions.UsagePointCustomPropertySetValuesManageException;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.User;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.security.Principal;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UsagePointCustomPropertySetExtensionImplTest {
    private static UsagePointDataInMemoryBootstrapModule inMemoryBootstrapModule = new UsagePointDataInMemoryBootstrapModule();
    private static UsagePointTestCustomPropertySet customPropertySet;
    private static String USAGE_POINT_MRID = "usagePoint";
    private static String METROLOGY_CONFIGURATION_MRID = "metrologyConfiguration";

    @Rule
    public TransactionalRule transactionalRule = new TransactionalRule(inMemoryBootstrapModule.getTransactionService());

    @BeforeClass
    public static void beforeClass() {
        inMemoryBootstrapModule.activate();
        customPropertySet = new UsagePointTestCustomPropertySet(inMemoryBootstrapModule.getPropertySpecService());
        inMemoryBootstrapModule.getCustomPropertySetService().addCustomPropertySet(customPropertySet);
    }

    @AfterClass
    public static void afterClass() {
        inMemoryBootstrapModule.deactivate();
    }

    @Test
    public void after() {
        try (TransactionContext context = inMemoryBootstrapModule.getTransactionService().getContext()) {
            getTestUsagePointInstance().delete();
            getTestMetrologyConfigurationInstance().delete();
            context.commit();
        }
    }

    private MetrologyConfiguration createTestMetrologyConfigurationInstance() {
        return inMemoryBootstrapModule.getUsagePointConfigurationService()
                .newMetrologyConfiguration(METROLOGY_CONFIGURATION_MRID);
    }

    private MetrologyConfiguration getTestMetrologyConfigurationInstance() {
        return inMemoryBootstrapModule.getUsagePointConfigurationService()
                .findMetrologyConfiguration(METROLOGY_CONFIGURATION_MRID).orElseGet(this::createTestMetrologyConfigurationInstance);
    }

    private UsagePoint createTestUsagePointInstance() {
        return inMemoryBootstrapModule.getMeteringService()
                .getServiceCategory(ServiceKind.ELECTRICITY)
                .get()
                .newUsagePoint(USAGE_POINT_MRID)
                .create();
    }

    private UsagePoint getTestUsagePointInstance() {
        return inMemoryBootstrapModule.getMeteringService()
                .findUsagePoint(USAGE_POINT_MRID).orElseGet(this::createTestUsagePointInstance);
    }

    private void linkTestUsagePointToTestMetrologyConfiguration() {
        inMemoryBootstrapModule.getUsagePointConfigurationService().link(getTestUsagePointInstance(),
                getTestMetrologyConfigurationInstance());
    }

    private RegisteredCustomPropertySet getRegisteredCustomPropertySet() {
        return inMemoryBootstrapModule.getCustomPropertySetService()
                .findActiveCustomPropertySet(customPropertySet.getId()).get();
    }

    private void addCustomPropertySetToTestMetrologyConfiguration() {
        getTestMetrologyConfigurationInstance().addCustomPropertySet(getRegisteredCustomPropertySet());
    }

    private Set<Privilege> getCurrentPrivileges() {
        Principal principal = inMemoryBootstrapModule.getThreadPrincipalService().getPrincipal();
        User newCurrentUser = mock(User.class);
        when(newCurrentUser.getName()).thenReturn("Test");
        Set<Privilege> newPrivileges = new HashSet<>();
        if (principal instanceof User) {
            newPrivileges.addAll(((User) principal).getPrivileges());
        }
        when(newCurrentUser.getPrivileges()).thenReturn(newPrivileges);
        inMemoryBootstrapModule.getThreadPrincipalService().set(newCurrentUser);
        return newPrivileges;
    }

    private void grantEditPrivilegesForCurrentUser() {
        Privilege editPrivilege = mock(Privilege.class);
        when(editPrivilege.getName()).thenReturn(EditPrivilege.LEVEL_1.getPrivilege());
        getCurrentPrivileges().add(editPrivilege);
    }

    private void grantViewPrivilegesForCurrentUser() {
        Privilege editPrivilege = mock(Privilege.class);
        when(editPrivilege.getName()).thenReturn(ViewPrivilege.LEVEL_1.getPrivilege());
        getCurrentPrivileges().add(editPrivilege);
    }

    private UsagePointCustomPropertySetExtension storeDefaultCustomPropertySetValues() {
        UsagePointCustomPropertySetExtension valuesExtension = inMemoryBootstrapModule.getUsagePointDataService()
                .findUsagePointExtensionByMrid(USAGE_POINT_MRID).get();
        CustomPropertySetValues values = CustomPropertySetValues.empty();
        values.setProperty(CustomPropertySetAttributes.NAME.propertyKey(), "Name");
        values.setProperty(CustomPropertySetAttributes.ENHANCED_SUPPORT.propertyKey(), Boolean.TRUE);
        valuesExtension.setMetrologyCustomPropertySetValue(customPropertySet, values);
        return valuesExtension;
    }

    @Test(expected = UsagePointCustomPropertySetValuesManageException.class)
    @Transactional
    public void testSetMetrologyValuesButThereIsNoCustomPropertySetsOnMetrologyConfiguration() {
        createTestMetrologyConfigurationInstance();
        createTestUsagePointInstance();
        linkTestUsagePointToTestMetrologyConfiguration();
        grantViewPrivilegesForCurrentUser();
        grantEditPrivilegesForCurrentUser();

        // Store values
        storeDefaultCustomPropertySetValues();

        // assert exception
    }

    @Test(expected = UsagePointCustomPropertySetValuesManageException.class)
    @Transactional
    public void testSetMetrologyValuesButThereIsNoLinkedMetrologyConfiguration() {
        createTestUsagePointInstance();
        createTestMetrologyConfigurationInstance();
        addCustomPropertySetToTestMetrologyConfiguration();
        grantViewPrivilegesForCurrentUser();
        grantEditPrivilegesForCurrentUser();

        // Store values
        storeDefaultCustomPropertySetValues();

        // assert exception
    }

    @Test(expected = UsagePointCustomPropertySetValuesManageException.class)
    @Transactional
    public void testSetMetrologyValuesButCurrentUserHasNoEditPrivileges() {
        createTestUsagePointInstance();
        createTestMetrologyConfigurationInstance();
        addCustomPropertySetToTestMetrologyConfiguration();
        grantViewPrivilegesForCurrentUser();

        // Store values
        storeDefaultCustomPropertySetValues();

        // assert exception
    }

    @Test
    @Transactional
    public void testReadMetrologyValuesButThereIsNoLinkedMetrologyConfiguration() {
        createTestUsagePointInstance();
        createTestMetrologyConfigurationInstance();
        addCustomPropertySetToTestMetrologyConfiguration();
        grantViewPrivilegesForCurrentUser();

        UsagePointCustomPropertySetExtension valuesExtension = inMemoryBootstrapModule.getUsagePointDataService().findUsagePointExtensionByMrid(USAGE_POINT_MRID).get();
        Map<RegisteredCustomPropertySet, CustomPropertySetValues> customPropertySetValues = valuesExtension.getMetrologyCustomPropertySetValues();

        assertThat(customPropertySetValues).isEmpty(); // no values and no exception
    }

    @Test
    @Transactional
    public void testReadMetrologyValuesButThereIsNoCustomPropertySetsOnMetrologyConfiguration() {
        createTestUsagePointInstance();
        createTestMetrologyConfigurationInstance();
        linkTestUsagePointToTestMetrologyConfiguration();
        grantViewPrivilegesForCurrentUser();

        UsagePointCustomPropertySetExtension valuesExtension = inMemoryBootstrapModule.getUsagePointDataService().findUsagePointExtensionByMrid(USAGE_POINT_MRID).get();
        Map<RegisteredCustomPropertySet, CustomPropertySetValues> customPropertySetValues = valuesExtension.getMetrologyCustomPropertySetValues();

        assertThat(customPropertySetValues).isEmpty(); // no values and no exception
    }

    @Test
    @Transactional
    public void testSetAndReadMetrologyValuesForSimpleCustomPropertySet() {
        createTestMetrologyConfigurationInstance();
        addCustomPropertySetToTestMetrologyConfiguration();
        createTestUsagePointInstance();
        linkTestUsagePointToTestMetrologyConfiguration();
        grantViewPrivilegesForCurrentUser();
        grantEditPrivilegesForCurrentUser();

        // Store values
        UsagePointCustomPropertySetExtension valuesExtension = storeDefaultCustomPropertySetValues();

        // Read values
        Map<RegisteredCustomPropertySet, CustomPropertySetValues> storedValues = valuesExtension.getMetrologyCustomPropertySetValues();

        assertThat(storedValues.size()).isEqualTo(1);
        assertThat(storedValues.keySet().iterator().next().getCustomPropertySet().getId()).isEqualTo(customPropertySet.getId());
        CustomPropertySetValues cpsValues = storedValues.values().iterator().next();
        assertThat(cpsValues.getProperty(CustomPropertySetAttributes.NAME.propertyKey())).isEqualTo("Name");
        assertThat(cpsValues.getProperty(CustomPropertySetAttributes.ENHANCED_SUPPORT.propertyKey())).isEqualTo(Boolean.TRUE);
    }
}
