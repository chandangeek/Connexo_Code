package com.energyict.protocolimplv2.eict.eiweb;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.protocols.mdc.channels.inbound.EIWebConnectionType;
import com.energyict.mdc.common.DataVault;
import com.energyict.mdc.common.DataVaultProvider;
import org.junit.*;

import static org.fest.assertions.api.Assertions.assertThat;
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
        DataVaultProvider dataVaultProvider = mock(DataVaultProvider.class);
        when(dataVaultProvider.getKeyVault()).thenReturn(dataVault);
        DataVaultProvider.instance.set(dataVaultProvider);
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