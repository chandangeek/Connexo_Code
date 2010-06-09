package com.energyict.genericprotocolimpl.nta.elster;

import com.energyict.cbo.BusinessException;
import com.energyict.dialer.core.Link;
import com.energyict.genericprotocolimpl.nta.abstractnta.AbstractMbusDevice;
import com.energyict.genericprotocolimpl.nta.elster.profiles.MbusProfile;
import com.energyict.genericprotocolimpl.nta.profiles.MbusDailyMonthlyProfile;
import com.energyict.mdw.core.CommunicationScheduler;
import com.energyict.mdw.core.Rtu;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 31-mei-2010
 * Time: 12:04:15
 */
public class MbusDevice extends AbstractMbusDevice {

    /** Empty constructor */
    public MbusDevice(){
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


    @Override
    public String getVersion() {
//        return super.getVersion();    //To change body of overridden methods use File | Settings | File Templates.
        return "$Date$" + " NTAProtocolVersion : " + super.getVersion();
    }
}
