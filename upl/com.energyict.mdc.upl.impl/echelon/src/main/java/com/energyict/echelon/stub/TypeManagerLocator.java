/**
 * TypeManagerLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package com.energyict.echelon.stub;

public class TypeManagerLocator extends org.apache.axis.client.Service implements com.energyict.echelon.stub.TypeManager {

    public TypeManagerLocator() {
    }


    public TypeManagerLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public TypeManagerLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for TypeManagerSoap
    private java.lang.String TypeManagerSoap_address = "http://eictapl4:81/CoreServices/TypeManager.asmx";

    public java.lang.String getTypeManagerSoapAddress() {
        return TypeManagerSoap_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String TypeManagerSoapWSDDServiceName = "TypeManagerSoap";

    public java.lang.String getTypeManagerSoapWSDDServiceName() {
        return TypeManagerSoapWSDDServiceName;
    }

    public void setTypeManagerSoapWSDDServiceName(java.lang.String name) {
        TypeManagerSoapWSDDServiceName = name;
    }

    public com.energyict.echelon.stub.TypeManagerSoap_PortType getTypeManagerSoap() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(TypeManagerSoap_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getTypeManagerSoap(endpoint);
    }

    public com.energyict.echelon.stub.TypeManagerSoap_PortType getTypeManagerSoap(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            com.energyict.echelon.stub.TypeManagerSoapStub _stub = new com.energyict.echelon.stub.TypeManagerSoapStub(portAddress, this);
            _stub.setPortName(getTypeManagerSoapWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setTypeManagerSoapEndpointAddress(java.lang.String address) {
        TypeManagerSoap_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (com.energyict.echelon.stub.TypeManagerSoap_PortType.class.isAssignableFrom(serviceEndpointInterface)) {
                com.energyict.echelon.stub.TypeManagerSoapStub _stub = new com.energyict.echelon.stub.TypeManagerSoapStub(new java.net.URL(TypeManagerSoap_address), this);
                _stub.setPortName(getTypeManagerSoapWSDDServiceName());
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
        if ("TypeManagerSoap".equals(inputPortName)) {
            return getTypeManagerSoap();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://wsdl.echelon.com/Panoramix/", "TypeManager");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://wsdl.echelon.com/Panoramix/", "TypeManagerSoap"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("TypeManagerSoap".equals(portName)) {
            setTypeManagerSoapEndpointAddress(address);
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
