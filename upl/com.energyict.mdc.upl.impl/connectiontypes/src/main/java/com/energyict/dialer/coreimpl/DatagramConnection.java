/*
 * DatagramConnection
 *
 * Created on 8 april 2004, 14:08
 */

package com.energyict.dialer.coreimpl;

import com.energyict.mdc.io.NestedIOException;

import java.io.IOException;

/**
 * @author Koen
 */
public class DatagramConnection extends StreamPortConnection {


    // socket for IP communication

    /**
     * Creates a new instance of DatagramConnection
     */
    public DatagramConnection(String ipPort) {
        super(ipPort);
    }

    // KV 03102005
    public DatagramConnection(UDPSession udpSession) {
        super(udpSession);
    }

    //****************************************************************************************
    // Delegate of implementation of interface StreamConnection
    //****************************************************************************************
    protected void doServerOpen() throws IOException {
        doOpen();
    }

    protected void doOpen() throws IOException {
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
                    throw new IOException(ex);
                }
                udpSession.close();
                throw new IOException(e);
            }
        } else {

            if ((strComPort != null) && (!boolVirtualOpen)) {
                try {
                    openVirtual();
                } catch (IOException e) {
                    throw new NestedIOException(e);
                }
            } else {
                throw new NestedIOException(new IOException("DatagramConnection, doOpen(), Port already open"));
            }
        }
    }

    protected void doServerClose() throws NestedIOException {
        doClose();
    }

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