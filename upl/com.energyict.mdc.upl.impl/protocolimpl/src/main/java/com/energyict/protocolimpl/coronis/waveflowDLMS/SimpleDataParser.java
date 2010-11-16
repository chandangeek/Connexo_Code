package com.energyict.protocolimpl.coronis.waveflowDLMS;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Logger;

import com.energyict.cbo.*;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;

public class SimpleDataParser {

	private Logger logger;

	byte[] genericHeader=null;

	// collected registervalues
	Map<ObisCode,RegisterValue> registerValues = new HashMap<ObisCode,RegisterValue>(); 
		
	final Map<ObisCode, RegisterValue> getRegisterValues() {
		return registerValues;
	}


	private final int CLASS_DATA=1;
	private final int CLASS_REGISTER=3;
	
	private final int ATTRIBUTE_VALUE=2;
	private final int ATTRIBUTE_SCALER=3;
	
	/*
	 *      0001 0101600100FF 02 
	 *      0003 0101010800FF 02
	 *      0003 0101010800FF 03
	 *      0003 0101020800FF 02
	 *      0003 0101020800FF 03
	 *      1 = Data
	 *      3 = register
	 */
	
	private void parseSubData(ObisCodeAndInfo obisCodeAndInfos, byte[] data) throws IOException {
		DataInputStream dais = null;
		try {
			byte[] temp;
			dais = new DataInputStream(new ByteArrayInputStream(data));
			// 7EA0162320AFB8C444E6E700C4018100 060030EBB7 D0957E
			temp=new byte[16];
			dais.read(temp);
			if (WaveflowProtocolUtils.toInt(temp[12]) != 0xC4) {
				throw new IOException("Error in dlms subframe. expected [GetResponse 0xC4], received ["+WaveflowProtocolUtils.toHexString(temp[12])+"]");
			}
			int statusCode=WaveflowProtocolUtils.toInt(temp[15]);
			if (statusCode != 0) {
				return;
			}
			temp=new byte[dais.available()];
			dais.read(temp);
			AbstractDataType adt = AXDRDecoder.decode(temp);
			
			if (obisCodeAndInfos.getInterfaceClass() == CLASS_DATA) {
				registerValues.remove(obisCodeAndInfos.getObisCode());
				if (adt.isOctetString()) {
					registerValues.put(obisCodeAndInfos.getObisCode(), new RegisterValue(obisCodeAndInfos.getObisCode(), adt.getOctetString().stringValue()));
				}
				else if (adt.isVisibleString()) {
					registerValues.put(obisCodeAndInfos.getObisCode(), new RegisterValue(obisCodeAndInfos.getObisCode(), adt.getOctetString().stringValue()));
				}
				else {
					registerValues.put(obisCodeAndInfos.getObisCode(), new RegisterValue(obisCodeAndInfos.getObisCode(), new Quantity(adt.toBigDecimal(),Unit.get(""))));
				}
			}
			else if (obisCodeAndInfos.getInterfaceClass() == CLASS_REGISTER) {
				RegisterValue registerValue = registerValues.get(obisCodeAndInfos.getObisCode());
				
				BigDecimal value = BigDecimal.valueOf(0); 
				Unit unit = Unit.get("");
				
				if (registerValue != null) {
					value = registerValue.getQuantity().getAmount();
					unit = registerValue.getQuantity().getUnit();
				}
				
				if (obisCodeAndInfos.getAttribute() == ATTRIBUTE_SCALER) {
					int scale = adt.getStructure().getDataType(0).intValue();
					int code = adt.getStructure().getDataType(1).intValue();
					unit = Unit.get(code, scale);
				}
				else if (obisCodeAndInfos.getAttribute() == ATTRIBUTE_VALUE) {
					value = adt.toBigDecimal();
				}
				
				Quantity quantity = new Quantity(value,unit);
				
				if (registerValue != null) {
					registerValue.setQuantity(quantity);
				}
				else {
					registerValues.put(obisCodeAndInfos.getObisCode(), new RegisterValue(obisCodeAndInfos.getObisCode(), quantity));
				}
			}
		}
		finally {
			if (dais != null) {
				try {
					dais.close();
				}
				catch(IOException e) {
					logger.severe(com.energyict.cbo.Utils.stack2string(e));
				}
			}
		}				
	}
	
	public SimpleDataParser(Logger logger) {
		super();
		this.logger = logger;
	}
	
	class ObisCodeAndInfo {
		
		private ObisCode obisCode;
		private int interfaceClass;
		private int attribute;
		
		private ObisCodeAndInfo(ObisCode obisCode, int interfaceClass, int attribute) {
			super();
			this.obisCode = obisCode;
			this.interfaceClass = interfaceClass;
			this.attribute = attribute;
		}
		
		final ObisCode getObisCode() {
			return obisCode;
		}

		final int getInterfaceClass() {
			return interfaceClass;
		}

