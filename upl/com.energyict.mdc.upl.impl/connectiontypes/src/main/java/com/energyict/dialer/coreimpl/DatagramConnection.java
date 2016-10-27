/*
 * DatagramConnection
 *
 * Created on 8 april 2004, 14:08
 */

package com.energyict.dialer.coreimpl;

import com.energyict.cbo.NestedIOException;

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
    protected void doServerOpen() throws NestedIOException {
        doOpen();
    }

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
    } // protected void doOpen(String strPhoneNr) throws NestedIOException,StreamConnectionException 

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

    } // protected void doClose() throws NestedIOException   


    //****************************************************************************************
    // Private core methods
    //****************************************************************************************
    private String getIPAddress(String strPhoneNr) throws IOException {
        String address = strPhoneNr.substring(0, strPhoneNr.indexOf(":"));
        if (address == null) {
            throw new IOException("DatagramConnection, getIPAddress, invalid IP connection string " + strPhoneNr);
        }
        //address = address.substring(0,address.indexOf(":"));
        //if (address == null) throw new IOException("DatagramConnection, getIPAddress, invalid IP connection string "+strPhoneNr);
        return address;
    }

    private String getTCPPort(String strPhoneNr) throws IOException {
        String port = strPhoneNr.substring(strPhoneNr.indexOf(":") + 1);
        if (port == null) {
            throw new IOException("DatagramConnection, getTCPPort, invalid IP connection string " + strPhoneNr);
        }
        int comPortIndex = port.indexOf(":");
        if (comPortIndex != -1) {
            String comPort = port.substring(comPortIndex + 1);
            port = port.substring(0, port.indexOf(":"));
            setComPort(comPort);
        }
        return port;
    }

}
