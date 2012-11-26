package com.energyict.genericprotocolimpl.nta.iskra;

import com.energyict.genericprotocolimpl.nta.abstractnta.*;
import com.energyict.mdw.core.Device;
import com.energyict.protocol.InvalidPropertyException;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 31-mei-2010
 * Time: 12:05:43
 */
public class MbusDevice extends AbstractMbusDevice {

    /**
     * Default constructor for revision number support
     */    
    public MbusDevice() {
        super();
    }

    public MbusDevice(String serial, int physicalAddress, Device mbusRtu, Logger logger) {
        super(serial, physicalAddress, mbusRtu, logger);    //To change body of overridden methods use File | Settings | File Templates.
    }

    /**
     * Extra protocol settings for a <b>subclassed NTA protocol</b>
     * Can be used to override a default custom property or add specific custom properties.
     *
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
//        return super.getVersion();    //To change body of overridden methods use File | Settings | File Templates.
        return "$Date$" + " NTAProtocolVersion : " + super.getVersion();
    }

}
