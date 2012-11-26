package com.energyict.genericprotocolimpl.webrtukp;

import com.energyict.genericprotocolimpl.nta.abstractnta.*;
import com.energyict.mdw.core.Device;
import com.energyict.protocol.InvalidPropertyException;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 2-jun-2010
 * Time: 16:37:17
 *
 * @deprecated use the {@link com.energyict.genericprotocolimpl.nta.eict.MbusDevice} instead
 */
public class MbusDevice extends AbstractMbusDevice {

    /**
     * Empty constructor for Revision number visualization in EIServer
     */
    public MbusDevice(){
        super();
    }

    /**
     * Constructor which is normally called from the {@link com.energyict.genericprotocolimpl.webrtukp.WebRTUKP} protocol
     * to create an MBus object
     *
     * @param serial the serialNumber of the MBus device
     * @param physicalAddress the physicalAddress of the MBus device
     * @param mbusRtu the {@link com.energyict.mdw.core.Device} in the database
     * @param logger the used logger
     */
    public MbusDevice(String serial, int physicalAddress, Device mbusRtu, Logger logger) {
        super(serial, physicalAddress, mbusRtu, logger);    //To change body of overridden methods use File | Settings | File Templates.
    }

    /**
     * Extra protocol settings for a <b>subclassed NTA protocol</b>
     * Can be used to override a default custom property or add specific custom properties.
     * @param properties
     */
    @Override
    public void doValidateProperties(Properties properties) throws InvalidPropertyException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Add extra optional keys
     *
     * @return a List<String> with optional key parameters, return null if no additionals are required
     */
    @Override
    protected List<String> doGetOptionalKeys() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Add extra required keys
     *
     * @return a List<String> with required key parameters, return null if no additionals are required
     */
    @Override
    protected List<String> doGetRequiredKeys() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Getter for the {@link com.energyict.genericprotocolimpl.nta.abstractnta.MbusObisCodeProvider}
     *
     * @return the {@link com.energyict.genericprotocolimpl.nta.abstractnta.MbusObisCodeProvider}
     */
    @Override
    public MbusObisCodeProvider getObiscodeProvider() throws IOException {
        return new NTAObisCodeProvider();
    }

    @Override
    public String getVersion() {
        return "$Date$";
    }
}
