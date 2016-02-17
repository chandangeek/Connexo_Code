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
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.User;
import org.junit.After;
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

    @After
    public void after() {
        UsagePoint usagePoint = getTestUsagePointInstance();
        MetrologyConfiguration metrologyConfiguration = getTestMetrologyConfigurationInstance();
        inMemoryBootstrapModule.getUsagePointConfigurationService().unlink(usagePoint, metrologyConfiguration);
        metrologyConfiguration.delete();
        usagePoint.delete();
        getTestServiceCategory().removeCustomPropertySet(getRegisteredCustomPropertySet());
        inMemoryBootstrapModule.getThreadPrincipalService().set(() -> "Test");
    }

    private MetrologyConfiguration createTestMetrologyConfigurationInstance() {
        return inMemoryBootstrapModule.getUsagePointConfigurationService()
                .newMetrologyConfiguration(METROLOGY_CONFIGURATION_MRID);
    }

    private MetrologyConfiguration getTestMetrologyConfigurationInstance() {
        return inMemoryBootstrapModule.getUsagePointConfigurationService()
                .findMetrologyConfiguration(METROLOGY_CONFIGURATION_MRID)
                .orElseGet(this::createTestMetrologyConfigurationInstance);
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
                .findUsagePoint(USAGE_POINT_MRID)
                .orElseGet(this::createTestUsagePointInstance);
    }

    private ServiceCategory getTestServiceCategory() {
        return inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY).get();
    }

    private void linkTestUsagePointToTestMetrologyConfiguration() {
        inMemoryBootstrapModule.getUsagePointConfigurationService()
                .link(getTestUsagePointInstance(), getTestMetrologyConfigurationInstance());
    }

    private RegisteredCustomPropertySet getRegisteredCustomPropertySet() {
        return inMemoryBootstrapModule.getCustomPropertySetService()
                .findActiveCustomPropertySet(customPropertySet.getId()).get();
    }

    private void addCustomPropertySetToTestMetrologyConfiguration() {
        getTestMetrologyConfigurationInstance().addCustomPropertySet(getRegisteredCustomPropertySet());
    }

    private void addCustomPropertySetToTestServiceCategory() {
        getTestServiceCategory().addCustomPropertySet(getRegisteredCustomPropertySet());
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

    private UsagePointCustomPropertySetExtension setCustomPropertySetValues() {
        UsagePointCustomPropertySetExtension usagePointExtension = inMemoryBootstrapModule.getUsagePointDataService()
                .findUsagePointExtensionByMrid(USAGE_POINT_MRID).get();
        CustomPropertySetValues values = CustomPropertySetValues.empty();
        values.setProperty(CustomPropertySetAttributes.NAME.propertyKey(), "Name");
        values.setProperty(CustomPropertySetAttributes.ENHANCED_SUPPORT.propertyKey(), Boolean.TRUE);
        usagePointExtension.setCustomPropertySetValue(customPropertySet, values);
        return usagePointExtension;
    }

    @Test(expected = UsagePointCustomPropertySetValuesManageException.class)
    @Transactional
    public void testSetCustomPropertySetValuesButThereIsNoLinkedCustomPropertySetOnMetrologyConfiguration() {
        createTestMetrologyConfigurationInstance();
        createTestUsagePointInstance();
        linkTestUsagePointToTestMetrologyConfiguration();
        grantViewPrivilegesForCurrentUser();
        grantEditPrivilegesForCurrentUser();

        // Store values
        setCustomPropertySetValues();

        // assert exception
    }

    @Test(expected = UsagePointCustomPropertySetValuesManageException.class)
    @Transactional
    public void testSetCustomPropertySetValuesButThereIsNoLinkedMetrologyConfiguration() {
        createTestUsagePointInstance();
        createTestMetrologyConfigurationInstance();
        addCustomPropertySetToTestMetrologyConfiguration();
        grantViewPrivilegesForCurrentUser();
        grantEditPrivilegesForCurrentUser();

        // Store values
        setCustomPropertySetValues();

        // assert exception
    }

    @Test(expected = UsagePointCustomPropertySetValuesManageException.class)
    @Transactional
    public void testSetCustomPropertySetValuesButUserHasNoEditPrivileges() {
        createTestUsagePointInstance();
        createTestMetrologyConfigurationInstance();
        addCustomPropertySetToTestMetrologyConfiguration();
        grantViewPrivilegesForCurrentUser();

        // Store values
        setCustomPropertySetValues();

        // assert exception
    }

    @Test
    @Transactional
    public void testGetCustomPropertySetValuesButThereIsNoLinkedMetrologyConfiguration() {
        createTestUsagePointInstance();
        createTestMetrologyConfigurationInstance();
        addCustomPropertySetToTestMetrologyConfiguration();
        grantViewPrivilegesForCurrentUser();

        UsagePointCustomPropertySetExtension usagePointExtension = inMemoryBootstrapModule.getUsagePointDataService()
                .findUsagePointExtensionByMrid(USAGE_POINT_MRID).get();
        Map<RegisteredCustomPropertySet, CustomPropertySetValues> customPropertySetValues = usagePointExtension.getCustomPropertySetValues(
                usagePointExtension.getCustomPropertySetsOnMetrologyConfiguration());

        assertThat(customPropertySetValues).isEmpty(); // no values and no exception
    }

    @Test
    @Transactional
    public void testGetCustomPropertySetValuesButThereIsNoCustomPropertySetsOnMetrologyConfiguration() {
        createTestUsagePointInstance();
        createTestMetrologyConfigurationInstance();
        linkTestUsagePointToTestMetrologyConfiguration();
        grantViewPrivilegesForCurrentUser();

        UsagePointCustomPropertySetExtension usagePointExtension = inMemoryBootstrapModule.getUsagePointDataService()
                .findUsagePointExtensionByMrid(USAGE_POINT_MRID).get();
        Map<RegisteredCustomPropertySet, CustomPropertySetValues> customPropertySetValues = usagePointExtension.getCustomPropertySetValues(
                usagePointExtension.getCustomPropertySetsOnMetrologyConfiguration());

        assertThat(customPropertySetValues).isEmpty(); // no values and no exception
    }

    @Test
    @Transactional
    public void testGetAndSetCustomPropertySetValuesOnMetrologyConfiguration() {
        createTestMetrologyConfigurationInstance();
        addCustomPropertySetToTestMetrologyConfiguration();
        createTestUsagePointInstance();
        linkTestUsagePointToTestMetrologyConfiguration();
        grantViewPrivilegesForCurrentUser();
        grantEditPrivilegesForCurrentUser();

        // Store values
        UsagePointCustomPropertySetExtension usagePointExtension = setCustomPropertySetValues();

        // Read values
        Map<RegisteredCustomPropertySet, CustomPropertySetValues> storedValues = usagePointExtension.getCustomPropertySetValues(
                usagePointExtension.getCustomPropertySetsOnMetrologyConfiguration());

        assertThat(storedValues.size()).isEqualTo(1);
        assertThat(storedValues.keySet().iterator().next().getCustomPropertySet().getId()).isEqualTo(customPropertySet.getId());
        CustomPropertySetValues cpsValues = storedValues.values().iterator().next();
        assertThat(cpsValues.getProperty(CustomPropertySetAttributes.NAME.propertyKey())).isEqualTo("Name");
        assertThat(cpsValues.getProperty(CustomPropertySetAttributes.ENHANCED_SUPPORT.propertyKey())).isEqualTo(Boolean.TRUE);
    }

    @Test
    @Transactional
    public void testGetCustomPropertySetsOnServiceCategoryButThereIsNoLinkedCustomPropertySetOnServiceCategory() {
        // no linked CPS to service category -> empty list
        getTestServiceCategory();
        getTestUsagePointInstance();
        grantViewPrivilegesForCurrentUser();

        UsagePointCustomPropertySetExtension usagePointExtension = inMemoryBootstrapModule.getUsagePointDataService()
                .findUsagePointExtensionByMrid(USAGE_POINT_MRID).get();
        assertThat(usagePointExtension.getCustomPropertySetsOnServiceCategory()).isEmpty();
    }

    @Test
    @Transactional
    public void testGetCustomPropertySetsOnServiceCategory() {
        // one linked CPS to service category -> list with one element
        getTestServiceCategory();
        getTestUsagePointInstance();
        addCustomPropertySetToTestServiceCategory();
        grantViewPrivilegesForCurrentUser();

        UsagePointCustomPropertySetExtension usagePointExtension = inMemoryBootstrapModule.getUsagePointDataService()
                .findUsagePointExtensionByMrid(USAGE_POINT_MRID).get();
        assertThat(usagePointExtension.getCustomPropertySetsOnServiceCategory()).hasSize(1);
        assertThat(usagePointExtension.getCustomPropertySetsOnServiceCategory().get(0).getCustomPropertySet().getId())
                .isEqualTo(customPropertySet.getId());
    }

    @Test
    @Transactional
    public void tesGetCustomPropertySetValuesButUserHasNoViewPrivilege() {
        // one linked CPS to service category but without view privilege -> empty list
        getTestServiceCategory();
        getTestUsagePointInstance();
        addCustomPropertySetToTestServiceCategory();

        UsagePointCustomPropertySetExtension usagePointExtension = inMemoryBootstrapModule.getUsagePointDataService()
                .findUsagePointExtensionByMrid(USAGE_POINT_MRID).get();
        assertThat(usagePointExtension.getCustomPropertySetsOnServiceCategory()).hasSize(1);
        assertThat(usagePointExtension.getCustomPropertySetValues(usagePointExtension.getCustomPropertySetsOnServiceCategory())).hasSize(0);
    }

    @Test
    @Transactional
    public void tesGetCustomPropertySetValuesButThereIsNoCustomPropertySetsOnServiceCategory() {
        // no linked CPS to service category -> empty map
        getTestServiceCategory();
        getTestUsagePointInstance();

        UsagePointCustomPropertySetExtension usagePointExtension = inMemoryBootstrapModule.getUsagePointDataService()
                .findUsagePointExtensionByMrid(USAGE_POINT_MRID).get();
        assertThat(usagePointExtension.getCustomPropertySetValues(usagePointExtension.getCustomPropertySetsOnServiceCategory())).hasSize(0);
    }

    @Test
    @Transactional
    public void tesGetCustomPropertySetValuesAndNoSavedValuesForThatCustomPropertySet() {
        // one linked CPS to service category without values -> map
        getTestServiceCategory();
        getTestUsagePointInstance();
        addCustomPropertySetToTestServiceCategory();
        grantViewPrivilegesForCurrentUser();

        UsagePointCustomPropertySetExtension usagePointExtension = inMemoryBootstrapModule.getUsagePointDataService()
                .findUsagePointExtensionByMrid(USAGE_POINT_MRID).get();
        Map<RegisteredCustomPropertySet, CustomPropertySetValues> customPropertySetValues = usagePointExtension
                .getCustomPropertySetValues(usagePointExtension.getCustomPropertySetsOnServiceCategory());

        assertThat(customPropertySetValues).hasSize(1);
        CustomPropertySetValues values = customPropertySetValues.get(getRegisteredCustomPropertySet());
        assertThat(values).isNull();
    }

    @Test(expected = UsagePointCustomPropertySetValuesManageException.class)
    @Transactional
    public void tesSetCustomPropertySetValuesButThereIsNoCustomPropertySetsOnServiceCategory() {
        getTestServiceCategory();
        getTestUsagePointInstance();
        grantViewPrivilegesForCurrentUser();
        grantEditPrivilegesForCurrentUser();

        setCustomPropertySetValues();

        // assert exception
    }

    @Test
    @Transactional
    public void testGetAndSetServiceCategoryValues() {
        // one linked CPS to service category values -> map with values
        getTestServiceCategory();
        getTestUsagePointInstance();
        addCustomPropertySetToTestServiceCategory();
        grantViewPrivilegesForCurrentUser();
        grantEditPrivilegesForCurrentUser();

        setCustomPropertySetValues();

        UsagePointCustomPropertySetExtension usagePointExtension = inMemoryBootstrapModule.getUsagePointDataService()
                .findUsagePointExtensionByMrid(USAGE_POINT_MRID).get();
        Map<RegisteredCustomPropertySet, CustomPropertySetValues> customPropertySetValues = usagePointExtension
                .getCustomPropertySetValues(usagePointExtension.getCustomPropertySetsOnServiceCategory());

        assertThat(customPropertySetValues).hasSize(1);
        CustomPropertySetValues values = customPropertySetValues.get(getRegisteredCustomPropertySet());
        assertThat(values).isNotNull();
        assertThat(values.getProperty(CustomPropertySetAttributes.NAME.propertyKey())).isNotNull();
        assertThat(values.getProperty(CustomPropertySetAttributes.ENHANCED_SUPPORT.propertyKey())).isNotNull();
    }
}
