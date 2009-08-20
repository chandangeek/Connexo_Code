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

	abstract String getConcentratorStatus() throws ServiceException, RemoteException, IOException, BusinessException;

	abstract String getMeterStatus(String meterID) throws ServiceException, BusinessException, IOException;

	abstract String getMetersList() throws ServiceException, BusinessException, IOException;

	abstract String getConcentratorEvents(String from, String to) throws ServiceException, RemoteException, IOException, BusinessException;

	abstract String getConcentratorSystemTime() throws ServiceException, RemoteException, IOException, BusinessException;

	abstract void setConcentratorSystemTime(String time) throws ServiceException, RemoteException, IOException, BusinessException;

	abstract void timeSync() throws ServiceException, RemoteException, IOException, BusinessException;

	abstract void setMeterTariffSettings(String xml) throws ServiceException, RemoteException, IOException, BusinessException;

	abstract void setCodeRed(String startDate, UnsignedInt duration, UnsignedInt groupId) throws ServiceException, RemoteException, IOException,
	BusinessException;

	abstract String getMeterProfile(String meterID, String profileID, String registerID, String from, String to) throws ServiceException, RemoteException,
	IOException, BusinessException;

	abstract String getMeterEvents(String meterID, String from, String to) throws ServiceException, RemoteException, IOException, BusinessException;

	abstract String getMeterPowerFailures(String meterID, String from, String to) throws ServiceException, RemoteException, IOException, BusinessException;

	abstract byte[] cosemGetRequest(String meterID, String startBefore, String endBefore, String instanceId, UnsignedInt classId, UnsignedInt attributeId)
	throws ServiceException, RemoteException, IOException, BusinessException;

	abstract String getMeterOnDemandResultsList(String serial, String[] registers) throws ServiceException, RemoteException, IOException, BusinessException;

	abstract void setMeterDisconnectControl(String serial, boolean b) throws ServiceException, RemoteException, IOException, BusinessException;

	abstract void cosemSetRequest(String meterID, String startBefore, String endBefore, String instanceId, UnsignedInt classId, UnsignedInt attributeId,
			byte[] value) throws ServiceException, RemoteException, IOException, BusinessException;

	abstract void setMeterCodeRedGroupId(String meterID, UnsignedInt groupID) throws ServiceException, RemoteException, IOException, BusinessException;

	abstract void setMeterCodeRedPowerLimit(String meterID, UnsignedInt powerLimit) throws ServiceException, RemoteException, IOException, BusinessException;

	abstract UnsignedInt getMeterLoadProfilePeriod(String meterID, PeriodicProfileType profile) throws ServiceException, RemoteException, IOException,
	BusinessException;

	abstract ObjectDef[] getMeterProfileConfig(String meterID, ProfileType profile) throws ServiceException, RemoteException, IOException, BusinessException;

	abstract CosemDateTime getMeterBillingReadTime(String meterID) throws ServiceException, RemoteException, IOException, BusinessException;

	abstract int getFileSize(String fileName) throws ServiceException, BusinessException, IOException;

	abstract byte[] downloadFileChunk(String fileName, int i, int fileSize) throws ServiceException, BusinessException, IOException;

	abstract void uploadFileChunk(String fileName, int i, boolean b, byte[] data) throws ServiceException, BusinessException, IOException;

	abstract void upgradeMeters(String fileName, String[] meters) throws ServiceException, BusinessException, IOException;

	abstract void copyFile(String sourceFile, String destinationFile, boolean overwrite) throws ServiceException, BusinessException, IOException;

	abstract String getMeterResults(String meterID, String registerID, String from, String to) throws ServiceException, BusinessException, IOException;

	abstract String[] getFiles(String dir, String filter) throws ServiceException, BusinessException, IOException;
}