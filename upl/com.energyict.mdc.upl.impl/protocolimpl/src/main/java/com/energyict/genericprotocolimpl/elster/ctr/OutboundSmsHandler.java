package com.energyict.genericprotocolimpl.elster.ctr;

import com.energyict.cbo.BusinessException;
import com.energyict.genericprotocolimpl.elster.ctr.encryption.SecureSmsConnection;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRConfigurationException;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRConnectionException;
import com.energyict.genericprotocolimpl.elster.ctr.messaging.MTU155MessageExecutor;
import com.energyict.mdw.core.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;

public class OutboundSmsHandler {

    private static final String SMS = "SMS";

    private MTU155 meterProtocol;

    public OutboundSmsHandler(MTU155 meterProtocol) {
        this.meterProtocol = meterProtocol;
    }

    /**
     * Execute all Otubound SMS schedules.
     *
     * @throws com.energyict.cbo.BusinessException
     *
     * @throws java.sql.SQLException
     */
    public void doExecute(CommunicationScheduler cs) throws BusinessException, SQLException, CTRConfigurationException, CTRConnectionException {

        meterProtocol.setRtu(cs.getRtu());
        meterProtocol.getProtocolProperties().addProperties(meterProtocol.getRtu().getProperties().toStringProperties());

//        String phoneNumber = meterProtocol.getRtu().getPhoneNumber();
        String phoneNumber = "";
        if ((phoneNumber == null) || ("".compareTo(phoneNumber) == 0)) {
            throw new CTRConfigurationException("The Phone Number cannot be left empty, correct first!");
        }

        meterProtocol.getLogger().info("Executing the outbound SMS CommunicationScheduler.");
        meterProtocol.storeStartTime();

        cs.startCommunication();
        cs.startReadingNow();
        executeCommunicationSchedule(cs);
        updateWriteDataBlockID(((SmsRequestFactory) meterProtocol.getRequestFactory()).getWriteDataBlockID());
        meterProtocol.logSuccess(cs);
    }

    /**
     * Executes the SMS communication schedule. Only the messages will be executed.
     *
     * @param communicationScheduler: the device's communication schedule
     * @throws IOException
     */
    private void executeCommunicationSchedule(CommunicationScheduler communicationScheduler) throws CTRConfigurationException, CTRConnectionException {
        CommunicationProfile communicationProfile = communicationScheduler.getCommunicationProfile();
        String csName = communicationScheduler.displayString();
        if (communicationProfile == null) {
            throw new CTRConfigurationException("CommunicationScheduler '" + csName + "' has no communication profile.");
        }

        //Send the meter messages
        if (communicationProfile.getSendRtuMessage()) {
            meterProtocol.log("Parsing messages for meter with serial number: " + meterProtocol.getMeterSerialNumberFromRtu());
            sendMeterMessages();
        }
    }

    private void sendMeterMessages() {
        MTU155MessageExecutor messageExecutor = getMessageExecuter();
        Iterator<OldDeviceMessage> it = meterProtocol.getRtu().getOldPendingMessages().iterator();
        OldDeviceMessage rm = null;
        while (it.hasNext()) {
            rm = it.next();
            try {
                ((SecureSmsConnection) meterProtocol.getRequestFactory().getConnection()).setRtuMessageID(rm.getId());
                messageExecutor.doMessage(rm);
                ((SecureSmsConnection) meterProtocol.getRequestFactory().getConnection()).postPendingSmsToQueue();
                meterProtocol.warning("Message [" + rm.displayString() + "] executed successfully.");
            } catch (BusinessException e) {
                meterProtocol.severe("Unable to send message [" + rm.displayString() + "]! " + e.getMessage());
            } catch (SQLException e) {
                meterProtocol.severe("Unable to send message [" + rm.displayString() + "]! " + e.getMessage());
            }
        }
    }

    private void updateWriteDataBlockID(int ID) throws BusinessException, SQLException {
        meterProtocol.setNetworkID(ID);
    }

    public MTU155MessageExecutor getMessageExecuter() {
        return new MTU155MessageExecutor(meterProtocol.getLogger(), meterProtocol.getRequestFactory(), meterProtocol.getRtu(), meterProtocol.getStoreObject());
    }

    public boolean isOutboundSmsProfile(CommunicationScheduler cs) {
        String displayName = cs != null ? cs.displayString() : null;
        return ((displayName != null) && displayName.contains(SMS) && !cs.getModemPool().getInbound());
    }
}