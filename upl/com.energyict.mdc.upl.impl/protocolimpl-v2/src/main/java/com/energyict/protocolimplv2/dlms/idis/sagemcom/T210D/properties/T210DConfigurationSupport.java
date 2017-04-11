package com.energyict.protocolimplv2.dlms.idis.sagemcom.T210D.properties;

import com.energyict.dlms.protocolimplv2.DlmsSessionProperties;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.security.PrivateKeyAlias;
import com.energyict.protocolimplv2.dlms.idis.am130.properties.AM130ConfigurationSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cisac on 12/22/2016.
 */
public class T210DConfigurationSupport extends AM130ConfigurationSupport {

    public T210DConfigurationSupport(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        ArrayList<PropertySpec> propertySpecs = new ArrayList<>(super.getUPLPropertySpecs());
        propertySpecs.add(clientPrivateSigningKeyPropertySpec());
        return propertySpecs;
    }

    /**
     * The private key of the client (the ComServer) used for digital signature (ECDSA)
     */
    private PropertySpec clientPrivateSigningKeyPropertySpec() {
        return this.getPropertySpecService().referenceSpec(PrivateKeyAlias.class.getName())
                .named(DlmsSessionProperties.CLIENT_PRIVATE_SIGNING_KEY, DlmsSessionProperties.CLIENT_PRIVATE_SIGNING_KEY)
                .describedAs(DlmsSessionProperties.CLIENT_PRIVATE_SIGNING_KEY)
                .finish();
    }
}