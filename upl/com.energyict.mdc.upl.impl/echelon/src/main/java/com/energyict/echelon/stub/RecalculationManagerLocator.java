/**
 * RecalculationManagerLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package com.energyict.echelon.stub;

public class RecalculationManagerLocator extends org.apache.axis.client.Service implements com.energyict.echelon.stub.RecalculationManager {

    public RecalculationManagerLocator() {
    }


    public RecalculationManagerLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public RecalculationManagerLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for RecalculationManagerSoap
    private java.lang.String RecalculationManagerSoap_address = "http://eictapl4:81/CoreServices/RecalculationManager.asmx";

    public java.lang.String getRecalculationManagerSoapAddress() {
        return RecalculationManagerSoap_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String RecalculationManagerSoapWSDDServiceName = "RecalculationManagerSoap";

    public java.lang.String getRecalculationManagerSoapWSDDServiceName() {
        return RecalculationManagerSoapWSDDServiceName;
    }

    public void setRecalculationManagerSoapWSDDServiceName(java.lang.String name) {
        RecalculationManagerSoapWSDDServiceName = name;
    }

    public com.energyict.echelon.stub.RecalculationManagerSoap_PortType getRecalculationManagerSoap() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(RecalculationManagerSoap_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getRecalculationManagerSoap(endpoint);
    }

    public com.energyict.echelon.stub.RecalculationManagerSoap_PortType getRecalculationManagerSoap(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            com.energyict.echelon.stub.RecalculationManagerSoapStub _stub = new com.energyict.echelon.stub.RecalculationManagerSoapStub(portAddress, this);
            _stub.setPortName(getRecalculationManagerSoapWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setRecalculationManagerSoapEndpointAddress(java.lang.String address) {
        RecalculationManagerSoap_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (com.energyict.echelon.stub.RecalculationManagerSoap_PortType.class.isAssignableFrom(serviceEndpointInterface)) {
                com.energyict.echelon.stub.RecalculationManagerSoapStub _stub = new com.energyict.echelon.stub.RecalculationManagerSoapStub(new java.net.URL(RecalculationManagerSoap_address), this);
                _stub.setPortName(getRecalculationManagerSoapWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("RecalculationManagerSoap".equals(inputPortName)) {
            return getRecalculationManagerSoap();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://wsdl.echelon.com/Panoramix/", "RecalculationManager");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://wsdl.echelon.com/Panoramix/", "RecalculationManagerSoap"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("RecalculationManagerSoap".equals(portName)) {
            setRecalculationManagerSoapEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
