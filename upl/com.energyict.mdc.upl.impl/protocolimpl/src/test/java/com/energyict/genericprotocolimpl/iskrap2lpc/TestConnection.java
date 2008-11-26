package com.energyict.genericprotocolimpl.iskrap2lpc;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.rpc.ServiceException;

import org.apache.axis.types.UnsignedInt;

import com.energyict.cbo.BusinessException;
import com.energyict.genericprotocolimpl.iskrap2lpc.stub.CosemDateTime;
import com.energyict.genericprotocolimpl.iskrap2lpc.stub.ObjectDef;
import com.energyict.genericprotocolimpl.iskrap2lpc.stub.PeriodicProfileType;
import com.energyict.genericprotocolimpl.iskrap2lpc.stub.ProfileType;

public class TestConnection implements Connection{
	
	private int timeout;
	private int retry;
	private int delayAfterRetry;
	private Concentrator concentrator;
	
	private static List<String> connectionEvents;
	
	public static String COSEMSETREQUEST = "CosemSetRequest";
	public static String GETFILESIZE = "GetFileSize";
	public static String DOWNLOADFILECHUNK = "DownLoadFileChunk";
	public static String UPLOADFILECHUNK = "UploadFileChunk";
	
	private static byte[] byteArrayResponse;

	// TestClass should have Zero argument constructor ...
	public TestConnection(){
	}
	
	public TestConnection(Concentrator concentrator){
		this(concentrator, 3, 5000);
	}
	
	public TestConnection(Concentrator concentrator, int retry, int delayAfterRetry){
		this.concentrator = concentrator;
		this.retry = retry;
		this.delayAfterRetry = delayAfterRetry;
		this.connectionEvents = new ArrayList<String>();
	}

	public byte[] cosemGetRequest(String meterID, String startBefore,
			String endBefore, String instanceId, UnsignedInt classId,
			UnsignedInt attributeId) throws ServiceException, RemoteException,
			IOException, BusinessException {
		// TODO Auto-generated method stub
		return null;
	}

	public void cosemSetRequest(String meterID, String startBefore,
			String endBefore, String instanceId, UnsignedInt classId,
			UnsignedInt attributeId, byte[] value) throws ServiceException,
			RemoteException, IOException, BusinessException {
		
		getConnectionEvents().add(COSEMSETREQUEST);
		
	}

	public byte[] downloadFileChunk(String fileName, int i, int fileSize)
			throws ServiceException, BusinessException, IOException {
		
		getConnectionEvents().add(DOWNLOADFILECHUNK);
//		byte[] b = new byte[]{0x3C, 0x53, 0x74, 0x72, 0x69, 0x6E, 0x67, 0x3E, 0x48, 0x65, 0x6C, 0x6C, 0x6F, 0x20, 0x57, 0x6F, 0x72, 0x6C, 0x64, 0x21, 0x3C, 0x2F, 0x53, 0x74, 0x72, 0x69, 0x6E, 0x67, 0x3E};
//		return b;
		return getByteArrayResponse();
	}

	public String getConcentratorEvents(String from, String to)
			throws ServiceException, RemoteException, IOException,
			BusinessException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getConcentratorStatus() throws ServiceException,
			RemoteException, IOException, BusinessException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getConcentratorSystemTime() throws ServiceException,
			RemoteException, IOException, BusinessException {
		// TODO Auto-generated method stub
		return null;
	}

	public int getFileSize(String fileName) throws ServiceException,
			BusinessException, IOException {

		getConnectionEvents().add(GETFILESIZE);
		
		return 0;
	}

	public CosemDateTime getMeterBillingReadTime(String meterID)
			throws ServiceException, RemoteException, IOException,
			BusinessException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getMeterEvents(String meterID, String from, String to)
			throws ServiceException, RemoteException, IOException,
			BusinessException {
		// TODO Auto-generated method stub
		return null;
	}

	public UnsignedInt getMeterLoadProfilePeriod(String meterID,
			PeriodicProfileType profile) throws ServiceException,
			RemoteException, IOException, BusinessException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getMeterOnDemandResultsList(String serial, String[] registers)
			throws ServiceException, RemoteException, IOException,
			BusinessException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getMeterPowerFailures(String meterID, String from, String to)
			throws ServiceException, RemoteException, IOException,
			BusinessException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getMeterProfile(String meterID, String profileID,
			String registerID, String from, String to) throws ServiceException,
			RemoteException, IOException, BusinessException {
		// TODO Auto-generated method stub
		return null;
	}

	public ObjectDef[] getMeterProfileConfig(String meterID, ProfileType profile)
			throws ServiceException, RemoteException, IOException,
			BusinessException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getMeterStatus(String meterID) throws ServiceException,
			BusinessException, IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getMetersList() throws ServiceException, BusinessException,
			IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public void setCodeRed(String startDate, UnsignedInt duration,
			UnsignedInt groupId) throws ServiceException, RemoteException,
			IOException, BusinessException {
		// TODO Auto-generated method stub
		
	}

	public void setConcentratorSystemTime(String time) throws ServiceException,
			RemoteException, IOException, BusinessException {
		// TODO Auto-generated method stub
		
	}

	public void setMeterCodeRedGroupId(String meterID, UnsignedInt groupID)
			throws ServiceException, RemoteException, IOException,
			BusinessException {
		// TODO Auto-generated method stub
		
	}

	public void setMeterCodeRedPowerLimit(String meterID, UnsignedInt powerLimit)
			throws ServiceException, RemoteException, IOException,
			BusinessException {
		// TODO Auto-generated method stub
		
	}

	public void setMeterDisconnectControl(String serial, boolean b)
			throws ServiceException, RemoteException, IOException,
			BusinessException {
		// TODO Auto-generated method stub
		
	}

	public void setMeterTariffSettings(String xml) throws ServiceException,
			RemoteException, IOException, BusinessException {
		// TODO Auto-generated method stub
		
	}

	public void timeSync() throws ServiceException, RemoteException,
			IOException, BusinessException {
		// TODO Auto-generated method stub
		
	}

	public void upgradeMeters(String fileName, String[] meters)
			throws ServiceException, BusinessException, IOException {
		// TODO Auto-generated method stub
		
	}

	public void uploadFileChunk(String fileName, int i, boolean b, byte[] data)
			throws ServiceException, BusinessException, IOException {
		
		getConnectionEvents().add(UPLOADFILECHUNK);
		
	}

	static public List<String> getConnectionEvents(){
		return connectionEvents;
	}
	
	static public void setByteArrayResponse(byte[] b){
		byteArrayResponse = b;
	}
	
	private byte[] getByteArrayResponse(){
		return TestConnection.byteArrayResponse;
	}
}
