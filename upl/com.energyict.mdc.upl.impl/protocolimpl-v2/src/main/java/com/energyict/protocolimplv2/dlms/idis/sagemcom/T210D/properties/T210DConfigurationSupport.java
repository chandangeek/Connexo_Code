package com.energyict.protocolimplv2.dlms.idis.sagemcom.T210D.properties;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.dlms.protocolimplv2.DlmsSessionProperties;
import com.energyict.protocolimplv2.dlms.idis.am500.properties.IDISConfigurationSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cisac on 12/22/2016.
 */
public class T210DConfigurationSupport extends IDISConfigurationSupport {

    @Override
    public List<PropertySpec> getOptionalProperties() {
        List<PropertySpec> optionalProperties = new ArrayList<>(super.getOptionalProperties());
        optionalProperties.add(clientPrivateSigningKeyPropertySpec());
//        optionalProperties.add(clientPrivateKeyAgreementKeyPropertySpec());
        return optionalProperties;
    }


    /**
     * The private key of the client (the ComServer) used for digital signature (ECDSA)
     */
    private PropertySpec clientPrivateSigningKeyPropertySpec() {
        return PropertySpecFactory.privateKeyAliasPropertySpec(DlmsSessionProperties.CLIENT_PRIVATE_SIGNING_KEY);
    }
}
