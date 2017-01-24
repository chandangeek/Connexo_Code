/*
 * FunctionWithoutDataCommand.java
 *
 * Created on 8 juli 2005, 11:32
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.alpha.core.connection;

import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
/**
 *
 * @author Koen
 */
public class FunctionWithDataCommand extends CommandBuilder {

    private static final int COMMANDBYTE = 0x18;
    private static final int PASSWORD_CHECK = 0x01;
    private static final int TIME_SET = 0x02;
    private static final int WHO_ARE_YOU = 0x06;
    private static final int BILLINGREAD_DIALIN = 0x07;
    private static final int PACKET_SIZE = 0x09;
    private static final int SET_COMMUNICATIONS_TIMEOUT = 0xF2;


    int expectedFrameType=AlphaConnection.FRAME_RESPONSE_TYPE_ACK_NAK;
    ResponseFrame responseFrame=null;
    int timeout;

    /** Creates a new instance of FunctionWithoutDataCommand */
    public FunctionWithDataCommand(AlphaConnection alphaConnection) {
        this(alphaConnection,0);
    }
    public FunctionWithDataCommand(AlphaConnection alphaConnection, int timeout) {
        super(alphaConnection);
        this.timeout = timeout;
    }

    public void PacketSize(int packetSize) throws IOException {
        expectedFrameType=AlphaConnection.FRAME_RESPONSE_TYPE_ACK_NAK;
        byte[] data = new byte[5];
        data[0] = COMMANDBYTE;  // CB
        data[1] = PACKET_SIZE;  // FUNC
        data[2] = (byte)timeout; // pad
        data[3] = 1; // len of the data
        data[4] = (byte)packetSize; // data
        sendCommandWithResponse(data);
    }

    // in 0.5 sec units
    public void communicationsTimeout(int communicationsTimeout) throws IOException {
        expectedFrameType=AlphaConnection.FRAME_RESPONSE_TYPE_ACK_NAK;
        byte[] data = new byte[5];
        data[0] = COMMANDBYTE;  // CB
        data[1] = (byte)SET_COMMUNICATIONS_TIMEOUT;  // FUNC
        data[2] = (byte)timeout; // pad
        data[3] = 1; // len of the data
        data[4] = (byte)communicationsTimeout; // data
        sendCommandWithResponse(data);
    }


    public WhoAreYouData whoAreYou(int deviceNumber) throws IOException {
        expectedFrameType=AlphaConnection.FRAME_RESPONSE_TYPE_WHO_ARE_YOU;
        byte[] data = new byte[5];
        data[0] = COMMANDBYTE;  // CB
        data[1] = WHO_ARE_YOU;  // FUNC
        data[2] = (byte)timeout; // pad
        data[3] = 1; // len of the data
        data[4] = (byte)deviceNumber; // data
        responseFrame = sendCommandWithResponse(data);
        return new WhoAreYouData(responseFrame.getData());
    }

    public void passwordCheck(WhoAreYouData whoAreYouData, String pass) throws IOException {
        try {
            long lPassword = Long.parseLong(pass,16);
            byte[] password = new byte[4];
            password[0] = (byte)((lPassword>>24)&0xFF);
            password[1] = (byte)((lPassword>>16)&0xFF);
            password[2] = (byte)((lPassword>>8)&0xFF);
            password[3] = (byte)(lPassword&0xFF);
            byte[] encryptedPassword; //=password;
            if (whoAreYouData!=null)
                 encryptedPassword = Encryptor.encrypt(password,whoAreYouData.getPasswordEncryptionKey());
            else {
                encryptedPassword = new byte[4];
                encryptedPassword[0]=password[1];
                encryptedPassword[1]=password[0];
                encryptedPassword[2]=password[3];
                encryptedPassword[3]=password[2];
            }
            byte[] data = new byte[8];
            data[0] = COMMANDBYTE;  // CB
            data[1] = PASSWORD_CHECK;  // FUNC
            data[2] = (byte)timeout; // pad
            data[3] = 4; // len of the data
            data[4] = encryptedPassword[0];
            data[5] = encryptedPassword[1];
            data[6] = encryptedPassword[2];
            data[7] = encryptedPassword[3];
            responseFrame = sendCommandWithResponse(data);
        }
        catch(NumberFormatException e) {
            throw new IOException("passwordCheck(), ERROR, password must contain only hexadecimal characters!");
        }
    }

    protected int getExpectedFrameType() {
        return expectedFrameType;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public void syncTime(int roundTripCorrection, TimeZone timeZone) throws IOException {
        Calendar cal = ProtocolUtils.getCalendar(timeZone);
        cal.add(Calendar.MILLISECOND,roundTripCorrection);
        expectedFrameType=AlphaConnection.FRAME_RESPONSE_TYPE_ACK_NAK;
        byte[] data = new byte[7];
        data[0] = COMMANDBYTE;  // CB
        data[1] = TIME_SET;  // FUNC
        data[2] = (byte)timeout; // pad
        data[3] = 3; // len of the data
        data[4] = ProtocolUtils.hex2BCD(cal.get(Calendar.HOUR_OF_DAY));
        data[5] = ProtocolUtils.hex2BCD(cal.get(Calendar.MINUTE));
        data[6] = ProtocolUtils.hex2BCD(cal.get(Calendar.SECOND));

        sendCommandWithResponse(data);
    }

    public void billingReadDialin(Date nextDialin, TimeZone timeZone) throws IOException {
        Calendar cal = ProtocolUtils.getCleanCalendar(timeZone);
        cal.setTime(nextDialin);
        byte[] data = new byte[7];
        data[0] = COMMANDBYTE;  // CB
        data[1] = BILLINGREAD_DIALIN;  // FUNC
        data[2] = (byte)timeout; // pad
        data[3] = 3; // len of the data
        data[4] = ProtocolUtils.hex2BCD(cal.get(Calendar.YEAR)%100);
        data[5] = ProtocolUtils.hex2BCD(cal.get(Calendar.MONTH)+1);
        data[6] = ProtocolUtils.hex2BCD(cal.get(Calendar.DATE));

        sendCommandWithResponse(data);
    }
}
