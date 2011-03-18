package com.energyict.genericprotocolimpl.nta.elster;

import com.energyict.cbo.BusinessException;
import com.energyict.dialer.core.Link;
import com.energyict.genericprotocolimpl.nta.abstractnta.*;
import com.energyict.genericprotocolimpl.nta.elster.messagehandling.AM100MbusMessageExecutor;
import com.energyict.genericprotocolimpl.nta.elster.profiles.MbusProfile;
import com.energyict.genericprotocolimpl.nta.profiles.MbusDailyMonthlyProfile;
import com.energyict.mdw.core.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.messaging.MessageCategorySpec;
import com.energyict.protocol.messaging.MessageSpec;
import com.energyict.protocolimpl.messages.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 31-mei-2010
 * Time: 12:04:15
 */
public class MbusDevice extends AbstractMbusDevice {

    /**
     * The OMS specification meter reading ObisCode
     */
    private final static ObisCode OBISCODE_READING_OMS = ObisCode.fromString("7.0.99.99.2.255");

    /**
     * Contains the string for the MbusType property
     */
    private final static String mBusTypeProperty = "MbusType";
    private final static String OMS = "OMS";
    private final static String NTA = "NTA";

    /**
     * Contains the MBus type, should be NTA or OMS
     */
    private String mbusType;


    /**
     * Empty constructor
     */
    public MbusDevice() {
        super();
    }

    public MbusDevice(String serial, int physicalAddress, Rtu mbusRtu, Logger logger) {
        super(serial, physicalAddress, mbusRtu, logger);
    }

    /**
     * Extra protocol settings for a <b>subclassed NTA protocol</b>
     * Can be used to override a default custom property or add specific custom properties.
     *
     * @param properties
     */
    @Override
    public void doValidateProperties(Properties properties) throws InvalidPropertyException {
        mbusType = properties.getProperty(mBusTypeProperty);
        if (!OMS.equalsIgnoreCase(mbusType) && !NTA.equalsIgnoreCase(mbusType)) {
            throw new InvalidPropertyException("MbusType should be 'NTA' or 'OMS'");
        }
    }

    /**
     * Add extra optional keys
     *
     * @return a List<String> with optional key parameters, return null if no additionals are required
     */
    @Override
    protected List<String> doGetOptionalKeys() {
        return null;  // no optional properties to add
    }

    /**
     * Add extra required keys
     *
     * @return a List<String> with required key parameters, return null if no additionals are required
     */
    @Override
    protected List<String> doGetRequiredKeys() {
        List<String> result = new ArrayList<String>(1);
        result.add(mBusTypeProperty);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(CommunicationScheduler scheduler, Link link, Logger logger) throws BusinessException, SQLException, IOException {
        this.commProfile = scheduler.getCommunicationProfile();

         validateProperties();

        // import profile
        if (commProfile.getReadDemandValues()) {
            getLogger().log(Level.INFO, "Getting loadProfile for meter with serialnumber: " + getMbus().getSerialNumber());
            MbusProfile mp = new MbusProfile(this);
            mp.getProfile(getObiscodeProvider().getHourlyProfileObisCode(getPhysicalAddress()));
        }

        if (commProfile.getReadMeterEvents()) {
            getLogger().log(Level.INFO, "Events are not available for Mbus meters connected to an AM100.");
        }

        // Registers
        if (commProfile.getReadMeterReadings()) {
            MbusDailyMonthlyProfile mdm = new MbusDailyMonthlyProfile(this);

            getLogger().log(Level.INFO, "Getting registers from Mbus meter with serialnumber:" + getMbus().getSerialNumber());
            doReadRegisters();
        }

        // send rtuMessages
        if (commProfile.getSendRtuMessage()) {
            sendMeterMessages();
        }
    }

    /**
     * Write the configured messages to the device
     */
    protected void sendMeterMessages() throws BusinessException, SQLException {
        AM100MbusMessageExecutor messageExecutor = new AM100MbusMessageExecutor(this);

		Iterator<RtuMessage> it = getMbus().getPendingMessages().iterator();
		RtuMessage rm = null;
		while (it.hasNext()) {
			rm = it.next();
			messageExecutor.doMessage(rm);
		}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List getMessageCategories() {
        List<MessageCategorySpec> categories = new ArrayList();
        MessageCategorySpec catDisconnect = getConnectControlCategory();
        MessageCategorySpec catMbusSetup = getAM100MbusSetupCategory();

        categories.add(catDisconnect);
        categories.add(catMbusSetup);
        return categories;
    }

    /**
     * Create two messages, one to <b>decommission</b> the mbus device and one to set the
     * <b>encryption keys</b>
     *
     * @return a category with four messages for Mbus functionality
     */
    private MessageCategorySpec getAM100MbusSetupCategory() {
        MessageCategorySpec catMbusSetup = new MessageCategorySpec(
                RtuMessageCategoryConstants.MBUSSETUP);
        MessageSpec msgSpec = addNoValueMsg(
                RtuMessageKeyIdConstants.MBUSDECOMMISSION,
                RtuMessageConstant.MBUS_DECOMMISSION, false);
        catMbusSetup.addMessageSpec(msgSpec);
        return catMbusSetup;
    }

    /**  
     * {@inheritDoc}
     */
    @Override
    public String getVersion() {
        return "$Date$" + " NTAProtocolVersion : " + super.getVersion();
    }


    /**
     * Getter for the {@link com.energyict.genericprotocolimpl.nta.abstractnta.MbusObisCodeProvider}
     *
     * @return the {@link com.energyict.genericprotocolimpl.nta.abstractnta.MbusObisCodeProvider}
     */
    @Override
    public MbusObisCodeProvider getObiscodeProvider() throws IOException {
        return new NTAObisCodeProvider();

        /* Before we hade more then one obiscode provider, depending on the config of the device */
//        if (NTA.equalsIgnoreCase(mbusType)) {
//            return new NTAObisCodeProvider();
//        } else if (OMS.equalsIgnoreCase(mbusType)) {
//
//            // TODO if it is an OMS meter, then there are also differences between the Gas and Water ObisCodes
//
//            return new OMSGasObisCodeProvider();
//        }
//        throw new IOException("Incorrect MbusObisCodeProvider selection type");
    }
}
