package com.energyict.genericprotocolimpl.webrtukp;

import com.energyict.genericprotocolimpl.nta.abstractnta.AbstractMbusDevice;
import com.energyict.genericprotocolimpl.nta.abstractnta.AbstractNTAProtocol;
import com.energyict.mdw.core.Rtu;

import java.util.List;
import java.util.logging.Logger;

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

    /**
     * Creates a new Instance of the the used MbusDevice type
     *
     * @param serial          the serialnumber of the mbusdevice
     * @param physicalAddress the physical address of the Mbus device
     * @param mbusRtu         the rtu in the database representing the mbus device
     * @param logger          the logger that will be used
     * @return a new Mbus class instance
     */
    @Override
    protected AbstractMbusDevice getMbusInstance(String serial, int physicalAddress, Rtu mbusRtu, Logger logger) {
        return new MbusDevice(serial, physicalAddress, mbusRtu, logger);  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getVersion() {
        return "$Date$";
    }
}
