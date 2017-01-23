package com.energyict.protocolimpl.coronis.waveflowDLMS;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TransparentAction extends AbstractTransparentObjectAccess {

    private final ObjectInfo objectInfo;

    private final int DLMS_ACTION_RESPONSE = 0xC7;
    private final int DLMS_RESPONSE_TAG_OFFSET = 3;
    private final int DLMS_RESPONSE_STATUS_CODE_OFFSET = 6;

    private AbstractDataType dataType;

    final AbstractDataType getDataType() {
        return dataType;
    }

    final void setDataType(AbstractDataType dataType) {
        this.dataType = dataType;
    }

    TransparentAction(AbstractDLMS abstractDLMS, ObjectInfo objectInfo) {
        super(abstractDLMS);
        this.objectInfo = objectInfo;
    }

    @Override
    InteractionParameter getInteractionParameter() {
        return InteractionParameter.ACTION;
    }

    @Override
    void parse(byte[] data) throws IOException {

        data = abstractDLMS.getEncryptor().decryptFrames(data, FIRST_FRAME_MAX_LENGTH, NEXT_FRAME_MAX_LENGTH);

        DataInputStream dais = null;
        try {
            dais = new DataInputStream(new ByteArrayInputStream(data));
            int classId = WaveflowProtocolUtils.toInt(dais.readShort());
            if (classId != objectInfo.getClassId()) {
                throw new WaveFlowDLMSException("Transparant object get error. Expected classId [" + WaveflowProtocolUtils.toHexString(objectInfo.getClassId()) + "], received [" + WaveflowProtocolUtils.toHexString(classId) + "]");
            }

            byte[] temp = new byte[6];
            dais.read(temp);
            ObisCode obisCode = ObisCode.fromByteArray(temp);
            if (!obisCode.equals(objectInfo.getObisCode())) {
                throw new WaveFlowDLMSException("Transparant object get error. Expected obis code [" + objectInfo.getObisCode() + "], received [" + obisCode + "]");
            }

            int attribute = WaveflowProtocolUtils.toInt(dais.readByte());
            if (attribute != objectInfo.getAttribute()) {
                throw new WaveFlowDLMSException("Transparant object get error. Expected attributeId d[" + WaveflowProtocolUtils.toHexString(objectInfo.getAttribute()) + "], received [" + WaveflowProtocolUtils.toHexString(attribute) + "]");
            }

            int length = WaveflowProtocolUtils.toInt(dais.readShort()); // don't need the length

            // validate the HDLC frame and extract the DLMS payload
            temp = new byte[dais.available()];
            dais.read(temp);
            HDLCFrameParser o = new HDLCFrameParser();
            o.parseFrame(temp, 0);
            byte[] dlmsData = o.getDLMSData();
            if (WaveflowProtocolUtils.toInt(dlmsData[DLMS_RESPONSE_TAG_OFFSET]) != DLMS_ACTION_RESPONSE) {
                throw new WaveFlowDLMSException("Transparant object get error. Expected DLMS tag [C7], received[" + WaveflowProtocolUtils.toHexString(dlmsData[DLMS_RESPONSE_TAG_OFFSET]) + "]");
            }

            int statusCode = WaveflowProtocolUtils.toInt(dlmsData[DLMS_RESPONSE_STATUS_CODE_OFFSET]);
            if (statusCode != 0) {
                throw new DataAccessResultException(statusCode);
            }

        } finally {
            if (dais != null) {
                try {
                    dais.close();
                } catch (IOException e) {
                    abstractDLMS.getLogger().severe(ProtocolUtils.stack2string(e));
                }
            }
        }

    }

    @Override
    byte[] prepare() throws IOException {

        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            DataOutputStream daos = new DataOutputStream(baos);
            daos.writeShort(objectInfo.getClassId());
            daos.write(objectInfo.getObisCode().getLN());
            daos.writeByte(objectInfo.getAttribute());
            daos.writeByte(getInteractionParameter().getId());
            daos.writeByte(dataType.getBEREncodedByteArray().length);
            daos.write(dataType.getBEREncodedByteArray());
            return baos.toByteArray();
        } finally {
            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException e) {
                    abstractDLMS.getLogger().severe(ProtocolUtils.stack2string(e));
                }
            }
        }
    }


}