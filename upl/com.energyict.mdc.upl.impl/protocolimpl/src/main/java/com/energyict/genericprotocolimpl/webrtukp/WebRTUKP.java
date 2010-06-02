package com.energyict.genericprotocolimpl.webrtukp;

import com.energyict.genericprotocolimpl.nta.abstractnta.AbstractNTAProtocol;

import java.util.List;

/**
 *
 * We keep the original protocol class for backwards compatibility.
 * What this does is mainly execute the abstractClass
 *
 * <br>Copyrights EnergyICT
 * <br>Date: 2-jun-2010
 * <br>Time: 16:29:12
 *
 * @deprecated use the {@link com.energyict.genericprotocolimpl.nta.eict.WebRTUKP} protocol instead
 */
public class WebRTUKP extends AbstractNTAProtocol {
    /**
     * Extra protocol settings for a <b>subclassed NTA protocol</b>
     * Can be used to override a default custom property or add specific custom properties.
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
        return "$Date$";
    }
}
