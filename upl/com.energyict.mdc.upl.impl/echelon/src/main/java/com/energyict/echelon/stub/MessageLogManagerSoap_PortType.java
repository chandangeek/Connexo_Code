/**
 * MessageLogManagerSoap_PortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package com.energyict.echelon.stub;

public interface MessageLogManagerSoap_PortType extends java.rmi.Remote {
    public java.lang.String enable(java.lang.String sKey, java.lang.String sLogTypeID) throws java.rmi.RemoteException;
    public java.lang.String disable(java.lang.String sKey, java.lang.String sLogTypeID) throws java.rmi.RemoteException;
    public java.lang.String update(java.lang.String sKey, java.lang.String sLogTypeID, java.lang.String sXmlParameters) throws java.rmi.RemoteException;
    public java.lang.String retrieve(java.lang.String sKey, java.lang.String sLogTypeID) throws java.rmi.RemoteException;
    public java.lang.String isEnabled(java.lang.String sKey, java.lang.String sLogTypeID) throws java.rmi.RemoteException;
    public java.lang.String log(java.lang.String sKey, java.lang.String sXmlParameters) throws java.rmi.RemoteException;
    public java.lang.String retrieveList(java.lang.String sKey, java.lang.String sXmlParameters) throws java.rmi.RemoteException;
}
