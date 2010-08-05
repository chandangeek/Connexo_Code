package com.energyict.genericprotocolimpl.nta.eict;

import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.genericprotocolimpl.common.StoreObject;
import com.energyict.genericprotocolimpl.nta.abstractnta.AbstractMbusDevice;
import com.energyict.genericprotocolimpl.nta.abstractnta.AbstractNTAProtocol;
import com.energyict.mdw.core.CommunicationScheduler;
import com.energyict.mdw.core.Rtu;

import java.util.List;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 1-jun-2010
 * Time: 11:58:24
 */
public class WebRTUKP extends AbstractNTAProtocol {

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
     * @return a List<String> with optional key parameters, return null if no additionals are required
     */
    @Override
    public List<String> doGetOptionalKeys() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Add extra required keys
     *
     * @return a List<String> with required key parameters, return null if no additionals are required
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
        return "$Date$" + " NTAProtocolVersion : " + super.getVersion();
    }

    @Override
    protected void setRtu(Rtu rtu){
        super.setRtu(rtu);
    }

    @Override
    protected void setMeterConfig(DLMSMeterConfig meterConfig) {
        super.setMeterConfig(meterConfig);
    }

    @Override
    protected void setCommunicationScheduler(CommunicationScheduler communicationScheduler) {
        super.setCommunicationScheduler(communicationScheduler);
    }

    @Override
    protected void setStoreObject(StoreObject storeObject) {
        super.setStoreObject(storeObject);
    }

}
