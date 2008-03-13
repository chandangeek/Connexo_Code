/**
 * P2LPCSoapPort_PortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package com.energyict.genericprotocolimpl.iskrap2lpc.stub;

public interface P2LPCSoapPort_PortType extends java.rmi.Remote {
    public void setMeterScheduledReadTime(java.lang.String meterID, com.energyict.genericprotocolimpl.iskrap2lpc.stub.CosemDateTime time) throws java.rmi.RemoteException;
    public com.energyict.genericprotocolimpl.iskrap2lpc.stub.CosemDateTime[] getMeterScheduledReadTimes(java.lang.String meterID) throws java.rmi.RemoteException;
    public void setMeterBillingReadTime(java.lang.String meterID, com.energyict.genericprotocolimpl.iskrap2lpc.stub.CosemDateTime time) throws java.rmi.RemoteException;
    public com.energyict.genericprotocolimpl.iskrap2lpc.stub.CosemDateTime getMeterBillingReadTime(java.lang.String meterID) throws java.rmi.RemoteException;
    public void setMeterLoadProfilePeriod(java.lang.String meterID, com.energyict.genericprotocolimpl.iskrap2lpc.stub.PeriodicProfileType profile, org.apache.axis.types.UnsignedInt profilePeriod) throws java.rmi.RemoteException;
    public org.apache.axis.types.UnsignedInt getMeterLoadProfilePeriod(java.lang.String meterID, com.energyict.genericprotocolimpl.iskrap2lpc.stub.PeriodicProfileType profile) throws java.rmi.RemoteException;
    public void setMeterProfileConfig(java.lang.String meterID, com.energyict.genericprotocolimpl.iskrap2lpc.stub.ProfileType profile, com.energyict.genericprotocolimpl.iskrap2lpc.stub.ObjectDef[] profileConfiguration) throws java.rmi.RemoteException;
    public com.energyict.genericprotocolimpl.iskrap2lpc.stub.ObjectDef[] getMeterProfileConfig(java.lang.String meterID, com.energyict.genericprotocolimpl.iskrap2lpc.stub.ProfileType profile) throws java.rmi.RemoteException;
    public java.lang.String meterMbusConfiguration(java.lang.String meterID, org.apache.axis.types.UnsignedInt channel, java.lang.String method, java.lang.String parameters) throws java.rmi.RemoteException;
    public void clearMeterScheduledReadTimes(java.lang.String meterID) throws java.rmi.RemoteException;
    public void setMeterPowerLimit(java.lang.String meterID, com.energyict.genericprotocolimpl.iskrap2lpc.stub.PowerLimits powerLimit) throws java.rmi.RemoteException;
    public com.energyict.genericprotocolimpl.iskrap2lpc.stub.PowerLimits getMeterPowerLimit(java.lang.String meterID) throws java.rmi.RemoteException;
    public org.apache.axis.types.UnsignedInt getMeterCodeRedGroupId(java.lang.String meterID) throws java.rmi.RemoteException;
    public void setMeterCodeRedGroupId(java.lang.String meterID, org.apache.axis.types.UnsignedInt groupID) throws java.rmi.RemoteException;
    public void setMeterDisplayList(java.lang.String meterID, com.energyict.genericprotocolimpl.iskrap2lpc.stub.DisplayListType listType, com.energyict.genericprotocolimpl.iskrap2lpc.stub.ObjectDef[] displayList) throws java.rmi.RemoteException;
    public com.energyict.genericprotocolimpl.iskrap2lpc.stub.ObjectDef[] getMeterDisplayList(java.lang.String meterID, com.energyict.genericprotocolimpl.iskrap2lpc.stub.DisplayListType listType) throws java.rmi.RemoteException;
    public void setExcludeIncludeMeterList(com.energyict.genericprotocolimpl.iskrap2lpc.stub.IncludeExcludeMeterList includeExcludeList) throws java.rmi.RemoteException;
    public void addToExcludeIncludeMeterList(com.energyict.genericprotocolimpl.iskrap2lpc.stub.IncludeExcludeMeterList includeExcludeList) throws java.rmi.RemoteException;
    public void removeFromExcludeIncludeMeterList(com.energyict.genericprotocolimpl.iskrap2lpc.stub.IncludeExcludeMeterList includeExcludeList) throws java.rmi.RemoteException;
    public com.energyict.genericprotocolimpl.iskrap2lpc.stub.IncludeExcludeMeterList getExcludeIncludeMeterList() throws java.rmi.RemoteException;
    public void cosemActionRequest(java.lang.String meterID, java.lang.String startBefore, java.lang.String endBefore, java.lang.String instanceId, org.apache.axis.types.UnsignedInt classId, org.apache.axis.types.UnsignedInt methodId, byte[] parameters) throws java.rmi.RemoteException;
    public void cosemSetRequest(java.lang.String meterID, java.lang.String startBefore, java.lang.String endBefore, java.lang.String instanceId, org.apache.axis.types.UnsignedInt classId, org.apache.axis.types.UnsignedInt attributeId, byte[] value) throws java.rmi.RemoteException;
    public byte[] cosemGetRequestEx(java.lang.String meterID, java.lang.String startBefore, java.lang.String endBefore, java.lang.String instanceId, org.apache.axis.types.UnsignedInt classId, org.apache.axis.types.UnsignedInt attributeId, java.lang.String from, java.lang.String to) throws java.rmi.RemoteException;
    public byte[] cosemGetRequest(java.lang.String meterID, java.lang.String startBefore, java.lang.String endBefore, java.lang.String instanceId, org.apache.axis.types.UnsignedInt classId, org.apache.axis.types.UnsignedInt attributeId) throws java.rmi.RemoteException;
    public void executeAction(org.apache.axis.types.UnsignedInt action) throws java.rmi.RemoteException;
    public com.energyict.genericprotocolimpl.iskrap2lpc.stub.CodeRedStatus getMeterCodeRedStatus(java.lang.String meterID) throws java.rmi.RemoteException;
    public org.apache.axis.types.UnsignedInt getMeterCodeRedPowerLimit(java.lang.String meterID) throws java.rmi.RemoteException;
    public void setMeterCodeRedPowerLimit(java.lang.String meterID, org.apache.axis.types.UnsignedInt powerLimit) throws java.rmi.RemoteException;
    public java.lang.String getConcentratorConfigId() throws java.rmi.RemoteException;
    public void deleteFile(java.lang.String fileName) throws java.rmi.RemoteException;
    public byte[] downloadFileChunk(java.lang.String fileName, org.apache.axis.types.UnsignedInt position, org.apache.axis.types.UnsignedInt length) throws java.rmi.RemoteException;
    public void uploadFileChunk(java.lang.String fileName, org.apache.axis.types.UnsignedInt position, boolean endOfFile, byte[] data) throws java.rmi.RemoteException;
    public org.apache.axis.types.UnsignedInt getFileSize(java.lang.String fileName) throws java.rmi.RemoteException;
    public java.lang.String[] getFiles(java.lang.String path, java.lang.String filter) throws java.rmi.RemoteException;
    public java.lang.String[] getDirectories(java.lang.String path, java.lang.String filter) throws java.rmi.RemoteException;
    public void upgradeMeters(java.lang.String fileName) throws java.rmi.RemoteException;
    public void timeSync() throws java.rmi.RemoteException;
    public void setCodeRed(java.lang.String startTime, org.apache.axis.types.UnsignedInt duration, org.apache.axis.types.UnsignedInt groupId) throws java.rmi.RemoteException;
    public void getMeterPurchaseTransactions(java.lang.String meterID, javax.xml.rpc.holders.StringHolder transactionList) throws java.rmi.RemoteException;
    public void setMeterPurchase(java.lang.String meterID, java.lang.String transaction, java.lang.String value) throws java.rmi.RemoteException;
    public void setMeterEncryptionKey(java.lang.String meterID, org.apache.axis.types.UnsignedInt keyOperation, java.lang.String keyToken) throws java.rmi.RemoteException;
    public void setAuthenticationKey(java.lang.String userID, org.apache.axis.types.UnsignedInt keyOperation, java.lang.String keyToken) throws java.rmi.RemoteException;
    public void setMeterAuthenticationKey(java.lang.String meterID, org.apache.axis.types.UnsignedInt keyOperation, java.lang.String keyToken) throws java.rmi.RemoteException;
    public void setTransportKey(java.lang.String keyToken) throws java.rmi.RemoteException;
    public void setMeterDisconnectControl(java.lang.String meterID, boolean controlState) throws java.rmi.RemoteException;
    public boolean getMeterDisconnectControl(java.lang.String meterID) throws java.rmi.RemoteException;
    public void setMeterServiceControl(java.lang.String meterID, boolean controlState) throws java.rmi.RemoteException;
    public boolean getMeterServiceControl(java.lang.String meterID) throws java.rmi.RemoteException;
    public void setMeterLoadControl(java.lang.String meterID, boolean controlState) throws java.rmi.RemoteException;
    public boolean getMeterLoadControl(java.lang.String meterID) throws java.rmi.RemoteException;
    public java.lang.String getMetersListByGroup(java.lang.String group) throws java.rmi.RemoteException;
    public java.lang.String getGroups() throws java.rmi.RemoteException;
    public java.lang.String getVersionInfo() throws java.rmi.RemoteException;
    public int setConcentratorSystemTime(java.lang.String concentratorSystemTime) throws java.rmi.RemoteException;
    public java.lang.String getConcentratorSystemTime() throws java.rmi.RemoteException;
    public java.lang.String getConcentratorEvents(java.lang.String from, java.lang.String to) throws java.rmi.RemoteException;
    public java.lang.String getConcentratorStatus() throws java.rmi.RemoteException;
    public java.lang.String getConcentratorResults(java.lang.String registerID, java.lang.String from, java.lang.String to) throws java.rmi.RemoteException;
    public java.lang.String getMeterOnDemandResults(java.lang.String meterID, java.lang.String registerID) throws java.rmi.RemoteException;
    public java.lang.String getMeterOnDemandResultsList(java.lang.String meterID, java.lang.String[] registerID) throws java.rmi.RemoteException;
    public java.lang.String getMeterPowerFailures(java.lang.String meterID, java.lang.String from, java.lang.String to) throws java.rmi.RemoteException;
    public java.lang.String getMeterEvents(java.lang.String meterID, java.lang.String from, java.lang.String to) throws java.rmi.RemoteException;
    public void setMeterTariffSettings(java.lang.String meterTariffSettingsXml) throws java.rmi.RemoteException;
    public java.lang.String getMeterTariffSettings(java.lang.String meterID) throws java.rmi.RemoteException;
    public java.lang.String getMeterStatus(java.lang.String meterID) throws java.rmi.RemoteException;
    public java.lang.String getMeterProfile(java.lang.String meterID, java.lang.String profileID, java.lang.String registerID, java.lang.String from, java.lang.String to) throws java.rmi.RemoteException;
    public java.lang.String getMeterResults(java.lang.String meterID, java.lang.String registerID, java.lang.String from, java.lang.String to) throws java.rmi.RemoteException;
    public java.lang.String getMetersList() throws java.rmi.RemoteException;
}
