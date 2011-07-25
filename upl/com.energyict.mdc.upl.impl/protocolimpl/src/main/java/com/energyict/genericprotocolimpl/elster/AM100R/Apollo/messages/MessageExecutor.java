package com.energyict.genericprotocolimpl.elster.AM100R.Apollo.messages;

import com.energyict.cbo.ApplicationException;
import com.energyict.cbo.BusinessException;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.cosem.*;
import com.energyict.genericprotocolimpl.common.GenericMessageExecutor;
import com.energyict.genericprotocolimpl.common.ParseUtils;
import com.energyict.genericprotocolimpl.common.messages.MessageHandler;
import com.energyict.genericprotocolimpl.elster.AM100R.Apollo.ApolloMeter;
import com.energyict.genericprotocolimpl.webrtu.common.csvhandling.CSVParser;
import com.energyict.genericprotocolimpl.webrtu.common.csvhandling.TestObject;
import com.energyict.mdw.core.*;
import com.energyict.mdw.shadow.RtuMessageShadow;
import com.energyict.protocolimpl.base.ActivityCalendarController;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 17-mrt-2011
 * Time: 8:51:14
 */
public class MessageExecutor extends GenericMessageExecutor {

    private final ApolloMeter protocol;

    private ApolloActivityCalendarController activityCalendarController;

    public MessageExecutor(final ApolloMeter protocol) {
        this.protocol = protocol;
    }

    @Override
    public void doMessage(final RtuMessage rtuMessage) throws BusinessException, SQLException {
        boolean success = false;
        String content = rtuMessage.getContents();
        MessageHandler messageHandler = new MessageHandler();


        try {
            importMessage(content, messageHandler);

            boolean timeOfUseMessage = messageHandler.getType().equals(RtuMessageConstant.TOU_ACTIVITY_CAL);
            boolean testMessage = messageHandler.getType().equals(RtuMessageConstant.TEST_MESSAGE);
            boolean activateCalendar = messageHandler.getType().equals(RtuMessageConstant.TOU_ACTIVATE_CALENDAR);
            if (timeOfUseMessage) {
                getLogger().log(Level.INFO, "Received update ActivityCalendar message.");
                getLogger().log(Level.FINEST, "Parsing the content of the CodeTable.");
                getActivityCalendarController().parseContent(content);
                getLogger().log(Level.FINEST, "Setting the new Passive Calendar Name.");
                getActivityCalendarController().writeCalendarName("");
                getLogger().log(Level.FINEST, "Sending out the new Passive Calendar objects.");
                getActivityCalendarController().writeCalendar();
                success = true;
            } else if (activateCalendar) {
                String dateFromMessage = messageHandler.getTOUActivationDate();
                getLogger().log(Level.INFO, "Handling message " + rtuMessage.displayString() + ": Activating activity calendar");
                if (!dateFromMessage.equals(null) && (!dateFromMessage.equals(""))) {
                    if (dateFromMessage.equalsIgnoreCase("1")) {
                        getActivityCalendarController().writeCalendarActivationTime(null);  //writing null will activate immediately
                    } else {
                        Calendar calendar = Calendar.getInstance(this.protocol.getTimeZone());
                        calendar.setTimeInMillis(Long.valueOf(dateFromMessage) * 1000);
                        getActivityCalendarController().writeCalendarActivationTime(calendar);
                    }
                } else {
                    throw new IOException("No activationDate is given in the \"ActivateCalendar\" message - A value for the activationDate field is required.");
                }

                success = true;
            } else if (testMessage) {
                getLogger().log(Level.INFO, "Handling message " + rtuMessage.displayString() + ": TestMessage");
                doTestMessage(messageHandler.getTestUserFileId());
                success = true;
            }
        } catch (IOException e) {
            getLogger().log(Level.INFO, "Message " + rtuMessage.displayString() + " has failed. " + e.getMessage());
        } finally {
            if (success) {
                rtuMessage.confirm();
                getLogger().log(Level.INFO, "Message " + rtuMessage.displayString() + " has finished.");
            } else {
                rtuMessage.setFailed();
                getLogger().log(Level.INFO, "Message " + rtuMessage.displayString() + " has FAILED.");
            }
        }
    }

