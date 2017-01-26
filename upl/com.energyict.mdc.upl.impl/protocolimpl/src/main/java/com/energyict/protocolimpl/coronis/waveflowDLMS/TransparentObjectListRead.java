package com.energyict.protocolimpl.coronis.waveflowDLMS;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.coronis.core.WaveFlowException;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implements the transparent dlms object list read. See page 13 of the Waveflow AC 150mW DLMS Version 1 Applicative Specification.
 * There are 3 subclasses implementing the set, get and action method invocation.  
 * @author kvds
 */
public class TransparentObjectListRead {

    static private final int TRANSPARANT_OBJECT_LIST_READING_REQ_TAG=0x36;
    static private final int TRANSPARANT_OBJECT_LIST_READING_RES_TAG=0xB6;

    static private final int MAX_LIST_MULTIFRAME_LENGTH=135;
    static private final int LIST_TAG=0xB6;
    static private final int MAX_NR_OF_OBISCODES=16;

    static private final int FIRST_FRAME_MAX_LENGTH=136;
    static private final int NEXT_FRAME_MAX_LENGTH=135;


    static private final int CLASS_DATA=1;
    static private final int CLASS_REGISTER=3;
    static private final int CLASS_EXTENDED_REGISTER=4;
    static private final int CLASS_GENERIC_PROFILE=7;

    static private final int ATTRIBUTE_VALUE=2;
    static private final int ATTRIBUTE_SCALER=3;

    // collected registervalues
    Map<ObisCode,RegisterValue> registerValues;

    public final Map<ObisCode, RegisterValue> getRegisterValues() {
        return registerValues;
    }

    /**
     * Reference to the implementation class.
     */
    private final AbstractDLMS abstractDLMS;

    /**
     * List of obis code objects to read
     */
    private final List<ObjectInfo> objectInfos;

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

    public TransparentObjectListRead(AbstractDLMS abstractDLMS,List<ObjectInfo> objectInfos) throws WaveFlowDLMSException {
        this(abstractDLMS,objectInfos,new HashMap<ObisCode,RegisterValue>());
    }

    public TransparentObjectListRead(final AbstractDLMS abstractDLMS,final List<ObjectInfo> objectInfos,final Map<ObisCode,RegisterValue> registerValues) throws WaveFlowDLMSException {
        this.abstractDLMS = abstractDLMS;
        this.objectInfos=objectInfos;
        this.registerValues=registerValues;
        if (objectInfos.size() > MAX_NR_OF_OBISCODES) {
            throw new WaveFlowDLMSException("Too many obis code requests in list. Requested ["+objectInfos.size()+"], max allowed ["+MAX_NR_OF_OBISCODES+"]");
        }
    }

    private GenericHeader genericHeader;

    final GenericHeader getGenericHeader() {
        return genericHeader;
    }

