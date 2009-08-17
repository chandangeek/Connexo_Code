package com.energyict.genericprotocolimpl.iskrap2lpc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.energyict.cbo.BusinessException;
import com.energyict.cpo.PersistentObject;
import com.energyict.mdw.core.CommunicationProfile;
import com.energyict.mdw.core.CommunicationProtocol;
import com.energyict.mdw.core.CommunicationScheduler;
import com.energyict.mdw.core.Group;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.core.Rtu;
import com.energyict.mdw.core.RtuMessage;
import com.energyict.mdw.core.RtuMessageState;
import com.energyict.mdw.core.RtuType;
import com.energyict.mdw.core.UserFile;
import com.energyict.mdw.shadow.RtuMessageShadow;
import com.energyict.protocolimpl.utils.Utilities;

/**
 * FIXME: Test do not run without a proper TestConnection - Adjust the test so it all works again
 * @author gna
 *
 */
public class P2LPCTest {
	
	private static Logger logger;
	private CommunicationProtocol commProtMeter = null;
	private Concentrator iskraConcentrator;
	private MeterReadTransaction meterReadTransaction;
	private Connection connection;
	private CommunicationScheduler commScheduler;
	private Rtu meter;
	private Rtu concentrator;
	private RtuType rtuTypeMeter = null;
	private String testMeter = "TestMeter";
	private String testConcentrator = "TestConcentrator";
	
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
	public void setUp() throws BusinessException, SQLException{
		iskraConcentrator = new Concentrator();
		iskraConcentrator.setLogger(logger);
		connection = new TConnection(iskraConcentrator);
		iskraConcentrator.setConnection(connection);
		meterReadTransaction = new MeterReadTransaction(iskraConcentrator, null, "12121212", null);
		
		
		// find out if the communication profile exists, if not, create it
		result = Utilities.mw().getCommunicationProtocolFactory().findAll();
		for(int i = 0; i < result.size(); i++){
			if(((CommunicationProtocol)result.get(i)).getJavaClassName().equalsIgnoreCase(jcnIskraMeter)){
				commProtMeter = (CommunicationProtocol)result.get(i);
				break;
			}
		}
		if(commProtMeter == null)
			commProtMeter = Utilities.createCommunicationProtocol(jcnIskraMeter);
		
		// find out if there is an rtuType defined with this testName, if not, create it
		result = Utilities.mw().getRtuTypeFactory().findByName(testMeter);
		if(result.size() == 0)
			rtuTypeMeter = Utilities.createRtuType(commProtMeter, testMeter, 4);
		else
			rtuTypeMeter = (RtuType)result.get(0);
	}
	
	@After
	public void tearDown() throws BusinessException, SQLException{
		// first delete all the device
		List result = Utilities.mw().getRtuFactory().findByName("12345678");
		result.addAll(Utilities.mw().getRtuTypeFactory().findByName(testMeter));
		result.addAll(Utilities.mw().getRtuTypeFactory().findByName(testConcentrator));
		result.addAll(Utilities.mw().getCommunicationProtocolFactory().findByName(jcnIskraMeter));
		result.addAll(Utilities.mw().getCommunicationProtocolFactory().findByName(jcnConcentrator));
		result.addAll(Utilities.mw().getCommunicationProfileFactory().findByName(Utilities.COMMPROFILE_SENDRTUMESSAGE));
		result.addAll(Utilities.mw().getCommunicationProfileFactory().findByName(Utilities.COMMPROFILE_ALL));
		result.addAll(Utilities.mw().getCommunicationProfileFactory().findByName(Utilities.COMMPROFILE_READDEMANDVALUES));
		result.addAll(Utilities.mw().getGroupFactory().findByName(Utilities.EMPTY_GROUP));
		result.addAll(Utilities.mw().getUserFileFactory().findByName(Utilities.EMPTY_USERFILE));
		result.addAll(Utilities.mw().getModemPoolFactory().findByName(Utilities.DUMMY_MODEMPOOL));
		
		if(result.size() > 0){
			for(int i = 0; i < result.size(); i++){
				((PersistentObject) result.get(i)).delete();
			}
		}
	}
	
