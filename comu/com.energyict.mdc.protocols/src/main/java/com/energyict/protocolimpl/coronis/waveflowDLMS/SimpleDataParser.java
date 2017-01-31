/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflowDLMS;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

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
					logger.severe(ProtocolUtils.stack2string(e));
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
					logger.severe(ProtocolUtils.stack2string(e));
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
					logger.severe(ProtocolUtils.stack2string(e));
				}
			}
		}
	}

	public String toString() {
		StringBuilder strBuilder = new StringBuilder();
		for (RegisterValue rv : registerValues.values()) {
			strBuilder.append(rv).append("\n");
		}
		return strBuilder.toString();

	}

	public int getQos() {

		if (genericHeader != null) {
			return WaveflowProtocolUtils.toInt(genericHeader[12]);
		}

		return 0;
	}

}
