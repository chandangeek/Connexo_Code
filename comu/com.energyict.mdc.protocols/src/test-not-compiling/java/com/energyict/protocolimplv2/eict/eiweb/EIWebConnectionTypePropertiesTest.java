package com.energyict.protocolimplv2.eict.eiweb;

import com.elster.jupiter.datavault.DataVault;
import com.elster.jupiter.datavault.LegacyDataVaultProvider;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.protocols.mdc.channels.inbound.EIWebConnectionType;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link EIWebConnectionType} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-03-22 (08:43)
 */
public class EIWebConnectionTypePropertiesTest  extends AbstractEIWebTests{

    @Before
    public void initializeDataVaultProvider () {
        DataVault dataVault = mock(DataVault.class);
        LegacyDataVaultProvider dataVaultProvider = mock(LegacyDataVaultProvider.class);
        when(dataVaultProvider.getKeyVault()).thenReturn(dataVault);
        LegacyDataVaultProvider.instance.set(dataVaultProvider);
    }

    @Test
    public void testGetPropertiesIsNotNull () {
        EIWebConnectionType connectionType = new EIWebConnectionType();
        assertThat(connectionType.getPropertySpecs()).isNotNull();
    }

    @Test
    public void testAllPropertiesAreReturnedByGetPropertySpec () {
        EIWebConnectionType connectionType = new EIWebConnectionType();
        for (PropertySpec propertySpec : connectionType.getPropertySpecs()) {
            assertThat(connectionType.getPropertySpec(propertySpec.getName())).
                    as("Property " + propertySpec.getName() + " is not returned by getPropertySpec").
                    isNotNull();
            assertThat(connectionType.getPropertySpec(propertySpec.getName())).isEqualTo(propertySpec);
        }
    }

}