	@Test
	public void firmwareUpgradeConcentratorMessageTest(){
		
		try {
			int pendingMessageID;
			prepareConcentratorCreation();
			
			// find out if there is already a concentrator with the TestConcentrator name, if not, create it
			result = Utilities.mw().getRtuFactory().findByName(testConcentrator);
			if(result.size() == 0)
				concentrator = Utilities.createRtu(rtuTypeMeter, "12345678", 900);
			else
				concentrator = (Rtu)result.get(0);
			
			if(concentrator == null)
				fail();
			
			Utilities.createCommunicationScheduler(concentrator, Utilities.COMMPROFILE_SENDRTUMESSAGE);
			if(((CommunicationScheduler)concentrator.getCommunicationSchedulers().get(0)).getCommunicationProfile().getSendRtuMessage()){
				
				CommunicationProfile commProfile = ((CommunicationScheduler)concentrator.getCommunicationSchedulers().get(0)).getCommunicationProfile();
				iskraConcentrator.setCommunicationProfile(commProfile);
				
				RtuMessageShadow rms = new RtuMessageShadow();
				RtuMessageState rmt = RtuMessageState.PENDING;
				String contents = "<UserFile ID of firmware bin file>220TEXT</UserFile ID of firmware bin file><GroupID of meters to receive new firmware>178TEXT</GroupID of meters to receive new firmware>";
				rms.setContents(contents);
				rms.setRtuId(concentrator.getId());
				rms.setState(rmt);
				concentrator.createMessage(rms);
				pendingMessageID = ((RtuMessage)concentrator.getPendingMessages().get(0)).getId();
//				iskraConcentrator.handleConcentrator(concentrator);
				
	            String serial = concentrator.getSerialNumber();
	            Iterator i = concentrator.getPendingMessages().iterator();
	            while (i.hasNext()) {
	                RtuMessage msg = (RtuMessage) i.next();
	                iskraConcentrator.handleConcentratorRtuMessage(concentrator, serial, msg);
	            }
				
				RtuMessage rtum = getJustExecutedPendingMessage(Utilities.mw().getRtuMessageFactory().findByRtu(concentrator), pendingMessageID);
				assertTrue(rtum.isFailed());	// message content contains NON-numeric values
				
				rms.setState(rmt);
				rms.setContents("<UserFile ID of firmware bin file>220</UserFile ID of firmware bin file><GroupID of meters to receive new firmware>17800</GroupID of meters to receive new firmware>");
				concentrator.createMessage(rms);
				pendingMessageID = ((RtuMessage)concentrator.getPendingMessages().get(0)).getId();
//				iskraConcentrator.handleConcentrator(concentrator);
				
				serial = concentrator.getSerialNumber();
	            i = concentrator.getPendingMessages().iterator();
	            while (i.hasNext()) {
	                RtuMessage msg = (RtuMessage) i.next();
	                iskraConcentrator.handleConcentratorRtuMessage(concentrator, serial, msg);
	            }
				
				rtum = getJustExecutedPendingMessage(Utilities.mw().getRtuMessageFactory().findByRtu(concentrator), pendingMessageID);
				assertTrue(rtum.isFailed());	// there is no GroupID with the value 17800
				
				Group gr = Utilities.createEmptyRtuGroup();
//				UserFile uf = Utilities.createEmptyUserFile();
				rms.setState(rmt);
				rms.setContents("<UserFile ID of firmware bin file>" + gr.getId() + "</UserFile ID of firmware bin file><GroupID of meters to receive new firmware>" + gr.getId() + "</GroupID of meters to receive new firmware>");
				concentrator.createMessage(rms);
				pendingMessageID = ((RtuMessage)concentrator.getPendingMessages().get(0)).getId();
//				iskraConcentrator.handleConcentrator(concentrator);
				
				serial = concentrator.getSerialNumber();
	            i = concentrator.getPendingMessages().iterator();
	            while (i.hasNext()) {
	                RtuMessage msg = (RtuMessage) i.next();
	                iskraConcentrator.handleConcentratorRtuMessage(concentrator, serial, msg);
	            }
	            
				rtum = getJustExecutedPendingMessage(Utilities.mw().getRtuMessageFactory().findByRtu(concentrator), pendingMessageID);
				assertTrue(rtum.isFailed()); 	// the userfile is NOT of the type userfile
				
				UserFile uf = Utilities.createEmptyUserFile();
				rms.setState(rmt);
				rms.setContents("<UserFile ID of firmware bin file>" + uf.getId() + "</UserFile ID of firmware bin file><GroupID of meters to receive new firmware>" + gr.getId() + "</GroupID of meters to receive new firmware>");
				concentrator.createMessage(rms);
				pendingMessageID = ((RtuMessage)concentrator.getPendingMessages().get(0)).getId();
//				iskraConcentrator.handleConcentrator(concentrator);
				
				serial = concentrator.getSerialNumber();
	            i = concentrator.getPendingMessages().iterator();
	            while (i.hasNext()) {
	                RtuMessage msg = (RtuMessage) i.next();
	                iskraConcentrator.handleConcentratorRtuMessage(concentrator, serial, msg);
	            }
	            
				rtum = getJustExecutedPendingMessage(Utilities.mw().getRtuMessageFactory().findByRtu(concentrator), pendingMessageID);
				assertTrue(rtum.isFailed()); 	// the length of the userFile is empty
				
				//TODO complete the test so the message can succeed
			}
			
			
			
		} catch (BusinessException e) {
			e.printStackTrace();
			fail();
		} catch (SQLException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	public void changPLCFreqMeterMessageTest(){
		
		try {
			// find out if there is already a meter with the TestMeter name, if not, create it
			result = Utilities.mw().getRtuFactory().findByName(testMeter);
			if(result.size() == 0)
				meter = Utilities.createRtu(rtuTypeMeter, "12345678", 900);
			else
				meter = (Rtu)result.get(0);
			
			if(meter==null)
				fail();
			
			Utilities.createCommunicationScheduler(meter, Utilities.COMMPROFILE_SENDRTUMESSAGE);
			if(((CommunicationScheduler)meter.getCommunicationSchedulers().get(0)).getCommunicationProfile().getSendRtuMessage()){

				CommunicationProfile commProfile = ((CommunicationScheduler)meter.getCommunicationSchedulers().get(0)).getCommunicationProfile();
				meterReadTransaction = new MeterReadTransaction(iskraConcentrator, null, meter.getSerialNumber(), commProfile);
				// create the rtumessage
				RtuMessageShadow rms = new RtuMessageShadow();
				RtuMessageState rmt = RtuMessageState.PENDING;
				String contents = "<changePLCFreq>4</changePLCFreq>";
				rms.setContents(contents);
				rms.setState(rmt);
				rms.setRtuId(meter.getId());
				
				meter.createMessage(rms);
				
				meterReadTransaction.sendMeterMessages(meter, null);
			} else {
				fail();
			}

			RtuMessage rtum = (RtuMessage) (Utilities.mw().getRtuMessageFactory().findByRtu(meter).get(0));
			assertEquals(TConnection.COSEMSETREQUEST, TConnection.getConnectionEvents().get(0));
			assertTrue(rtum.isConfirmed());
			
		} catch (BusinessException e) {
			e.printStackTrace();
			fail();
		} catch (SQLException e) {
			e.printStackTrace();
			fail();
		} catch (NumberFormatException e) {
			e.printStackTrace();
			fail();
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Ignore
	@Test
	public void changPLCFreqConcentratorMessageTest(){
		try {
			
			int pendingMessageID = 0;
			
			prepareConcentratorCreation();
			
			// find out if there is already a concentrator with the TestConcentrator name, if not, create it
			result = Utilities.mw().getRtuFactory().findByName(testConcentrator);
			if(result.size() == 0)
				concentrator = Utilities.createRtu(rtuTypeMeter, "12345678", 900);
			else
				concentrator = (Rtu)result.get(0);
			
			if(concentrator == null)
				fail();
			
			Utilities.createCommunicationScheduler(concentrator, Utilities.COMMPROFILE_SENDRTUMESSAGE);
			if(((CommunicationScheduler)concentrator.getCommunicationSchedulers().get(0)).getCommunicationProfile().getSendRtuMessage()){
				
				CommunicationProfile commProfile = ((CommunicationScheduler)concentrator.getCommunicationSchedulers().get(0)).getCommunicationProfile();
				iskraConcentrator.setCommunicationProfile(commProfile);
				
				// create the rtumessage
				RtuMessageShadow rms = new RtuMessageShadow();
				RtuMessageState rmt = RtuMessageState.PENDING;
				String contents = "<Frequency mark>66</Frequency mark><Frequency space>75</Frequency space>";
				rms.setContents(contents);
				rms.setRtuId(concentrator.getId());
				rms.setState(rmt);
				concentrator.createMessage(rms);
				pendingMessageID = ((RtuMessage)concentrator.getPendingMessages().get(0)).getId();
				
				// the response contains no DLC tag, message should fail
				TConnection.setByteArrayResponse(new byte[]{0x3C, 0x53, 0x74, 0x72, 0x69, 0x6E, 0x67, 0x3E, 0x48, 0x65, 0x6C, 0x6C, 0x6F, 0x20, 0x57, 0x6F, 0x72, 0x6C, 0x64, 0x21, 0x3C, 0x2F, 0x53, 0x74, 0x72, 0x69, 0x6E, 0x67, 0x3E});
//				iskraConcentrator.handleConcentrator(concentrator);
				
	            String serial = concentrator.getSerialNumber();
	            Iterator i = concentrator.getPendingMessages().iterator();
	            while (i.hasNext()) {
	                RtuMessage msg = (RtuMessage) i.next();
	                iskraConcentrator.handleConcentratorRtuMessage(concentrator, serial, msg);
	            }
	            
				RtuMessage rtum = getJustExecutedPendingMessage(Utilities.mw().getRtuMessageFactory().findByRtu(concentrator), pendingMessageID);
				assertTrue(rtum.isFailed());
				assertEquals(TConnection.GETFILESIZE, TConnection.getConnectionEvents().get(0));
				assertEquals(TConnection.DOWNLOADFILECHUNK, TConnection.getConnectionEvents().get(1));
				
				rms.setState(rmt);
				concentrator.createMessage(rms);
				pendingMessageID = ((RtuMessage)concentrator.getPendingMessages().get(0)).getId();
				// the response contains a DLC tag, message should succeed
				TConnection.setByteArrayResponse(new byte[]{0x3C, 0x44, 0x4C, 0x43, 0x3E, 0x48, 0x65, 0x6C, 0x6C, 0x6F, 0x20, 0x57, 0x6F, 0x72, 0x6C, 0x64, 0x21, 0x3C, 0x2F, 0x44, 0x4C, 0x43, 0x3E});
//				iskraConcentrator.handleConcentrator(concentrator);
				
				serial = concentrator.getSerialNumber();
	            i = concentrator.getPendingMessages().iterator();
	            while (i.hasNext()) {
	                RtuMessage msg = (RtuMessage) i.next();
	                iskraConcentrator.handleConcentratorRtuMessage(concentrator, serial, msg);
	            }
	            
				rtum = getJustExecutedPendingMessage(Utilities.mw().getRtuMessageFactory().findByRtu(concentrator), pendingMessageID);
				assertTrue(rtum.isConfirmed());
				assertEquals(TConnection.GETFILESIZE, TConnection.getConnectionEvents().get(2));
				assertEquals(TConnection.DOWNLOADFILECHUNK, TConnection.getConnectionEvents().get(3));
				assertEquals(TConnection.UPLOADFILECHUNK, TConnection.getConnectionEvents().get(4));
				assertEquals(TConnection.UPLOADFILECHUNK, TConnection.getConnectionEvents().get(5));
				
				rms.setState(rmt);
				rms.setContents("<Frequency mark>66</Frequency mark><Frequency space>TEXT75</Frequency space>");
				concentrator.createMessage(rms);
				pendingMessageID = ((RtuMessage)concentrator.getPendingMessages().get(0)).getId();
				// the message should fail because the content contains a NON-numeric value
//				iskraConcentrator.handleConcentrator(concentrator);
				
				serial = concentrator.getSerialNumber();
	            i = concentrator.getPendingMessages().iterator();
	            while (i.hasNext()) {
	                RtuMessage msg = (RtuMessage) i.next();
	                iskraConcentrator.handleConcentratorRtuMessage(concentrator, serial, msg);
	            }
	            
				rtum = getJustExecutedPendingMessage(Utilities.mw().getRtuMessageFactory().findByRtu(concentrator), pendingMessageID);
				assertTrue(rtum.isFailed());
				assertEquals(6, TConnection.getConnectionEvents().size());
				
				
			} else {
				fail();
			}
			
			
		} catch (BusinessException e) {
			e.printStackTrace();
			fail();
		} catch (SQLException e) {
			e.printStackTrace();
			fail();
		} catch (NumberFormatException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	private RtuMessage getJustExecutedPendingMessage(List<RtuMessage> findByRtu, int pendingMessageID) {
		Iterator<RtuMessage> it = findByRtu.iterator();
		RtuMessage rm;
		while(it.hasNext()){
			rm = it.next();
			if(rm.getId() == pendingMessageID)
				return rm; 
		}
		return null;
	}

	private void prepareConcentratorCreation() throws BusinessException, SQLException{
		// find out if the communication profile exists, if not, create it
		result = Utilities.mw().getCommunicationProtocolFactory().findAll();
		for(int i = 0; i < result.size(); i++){
			if(((CommunicationProtocol)result.get(i)).getJavaClassName().equalsIgnoreCase(jcnConcentrator)){
				commProtMeter = (CommunicationProtocol)result.get(i);
				break;
			}
		}
		if(commProtMeter == null)
			commProtMeter = Utilities.createCommunicationProtocol(jcnConcentrator);
		
		// find out if there is an rtuType defined with this testName, if not, create it
		result = Utilities.mw().getRtuTypeFactory().findByName(testConcentrator);
		if(result.size() == 0)
			rtuTypeMeter = Utilities.createRtuType(commProtMeter, testConcentrator, 0);
		else
			rtuTypeMeter = (RtuType)result.get(0);
	}
}
