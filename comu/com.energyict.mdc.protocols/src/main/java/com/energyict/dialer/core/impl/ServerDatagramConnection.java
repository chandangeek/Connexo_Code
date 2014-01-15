/*
 * SocketStreamConnection
 *
 * Created on 8 april 2004, 14:08
 */

package com.energyict.dialer.core.impl;

import com.energyict.mdc.common.Environment;
import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.protocol.api.dialer.core.UDPSession;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Koen
 */
public class ServerDatagramConnection extends StreamPortConnection {

    private static final Logger LOGGER = Logger.getLogger(ServerDatagramConnection.class.getName());

    // socket for UDP communication
    private DatagramSocket serverSocket = null;
    DatagramConnection session = null;
    Map<String, DatagramConnection> sessions = new HashMap<String, DatagramConnection>();
    public static final String DataGramBufferInputStream = "DatagramInputStreamBufferSize";

    /**
     * Creates a new instance of SocketStreamConnection
     */
    public ServerDatagramConnection(String ipPort) {
        super(ipPort);
    }

    public DatagramConnection getAcceptedDatagramConnection() {
        return session;
    }

    public synchronized void send(DatagramPacket sendPacket) throws IOException {
        serverSocket.send(sendPacket);
    }

    public synchronized void closeSession(UDPSession udpSession) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("Remove UDPSession " + udpSession.getSignature());
        }
        sessions.remove(udpSession.getSignature());
    }

    public void accept() throws NestedIOException {
        while (true) {
            try {
                int bufferSize = 1024;
                try {
                    String buffSizeString = Environment.DEFAULT.get().getProperty(DataGramBufferInputStream);
                    if (buffSizeString != null) {
                        bufferSize = Integer.valueOf(buffSizeString);
                    }
                } catch (NumberFormatException e) {
                    bufferSize = 1024;
                }
                byte[] receiveData = new byte[bufferSize];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);
                String signature = receivePacket.getAddress() + " " + receivePacket.getPort();


                session = sessions.get(signature);

                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine("nr of sessions: " + sessions.size());
                }
                if (session == null) {
                    if (LOGGER.isLoggable(Level.FINEST)) {
                        LOGGER.finest("Create UDPSession signature " + signature);
                    }
                    udpSession = new UDPSessionImpl(this, signature);
                    session = new DatagramConnection(udpSession);
                    sessions.put(signature, session);
                    session.getUdpSession().receive(receivePacket);
                    return;
                } else {
                    if (LOGGER.isLoggable(Level.FINEST)) {
                        LOGGER.finest("UDPSession with signature " + signature + " exist");
                    }
                    session.getUdpSession().receive(receivePacket);
                }
            } catch (BindException e) {
                throw new NestedIOException(e, "BindException, there is already an instance active!");
            } catch (SocketException e) {
                if (!e.toString().contains("socket closed")) {
                    throw new NestedIOException(e, "SocketException");
                }
            } catch (IOException e) {
                throw new NestedIOException(e, "IOException");
            }
        } // while(true)
    } // public Socket listen()

    /**
     * Wait and accept an incoming DatagramPacket.
     *
     * @param timeOut the timeout to wait for an incomming packet
     * @throws NestedIOException if the socketTimeout exceeded
     */
    @Override
    public void accept(final int timeOut) throws NestedIOException {
        if (serverSocket != null) {
            try {
                serverSocket.setSoTimeout(timeOut);
            } catch (SocketException e) {
                if (!e.toString().contains("socket closed")) {
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
                if (!ipPort.contains(":")) {
                    throw new IOException("Missing port descriptor for TCP inbound connection! Adjust first.");
                }
                setComPort(ipPort.substring(0, ipPort.indexOf(":")));
                serverSocket = new DatagramSocket(Integer.parseInt(getTCPPort(ipPort)));
                serverSocket.setBroadcast(false);
                boolOpen = true;
            } catch (NestedIOException e) {
                try {

                    inputStream.close();
                    outputStream.close();
                } catch (IOException ex) {
                    throw new NestedIOException(ex);
                }
                serverSocket.close();
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
                if (serverSocket != null) {
                    serverSocket.close();
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
                if (serverSocket == null) {
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
            if (serverSocket != null) {
                serverSocket.close();
                serverSocket = null;
            }
            if (socket == null) {
                boolOpen = false;
            }
        } else {
            throw new NestedIOException(new IOException("ServerDatagramConnection, doServerClose, IP server port is not open"));
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
            throw new IOException("ServerDatagramConnection, getTCPPort, invalid IP connection string " + strPhoneNr);
        }
        return port;
    }


}
