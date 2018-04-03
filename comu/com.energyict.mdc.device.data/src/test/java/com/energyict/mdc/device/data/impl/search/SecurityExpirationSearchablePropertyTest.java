/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyGroup;

import java.util.List;
import java.util.Optional;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class SecurityExpirationSearchablePropertyTest {
    private static InMemoryPersistence inMemoryPersistence = new InMemoryPersistence();

    @Mock
    private DeviceSearchDomain domain;
    private SecuritySearchablePropertyGroup securitySearchablePropertyGroup;

    @BeforeClass
    public static void initialize() {
        inMemoryPersistence.activate();
    }

    @AfterClass
    public static void uninstall(){
        inMemoryPersistence.deactivate();
    }

    @Before
    public void initializeMocks() {
        this.securitySearchablePropertyGroup = new SecuritySearchablePropertyGroup(inMemoryPersistence.getThesaurus());
    }

    @Test
    public void testGetDomain() {
        SecurityExpirationSearchableProperty property = this.getTestInstance();

        // Business method
        SearchDomain domain = property.getDomain();

        // Asserts
        assertThat(domain).isEqualTo(this.domain);
    }

    @Test
    public void testGroup() {
        SecurityExpirationSearchableProperty property = this.getTestInstance();

        // Business method
        Optional<SearchablePropertyGroup> group = property.getGroup();

        // Asserts
        assertThat(group).isPresent();
        assertThat(group.get().getId()).isEqualTo(SecuritySearchablePropertyGroup.GROUP_NAME);
    }

    @Test
    public void testVisibility() {
        SecurityExpirationSearchableProperty property = this.getTestInstance();

        // Business method
        SearchableProperty.Visibility visibility = property.getVisibility();

        // Asserts
        assertThat(visibility).isEqualTo(SearchableProperty.Visibility.REMOVABLE);
    }

    @Test
    public void testSelectionMode() {
        SecurityExpirationSearchableProperty property = this.getTestInstance();

        // Business method
        SearchableProperty.SelectionMode selectionMode = property.getSelectionMode();

        // Asserts
        assertThat(selectionMode).isEqualTo(SearchableProperty.SelectionMode.SINGLE);
    }

    @Test
    public void testSpecification() {
        SecurityExpirationSearchableProperty property = this.getTestInstance();

        // Business method
        PropertySpec specification = property.getSpecification();

        // Asserts
        assertThat(specification).isNotNull();
        assertThat(specification.isReference()).isFalse();
        assertThat(specification.getValueFactory().getValueType()).isEqualTo(com.elster.jupiter.properties.Expiration.class);
    }

    @Test
    public void testPossibleValues() {
        SecurityExpirationSearchableProperty property = this.getTestInstance();

        // Business method
        PropertySpec specification = property.getSpecification();

        // Asserts
        assertThat(specification.getPossibleValues()).isNull();
    }

    @Test
    public void testPropertyHasNoConstraints() {
        SecurityExpirationSearchableProperty property = this.getTestInstance();

        // Business method
        List<SearchableProperty> constraints = property.getConstraints();

        // Asserts
        assertThat(constraints).isEmpty();
    }

    private SecurityExpirationSearchableProperty getTestInstance() {
        return new SecurityExpirationSearchableProperty(inMemoryPersistence.getDataModel(), inMemoryPersistence.getSecurityManagementService(), inMemoryPersistence.getPropertySpecService(), inMemoryPersistence.getThesaurus()).init(this.domain, securitySearchablePropertyGroup);
    }
}
