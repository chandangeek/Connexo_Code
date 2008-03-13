/**
 * UserManagerSoap_PortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package com.energyict.echelon.stub;

public interface UserManagerSoap_PortType extends java.rmi.Remote {
    public java.lang.String logout(java.lang.String sSecurityKey) throws java.rmi.RemoteException;
    public java.lang.String login(java.lang.String sUserLogin, java.lang.String sPassword, java.lang.String sAuthenticationTypeID) throws java.rmi.RemoteException;
}
