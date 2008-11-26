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

public interface Connection {

	public abstract String getConcentratorStatus() throws ServiceException,
			RemoteException, IOException, BusinessException;

	public abstract String getMeterStatus(String meterID)
			throws ServiceException, BusinessException, IOException;

	public abstract String getMetersList() throws ServiceException,
			BusinessException, IOException;

	public abstract String getConcentratorEvents(String from, String to)
			throws ServiceException, RemoteException, IOException,
			BusinessException;

	public abstract String getConcentratorSystemTime() throws ServiceException,
			RemoteException, IOException, BusinessException;

	public abstract void setConcentratorSystemTime(String time)
			throws ServiceException, RemoteException, IOException,
			BusinessException;

	public abstract void timeSync() throws ServiceException, RemoteException,
			IOException, BusinessException;

	public abstract void setMeterTariffSettings(String xml)
			throws ServiceException, RemoteException, IOException,
			BusinessException;

	public abstract void setCodeRed(String startDate, UnsignedInt duration,
			UnsignedInt groupId) throws ServiceException, RemoteException,
			IOException, BusinessException;

	public abstract String getMeterProfile(String meterID, String profileID,
			String registerID, String from, String to) throws ServiceException,
			RemoteException, IOException, BusinessException;

	public abstract String getMeterEvents(String meterID, String from, String to)
			throws ServiceException, RemoteException, IOException,
			BusinessException;

	public abstract String getMeterPowerFailures(String meterID, String from,
			String to) throws ServiceException, RemoteException, IOException,
			BusinessException;

	public abstract byte[] cosemGetRequest(String meterID, String startBefore,
			String endBefore, String instanceId, UnsignedInt classId,
			UnsignedInt attributeId) throws ServiceException, RemoteException,
			IOException, BusinessException;

	public abstract String getMeterOnDemandResultsList(String serial,
			String[] registers) throws ServiceException, RemoteException,
			IOException, BusinessException;

	public abstract void setMeterDisconnectControl(String serial, boolean b)
			throws ServiceException, RemoteException, IOException,
			BusinessException;

	public abstract void cosemSetRequest(String meterID, String startBefore,
			String endBefore, String instanceId, UnsignedInt classId,
			UnsignedInt attributeId, byte[] value) throws ServiceException,
			RemoteException, IOException, BusinessException;

	public abstract void setMeterCodeRedGroupId(String meterID,
			UnsignedInt groupID) throws ServiceException, RemoteException,
			IOException, BusinessException;

	public abstract void setMeterCodeRedPowerLimit(String meterID,
			UnsignedInt powerLimit) throws ServiceException, RemoteException,
			IOException, BusinessException;

	public abstract UnsignedInt getMeterLoadProfilePeriod(String meterID,
			PeriodicProfileType profile) throws ServiceException,
			RemoteException, IOException, BusinessException;

	public abstract ObjectDef[] getMeterProfileConfig(String meterID,
			ProfileType profile) throws ServiceException, RemoteException,
			IOException, BusinessException;

	public abstract CosemDateTime getMeterBillingReadTime(String meterID)
			throws ServiceException, RemoteException, IOException,
			BusinessException;

	public abstract int getFileSize(String fileName) throws ServiceException,
			BusinessException, IOException;

	public abstract byte[] downloadFileChunk(String fileName, int i,
			int fileSize) throws ServiceException, BusinessException,
			IOException;

	public abstract void uploadFileChunk(String fileName, int i, boolean b,
			byte[] data) throws ServiceException, BusinessException,
			IOException;

	public abstract void upgradeMeters(String fileName, String[] meters)
			throws ServiceException, BusinessException, IOException;

}