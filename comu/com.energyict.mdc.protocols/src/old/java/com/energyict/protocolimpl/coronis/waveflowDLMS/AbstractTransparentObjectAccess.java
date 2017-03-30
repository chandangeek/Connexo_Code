/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflowDLMS;

import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.coronis.core.WaveFlowException;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Implements the transparent access to a DLMS object. See page 13 of the Waveflow AC 150mW DLMS Version 1 Applicative Specification.
 * There are 3 subclasses implementing the set, get and action method invocation.
 * @author kvds
 */
abstract class AbstractTransparentObjectAccess {

    // non encrypted
    static final int FIRST_FRAME_MAX_LENGTH=143;
    static final int NEXT_FRAME_MAX_LENGTH=132;

    // enrcrypted
//	static final int FIRST_FRAME_MAX_LENGTH=144;
//	static final int NEXT_FRAME_MAX_LENGTH=144;


    /**
     * the DLSM interaction get, set or action method
     * @author kvds
     *
     */
    enum InteractionParameter {

        GET(0,"get"),
        SET(1,"set"),
        ACTION(2,"action");

        private final int id;
        private final String description;

        InteractionParameter(final int id, final String description) {
            this.id=id;
            this.description=description;

        }

        final int getId() {
            return id;
        }
    }

    private final int TRANSPARANT_OBJECT_READING_REQ_TAG=0x31;
    private final int TRANSPARANT_OBJECT_READING_RES_TAG=0xB1;

    /**
     * Reference to the implementation class.
     */
    AbstractDLMS abstractDLMS;

    /**
     * Frame count to be received.this is part of the WaveFlow AC protocol implementation.
     */
    int frameCount=0;

    /**
     * Return the frameCount
     * @return framecount
     */
    final int getFrameCount() {
        return frameCount;
    }

    AbstractTransparentObjectAccess(AbstractDLMS abstractDLMS) {
        this.abstractDLMS = abstractDLMS;
    }

    /**
     * Return the dlms primitive get, set or action
     * @return interaction enum
     */
    abstract InteractionParameter getInteractionParameter();

    /**
     * Parse the received response from the waveflow DLMS
     * @param data
     * @throws IOException
     */
    abstract void parse(byte[] data) throws IOException;

    /**
     * Prepare the transparent access to the Waveflow device
     * @return
     * @throws IOException
     */
    abstract byte[] prepare() throws IOException;

    private GenericHeader genericHeader;

    final GenericHeader getGenericHeader() {
        return genericHeader;
    }

    void invoke() throws IOException {
        int retry=0;
        while(true) {
            ByteArrayOutputStream baos = null;
            try {
                baos = new ByteArrayOutputStream();
                DataOutputStream daos = new DataOutputStream(baos);
                daos.writeByte(TRANSPARANT_OBJECT_READING_REQ_TAG);

                daos.write(abstractDLMS.getEncryptor().encrypt(prepare()));

                byte[] data = baos.toByteArray();
                //System.out.println(ProtocolUtils.outputHexString(data));
                abstractDLMS.getWaveFlowConnect().getEscapeCommandFactory().sendUsingSendMessage();
                parseResponse(abstractDLMS.getWaveFlowConnect().sendData(data));
                return;
            }
            catch(ConnectionException e) {
                if (retry++ >= abstractDLMS.getInfoTypeProtocolRetriesProperty()) {
                    throw new IOException(e.getMessage()+", gave up after ["+abstractDLMS.getInfoTypeProtocolRetriesProperty()+"] reties!");
                }
                else {
                    abstractDLMS.getLogger().warning(e.getMessage()+", retry ["+retry+"]");
                }
            }
            catch(WaveFlowDLMSException e) {
                if (retry++ >= abstractDLMS.getInfoTypeProtocolRetriesProperty()) {
                    throw new WaveFlowDLMSException(e.getMessage()+", gave up after ["+abstractDLMS.getInfoTypeProtocolRetriesProperty()+"] reties!");
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

    private void validateResultCode(int resultCode) throws WaveFlowException {
        switch(resultCode) {
            case 0xFF: throw new WaveFlowDLMSException("Transparant object access error. Error Bad request format!");
            case 0xFE: throw new WaveFlowExceptionNotPaired("Transparant object access error. Pairing request never sent!");
            case 0xFD: throw new WaveFlowDLMSException("Transparant object access error. Connection rejected!");
            case 0xFC: throw new WaveFlowDLMSException("Transparant object access error. Association rejected!");
            case 0xFB: throw new WaveFlowDLMSException("Transparant object access error. Interaction (Get, Set or Action) failed!");
            default: return;
        }
    }

    private void parseResponse(byte[] sendData) throws IOException {


        DataInputStream dais = null;
        try {
            dais = new DataInputStream(new ByteArrayInputStream(sendData));
            int responseTag = WaveflowProtocolUtils.toInt(dais.readByte());
            if (responseTag != TRANSPARANT_OBJECT_READING_RES_TAG) {
                throw new WaveFlowException("Transparant object access error. Expected ["+WaveflowProtocolUtils.toHexString(TRANSPARANT_OBJECT_READING_RES_TAG)+"], received ["+WaveflowProtocolUtils.toHexString(responseTag)+"]");
            }
            byte[] temp = new byte[GenericHeader.size()];
            dais.read(temp);
            genericHeader = new GenericHeader(temp, abstractDLMS);

            int resultCode = WaveflowProtocolUtils.toInt(dais.readByte());
            validateResultCode(resultCode);
            frameCount=resultCode;

            temp = new byte[dais.available()];
            dais.read(temp);
            parse(ProtocolUtils.getSubArray(temp, 0));

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