    private boolean doTestMessage(String userFileId) throws IOException, BusinessException, SQLException {

        int failures = 0;
        Date currentTime;
        if (!userFileId.equalsIgnoreCase("")) {
            if (ParseUtils.isInteger(userFileId)) {
                UserFile uf = mw().getUserFileFactory().find(Integer.parseInt(userFileId));
                if (uf != null) {
                    byte[] data = uf.loadFileInByteArray();
                    CSVParser csvParser = new CSVParser();
                    csvParser.parse(data);
                    boolean hasWritten;
                    TestObject to = new TestObject("");
                    for (int i = 0; i < csvParser.size(); i++) {
                        to = csvParser.getTestObject(i);
                        if (csvParser.isValidLine(to)) {
                            currentTime = new Date(System.currentTimeMillis());
                            hasWritten = false;
                            try {
                                switch (to.getType()) {
                                    case 0: { // GET
                                        GenericRead gr = this.protocol.getCosemObjectFactory().getGenericRead(to.getObisCode(), DLMSUtils.attrLN2SN(to.getAttribute()), to.getClassId());
                                        to.setResult("0x" + ParseUtils.decimalByteToString(gr.getResponseData()));
                                        hasWritten = true;
                                    }
                                    break;
                                    case 1: { // SET
                                        GenericWrite gw = this.protocol.getCosemObjectFactory().getGenericWrite(to.getObisCode(), to.getAttribute(), to.getClassId());
                                        gw.write(ParseUtils.hexStringToByteArray(to.getData()));
                                        to.setResult("OK");
                                        hasWritten = true;
                                    }
                                    break;
                                    case 2: { // ACTION
                                        GenericInvoke gi = this.protocol.getCosemObjectFactory().getGenericInvoke(to.getObisCode(), to.getClassId(), to.getMethod());
                                        if (to.getData().equalsIgnoreCase("")) {
                                            gi.invoke();
                                        } else {
                                            gi.invoke(ParseUtils.hexStringToByteArray(to.getData()));
                                        }
                                        to.setResult("OK");
                                        hasWritten = true;
                                    }
                                    break;
                                    case 3: { // MESSAGE
                                        RtuMessageShadow rms = new RtuMessageShadow();
                                        rms.setContents(csvParser.getTestObject(i).getData());
                                        rms.setRtuId(this.protocol.getMeter().getId());
                                        RtuMessage rm = mw().getRtuMessageFactory().create(rms);
                                        doMessage(rm);
                                        if (rm.getState().getId() == rm.getState().CONFIRMED.getId()) {
                                            to.setResult("OK");
                                        } else {
                                            to.setResult("MESSAGE failed, current state " + rm.getState().getId());
                                        }
                                        hasWritten = true;
                                    }
                                    break;
                                    case 4: { // WAIT
                                        waitCyclus(Integer.parseInt(to.getData()));
                                        to.setResult("OK");
                                        hasWritten = true;
                                    }
                                    break;
                                    case 5: {
                                        // do nothing, it's no valid line
                                    }
                                    break;
                                    default: {
                                        throw new ApplicationException("Row " + i + " of the CSV file does not contain a valid type.");
                                    }
                                }
                                to.setTime(currentTime.getTime());

                                // Check if the expected value is the same as the result
                                if ((to.getExpected() == null) || (!to.getExpected().equalsIgnoreCase(to.getResult()))) {
                                    to.setResult("Failed - " + to.getResult());
                                    failures++;
                                    getLogger().log(Level.INFO, "Test " + i + " has successfully finished, but the result didn't match the expected value.");
                                } else {
                                    getLogger().log(Level.INFO, "Test " + i + " has successfully finished.");
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                                if (!hasWritten) {
                                    if ((to.getExpected() != null) && (e.getMessage().indexOf(to.getExpected()) != -1)) {
                                        to.setResult(e.getMessage());
                                        getLogger().log(Level.INFO, "Test " + i + " has successfully finished.");
                                        hasWritten = true;
                                    } else {
                                        getLogger().log(Level.INFO, "Test " + i + " has failed.");
                                        String eMessage;
                                        if (e.getMessage().indexOf("\r\n") != -1) {
                                            eMessage = e.getMessage().substring(0, e.getMessage().indexOf("\r\n")) + "...";
                                        } else {
                                            eMessage = e.getMessage();
                                        }
                                        to.setResult("Failed. " + eMessage);
                                        hasWritten = true;
                                        failures++;
                                    }
                                    to.setTime(currentTime.getTime());
                                }
                            } finally {
                                if (!hasWritten) {
                                    to.setResult("Failed - Unknow exception ...");
                                    failures++;
                                    to.setTime(currentTime.getTime());
                                }
                            }
                        }
                    }
                    if (failures == 0) {
                        csvParser.addLine("All the tests are successfully finished.");
                    } else {
                        csvParser.addLine("" + failures + " of the " + csvParser.getValidSize() + " tests " + ((failures == 1) ? "has" : "have") + " failed.");
                    }
                    mw().getUserFileFactory().create(csvParser.convertResultToUserFile(uf, this.protocol.getMeter().getFolderId()));
                } else {
                    throw new ApplicationException("Userfile with ID " + userFileId + " does not exist.");
                }
            } else {
                throw new IOException("UserFileId is not a valid number");
            }
        } else {
            throw new IOException("No userfile id is given.");
        }

        return true;
    }

    private void waitCyclus(int delay) throws IOException {
        try {
            int nrOfPolls = (delay / 20) + (delay % 20 == 0 ? 0 : 1);
            for (int i = 0; i < nrOfPolls; i++) {
                if (i < nrOfPolls - 1) {
                    ProtocolTools.delay(20000);
                } else {
                    ProtocolTools.delay((delay - (i * 20)) * 1000);
                }
                this.protocol.getLogger().log(Level.INFO, "Keeping connection alive");
                this.protocol.getTime();
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException("Could not keep connection alive." + e.getMessage());
        }
    }

    /**
     * Short notation for MeteringWarehouse.getCurrent()
     */
    public MeteringWarehouse mw() {
        return MeteringWarehouse.getCurrent();
    }

    @Override
    protected TimeZone getTimeZone() {
        return this.protocol.getTimeZone();
    }

    private ApolloActivityCalendarController getActivityCalendarController() {
        return this.protocol.getActivityCalendarController();
    }

    private Logger getLogger() {
        return this.protocol.getLogger();
    }
}
