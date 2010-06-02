package com.energyict.genericprotocolimpl.nta.eict;

import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.genericprotocolimpl.common.StoreObject;
import com.energyict.genericprotocolimpl.nta.abstractnta.AbstractNTAProtocol;
import com.energyict.mdw.core.CommunicationScheduler;
import com.energyict.mdw.core.Rtu;

import java.util.List;

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
