package com.elster.protocolimpl.dlms.connection;

import com.elster.coding.CodingUtils;
import com.elster.dlms.cosem.applicationlayer.CosemApplicationLayer;
import com.elster.dlms.cosem.profiles.hdlc.EictDlmsHdlcStack;
import com.elster.protocolimpl.dlms.SecurityData;
import com.energyict.dialer.connection.Connection;
import com.energyict.dialer.connection.ConnectionException;

import java.io.*;

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
    private int tryDoubleConnTo = 0;

    private EictDlmsHdlcStack stack = null;
    private boolean useModeE;
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
     * @param serverAddress - address of device
     * @param logicalDevice - address of logical device
     * @param clientID      - party connecting to device
     * @param securityData  - as the name said...
     * @param useModeE      - if used with optical head
     * @param tryDoubleConnTo - try to connect first to the logical device set here (testing purposes)
     *
     * @return always null
     * @throws java.io.IOException - in case of an error
     */
    public String connect(int serverAddress, int logicalDevice, int clientID, SecurityData securityData, boolean useModeE, int tryDoubleConnTo) throws IOException {

        this.serverAddress = serverAddress;
        this.logicalDevice = logicalDevice;
        this.clientID = clientID;
        this.secData = securityData;
        this.useModeE = useModeE;
        this.tryDoubleConnTo = tryDoubleConnTo;
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

        if (tryDoubleConnTo > 0) {
            stack.setLogicalDeviceId(0x01);
            stack.setServerLowerHdlcAddress(serverAddress);
            stack.setClientId(0x10);
            stack.setModeE(useModeE);
            stack.setSecurityLevel(EictDlmsHdlcStack.SecurityLevel.LOWEST_LEVEL_SECURITY);
            stack.open();
            stack.close();
        }

        if (logicalDevice > 0) {
            stack.setLogicalDeviceId(logicalDevice);
        }
        stack.setServerLowerHdlcAddress(serverAddress);
        stack.setClientId(clientID);
        stack.setModeE(useModeE);

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
}
