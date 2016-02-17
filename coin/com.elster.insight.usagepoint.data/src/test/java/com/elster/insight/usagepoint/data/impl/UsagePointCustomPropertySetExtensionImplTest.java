package com.elster.insight.usagepoint.data.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.User;
import com.elster.insight.usagepoint.data.UsagePointCustomPropertySetExtension;
import com.elster.insight.usagepoint.data.impl.cps.CustomPropertySetAttributes;
import com.elster.insight.usagepoint.data.impl.cps.UsagePointTestCustomPropertySet;
import com.elster.insight.usagepoint.data.impl.exceptions.UsagePointCustomPropertySetValuesManageException;

import java.security.Principal;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

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
        usagePoint.delete();
        MetrologyConfiguration metrologyConfiguration = getTestMetrologyConfigurationInstance();
        metrologyConfiguration.delete();
        getTestServiceCategory().removeCustomPropertySet(getRegisteredCustomPropertySet());
        inMemoryBootstrapModule.getThreadPrincipalService().set(() -> "Test");
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

    private ServiceCategory getTestServiceCategory() {
        return inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY).get();
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

    private void storeCustomPropertySetValues(BiConsumer<CustomPropertySet<?, ?>, CustomPropertySetValues> storer) {
        CustomPropertySetValues values = CustomPropertySetValues.empty();
        values.setProperty(CustomPropertySetAttributes.NAME.propertyKey(), "Name");
        values.setProperty(CustomPropertySetAttributes.ENHANCED_SUPPORT.propertyKey(), Boolean.TRUE);
        storer.accept(customPropertySet, values);
    }

    private UsagePointCustomPropertySetExtension storeMetrologyCustomPropertySetValues() {
        UsagePointCustomPropertySetExtension valuesExtension = inMemoryBootstrapModule.getUsagePointDataService()
                .findUsagePointExtensionByMrid(USAGE_POINT_MRID).get();
        storeCustomPropertySetValues(valuesExtension::setMetrologyConfigurationCustomPropertySetValue);
        return valuesExtension;
    }

    private UsagePointCustomPropertySetExtension storeServiceCategoryCustomPropertySetValues() {
        UsagePointCustomPropertySetExtension valuesExtension = inMemoryBootstrapModule.getUsagePointDataService()
                .findUsagePointExtensionByMrid(USAGE_POINT_MRID).get();
        storeCustomPropertySetValues(valuesExtension::setServiceCategoryCustomPropertySetValue);
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
        storeMetrologyCustomPropertySetValues();

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
        storeMetrologyCustomPropertySetValues();

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
        storeMetrologyCustomPropertySetValues();

        // assert exception
    }

    @Test
    @Transactional
    public void testGetMetrologyValuesButThereIsNoLinkedMetrologyConfiguration() {
        createTestUsagePointInstance();
        createTestMetrologyConfigurationInstance();
        addCustomPropertySetToTestMetrologyConfiguration();
        grantViewPrivilegesForCurrentUser();

        UsagePointCustomPropertySetExtension valuesExtension = inMemoryBootstrapModule.getUsagePointDataService().findUsagePointExtensionByMrid(USAGE_POINT_MRID).get();
        Map<RegisteredCustomPropertySet, CustomPropertySetValues> customPropertySetValues = valuesExtension.getMetrologyConfigurationCustomPropertySetValues();

        assertThat(customPropertySetValues).isEmpty(); // no values and no exception
    }

    @Test
    @Transactional
    public void testGetMetrologyValuesButThereIsNoCustomPropertySetsOnMetrologyConfiguration() {
        createTestUsagePointInstance();
        createTestMetrologyConfigurationInstance();
        linkTestUsagePointToTestMetrologyConfiguration();
        grantViewPrivilegesForCurrentUser();

        UsagePointCustomPropertySetExtension valuesExtension = inMemoryBootstrapModule.getUsagePointDataService().findUsagePointExtensionByMrid(USAGE_POINT_MRID).get();
        Map<RegisteredCustomPropertySet, CustomPropertySetValues> customPropertySetValues = valuesExtension.getMetrologyConfigurationCustomPropertySetValues();

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
        UsagePointCustomPropertySetExtension valuesExtension = storeMetrologyCustomPropertySetValues();

        // Read values
        Map<RegisteredCustomPropertySet, CustomPropertySetValues> storedValues = valuesExtension.getMetrologyConfigurationCustomPropertySetValues();

        assertThat(storedValues.size()).isEqualTo(1);
        assertThat(storedValues.keySet().iterator().next().getCustomPropertySet().getId()).isEqualTo(customPropertySet.getId());
        CustomPropertySetValues cpsValues = storedValues.values().iterator().next();
        assertThat(cpsValues.getProperty(CustomPropertySetAttributes.NAME.propertyKey())).isEqualTo("Name");
        assertThat(cpsValues.getProperty(CustomPropertySetAttributes.ENHANCED_SUPPORT.propertyKey())).isEqualTo(Boolean.TRUE);
    }

    @Test
    @Transactional
    public void testGetServiceCategoryCustomPropertySetsButNoLinkedSet() {
        // no linked CPS to service category -> empty list
        getTestServiceCategory();
        getTestUsagePointInstance();
        grantViewPrivilegesForCurrentUser();

        UsagePointCustomPropertySetExtension valuesExtension = inMemoryBootstrapModule.getUsagePointDataService().findUsagePointExtensionByMrid(USAGE_POINT_MRID).get();
        assertThat(valuesExtension.getServiceCategoryPropertySets()).isEmpty();
    }

    @Test
    @Transactional
    public void testGetServiceCategoryCustomPropertySets() {
        // one linked CPS to service category -> list with one element
        getTestServiceCategory();
        getTestUsagePointInstance();
        addCustomPropertySetToTestServiceCategory();
        grantViewPrivilegesForCurrentUser();

        UsagePointCustomPropertySetExtension valuesExtension = inMemoryBootstrapModule.getUsagePointDataService().findUsagePointExtensionByMrid(USAGE_POINT_MRID).get();
        assertThat(valuesExtension.getServiceCategoryPropertySets()).hasSize(1);
        assertThat(valuesExtension.getServiceCategoryPropertySets().get(0).getCustomPropertySet().getId()).isEqualTo(customPropertySet.getId());
    }

    @Test
    @Transactional
    public void testGetServiceCategoryCustomPropertySetsNoViewPrivilege() {
        // one linked CPS to service category but without view privilege -> empty list
        getTestServiceCategory();
        getTestUsagePointInstance();
        addCustomPropertySetToTestServiceCategory();

        UsagePointCustomPropertySetExtension valuesExtension = inMemoryBootstrapModule.getUsagePointDataService().findUsagePointExtensionByMrid(USAGE_POINT_MRID).get();
        assertThat(valuesExtension.getServiceCategoryPropertySets()).hasSize(1);
        assertThat(valuesExtension.getServiceCategoryCustomPropertySetValues()).hasSize(0);
    }

    @Test
    @Transactional
    public void testGetServiceCategoryValuesButNoLinkedSet() {
        // no linked CPS to service category -> empty map
        getTestServiceCategory();
        getTestUsagePointInstance();

        UsagePointCustomPropertySetExtension valuesExtension = inMemoryBootstrapModule.getUsagePointDataService().findUsagePointExtensionByMrid(USAGE_POINT_MRID).get();
        assertThat(valuesExtension.getServiceCategoryCustomPropertySetValues()).hasSize(0);
    }

    @Test
    @Transactional
    public void testGetServiceCategoryValuesAndNoSavedValues() {
        // one linked CPS to service category without values -> map
        getTestServiceCategory();
        getTestUsagePointInstance();
        addCustomPropertySetToTestServiceCategory();
        grantViewPrivilegesForCurrentUser();

        UsagePointCustomPropertySetExtension valuesExtension = inMemoryBootstrapModule.getUsagePointDataService().findUsagePointExtensionByMrid(USAGE_POINT_MRID).get();
        assertThat(valuesExtension.getServiceCategoryCustomPropertySetValues()).hasSize(1);
        CustomPropertySetValues values = valuesExtension.getServiceCategoryCustomPropertySetValues().get(getRegisteredCustomPropertySet());
        assertThat(values).isNotNull();
        assertThat(values.getProperty(CustomPropertySetAttributes.NAME.propertyKey())).isNull();
        assertThat(values.getProperty(CustomPropertySetAttributes.ENHANCED_SUPPORT.propertyKey())).isNull();
    }

    @Test(expected = UsagePointCustomPropertySetValuesManageException.class)
    @Transactional
    public void testSetServiceCategoryValuesButNoLinkedSet() {
        getTestServiceCategory();
        getTestUsagePointInstance();
        grantViewPrivilegesForCurrentUser();
        grantEditPrivilegesForCurrentUser();

        storeServiceCategoryCustomPropertySetValues();

        // assert exception
    }

    @Test(expected = UsagePointCustomPropertySetValuesManageException.class)
    @Transactional
    public void testSetServiceCategoryValuesNoEditPrivilege() {
        getTestServiceCategory();
        getTestUsagePointInstance();
        addCustomPropertySetToTestServiceCategory();
        grantViewPrivilegesForCurrentUser();

        storeServiceCategoryCustomPropertySetValues();

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

        storeServiceCategoryCustomPropertySetValues();
        UsagePointCustomPropertySetExtension valuesExtension = inMemoryBootstrapModule.getUsagePointDataService().findUsagePointExtensionByMrid(USAGE_POINT_MRID).get();
        assertThat(valuesExtension.getServiceCategoryCustomPropertySetValues()).hasSize(1);
        CustomPropertySetValues values = valuesExtension.getServiceCategoryCustomPropertySetValues().get(getRegisteredCustomPropertySet());
        assertThat(values).isNotNull();
        assertThat(values.getProperty(CustomPropertySetAttributes.NAME.propertyKey())).isNotNull();
        assertThat(values.getProperty(CustomPropertySetAttributes.ENHANCED_SUPPORT.propertyKey())).isNotNull();
    }

}
