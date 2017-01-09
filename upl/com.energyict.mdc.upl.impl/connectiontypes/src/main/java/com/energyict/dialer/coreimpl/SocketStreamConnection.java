/*
 * SocketStreamConnection
 *
 * Created on 8 april 2004, 14:08
 */

package com.energyict.dialer.coreimpl;

import com.energyict.mdc.io.NestedIOException;
import com.energyict.mdc.upl.RuntimeEnvironment;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

/**
 * @author Koen
 */
public class SocketStreamConnection extends StreamPortConnection {

    // socket for IP communication
    public SocketStreamConnection(String ipPort, RuntimeEnvironment runtimeEnvironment) {
        super(ipPort, runtimeEnvironment);
    }

    // KV 03102005
    public SocketStreamConnection(Socket socket, RuntimeEnvironment runtimeEnvironment) {
        super(socket, runtimeEnvironment);
    }

    @Override
    protected void doServerOpen() throws NestedIOException {
        doOpen();
    }

    @Override
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

    }

    private String getIPAddress(String strPhoneNr) throws IOException {
        if (strPhoneNr.contains(":")) {
            return strPhoneNr.substring(0, strPhoneNr.indexOf(":"));
        } else {
            throw new IOException("SocketStreamConnection, getIPAddress, not a valid IP connection string : " + strPhoneNr);
        }
    }

    private String getTCPPort(String strPhoneNr) throws IOException {
        if (strPhoneNr.contains(":")) {
            String port = strPhoneNr.substring(strPhoneNr.indexOf(":") + 1);
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