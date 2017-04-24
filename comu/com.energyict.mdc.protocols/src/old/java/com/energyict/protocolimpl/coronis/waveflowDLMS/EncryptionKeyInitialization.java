/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflowDLMS;

import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.protocolimpl.coronis.core.WaveFlowException;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * This command allows to recover general information included in �generic header�, the type and meter serial
 * number of the meter paired. It also allows, if specified, to modify encryption key of the unit.
 * @author kvds
 */
public class EncryptionKeyInitialization {

    private final int ENCRYPTIONKEY_INIT_REQUEST_TAG=0x35;
    private final int ENCRYPTIONKEY_INIT_RESPONSE_TAG=0xB5;


    /**
     * Reference to the implementation class.
     */
    private final AbstractDLMS abstractDLMS;


    enum MeterTypeId {

        ElsterAS253(0x01,"Elster AS253"),
        ElsterAS1253(0x02,"Elster AS1253"),
        ElsterA1800(0x03,"Elster A1800");

        private final int id;
        private final String description;

        MeterTypeId(final int id, final String description) {
            this.id=id;
            this.description=description;
        }

        public String toString() {
            return "MeterTypeId: "+description;
        }

        static MeterTypeId fromId(int id) {
            for (MeterTypeId mid : values()) {
                if (mid.id == id) {
                    return mid;
                }
            }
            return null;
        }

    } // enum MeterTypeId

    /**
     * The metertype read with the 0x35 command!
     */
    private MeterTypeId meterTypeId;

    public final MeterTypeId getMeterTypeId() {
        return meterTypeId;
    }

    /**
     * The encryption key to program in the meter
     */
    byte[] key=null;

    final byte[] getKey() {
        return key;
    }

    final void setKey(byte[] key) {
        this.key = key;
    }

    /**
     * the meterserialnumber read with the 0x35 command!
     */
    private String meterSerialNumber;

    public final String getMeterSerialNumber() {
        return meterSerialNumber;
    }

    public EncryptionKeyInitialization(AbstractDLMS abstractDLMS) throws WaveFlowDLMSException {
        this.abstractDLMS = abstractDLMS;
    }

    private GenericHeader genericHeader;

    final GenericHeader getGenericHeader() {
        return genericHeader;
    }

    public final void write() throws IOException {
        int retry = 0;

        while(true) {
            ByteArrayOutputStream baos = null;
            try {
                baos = new ByteArrayOutputStream();
                DataOutputStream daos = new DataOutputStream(baos);
                daos.writeByte(ENCRYPTIONKEY_INIT_REQUEST_TAG);

                //daos.write(...);

                abstractDLMS.getWaveFlowConnect().getEscapeCommandFactory().sendUsingSendMessage();
                parseResponse(abstractDLMS.getWaveFlowConnect().sendData(baos.toByteArray()));
                return;
            }
            catch(ConnectionException e) {
                if (retry++ >= abstractDLMS.getInfoTypeProtocolRetriesProperty()) {
                    throw new WaveFlowDLMSException(e.getMessage()+", gave up after ["+abstractDLMS.getInfoTypeProtocolRetriesProperty()+"] reties!");
                }
                else {
                    abstractDLMS.getLogger().warning(e.getMessage()+", retry ["+retry+"]");
                }
            }
            catch(WaveFlowDLMSException e) {
                if (retry++ >= abstractDLMS.getInfoTypeProtocolRetriesProperty()) {
                    throw new WaveFlowDLMSException(e.getMessage()+", after ["+abstractDLMS.getInfoTypeProtocolRetriesProperty()+"] reties!");
                }
                else {
                    abstractDLMS.getLogger().warning(e.getMessage()+", retry ["+retry+"]");
                }
            }
            finally {
                abstractDLMS.getWaveFlowConnect().getEscapeCommandFactory().sendUsingSendFrame();
                if (baos != null) {
                    try {
                        baos.close();
                    }
                    catch(IOException e) {
                        abstractDLMS.getLogger().severe(ProtocolUtils.stack2string(e));
                    }
                }
            }
        }
    }



