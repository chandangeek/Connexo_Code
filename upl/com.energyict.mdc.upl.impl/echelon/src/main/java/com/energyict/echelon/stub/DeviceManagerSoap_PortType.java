/**
 * DeviceManagerSoap_PortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package com.energyict.echelon.stub;

public interface DeviceManagerSoap_PortType extends java.rmi.Remote {
    public java.lang.String create(java.lang.String sKey, java.lang.String sXmlParameters) throws java.rmi.RemoteException;
    public java.lang.String retrieve(java.lang.String sKey, java.lang.String sDeviceID, java.lang.String sXmlParameters) throws java.rmi.RemoteException;
    public java.lang.String retrieveByParameter(java.lang.String sKey, java.lang.String sIDTypeID, java.lang.String sID, java.lang.String sXmlParameters) throws java.rmi.RemoteException;
    public java.lang.String retrieveList(java.lang.String sKey, java.lang.String sXmlParameters) throws java.rmi.RemoteException;
    public java.lang.String update(java.lang.String sKey, java.lang.String sDeviceID, java.lang.String sXmlParameters) throws java.rmi.RemoteException;
    public java.lang.String delete(java.lang.String sKey, java.lang.String sDeviceID, java.lang.String sXmlParameters) throws java.rmi.RemoteException;
    public java.lang.String unassignAttribute(java.lang.String sKey, java.lang.String sDeviceID, java.lang.String sAttributeID) throws java.rmi.RemoteException;
    public java.lang.String assignStringAttributeValue(java.lang.String sKey, java.lang.String sDeviceID, java.lang.String sAttributeID, java.lang.String sValue) throws java.rmi.RemoteException;
    public java.lang.String assignNumericAttributeValue(java.lang.String sKey, java.lang.String sDeviceID, java.lang.String sAttributeID, double dActualValue) throws java.rmi.RemoteException;
    public java.lang.String assignDefinedAttributeValue(java.lang.String sKey, java.lang.String sDeviceID, java.lang.String sAttributeID, java.lang.String sValueID) throws java.rmi.RemoteException;
    public java.lang.String assignHierarchy(java.lang.String sKey, java.lang.String sDeviceID, java.lang.String sHierarchyLevelMemberID) throws java.rmi.RemoteException;
    public java.lang.String unassignHierarchy(java.lang.String sKey, java.lang.String sDeviceID, java.lang.String sHierarchyLevelMemberID) throws java.rmi.RemoteException;
    public java.lang.String assignDataPoints(java.lang.String sKey, java.lang.String sDeviceID, java.lang.String sXmlParameters) throws java.rmi.RemoteException;
    public java.lang.String unassignDataPoints(java.lang.String sKey, java.lang.String sDeviceID, java.lang.String sXmlParameters) throws java.rmi.RemoteException;
    public java.lang.String retrieveStatistics(java.lang.String sKey) throws java.rmi.RemoteException;
    public java.lang.String performCommand(java.lang.String sKey, java.lang.String sXmlParameters) throws java.rmi.RemoteException;
    public java.lang.String retrieveCommandHistory(java.lang.String sKey, java.lang.String sXmlParameters) throws java.rmi.RemoteException;
    public java.lang.String deleteResultList(java.lang.String sKey, java.lang.String sXmlParameters) throws java.rmi.RemoteException;
    public java.lang.String retrieveResult(java.lang.String sKey, java.lang.String sResultID) throws java.rmi.RemoteException;
    public java.lang.String retrieveResultList(java.lang.String sKey, java.lang.String sXmlParameters) throws java.rmi.RemoteException;
    public java.lang.String performCommandOnGroup(java.lang.String sKey, java.lang.String sXmlParameters) throws java.rmi.RemoteException;
    public java.lang.String updateCommand(java.lang.String sKey, java.lang.String sXmlParameters) throws java.rmi.RemoteException;
    public java.lang.String move(java.lang.String sKey, java.lang.String sXmlParameters) throws java.rmi.RemoteException;
    public java.lang.String retrieveDiscoveredList(java.lang.String sKey, java.lang.String sXmlParameters) throws java.rmi.RemoteException;
    public java.lang.String retrieveDiscoveredInformation(java.lang.String sKey, java.lang.String sIDTypeID, java.lang.String sID, java.lang.String sXmlParameters) throws java.rmi.RemoteException;
}
