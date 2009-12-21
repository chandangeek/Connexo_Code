package com.energyict.genericprotocolimpl.iskrap2lpc;

import java.io.FileReader;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.rpc.ServiceException;

import org.apache.axis.types.UnsignedInt;

import antlr.Utils;

import com.energyict.cbo.BusinessException;
import com.energyict.genericprotocolimpl.iskrap2lpc.stub.CosemDateTime;
import com.energyict.genericprotocolimpl.iskrap2lpc.stub.ObjectDef;
import com.energyict.genericprotocolimpl.iskrap2lpc.stub.PeriodicProfileType;
import com.energyict.genericprotocolimpl.iskrap2lpc.stub.ProfileType;
import com.energyict.protocolimpl.utils.Utilities;

public class TConnection implements Connection{
	
	private int timeout;
	private int retry;
	private int delayAfterRetry;
	private Concentrator concentrator;
	
	private List<String> connectionEvents;
	
	public static String COSEMSETREQUEST = "CosemSetRequest";
	public static String GETFILESIZE = "GetFileSize";
	public static String DOWNLOADFILECHUNK = "DownLoadFileChunk";
	public static String UPLOADFILECHUNK = "UploadFileChunk";
	
	private String[] getMeterProfileTestFiles;
	private int getMeterProfileRequestCounter = 0;
	
	private byte[] byteArrayResponse;

	// TestClass should have Zero argument constructor ...
	public TConnection(){
		super();
	}
	
	public TConnection(Concentrator concentrator){
		this(concentrator, 3, 5000);
	}
	
	public TConnection(Concentrator concentrator, int retry, int delayAfterRetry){
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
	
	/**
	 * Set the testFile names
	 * @param testFiles a string of testFileNames
	 */
	protected void setMeterProfileTestFiles(String[] testFiles){
		this.getMeterProfileTestFiles = testFiles;
		this.getMeterProfileRequestCounter = 0;
	}

	/**
	 * {@inheritDoc}
	 * @return the meterProfile
	 */
	public String getMeterProfile(String meterID, String profileID,
			String registerID, String from, String to) throws ServiceException,
			RemoteException, IOException, BusinessException {
		
		FileReader inFile = new FileReader(Utils.class.getResource(this.getMeterProfileTestFiles[getMeterProfileRequestCounter++]).getFile());
		return Utilities.readWithStringBuffer(inFile);
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

	public List<String> getConnectionEvents(){
		return connectionEvents;
	}
	
	public void setByteArrayResponse(byte[] b){
		byteArrayResponse = b;
	}
	
	private byte[] getByteArrayResponse(){
		return this.byteArrayResponse;
	}

	public void copyFile(String sourceFile, String destinationFile,
			boolean overwrite) throws ServiceException, BusinessException,
			IOException {
		// TODO Auto-generated method stub
		
	}

	public String getMeterResults(String meterID, String registerID,
			String from, String to) throws ServiceException, BusinessException,
			IOException {
		// TODO Auto-generated method stub
		return null;
	}

	public String[] getFiles(String dir, String filter)
			throws ServiceException, BusinessException, IOException {
		// TODO Auto-generated method stub
		return null;
	}
}
