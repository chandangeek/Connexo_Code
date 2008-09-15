/**
 * WebServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package com.energyict.genericprotocolimpl.iskrap2lpc.stub;

public class WebServiceLocator extends org.apache.axis.client.Service implements com.energyict.genericprotocolimpl.iskrap2lpc.stub.WebService {

    public WebServiceLocator() {
    }


    public WebServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public WebServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for P2LPCSoapPort
    private java.lang.String P2LPCSoapPort_address = "http://127.0.0.1/WebService.wsdl";

    public java.lang.String getP2LPCSoapPortAddress() {
        return P2LPCSoapPort_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String P2LPCSoapPortWSDDServiceName = "P2LPCSoapPort";

    public java.lang.String getP2LPCSoapPortWSDDServiceName() {
        return P2LPCSoapPortWSDDServiceName;
    }

    public void setP2LPCSoapPortWSDDServiceName(java.lang.String name) {
        P2LPCSoapPortWSDDServiceName = name;
    }

    public com.energyict.genericprotocolimpl.iskrap2lpc.stub.P2LPCSoapPort_PortType getP2LPCSoapPort() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(P2LPCSoapPort_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getP2LPCSoapPort(endpoint);
    }

    public com.energyict.genericprotocolimpl.iskrap2lpc.stub.P2LPCSoapPort_PortType getP2LPCSoapPort(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            com.energyict.genericprotocolimpl.iskrap2lpc.stub.P2LPCSoapBindingStub _stub = new com.energyict.genericprotocolimpl.iskrap2lpc.stub.P2LPCSoapBindingStub(portAddress, this);
            // I manually set this timeout
            _stub.setTimeout(120000);
            _stub.setPortName(getP2LPCSoapPortWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setP2LPCSoapPortEndpointAddress(java.lang.String address) {
        P2LPCSoapPort_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (com.energyict.genericprotocolimpl.iskrap2lpc.stub.P2LPCSoapPort_PortType.class.isAssignableFrom(serviceEndpointInterface)) {
                com.energyict.genericprotocolimpl.iskrap2lpc.stub.P2LPCSoapBindingStub _stub = new com.energyict.genericprotocolimpl.iskrap2lpc.stub.P2LPCSoapBindingStub(new java.net.URL(P2LPCSoapPort_address), this);
                _stub.setPortName(getP2LPCSoapPortWSDDServiceName());
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
        if ("P2LPCSoapPort".equals(inputPortName)) {
            return getP2LPCSoapPort();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://tempuri.org/wsdl/", "WebService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://tempuri.org/wsdl/", "P2LPCSoapPort"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("P2LPCSoapPort".equals(portName)) {
            setP2LPCSoapPortEndpointAddress(address);
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
