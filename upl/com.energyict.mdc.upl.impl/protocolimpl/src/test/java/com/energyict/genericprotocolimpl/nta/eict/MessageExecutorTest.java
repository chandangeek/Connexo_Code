package com.energyict.genericprotocolimpl.nta.eict;


import com.energyict.cbo.BusinessException;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.genericprotocolimpl.nta.messagehandling.MessageExecutor;
import com.energyict.mdw.core.*;
import com.energyict.mdw.shadow.RtuMessageShadow;
import com.energyict.mdw.testutils.RtuCRUD;
import com.energyict.mdw.testutils.RtuTypeCRUD;
import com.energyict.protocolimpl.utils.DummyDLMSConnection;
import com.energyict.protocolimpl.utils.Utilities;
import org.junit.*;

import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

import static org.junit.Assert.*;

public class MessageExecutorTest {

	private static WebRTUKP webRtu;
	private static Logger logger;
	private static DummyDLMSConnection connection;
	private static int rtuMessageID = 369;
	private static int rtuId = 963;
	private static String trackingId = "testMessageTrackingId";
	private static Rtu rtu;
	private static RtuType rtuType;
	private static String rtuName = "";
	private static String rtuTypeName = "";

	private RtuMessage rtuMessage;
	private String changeLLSContent = "<Change_LLS_Secret/>";
	private String okResponse = "100042c4014200";
	private byte[] expectedRequest = DLMSUtils.hexStringToByteArray("E6E600C10181000f0000280000FF0700090c4e65774c4c53536563726574");
	private byte[] expectedRequest2 = DLMSUtils.hexStringToByteArray("E6E600C10181000f0000280000FF070009083132333435363738");

	@BeforeClass
	public static void setUpOnce() throws Exception {

		Utilities.createEnvironment();
		MeteringWarehouse.createBatchContext(false);

		logger = Logger.getLogger("global");
		webRtu = new WebRTUKP();
		connection = new DummyDLMSConnection();
		webRtu.setLogger(logger);
		webRtu.setDLMSConnection(connection);
		webRtu.setCosemObjectFactory(new CosemObjectFactory(webRtu));

		rtuTypeName = "RtuTypeName" + System.currentTimeMillis();
		rtuName = "RtuName" + System.currentTimeMillis();
		rtuType = RtuTypeCRUD.findOrCreateRtuType(rtuTypeName, 0);
		rtu = RtuCRUD.findOrCreateRtu(rtuType, rtuName, 900);
	}

	@AfterClass
	public static void tearDownOnce() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
		RtuCRUD.deleteRtu(rtuName);
		RtuTypeCRUD.deleteRtuType(rtuTypeName);
		RtuMessage rm = MeteringWarehouse.getCurrent().getRtuMessageFactory().find(rtuMessageID);
		if(rm != null){
			rm.delete();
		}
	}

	@Test
	public void changeLLSSecretTest(){
		try {
			RtuMessageShadow rms = new RtuMessageShadow();
//			rms.setUserId(0);
			rms.setId(rtuMessageID);
			rms.setContents(changeLLSContent);
			rms.setRtuId(rtu.getId());
			rms.setTrackingId(trackingId);

			rtuMessage = MeteringWarehouse.getCurrent().getRtuMessageFactory().create(rms);

			Properties props = new Properties();
			props.put("NewLLSSecret", "NewLLSSecret");
			webRtu.addProperties(props);
			connection.setResponseByte(DLMSUtils.hexStringToByteArray(okResponse));

			MessageExecutor me = new MessageExecutor(webRtu);
			me.doMessage(rtuMessage);
			assertArrayEquals(expectedRequest, connection.getSentBytes());
			assertEquals(rtuMessage.getState(), RtuMessageState.CONFIRMED);




			props = new Properties();
			props.put("NewLLSSecret", "12345678");
			webRtu.addProperties(props);
			rtuMessage.setPending();	// change it so it's not confirmed anymore

			me.doMessage(rtuMessage);
			assertArrayEquals(expectedRequest2, connection.getSentBytes());
			assertEquals(rtuMessage.getState(), RtuMessageState.CONFIRMED);

		} catch (SQLException e) {
			e.printStackTrace();
			fail();
		} catch (BusinessException e) {
			e.printStackTrace();
			fail();
		}
	}
}
