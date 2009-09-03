package com.energyict.genericprotocolimpl.iskrap2lpc;

import java.io.IOException;
import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;

import org.apache.axis.types.UnsignedInt;

import com.energyict.cbo.BusinessException;
import com.energyict.genericprotocolimpl.iskrap2lpc.stub.CosemDateTime;
import com.energyict.genericprotocolimpl.iskrap2lpc.stub.ObjectDef;
import com.energyict.genericprotocolimpl.iskrap2lpc.stub.PeriodicProfileType;
import com.energyict.genericprotocolimpl.iskrap2lpc.stub.ProfileType;
import com.energyict.protocol.ProtocolUtils;
/**
 * Basic connection implementation for retry logic
 * @author gna
 *
 */
public class RealTimeConnection implements Connection {
	
	private int timeout;
	private int retry;
	private int delayAfterRetry;
	private Concentrator concentrator;
	
	public RealTimeConnection(Concentrator concentrator){
		this(concentrator, 3, 5000);
	}
	
	public RealTimeConnection(Concentrator concentrator, int retry, int delayAfterRetry) {
		this.concentrator = concentrator;
		this.retry = retry;
		this.delayAfterRetry = delayAfterRetry;
		resetTimeOut();
	}

	private void resetTimeOut(){
		this.timeout = retry;
	}
	
	private Concentrator getConcentrator(){
		return this.concentrator;
	}
	
