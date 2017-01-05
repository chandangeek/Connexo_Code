package com.energyict.protocolimplv2.eict.eiweb;

import com.energyict.mdc.channels.inbound.EIWebConnectionType;

import com.energyict.cpo.PropertySpec;
import com.energyict.mdw.core.DataVault;
import com.energyict.mdw.core.DataVaultProvider;

import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link EIWebConnectionType} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-03-22 (08:43)
 */
public class EIWebConnectionTypePropertiesTest {

    @Before
    public void initializeDataVaultProvider () {
        DataVault dataVault = mock(DataVault.class);
        DataVaultProvider dataVaultProvider = mock(DataVaultProvider.class);
        when(dataVaultProvider.getKeyVault()).thenReturn(dataVault);
        DataVaultProvider.instance.set(dataVaultProvider);
    }

    @Test
    public void testGetOptionalPropertiesIsNotNull () {
        EIWebConnectionType connectionType = new EIWebConnectionType();
        assertThat(connectionType.getOptionalProperties()).isNotNull();
    }

    @Test
    public void testAllOptionalPropertiesAreReturnedByGetPropertySpec () {
        EIWebConnectionType connectionType = new EIWebConnectionType();
        for (PropertySpec optionalPropertySpec : connectionType.getOptionalProperties()) {
            assertThat(connectionType.getUPLPropertySpec(optionalPropertySpec.getName())).
                    as("Property " + optionalPropertySpec.getName() + " is not returned by getPropertySpec").
                    isNotNull();
            assertThat(connectionType.getUPLPropertySpec(optionalPropertySpec.getName())).isEqualTo(optionalPropertySpec);
        }
    }

    @Test
    public void testOptionalPropertiesAreNotRequired () {
        EIWebConnectionType connectionType = new EIWebConnectionType();
        for (PropertySpec optionalPropertySpec : connectionType.getOptionalProperties()) {
            assertThat(connectionType.isRequiredProperty(optionalPropertySpec.getName())).isFalse();
        }
    }

    @Test
    public void testGetRequiredPropertiesIsNotNull () {
        EIWebConnectionType connectionType = new EIWebConnectionType();
        assertThat(connectionType.getRequiredProperties()).isNotNull();
    }

    @Test
    public void testAllRequiredPropertiesAreReturnedByGetPropertySpec () {
        EIWebConnectionType connectionType = new EIWebConnectionType();
        for (PropertySpec requiredPropertySpec : connectionType.getRequiredProperties()) {
            assertThat(connectionType.getUPLPropertySpec(requiredPropertySpec.getName())).
                    as("Property " + requiredPropertySpec.getName() + " is not returned by getPropertySpec").
                    isNotNull();
            assertThat(connectionType.getUPLPropertySpec(requiredPropertySpec.getName())).isEqualTo(requiredPropertySpec);
        }
    }

    @Test
    public void testRequiredPropertiesAreRequired () {
        EIWebConnectionType connectionType = new EIWebConnectionType();
        for (PropertySpec requiredPropertySpec : connectionType.getRequiredProperties()) {
            assertThat(connectionType.isRequiredProperty(requiredPropertySpec.getName())).
                    as("Optional property " + requiredPropertySpec.getName() + " is expected to be required").
                    isTrue();
        }
    }

}