package com.energyict.protocolimplv2.dlms.idis.am500.properties;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.protocolimplv2.dlms.idis.am130.properties.AM130ConfigurationSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * Same like the AM130 properties, adds 'CallingAPTitle' and 'SwapServerAndClientAddress'
 * <p/>
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 19/12/2014 - 15:53
 */
public class IDISConfigurationSupport extends AM130ConfigurationSupport {

    public static final String SWAP_SERVER_AND_CLIENT_ADDRESS_PROPERTY = "SwapServerAndClientAddress";

    @Override
    public List<PropertySpec> getOptionalProperties() {
        List<PropertySpec> result = new ArrayList<>();
        result.addAll(super.getOptionalProperties());
        result.add(this.swapServerAndClientAddress());

        // Not supported in IDIS P1
        result.remove(super.useGeneralBlockTransferPropertySpec());
        result.remove(super.generalBlockTransferWindowSizePropertySpec());
        result.remove(super.cipheringTypePropertySpec());
        return result;
    }

    protected PropertySpec swapServerAndClientAddress() {
        return PropertySpecFactory.notNullableBooleanPropertySpec(SWAP_SERVER_AND_CLIENT_ADDRESS_PROPERTY, true);
    }
}