    public final void invoke() throws IOException {
        int retry = 0;

        while(true) {
            ByteArrayOutputStream baos = null;
            try {
                baos = new ByteArrayOutputStream();
                DataOutputStream daos = new DataOutputStream(baos);
                daos.writeByte(ENCRYPTIONKEY_INIT_REQUEST_TAG);

                if (key != null) {
                    daos.write(key);
                }
                //abstractDLMS.getWaveFlowConnect().getEscapeCommandFactory().sendUsingSendMessage();
                parseResponse(abstractDLMS.getWaveFlowConnect().sendData(baos.toByteArray()));
                return;
            }
            catch(ConnectionException e) {
                if (retry++ >= abstractDLMS.getInfoTypeProtocolRetriesProperty()) {
                    throw new WaveFlowDLMSException(e.getMessage()+", gave up after ["+abstractDLMS.getInfoTypeProtocolRetriesProperty()+"] reties!");
                }
                else {
                    abstractDLMS.getLogger().warning(e.getMessage()+", retry ["+retry+"]");
                }
            }
            catch(WaveFlowDLMSException e) {
                if (retry++ >= abstractDLMS.getInfoTypeProtocolRetriesProperty()) {
                    throw new WaveFlowDLMSException(e.getMessage()+", after ["+abstractDLMS.getInfoTypeProtocolRetriesProperty()+"] reties!");
                }
                else {
                    abstractDLMS.getLogger().warning(e.getMessage()+", retry ["+retry+"]");
                }
            }
            finally {
                abstractDLMS.getWaveFlowConnect().getEscapeCommandFactory().sendUsingSendFrame();
                if (baos != null) {
                    try {
                        baos.close();
                    }
                    catch(IOException e) {
                        abstractDLMS.getLogger().severe(ProtocolUtils.stack2string(e));
                    }
                }
            }
        }
    }

    private final void validateResultCode(int resultCode) throws WaveFlowException {
        switch(resultCode) {
            case 0xFF: throw new WaveFlowDLMSException("Encryption key initialization error. Bad request format!");
            default: return;
        }
    }

    private final void parseResponse(byte[] sendData) throws IOException {

        //System.out.println("Received : "+ProtocolUtils.outputHexString(sendData));


        DataInputStream dais = null;
        try {
            dais = new DataInputStream(new ByteArrayInputStream(sendData));
            int responseTag = WaveflowProtocolUtils.toInt(dais.readByte());
            if (responseTag != ENCRYPTIONKEY_INIT_RESPONSE_TAG) {
                throw new WaveFlowException("encryption key initialization error. Expected ["+WaveflowProtocolUtils.toHexString(ENCRYPTIONKEY_INIT_RESPONSE_TAG)+"], received ["+WaveflowProtocolUtils.toHexString(responseTag)+"]");
            }
            byte[] temp = new byte[GenericHeader.size()];
            dais.read(temp);
            genericHeader = new GenericHeader(temp, abstractDLMS);

            temp = new byte[dais.available()];
            dais.read(temp);
            validateResultCode(WaveflowProtocolUtils.toInt(temp[0]));
            parse(temp);

        }
        finally {
            if (dais != null) {
                try {
                    dais.close();
                }
                catch(IOException e) {
                    abstractDLMS.getLogger().severe(ProtocolUtils.stack2string(e));
                }
            }
        }
    }

    void parse(byte[] data) throws IOException {

        data = abstractDLMS.getEncryptor().decryptFrame(data);

        int count=0;
        DataInputStream dais = null;
        byte[] temp;
        try {
            dais = new DataInputStream(new ByteArrayInputStream(data));
            meterTypeId = MeterTypeId.fromId( WaveflowProtocolUtils.toInt(dais.readByte()));

            if (meterTypeId==null) {
                abstractDLMS.getLogger().warning("EncryptionKeyInitialization: No meter paired.");
            }

            int length = WaveflowProtocolUtils.toInt(dais.readByte());

            if (length == 0) {
                abstractDLMS.getLogger().warning("EncryptionKeyInitialization: No meter serial number in frame.");
            }

            temp = new byte[dais.available()];
            dais.read(temp);
            HDLCFrameParser o = new HDLCFrameParser();
            o.parseFrame(temp,0);
            XDLMSDataParser xDLMSParser = new XDLMSDataParser(abstractDLMS.getLogger());
            try {
                AbstractDataType adt = AXDRDecoder.decode(xDLMSParser.parseAXDRData(o.getDLMSData()));
                meterSerialNumber = adt.toString();
            }
            catch(DataAccessResultException e) {
                meterSerialNumber=null;
                abstractDLMS.getLogger().severe("Error reading serial number with the 0x35 command. GET_RESPONSE returned error ["+e.getMessage()+"]");
            }


        }
        finally {
            if (dais != null) {
                try {
                    dais.close();
                }
                catch(IOException e) {
                    abstractDLMS.getLogger().severe(ProtocolUtils.stack2string(e));
                }
            }
        }
    }
}
