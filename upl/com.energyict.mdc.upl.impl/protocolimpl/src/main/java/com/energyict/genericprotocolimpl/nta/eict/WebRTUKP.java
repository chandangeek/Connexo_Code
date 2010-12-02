package com.energyict.genericprotocolimpl.nta.eict;

import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.aso.*;
import com.energyict.genericprotocolimpl.common.StoreObject;
import com.energyict.genericprotocolimpl.nta.abstractnta.AbstractMbusDevice;
import com.energyict.genericprotocolimpl.nta.abstractnta.AbstractNTAProtocol;
import com.energyict.mdw.core.CommunicationScheduler;
import com.energyict.mdw.core.Rtu;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Subclass of the {@link com.energyict.genericprotocolimpl.nta.abstractnta.AbstractNTAProtocol}
 *
 * <br/>
 * Copyrights EnergyICT<br/>
 * Date: 1-jun-2010<br/>
 * Time: 11:58:24<br/>
 */
public class WebRTUKP extends AbstractNTAProtocol {

    /**
     * The property indication for the NTA Simulation Tool
     */
    private static final String PR_NTA_SIMULATION_TOOL = "NTASimulationTool";

    /**
     * Variable indicates whether the NTASimulator is used(true) or not(false)
     */
    private boolean ntaSimulationTool;

    /**
     * Construct the desired {@link com.energyict.dlms.aso.ApplicationServiceObject}
     *
     * @param xDlmsAse the {@link com.energyict.dlms.aso.XdlmsAse} to use
     * @param sc       the {@link com.energyict.dlms.aso.SecurityContext} to use
     * @return the newly create ApplicationServiceObject
     */
    @Override
    protected ApplicationServiceObject buildApplicationServiceObject(XdlmsAse xDlmsAse, SecurityContext sc) {
        if (this.ntaSimulationTool) {
            return new ApplicationServiceObject(xDlmsAse, this, sc,
                    (this.datatransportSecurityLevel == 0) ? AssociationControlServiceElement.LOGICAL_NAME_REFERENCING_NO_CIPHERING :
                            AssociationControlServiceElement.LOGICAL_NAME_REFERENCING_WITH_CIPHERING,
                    this.serialNumber.getBytes(), null);
        } else {
            return super.buildApplicationServiceObject(xDlmsAse, sc);
        }
    }

    /**
     * Extra protocol settings for a <b>subclassed NTA protocol</b>
     */
    @Override
    public void doValidateProperties() {
        this.ntaSimulationTool = !properties.getProperty(PR_NTA_SIMULATION_TOOL, "0").equalsIgnoreCase("0");
    }

    /**
     * Add extra optional keys
     *
     * @return a List<String> with optional key parameters, return null if no additionals are required
     */
    @Override
    public List<String> doGetOptionalKeys() {
        List<String> optionals = new ArrayList<String>(1);
        optionals.add(PR_NTA_SIMULATION_TOOL);
        return optionals;
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
    protected void setRtu(Rtu rtu) {
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