		final int getAttribute() {
			return attribute;
		}
	}
	
	
	private final ObisCodeAndInfo[] parseRequest(byte[] request) throws IOException {
		DataInputStream dais = null;
		
		try {
		
			dais = new DataInputStream(new ByteArrayInputStream(request));
			dais.readByte(); // skip command byte
			int nrOfEntries = WaveflowProtocolUtils.toInt(dais.readByte());
			ObisCodeAndInfo[] obisCodeAndInfos = new ObisCodeAndInfo[nrOfEntries];
			for (int i=0;i<nrOfEntries;i++) {
				int interfaceclass = dais.readShort();
				int a = WaveflowProtocolUtils.toInt(dais.readByte());
				int b = WaveflowProtocolUtils.toInt(dais.readByte());
				int c = WaveflowProtocolUtils.toInt(dais.readByte());
				int d = WaveflowProtocolUtils.toInt(dais.readByte());
				int e = WaveflowProtocolUtils.toInt(dais.readByte());
				int f = WaveflowProtocolUtils.toInt(dais.readByte());
				ObisCode obisCode = new ObisCode(a,b,c,d,e,f);
				int arrtibute = WaveflowProtocolUtils.toInt(dais.readByte());
				obisCodeAndInfos[i] = new ObisCodeAndInfo(obisCode,interfaceclass,arrtibute);
			}
			return obisCodeAndInfos;
		}
		finally {
			if (dais != null) {
				try {
					dais.close();
				}
				catch(IOException e) {
					logger.severe(com.energyict.cbo.Utils.stack2string(e));
				}
			}
		}			
		
	}
	
	public final void parse(byte[] request, byte[] response) throws IOException {
		DataInputStream dais = null;
		
		
		try {
		
			ObisCodeAndInfo[] obisCodeAndInfos = parseRequest(request);
			
			if (WaveflowProtocolUtils.toInt(response[0]) != 0xB2) {
				throw new IOException("Error in frame. Invalid command response code expected [0xB2], received ["+WaveflowProtocolUtils.toHexString(response[0])+"]");
			}
			
			if (WaveflowProtocolUtils.toInt(response[1]) == 0xff) {
				throw new WaveflowDLMSStatusError("Error in frame. Status error");
			}
			dais = new DataInputStream(new ByteArrayInputStream(response));
			dais.readByte(); // skip command response byte
			genericHeader = new byte[13];
			dais.read(genericHeader);
			
			
			
			int nrOfEntries = WaveflowProtocolUtils.toInt(dais.readByte());
			if (obisCodeAndInfos.length != nrOfEntries) {
				throw new IOException("Error in frame. Invalid nr of entries received expected ["+obisCodeAndInfos.length+"], received ["+nrOfEntries+"]");
			}
			
			for (int i=0;i<nrOfEntries;i++) {
				
				int subDataLength=dais.readByte();
				if (subDataLength != 0) {
					byte[] subData = new byte[subDataLength];
					dais.read(subData);
					parseSubData(obisCodeAndInfos[i],subData);
				}
			}
		}
		finally {
			if (dais != null) {
				try {
					dais.close();
				}
				catch(IOException e) {
					logger.severe(com.energyict.cbo.Utils.stack2string(e));
				}
			}
		}			
	}	
	
	public String toString() {
		
		StringBuilder strBuilder = new StringBuilder(); 
		
		for (RegisterValue rv : registerValues.values()) {
			
			strBuilder.append(rv+"\n");
		}
		
		return strBuilder.toString();
		
	}
	
	public int getQos() {
		
		if (genericHeader != null) {
			return WaveflowProtocolUtils.toInt(genericHeader[12]);
		}
		
		return 0;
	}
	
