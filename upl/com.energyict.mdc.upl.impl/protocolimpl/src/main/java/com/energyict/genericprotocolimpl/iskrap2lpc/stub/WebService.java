/**
 * WebService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2.1 Jun 14, 2005 (09:15:57 EDT) WSDL2Java emitter.
 */

package com.energyict.genericprotocolimpl.iskrap2lpc.stub;

public interface WebService extends javax.xml.rpc.Service {
    public java.lang.String getP2LPCSoapPortAddress();

    public com.energyict.genericprotocolimpl.iskrap2lpc.stub.P2LPCSoapPort_PortType getP2LPCSoapPort() throws javax.xml.rpc.ServiceException;

    public com.energyict.genericprotocolimpl.iskrap2lpc.stub.P2LPCSoapPort_PortType getP2LPCSoapPort(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
