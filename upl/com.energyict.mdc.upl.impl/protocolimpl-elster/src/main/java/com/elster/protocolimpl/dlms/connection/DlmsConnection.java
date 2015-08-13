package com.elster.protocolimpl.dlms.connection;

import com.elster.coding.CodingUtils;
import com.elster.dlms.cosem.applicationlayer.CosemApplicationLayer;
import com.elster.dlms.cosem.profiles.hdlc.EictDlmsHdlcStack;
import com.elster.protocolimpl.dlms.SecurityData;
import com.energyict.dialer.connection.Connection;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.protocolimplv2.MdcManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;

/**
 * Implementation of dsfg protocol. <br>
 * <br>
 * <p/>
 * <b>General Description:</b><br>
 * <br>
 * <br>
 *
 * @author gh
 * @since 5-mai-2010
 */

public class DlmsConnection extends Connection {

    //int iProtocolTimeout = 20000;

    private InputStream in;
    private OutputStream out;

    private int serverAddress = 0;
    private int logicalDevice = 0;
    private int clientID = 0;
    private SecurityData secData;
    private int timeout = -1;

    private EictDlmsHdlcStack stack = null;
    private boolean useModeE = false;
    private int clientMaxReceivePduSize = 0;
    /*
      * possible additional parameters: this.iMaxRetries = iMaxRetries;
      * this.lForceDelay = lForceDelay; this.iEchoCancelling = iEchoCancelling;
      * this.iIEC1107Compatible = iIEC1107Compatible; this.encryptor=encryptor;
      * iProtocolTimeout=iTimeout; boolFlagIEC1107Connected=false;
      * this.errorSignature=errorSignature; this.software7E1 = software7E1;
      */

    /**
     * constructor of dsfg protocol
     *
     * @param inputStream  - incomming stream
     * @param outputStream - outgoing stream
     * @throws ConnectionException - in case of error
     */
    public DlmsConnection(InputStream inputStream, OutputStream outputStream)
            throws ConnectionException {
        super(inputStream, outputStream);
        this.in = inputStream;
        this.out = outputStream;
    }

    /**
     * starting communication
     *
     * @param serverAddress           - address of device
     * @param logicalDevice           - address of logical device
     * @param clientID                - party connecting to device
     * @param securityData            - as the name said...
     * @param useModeE                - if used with optical head
     * @param timeout                 -  timeout
     * @param clientMaxReceivePduSize - dlms block size
     * @return always null
     */
    public String connect(int serverAddress, int logicalDevice, int clientID, SecurityData securityData, boolean useModeE, int timeout, int clientMaxReceivePduSize) {
        this.serverAddress = serverAddress;
        this.logicalDevice = logicalDevice;
        this.clientID = clientID;
        this.secData = securityData;
        this.useModeE = useModeE;
        this.timeout = timeout;
        this.clientMaxReceivePduSize = clientMaxReceivePduSize;

        // "replace" input stream with version what operates like rxtx serio stream
        in = new MyInputStream(in);

        return null;
    }

    /**
     * tries to sign on to a dsfg device (has to be done directly after
     * connection) if the login was successful, the instance address of the dfue
     * unit is stored
     *
     * @param password (is currently ignored)
     * @throws java.io.IOException - in case of an error
     */
    @SuppressWarnings({"unused"})
    public void signon(String password) throws IOException {

        stack = new EictDlmsHdlcStack(in, out);

        if (timeout > 0) {
            stack.setResponseTimeOut(timeout);
        }

        if (logicalDevice > 0) {
            stack.setLogicalDeviceId(logicalDevice);
        }
        stack.setServerLowerHdlcAddress(serverAddress);
        stack.setClientId(clientID);
        stack.setModeE(useModeE);

        if (clientMaxReceivePduSize > 0) {
            stack.setClientMaxReceivePduSize(clientMaxReceivePduSize);
        }

        switch (secData.getAuthenticationLevel()) {
            case 0:
                stack.setSecurityLevel(EictDlmsHdlcStack.SecurityLevel.LOWEST_LEVEL_SECURITY);
                break;
            case 1:
                stack.setSecurityLevel(EictDlmsHdlcStack.SecurityLevel.LOW_LEVEL_SECURITY);
                stack.setLowLevelSecurityKey(password);
                break;
            case 5:
                stack.setSecurityLevel(EictDlmsHdlcStack.SecurityLevel.HIGH_LEVEL_SECURITY_USING_GMAC);
                stack.setEncryptionKey(CodingUtils.string2ByteArray(secData.getEncryptionKey()));
                stack.setAuthenticationKey(CodingUtils.string2ByteArray(secData.getAuthenticationKey()));
                stack.setSystemTitle("EIServer".getBytes());
        }
        stack.open();

        if (!stack.isOpen()) {
            throw new IOException("Error when opening stack!");
        }
    }

    /**
     * returns the applicationlayer of the opened dlms stack
     *
     * @return reference to application layer
     */
    public CosemApplicationLayer getApplicationLayer() {
        return stack.getCosemApplicationLayer();
    }

    /**
     * Closes an open dlms connection
     *
     * @throws IOException - in case of an error
     */
    public void disconnect() throws IOException {
        try {
            stack.close();
        } finally {
            stack.cleanup();
        }
    }

    private static class MyInputStream extends InputStream {

        private final InputStream in;
        private final int timeout;

        public MyInputStream(InputStream in) {
            this.in = in;
            this.timeout = 100;
        }

        @Override
        public int read() throws IOException {
            long time = System.currentTimeMillis() + timeout;

            do {
                if (in.available() > 0) {
                    return in.read();
                }

                if (System.currentTimeMillis() > time) {
                    throw new SocketTimeoutException("timeout");
                }
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw MdcManager.getComServerExceptionFactory().communicationInterruptedException(e);
                }
            }
            while (true);
        }

        @Override
        public int available() throws IOException {
            return in.available();
        }

        @Override
        public int read(byte[] b) throws IOException {
            try {
                return super.read(b);
            } catch (final SocketTimeoutException sto) {
                return 0;
            }
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            try {
                return super.read(b, off, len);
            } catch (final SocketTimeoutException sto) {
                return 0;
            }

        }

        @Override
        public long skip(long n) throws IOException {
            try {
                return super.skip(n);
            } catch (final SocketTimeoutException sto) {
                return 0;
            }
        }
    }
}
