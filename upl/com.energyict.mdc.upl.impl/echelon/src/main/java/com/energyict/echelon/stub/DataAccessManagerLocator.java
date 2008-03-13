/**
 * DataAccessManagerLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package com.energyict.echelon.stub;

public class DataAccessManagerLocator extends org.apache.axis.client.Service implements com.energyict.echelon.stub.DataAccessManager {

    public DataAccessManagerLocator() {
    }


    public DataAccessManagerLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public DataAccessManagerLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for DataAccessManagerSoap
    private java.lang.String DataAccessManagerSoap_address = "http://eictapl4:81/CoreServices/DataAccessManager.asmx";

    public java.lang.String getDataAccessManagerSoapAddress() {
        return DataAccessManagerSoap_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String DataAccessManagerSoapWSDDServiceName = "DataAccessManagerSoap";

    public java.lang.String getDataAccessManagerSoapWSDDServiceName() {
        return DataAccessManagerSoapWSDDServiceName;
    }

    public void setDataAccessManagerSoapWSDDServiceName(java.lang.String name) {
        DataAccessManagerSoapWSDDServiceName = name;
    }

    public com.energyict.echelon.stub.DataAccessManagerSoap_PortType getDataAccessManagerSoap() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(DataAccessManagerSoap_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getDataAccessManagerSoap(endpoint);
    }

    public com.energyict.echelon.stub.DataAccessManagerSoap_PortType getDataAccessManagerSoap(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            com.energyict.echelon.stub.DataAccessManagerSoapStub _stub = new com.energyict.echelon.stub.DataAccessManagerSoapStub(portAddress, this);
            _stub.setPortName(getDataAccessManagerSoapWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setDataAccessManagerSoapEndpointAddress(java.lang.String address) {
        DataAccessManagerSoap_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (com.energyict.echelon.stub.DataAccessManagerSoap_PortType.class.isAssignableFrom(serviceEndpointInterface)) {
                com.energyict.echelon.stub.DataAccessManagerSoapStub _stub = new com.energyict.echelon.stub.DataAccessManagerSoapStub(new java.net.URL(DataAccessManagerSoap_address), this);
                _stub.setPortName(getDataAccessManagerSoapWSDDServiceName());
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
        if ("DataAccessManagerSoap".equals(inputPortName)) {
            return getDataAccessManagerSoap();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://wsdl.echelon.com/Panoramix/", "DataAccessManager");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://wsdl.echelon.com/Panoramix/", "DataAccessManagerSoap"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("DataAccessManagerSoap".equals(portName)) {
            setDataAccessManagerSoapEndpointAddress(address);
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
