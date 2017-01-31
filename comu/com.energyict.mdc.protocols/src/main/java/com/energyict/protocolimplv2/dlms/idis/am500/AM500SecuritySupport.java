/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.dlms.idis.am500;


import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.protocols.naming.SecurityPropertySpecName;

import com.energyict.protocolimplv2.security.DsmrSecuritySupport;

import java.math.BigDecimal;

public class AM500SecuritySupport extends DsmrSecuritySupport {

    public AM500SecuritySupport(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(propertySpecService, thesaurus);
    }

    @Override
    protected PropertySpec getClientMacAddressPropertySpec() {
        return getPropertySpecService().boundedBigDecimalSpec(BigDecimal.valueOf(0), BigDecimal.valueOf(0x7F))
                .named(SecurityPropertySpecName.CLIENT_MAC_ADDRESS.toString(), SecurityPropertySpecName.CLIENT_MAC_ADDRESS.toString())
                .describedAs(SecurityPropertySpecName.CLIENT_MAC_ADDRESS.toString()).setDefaultValue(BigDecimal.valueOf(1)).finish();
    }
}
