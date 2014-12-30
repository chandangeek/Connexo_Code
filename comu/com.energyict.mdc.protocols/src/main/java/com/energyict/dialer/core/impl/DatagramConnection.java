package com.energyict.dialer.core.impl;

import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.protocol.api.dialer.core.UDPSession;

import com.energyict.protocols.mdc.services.impl.EnvironmentPropertyService;

import java.io.IOException;

/**
 * @author Koen
 */
public class DatagramConnection extends StreamPortConnection {

    public DatagramConnection(UDPSession udpSession, EnvironmentPropertyService propertyService) {
        super(udpSession, propertyService);
    }

    @Override
    protected void doServerOpen() throws NestedIOException {
        doOpen();
    }

    @Override
    protected void doOpen() throws NestedIOException {
        if (!boolOpen) {
            try {
                if (udpSession == null) // KV 03102005
                //udpSession = new UDPSession(...);
                {
                    throw new IOException("DatagramConnection, doOpen, no UDB (catagram) client implemented yet!");
                }

                setStreams(udpSession.getInputStream(), udpSession.getOutputStream());
                if (strComPort != null) {
                    openVirtual();
                }
                boolOpen = true;
            } catch (NestedIOException e) {
                try {

                    inputStream.close();
                    outputStream.close();
                } catch (IOException ex) {
                    throw new NestedIOException(ex);
                }
                udpSession.close();
                throw e;
            } catch (Exception e) {
                try {
                    inputStream.close();
                    outputStream.close();
                } catch (IOException ex) {
                    throw new NestedIOException(ex);
                }
                udpSession.close();
                throw new NestedIOException(e);
            }
        } else {

            if ((strComPort != null) && (!boolVirtualOpen)) {
                try {
                    openVirtual();
                } catch (IOException e) {
                    throw new NestedIOException(e);
                }
            } else
            //System.out.println("DatagramConnection, doOpen(), session for "+getUdpSession().getSignature()+" exists and is already open..."); //
            {
                throw new NestedIOException(new IOException("DatagramConnection, doOpen(), Port already open"));
            }
        }
    }

    @Override
    protected void doServerClose() throws NestedIOException {
        doClose();
    }

    @Override
    protected void doClose() throws NestedIOException {
        if (boolOpen) {
            try {
                if (strComPort != null) {
                    closeVirtual();
                }
                outputStream.close();
                inputStream.close();
                udpSession.close();

                boolOpen = false;
            } catch (IOException e) {
                throw new NestedIOException(e);
            }
        } else {
            if ((strComPort != null) && (boolVirtualOpen)) {
                try {
                    closeVirtual();
                } catch (IOException e) {
                    throw new NestedIOException(e);
                }
            } else {
                throw new NestedIOException(new IOException("IP port is not open"));
            }
        }

    }

}