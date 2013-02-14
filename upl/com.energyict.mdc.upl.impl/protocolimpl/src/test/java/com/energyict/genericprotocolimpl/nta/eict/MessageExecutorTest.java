package com.energyict.genericprotocolimpl.nta.eict;


import com.energyict.cbo.BusinessException;
import com.energyict.cbo.DuplicateException;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.genericprotocolimpl.nta.messagehandling.MessageExecutor;
import com.energyict.mdc.InMemoryPersistence;
import com.energyict.mdw.core.*;
import com.energyict.mdw.coreimpl.VersionedGatewayRelationTypeCreatorProvider;
import com.energyict.mdw.shadow.OldDeviceMessageShadow;
import com.energyict.protocolimpl.utils.DummyDLMSConnection;
import org.junit.*;

import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MessageExecutorTest {

	private static WebRTUKP webRtu;
	private static Logger logger;
	private static DummyDLMSConnection connection;
	private static int rtuMessageID = 369;
	private static int rtuId = 963;
	private static String trackingId = "testMessageTrackingId";
	private static Device rtu;

	private OldDeviceMessage rtuMessage;
	private String changeLLSContent = "<Change_LLS_Secret/>";
	private String okResponse = "100042c4014200";
	private byte[] expectedRequest = DLMSUtils.hexStringToByteArray("E6E600C10181000f0000280000FF0700090c4e65774c4c53536563726574");
	private byte[] expectedRequest2 = DLMSUtils.hexStringToByteArray("E6E600C10181000f0000280000FF070009083132333435363738");

	@BeforeClass
	public static void setUpOnce() throws Exception {

        InMemoryPersistence.initializeDatabase();
        InMemoryPersistence.createTablesForDeviceUsage();

        createGateWayRelationType();

		logger = Logger.getLogger("global");
		webRtu = new WebRTUKP();
		connection = new DummyDLMSConnection();
		webRtu.setLogger(logger);
		webRtu.setDLMSConnection(connection);
		webRtu.setCosemObjectFactory(new CosemObjectFactory(webRtu));

        rtu = mock(Device.class);
        when(rtu.getId()).thenReturn(rtuId);
	}

    private static void createGateWayRelationType() throws SQLException, BusinessException {
        try {
            VersionedGatewayRelationTypeCreatorProvider.instance.get().getVersionedGatewayRelationTypeCreator().createRelationType();
        } catch (DuplicateException e) {
            if(!e.getMessage().contains("A relation type with the name \"gateway\" already exists")){
                throw e;
            }
        }
    }

    @AfterClass
	public static void tearDownOnce() throws Exception {
        InMemoryPersistence.cleanUpDataBase();
	}


	@Test
	public void changeLLSSecretTest(){
		try {
			OldDeviceMessageShadow rms = new OldDeviceMessageShadow();
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
			assertEquals(rtuMessage.getState(), DeviceMessageState.CONFIRMED);

			props = new Properties();
			props.put("NewLLSSecret", "12345678");
			webRtu.addProperties(props);
			rtuMessage.setPending();	// change it so it's not confirmed anymore

			me.doMessage(rtuMessage);
			assertArrayEquals(expectedRequest2, connection.getSentBytes());
			assertEquals(rtuMessage.getState(), DeviceMessageState.CONFIRMED);

		} catch (SQLException e) {
			e.printStackTrace();
			fail();
		} catch (BusinessException e) {
			e.printStackTrace();
			fail();
		}
	}
}
