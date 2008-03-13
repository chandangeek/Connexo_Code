/**
 * ExpressionManagerSoap_PortType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package com.energyict.echelon.stub;

public interface ExpressionManagerSoap_PortType extends java.rmi.Remote {
    public java.lang.String createSoapDefinition(java.lang.String sKey, java.lang.String sXmlParameters) throws java.rmi.RemoteException;
    public java.lang.String createSoapDefinitionParameter(java.lang.String sKey, java.lang.String sSoapCallID, java.lang.String sXmlParameters) throws java.rmi.RemoteException;
    public java.lang.String updateSoapDefinitionParameter(java.lang.String sKey, java.lang.String sSoapCallID, java.lang.String sSoapCallParameterID, java.lang.String sXmlParameters) throws java.rmi.RemoteException;
    public java.lang.String deleteSoapDefinitionParameter(java.lang.String sKey, java.lang.String sSoapCallID, java.lang.String sSoapCallParameterID) throws java.rmi.RemoteException;
    public java.lang.String retrieveSoapDefinitionParameterList(java.lang.String sKey, java.lang.String sSoapCallID, java.lang.String sXmlParameters) throws java.rmi.RemoteException;
    public java.lang.String createFunctionDefinitionParameter(java.lang.String sKey, java.lang.String sFunctionCallID, java.lang.String sXmlParameters) throws java.rmi.RemoteException;
    public java.lang.String updateFunctionDefinitionParameter(java.lang.String sKey, java.lang.String sFunctionCallID, java.lang.String sFunctionCallParameterID, java.lang.String sXmlParameters) throws java.rmi.RemoteException;
    public java.lang.String deleteFunctionDefinitionParameter(java.lang.String sKey, java.lang.String sFunctionCallID, java.lang.String sFunctionCallParameterID) throws java.rmi.RemoteException;
    public java.lang.String retrieveFunctionDefinitionParameterList(java.lang.String sKey, java.lang.String sFunctionCallID, java.lang.String sXmlParameters) throws java.rmi.RemoteException;
    public java.lang.String createFunctionDefinition(java.lang.String sKey, java.lang.String sXmlParameters) throws java.rmi.RemoteException;
    public java.lang.String updateSoapDefinition(java.lang.String sKey, java.lang.String sSoapCallID, java.lang.String sXmlParameters) throws java.rmi.RemoteException;
    public java.lang.String updateFunctionDefinition(java.lang.String sKey, java.lang.String sFunctionCallID, java.lang.String sXmlParameters) throws java.rmi.RemoteException;
    public java.lang.String retrieveSoapDefinition(java.lang.String sKey, java.lang.String sSoapCallID) throws java.rmi.RemoteException;
    public java.lang.String retrieveFunctionDefinition(java.lang.String sKey, java.lang.String sFunctionCallID) throws java.rmi.RemoteException;
    public java.lang.String deleteSoapDefinition(java.lang.String sKey, java.lang.String sSoapCallID) throws java.rmi.RemoteException;
    public java.lang.String deleteFunctionDefinition(java.lang.String sKey, java.lang.String sFunctionCallID) throws java.rmi.RemoteException;
    public java.lang.String retrieveFunctionList(java.lang.String sKey, java.lang.String sXmlParameters) throws java.rmi.RemoteException;
    public java.lang.String retrieveSoapCallList(java.lang.String sKey, java.lang.String sXmlParameters) throws java.rmi.RemoteException;
}
