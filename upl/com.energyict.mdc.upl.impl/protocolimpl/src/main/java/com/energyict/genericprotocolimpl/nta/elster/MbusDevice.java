package com.energyict.genericprotocolimpl.nta.elster;

import com.energyict.cbo.BusinessException;
import com.energyict.dialer.core.Link;
import com.energyict.genericprotocolimpl.common.messages.RtuMessageCategoryConstants;
import com.energyict.genericprotocolimpl.common.messages.RtuMessageConstant;
import com.energyict.genericprotocolimpl.common.messages.RtuMessageKeyIdConstants;
import com.energyict.genericprotocolimpl.nta.abstractnta.AbstractMbusDevice;
import com.energyict.genericprotocolimpl.nta.elster.profiles.MbusProfile;
import com.energyict.genericprotocolimpl.nta.profiles.MbusDailyMonthlyProfile;
import com.energyict.mdw.core.CommunicationScheduler;
import com.energyict.mdw.core.Rtu;
import com.energyict.protocol.messaging.MessageCategorySpec;
import com.energyict.protocol.messaging.MessageSpec;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 31-mei-2010
 * Time: 12:04:15
 */
public class MbusDevice extends AbstractMbusDevice {

    /**
     * Empty constructor
     */
    public MbusDevice() {
        super();
    }

    public MbusDevice(String serial, int physicalAddress, Rtu mbusRtu, Logger logger) {
        super(serial, physicalAddress, mbusRtu, logger);
    }

    @Override
    public void execute(CommunicationScheduler scheduler, Link link, Logger logger) throws BusinessException, SQLException, IOException {
        this.commProfile = scheduler.getCommunicationProfile();

//		try {
        // Before reading data, check the serialnumber
//			verifySerialNumber(); //TODO set back!
//		} catch (IOException e) {
//			e.printStackTrace();
//			throw new IOException(e.getMessage());
//		}

        // import profile
        if (commProfile.getReadDemandValues()) {
            getLogger().log(Level.INFO, "Getting loadProfile for meter with serialnumber: " + getMbus().getSerialNumber());
            MbusProfile mp = new MbusProfile(this);
            mp.getProfile(getWebRTU().getMeterConfig().getMbusProfile(getPhysicalAddress()).getObisCode());
        }

        if (commProfile.getReadMeterEvents()) {
//            getLogger().log(Level.INFO, "Getting events for meter with serialnumber: " + getMbus().getSerialNumber());
//            MbusEventProfile mep = new MbusEventProfile(this);
//            mep.getEvents();
        }

        // import daily/monthly
        if (commProfile.getReadMeterReadings()) {
            MbusDailyMonthlyProfile mdm = new MbusDailyMonthlyProfile(this);

            if (getWebRTU().isReadDaily()) {
                getLogger().log(Level.INFO, "Getting Daily values for meter with serialnumber: " + getMbus().getSerialNumber());
                mdm.getDailyProfile(getMeterConfig().getDailyProfileObject().getObisCode());
            }

            if (getWebRTU().isReadMonthly()) {
                getLogger().log(Level.INFO, "Getting Monthly values for meter with serialnumber: " + getMbus().getSerialNumber());
                mdm.getMonthlyProfile(getMeterConfig().getMonthlyProfileObject().getObisCode());
            }
            getLogger().log(Level.INFO, "Getting registers from Mbus meter " + (getPhysicalAddress() + 1));
            doReadRegisters();
        }

        // send rtuMessages
        if (commProfile.getSendRtuMessage()) {
            sendMeterMessages();
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
        msgSpec = addEncryptionkeys(RtuMessageKeyIdConstants.MBUSENCRYPTIONKEY,
                RtuMessageConstant.MBUS_ENCRYPTION_KEYS, false);
        catMbusSetup.addMessageSpec(msgSpec);
        return catMbusSetup;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getVersion() {
//        return super.getVersion();    //To change body of overridden methods use File | Settings | File Templates.
        return "$Date$" + " NTAProtocolVersion : " + super.getVersion();
    }
}
