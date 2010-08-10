package com.energyict.protocolimpl.elster.ctr;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.base.*;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.*;
import java.util.*;

/**
 * Copyrights EnergyICT
 * Date: 5-aug-2010
 * Time: 11:12:17
 */
public class MTU155 extends AbstractProtocol {

    private CTRConnection ctrConnection;
    private final ProtocolProperties protocolProperties = new MTU155Properties();

    @Override
    protected void doConnect() throws IOException {
        byte[] packet = new byte[] {
                (byte) 0x0A, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xD1, (byte) 0x40, (byte) 0xC0,
                (byte) 0x26, (byte) 0x9F, (byte) 0x69, (byte) 0x58, (byte) 0xEC, (byte) 0x35, (byte) 0x02, (byte) 0x86,
                (byte) 0x1D, (byte) 0x91, (byte) 0x44, (byte) 0xB9, (byte) 0x67, (byte) 0xC6, (byte) 0xEA, (byte) 0x07,
                (byte) 0x40, (byte) 0x5B, (byte) 0x9E, (byte) 0xC8, (byte) 0x59, (byte) 0x54, (byte) 0xAB, (byte) 0x13,
                (byte) 0x10, (byte) 0x9E, (byte) 0x13, (byte) 0xEF, (byte) 0x0D, (byte) 0xBB, (byte) 0x7D, (byte) 0xAE,
                (byte) 0x64, (byte) 0x6D, (byte) 0x63, (byte) 0x7B, (byte) 0x16, (byte) 0xF9, (byte) 0xFE, (byte) 0x77,
                (byte) 0xA7, (byte) 0x5C, (byte) 0x34, (byte) 0xFA, (byte) 0x21, (byte) 0x03, (byte) 0xD4, (byte) 0xDC,
                (byte) 0x1D, (byte) 0xDF, (byte) 0x76, (byte) 0xF7, (byte) 0x42, (byte) 0xCB, (byte) 0x48, (byte) 0xAA,
                (byte) 0x92, (byte) 0x4E, (byte) 0xC7, (byte) 0x35, (byte) 0x17, (byte) 0xD5, (byte) 0x97, (byte) 0x6E,
                (byte) 0x2E, (byte) 0x82, (byte) 0xEE, (byte) 0x9B, (byte) 0xCE, (byte) 0x4B, (byte) 0x1A, (byte) 0x6B,
                (byte) 0x3A, (byte) 0x2A, (byte) 0x2C, (byte) 0x5B, (byte) 0x78, (byte) 0xF5, (byte) 0x02, (byte) 0xB9,
                (byte) 0x9E, (byte) 0xE4, (byte) 0xB7, (byte) 0x2E, (byte) 0xD9, (byte) 0x4F, (byte) 0x36, (byte) 0x24,
                (byte) 0x01, (byte) 0x12, (byte) 0x97, (byte) 0x3D, (byte) 0x1E, (byte) 0x12, (byte) 0x97, (byte) 0xC8,
                (byte) 0xCE, (byte) 0xFE, (byte) 0x79, (byte) 0x12, (byte) 0x67, (byte) 0xBE, (byte) 0x2C, (byte) 0x15,
                (byte) 0x0C, (byte) 0xF2, (byte) 0x66, (byte) 0x54, (byte) 0xF0, (byte) 0x23, (byte) 0xE5, (byte) 0xCD,
                (byte) 0x48, (byte) 0x77, (byte) 0x95, (byte) 0x4F, (byte) 0x43, (byte) 0x0F, (byte) 0x02, (byte) 0xA6,
                (byte) 0x95, (byte) 0xDA, (byte) 0x57, (byte) 0x13, (byte) 0x37, (byte) 0xD1, (byte) 0x7E, (byte) 0x7F,
                (byte) 0x80, (byte) 0x81, (byte) 0x82, (byte) 0x97, (byte) 0x82, (byte) 0x0D
        };
        
        getCtrConnection().writeRawData(packet);
        byte[] response = getCtrConnection().readRawData();
        System.out.println(ProtocolTools.getHexStringFromBytes(response));
    }

    @Override
    protected void doDisConnect() throws IOException {

    }

    @Override
    protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        getProtocolProperties().initProperties(properties);
    }

    @Override
    public List getRequiredKeys() {
        return getProtocolProperties().getRequiredKeys();
    }

    @Override
    protected List doGetOptionalKeys() {
        return getProtocolProperties().getOptionalKeys();
    }



    @Override
    protected ProtocolConnection doInit(InputStream inputStream, OutputStream outputStream, int timeout, int retries, int forcedDelay, int echoCancelling, int protocolCompatible, Encryptor encryptor, HalfDuplexController halfDuplexController) throws IOException {
        this.ctrConnection = new CTRConnection(inputStream, outputStream, forcedDelay, timeout, retries, getProtocolProperties().getPassword(), getProtocolProperties().getEncryptionKey());
        return ctrConnection;
    }

    @Override
    public Date getTime() throws IOException {
        throw new IOException("Not implemented yet.");
    }

    @Override
    public void setTime() throws IOException {
        throw new IOException("Not implemented yet.");
    }

    @Override
    public String getProtocolVersion() {
        return "$Revision$";  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getFirmwareVersion() throws IOException, UnsupportedException {
        throw new IOException("Not implemented yet.");
    }

    public CTRConnection getCtrConnection() {
        return ctrConnection;
    }

    public ProtocolProperties getProtocolProperties() {
        return protocolProperties;
    }
}
