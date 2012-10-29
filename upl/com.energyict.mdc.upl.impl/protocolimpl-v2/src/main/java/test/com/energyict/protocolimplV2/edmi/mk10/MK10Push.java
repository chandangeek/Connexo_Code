/**
 * MK10Push.java
 *
 * Created on 8-jan-2009, 12:47:25 by jme
 *
 */
package test.com.energyict.protocolimplV2.edmi.mk10;

import com.energyict.cbo.BusinessException;
import com.energyict.cpo.Environment;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.cpo.TypedProperties;
import com.energyict.dialer.core.Link;
import com.energyict.mdw.amr.GenericProtocol;
import com.energyict.mdw.core.*;
import com.energyict.mdw.shadow.CommunicationSchedulerShadow;
import com.energyict.protocol.MeterReadingData;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolException;
import test.com.energyict.protocolimplV2.edmi.mk10.executer.MK10ProtocolExecuter;
import test.com.energyict.protocolimplV2.edmi.mk10.packets.PushPacket;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author jme
 *         <p/>
 *         JME|14102009|Quick fix for ImServ. They have a meter with a different discovery packet. (See MK10InputStreamParser.java)
 *         JME|09072010|COMMUNICATION-59 Fixed timeouts when udp packets were > 1024 bytes.
 *         JME|15072010|COMMUNICATION-59 Refactored MK10Push input stream
 */
// ToDo: should be integrated in new DeviceProtocol
public class MK10Push implements GenericProtocol {

    private static final int DEBUG = 0;

    private static final int BYTE_MASK = 0x000000FF;

    private Logger logger = null;
    private long connectTime = 0;
    private long disconnectTime = 0;
    private Link link = null;
    private MK10ProtocolExecuter MK10Executor = new MK10ProtocolExecuter(this);
    private String errorString = "";

    private InputStream inputStream = null;
    private OutputStream outputStream = null;

    private boolean fullDebugLogging = false;

    /*
      * Constructors
      */

    public MK10Push() {
    }

    /*
      * Private getters, setters and methods
      */

    private long getConnectTime() {
        return connectTime;
    }

    public void setConnectTime(long connectTime) {
        this.connectTime = connectTime;
    }

    private long getDisconnectTime() {
        return disconnectTime;
    }

    private void setDisconnectTime(long disconnectTime) {
        this.disconnectTime = disconnectTime;
    }

    private InputStream getInputStream() {
        return inputStream;
    }

    private OutputStream getOutputStream() {
        return outputStream;
    }

    private Rtu getMeter() {
        return getMK10Executor().getMeter();
    }

    public MK10ProtocolExecuter getMK10Executor() {
        return MK10Executor;
    }

    public MeteringWarehouse mw() {
        MeteringWarehouse result = MeteringWarehouse.getCurrent();
        return (result == null) ? new MeteringWarehouseFactory().getBatch() : result;
    }

    public String getErrorString() {
        if (errorString == null) {
            return "";
        }
        String returnValue = errorString.toString();
        if (returnValue == null) {
            return "";
        }
        return returnValue;
    }

    private void addLogging(CommunicationScheduler cs, int completionCode, String completionMessage, List<AmrJournalEntry> journal, boolean success, Exception exception) throws SQLException, BusinessException {
        sendDebug("** addLogging **", 2);

        // check if there was an protocol or timeout error
        if (!success && (completionCode == AmrJournalEntry.CC_OK)) {
            if (exception != null) {
                if ((exception.getMessage() != null) && (exception.getMessage().indexOf("timeout") != -1)) {
                    completionCode = AmrJournalEntry.CC_IOERROR;
                } else {
                    completionCode = AmrJournalEntry.CC_PROTOCOLERROR;
                }
            }
        }

        if ((cs != null) && (!cs.getActive())) {
            clearNextCommunicationDate(cs);

//            AMRJournalManager amrjm = new AMRJournalManager(getMeter(), cs);
//            amrjm.journal(new AmrJournalEntry(completionCode));
//            amrjm.journal(new AmrJournalEntry(AmrJournalEntry.CONNECTTIME, Math.abs(getDisconnectTime() - getConnectTime()) / 1000));
//
//            for (int i = 0; i < journal.size(); i++) {
//                AmrJournalEntry amrJournalEntry = journal.get(i);
//                amrjm.journal(amrJournalEntry);
//            }
//
//            if (getErrorString().length() > 0) {
//                amrjm.journal(new AmrJournalEntry(AmrJournalEntry.DETAIL, getErrorString()));
//            }
//            if (completionMessage.length() > 0) {
//                amrjm.journal(new AmrJournalEntry(AmrJournalEntry.DETAIL, completionMessage));
//            }
//            if (exception != null) {
//                amrjm.journal(new AmrJournalEntry(AmrJournalEntry.DETAIL, "Exception: " + exception.toString()));
//            }
//
//            if (success) {
//                sendDebug("** updateLastCommunication **", 3);
//                amrjm.updateLastCommunication();
//            } else {
//                sendDebug("** updateRetrials **", 3);
//                amrjm.updateRetrials();
//            }
        } else {
            getLogger().log(Level.INFO, "Failed to enter an AMR journal entry.");
        }
    }

