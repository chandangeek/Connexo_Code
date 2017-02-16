/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.impl;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointCustomPropertySetValuesManageException;
import com.elster.jupiter.metering.UsagePointPropertySet;
import com.elster.jupiter.metering.UsagePointVersionedPropertySet;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.UsagePointMetrologyConfiguration;
import com.elster.jupiter.metering.impl.cps.CustomPropertySetAttributes;
import com.elster.jupiter.metering.impl.cps.UsagePointTestCustomPropertySet;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.User;
import com.elster.jupiter.util.time.Interval;

import java.security.Principal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UsagePointCustomPropertySetExtensionImplTestIT {
    private static MeteringInMemoryBootstrapModule inMemoryBootstrapModule = MeteringInMemoryBootstrapModule.withAllDefaults();
    private static UsagePointTestCustomPropertySet customPropertySet;
    private static final String USAGE_POINT_NAME = "usagePoint";
    private static final String METROLOGY_CONFIGURATION_MRID = "metrologyConfiguration";

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
        inMemoryBootstrapModule.getMeteringService()
                .findUsagePointByName(USAGE_POINT_NAME)
                .ifPresent(UsagePoint::delete);
        inMemoryBootstrapModule.getMetrologyConfigurationService()
                .findMetrologyConfiguration(METROLOGY_CONFIGURATION_MRID)
                .ifPresent(MetrologyConfiguration::delete);
        getServiceCategory().removeCustomPropertySet(getRegisteredCustomPropertySet());
        inMemoryBootstrapModule.getThreadPrincipalService().set(() -> "Test");
    }

    private UsagePointMetrologyConfiguration createMetrologyConfiguration() {
        return inMemoryBootstrapModule.getMetrologyConfigurationService()
                .newUsagePointMetrologyConfiguration(METROLOGY_CONFIGURATION_MRID, getServiceCategory()).create();
    }

    private UsagePointMetrologyConfiguration getMetrologyConfiguration() {
        return inMemoryBootstrapModule.getMetrologyConfigurationService()
                .findMetrologyConfiguration(METROLOGY_CONFIGURATION_MRID)
                .filter(mc -> mc instanceof UsagePointMetrologyConfiguration)
                .map(UsagePointMetrologyConfiguration.class::cast)
                .orElseThrow(() -> new IllegalStateException("MetrologyConfiguration doesn't exist."));
    }

    private UsagePoint createUsagePoint() {
        return inMemoryBootstrapModule.getMeteringService()
                .getServiceCategory(ServiceKind.ELECTRICITY)
                .get()
                .newUsagePoint(USAGE_POINT_NAME, Instant.EPOCH)
                .create();
    }

    private UsagePoint getUsagePoint() {
        return inMemoryBootstrapModule.getMeteringService()
                .findUsagePointByName(USAGE_POINT_NAME)
                .orElseThrow(() -> new IllegalStateException("UsagePoint doesn't exist."));
    }

    private ServiceCategory getServiceCategory() {
        return inMemoryBootstrapModule.getMeteringService().getServiceCategory(ServiceKind.ELECTRICITY).get();
    }

    private void linkUsagePointToMetrologyConfiguration() {
        getUsagePoint().apply(getMetrologyConfiguration());
    }

    private RegisteredCustomPropertySet getRegisteredCustomPropertySet() {
        return inMemoryBootstrapModule.getCustomPropertySetService()
                .findActiveCustomPropertySet(customPropertySet.getId()).get();
    }

    private void addCustomPropertySetToMetrologyConfiguration() {
        getMetrologyConfiguration().addCustomPropertySet(getRegisteredCustomPropertySet());
    }

    private void addCustomPropertySetToServiceCategory() {
        getServiceCategory().addCustomPropertySet(getRegisteredCustomPropertySet());
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
        when(newCurrentUser.getPrivileges(anyString())).thenReturn(newPrivileges);
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

    private CustomPropertySetValues getPropertySetValues() {
        CustomPropertySetValues values = CustomPropertySetValues.emptyFrom(getUsagePoint().getInstallationTime());
        values.setProperty(CustomPropertySetAttributes.NAME.propertyKey(), "Name");
        values.setProperty(CustomPropertySetAttributes.ENHANCED_SUPPORT.propertyKey(), Boolean.TRUE);
        return values;
    }

    @Test
    @Transactional
    public void testNoPropertiesWhenNoMetrologyConfigurtion() {
        UsagePoint usagePoint = createUsagePoint();
        grantViewPrivilegesForCurrentUser();

        assertThat(usagePoint.forCustomProperties().getPropertySetsOnMetrologyConfiguration()).isEmpty();
    }

    @Test
    @Transactional
    public void testNoPropertiesWhenNoPropertySetsOnMetrologyConfigurtion() {
        createUsagePoint();
        createMetrologyConfiguration();
        linkUsagePointToMetrologyConfiguration();
        grantViewPrivilegesForCurrentUser();

        assertThat(getUsagePoint().forCustomProperties().getPropertySetsOnMetrologyConfiguration()).isEmpty();
    }

    @Test
    @Transactional
    public void testNoPropertiesOnMetrologyConfigurationWhenNoViewPrivilege() {
        createUsagePoint();
        createMetrologyConfiguration();
        linkUsagePointToMetrologyConfiguration();
        addCustomPropertySetToMetrologyConfiguration();

        List<UsagePointPropertySet> propertySets = getUsagePoint().forCustomProperties().getPropertySetsOnMetrologyConfiguration();
        assertThat(propertySets).isEmpty();
    }

    @Test
    @Transactional
    public void testGetPropertySetsOnMetrologyConfigurtion() {
        createUsagePoint();
        createMetrologyConfiguration();
        linkUsagePointToMetrologyConfiguration();
        addCustomPropertySetToMetrologyConfiguration();
        grantViewPrivilegesForCurrentUser();

        List<UsagePointPropertySet> propertySets = getUsagePoint().forCustomProperties().getPropertySetsOnMetrologyConfiguration();
        assertThat(propertySets).hasSize(1);
        assertThat(propertySets.get(0).getId()).isEqualTo(getRegisteredCustomPropertySet().getId());
    }

    @Test
    @Transactional
    public void testNoPropertiesWhenNoPropertySetsOnSeviceCategory() {
        createUsagePoint();
        grantViewPrivilegesForCurrentUser();

        List<UsagePointPropertySet> propertySets = getUsagePoint().forCustomProperties().getPropertySetsOnServiceCategory();
        assertThat(propertySets).isEmpty();
    }

    @Test
    @Transactional
    public void testNoPropertiesOnServiceCategoryWhenNoViewPrivilege() {
        createUsagePoint();
        addCustomPropertySetToServiceCategory();

        List<UsagePointPropertySet> propertySets = getUsagePoint().forCustomProperties().getPropertySetsOnServiceCategory();
        assertThat(propertySets).isEmpty();
    }

    @Test
    @Transactional
    public void testGetPropertySetsOnServiceCategory() {
        createUsagePoint();
        addCustomPropertySetToServiceCategory();
        grantViewPrivilegesForCurrentUser();

        List<UsagePointPropertySet> propertySets = getUsagePoint().forCustomProperties().getPropertySetsOnServiceCategory();
        assertThat(propertySets).hasSize(1);
        assertThat(propertySets.get(0).getId()).isEqualTo(getRegisteredCustomPropertySet().getId());
    }

    @Test
    @Transactional
    public void testGetAllPropertySets() {
        createUsagePoint();
        addCustomPropertySetToServiceCategory();
        grantViewPrivilegesForCurrentUser();

        List<UsagePointPropertySet> propertySets = getUsagePoint().forCustomProperties().getAllPropertySets();
        assertThat(propertySets).hasSize(1);
        assertThat(propertySets.get(0).getId()).isEqualTo(getRegisteredCustomPropertySet().getId());
    }

    @Test
    @Transactional
    public void testGetPropertySetEmptyValueByDefault() {
        createUsagePoint();
        addCustomPropertySetToServiceCategory();
        grantViewPrivilegesForCurrentUser();

        CustomPropertySetValues values = getUsagePoint().forCustomProperties().getAllPropertySets().get(0).getValues();
        assertThat(values).isNull();
    }

    @Test(expected = UsagePointCustomPropertySetValuesManageException.class)
    @Transactional
    public void testSetPropertySetValueNoEditPrivilege() {
        createUsagePoint();
        addCustomPropertySetToServiceCategory();
        grantViewPrivilegesForCurrentUser();

        UsagePointVersionedPropertySet versionedPropertySet = (UsagePointVersionedPropertySet) getUsagePoint().forCustomProperties().getAllPropertySets().get(0);
        versionedPropertySet.setValues(getPropertySetValues()); // assert exception
    }

    @Test
    @Transactional
    public void testSetAndGetPropertySetValue() {
        createUsagePoint();
        addCustomPropertySetToServiceCategory();
        grantViewPrivilegesForCurrentUser();
        grantEditPrivilegesForCurrentUser();

        UsagePointVersionedPropertySet versionedPropertySet = (UsagePointVersionedPropertySet) getUsagePoint().forCustomProperties().getAllPropertySets().get(0);
        versionedPropertySet.setValues(getPropertySetValues());
        CustomPropertySetValues values = versionedPropertySet.getValues();
        assertThat(values).isNotNull();
        assertThat(values.getProperty(CustomPropertySetAttributes.NAME.propertyKey())).isEqualTo("Name");
        assertThat(values.getProperty(CustomPropertySetAttributes.ENHANCED_SUPPORT.propertyKey())).isEqualTo(true);
    }

    @Test
    @Transactional
    public void testUpdatePropertySetValue() {
        createUsagePoint();
        addCustomPropertySetToServiceCategory();
        grantViewPrivilegesForCurrentUser();
        grantEditPrivilegesForCurrentUser();

        UsagePointVersionedPropertySet versionedPropertySet = (UsagePointVersionedPropertySet) getUsagePoint().forCustomProperties().getAllPropertySets().get(0);
        CustomPropertySetValues oldValues = getPropertySetValues();

        versionedPropertySet.setValues(oldValues);

        Instant now = inMemoryBootstrapModule.getClock().instant();
        CustomPropertySetValues values = CustomPropertySetValues.emptyDuring(Interval.of(now, null));
        values.setProperty(CustomPropertySetAttributes.NAME.propertyKey(), "new");
        values.setProperty(CustomPropertySetAttributes.ENHANCED_SUPPORT.propertyKey(), false);
        versionedPropertySet.setVersionValues(now, values);

        values = versionedPropertySet.getValues();
        assertThat(values).isNotNull();
        assertThat(values.getEffectiveRange().lowerEndpoint()).isEqualTo(now);
        assertThat(values.getProperty(CustomPropertySetAttributes.NAME.propertyKey())).isEqualTo("new");
        assertThat(values.getProperty(CustomPropertySetAttributes.ENHANCED_SUPPORT.propertyKey())).isEqualTo(false);
        assertThat(versionedPropertySet.getAllVersionValues()).hasSize(1);
    }

    @Test
    @Transactional
    public void testCanCreateTwoVersions() {
        createUsagePoint();
        addCustomPropertySetToServiceCategory();
        grantViewPrivilegesForCurrentUser();
        grantEditPrivilegesForCurrentUser();

        UsagePointVersionedPropertySet versionedPropertySet = (UsagePointVersionedPropertySet) getUsagePoint().forCustomProperties().getAllPropertySets().get(0);
        Instant now = inMemoryBootstrapModule.getClock().instant();
        CustomPropertySetValues values = CustomPropertySetValues.emptyDuring(Interval.of(null, now));
        values.setProperty(CustomPropertySetAttributes.NAME.propertyKey(), "version 1");
        values.setProperty(CustomPropertySetAttributes.ENHANCED_SUPPORT.propertyKey(), true);
        versionedPropertySet.setVersionValues(values.getEffectiveRange().upperEndpoint(), values);

        values = CustomPropertySetValues.emptyDuring(Interval.of(now, null));
        values.setProperty(CustomPropertySetAttributes.NAME.propertyKey(), "version 2");
        values.setProperty(CustomPropertySetAttributes.ENHANCED_SUPPORT.propertyKey(), false);
        versionedPropertySet.setVersionValues(values.getEffectiveRange().lowerEndpoint(), values);
        List<CustomPropertySetValues> allVersionValues = versionedPropertySet.getAllVersionValues();

        assertThat(allVersionValues).hasSize(2);
        assertThat(allVersionValues.get(0).getProperty(CustomPropertySetAttributes.NAME.propertyKey())).isEqualTo("version 1");
        assertThat(allVersionValues.get(0).getProperty(CustomPropertySetAttributes.ENHANCED_SUPPORT.propertyKey())).isEqualTo(true);
        assertThat(allVersionValues.get(1).getProperty(CustomPropertySetAttributes.NAME.propertyKey())).isEqualTo("version 2");
        assertThat(allVersionValues.get(1).getProperty(CustomPropertySetAttributes.ENHANCED_SUPPORT.propertyKey())).isEqualTo(false);
    }

    @Test
    @Transactional
    public void testCanGetSpecificVersionValues() {
        createUsagePoint();
        addCustomPropertySetToServiceCategory();
        grantViewPrivilegesForCurrentUser();
        grantEditPrivilegesForCurrentUser();

        UsagePointVersionedPropertySet versionedPropertySet = (UsagePointVersionedPropertySet) getUsagePoint().forCustomProperties().getAllPropertySets().get(0);
        Instant hourAfter = inMemoryBootstrapModule.getClock().instant().plus(1, ChronoUnit.HOURS);
        CustomPropertySetValues values = CustomPropertySetValues.emptyDuring(Interval.of(hourAfter, null));
        values.setProperty(CustomPropertySetAttributes.NAME.propertyKey(), "name");
        values.setProperty(CustomPropertySetAttributes.ENHANCED_SUPPORT.propertyKey(), true);
        versionedPropertySet.setVersionValues(values.getEffectiveRange().lowerEndpoint(), values);

        assertThat(versionedPropertySet.getValues()).isNull(); // no active value
        values = versionedPropertySet.getVersionValues(hourAfter);
        assertThat(values).isNotNull();
        assertThat(values.getProperty(CustomPropertySetAttributes.NAME.propertyKey())).isEqualTo("name");
        assertThat(values.getProperty(CustomPropertySetAttributes.ENHANCED_SUPPORT.propertyKey())).isEqualTo(true);
    }
}
