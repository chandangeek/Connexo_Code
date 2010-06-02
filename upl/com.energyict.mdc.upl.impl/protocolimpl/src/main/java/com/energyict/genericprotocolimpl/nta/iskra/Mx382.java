package com.energyict.genericprotocolimpl.nta.iskra;

import com.energyict.genericprotocolimpl.nta.abstractnta.AbstractNTAProtocol;

import java.util.List;

/**
 * This is the subclass for the Iskra NTA device.
 *
 * Copyrights EnergyICT
 * Date: 28-mei-2010
 * Time: 15:35:36
 */
public class Mx382 extends AbstractNTAProtocol {

    /**
     * Extra protocol settings for a <b>subclassed NTA protocol</b>
     */
    @Override
    public void doValidateProperties() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Add extra optional keys
     *
     * @return a List<String> with optional key parameters
     */
    @Override
    public List<String> doGetOptionalKeys() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Add extra required keys
     *
     * @return a List<String> with required key parameters
     */
    @Override
    public List<String> doGetRequiredKeys() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getVersion() {
        return "$Date$" + " NTAProtocolVersion : " + super.getVersion();
    }
}