	public static void main(String[] args) {
//	    byte[] data = new byte[]{(byte)0xB2,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x04,(byte)0x0D,(byte)0x38,(byte)0x20,(byte)0x05,(byte)0x18,(byte)0x7E,(byte)0xA0,(byte)0x16,(byte)0x23,(byte)0x20,(byte)0xAF,(byte)0xDA,(byte)0xD0,(byte)0x04,(byte)0xE6,(byte)0xE7,(byte)0x00,(byte)0xC4,(byte)0x01,(byte)0x81,(byte)0x00,(byte)0x06,(byte)0x00,(byte)0x30,(byte)0xEB,(byte)0xB7,(byte)0xD0,(byte)0x95,(byte)0x7E,(byte)0x15,(byte)0x7E,(byte)0xA0,(byte)0x13,(byte)0x23,(byte)0x20,(byte)0xAF,(byte)0xFC,(byte)0xB0,(byte)0x66,(byte)0xE6,(byte)0xE7,(byte)0x00,(byte)0xC4,(byte)0x01,(byte)0x81,(byte)0x00,(byte)0x11,(byte)0x00,(byte)0xAC,(byte)0x5B,(byte)0x7E,(byte)0x19,(byte)0x7E,(byte)0xA0,(byte)0x17,(byte)0x23,(byte)0x20,(byte)0xAF,(byte)0x1E,(byte)0xBC,(byte)0x8F,(byte)0xE6,(byte)0xE7,(byte)0x00,(byte)0xC4,(byte)0x01,(byte)0x81,(byte)0x00,(byte)0x02,(byte)0x02,(byte)0x0F,(byte)0x03,(byte)0x16,(byte)0x1E,(byte)0xF4,(byte)0x5E,(byte)0x7E,(byte)0x14,(byte)0x7E,(byte)0xA0,(byte)0x12,(byte)0x23,(byte)0x20,(byte)0xAF,(byte)0x30,(byte)0x94,(byte)0x61,(byte)0xE6,(byte)0xE7,(byte)0x00,(byte)0xC4,(byte)0x01,(byte)0x81,(byte)0x01,(byte)0x0B,(byte)0x4D,(byte)0x08,(byte)0x7E,(byte)0x14,(byte)0x7E,(byte)0xA0,(byte)0x12,(byte)0x23,(byte)0x20,(byte)0xAF,(byte)0x52,(byte)0x80,(byte)0x21,(byte)0xE6,(byte)0xE7,(byte)0x00,(byte)0xC4,(byte)0x01,(byte)0x81,(byte)0x01,(byte)0x0B,(byte)0x4D,(byte)0x08,(byte)0x7E};
	    
		
		SimpleDataParser o = new SimpleDataParser(null);
		
		
		byte[] request =  new byte[]{(byte)0x32,(byte)0x05,(byte)0x00,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x60,(byte)0x01,(byte)0x00,(byte)0xFF,(byte)0x02,(byte)0x00,(byte)0x03,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x08,(byte)0x00,(byte)0xFF,(byte)0x02,(byte)0x00,(byte)0x03,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x08,(byte)0x00,(byte)0xFF,(byte)0x03,(byte)0x00,(byte)0x03,(byte)0x01,(byte)0x01,(byte)0x02,(byte)0x08,(byte)0x00,(byte)0xFF,(byte)0x02,(byte)0x00,(byte)0x03,(byte)0x01,(byte)0x01,(byte)0x02,(byte)0x08,(byte)0x00,(byte)0xFF,(byte)0x03};
		
	    byte[] response = new byte[]{(byte)0xB2,(byte)0x01,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x01,(byte)0x05,(byte)0x17,(byte)0x3A,(byte)0x20,(byte)0x05,(byte)0x18,(byte)0x7E,(byte)0xA0,(byte)0x16,(byte)0x23,(byte)0x20,(byte)0xAF,(byte)0xB8,(byte)0xC4,(byte)0x44,(byte)0xE6,(byte)0xE7,(byte)0x00,(byte)0xC4,(byte)0x01,(byte)0x81,(byte)0x00,(byte)0x06,(byte)0x00,(byte)0x30,(byte)0xEB,(byte)0xB7,(byte)0xD0,(byte)0x95,(byte)0x7E,(byte)0x15,(byte)0x7E,(byte)0xA0,(byte)0x13,(byte)0x23,(byte)0x20,(byte)0xAF,(byte)0xDA,(byte)0x84,(byte)0x22,(byte)0xE6,(byte)0xE7,(byte)0x00,(byte)0xC4,(byte)0x01,(byte)0x81,(byte)0x00,(byte)0x11,(byte)0x00,(byte)0xAC,(byte)0x5B,(byte)0x7E,(byte)0x19,(byte)0x7E,(byte)0xA0,(byte)0x17,(byte)0x23,(byte)0x20,(byte)0xAF,(byte)0xFC,(byte)0xA0,(byte)0x4B,(byte)0xE6,(byte)0xE7,(byte)0x00,(byte)0xC4,(byte)0x01,(byte)0x81,(byte)0x00,(byte)0x02,(byte)0x02,(byte)0x0F,(byte)0x03,(byte)0x16,(byte)0x1E,(byte)0xF4,(byte)0x5E,(byte)0x7E,(byte)0x15,(byte)0x7E,(byte)0xA0,(byte)0x13,(byte)0x23,(byte)0x20,(byte)0xAF,(byte)0x1E,(byte)0xAC,(byte)0xA2,(byte)0xE6,(byte)0xE7,(byte)0x00,(byte)0xC4,(byte)0x01,(byte)0x81,(byte)0x00,(byte)0x11,(byte)0x00,(byte)0xAC,(byte)0x5B,(byte)0x7E,(byte)0x19,(byte)0x7E,(byte)0xA0,(byte)0x17,(byte)0x23,(byte)0x20,(byte)0xAF,(byte)0x30,(byte)0xC0,(byte)0x47,(byte)0xE6,(byte)0xE7,(byte)0x00,(byte)0xC4,(byte)0x01,(byte)0x81,(byte)0x00,(byte)0x02,(byte)0x02,(byte)0x0F,(byte)0x03,(byte)0x16,(byte)0x1E,(byte)0xF4,(byte)0x5E,(byte)0x7E};
	    
	    
	    try {
			o.parse(request,response);
			System.out.println(o);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}
