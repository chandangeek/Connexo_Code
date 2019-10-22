/**
 * WUTriggerServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.vodafone.gdsp.ws;

public class WUTriggerServiceLocator extends org.apache.axis.client.Service implements com.vodafone.gdsp.ws.WUTriggerService {

    public WUTriggerServiceLocator() {
    }


    public WUTriggerServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public WUTriggerServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for WUTriggerPort
    private java.lang.String WUTriggerPort_address = "http://ES1WI272:4423/SharedResources/COMM_DEVICE/WUTriggerService.serviceagent/WUTriggerPort";

    public java.lang.String getWUTriggerPortAddress() {
        return WUTriggerPort_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String WUTriggerPortWSDDServiceName = "WUTriggerPort";

    public java.lang.String getWUTriggerPortWSDDServiceName() {
        return WUTriggerPortWSDDServiceName;
    }

    public void setWUTriggerPortWSDDServiceName(java.lang.String name) {
        WUTriggerPortWSDDServiceName = name;
    }

    public com.vodafone.gdsp.ws.WUTrigger getWUTriggerPort() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(WUTriggerPort_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getWUTriggerPort(endpoint);
    }

    public com.vodafone.gdsp.ws.WUTrigger getWUTriggerPort(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            com.vodafone.gdsp.ws.WUTriggerPortBindingStub _stub = new com.vodafone.gdsp.ws.WUTriggerPortBindingStub(portAddress, this);
            _stub.setPortName(getWUTriggerPortWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setWUTriggerPortEndpointAddress(java.lang.String address) {
        WUTriggerPort_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (com.vodafone.gdsp.ws.WUTrigger.class.isAssignableFrom(serviceEndpointInterface)) {
                com.vodafone.gdsp.ws.WUTriggerPortBindingStub _stub = new com.vodafone.gdsp.ws.WUTriggerPortBindingStub(new java.net.URL(WUTriggerPort_address), this);
                _stub.setPortName(getWUTriggerPortWSDDServiceName());
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
        if ("WUTriggerPort".equals(inputPortName)) {
            return getWUTriggerPort();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://ws.gdsp.vodafone.com/", "WUTriggerService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://ws.gdsp.vodafone.com/", "WUTriggerPort"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("WUTriggerPort".equals(portName)) {
            setWUTriggerPortEndpointAddress(address);
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
