package com.energyict.genericprotocolimpl.iskrap2lpc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.energyict.mdw.core.*;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.energyict.cbo.BusinessException;
import com.energyict.cpo.PersistentObject;
import com.energyict.mdw.shadow.RtuMessageShadow;
import com.energyict.protocolimpl.utils.Utilities;

/**
 * @author gna
 * Generic Protocol test can be ignored from version 9.1
 */
@Ignore
public class P2LPCTest {

    private static Logger logger;
    private CommunicationProtocol commProtMeter = null;
    private Concentrator iskraConcentrator;
    private MeterReadTransaction meterReadTransaction;
    private TConnection connection;
    private CommunicationScheduler commScheduler;
    private Device meter;
    private Device concentrator;
    private DeviceType rtuTypeMeter = null;
    private String testMeter = "TestMeter";
    private String testConcentrator = "TestConcentrator";
    private String testname = "12345678";

    private String jcnIskraMeter = "com.energyict.genericprotocolimpl.iskrap2lpc.Meter";
    private String jcnConcentrator = "com.energyict.genericprotocolimpl.iskrap2lpc.Concentrator";

    private List result = new ArrayList();

    @BeforeClass
    public static void setUpOnce() {
        Utilities.createEnvironment();
        MeteringWarehouse.createBatchContext(false);
        logger = Logger.getLogger("global");
    }

    @Before
    public void setUp() throws BusinessException, SQLException {
        iskraConcentrator = new Concentrator();
        iskraConcentrator.setLogger(logger);
        connection = new TConnection(iskraConcentrator);
        iskraConcentrator.setConnection(connection);
        meterReadTransaction = new MeterReadTransaction(iskraConcentrator, null, "12121212", null);


        // Create unique names
        testMeter = "TestMeter" + System.currentTimeMillis();
        testConcentrator = "TestConcentrator" + System.currentTimeMillis();

        // find out if the communication profile exists, if not, create it
        result = Utilities.mw().getCommunicationProtocolFactory().findAll();
        for (int i = 0; i < result.size(); i++) {
            if (((CommunicationProtocol) result.get(i)).getJavaClassName().equalsIgnoreCase(jcnIskraMeter)) {
                commProtMeter = (CommunicationProtocol) result.get(i);
                break;
            }
        }
        if (commProtMeter == null) {
            commProtMeter = Utilities.findOrcreateCommunicationProtocol(jcnIskraMeter);
        }

        // find out if there is an rtuType defined with this testName, if not, create it
        result = Utilities.mw().getDeviceTypeFactory().findByName(testMeter);
        if (result.size() == 0) {
            rtuTypeMeter = Utilities.createRtuType(commProtMeter, testMeter, 4);
        } else {
            rtuTypeMeter = (DeviceType) result.get(0);
        }
    }

