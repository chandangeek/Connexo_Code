/**
 * DataPointManagerSoap_PortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package com.energyict.echelon.stub;

public interface DataPointManagerSoap_PortType extends java.rmi.Remote {
    public java.lang.String create(java.lang.String sKey, java.lang.String sXmlParameters) throws java.rmi.RemoteException;
    public java.lang.String update(java.lang.String sKey, java.lang.String sDataPointID, java.lang.String sXmlParameters) throws java.rmi.RemoteException;
    public java.lang.String delete(java.lang.String sKey, java.lang.String sDataPointID) throws java.rmi.RemoteException;
    public java.lang.String enable(java.lang.String sKey, java.lang.String sDataPointID) throws java.rmi.RemoteException;
    public java.lang.String disable(java.lang.String sKey, java.lang.String sDataPointID) throws java.rmi.RemoteException;
    public java.lang.String retrieve(java.lang.String sKey, java.lang.String sDataPointID, java.lang.String sXmlParameters) throws java.rmi.RemoteException;
    public java.lang.String retrieveList(java.lang.String sKey, java.lang.String sXmlParameters) throws java.rmi.RemoteException;
    public java.lang.String assignHierarchy(java.lang.String sKey, java.lang.String sDataPointID, java.lang.String sHierarchyLevelMemberID) throws java.rmi.RemoteException;
    public java.lang.String unassignHierarchy(java.lang.String sKey, java.lang.String sDataPointID, java.lang.String sHierarchyLevelMemberID) throws java.rmi.RemoteException;
    public java.lang.String retrieveListWithHierarchyLevelMember(java.lang.String sKey, java.lang.String sHierarchyLevelMemberID) throws java.rmi.RemoteException;
    public java.lang.String assignDefinedAttributeValue(java.lang.String sKey, java.lang.String sDataPointID, java.lang.String sAttributeID, java.lang.String sValueID) throws java.rmi.RemoteException;
    public java.lang.String assignNumericAttributeValue(java.lang.String sKey, java.lang.String sDataPointID, java.lang.String sAttributeID, double dValue) throws java.rmi.RemoteException;
    public java.lang.String assignStringAttributeValue(java.lang.String sKey, java.lang.String sDataPointID, java.lang.String sAttributeID, java.lang.String sValue) throws java.rmi.RemoteException;
    public java.lang.String retrieveListWithAttribute(java.lang.String sKey, java.lang.String sAttributeID) throws java.rmi.RemoteException;
    public java.lang.String retrieveListWithDefinedAttributeValue(java.lang.String sKey, java.lang.String sAttributeID, java.lang.String sValueID) throws java.rmi.RemoteException;
    public java.lang.String retrieveListWithNumericAttributeValue(java.lang.String sKey, java.lang.String sAttributeID, double dValue) throws java.rmi.RemoteException;
    public java.lang.String retrieveListWithStringAttributeValue(java.lang.String sKey, java.lang.String sAttributeID, java.lang.String sValue) throws java.rmi.RemoteException;
    public java.lang.String unassignAttribute(java.lang.String sKey, java.lang.String sDataPointID, java.lang.String sAttributeID) throws java.rmi.RemoteException;
    public java.lang.String retrieveDataPointValueList(java.lang.String sKey, java.lang.String sXmlParameters) throws java.rmi.RemoteException;
    public java.lang.String deleteDataPointValueList(java.lang.String sKey, java.lang.String sDataPointID, java.lang.String sXmlParameters) throws java.rmi.RemoteException;
    public java.lang.String retrieveTodaysScheduledTaskList(java.lang.String sKey, java.lang.String sXmlParameters) throws java.rmi.RemoteException;
    public java.lang.String retrieveSoapDefinitionDataPointList(java.lang.String sKey, java.lang.String sSoapCallID) throws java.rmi.RemoteException;
    public java.lang.String retrieveFunctionDefinitionDataPointList(java.lang.String sKey, java.lang.String sFunctionID) throws java.rmi.RemoteException;
    public java.lang.String retrieveStatistics(java.lang.String sKey) throws java.rmi.RemoteException;
    public java.lang.String deleteSchedule(java.lang.String sKey, java.lang.String sDataPointID) throws java.rmi.RemoteException;
    public java.lang.String retrieveDataPointAndValueList(java.lang.String sKey, java.lang.String sXmlParameters) throws java.rmi.RemoteException;
    public java.lang.String assignSchedule(java.lang.String sKey, java.lang.String sDataPointID, java.lang.String sScheduleID, java.lang.String sXmlParameters) throws java.rmi.RemoteException;
    public java.lang.String unassignSchedule(java.lang.String sKey, java.lang.String sDataPointID, java.lang.String sScheduleID, java.lang.String sXmlParameters) throws java.rmi.RemoteException;
    public java.lang.String retrieveScheduledOccurrenceList(java.lang.String sKey, java.lang.String sXmlParameters) throws java.rmi.RemoteException;
}
