/**
 * ExpressionManagerLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package com.energyict.echelon.stub;

public class ExpressionManagerLocator extends org.apache.axis.client.Service implements com.energyict.echelon.stub.ExpressionManager {

    public ExpressionManagerLocator() {
    }


    public ExpressionManagerLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public ExpressionManagerLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for ExpressionManagerSoap
    private java.lang.String ExpressionManagerSoap_address = "http://eictapl4:81/CoreServices/ExpressionManager.asmx";

    public java.lang.String getExpressionManagerSoapAddress() {
        return ExpressionManagerSoap_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String ExpressionManagerSoapWSDDServiceName = "ExpressionManagerSoap";

    public java.lang.String getExpressionManagerSoapWSDDServiceName() {
        return ExpressionManagerSoapWSDDServiceName;
    }

    public void setExpressionManagerSoapWSDDServiceName(java.lang.String name) {
        ExpressionManagerSoapWSDDServiceName = name;
    }

    public com.energyict.echelon.stub.ExpressionManagerSoap_PortType getExpressionManagerSoap() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(ExpressionManagerSoap_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getExpressionManagerSoap(endpoint);
    }

    public com.energyict.echelon.stub.ExpressionManagerSoap_PortType getExpressionManagerSoap(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            com.energyict.echelon.stub.ExpressionManagerSoapStub _stub = new com.energyict.echelon.stub.ExpressionManagerSoapStub(portAddress, this);
            _stub.setPortName(getExpressionManagerSoapWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setExpressionManagerSoapEndpointAddress(java.lang.String address) {
        ExpressionManagerSoap_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (com.energyict.echelon.stub.ExpressionManagerSoap_PortType.class.isAssignableFrom(serviceEndpointInterface)) {
                com.energyict.echelon.stub.ExpressionManagerSoapStub _stub = new com.energyict.echelon.stub.ExpressionManagerSoapStub(new java.net.URL(ExpressionManagerSoap_address), this);
                _stub.setPortName(getExpressionManagerSoapWSDDServiceName());
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
        if ("ExpressionManagerSoap".equals(inputPortName)) {
            return getExpressionManagerSoap();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://wsdl.echelon.com/Panoramix/", "ExpressionManager");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://wsdl.echelon.com/Panoramix/", "ExpressionManagerSoap"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("ExpressionManagerSoap".equals(portName)) {
            setExpressionManagerSoapEndpointAddress(address);
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