    private final byte[] objectList2ByteArray() throws IOException {
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            DataOutputStream daos = new DataOutputStream(baos);

            for (ObjectInfo ob : objectInfos) {
                daos.writeShort(ob.getClassId());
                daos.write(ob.getObisCode().getLN());
                daos.writeByte(ob.getAttribute());
            }

            byte[] data =  baos.toByteArray();

            //System.out.println("Send : "+ProtocolUtils.outputHexString(data));
            return data;
        }
        finally {
            if (baos != null) {
                try {
                    baos.close();
                }
                catch(IOException e) {
                    abstractDLMS.getLogger().severe(com.energyict.cbo.Utils.stack2string(e));
                }
            }
        }
    }

    public final void read() throws IOException {
        if (objectInfos.size() == 0) {
            throw new WaveFlowDLMSException("No obiscodes to read in the list! Cannort preform a transparant object list read");
        }
        int retry = 0;

        while(true) {
            ByteArrayOutputStream baos = null;
            try {
                baos = new ByteArrayOutputStream();
                DataOutputStream daos = new DataOutputStream(baos);
                daos.writeByte(TRANSPARANT_OBJECT_LIST_READING_REQ_TAG);

                daos.write(abstractDLMS.getEncryptor().encrypt(ProtocolUtils.concatByteArrays(new byte[]{(byte)objectInfos.size()}, objectList2ByteArray())));

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
                        abstractDLMS.getLogger().severe(com.energyict.cbo.Utils.stack2string(e));
                    }
                }
            }
        }
    }

    private void validateResultCode(int resultCode) throws WaveFlowException {
        switch (resultCode) {
            case 0xFF:
                throw new WaveFlowDLMSException("Transparent object list read error. Bad request format.");
            case 0xFE:
                throw new WaveFlowExceptionNotPaired("Transparent object list read error. No data in buffer.");
        }
    }

    private final void parseResponse(byte[] sendData) throws IOException {

        //System.out.println("Received : "+ProtocolUtils.outputHexString(sendData));


        DataInputStream dais = null;
        try {
            dais = new DataInputStream(new ByteArrayInputStream(sendData));
            int responseTag = WaveflowProtocolUtils.toInt(dais.readByte());
            if (responseTag != TRANSPARANT_OBJECT_LIST_READING_RES_TAG) {
                throw new WaveFlowException("Transparant object list read error. Expected ["+WaveflowProtocolUtils.toHexString(TRANSPARANT_OBJECT_LIST_READING_RES_TAG)+"], received ["+WaveflowProtocolUtils.toHexString(responseTag)+"]");
            }
            byte[] temp = new byte[GenericHeader.size()];
            dais.read(temp);
            genericHeader = new GenericHeader(temp, abstractDLMS);

            int resultCode = WaveflowProtocolUtils.toInt(dais.readByte());
            validateResultCode(resultCode);

            frameCount=resultCode; // if > 1, multiframe. However, we only check if length > 135 bytes for multiframe indication


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
                    abstractDLMS.getLogger().severe(com.energyict.cbo.Utils.stack2string(e));
                }
            }
        }
    }

    void parse(byte[] data) throws IOException {


        //System.out.println("1-->"+ProtocolUtils.outputHexString(data));
        data = abstractDLMS.getEncryptor().decryptFrames(data,FIRST_FRAME_MAX_LENGTH,NEXT_FRAME_MAX_LENGTH);
        //System.out.println("2-->"+ProtocolUtils.outputHexString(data));

        int count=0;
        DataInputStream dais = null;
        byte[] temp;
        try {
            dais = new DataInputStream(new ByteArrayInputStream(data));
            int nrOfResults = WaveflowProtocolUtils.toInt(dais.readByte());
            int length = dais.available();
            // validate the HDLC frame(s) and extract the DLMS payload
            temp = new byte[length];
            dais.read(temp);
            if (length > MAX_LIST_MULTIFRAME_LENGTH) {
                temp = decodeMultiFrame(temp);
            }

            parseHDLCFrames(nrOfResults,temp);
        }
        finally {
            if (dais != null) {
                try {
                    dais.close();
                }
                catch(IOException e) {
                    abstractDLMS.getLogger().severe(com.energyict.cbo.Utils.stack2string(e));
                }
            }
        }
    }

    private final byte[] decodeMultiFrame(final byte[] multiFrameData) throws IOException {

        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        DataInputStream dais = null;
        try {
            dais = new DataInputStream(new ByteArrayInputStream(multiFrameData));

            byte[] temp = new byte[MAX_LIST_MULTIFRAME_LENGTH];
            dais.read(temp);
            baos.write(temp);

            int initialFrameCounter=0;

            while(true) {
                int multiframeTag = WaveflowProtocolUtils.toInt(dais.readByte());
                if (multiframeTag != LIST_TAG) {
                    throw new WaveFlowDLMSException("Transparant object list read error. Expected multiframe tag ["+WaveflowProtocolUtils.toHexString(LIST_TAG)+"], received ["+WaveflowProtocolUtils.toHexString(multiframeTag)+"]");
                }
                else {
                    boolean duplicate=false;
                    if (initialFrameCounter==0) {
                        // first multiframe, save frame counter...
                        initialFrameCounter = WaveflowProtocolUtils.toInt(dais.readByte());
                    }
                    else {
                        int frameCounter = WaveflowProtocolUtils.toInt(dais.readByte());
                        if (initialFrameCounter == frameCounter) {
                            // duplicate frame
                            duplicate=true;
                        }
                        else if ((initialFrameCounter-1) == frameCounter) {
                            initialFrameCounter--;
                        }
                        else {
                            throw new WaveFlowDLMSException("Transparant object get error. Multiframe sequence error. expected frame ["+WaveflowProtocolUtils.toHexString(initialFrameCounter)+"], received ["+WaveflowProtocolUtils.toHexString(frameCounter)+"]");
                        }
                    }

                    if (initialFrameCounter == 1) {
                        temp = new byte[dais.available()];
                        dais.read(temp);
                        if (!duplicate) baos.write(temp);
                        return baos.toByteArray();
                    }
                    else {
                        temp = new byte[MAX_LIST_MULTIFRAME_LENGTH];
                        dais.read(temp);
                        if (!duplicate) baos.write(temp);
                    }
                }
            }
        }
        finally {
            if (dais != null) {
                try {
                    dais.close();
                }
                catch(IOException e) {
                    abstractDLMS.getLogger().severe(com.energyict.cbo.Utils.stack2string(e));
                }
            }
        }
    }

    private final void parseHDLCFrames(final int nrOfResults,final byte[] data) throws IOException {
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        DataInputStream dais = null;

        try {
            dais = new DataInputStream(new ByteArrayInputStream(data));
            for (int i=0;i<nrOfResults;i++) {
                int classId = WaveflowProtocolUtils.toInt(dais.readShort());
                if (classId != objectInfos.get(i).getClassId()) {
                    throw new WaveFlowDLMSException("Transparant object list read error. Expected classId ["+WaveflowProtocolUtils.toHexString(objectInfos.get(i).getClassId())+"], received ["+WaveflowProtocolUtils.toHexString(classId)+"]");
                }

                byte[] temp = new byte[6];
                dais.read(temp);
                ObisCode obisCode = ObisCode.fromByteArray(temp);
                if (!obisCode.equals(objectInfos.get(i).getObisCode())) {
                    throw new WaveFlowDLMSException("Transparant object list read error. Expected obis code ["+objectInfos.get(i).getObisCode()+"], received ["+obisCode+"]");
                }

                int attribute = WaveflowProtocolUtils.toInt(dais.readByte());
                if (attribute != objectInfos.get(i).getAttribute()) {
                    throw new WaveFlowDLMSException("Transparant object list read error. Expected attributeId d["+WaveflowProtocolUtils.toHexString(objectInfos.get(i).getAttribute())+"], received ["+WaveflowProtocolUtils.toHexString(attribute)+"]");
                }

                int returnedDataLength = WaveflowProtocolUtils.toInt(dais.readByte());

                temp = new byte[returnedDataLength];
                dais.read(temp);

                XDLMSDataParser xdlmsParser = new XDLMSDataParser(abstractDLMS==null?null:abstractDLMS.getLogger());

                try {
                    parseAXDRObjectData(objectInfos.get(i),xdlmsParser.parseAXDRData(parseHDLCData(temp)));
                }
                catch(DataAccessResultException e) {
                    abstractDLMS.getLogger().warning("Error during list access reading ["+obisCode+"], ["+e.getMessage()+"]");
                }
            }
        }
        finally {
            if (dais != null) {
                try {
                    dais.close();
                }
                catch(IOException e) {
                    abstractDLMS.getLogger().severe(com.energyict.cbo.Utils.stack2string(e));
                }
            }
        }
    }

    /**
     * parce the HDLC data and return the DLMS data
     * @return DLMS data
     * @throws IOException
     */
    private final byte[] parseHDLCData(final byte[] hdlcData) throws IOException {

        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        int hdlcDataLength = hdlcData.length;
        int offset = 0;

        while(offset<hdlcDataLength) {
            HDLCFrameParser o = new HDLCFrameParser();
            o.parseFrame(hdlcData,offset);
            baos.write(o.getDLMSData());
            offset+=(o.getLength());
        }
        return baos.toByteArray();
    }

	
	
	/*
	 *      0001 0101600100FF 02 
	 *      0003 0101010800FF 02
	 *      0003 0101010800FF 03
	 *      0003 0101020800FF 02
	 *      0003 0101020800FF 03
	 *      1 = Data
	 *      3 = register
	 */

    private final void parseAXDRObjectData(final ObjectInfo objectInfo,final byte[] axdrData) throws IOException {
        AbstractDataType adt = AXDRDecoder.decode(axdrData);

        if (objectInfo.getClassId() == CLASS_DATA) {
            registerValues.remove(objectInfo.getObisCode());
            if (adt.isOctetString()) {
                registerValues.put(objectInfo.getObisCode(), new RegisterValue(objectInfo.getObisCode(), adt.getOctetString().stringValue()+" ["+ProtocolUtils.outputHexString(adt.getOctetString().getOctetStr())+"]"));
            }
            else if (adt.isVisibleString()) {
                registerValues.put(objectInfo.getObisCode(), new RegisterValue(objectInfo.getObisCode(), adt.getVisibleString().getStr()));
            }
            else {
                registerValues.put(objectInfo.getObisCode(), new RegisterValue(objectInfo.getObisCode(), new Quantity(adt.toBigDecimal(),Unit.get(""))));
            }
        }
        else if (objectInfo.getClassId() == CLASS_REGISTER) {
            RegisterValue registerValue = registerValues.get(objectInfo.getObisCode());

            BigDecimal value = BigDecimal.valueOf(0);
            Unit unit = Unit.get("");

            if (registerValue != null) {
                value = registerValue.getQuantity().getAmount();
                unit = registerValue.getQuantity().getUnit();
            }

            if (objectInfo.getAttribute() == ATTRIBUTE_SCALER) {
                int scale = adt.getStructure().getDataType(0).intValue();
                int code = adt.getStructure().getDataType(1).intValue();
                unit = Unit.get(code, scale);
            }
            else if (objectInfo.getAttribute() == ATTRIBUTE_VALUE) {
                value = adt.toBigDecimal();
            }

            Quantity quantity = new Quantity(value,unit);

            if (registerValue != null) {
                registerValue.setQuantity(quantity);
            }
            else {
                registerValues.put(objectInfo.getObisCode(), new RegisterValue(objectInfo.getObisCode(), quantity));
            }
        }
        else if (objectInfo.getClassId() == CLASS_EXTENDED_REGISTER) {
            RegisterValue registerValue = registerValues.get(objectInfo.getObisCode());

            BigDecimal value = BigDecimal.valueOf(0);
            Unit unit = Unit.get("");

            if (registerValue != null) {
                value = registerValue.getQuantity().getAmount();
                unit = registerValue.getQuantity().getUnit();
            }

            if (objectInfo.getAttribute() == ATTRIBUTE_SCALER) {
                int scale = adt.getStructure().getDataType(0).intValue();
                int code = adt.getStructure().getDataType(1).intValue();
                unit = Unit.get(code, scale);
            }
            else if (objectInfo.getAttribute() == ATTRIBUTE_VALUE) {
                value = adt.toBigDecimal();
            }

            Quantity quantity = new Quantity(value,unit);

            if (registerValue != null) {
                registerValue.setQuantity(quantity);
            }
            else {
                registerValues.put(objectInfo.getObisCode(), new RegisterValue(objectInfo.getObisCode(), quantity));
            }
        }
        else if (objectInfo.getClassId() == CLASS_GENERIC_PROFILE) {
            registerValues.put(objectInfo.getObisCode(), new RegisterValue(objectInfo.getObisCode(), new Quantity(adt.toBigDecimal(),Unit.get(""))));
        }
    }
