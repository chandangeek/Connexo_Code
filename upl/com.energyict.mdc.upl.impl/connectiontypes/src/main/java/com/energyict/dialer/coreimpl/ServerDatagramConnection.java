/*
 * SocketStreamConnection
 *
 * Created on 8 april 2004, 14:08
 */

package com.energyict.dialer.coreimpl;

import com.energyict.mdc.io.NestedIOException;
import com.energyict.mdc.upl.RuntimeEnvironment;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Koen
 */
public class ServerDatagramConnection extends StreamPortConnection {

    private static final Log logger = LogFactory.getLog(ServerDatagramConnection.class);

    // socket for UDP communication
    private DatagramSocket serverSocket = null;
    DatagramConnection session = null;
    Map<String, DatagramConnection> sessions = new HashMap<>();
    public static final String DataGramBufferInputStream = "DatagramInputStreamBufferSize";

    public ServerDatagramConnection(String ipPort, RuntimeEnvironment runtimeEnvironment) {
        super(ipPort, runtimeEnvironment);
    }

    public synchronized void send(DatagramPacket sendPacket) throws IOException {
        serverSocket.send(sendPacket);
    }

    public synchronized void closeSession(UDPSession udpSession) {
        if (logger.isTraceEnabled()) {
            logger.trace("Remove UDPSession " + udpSession.getSignature());
        }
        sessions.remove(udpSession.getSignature());
    }

    @Override
    public void accept() throws NestedIOException {
        while (true) {
            try {
                int bufferSize = this.getIntProperty(DataGramBufferInputStream, 1024);
                byte[] receiveData = new byte[bufferSize];
                DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                serverSocket.receive(receivePacket);
                String signature = receivePacket.getAddress() + " " + receivePacket.getPort();

                session = sessions.get(signature);

                if (logger.isDebugEnabled()) {
                    logger.debug("nr of sessions: " + sessions.size());
                }
                if (session == null) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("Create UDPSession signature " + signature);
                    }
                    udpSession = new UDPSession(this, this.getRuntimeEnvironment(), signature);
                    session = new DatagramConnection(udpSession, this.getRuntimeEnvironment());
                    sessions.put(signature, session);
                    return;
                } else {
                    if (logger.isTraceEnabled()) {
                        logger.trace("UDPSession with signature " + signature + " exist");
                    }
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
        }
    }

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

    @Override
    protected void doOpen() throws NestedIOException {
        boolOpen = true;
        setComPort(ipPort.substring(0, ipPort.indexOf(":")));
    }

    @Override
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
    }

    @Override
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

    }

    @Override
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

    }

    private String getTCPPort(String strPhoneNr) throws IOException {
        String port = strPhoneNr.substring(strPhoneNr.indexOf(":") + 1);
        if (port.isEmpty()) {
            throw new IOException("ServerDatagramConnection, getTCPPort, invalid IP connection string " + strPhoneNr);
        }
        return port;
    }

}