/**
 * GatewayManagerLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package com.energyict.echelon.stub;

public class GatewayManagerLocator extends org.apache.axis.client.Service implements com.energyict.echelon.stub.GatewayManager {

    public GatewayManagerLocator() {
    }


    public GatewayManagerLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public GatewayManagerLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for GatewayManagerSoap
    private java.lang.String GatewayManagerSoap_address = "http://eictapl4:81/CoreServices/GatewayManager.asmx";

    public java.lang.String getGatewayManagerSoapAddress() {
        return GatewayManagerSoap_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String GatewayManagerSoapWSDDServiceName = "GatewayManagerSoap";

    public java.lang.String getGatewayManagerSoapWSDDServiceName() {
        return GatewayManagerSoapWSDDServiceName;
    }

    public void setGatewayManagerSoapWSDDServiceName(java.lang.String name) {
        GatewayManagerSoapWSDDServiceName = name;
    }

    public com.energyict.echelon.stub.GatewayManagerSoap_PortType getGatewayManagerSoap() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(GatewayManagerSoap_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getGatewayManagerSoap(endpoint);
    }

    public com.energyict.echelon.stub.GatewayManagerSoap_PortType getGatewayManagerSoap(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            com.energyict.echelon.stub.GatewayManagerSoapStub _stub = new com.energyict.echelon.stub.GatewayManagerSoapStub(portAddress, this);
            _stub.setPortName(getGatewayManagerSoapWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setGatewayManagerSoapEndpointAddress(java.lang.String address) {
        GatewayManagerSoap_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (com.energyict.echelon.stub.GatewayManagerSoap_PortType.class.isAssignableFrom(serviceEndpointInterface)) {
                com.energyict.echelon.stub.GatewayManagerSoapStub _stub = new com.energyict.echelon.stub.GatewayManagerSoapStub(new java.net.URL(GatewayManagerSoap_address), this);
                _stub.setPortName(getGatewayManagerSoapWSDDServiceName());
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
        if ("GatewayManagerSoap".equals(inputPortName)) {
            return getGatewayManagerSoap();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://wsdl.echelon.com/Panoramix/", "GatewayManager");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://wsdl.echelon.com/Panoramix/", "GatewayManagerSoap"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("GatewayManagerSoap".equals(portName)) {
            setGatewayManagerSoapEndpointAddress(address);
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
