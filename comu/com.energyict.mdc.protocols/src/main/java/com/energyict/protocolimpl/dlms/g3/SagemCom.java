package com.energyict.protocolimpl.dlms.g3;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.protocols.mdc.services.impl.OrmClient;

import com.energyict.protocolimpl.dlms.g3.messaging.G3MessagingSagemCom;

import javax.inject.Inject;

/**
 * The only difference (so far) is a slightly different way of initiating the image transfer, and the conformance block in the user information field
 * <p/>
 * Copyrights EnergyICT
 * Date: 26/11/12
 * Time: 14:02
 * Author: khe
 */
public class SagemCom extends AS330D {

    @Inject
    public SagemCom(PropertySpecService propertySpecService, OrmClient ormClient) {
        super(propertySpecService, ormClient);
    }

    @Override
    protected void initMessaging() {
        setMessaging(new G3MessagingSagemCom(getSession(), getProperties()));
    }

    protected G3Properties getProperties() {
        if (properties == null) {
            properties = new SagemComG3Properties();
        }
        return properties;
    }

    /**
     * The protocol version
     */
    @Override
    public String getProtocolVersion() {
        return "$Date: 2014-06-02 13:26:25 +0200 (Mon, 02 Jun 2014) $";
    }
}