    private void clearNextCommunicationDate(CommunicationScheduler cs) throws SQLException, BusinessException {
        CommunicationSchedulerShadow shadow = cs.getShadow();
        shadow.setNextCommunication(null);
        cs.update(shadow);
    }

    private void startCommunication(CommunicationScheduler cs) throws SQLException, BusinessException {
        if ((cs != null) && (!cs.getActive())) {
            cs.startCommunication(cs.getComPortId());
        }
    }

    private Rtu findMatchingMeter(String serial) {
        if (serial == null) {
            return null;
        }
        List meterList = mw().getRtuFactory().findBySerialNumber(serial);

        for (int i = 0; i < meterList.size(); i++) {
            Rtu tempMeter = (Rtu) meterList.get(i);
            if (tempMeter.getDialHomeId() != null) {
                if (tempMeter.getDialHomeId().trim().equalsIgnoreCase(serial.trim())) {
                    return (Rtu) meterList.get(i);
                }
            }
        }

        return null;
    }

    private Rtu waitForPushMeter() throws IOException, BusinessException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        Rtu pushDevice = null;
        while (inputStream.available() > 0) {
            buffer.write(inputStream.read() & BYTE_MASK);
        }

        PushPacket packet = PushPacket.getPushPacket(buffer.toByteArray());
        sendDebug("** Received packet: " + packet.getPushPacketType() + " **", 0);
        switch (packet.getPushPacketType()) {
            case README:
            case HEARTBEAT:
                pushDevice = findMatchingMeter(packet.getSerial());
                break;
            case COMMISSIONING:
                sendDebug(packet.toString(), 0);
            default:
                throw new BusinessException("Received wrong packet [" + packet.getPushPacketType() + "].");
        }

        if (pushDevice == null) {
            throw new BusinessException("RTU with callerID [" + packet.getSerial() + "] not found.");
        }

