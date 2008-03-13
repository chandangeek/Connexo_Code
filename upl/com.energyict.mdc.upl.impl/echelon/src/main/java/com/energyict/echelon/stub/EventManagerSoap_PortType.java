/**
 * EventManagerSoap_PortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package com.energyict.echelon.stub;

public interface EventManagerSoap_PortType extends java.rmi.Remote {
    public java.lang.String updateDefinition(java.lang.String sKey, java.lang.String sEventDefinitionID, java.lang.String sXmlParameters) throws java.rmi.RemoteException;
    public java.lang.String retrieveDefinition(java.lang.String sKey, java.lang.String sEventDefinitionID) throws java.rmi.RemoteException;
    public java.lang.String retrieveDefinitionList(java.lang.String sKey, java.lang.String sXmlParameters) throws java.rmi.RemoteException;
    public java.lang.String retrieveHistory(java.lang.String sKey, java.lang.String sXmlParameters) throws java.rmi.RemoteException;
    public java.lang.String deleteHistoryList(java.lang.String sKey, java.lang.String sXmlParameters) throws java.rmi.RemoteException;
}
