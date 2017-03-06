package com.energyict.protocolimpl.edmi.common.command;

import com.energyict.protocol.ProtocolException;
import com.energyict.protocol.ProtocolUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jme
 *
 */
public class RegisterInfo {

	private List<InfoItem> ir = new ArrayList<>();

	/*
	 * Constructors
	 */

	public RegisterInfo() {
		// System information
		add(0x0F000, "A");
		add(0x0F001, "A");
		add(0x0F002, "A");
		add(0x0F003, "A");
		add(0x0F006, "C");
		add(0x0F00D, "A");
		add(0x0F00F, "A");
		add(0x0F060, "A");
		add(0x0F080, "L");
		add(0x0F081, "A");
		add(0x0F082, "A");
		add(0x0F083, "A");
		add(0x0F084, "A");
		add(0x0F085, "A");
		add(0x0F086, "A");
		add(0x0F087, "A");
		add(0x0F090, "L");
		add(0x0F091, "I");


		// Meter Power status
		add(0x0F03D, "T", "T");
		add(0x0F061, "T", "T");

		addRange(0x0D800, 0x0D801, "L", "T");

		addRange(0x0D802, 0x0D805, "L");

		addRange(0x0D840, 0x0D85F, "I");
		addRange(0x0D860, 0x0D87F, "I");
		addRange(0x0D880, 0x0D89F, "I");
	}

	/*
	 * Private inner classes
	 */

	private class InfoItem {
		private int registerID				= 0;
		private String registerType			= "";
		private String registerUnit 		= "";
		private String registerDescription 	= "";

		public InfoItem(int registerID, String registerType, String registerUnit, String registerDescription) {
			this.registerID = registerID;
			this.registerType = registerType;
			this.registerUnit = registerUnit;
			this.registerDescription = registerDescription;
		}

		public int getRegisterID() {return registerID;}
		public String getRegisterType() {return registerType;}
		public String getRegisterUnit() {return registerUnit;}
		public String getRegisterDescription() {return registerDescription;}

	}

	/*
	 * Private getters, setters and methods
	 */

	private void add(int regId, String regType, String regUnit, String regDesc) {
		ir.add(new InfoItem(regId, regType, regUnit, regDesc));
	}

	private void add(int regId, String regType, String regUnit) {
		this.add(regId, regType, regUnit, "");
	}

	private void add(int regId, String regType) {
		this.add(regId, regType, "N", "");
	}

	private void addRange(int regIdFrom, int regIdTo, String regType) {
		for (int regId = regIdFrom; regId <= regIdTo; regId++) {
			this.add(regId, regType, "N", "");
		}
	}

	private void addRange(int regIdFrom, int regIdTo, String regType, String regUnit) {
		for (int regId = regIdFrom; regId <= regIdTo; regId++) {
			this.add(regId, regType, regUnit, "");
		}
	}

	public InfoItem getInfoItem(int registerId) throws ProtocolException {
		for (InfoItem infoItem : ir) {
			if (infoItem.registerID == registerId) {
				return infoItem;
			}
		}
		throw new ProtocolException("Hard coded register info not found for register " +  ProtocolUtils.buildStringHex(registerId, 5));
	}

	/*
	 * Public methods
	 */

	public boolean isInfoHardCoded(int registerId) {
		for (InfoItem infoItem : ir) {
			if (infoItem.registerID == registerId) {
				return true;
			}
		}
		return false;
	}

	public byte[] getInfoResponse(int registerId) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		InfoItem infoItem = getInfoItem(registerId);

		buffer.write(0x0049);
		buffer.write((registerId & 0x0000FF00) >> 8);
		buffer.write((registerId & 0x000000FF));
		buffer.write(infoItem.getRegisterType().getBytes());
		buffer.write(infoItem.getRegisterUnit().getBytes());
		buffer.write(infoItem.getRegisterDescription().getBytes());
		buffer.write(0x00);
		return buffer.toByteArray();
	}
}