/*
 * SocketStreamConnection
 *
 * Created on 8 april 2004, 14:08
 */

package com.energyict.dialer.coreimpl;

import com.energyict.mdc.io.NestedIOException;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.SocketException;

/**
 * @author Koen
 */
public class ServerSocketStreamConnection extends StreamPortConnection {


    // socket for IP communication
    private ServerSocket ss = null;

    SocketStreamConnection ssc = null;

    /**
     * Creates a new instance of SocketStreamConnection
     */
    public ServerSocketStreamConnection(String ipPort) {
        super(ipPort);
    }

    public SocketStreamConnection getAcceptedSocketStreamConnection() {
        return ssc;
    }

    public void accept() throws NestedIOException {
        while (true) {
            try {
                socket = null;
                socket = ss.accept();
                ssc = new SocketStreamConnection(socket);
                return;
            } catch (BindException e) {
                throw new NestedIOException(e, "BindException, there is already an instance active!");
            } catch (SocketException e) {
                if (e.toString().indexOf("socket closed") == -1) {
                    throw new NestedIOException(e, "SocketException");
                }
            } catch (IOException e) {
                throw new NestedIOException(e, "IOException");
            }
        } // while(true)
    } // public Socket listen()

    /**
     * Wait and accept an incoming Socket.
     *
     * @param timeOut the timeout to wait for an incoming Socket
     * @throws NestedIOException if the socketTimeout exceeded
     */
    @Override
    public void accept(final int timeOut) throws NestedIOException {
        if (ss != null) {
            try {
                ss.setSoTimeout(timeOut);
            } catch (SocketException e) {
                if (e.toString().indexOf("socket closed") == -1) {
                    throw new NestedIOException(e, "SocketException while setting SocketTimout");
                }
            }
        }
        accept();
    }

    //****************************************************************************************
    // Delegate of implementation of interface StreamConnection
    //****************************************************************************************
    protected void doOpen() throws NestedIOException {
        boolOpen = true;
        setComPort(ipPort.substring(0, ipPort.indexOf(":")));
    } // protected void doOpen(String strPhoneNr) throws NestedIOException,StreamConnectionException

    protected void doServerOpen() throws NestedIOException {
        if (!boolOpen) {
            try {
                if (ipPort.indexOf(":") == -1) {
                    throw new IOException("Missing port descriptor for TCP inbound connection! Adjust first.");
                }
                setComPort(ipPort.substring(0, ipPort.indexOf(":")));

                ss = new ServerSocket(Integer.parseInt(getTCPPort(ipPort)));
                boolOpen = true;
            } catch (NestedIOException e) {
                try {

                    inputStream.close();
                    outputStream.close();
                } catch (IOException ex) {
                    throw new NestedIOException(ex);
                }
                try {
                    ss.close();
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
                    if (ss != null) {
                        ss.close();
                    }
                } catch (IOException ex) {
                    // absorb
                }
                throw new NestedIOException(e);
            }
        } else {
            throw new NestedIOException(new IOException("Port already open"));
        }
    } // protected void doServerOpen(String strPhoneNr) throws NestedIOException,StreamConnectionException

    protected void doClose() throws NestedIOException {
        if (boolOpen) {
            try {
                if (socket != null) {
                    if (outputStream != null) {
                        outputStream.close();
                    }
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    socket.close();
                    socket = null;
                }
                if (ss == null) {
                    boolOpen = false;
                }
            } catch (IOException e) {
                throw new NestedIOException(e);
            }
        } else {
            throw new NestedIOException(new IOException("IP port is not open"));
        }

    } // protected void doClose() throws NestedIOException

    protected void doServerClose() throws NestedIOException {
        if (boolOpen) {
            try {
                if (ss != null) {
                    ss.close();
                    ss = null;
                }
                if (socket == null) {
                    boolOpen = false;
                }
            } catch (IOException e) {
                throw new NestedIOException(e);
            }
        } else {
            throw new NestedIOException(new IOException("IP server port is not open"));
        }

    } // protected void doClose() throws NestedIOException

    //****************************************************************************************
    // Private core methods
    //****************************************************************************************
    /*
     *   e.g. strPhoneNr = TCP:9000
     */
    private String getTCPPort(String strPhoneNr) throws IOException {
        String port = strPhoneNr.substring(strPhoneNr.indexOf(":") + 1);
        if ((port == null) || ("".compareTo(port) == 0)) {
            throw new IOException("SocketStreamConnection, getTCPPort, invalid IP connection string " + strPhoneNr);
        }
        return port;
    }


}
