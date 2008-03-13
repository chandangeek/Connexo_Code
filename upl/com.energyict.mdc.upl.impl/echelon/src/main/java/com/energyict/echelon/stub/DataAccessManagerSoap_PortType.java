/**
 * DataAccessManagerSoap_PortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package com.energyict.echelon.stub;

public interface DataAccessManagerSoap_PortType extends java.rmi.Remote {
    public java.lang.String retrieve(java.lang.String sKey, java.lang.String sDatabaseTypeID) throws java.rmi.RemoteException;
    public java.lang.String update(java.lang.String sKey, java.lang.String sDatabaseTypeID, java.lang.String sXmlParameters) throws java.rmi.RemoteException;
}
