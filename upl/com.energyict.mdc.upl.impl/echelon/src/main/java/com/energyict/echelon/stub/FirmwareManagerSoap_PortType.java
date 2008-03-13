/**
 * FirmwareManagerSoap_PortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package com.energyict.echelon.stub;

public interface FirmwareManagerSoap_PortType extends java.rmi.Remote {
    public java.lang.String create(java.lang.String sKey, java.lang.String sXmlParameters, byte[] ayFirmware) throws java.rmi.RemoteException;
    public java.lang.String createWithTwoImages(java.lang.String sKey, java.lang.String sXmlParameters, byte[] ayFirmwareImageBank0, byte[] ayFirmwareImageBank1) throws java.rmi.RemoteException;
    public java.lang.String delete(java.lang.String sKey, java.lang.String sFirmwareVersionID) throws java.rmi.RemoteException;
    public java.lang.String retrieveList(java.lang.String sKey, java.lang.String sXmlParameters) throws java.rmi.RemoteException;
}
