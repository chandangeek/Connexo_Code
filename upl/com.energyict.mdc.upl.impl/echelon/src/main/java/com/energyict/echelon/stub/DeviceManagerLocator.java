/**
 * DeviceManagerLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package com.energyict.echelon.stub;

public class DeviceManagerLocator extends org.apache.axis.client.Service implements com.energyict.echelon.stub.DeviceManager {

    public DeviceManagerLocator() {
    }


    public DeviceManagerLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public DeviceManagerLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for DeviceManagerSoap
    private java.lang.String DeviceManagerSoap_address = "http://eictapl4:81/CoreServices/DeviceManager.asmx";

    public java.lang.String getDeviceManagerSoapAddress() {
        return DeviceManagerSoap_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String DeviceManagerSoapWSDDServiceName = "DeviceManagerSoap";

    public java.lang.String getDeviceManagerSoapWSDDServiceName() {
        return DeviceManagerSoapWSDDServiceName;
    }

    public void setDeviceManagerSoapWSDDServiceName(java.lang.String name) {
        DeviceManagerSoapWSDDServiceName = name;
    }

    public com.energyict.echelon.stub.DeviceManagerSoap_PortType getDeviceManagerSoap() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(DeviceManagerSoap_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getDeviceManagerSoap(endpoint);
    }

    public com.energyict.echelon.stub.DeviceManagerSoap_PortType getDeviceManagerSoap(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            com.energyict.echelon.stub.DeviceManagerSoapStub _stub = new com.energyict.echelon.stub.DeviceManagerSoapStub(portAddress, this);
            _stub.setPortName(getDeviceManagerSoapWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setDeviceManagerSoapEndpointAddress(java.lang.String address) {
        DeviceManagerSoap_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (com.energyict.echelon.stub.DeviceManagerSoap_PortType.class.isAssignableFrom(serviceEndpointInterface)) {
                com.energyict.echelon.stub.DeviceManagerSoapStub _stub = new com.energyict.echelon.stub.DeviceManagerSoapStub(new java.net.URL(DeviceManagerSoap_address), this);
                _stub.setPortName(getDeviceManagerSoapWSDDServiceName());
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
        if ("DeviceManagerSoap".equals(inputPortName)) {
            return getDeviceManagerSoap();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://wsdl.echelon.com/Panoramix/", "DeviceManager");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://wsdl.echelon.com/Panoramix/", "DeviceManagerSoap"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("DeviceManagerSoap".equals(portName)) {
            setDeviceManagerSoapEndpointAddress(address);
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
