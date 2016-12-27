/*
 * SocketStreamConnection
 *
 * Created on 8 april 2004, 14:08
 */

package com.energyict.dialer.coreimpl;

import com.energyict.mdc.io.NestedIOException;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

/**
 * @author Koen
 */
public class SocketStreamConnection extends StreamPortConnection {


    // socket for IP communication

    /**
     * Creates a new instance of SocketStreamConnection
     */
    public SocketStreamConnection(String ipPort) {
        super(ipPort);
    }

    // KV 03102005
    public SocketStreamConnection(Socket socket) {
        super(socket);
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
                if (socket == null) {
                    socket = new Socket(InetAddress.getByName(getIPAddress(ipPort)), Integer.parseInt(getTCPPort(ipPort)));
                }
                setStreams(socket.getInputStream(), socket.getOutputStream());
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
                try {
                    socket.close();
                } catch (IOException ex) {
                    // absorb
                }
                throw e;
            } catch (SocketException e) {
                throw new NestedIOException(e);
            } catch (Exception e) {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (outputStream != null) {
                        outputStream.close();
                    }
                } catch (IOException ex) {
                    throw new NestedIOException(ex);
                }
                try {
                    if (socket != null) {
                        socket.close();
                    }
                } catch (IOException ex) {
                    // absorb
                }
                throw new NestedIOException(e);
            }
        } else {

            if ((strComPort != null) && (!boolVirtualOpen)) {
                try {
                    openVirtual();
                } catch (IOException e) {
                    throw new NestedIOException(e);
                }
            } else {
                throw new NestedIOException(new IOException("Port already open"));
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
                socket.close();
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
        if (strPhoneNr.contains(":")) {
            String address = strPhoneNr.substring(0, strPhoneNr.indexOf(":"));
            if (address == null) {
                throw new IOException("SocketStreamConnection, getIPAddress, invalid IP connection string " + strPhoneNr);
            }
            //address = address.substring(0,address.indexOf(":"));
            //if (address == null) throw new IOException("SocketStreamConnection, getIPAddress, invalid IP connection string "+strPhoneNr);
            return address;
        } else {
            throw new IOException("SocketStreamConnection, getIPAddress, not a valid IP connection string : " + strPhoneNr);
        }
    }

    private String getTCPPort(String strPhoneNr) throws IOException {
        if (strPhoneNr.contains(":")) {
            String port = strPhoneNr.substring(strPhoneNr.indexOf(":") + 1);
            if (port == null) {
                throw new IOException("SocketStreamConnection, getTCPPort, invalid IP connection string " + strPhoneNr);
            }
            int comPortIndex = port.indexOf(":");
            if (comPortIndex != -1) {
                String comPort = port.substring(comPortIndex + 1);
                port = port.substring(0, port.indexOf(":"));
                setComPort(comPort);
            }
            return port;
        } else {
            throw new IOException("SocketStreamConnection, getTCPPort, not a valid IP connection string " + strPhoneNr);
        }
    }

}
