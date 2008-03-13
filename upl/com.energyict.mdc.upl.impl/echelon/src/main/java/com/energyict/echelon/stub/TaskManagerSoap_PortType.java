/**
 * TaskManagerSoap_PortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package com.energyict.echelon.stub;

public interface TaskManagerSoap_PortType extends java.rmi.Remote {
    public java.lang.String retrieve(java.lang.String sKey, java.lang.String sTaskID, java.lang.String sXmlParameters) throws java.rmi.RemoteException;
    public java.lang.String retrieveList(java.lang.String sKey, java.lang.String sXmlParameters) throws java.rmi.RemoteException;
    public java.lang.String reQueue(java.lang.String sKey, java.lang.String sTaskID, java.lang.String sXmlParameters) throws java.rmi.RemoteException;
    public java.lang.String delete(java.lang.String sKey, java.lang.String sTaskID) throws java.rmi.RemoteException;
    public java.lang.String retrieveGatewaysWithOpenTasks(java.lang.String sKey, java.lang.String sXmlParameters) throws java.rmi.RemoteException;
}