	/* (non-Javadoc)
	 * @see com.energyict.genericprotocolimpl.iskrap2lpc.ConnectionInterface#getConcentratorStatus()
	 */
	public String getConcentratorStatus() throws ServiceException, RemoteException, IOException, BusinessException{
		resetTimeOut();
		while(timeout > 0){
			try {
				return getConcentrator().port(getConcentrator().getConcentrator()).getConcentratorStatus();
			} catch (RemoteException e) {
				e.printStackTrace();
				checkDefaultErrors(e);
				timeout--;
				if(timeout == 0) {
					throw new RemoteException(e.getMessage());
				}
				ProtocolUtils.delayProtocol(delayAfterRetry);
			} catch (ServiceException e) {
				e.printStackTrace();
				timeout--;
				if(timeout == 0) {
					throw new ServiceException(e.getMessage());
				}
				ProtocolUtils.delayProtocol(delayAfterRetry);
			}
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see com.energyict.genericprotocolimpl.iskrap2lpc.ConnectionInterface#getMeterStatus(java.lang.String)
	 */
	public String getMeterStatus(String meterID) throws ServiceException, BusinessException, IOException{
//		resetTimeOut();
//		while(timeout > 0){
//			try {
//				return getConcentrator().port(getConcentrator().getConcentrator()).getMeterStatus(meterID);
//			} catch (RemoteException e) {
//				e.printStackTrace();
//				checkDefaultErrors(e);
//				timeout--;
//				if(timeout == 0)
//					throw new RemoteException(e.getMessage());
//				ProtocolUtils.delayProtocol(delayAfterRetry);
//			} catch (ServiceException e) {
//				e.printStackTrace();
//				timeout--;
//				if(timeout == 0)
//					throw new ServiceException(e.getMessage());
//				ProtocolUtils.delayProtocol(delayAfterRetry);
//			}
//		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see com.energyict.genericprotocolimpl.iskrap2lpc.ConnectionInterface#getMetersList()
	 */
	public String getMetersList() throws ServiceException, BusinessException, IOException{
		resetTimeOut();
		while(timeout > 0){
			try {
				return getConcentrator().port(getConcentrator().getConcentrator()).getMetersList();
			} catch (RemoteException e) {
				e.printStackTrace();
				checkDefaultErrors(e);
				timeout--;
				if(timeout == 0) {
					throw new RemoteException(e.getMessage());
				}
				ProtocolUtils.delayProtocol(delayAfterRetry);
			} catch (ServiceException e) {
				e.printStackTrace();
				timeout--;
				if(timeout == 0) {
					throw new ServiceException(e.getMessage());
				}
				ProtocolUtils.delayProtocol(delayAfterRetry);
			}
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see com.energyict.genericprotocolimpl.iskrap2lpc.ConnectionInterface#getConcentratorEvents(java.lang.String, java.lang.String)
	 */
	public String getConcentratorEvents(String from, String to) throws ServiceException, RemoteException, IOException, BusinessException{
		resetTimeOut();
		while(timeout > 0){
			try {
				return getConcentrator().port(getConcentrator().getConcentrator()).getConcentratorEvents(from, to);
			} catch (RemoteException e) {
				e.printStackTrace();
				checkDefaultErrors(e);
				timeout--;
				if(timeout == 0) {
					throw new RemoteException(e.getMessage());
				}
				ProtocolUtils.delayProtocol(delayAfterRetry);
			} catch (ServiceException e) {
				e.printStackTrace();
				timeout--;
				if(timeout == 0) {
					throw new ServiceException(e.getMessage());
				}
				ProtocolUtils.delayProtocol(delayAfterRetry);
			}
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see com.energyict.genericprotocolimpl.iskrap2lpc.ConnectionInterface#getConcentratorSystemTime()
	 */
	public String getConcentratorSystemTime() throws ServiceException, RemoteException, IOException, BusinessException{
		resetTimeOut();
		while(timeout > 0){
			try {
				return getConcentrator().port(getConcentrator().getConcentrator()).getConcentratorSystemTime();
			} catch (RemoteException e) {
				e.printStackTrace();
				checkDefaultErrors(e);
				timeout--;
				if(timeout == 0) {
					throw new RemoteException(e.getMessage());
				}
				ProtocolUtils.delayProtocol(delayAfterRetry);
			} catch (ServiceException e) {
				e.printStackTrace();
				timeout--;
				if(timeout == 0) {
					throw new ServiceException(e.getMessage());
				}
				ProtocolUtils.delayProtocol(delayAfterRetry);
			}
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see com.energyict.genericprotocolimpl.iskrap2lpc.ConnectionInterface#setConcentratorSystemTime(java.lang.String)
	 */
	public void setConcentratorSystemTime(String time) throws ServiceException, RemoteException, IOException, BusinessException{
		resetTimeOut();
		while(timeout > 0){
			try {
				getConcentrator().port(getConcentrator().getConcentrator()).setConcentratorSystemTime(time);
				timeout = 0;
				break;
			} catch (RemoteException e) {
				e.printStackTrace();
				checkDefaultErrors(e);
				timeout--;
				if(timeout == 0) {
					throw new RemoteException(e.getMessage());
				}
				ProtocolUtils.delayProtocol(delayAfterRetry);
			} catch (ServiceException e) {
				e.printStackTrace();
				timeout--;
				if(timeout == 0) {
					throw new ServiceException(e.getMessage());
				}
				ProtocolUtils.delayProtocol(delayAfterRetry);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see com.energyict.genericprotocolimpl.iskrap2lpc.ConnectionInterface#timeSync()
	 */
	public void timeSync() throws ServiceException, RemoteException, IOException, BusinessException{
		resetTimeOut();
		while(timeout > 0){
			try {
				getConcentrator().port(getConcentrator().getConcentrator()).timeSync();
				timeout = 0;
				break;
			} catch (RemoteException e) {
				e.printStackTrace();
				checkDefaultErrors(e);
				timeout--;
				if(timeout == 0) {
					throw new RemoteException(e.getMessage());
				}
				ProtocolUtils.delayProtocol(delayAfterRetry);
			} catch (ServiceException e) {
				e.printStackTrace();
				timeout--;
				if(timeout == 0) {
					throw new ServiceException(e.getMessage());
				}
				ProtocolUtils.delayProtocol(delayAfterRetry);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see com.energyict.genericprotocolimpl.iskrap2lpc.ConnectionInterface#setMeterTariffSettings(java.lang.String)
	 */
	public void setMeterTariffSettings(String xml) throws ServiceException, RemoteException, IOException, BusinessException{
		resetTimeOut();
		while(timeout > 0){
			try {
				getConcentrator().port(getConcentrator().getConcentrator()).setMeterTariffSettings(xml);
				timeout = 0;
				break;
			} catch (RemoteException e) {
				e.printStackTrace();
				checkDefaultErrors(e);
				timeout--;
				if(timeout == 0) {
					throw new RemoteException(e.getMessage());
				}
				ProtocolUtils.delayProtocol(delayAfterRetry);
			} catch (ServiceException e) {
				e.printStackTrace();
				timeout--;
				if(timeout == 0) {
					throw new ServiceException(e.getMessage());
				}
				ProtocolUtils.delayProtocol(delayAfterRetry);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see com.energyict.genericprotocolimpl.iskrap2lpc.ConnectionInterface#setCodeRed(java.lang.String, org.apache.axis.types.UnsignedInt, org.apache.axis.types.UnsignedInt)
	 */
	public void setCodeRed(String startDate, UnsignedInt duration, UnsignedInt groupId) throws ServiceException, RemoteException, IOException, BusinessException{
		resetTimeOut();
		while(timeout > 0){
			try {
				getConcentrator().port(getConcentrator().getConcentrator()).setCodeRed(startDate, duration, groupId);
				timeout = 0;
				break;
			} catch (RemoteException e) {
				e.printStackTrace();
				checkDefaultErrors(e);
				timeout--;
				if(timeout == 0) {
					throw new RemoteException(e.getMessage());
				}
				ProtocolUtils.delayProtocol(delayAfterRetry);
			} catch (ServiceException e) {
				e.printStackTrace();
				timeout--;
				if(timeout == 0) {
					throw new ServiceException(e.getMessage());
				}
				ProtocolUtils.delayProtocol(delayAfterRetry);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see com.energyict.genericprotocolimpl.iskrap2lpc.ConnectionInterface#getMeterProfile(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
	 */
	public String getMeterProfile(String meterID, String profileID, String registerID, String from, String to) throws ServiceException, RemoteException, IOException, BusinessException{
		resetTimeOut();
		while(timeout > 0){
			try {
				if(getConcentrator().getReadingsFileType() == 0){
					return getConcentrator().port(getConcentrator().getConcentrator()).getMeterProfile(meterID, profileID, registerID, from, to);
				} else if(getConcentrator().getReadingsFileType() == 1){	// get the readings from the result file
					return getConcentrator().port(getConcentrator().getConcentrator()).getMeterResults(meterID, registerID, from, to);
				}
			} catch (RemoteException e) {
				e.printStackTrace();
				checkDefaultErrors(e);
				timeout--;
				if(timeout == 0) {
					throw new RemoteException(e.getMessage());
				}
				ProtocolUtils.delayProtocol(delayAfterRetry);
			} catch (ServiceException e) {
				e.printStackTrace();
				timeout--;
				if(timeout == 0) {
					throw new ServiceException(e.getMessage());
				}
				ProtocolUtils.delayProtocol(delayAfterRetry);
			}
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see com.energyict.genericprotocolimpl.iskrap2lpc.ConnectionInterface#getMeterEvents(java.lang.String, java.lang.String, java.lang.String)
	 */
	public String getMeterEvents(String meterID, String from, String to) throws ServiceException, RemoteException, IOException, BusinessException{
		resetTimeOut();
		while(timeout > 0){
			try {
				return getConcentrator().port(getConcentrator().getConcentrator()).getMeterEvents(meterID, from, to);
			} catch (RemoteException e) {
				e.printStackTrace();
				checkDefaultErrors(e);
				timeout--;
				if(timeout == 0) {
					throw new RemoteException(e.getMessage());
				}
				ProtocolUtils.delayProtocol(delayAfterRetry);
			} catch (ServiceException e) {
				e.printStackTrace();
				timeout--;
				if(timeout == 0) {
					throw new ServiceException(e.getMessage());
				}
				ProtocolUtils.delayProtocol(delayAfterRetry);
			}
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see com.energyict.genericprotocolimpl.iskrap2lpc.ConnectionInterface#getMeterPowerFailures(java.lang.String, java.lang.String, java.lang.String)
	 */
	public String getMeterPowerFailures(String meterID, String from, String to) throws ServiceException, RemoteException, IOException, BusinessException{
		resetTimeOut();
		while(timeout > 0){
			try {
				return getConcentrator().port(getConcentrator().getConcentrator()).getMeterPowerFailures(meterID, from, to);
			} catch (RemoteException e) {
				e.printStackTrace();
				checkDefaultErrors(e);
				timeout--;
				if(timeout == 0) {
					throw new RemoteException(e.getMessage());
				}
				ProtocolUtils.delayProtocol(delayAfterRetry);
			} catch (ServiceException e) {
				e.printStackTrace();
				timeout--;
				if(timeout == 0) {
					throw new ServiceException(e.getMessage());
				}
				ProtocolUtils.delayProtocol(delayAfterRetry);
			}
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see com.energyict.genericprotocolimpl.iskrap2lpc.ConnectionInterface#cosemGetRequest(java.lang.String, java.lang.String, java.lang.String, java.lang.String, org.apache.axis.types.UnsignedInt, org.apache.axis.types.UnsignedInt)
	 */
	public byte[] cosemGetRequest(String meterID, String startBefore, String endBefore, String instanceId, UnsignedInt classId, UnsignedInt attributeId) throws ServiceException, RemoteException, IOException, BusinessException{
		resetTimeOut();
		while(timeout > 0){
			try {
				return getConcentrator().port(getConcentrator().getConcentrator()).cosemGetRequest(meterID, startBefore, endBefore, instanceId, classId, attributeId);
			} catch (RemoteException e) {
				e.printStackTrace();
				checkDefaultErrors(e);
				timeout--;
				if(timeout == 0) {
					throw new RemoteException(e.getMessage());
				}
				ProtocolUtils.delayProtocol(delayAfterRetry);
			} catch (ServiceException e) {
				e.printStackTrace();
				timeout--;
				if(timeout == 0) {
					throw new ServiceException(e.getMessage());
				}
				ProtocolUtils.delayProtocol(delayAfterRetry);
			}
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see com.energyict.genericprotocolimpl.iskrap2lpc.ConnectionInterface#getMeterOnDemandResultsList(java.lang.String, java.lang.String[])
	 */
	public String getMeterOnDemandResultsList(String serial, String[] registers) throws ServiceException, RemoteException, IOException, BusinessException{
		resetTimeOut();
		while(timeout > 0){
			try {
				return getConcentrator().port(getConcentrator().getConcentrator()).getMeterOnDemandResultsList(serial, registers);
			} catch (RemoteException e) {
				e.printStackTrace();
				checkDefaultErrors(e);
				timeout--;
				if(timeout == 0) {
					throw new RemoteException(e.getMessage());
				}
				ProtocolUtils.delayProtocol(delayAfterRetry);
			} catch (ServiceException e) {
				e.printStackTrace();
				timeout--;
				if(timeout == 0) {
					throw new ServiceException(e.getMessage());
				}
				ProtocolUtils.delayProtocol(delayAfterRetry);
			}
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see com.energyict.genericprotocolimpl.iskrap2lpc.ConnectionInterface#setMeterDisconnectControl(java.lang.String, boolean)
	 */
	public void setMeterDisconnectControl(String serial, boolean b) throws ServiceException, RemoteException, IOException, BusinessException{
		resetTimeOut();
		while(timeout > 0){
			try {
				getConcentrator().port(getConcentrator().getConcentrator()).setMeterDisconnectControl(serial, b);
				timeout = 0;
				break;
			} catch (RemoteException e) {
				e.printStackTrace();
				checkDefaultErrors(e);
				timeout--;
				if(timeout == 0) {
					throw new RemoteException(e.getMessage());
				}
				ProtocolUtils.delayProtocol(delayAfterRetry);
			} catch (ServiceException e) {
				e.printStackTrace();
				timeout--;
				if(timeout == 0) {
					throw new ServiceException(e.getMessage());
				}
				ProtocolUtils.delayProtocol(delayAfterRetry);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see com.energyict.genericprotocolimpl.iskrap2lpc.ConnectionInterface#cosemSetRequest(java.lang.String, java.lang.String, java.lang.String, java.lang.String, org.apache.axis.types.UnsignedInt, org.apache.axis.types.UnsignedInt, byte[])
	 */
	public void cosemSetRequest(String meterID, String startBefore, String endBefore, String instanceId, UnsignedInt classId, UnsignedInt attributeId, byte[] value) throws ServiceException, RemoteException, IOException, BusinessException{
		resetTimeOut();
		while(timeout > 0){
			try {
				getConcentrator().port(getConcentrator().getConcentrator()).cosemSetRequest(meterID, startBefore, endBefore, instanceId, classId, attributeId, value);
				timeout = 0;
				break;
			} catch (RemoteException e) {
				e.printStackTrace();
				checkDefaultErrors(e);
				timeout--;
				if(timeout == 0) {
					throw new RemoteException(e.getMessage());
				}
				ProtocolUtils.delayProtocol(delayAfterRetry);
			} catch (ServiceException e) {
				e.printStackTrace();
				timeout--;
				if(timeout == 0) {
					throw new ServiceException(e.getMessage());
				}
				ProtocolUtils.delayProtocol(delayAfterRetry);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see com.energyict.genericprotocolimpl.iskrap2lpc.ConnectionInterface#setMeterCodeRedGroupId(java.lang.String, org.apache.axis.types.UnsignedInt)
	 */
	public void setMeterCodeRedGroupId(String meterID, UnsignedInt groupID) throws ServiceException, RemoteException, IOException, BusinessException{
		resetTimeOut();
		while(timeout > 0){
			try {
				getConcentrator().port(getConcentrator().getConcentrator()).setMeterCodeRedGroupId(meterID, groupID);
				timeout = 0;
				break;
			} catch (RemoteException e) {
				e.printStackTrace();
				checkDefaultErrors(e);
				timeout--;
				if(timeout == 0) {
					throw new RemoteException(e.getMessage());
				}
				ProtocolUtils.delayProtocol(delayAfterRetry);
			} catch (ServiceException e) {
				e.printStackTrace();
				timeout--;
				if(timeout == 0) {
					throw new ServiceException(e.getMessage());
				}
				ProtocolUtils.delayProtocol(delayAfterRetry);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see com.energyict.genericprotocolimpl.iskrap2lpc.ConnectionInterface#setMeterCodeRedPowerLimit(java.lang.String, org.apache.axis.types.UnsignedInt)
	 */
	public void setMeterCodeRedPowerLimit(String meterID, UnsignedInt powerLimit) throws ServiceException, RemoteException, IOException, BusinessException{
		resetTimeOut();
		while(timeout > 0){
			try {
				getConcentrator().port(getConcentrator().getConcentrator()).setMeterCodeRedPowerLimit(meterID, powerLimit);
				timeout = 0;
				break;
			} catch (RemoteException e) {
				e.printStackTrace();
				checkDefaultErrors(e);
				timeout--;
				if(timeout == 0) {
					throw new RemoteException(e.getMessage());
				}
				ProtocolUtils.delayProtocol(delayAfterRetry);
			} catch (ServiceException e) {
				e.printStackTrace();
				timeout--;
				if(timeout == 0) {
					throw new ServiceException(e.getMessage());
				}
				ProtocolUtils.delayProtocol(delayAfterRetry);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see com.energyict.genericprotocolimpl.iskrap2lpc.ConnectionInterface#getMeterLoadProfilePeriod(java.lang.String, com.energyict.genericprotocolimpl.iskrap2lpc.stub.PeriodicProfileType)
	 */
	public UnsignedInt getMeterLoadProfilePeriod(String meterID, PeriodicProfileType profile) throws ServiceException, RemoteException, IOException, BusinessException{
		resetTimeOut();
		while(timeout > 0){
			try {
				return getConcentrator().port(getConcentrator().getConcentrator()).getMeterLoadProfilePeriod(meterID, profile);
			} catch (RemoteException e) {
				e.printStackTrace();
				checkDefaultErrors(e);
				timeout--;
				if(timeout == 0) {
					throw new RemoteException(e.getMessage());
				}
				ProtocolUtils.delayProtocol(delayAfterRetry);
			} catch (ServiceException e) {
				e.printStackTrace();
				timeout--;
				if(timeout == 0) {
					throw new ServiceException(e.getMessage());
				}
				ProtocolUtils.delayProtocol(delayAfterRetry);
			}
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see com.energyict.genericprotocolimpl.iskrap2lpc.ConnectionInterface#getMeterProfileConfig(java.lang.String, com.energyict.genericprotocolimpl.iskrap2lpc.stub.ProfileType)
	 */
	public ObjectDef[] getMeterProfileConfig(String meterID, ProfileType profile) throws ServiceException, RemoteException, IOException, BusinessException{
		resetTimeOut();
		while(timeout > 0){
			try {
				return getConcentrator().port(getConcentrator().getConcentrator()).getMeterProfileConfig(meterID, profile);
			} catch (RemoteException e) {
				e.printStackTrace();
				checkDefaultErrors(e);
				timeout--;
				if(timeout == 0) {
					throw new RemoteException(e.getMessage());
				}
				ProtocolUtils.delayProtocol(delayAfterRetry);
			} catch (ServiceException e) {
				e.printStackTrace();
				timeout--;
				if(timeout == 0) {
					throw new ServiceException(e.getMessage());
				}
				ProtocolUtils.delayProtocol(delayAfterRetry);
			}
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see com.energyict.genericprotocolimpl.iskrap2lpc.ConnectionInterface#getMeterBillingReadTime(java.lang.String)
	 */
	public CosemDateTime getMeterBillingReadTime(String meterID) throws ServiceException, RemoteException, IOException, BusinessException{
		resetTimeOut();
		while(timeout > 0){
			try {
				return getConcentrator().port(getConcentrator().getConcentrator()).getMeterBillingReadTime(meterID);
			} catch (RemoteException e) {
				e.printStackTrace();
				checkDefaultErrors(e);
				timeout--;
				if(timeout == 0) {
					throw new RemoteException(e.getMessage());
				}
				ProtocolUtils.delayProtocol(delayAfterRetry);
			} catch (ServiceException e) {
				e.printStackTrace();
				timeout--;
				if(timeout == 0) {
					throw new ServiceException(e.getMessage());
				}
				ProtocolUtils.delayProtocol(delayAfterRetry);
			}
		}
		return null;
	}
	
	private void checkDefaultErrors(Exception e) throws RemoteException, ServiceException, BusinessException{
		if(e.getMessage().equalsIgnoreCase("Demand reading error: Meter disconnected!")){
			if(e instanceof RemoteException){
				throw new RemoteException(e.getMessage());
			} else if (e instanceof ServiceException){
				throw new ServiceException(e.getMessage());
			}
		} else if(e.getMessage().equalsIgnoreCase("Demand reading error: Transaction error!")){
			if(e instanceof RemoteException){
				throw new RemoteException(e.getMessage());
			} else if (e instanceof ServiceException){
				throw new ServiceException(e.getMessage());
			}
		} else if(e.getMessage().equalsIgnoreCase("Demand reading error: Meter does not exists!")){
			if(e instanceof RemoteException){
				throw new RemoteException(e.getMessage());
			} else if (e instanceof ServiceException){
				throw new ServiceException(e.getMessage());
			}
		} else if(e.getMessage().equalsIgnoreCase("Demand reading error: Access failed (object-unavailable)!")){
			if(e instanceof RemoteException){
				throw new RemoteException(e.getMessage());
			} else if (e instanceof ServiceException){
				throw new ServiceException(e.getMessage());
			}
		} else if(e.getMessage().equalsIgnoreCase("Demand reading error: Unable to start demand reading (connect error)!")){
			if(e instanceof RemoteException){
				throw new RemoteException(e.getMessage());
			} else if (e instanceof ServiceException){
				throw new ServiceException(e.getMessage());
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.energyict.genericprotocolimpl.iskrap2lpc.ConnectionInterface#getFileSize(java.lang.String)
	 */
	public int getFileSize(String fileName) throws ServiceException, BusinessException, IOException {
		resetTimeOut();
		while(timeout > 0){
			try {
				return getConcentrator().port(getConcentrator().getConcentrator()).getFileSize(fileName).intValue();
			} catch (RemoteException e) {
				e.printStackTrace();
				checkDefaultErrors(e);
				timeout--;
				if(timeout == 0) {
					throw new RemoteException(e.getMessage());
				}
				ProtocolUtils.delayProtocol(delayAfterRetry);
			} catch (ServiceException e) {
				e.printStackTrace();
				timeout--;
				if(timeout == 0) {
					throw new ServiceException(e.getMessage());
				}
				ProtocolUtils.delayProtocol(delayAfterRetry);
			}
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see com.energyict.genericprotocolimpl.iskrap2lpc.ConnectionInterface#downloadFileChunk(java.lang.String, int, int)
	 */
	public byte[] downloadFileChunk(String fileName, int i, int fileSize) throws ServiceException, BusinessException, IOException {
		resetTimeOut();
		while(timeout > 0){
			try {
				return getConcentrator().port(getConcentrator().getConcentrator()).downloadFileChunk(fileName, new UnsignedInt(i), new UnsignedInt(fileSize));
			} catch (RemoteException e) {
				e.printStackTrace();
				checkDefaultErrors(e);
				timeout--;
				if(timeout == 0) {
					throw new RemoteException(e.getMessage());
				}
				ProtocolUtils.delayProtocol(delayAfterRetry);
			} catch (ServiceException e) {
				e.printStackTrace();
				timeout--;
				if(timeout == 0) {
					throw new ServiceException(e.getMessage());
				}
				ProtocolUtils.delayProtocol(delayAfterRetry);
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see com.energyict.genericprotocolimpl.iskrap2lpc.ConnectionInterface#uploadFileChunk(java.lang.String, int, boolean, byte[])
	 */
	public void uploadFileChunk(String fileName, int i, boolean b, byte[] data) throws ServiceException, BusinessException, IOException {
		resetTimeOut();
		while(timeout > 0){
			try {
				getConcentrator().port(getConcentrator().getConcentrator()).uploadFileChunk(fileName, new UnsignedInt(i), b, data);
				timeout = 0;
				break;
			} catch (RemoteException e) {
				e.printStackTrace();
				checkDefaultErrors(e);
				timeout--;
				if(timeout == 0) {
					throw new RemoteException(e.getMessage());
				}
				ProtocolUtils.delayProtocol(delayAfterRetry);
			} catch (ServiceException e) {
				e.printStackTrace();
				timeout--;
				if(timeout == 0) {
					throw new ServiceException(e.getMessage());
				}
				ProtocolUtils.delayProtocol(delayAfterRetry);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see com.energyict.genericprotocolimpl.iskrap2lpc.ConnectionInterface#upgradeMeters(java.lang.String, java.lang.String[])
	 */
	public void upgradeMeters(String fileName, String[] meters) throws ServiceException, BusinessException, IOException {
		resetTimeOut();
		while(timeout > 0){
			try {
				getConcentrator().port(getConcentrator().getConcentrator()).upgradeMeters(fileName, meters);
				timeout = 0;
				break;
			} catch (RemoteException e) {
				e.printStackTrace();
				checkDefaultErrors(e);
				timeout--;
				if(timeout == 0) {
					throw new RemoteException(e.getMessage());
				}
				ProtocolUtils.delayProtocol(delayAfterRetry);
			} catch (ServiceException e) {
				e.printStackTrace();
				timeout--;
				if(timeout == 0) {
					throw new ServiceException(e.getMessage());
				}
				ProtocolUtils.delayProtocol(delayAfterRetry);
			}
		}
	}

	public void copyFile(String sourceFile, String destinationFile,
			boolean overwrite) throws ServiceException, BusinessException,
			IOException {
		resetTimeOut();
		while(timeout > 0){
			try{
				getConcentrator().port(getConcentrator().getConcentrator()).copyFile(sourceFile, destinationFile, overwrite);
				timeout = 0;
				break;
			} catch (RemoteException e) {
				e.printStackTrace();
				checkDefaultErrors(e);
				timeout--;
				if(timeout == 0) {
					throw new RemoteException(e.getMessage());
				}
				ProtocolUtils.delayProtocol(delayAfterRetry);
			} catch (ServiceException e) {
				e.printStackTrace();
				timeout--;
				if(timeout == 0) {
					throw new ServiceException(e.getMessage());
				}
				ProtocolUtils.delayProtocol(delayAfterRetry);
			}
		}
	}

	public String getMeterResults(String meterID, String registerID, String from, String to) throws ServiceException, BusinessException, IOException {
		resetTimeOut();
		while(timeout > 0){
			try {
				return getConcentrator().port(getConcentrator().getConcentrator()).getMeterResults(meterID, registerID, from, to);
			} catch (RemoteException e) {
				e.printStackTrace();
				checkDefaultErrors(e);
				timeout--;
				if(timeout == 0) {
					throw new RemoteException(e.getMessage());
				}
				ProtocolUtils.delayProtocol(delayAfterRetry);
			} catch (ServiceException e) {
				e.printStackTrace();
				timeout--;
				if(timeout == 0) {
					throw new ServiceException(e.getMessage());
				}
				ProtocolUtils.delayProtocol(delayAfterRetry);
			}
		}
		return null;
	}

	public String[] getFiles(String dir, String filter) throws ServiceException, BusinessException, IOException {
		resetTimeOut();
		while(timeout > 0){
			try {
				return getConcentrator().port(getConcentrator().getConcentrator()).getFiles(dir, filter);
			} catch (RemoteException e) {
				e.printStackTrace();
				checkDefaultErrors(e);
				timeout--;
				if(timeout == 0) {
					throw new RemoteException(e.getMessage());
				}
				ProtocolUtils.delayProtocol(delayAfterRetry);
			} catch (ServiceException e) {
				e.printStackTrace();
				timeout--;
				if(timeout == 0) {
					throw new ServiceException(e.getMessage());
				}
				ProtocolUtils.delayProtocol(delayAfterRetry);
			}
		}
		return null;
	}
}
