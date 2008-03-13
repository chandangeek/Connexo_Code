/**
 * VariableTypeManagerSoap_PortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package com.energyict.echelon.stub;

public interface VariableTypeManagerSoap_PortType extends java.rmi.Remote {
    public java.lang.String _import(java.lang.String sKey, java.lang.String sXmlVariableTypes) throws java.rmi.RemoteException;
    public java.lang.String export(java.lang.String sKey, java.lang.String sXmlParameters) throws java.rmi.RemoteException;
    public java.lang.String delete(java.lang.String sKey, java.lang.String sXmlParameters) throws java.rmi.RemoteException;
    public java.lang.String retrieveList(java.lang.String sKey) throws java.rmi.RemoteException;
    public java.lang.String retrieveComponents(java.lang.String sKey, java.lang.String sVariableTypeID) throws java.rmi.RemoteException;
}
