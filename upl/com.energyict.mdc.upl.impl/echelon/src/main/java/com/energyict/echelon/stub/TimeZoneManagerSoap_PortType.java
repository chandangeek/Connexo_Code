/**
 * TimeZoneManagerSoap_PortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package com.energyict.echelon.stub;

public interface TimeZoneManagerSoap_PortType extends java.rmi.Remote {
    public java.lang.String retrieve(java.lang.String sKey, java.lang.String sTimeZoneID) throws java.rmi.RemoteException;
    public java.lang.String retrieveList(java.lang.String sKey) throws java.rmi.RemoteException;
    public java.lang.String convertLocalToUtcDateTime(java.lang.String sKey, java.lang.String sTimeZoneID, java.util.Calendar dtLocalDateTime) throws java.rmi.RemoteException;
    public java.lang.String convertUtcToLocalDateTime(java.lang.String sKey, java.lang.String sTimeZoneID, java.util.Calendar dtUtcDateTime) throws java.rmi.RemoteException;
}
