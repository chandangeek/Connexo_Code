package com.energyict.protocolimpl.coronis.waveflowDLMS;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.AxdrType;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class TransparentGet extends AbstractTransparentObjectAccess {

    private final ObjectInfo objectInfo;

    private final int MAX_MULTIFRAME_LENGTH=132;
    private final int MULTIFRAME_TAG=0xB1;

    /**
     * The parsed DLMS AXDR decoded data type.
     */
    private AbstractDataType dataType;

    /**
     * In case of selective access with range descriptor
     */
    private Date fromDate=null;
	private Date toDate=null;

    /**
     * In case of selective access entry descriptor
     */
    private int fromEntry=-1;
    private int offset = 0;


    final void setFromEntry(int fromEntry) {
        this.fromEntry = fromEntry;
    }

    final void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    final AbstractDataType getDataType() {
        return dataType;
    }

    TransparentGet(AbstractDLMS abstractDLMS, ObjectInfo objectInfo) {
        super(abstractDLMS);
        this.objectInfo=objectInfo;
    }

    @Override
    InteractionParameter getInteractionParameter() {
        return InteractionParameter.GET;
    }

    private byte[] decodeMultiFrame(byte[] multiFrameData) throws IOException {

        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        DataInputStream dais = null;
        try {
            dais = new DataInputStream(new ByteArrayInputStream(multiFrameData));

            byte[] temp = new byte[MAX_MULTIFRAME_LENGTH];
            dais.read(temp);
            baos.write(temp);

            int initialFrameCounter=0;

            while(true) {


                int multiframeTag = WaveflowProtocolUtils.toInt(dais.readByte());
                if (multiframeTag != MULTIFRAME_TAG) {
                    throw new WaveFlowDLMSException("Transparant object get error. Expected multiframe tag ["+WaveflowProtocolUtils.toHexString(MULTIFRAME_TAG)+"], received ["+WaveflowProtocolUtils.toHexString(multiframeTag)+"]");
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
                        if (!duplicate) {
                            baos.write(temp);
                        }
                        return baos.toByteArray();
                    }
                    else {
                        temp = new byte[MAX_MULTIFRAME_LENGTH];
                        dais.read(temp);
                        if (!duplicate) {
                            baos.write(temp);
                        }
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
                    abstractDLMS.getLogger().severe(ProtocolUtils.stack2string(e));
                }
            }
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



    @Override
    void parse(byte[] data) throws IOException {

        data = abstractDLMS.getEncryptor().decryptFrames(data,FIRST_FRAME_MAX_LENGTH,NEXT_FRAME_MAX_LENGTH);

        DataInputStream dais = null;
        try {
            dais = new DataInputStream(new ByteArrayInputStream(data));

            int classId = WaveflowProtocolUtils.toInt(dais.readShort());
            if (classId != objectInfo.getClassId()) {
                throw new WaveFlowDLMSException("Transparant object get error. Expected classId ["+WaveflowProtocolUtils.toHexString(objectInfo.getClassId())+"], received ["+WaveflowProtocolUtils.toHexString(classId)+"]");
            }

            byte[] temp = new byte[6];
            dais.read(temp);
            ObisCode obisCode = ObisCode.fromByteArray(temp);
            if (!obisCode.equals(objectInfo.getObisCode())) {
                throw new WaveFlowDLMSException("Transparant object get error. Expected obis code ["+objectInfo.getObisCode()+"], received ["+obisCode+"]");
            }

            int attribute = WaveflowProtocolUtils.toInt(dais.readByte());
            if (attribute != objectInfo.getAttribute()) {
                throw new WaveFlowDLMSException("Transparant object get error. Expected attributeId d["+WaveflowProtocolUtils.toHexString(objectInfo.getAttribute())+"], received ["+WaveflowProtocolUtils.toHexString(attribute)+"]");
            }



            int length = WaveflowProtocolUtils.toInt(dais.readShort());
            //int length = dais.available();


            // validate the HDLC frame(s) and extract the DLMS payload
            temp = new byte[dais.available()];
            dais.read(temp);
            if (length > MAX_MULTIFRAME_LENGTH) {
                temp = decodeMultiFrame(temp);
                //System.out.println("temp: "+ProtocolUtils.outputHexString(temp));
            }



            byte[] dlmsData = parseHDLCData(temp);

//			System.out.println("KV_DEBUG> dlmsData "+ProtocolUtils.outputHexString(dlmsData));

            XDLMSDataParser xdlmsParser = new XDLMSDataParser(abstractDLMS==null?null:abstractDLMS.getLogger());
            byte[] axdrData = xdlmsParser.parseAXDRData(dlmsData);


//			System.out.println("KV_DEBUG> axdrData "+ProtocolUtils.outputHexString(axdrData));
            //System.out.println("axdrData: "+ProtocolUtils.outputHexString(axdrData));
            dataType = AXDRDecoder.decode(axdrData);

//			System.out.println("KV_DEBUG> dataType: "+dataType);

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

    /**
     * parce the HDLC data and return the DLMS data
     * @return DLMS data
     * @throws IOException
     */
    private byte[] parseHDLCData(byte[] hdlcData) throws IOException {

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

            if (fromDate != null) {

                TimeZone timeZone = abstractDLMS==null?TimeZone.getTimeZone("GMT"):abstractDLMS.getTimeZone();

                Calendar fromCalendar = Calendar.getInstance(timeZone);
                fromCalendar.setTime(fromDate);

                Calendar toCalendar = Calendar.getInstance(timeZone);
                if (toDate != null) {
                    toCalendar.setTime(toDate);
                }

				byte[] rangeData = getBufferRangeDescriptorDefault(fromCalendar,toCalendar,timeZone);
                daos.writeByte(rangeData.length);
                daos.write(rangeData);
            }
            else {

                if (fromEntry != -1) {
                    byte[] rangeData = getBufferEntryDescriptorDefault(fromEntry, offset);
                    daos.writeByte(rangeData.length);
                    daos.write(rangeData);
                }
                else {
                    daos.writeByte(0);
                }
            }
            return baos.toByteArray();
        }
        finally {
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


    private final byte RANGE_DESCRIPTOR=1;
    private final byte ENTRY_DESCRIPTOR=2;

    /**
     * @param from
     * @param to
     * @return BER encoded byte array
     */
    private byte[] getBufferEntryDescriptorDefault(int from, int to) {
        Structure s = new Structure();
        s.addDataType(new Unsigned32(to));
        s.addDataType(new Unsigned32(from));
        s.addDataType(new Unsigned16(0));
        s.addDataType(new Unsigned16(0));
        return ProtocolUtils.concatByteArrays(new byte[]{ENTRY_DESCRIPTOR},s.getBEREncodedByteArray());
    }

    /**
     * @param fromCalendar
     * @param toCalendar
     * @return
     */
    private byte[] getBufferRangeDescriptorDefault(Calendar fromCalendar, Calendar toCalendar, TimeZone timeZone) {

        byte[] intreq = {
                RANGE_DESCRIPTOR, // range descriptor
                (byte) 0x02, // structure
                (byte) 0x04, // 4 items in structure
                // capture object definition
                (byte) 0x02, (byte) 0x04, (byte) 0x12, (byte) 0x00, (byte) 0x08, (byte) 0x09, (byte) 0x06, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x00,
                (byte) 0x00, (byte) 0xFF, (byte) 0x0F, (byte) 0x02, (byte) 0x12, (byte) 0x00,
                (byte) 0x00,
                // from value
                (byte) 0x09, (byte) 0x0C, (byte) 0x07, (byte) 0xD2, (byte) 0x05, (byte) 23, (byte) 0xFF, (byte) 11, (byte) 0x00, (byte) 0x00, (byte) 0xFF,
                (byte) 0x80, (byte) 0x00, (byte) 0x00,
                // to value
                (byte) 0x09, (byte) 0x0C, (byte) 0x07, (byte) 0xD2, (byte) 0x05, (byte) 23, (byte) 0xFF, (byte) 13, (byte) 0x00, (byte) 0x00, (byte) 0xFF,
                (byte) 0x80, (byte) 0x00, (byte) 0x00,
                // selected values
                (byte) 0x01, (byte) 0x00 };

        int CAPTURE_FROM_OFFSET = 21;
        int CAPTURE_TO_OFFSET = 35;

        intreq[CAPTURE_FROM_OFFSET] = AxdrType.OCTET_STRING.getTag();
        intreq[CAPTURE_FROM_OFFSET + 1] = 12; // length
        intreq[CAPTURE_FROM_OFFSET + 2] = (byte) (fromCalendar.get(Calendar.YEAR) >> 8);
        intreq[CAPTURE_FROM_OFFSET + 3] = (byte) fromCalendar.get(Calendar.YEAR);
        intreq[CAPTURE_FROM_OFFSET + 4] = (byte) (fromCalendar.get(Calendar.MONTH) + 1);
        intreq[CAPTURE_FROM_OFFSET + 5] = (byte) fromCalendar.get(Calendar.DAY_OF_MONTH);
        //             bDOW = (byte)fromCalendar.get(Calendar.DAY_OF_WEEK);
        //             intreq[CAPTURE_FROM_OFFSET+6]=bDOW--==1?(byte)7:bDOW;
        intreq[CAPTURE_FROM_OFFSET + 6] = (byte) 0xff;
        intreq[CAPTURE_FROM_OFFSET + 7] = (byte) fromCalendar.get(Calendar.HOUR_OF_DAY);
        intreq[CAPTURE_FROM_OFFSET + 8] = (byte) fromCalendar.get(Calendar.MINUTE);
        //             intreq[CAPTURE_FROM_OFFSET+9]=(byte)fromCalendar.get(Calendar.SECOND);
        intreq[CAPTURE_FROM_OFFSET + 9] = 0x01;

        intreq[CAPTURE_FROM_OFFSET + 10] = (byte) 0xFF;
        intreq[CAPTURE_FROM_OFFSET + 11] = (byte) 0x80;
        intreq[CAPTURE_FROM_OFFSET + 12] = 0x00;

        if (timeZone.inDaylightTime(fromCalendar.getTime())) {
            intreq[CAPTURE_FROM_OFFSET + 13] = (byte) 0x80;
        } else {
            intreq[CAPTURE_FROM_OFFSET + 13] = 0x00;
        }

        intreq[CAPTURE_TO_OFFSET] = AxdrType.OCTET_STRING.getTag();
        intreq[CAPTURE_TO_OFFSET + 1] = 12; // length
        intreq[CAPTURE_TO_OFFSET + 2] = toCalendar != null ? (byte) (toCalendar.get(Calendar.YEAR) >> 8) : (byte) 0xFF;
        intreq[CAPTURE_TO_OFFSET + 3] = toCalendar != null ? (byte) toCalendar.get(Calendar.YEAR) : (byte) 0xFF;
        intreq[CAPTURE_TO_OFFSET + 4] = toCalendar != null ? (byte) (toCalendar.get(Calendar.MONTH) + 1) : (byte) 0xFF;
        intreq[CAPTURE_TO_OFFSET + 5] = toCalendar != null ? (byte) toCalendar.get(Calendar.DAY_OF_MONTH) : (byte) 0xFF;
        intreq[CAPTURE_TO_OFFSET + 6] = (byte) 0xFF;
        intreq[CAPTURE_TO_OFFSET + 7] = toCalendar != null ? (byte) toCalendar.get(Calendar.HOUR_OF_DAY) : (byte) 0xFF;
        intreq[CAPTURE_TO_OFFSET + 8] = toCalendar != null ? (byte) toCalendar.get(Calendar.MINUTE) : (byte) 0xFF;
        intreq[CAPTURE_TO_OFFSET + 9] = 0x00;
        intreq[CAPTURE_TO_OFFSET + 10] = (byte) 0xFF;
        intreq[CAPTURE_TO_OFFSET + 11] = (byte) 0x80;
        intreq[CAPTURE_TO_OFFSET + 12] = 0x00;

        if ((toCalendar != null) && timeZone.inDaylightTime(toCalendar.getTime())) {
            intreq[CAPTURE_TO_OFFSET + 13] = (byte) 0x80;
        } else {
            intreq[CAPTURE_TO_OFFSET + 13] = 0x00;
        }

        return intreq;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

}