/**
 * ScheduleManagerLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package com.energyict.echelon.stub;

public class ScheduleManagerLocator extends org.apache.axis.client.Service implements com.energyict.echelon.stub.ScheduleManager {

    public ScheduleManagerLocator() {
    }


    public ScheduleManagerLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public ScheduleManagerLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for ScheduleManagerSoap
    private java.lang.String ScheduleManagerSoap_address = "http://eictapl4:81/CoreServices/ScheduleManager.asmx";

    public java.lang.String getScheduleManagerSoapAddress() {
        return ScheduleManagerSoap_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String ScheduleManagerSoapWSDDServiceName = "ScheduleManagerSoap";

    public java.lang.String getScheduleManagerSoapWSDDServiceName() {
        return ScheduleManagerSoapWSDDServiceName;
    }

    public void setScheduleManagerSoapWSDDServiceName(java.lang.String name) {
        ScheduleManagerSoapWSDDServiceName = name;
    }

    public com.energyict.echelon.stub.ScheduleManagerSoap_PortType getScheduleManagerSoap() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(ScheduleManagerSoap_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getScheduleManagerSoap(endpoint);
    }

    public com.energyict.echelon.stub.ScheduleManagerSoap_PortType getScheduleManagerSoap(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            com.energyict.echelon.stub.ScheduleManagerSoapStub _stub = new com.energyict.echelon.stub.ScheduleManagerSoapStub(portAddress, this);
            _stub.setPortName(getScheduleManagerSoapWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setScheduleManagerSoapEndpointAddress(java.lang.String address) {
        ScheduleManagerSoap_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (com.energyict.echelon.stub.ScheduleManagerSoap_PortType.class.isAssignableFrom(serviceEndpointInterface)) {
                com.energyict.echelon.stub.ScheduleManagerSoapStub _stub = new com.energyict.echelon.stub.ScheduleManagerSoapStub(new java.net.URL(ScheduleManagerSoap_address), this);
                _stub.setPortName(getScheduleManagerSoapWSDDServiceName());
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
        if ("ScheduleManagerSoap".equals(inputPortName)) {
            return getScheduleManagerSoap();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://wsdl.echelon.com/Panoramix/", "ScheduleManager");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://wsdl.echelon.com/Panoramix/", "ScheduleManagerSoap"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("ScheduleManagerSoap".equals(portName)) {
            setScheduleManagerSoapEndpointAddress(address);
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
