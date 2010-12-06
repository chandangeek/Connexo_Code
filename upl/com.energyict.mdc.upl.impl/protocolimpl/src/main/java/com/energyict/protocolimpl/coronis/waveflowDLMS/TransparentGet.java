package com.energyict.protocolimpl.coronis.waveflowDLMS;

import java.io.*;
import java.util.*;

import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;

public class TransparentGet extends AbstractTransparentObjectAccess implements DLMSCOSEMGlobals {

	private final ObjectInfo objectInfo;
	
	private final int DLMS_GET_RESPONSE=0xC4;
	private final int DLMS_RESPONSE_TAG_OFFSET=3;
	private final int DLMS_RESPONSE_STATUS_CODE_OFFSET=6;
	private final int DLMS_RESPONSE_RESULT_CODE_OFFSET=7;

	
	private final int MAX_MULTIFRAME_LENGTH=132;
	private final int MULTIFRAME_TAG=0xB1;
	
	/**
	 * The parsed DLMS AXDR decoded data type.
	 */
	private AbstractDataType dataType;
	
	private Date fromDate=null;
	
	
	final void setFromDate(Date fromDate) {
		this.fromDate = fromDate;
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
					
					if (initialFrameCounter==0) {
						// first multiframe, save frame counter...
						initialFrameCounter = WaveflowProtocolUtils.toInt(dais.readByte());
					}
					else {
						initialFrameCounter--;
						int frameCounter = WaveflowProtocolUtils.toInt(dais.readByte());
						if (initialFrameCounter != frameCounter) {
							throw new WaveFlowDLMSException("Transparant object get error. Mmultiframe seque'nce error. expected frame ["+WaveflowProtocolUtils.toHexString(initialFrameCounter)+"], received ["+WaveflowProtocolUtils.toHexString(frameCounter)+"]");
						}
					}
					
					if (initialFrameCounter == 1) {
						temp = new byte[dais.available()];
						dais.read(temp);
						baos.write(temp);
						return baos.toByteArray();
					}
					else {
						temp = new byte[MAX_MULTIFRAME_LENGTH];
						dais.read(temp);
						baos.write(temp);					}
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
	
	
	
	@Override
	void parse(byte[] data) throws IOException {

		//System.out.println("multiFrameData: "+ProtocolUtils.outputHexString(data));
		
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

			int length = WaveflowProtocolUtils.toInt(dais.readShort()); // don't need the length

			
			// validate the HDLC frame(s) and extract the DLMS payload
			temp = new byte[dais.available()];
			dais.read(temp);
			int padding=1;
			if (length > MAX_MULTIFRAME_LENGTH) {
				temp = decodeMultiFrame(temp);
				//System.out.println("temp: "+ProtocolUtils.outputHexString(temp));
				//padding=9;
			}

			byte[] dlmsData = parseHDLCData(temp);
			
			//System.out.println("dlmsData: "+ProtocolUtils.outputHexString(dlmsData));
			
			if (WaveflowProtocolUtils.toInt(dlmsData[DLMS_RESPONSE_TAG_OFFSET]) != DLMS_GET_RESPONSE) {
				throw new WaveFlowDLMSException("Transparant object get error. Expected DLMS tag [C4], received["+WaveflowProtocolUtils.toHexString(dlmsData[DLMS_RESPONSE_TAG_OFFSET])+"]");
			}
			int statusCode=WaveflowProtocolUtils.toInt(dlmsData[DLMS_RESPONSE_STATUS_CODE_OFFSET]);
			if (statusCode != 0) {
				throw new DataAccessResultException(WaveflowProtocolUtils.toInt(dlmsData[DLMS_RESPONSE_RESULT_CODE_OFFSET]));
			}
			
			// DEBUG skipped 3 nulls...
			byte[] axdrData = ProtocolUtils.getSubArray(dlmsData, DLMS_RESPONSE_STATUS_CODE_OFFSET+padding);
			//System.out.println("axdrData: "+ProtocolUtils.outputHexString(axdrData));
			dataType = AXDRDecoder.decode(axdrData);
			
			System.out.println("dataType: "+dataType);
			
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
				Calendar toCalendar = Calendar.getInstance(abstractDLMS.getTimeZone());
				toCalendar.setTime(fromDate);
				byte[] rangeData = getBufferRangeDescriptorDefault(Calendar.getInstance(abstractDLMS.getTimeZone()), toCalendar);
				daos.writeByte(rangeData.length);
				daos.write(rangeData);
			}
			else {
				daos.writeByte(0);
			}
			return baos.toByteArray();
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
	
	
	/**
	 * @param fromCalendar
	 * @param toCalendar
	 * @return
	 */
	private byte[] getBufferRangeDescriptorDefault(Calendar fromCalendar, Calendar toCalendar) {

		byte[] intreq = {
				(byte) 0x01, // range descriptor
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

		intreq[CAPTURE_FROM_OFFSET] = TYPEDESC_OCTET_STRING;
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

		if (abstractDLMS.getTimeZone().inDaylightTime(fromCalendar.getTime())) {
			intreq[CAPTURE_FROM_OFFSET + 13] = (byte) 0x80;
		} else {
			intreq[CAPTURE_FROM_OFFSET + 13] = 0x00;
		}

		intreq[CAPTURE_TO_OFFSET] = TYPEDESC_OCTET_STRING;
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

        if ((toCalendar != null) && abstractDLMS.getTimeZone().inDaylightTime(toCalendar.getTime())) {
            intreq[CAPTURE_TO_OFFSET + 13] = (byte) 0x80;
        } else {
            intreq[CAPTURE_TO_OFFSET + 13] = 0x00;
        }

        return intreq;
	}	
	
	public static void main(String[] args) {
		
		
		byte[] data = new byte[]{(byte)0xE6,(byte)0xE7,(byte)0x00,(byte)0xC4,(byte)0x02,
				                 (byte)0x81,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
				                 (byte)0x01,(byte)0x00,(byte)0x82,(byte)0x03,(byte)0x57,
				                 (byte)0x01,(byte)0x82,(byte)0x00,(byte)0x28,
				                 (byte)0x02,(byte)0x04,(byte)0x12,(byte)0x00,(byte)0x01,(byte)0x11,(byte)0x00,(byte)0x09,(byte)0x06,(byte)0x01,(byte)0x01,(byte)0x60,(byte)0x01,(byte)0x00,(byte)0xFF,(byte)0x02,(byte)0x02,(byte)0x01,(byte)0x02,(byte)0x02,(byte)0x03,(byte)0x0F,(byte)0x01,(byte)0x16,(byte)0x01,(byte)0x00,(byte)0x02,(byte)0x03,(byte)0x0F,(byte)0x02,(byte)0x16,(byte)0x01,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x02,(byte)0x04,(byte)0x12,(byte)0x00,(byte)0x01,(byte)0x11,(byte)0x00,(byte)0x09,(byte)0x06,(byte)0x01,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0xFF,(byte)0x02,(byte)0x02,(byte)0x01,(byte)0x02,(byte)0x02,(byte)0x03,(byte)0x0F,(byte)0x01,(byte)0x16,(byte)0x01,(byte)0x00,(byte)0x02,(byte)0x03,(byte)0x0F,(byte)0x02,(byte)0x16,(byte)0x01,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x02,(byte)0x04,(byte)0x12,(byte)0x00,(byte)0x01,(byte)0x11,(byte)0x00,(byte)0x09,(byte)0x06,(byte)0x01,(byte)0x01,(byte)0x00,(byte)0x09,(byte)0x01,(byte)0xFF,(byte)0x02,(byte)0x02,(byte)0x01,(byte)0x02,(byte)0x02,(byte)0x03,(byte)0x0F,(byte)0x01,(byte)0x16,(byte)0x01,(byte)0x00,(byte)0x02,(byte)0x03,(byte)0x0F,(byte)0x02,(byte)0x16,(byte)0x01,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x02,(byte)0x04,(byte)0x12,(byte)0x00,(byte)0x01,(byte)0x11,(byte)0x00,(byte)0x09,(byte)0x06,(byte)0x01,(byte)0x01,(byte)0x00,(byte)0x09,(byte)0x02,(byte)0xFF,(byte)0x02,(byte)0x02,(byte)0x01,(byte)0x02,(byte)0x02,(byte)0x03,(byte)0x0F,(byte)0x01,(byte)0x16,(byte)0x01,(byte)0x00,(byte)0x02,(byte)0x03,(byte)0x0F,(byte)0x02,(byte)0x16,(byte)0x01,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x02,(byte)0x04,(byte)0x12,(byte)0x00,(byte)0x03,(byte)0x11,(byte)0x00,(byte)0x09,(byte)0x06,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x08,(byte)0x00,(byte)0xFF,(byte)0x02,(byte)0x02,(byte)0x01,(byte)0x03,(byte)0x02,(byte)0x03,(byte)0x0F,(byte)0x01,(byte)0x16,(byte)0x01,(byte)0x00,(byte)0x02,(byte)0x03,(byte)0x0F,(byte)0x02,(byte)0x16,(byte)0x01,(byte)0x00,(byte)0x02,(byte)0x03,(byte)0x0F,(byte)0x03,(byte)0x16,(byte)0x01,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x02,(byte)0x04,(byte)0x12,(byte)0x00,(byte)0x03,(byte)0x11,(byte)0x00,(byte)0x09,(byte)0x06,(byte)0x01,(byte)0x01,(byte)0x02,(byte)0x08,(byte)0x00,(byte)0xFF,(byte)0x02,(byte)0x02,(byte)0x01,(byte)0x03,(byte)0x02,(byte)0x03,(byte)0x0F,(byte)0x01,(byte)0x16,(byte)0x01,(byte)0x00,(byte)0x02,(byte)0x03,(byte)0x0F,(byte)0x02,(byte)0x16,(byte)0x01,(byte)0x00,(byte)0x02,(byte)0x03,(byte)0x0F,(byte)0x03,(byte)0x16,(byte)0x01,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x02,(byte)0x04,(byte)0x12,(byte)0x00,(byte)0x03,(byte)0x11,(byte)0x00,(byte)0x09,(byte)0x06,(byte)0x01,(byte)0x01,(byte)0x03,(byte)0x08,(byte)0x00,(byte)0xFF,(byte)0x02,(byte)0x02,(byte)0x01,(byte)0x03,(byte)0x02,(byte)0x03,(byte)0x0F,(byte)0x01,(byte)0x16,(byte)0x01,(byte)0x00,(byte)0x02,(byte)0x03,(byte)0x0F,(byte)0x02,(byte)0x16,(byte)0x01,(byte)0x00,(byte)0x02,(byte)0x03,(byte)0x0F,(byte)0x03,(byte)0x16,(byte)0x01,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x02,(byte)0x04,(byte)0x12,(byte)0x00,(byte)0x04,(byte)0x11,(byte)0x00,(byte)0x09,(byte)0x06,(byte)0x01,(byte)0x01,(byte)0x09,(byte)0x06,(byte)0x01,(byte)0xFF,(byte)0x02,(byte)0x02,(byte)0x01,(byte)0x04,(byte)0x02,(byte)0x03,(byte)0x0F,(byte)0x01,(byte)0x16,(byte)0x01,(byte)0x00,(byte)0x02,(byte)0x03,(byte)0x0F,(byte)0x02,(byte)0x16,(byte)0x01,(byte)0x00,(byte)0x02,(byte)0x03,(byte)0x0F,(byte)0x03,(byte)0x16,(byte)0x01,(byte)0x00,(byte)0x02,(byte)0x03,(byte)0x0F,(byte)0x05,(byte)0x16,(byte)0x01,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x02,(byte)0x04,(byte)0x12,(byte)0x00,(byte)0x01,(byte)0x11,(byte)0x00,(byte)0x09,(byte)0x06,(byte)0x01,(byte)0x01,(byte)0x61,(byte)0x61,(byte)0xFF,(byte)0xFF,(byte)0x02,(byte)0x02,(byte)0x01,(byte)0x02,(byte)0x02,(byte)0x03,(byte)0x0F,(byte)0x01,(byte)0x16,(byte)0x01,(byte)0x00,(byte)0x02,(byte)0x03,(byte)0x0F,(byte)0x02,(byte)0x16,(byte)0x01,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x02,(byte)0x04,(byte)0x12,(byte)0x00,(byte)0x01,(byte)0x11,(byte)0x00,(byte)0x09,(byte)0x06,(byte)0x01,(byte)0x01,(byte)0x61,(byte)0x61,(byte)0x01,(byte)0xFF,(byte)0x02,(byte)0x02,(byte)0x01,(byte)0x02,(byte)0x02,(byte)0x03,(byte)0x0F,(byte)0x01,(byte)0x16,(byte)0x01,(byte)0x00,(byte)0x02,(byte)0x03,(byte)0x0F,(byte)0x02,(byte)0x16,(byte)0x01,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x02,(byte)0x04,(byte)0x12,(byte)0x00,(byte)0x01,(byte)0x11,(byte)0x00,(byte)0x09,(byte)0x06,(byte)0x01,(byte)0x01,(byte)0x61,(byte)0x61,(byte)0x02,(byte)0xFF,(byte)0x02,(byte)0x02,(byte)0x01,(byte)0x02,(byte)0x02,(byte)0x03,(byte)0x0F,(byte)0x01,(byte)0x16,(byte)0x01,(byte)0x00,(byte)0x02,(byte)0x03,(byte)0x0F,(byte)0x02,(byte)0x16,(byte)0x01,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x02,(byte)0x04,(byte)0x12,(byte)0x00,(byte)0x01,(byte)0x11,(byte)0x00,(byte)0x09,(byte)0x06,(byte)0x01,(byte)0x01,(byte)0x61,(byte)0x61,(byte)0x03,(byte)0xFF,(byte)0x02,(byte)0x02,(byte)0x01,(byte)0x02,(byte)0x02,(byte)0x03,(byte)0x0F,(byte)0x01,(byte)0x16,(byte)0x01,(byte)0x00,(byte)0x02,(byte)0x03,(byte)0x0F,(byte)0x02,(byte)0x16,(byte)0x01,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x02,(byte)0x04,(byte)0x12,(byte)0x00,(byte)0x01,(byte)0x11,(byte)0x00,(byte)0x09,(byte)0x06,(byte)0x01,(byte)0x01,(byte)0x60,(byte)0x03,(byte)0x00,(byte)0xFF,(byte)0x02,(byte)0x02,(byte)0x01,(byte)0x02,(byte)0x02,(byte)0x03,(byte)0x0F,(byte)0x01,(byte)0x16,(byte)0x01,(byte)0x00,(byte)0x02,(byte)0x03,(byte)0x0F,(byte)0x02,(byte)0x16,(byte)0x01,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x02,(byte)0x04,(byte)0x12,(byte)0x00,(byte)0x01,(byte)0x11,(byte)0x00,(byte)0x09,(byte)0x06,(byte)0x01,(byte)0x01,(byte)0x60,(byte)0x38,(byte)0xFF,(byte)0xFF,(byte)0x02,(byte)0x02,(byte)0x01,(byte)0x02,(byte)0x02,(byte)0x03,(byte)0x0F,(byte)0x01,(byte)0x16,(byte)0x01,(byte)0x00,(byte)0x02,(byte)0x03,(byte)0x0F,(byte)0x02,(byte)0x16,(byte)0x01,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x02,(byte)0x04,(byte)0x12,(byte)0x00,(byte)0x07,(byte)0x11,(byte)0x01,(byte)0x09,(byte)0x06,(byte)0x01,(byte)0x01,(byte)0x63,(byte)0x62,(byte)0x00,(byte)0xFF,(byte)0x02,(byte)0x02,(byte)0x01,(byte)0x04,(byte)0x02,(byte)0x03,(byte)0x0F,(byte)0x01,(byte)0x16,(byte)0x01,(byte)0x00,(byte)0x02,(byte)0x03,(byte)0x0F,(byte)0x02,(byte)0x16,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x0F,(byte)0x01,(byte)0x02,(byte)0x03,(byte)0x0F,(byte)0x03,(byte)0x16,(byte)0x01,(byte)0x00,(byte)0x02,(byte)0x03,(byte)0x0F,(byte)0x04,(byte)0x16,(byte)0x01,(byte)0x00,(byte)0x01,(byte)0x01,(byte)0x02,(byte)0x02,(byte)0x0F,(byte)0x01,(byte)0x03,(byte)0x01,(byte)0x02,(byte)0x04,(byte)0x12,(byte)0x00,(byte)0x07,(byte)0x11,(byte)0x01,(byte)0x09,(byte)0x06,(byte)0x01,(byte)0x01,(byte)0x63,(byte)0x01,(byte)0x00,(byte)0xFF,(byte)0x02,(byte)0x02,(byte)0x01,(byte)0x04,(byte)0x02,(byte)0x03,(byte)0x0F,(byte)0x01,(byte)0x16,(byte)0x01,(byte)0x00,(byte)0x02,(byte)0x03,(byte)0x0F,(byte)0x02,(byte)0x16,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x0F,(byte)0x01,(byte)0x02,(byte)0x03,(byte)0x0F,(byte)0x03,(byte)0x16,(byte)0x01,(byte)0x00,(byte)0x02,(byte)0x03,(byte)0x0F,(byte)0x04,(byte)0x16,(byte)0x01,(byte)0x00,(byte)0x01,(byte)0x01,(byte)0x02,(byte)0x02,(byte)0x0F,(byte)0x01,(byte)0x03,(byte)0x01,(byte)0x02,(byte)0x04,(byte)0x12,(byte)0x00,(byte)0x03,(byte)0x11,(byte)0x00,(byte)0x09,(byte)0x06,(byte)0x01,(byte)0x01,(byte)0x97,(byte)0x05,(byte)0x00,(byte)0xFF,(byte)0x02,(byte)0x02,(byte)0x01,(byte)0x03,(byte)0x02,(byte)0x03,(byte)0x0F,(byte)0x01,(byte)0x16,(byte)0x01,(byte)0x00,(byte)0x02,(byte)0x03,(byte)0x0F,(byte)0x02,(byte)0x16,(byte)0x01,(byte)0x00,(byte)0x02,(byte)0x03,(byte)0x0F,(byte)0x03,(byte)0x16,(byte)0x01,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x02,(byte)0x04,(byte)0x12,(byte)0x00,(byte)0x03,(byte)0x11,(byte)0x00,(byte)0x09,(byte)0x06,(byte)0x01,(byte)0x01,(byte)0x97,(byte)0x08,(byte)0x00,(byte)0xFF,(byte)0x02,(byte)0x02,(byte)0x01,(byte)0x03,(byte)0x02,(byte)0x03,(byte)0x0F,(byte)0x01,(byte)0x16,(byte)0x01,(byte)0x00,(byte)0x02,(byte)0x03,(byte)0x0F,(byte)0x02,(byte)0x16,(byte)0x01,(byte)0x00,(byte)0x02,(byte)0x03,(byte)0x0F,(byte)0x03,(byte)0x16,(byte)0x01,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x02,(byte)0x04,(byte)0x12,(byte)0x00,(byte)0x03,(byte)0x11,(byte)0x00,(byte)0x09,(byte)0x06,(byte)0x01,(byte)0x01,(byte)0xA1,(byte)0x01,(byte)0x00,(byte)0xFF,(byte)0x02,(byte)0x02,(byte)0x01,(byte)0x03,(byte)0x02,(byte)0x03,(byte)0x0F,(byte)0x01,(byte)0x16,(byte)0x01,(byte)0x00,(byte)0x02,(byte)0x03,(byte)0x0F,(byte)0x02,(byte)0x16,(byte)0x01,(byte)0x00,(byte)0x02,(byte)0x03,(byte)0x0F,(byte)0x03,(byte)0x16,(byte)0x01,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x02,(byte)0x04,(byte)0x12,(byte)0x00,(byte)0x03,(byte)0x11,(byte)0x00,(byte)0x09,(byte)0x06,(byte)0x01,(byte)0x01,(byte)0xA1,(byte)0x02,(byte)0x00,(byte)0xFF,(byte)0x02,(byte)0x02,(byte)0x01,(byte)0x03,(byte)0x02,(byte)0x03,(byte)0x0F,(byte)0x01,(byte)0x16,(byte)0x01,(byte)0x00,(byte)0x02,(byte)0x03,(byte)0x0F,(byte)0x02,(byte)0x16,(byte)0x01,(byte)0x00,(byte)0x02,(byte)0x03,(byte)0x0F,(byte)0x03,(byte)0x16,(byte)0x01,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x02,(byte)0x04,(byte)0x12,(byte)0x00,(byte)0x03,(byte)0x11,(byte)0x00,(byte)0x09,(byte)0x06,(byte)0x01,(byte)0x01,(byte)0xA1,(byte)0x03,(byte)0x00,(byte)0xFF,(byte)0x02,(byte)0x02,(byte)0x01,(byte)0x03,(byte)0x02,(byte)0x03,(byte)0x0F,(byte)0x01,(byte)0x16,(byte)0x01,(byte)0x00,(byte)0x02,(byte)0x03,(byte)0x0F,(byte)0x02,(byte)0x16,(byte)0x01,(byte)0x00,(byte)0x02,(byte)0x03,(byte)0x0F,(byte)0x03,(byte)0x16,(byte)0x01,(byte)0x00,(byte)0x01,(byte)0x00};
		
		
		data = ProtocolUtils.getSubArray(data, 15);
		try {
			
			AbstractDataType dataType = AXDRDecoder.decode(data);
			System.out.println(dataType);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	
	
}