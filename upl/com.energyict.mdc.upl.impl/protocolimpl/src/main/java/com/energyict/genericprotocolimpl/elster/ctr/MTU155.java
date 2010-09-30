package com.energyict.genericprotocolimpl.elster.ctr;

import com.energyict.genericprotocolimpl.common.AbstractGenericProtocol;
import com.energyict.protocolimpl.base.ProtocolProperties;

import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 24-sep-2010
 * Time: 11:43:45
 */
public class MTU155 extends AbstractGenericProtocol {

    private ProtocolProperties properties = new MTU155Properties();

    public String getVersion() {
        return "$Date$";
    }

    public List<String> getRequiredKeys() {
        return properties.getRequiredKeys();
    }

    public List<String> getOptionalKeys() {
        return properties.getOptionalKeys();
    }

    @Override
    public void initProperties() {
        properties.initProperties(getProperties());
    }

    @Override
    protected void doExecute() {
        // Hmmm, still work to do :)
    }

}