    @After
    public void tearDown() throws BusinessException, SQLException {
        // first delete all the device
        List result = Utilities.mw().getDeviceFactory().findByName(testname);
        result.addAll(Utilities.mw().getDeviceTypeFactory().findByName(testMeter));
        result.addAll(Utilities.mw().getDeviceTypeFactory().findByName(testConcentrator));
        result.addAll(Utilities.mw().getCommunicationProtocolFactory().findByName(jcnIskraMeter));
        result.addAll(Utilities.mw().getCommunicationProtocolFactory().findByName(jcnConcentrator));
        result.addAll(Utilities.mw().getCommunicationProfileFactory().findByName(Utilities.commProfile_SendRtuMessage));
        result.addAll(Utilities.mw().getCommunicationProfileFactory().findByName(Utilities.commProfile_All));
        result.addAll(Utilities.mw().getCommunicationProfileFactory().findByName(Utilities.commProfile_ReadDemandValues));
        result.addAll(Utilities.mw().getGroupFactory().findByName(Utilities.emptyGroup));
        result.addAll(Utilities.mw().getGroupFactory().findByName(Utilities.notEmptyGroup));
        result.addAll(Utilities.mw().getUserFileFactory().findByName(Utilities.emptyUserFile));
        result.addAll(Utilities.mw().getUserFileFactory().findByName(Utilities.notEmptyUserFile));
        result.addAll(Utilities.mw().getModemPoolFactory().findByName(Utilities.dummyModemPool));
//        result.addAll(Utilities.mw().getFolderFactory().findByName(folderName));
//        result.addAll(Utilities.mw().getFolderTypeFactory().findByName(folderTypeName));


        if (result.size() > 0) {
            for (int i = 0; i < result.size(); i++) {
                try {
                    ((PersistentObject) result.get(i)).delete();
                } catch (SQLException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (BusinessException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }

    }

    @Test
    public void firmwareUpgradeConcentratorMessageTest() throws BusinessException, SQLException, IOException {
        Group gr = Utilities.createEmptyRtuGroup();

        int pendingMessageID;
        prepareConcentratorCreation();

        // find out if there is already a concentrator with the TestConcentrator name, if not, create it
        result = Utilities.mw().getDeviceFactory().findByName(testConcentrator);
        if (result.size() == 0) {
            concentrator = Utilities.createRtu(rtuTypeMeter, testname, 900);
        } else {
            concentrator = (Device) result.get(0);
        }

        if (concentrator == null) {
            fail();
        }

        System.out.println("FolderID concentrator: " + concentrator.getFolderId());

        Utilities.createCommunicationScheduler(concentrator, Utilities.commProfile_SendRtuMessage);
        if (((CommunicationScheduler) concentrator.getCommunicationSchedulers().get(0)).getCommunicationProfile().getSendRtuMessage()) {

            CommunicationProfile commProfile = ((CommunicationScheduler) concentrator.getCommunicationSchedulers().get(0)).getCommunicationProfile();
            iskraConcentrator.setCommunicationProfile(commProfile);

            RtuMessageShadow rms = new RtuMessageShadow();
            RtuMessageState rmt = RtuMessageState.PENDING;
            String contents = "<" + RtuMessageConstant.FIRMWARE + ">220TEXT</" + RtuMessageConstant.FIRMWARE + "><GroupID of meters to receive new firmware>178TEXT</GroupID of meters to receive new firmware>";
//				rms.setUserId(0);
            rms.setContents(contents);
            rms.setRtuId(concentrator.getId());
            rms.setState(rmt);
            concentrator.createMessage(rms);
            pendingMessageID = ((RtuMessage) concentrator.getPendingMessages().get(0)).getId();

            String serial = concentrator.getSerialNumber();
            Iterator i = concentrator.getPendingMessages().iterator();
            while (i.hasNext()) {
                RtuMessage msg = (RtuMessage) i.next();
                iskraConcentrator.handleConcentratorRtuMessage(concentrator, serial, msg);
            }

            RtuMessage rtum = getJustExecutedPendingMessage(Utilities.mw().getRtuMessageFactory().findByRtu(concentrator), pendingMessageID);
            assertTrue(rtum.isFailed());    // message content contains NON-numeric values

            rms.setState(rmt);
            rms.setContents("<" + RtuMessageConstant.FIRMWARE + ">220</" + RtuMessageConstant.FIRMWARE + "><GroupID of meters to receive new firmware>17800</GroupID of meters to receive new firmware>");
            concentrator.createMessage(rms);
            pendingMessageID = ((RtuMessage) concentrator.getPendingMessages().get(0)).getId();

            serial = concentrator.getSerialNumber();
            i = concentrator.getPendingMessages().iterator();
            while (i.hasNext()) {
                RtuMessage msg = (RtuMessage) i.next();
                iskraConcentrator.handleConcentratorRtuMessage(concentrator, serial, msg);
            }

            rtum = getJustExecutedPendingMessage(Utilities.mw().getRtuMessageFactory().findByRtu(concentrator), pendingMessageID);
            assertTrue(rtum.isFailed());    // there is no GroupID with the value 17800

            rms.setState(rmt);
            rms.setContents("<" + RtuMessageConstant.FIRMWARE + ">" + gr.getId() + "</" + RtuMessageConstant.FIRMWARE + "><GroupID of meters to receive new firmware>" + gr.getId() + "</GroupID of meters to receive new firmware>");
            concentrator.createMessage(rms);
            pendingMessageID = ((RtuMessage) concentrator.getPendingMessages().get(0)).getId();

            serial = concentrator.getSerialNumber();
            i = concentrator.getPendingMessages().iterator();
            while (i.hasNext()) {
                RtuMessage msg = (RtuMessage) i.next();
                iskraConcentrator.handleConcentratorRtuMessage(concentrator, serial, msg);
            }

            rtum = getJustExecutedPendingMessage(Utilities.mw().getRtuMessageFactory().findByRtu(concentrator), pendingMessageID);
            assertTrue(rtum.isFailed());     // the userfile is NOT of the type userfile

            UserFile uf = Utilities.createEmptyUserFile();
            rms.setState(rmt);
            rms.setContents("<" + RtuMessageConstant.FIRMWARE + ">" + uf.getId() + "</" + RtuMessageConstant.FIRMWARE + "><GroupID of meters to receive new firmware>" + gr.getId() + "</GroupID of meters to receive new firmware>");
            concentrator.createMessage(rms);
            pendingMessageID = ((RtuMessage) concentrator.getPendingMessages().get(0)).getId();

            serial = concentrator.getSerialNumber();
            i = concentrator.getPendingMessages().iterator();
            while (i.hasNext()) {
                RtuMessage msg = (RtuMessage) i.next();
                iskraConcentrator.handleConcentratorRtuMessage(concentrator, serial, msg);
            }

            rtum = getJustExecutedPendingMessage(Utilities.mw().getRtuMessageFactory().findByRtu(concentrator), pendingMessageID);
            assertTrue(rtum.isFailed());     // the length of the userFile is empty

            File dummyUserFile = File.createTempFile("userfile", "txt");
            FileOutputStream fos = new FileOutputStream(dummyUserFile);
            fos.write(new byte[]{1, 2, 3, 4, 5});
            fos.close();
            dummyUserFile.deleteOnExit();
            uf = Utilities.createDummyNotEmptyUserFile(dummyUserFile);

            Folder folder = (Folder) Utilities.mw().getFolderFactory().findAll().get(1);
            Group group2 = Utilities.createRtuTypeGroup();
            group2.moveToFolder(folder);
            concentrator.moveToFolder(folder);

            rms.setState(rmt);
            rms.setContents("<" + RtuMessageConstant.FIRMWARE + ">" + uf.getId() + "</" + RtuMessageConstant.FIRMWARE + "><GroupID of meters to receive new firmware>" + group2.getId() + "</GroupID of meters to receive new firmware>");
            concentrator.createMessage(rms);
            pendingMessageID = ((RtuMessage) concentrator.getPendingMessages().get(0)).getId();

            serial = concentrator.getSerialNumber();
            i = concentrator.getPendingMessages().iterator();
            while (i.hasNext()) {
                RtuMessage msg = (RtuMessage) i.next();
                iskraConcentrator.handleConcentratorRtuMessage(concentrator, serial, msg);
            }

            rtum = getJustExecutedPendingMessage(Utilities.mw().getRtuMessageFactory().findByRtu(concentrator), pendingMessageID);
            assertTrue(rtum.isConfirmed());     // the length of the userFile is empty
        }
    }

    @Test
    public void changPLCFreqMeterMessageTest() {

        try {
            // find out if there is already a meter with the TestMeter name, if not, create it
            result = Utilities.mw().getDeviceFactory().findByName(testMeter);
            if (result.size() == 0) {
                meter = Utilities.createRtu(rtuTypeMeter, testname, 900);
            } else {
                meter = (Device) result.get(0);
            }

            if (meter == null) {
                fail();
            }

            Utilities.createCommunicationScheduler(meter, Utilities.commProfile_SendRtuMessage);
            if (((CommunicationScheduler) meter.getCommunicationSchedulers().get(0)).getCommunicationProfile().getSendRtuMessage()) {

                CommunicationProfile commProfile = ((CommunicationScheduler) meter.getCommunicationSchedulers().get(0)).getCommunicationProfile();
                meterReadTransaction = new MeterReadTransaction(iskraConcentrator, null, meter.getSerialNumber(), commProfile);
                // create the rtumessage
                RtuMessageShadow rms = new RtuMessageShadow();
                RtuMessageState rmt = RtuMessageState.PENDING;
                String contents = "<changePLCFreq>4</changePLCFreq>";
//				rms.setUserId(0);
                rms.setContents(contents);
                rms.setState(rmt);
                rms.setRtuId(meter.getId());

                meter.createMessage(rms);

                meterReadTransaction.sendMeterMessages(meter, null);
            } else {
                fail();
            }

            RtuMessage rtum = (RtuMessage) (Utilities.mw().getRtuMessageFactory().findByRtu(meter).get(0));
            assertEquals(TConnection.COSEMSETREQUEST, connection.getConnectionEvents().get(0));
            assertTrue(rtum.isConfirmed());

        } catch (BusinessException e) {
            finest(e.getMessage());
            fail();
        } catch (SQLException e) {
            finest(e.getMessage());
            fail();
        } catch (NumberFormatException e) {
            finest(e.getMessage());
            fail();
        } catch (IOException e) {
            finest(e.getMessage());
            fail();
        }
    }

    @Test
    public void changPLCFreqConcentratorMessageTest() {
        try {

            int pendingMessageID = 0;

            prepareConcentratorCreation();

            // find out if there is already a concentrator with the TestConcentrator name, if not, create it
            result = Utilities.mw().getDeviceFactory().findByName(testConcentrator);
            if (result.size() == 0) {
                concentrator = Utilities.createRtu(rtuTypeMeter, testname, 900);
            } else {
                concentrator = (Device) result.get(0);
            }

            if (concentrator == null) {
                fail();
            }

            Utilities.createCommunicationScheduler(concentrator, Utilities.commProfile_SendRtuMessage);
            if (((CommunicationScheduler) concentrator.getCommunicationSchedulers().get(0)).getCommunicationProfile().getSendRtuMessage()) {

                CommunicationProfile commProfile = ((CommunicationScheduler) concentrator.getCommunicationSchedulers().get(0)).getCommunicationProfile();
                iskraConcentrator.setCommunicationProfile(commProfile);

                // create the rtumessage
                RtuMessageShadow rms = new RtuMessageShadow();
                RtuMessageState rmt = RtuMessageState.PENDING;
                String contents = "<Frequency mark>66</Frequency mark><Frequency space>75</Frequency space>";
//				rms.setUserId(0);
                rms.setContents(contents);
                rms.setRtuId(concentrator.getId());
                rms.setState(rmt);
                concentrator.createMessage(rms);
                pendingMessageID = ((RtuMessage) concentrator.getPendingMessages().get(0)).getId();

                // the response contains no DLC tag, message should fail
                connection.setByteArrayResponse(new byte[]{0x3C, 0x53, 0x74, 0x72, 0x69, 0x6E, 0x67, 0x3E, 0x48, 0x65, 0x6C, 0x6C, 0x6F, 0x20, 0x57, 0x6F, 0x72, 0x6C, 0x64, 0x21, 0x3C, 0x2F, 0x53, 0x74, 0x72, 0x69, 0x6E, 0x67, 0x3E});

                String serial = concentrator.getSerialNumber();
                Iterator i = concentrator.getPendingMessages().iterator();
                while (i.hasNext()) {
                    RtuMessage msg = (RtuMessage) i.next();
                    iskraConcentrator.handleConcentratorRtuMessage(concentrator, serial, msg);
                }

                RtuMessage rtum = getJustExecutedPendingMessage(Utilities.mw().getRtuMessageFactory().findByRtu(concentrator), pendingMessageID);
                assertTrue(rtum.isFailed());
                assertEquals(TConnection.GETFILESIZE, connection.getConnectionEvents().get(0));
                assertEquals(TConnection.DOWNLOADFILECHUNK, connection.getConnectionEvents().get(1));

                rms.setState(rmt);
                concentrator.createMessage(rms);
                pendingMessageID = ((RtuMessage) concentrator.getPendingMessages().get(0)).getId();
                // the response contains a DLC tag, message should succeed
                connection.setByteArrayResponse(new byte[]{0x3C, 0x44, 0x4C, 0x43, 0x3E, 0x48, 0x65, 0x6C, 0x6C, 0x6F, 0x20, 0x57, 0x6F, 0x72, 0x6C, 0x64, 0x21, 0x3C, 0x2F, 0x44, 0x4C, 0x43, 0x3E});

                serial = concentrator.getSerialNumber();
                i = concentrator.getPendingMessages().iterator();
                while (i.hasNext()) {
                    RtuMessage msg = (RtuMessage) i.next();
                    iskraConcentrator.handleConcentratorRtuMessage(concentrator, serial, msg);
                }

                rtum = getJustExecutedPendingMessage(Utilities.mw().getRtuMessageFactory().findByRtu(concentrator), pendingMessageID);
                assertTrue(rtum.isConfirmed());
                assertEquals(TConnection.GETFILESIZE, connection.getConnectionEvents().get(2));
                assertEquals(TConnection.DOWNLOADFILECHUNK, connection.getConnectionEvents().get(3));
                assertEquals(TConnection.UPLOADFILECHUNK, connection.getConnectionEvents().get(4));
                assertEquals(TConnection.UPLOADFILECHUNK, connection.getConnectionEvents().get(5));

                rms.setState(rmt);
                rms.setContents("<Frequency mark>66</Frequency mark><Frequency space>TEXT75</Frequency space>");
                concentrator.createMessage(rms);
                pendingMessageID = ((RtuMessage) concentrator.getPendingMessages().get(0)).getId();
                // the message should fail because the content contains a NON-numeric value

                serial = concentrator.getSerialNumber();
                i = concentrator.getPendingMessages().iterator();
                while (i.hasNext()) {
                    RtuMessage msg = (RtuMessage) i.next();
                    iskraConcentrator.handleConcentratorRtuMessage(concentrator, serial, msg);
                }

                rtum = getJustExecutedPendingMessage(Utilities.mw().getRtuMessageFactory().findByRtu(concentrator), pendingMessageID);
                assertTrue(rtum.isFailed());
                assertEquals(6, connection.getConnectionEvents().size());


            } else {
                fail();
            }


        } catch (BusinessException e) {
            finest(e.getMessage());
            fail();
        } catch (SQLException e) {
            finest(e.getMessage());
            fail();
        } catch (NumberFormatException e) {
            finest(e.getMessage());
            fail();
        }
    }

    /**
     * Return the {@link RtuMessage} with the given id
     *
     * @param findByRtu        the list to search in
     * @param pendingMessageID the ID of the RtuMessage
     * @return the RtuMessage
     */
    private RtuMessage getJustExecutedPendingMessage(List<RtuMessage> findByRtu, int pendingMessageID) {
        Iterator<RtuMessage> it = findByRtu.iterator();
        RtuMessage rm;
        while (it.hasNext()) {
            rm = it.next();
            if (rm.getId() == pendingMessageID) {
                return rm;
            }
        }
        return null;
    }

    private void prepareConcentratorCreation() throws BusinessException, SQLException {
        // find out if the communication profile exists, if not, create it
        result = Utilities.mw().getCommunicationProtocolFactory().findAll();
        for (int i = 0; i < result.size(); i++) {
            if (((CommunicationProtocol) result.get(i)).getJavaClassName().equalsIgnoreCase(jcnConcentrator)) {
                commProtMeter = (CommunicationProtocol) result.get(i);
                break;
            }
        }
        if (commProtMeter == null) {
            commProtMeter = Utilities.findOrcreateCommunicationProtocol(jcnConcentrator);
        }

        // find out if there is an rtuType defined with this testName, if not, create it
        result = Utilities.mw().getDeviceTypeFactory().findByName(testConcentrator);
        if (result.size() == 0) {
            rtuTypeMeter = Utilities.createRtuType(commProtMeter, testConcentrator, 0);
        } else {
            rtuTypeMeter = (DeviceType) result.get(0);
        }
    }

    /**
     * Log a certain stacktrace to the logger
     *
     * @param message - the message to log
     */
    private void finest(String message) {
        logger.log(Level.FINEST, message);
    }
}