        return pushDevice;
    }

    private void storeMeterData(MeterReadingData meterReadingData, ProfileData meterProfileData) throws SQLException, BusinessException {

        if (DEBUG >= 2) {
            System.out.println("storeMeterData()");
            System.out.println(" meterReadingData = " + meterReadingData);
            System.out.println(" meterProfileData = " + meterProfileData);
        }

        if (meterReadingData != null) {
            getMeter().store(meterReadingData);
        }
        if (meterProfileData != null) {
            getMeter().store(meterProfileData);
        }
    }

    /*
      * Public methods
      */

    public void execute(CommunicationScheduler scheduler, Link link, Logger logger) throws BusinessException, SQLException, IOException {
        boolean success = true;
        Exception exception = null;

        this.link = link;
        this.logger = logger;
        this.inputStream = getLink().getInputStream();
        this.outputStream = getLink().getOutputStream();
        setConnectTime(System.currentTimeMillis());

        try {

            // Check if we got a message from the COMMSERVER UDP Listener
            if (scheduler != null) {
                throw new ProtocolException("scheduler != null. Execute must be triggered by UDP listener.");
            }

            sendDebug("** A new UDP session is started **", 0);
            sendDebug("** ConnectionTime: [" + getConnectTime() + "] **", 0);

            Rtu pushDevice = waitForPushMeter();
            getMK10Executor().setMeter(pushDevice);

            startCommunication(getMK10Executor().getInboundCommunicationScheduler());
            getMK10Executor().doMeterProtocol();
            storeMeterData(getMK10Executor().getMeterReadingData(), getMK10Executor().getMeterProfileData());

        } catch (ProtocolException e) {
            sendDebug("** EXCEPTION: " + e.getMessage() + " **", 1);
            errorString += e.getMessage();
            success = false;
            exception = e;
            e.printStackTrace();
            throw new BusinessException(e.getMessage());
        } catch (IOException e) {
            sendDebug("** EXCEPTION: " + e.getMessage() + " **", 1);
            errorString += e.getMessage();
            success = false;
            exception = e;
            e.printStackTrace();
            throw new BusinessException(e.getMessage());
        } catch (SQLException e) {
            sendDebug("** EXCEPTION: " + e.getMessage() + " **", 1);
            errorString += e.getMessage();
            success = false;
            exception = e;
            e.printStackTrace();
            throw new BusinessException(e.getMessage());
        } catch (BusinessException e) {
            sendDebug("** EXCEPTION: " + e.getMessage() + " **", 1);
            errorString += e.getMessage();
            success = false;
            exception = e;
            e.printStackTrace();
            throw new BusinessException(e.getMessage());
        } catch (Exception e) {
            sendDebug("** EXCEPTION: " + e.getMessage() + " **", 1);
            errorString += e.getMessage();
            success = false;
            exception = e;
            e.printStackTrace();
            throw new BusinessException(e.getMessage());
        } finally {

            setDisconnectTime(System.currentTimeMillis());

            sendDebug("** DisconnectTime: [" + getDisconnectTime() + "] **", 0);
            sendDebug("** Connection ended after " + (getDisconnectTime() - getConnectTime()) + " ms **", 0);
            sendDebug("** Closing the UDP session **", 0);

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ;

            try {
                addLogging(
                        getMK10Executor().getInboundCommunicationScheduler(),
                        getMK10Executor().getCompletionCode(),
                        getMK10Executor().getCompletionErrorString(),
                        getMK10Executor().getJournal(),
                        success,
                        exception
                );
            } catch (BusinessException e) {
                sendDebug("** BusinessException **", 1);
                e.printStackTrace();
                throw e;
            } catch (SQLException e) {
                sendDebug("** SQLException **", 1);
                e.printStackTrace();
                // Close the connection after an SQL exception, connection will startup again if requested
                Environment.getDefault().closeConnection();
                throw e;
            }
        }

    }

    /*
      * Public getters and setters
      */

    public Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(getClass().getName());
        }
        return logger;
    }

    public Link getLink() {
        return link;
    }

    public String getVersion() {
        return "$Date$";
    }

    public void addProperties(Properties properties) {
        sendDebug("** addProperties **", 2);
        getMK10Executor().addProperties(properties);
    }

    @Override
    public void addProperties(TypedProperties properties) {
        addProperties(properties.toStringProperties());
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return PropertySpecFactory.toPropertySpecs(getRequiredKeys());
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return PropertySpecFactory.toPropertySpecs(getOptionalKeys());
    }

    /*
      * Private debugging methods
      */

    public void sendDebug(String message, int debuglvl) {
        String returnMessage = "";
        if (DEBUG == 0) {
            returnMessage += " [MK10Push] > " + message;
        } else {
            returnMessage += " ##### DEBUG [";
            returnMessage += new Date().getTime();
            returnMessage += "] ######## > ";
            returnMessage += message;
        }
        if ((debuglvl <= DEBUG) && (getLogger() != null)) {
            getLogger().log(Level.INFO, returnMessage);
            System.out.print(returnMessage + "\n");
        }
    }

    public List<String> getOptionalKeys() {
        List<String> list = new ArrayList<String>();
        list.addAll(getMK10Executor().getOptionalKeys());
        list.add("FullDebug");
        return list;
    }

    public List<String> getRequiredKeys() {
        List<String> list = new ArrayList<String>();
        list.addAll(getMK10Executor().getRequiredKeys());
        return list;
    }

    public long getTimeDifference() {
        // TODO Auto-generated method stub
        return 0;
    }

    public boolean isFullDebugLogging() {
        return fullDebugLogging;
    }

    public void setFullDebugLogging(boolean fullDebugLogging) {
        this.fullDebugLogging = fullDebugLogging;
    }
}