//	
//	public static void main(String[] args) {
//		
//		byte[] data = new byte[]{(byte)0x0C,(byte)0x00,(byte)0x03,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x08,(byte)0x00,(byte)0xFF,(byte)0x02,(byte)0x15,(byte)0x7E,(byte)0xA0,(byte)0x13,(byte)0x23,(byte)0x20,(byte)0xAF,(byte)0x52,(byte)0xC4,(byte)0x2A,(byte)0xE6,(byte)0xE7,(byte)0x00,(byte)0xC4,(byte)0x01,(byte)0x81,(byte)0x00,(byte)0x11,(byte)0x00,(byte)0xAC,(byte)0x5B,(byte)0x7E,(byte)0x00,(byte)0x03,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x08,(byte)0x00,(byte)0xFF,(byte)0x03,(byte)0x19,(byte)0x7E,(byte)0xA0,(byte)0x17,(byte)0x23,(byte)0x20,(byte)0xAF,(byte)0x74,(byte)0xE0,(byte)0x43,(byte)0xE6,(byte)0xE7,(byte)0x00,(byte)0xC4,(byte)0x01,(byte)0x81,(byte)0x00,(byte)0x02,(byte)0x02,(byte)0x0F,(byte)0x03,(byte)0x16,(byte)0x1E,(byte)0xF4,(byte)0x5E,(byte)0x7E,(byte)0x00,(byte)0x03,(byte)0x01,(byte)0x01,(byte)0x02,(byte)0x08,(byte)0x00,(byte)0xFF,(byte)0x02,(byte)0x15,(byte)0x7E,(byte)0xA0,(byte)0x13,(byte)0x23,(byte)0x20,(byte)0xAF,(byte)0x96,(byte)0xEC,(byte)0xAA,(byte)0xE6,(byte)0xE7,(byte)0x00,(byte)0xC4,(byte)0x01,(byte)0x81,(byte)0x00,(byte)0x11,(byte)0x00,(byte)0xAC,(byte)0x5B,(byte)0x7E,(byte)0x00,(byte)0x03,(byte)0x01,(byte)0x01,(byte)0x02,(byte)0x08,(byte)0x00,(byte)0xFF,(byte)0x03,(byte)0x19,(byte)0x7E,(byte)0xA0,(byte)0x17,(byte)0x23,(byte)0x20,(byte)0xAF,(byte)0xB8,(byte)0x80,(byte)0x4F,(byte)0xE6,(byte)0xE7,(byte)0x00,(byte)0xC4,(byte)0x01,(byte)0x81,(byte)0x00,(byte)0x02,(byte)0x02,(byte)0x0F,(byte)0x03,(byte)0x16,(byte)0x1E,(byte)0xF4,(byte)0x5E,(byte)0x7E,(byte)0x00,(byte)0x03,(byte)0x01,(byte)0xB6,(byte)0x02,(byte)0x01,(byte)0x03,(byte)0x08,(byte)0x00,(byte)0xFF,(byte)0x02,(byte)0x15,(byte)0x7E,(byte)0xA0,(byte)0x13,(byte)0x23,(byte)0x20,(byte)0xAF,(byte)0xDA,(byte)0x84,(byte)0x22,(byte)0xE6,(byte)0xE7,(byte)0x00,(byte)0xC4,(byte)0x01,(byte)0x81,(byte)0x00,(byte)0x11,(byte)0x00,(byte)0xAC,(byte)0x5B,(byte)0x7E,(byte)0x00,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x60,(byte)0x01,(byte)0x00,(byte)0xFF,(byte)0x02,(byte)0x18,(byte)0x7E,(byte)0xA0,(byte)0x16,(byte)0x23,(byte)0x20,(byte)0xAF,(byte)0xFC,(byte)0xE4,(byte)0x40,(byte)0xE6,(byte)0xE7,(byte)0x00,(byte)0xC4,(byte)0x01,(byte)0x81,(byte)0x00,(byte)0x06,(byte)0x00,(byte)0x30,(byte)0xEB,(byte)0xB7,(byte)0xD0,(byte)0x95,(byte)0x7E,(byte)0x00,(byte)0x03,(byte)0x01,(byte)0x01,(byte)0x20,(byte)0x07,(byte)0x00,(byte)0xFF,(byte)0x02,(byte)0x15,(byte)0x7E,(byte)0xA0,(byte)0x13,(byte)0x23,(byte)0x20,(byte)0xAF,(byte)0x1E,(byte)0xAC,(byte)0xA2,(byte)0xE6,(byte)0xE7,(byte)0x00,(byte)0xC4,(byte)0x01,(byte)0x81,(byte)0x00,(byte)0x11,(byte)0x00,(byte)0xAC,(byte)0x5B,(byte)0x7E,(byte)0x00,(byte)0x03,(byte)0x01,(byte)0x01,(byte)0x34,(byte)0x07,(byte)0x00,(byte)0xFF,(byte)0x02,(byte)0x15,(byte)0x7E,(byte)0xA0,(byte)0x13,(byte)0x23,(byte)0x20,(byte)0xAF,(byte)0x30,(byte)0xD0,(byte)0x6A,(byte)0xE6,(byte)0xE7,(byte)0x00,(byte)0xC4,(byte)0x01,(byte)0x81,(byte)0x00,(byte)0x11,(byte)0x00,(byte)0xAC,(byte)0x5B,(byte)0x7E,(byte)0x00,(byte)0x03,(byte)0x01,(byte)0x01,(byte)0x48,(byte)0x07,(byte)0x00,(byte)0xFF,(byte)0x02,(byte)0x16,(byte)0x7E,(byte)0xB6,(byte)0x01,(byte)0xA0,(byte)0x14,(byte)0x23,(byte)0x20,(byte)0xAF,(byte)0x52,(byte)0x18,(byte)0x1A,(byte)0xE6,(byte)0xE7,(byte)0x00,(byte)0xC4,(byte)0x01,(byte)0x81,(byte)0x00,(byte)0x12,(byte)0x09,(byte)0x3A,(byte)0xE0,(byte)0x39,(byte)0x7E,(byte)0x00,(byte)0x03,(byte)0x01,(byte)0x01,(byte)0x1F,(byte)0x07,(byte)0x00,(byte)0xFF,(byte)0x02,(byte)0x15,(byte)0x7E,(byte)0xA0,(byte)0x13,(byte)0x23,(byte)0x20,(byte)0xAF,(byte)0x74,(byte)0xF0,(byte)0x6E,(byte)0xE6,(byte)0xE7,(byte)0x00,(byte)0xC4,(byte)0x01,(byte)0x81,(byte)0x00,(byte)0x11,(byte)0x00,(byte)0xAC,(byte)0x5B,(byte)0x7E,(byte)0x00,(byte)0x03,(byte)0x01,(byte)0x01,(byte)0x33,(byte)0x07,(byte)0x00,(byte)0xFF,(byte)0x02,(byte)0x15,(byte)0x7E,(byte)0xA0,(byte)0x13,(byte)0x23,(byte)0x20,(byte)0xAF,(byte)0x96,(byte)0xEC,(byte)0xAA,(byte)0xE6,(byte)0xE7,(byte)0x00,(byte)0xC4,(byte)0x01,(byte)0x81,(byte)0x00,(byte)0x11,(byte)0x00,(byte)0xAC,(byte)0x5B,(byte)0x7E,(byte)0x00,(byte)0x03,(byte)0x01,(byte)0x01,(byte)0x47,(byte)0x07,(byte)0x00,(byte)0xFF,(byte)0x02,(byte)0x15,(byte)0x7E,(byte)0xA0,(byte)0x13,(byte)0x23,(byte)0x20,(byte)0xAF,(byte)0xB8,(byte)0x90,(byte)0x62,(byte)0xE6,(byte)0xE7,(byte)0x00,(byte)0xC4,(byte)0x01,(byte)0x81,(byte)0x00,(byte)0x11,(byte)0x00,(byte)0xAC,(byte)0x5B,(byte)0x7E};
//		
//		TransparentObjectListRead o;
//		try {
//	    	List<ObjectInfo> objectInfos = new ArrayList<ObjectInfo>();
//	    	objectInfos.add(new ObjectInfo(2, 3, ObisCode.fromString("1.1.1.8.0.255")));
//	    	objectInfos.add(new ObjectInfo(3, 3, ObisCode.fromString("1.1.1.8.0.255")));
//	    	objectInfos.add(new ObjectInfo(2, 3, ObisCode.fromString("1.1.2.8.0.255")));
//	    	objectInfos.add(new ObjectInfo(3, 3, ObisCode.fromString("1.1.2.8.0.255")));
//	    	objectInfos.add(new ObjectInfo(2, 3, ObisCode.fromString("1.1.3.8.0.255")));
//	    	objectInfos.add(new ObjectInfo(2, 1, ObisCode.fromString("1.1.96.1.0.255")));
//	    	objectInfos.add(new ObjectInfo(2, 3, ObisCode.fromString("1.1.32.7.0.255")));
//	    	objectInfos.add(new ObjectInfo(2, 3, ObisCode.fromString("1.1.52.7.0.255")));
//	    	objectInfos.add(new ObjectInfo(2, 3, ObisCode.fromString("1.1.72.7.0.255")));
//	    	objectInfos.add(new ObjectInfo(2, 3, ObisCode.fromString("1.1.31.7.0.255")));
//	    	objectInfos.add(new ObjectInfo(2, 3, ObisCode.fromString("1.1.51.7.0.255")));
//	    	objectInfos.add(new ObjectInfo(2, 3, ObisCode.fromString("1.1.71.7.0.255")));
//	    	
//	    	o = new TransparentObjectListRead(null,objectInfos);
//			o.parse(data);
//			
//			for (RegisterValue rv : o.getRegisterValues().values()) {
//				System.out.println(rv);
//			}
//			
//			
//		} catch (WaveFlowDLMSException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		
//	}



}
