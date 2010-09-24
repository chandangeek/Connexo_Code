package com.energyict.genericprotocolimpl.elster.ctr;

import com.energyict.genericprotocolimpl.common.AbstractGenericProtocol;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 24-sep-2010
 * Time: 11:43:45
 */
public class MTU155 extends AbstractGenericProtocol {

    public String getVersion() {
        return "$Date$";
    }

    public List<String> getRequiredKeys() {
        List<String> requiredKeys = new ArrayList<String>();
        // Add required keys
        return requiredKeys;
    }

    public List<String> getOptionalKeys() {
        List<String> optionalKeys = new ArrayList<String>();
        // Add optional keys
        return optionalKeys;
    }

    @Override
    protected void doExecute() {
        // Hmmm, still work to do :)
    }


}
