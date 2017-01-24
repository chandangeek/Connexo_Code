package com.energyict.protocolimpl.eig.nexus1272.parse;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.cbo.Unit;
import com.energyict.protocolimpl.eig.nexus1272.NexusProtocolConnection;
import com.energyict.protocolimpl.eig.nexus1272.command.Command;
import com.energyict.protocolimpl.eig.nexus1272.command.NexusCommandFactory;
import com.energyict.protocolimpl.eig.nexus1272.command.ReadCommand;

import java.io.IOException;
import java.io.OutputStream;

public class ScaledEnergySetting {


	private int numDecimalPlaces;
	private Unit unit;
	private int numDigits;
	private int line;
	private int point;
	private OutputStream outputStream;
	private NexusProtocolConnection connection;

	public ScaledEnergySetting(LinePoint lp, OutputStream os ,NexusProtocolConnection npc) throws IOException {
		outputStream = os;
		connection = npc;
		this.line = lp.getLine();
		this.point = lp.getPoint();
		load();
	}

	private void load() throws IOException {
		Command getSES;
		byte[] resp;
		int unitCode;

		switch (line) {
		case 563:
			switch(point) {
			default:
				getSES = NexusCommandFactory.getFactory().getReadSingleRegisterCommand();
				((ReadCommand)getSES).setStartAddress(new byte[] {(byte) 0xCA, (byte) 0x01});//Q34 VARh/ Q14 Wh
				outputStream.write(getSES.build());
				resp = connection.receiveWriteResponse(getSES).toByteArray();

				numDecimalPlaces = ProtocolUtils.byte2int((byte) (resp[1]&0x07));
				unitCode = ProtocolUtils.byte2int((byte) ((byte) (resp[1]>>3&0x03)));
				switch (unitCode) {
				case 0:
					unit = Unit.get("Wh");
					break;
				case 1:
					unit = Unit.get("kWh");
					break;
				case 2:
				case 3:
					unit = Unit.get("MWh");
					break;
				default:
					unit = Unit.getUndefined();
				}
				numDigits = ProtocolUtils.byte2int((byte) ((byte) (resp[1]>>5&0x07)));
				break;
			}
			break;
		case 565:
			switch(point) {
			default:
				getSES = NexusCommandFactory.getFactory().getReadSingleRegisterCommand();
				((ReadCommand)getSES).setStartAddress(new byte[] {(byte) 0xCA, (byte) 0x04});//Q23 Wh/ Q2 VAh
				outputStream.write(getSES.build());
				resp = connection.receiveWriteResponse(getSES).toByteArray();

				numDecimalPlaces = ProtocolUtils.byte2int((byte) (resp[0]&0x07));
				unitCode = ProtocolUtils.byte2int((byte) ((byte) (resp[0]>>3&0x03)));
				switch (unitCode) {
				case 0:
					unit = Unit.get("Wh");
					break;
				case 1:
					unit = Unit.get("kWh");
					break;
				case 2:
				case 3:
					unit = Unit.get("MVh");
					break;
				default:
					unit = Unit.getUndefined();
				}
				numDigits = ProtocolUtils.byte2int((byte) ((byte) (resp[0]>>5&0x07)));
				break;
			}
			break;
		case 567:
			switch(point) {
			default:
				 getSES = NexusCommandFactory.getFactory().getReadSingleRegisterCommand();
				((ReadCommand)getSES).setStartAddress(new byte[] {(byte) 0xCA, 0x00});//Q1234 VAh/ Q12 VARh
				outputStream.write(getSES.build());
				resp = connection.receiveWriteResponse(getSES).toByteArray();
				numDecimalPlaces = ProtocolUtils.byte2int((byte) (resp[1]&0x07));
				unitCode = ProtocolUtils.byte2int((byte) ((byte) (resp[1]>>3&0x03)));
				switch (unitCode) {
				case 0:
					unit = Unit.get("varh");
					break;
				case 1:
					unit = Unit.get("kvarh");
					break;
				case 2:
				case 3:
					unit = Unit.get("Mvarh");
					break;
				default:
					unit = Unit.getUndefined();
				}
				numDigits = ProtocolUtils.byte2int((byte) ((byte) (resp[1]>>5&0x07)));
				break;
			}
			break;
		case 569:
			switch(point) {
			default:
				getSES = NexusCommandFactory.getFactory().getReadSingleRegisterCommand();
				((ReadCommand)getSES).setStartAddress(new byte[] {(byte) 0xCA, (byte) 0x01});//Q34 VARh/ Q14 Wh
				outputStream.write(getSES.build());
				resp = connection.receiveWriteResponse(getSES).toByteArray();

				numDecimalPlaces = ProtocolUtils.byte2int((byte) (resp[0]&0x07));
				unitCode = ProtocolUtils.byte2int((byte) ((byte) (resp[0]>>3&0x03)));
				switch (unitCode) {
				case 0:
					unit = Unit.get("varh");
					break;
				case 1:
					unit = Unit.get("kvarh");
					break;
				case 2:
				case 3:
					unit = Unit.get("Mvarh");
					break;
				default:
					unit = Unit.getUndefined();
				}
				numDigits = ProtocolUtils.byte2int((byte) ((byte) (resp[0]>>5&0x07)));
				break;
			}
			break;
		case 583:
			switch(point) {
			case 1:
				//Positive varh (Q12)
				 getSES = NexusCommandFactory.getFactory().getReadSingleRegisterCommand();
				((ReadCommand)getSES).setStartAddress(new byte[] {(byte) 0xCA, 0x00});//Q1234 VAh/ Q12 VARh
				outputStream.write(getSES.build());
				resp = connection.receiveWriteResponse(getSES).toByteArray();

//				System.out.println("1 ***************** " + ProtocolUtils.byte2int(resp[0]));
//				System.out.println("1 ***************** " + ProtocolUtils.byte2int(resp[1]));
//				System.out.println(ParseUtils.buildBinaryRepresentation(ProtocolUtils.byte2int(resp[0]), 8));
//				System.out.println(ParseUtils.buildBinaryRepresentation(ProtocolUtils.byte2int((byte) (resp[0]&0x07)), 8));
//				System.out.println(ParseUtils.buildBinaryRepresentation(ProtocolUtils.byte2int((byte) ((byte) (resp[0]&0x18)>>3)), 8));
//				System.out.println(ParseUtils.buildBinaryRepresentation(ProtocolUtils.byte2int((byte) ((byte) (resp[0]>>5&0x07))), 3));
//				System.out.println(ProtocolUtils.byte2int((byte) (resp[0]&0x07)));
//				System.out.println(ProtocolUtils.byte2int((byte) ((byte) (resp[0]>>3&0x03))));
//				System.out.println(ProtocolUtils.byte2int((byte) ((byte) (resp[0]>>5&0x07))));

				numDecimalPlaces = ProtocolUtils.byte2int((byte) (resp[1]&0x07));
				unitCode = ProtocolUtils.byte2int((byte) ((byte) (resp[1]>>3&0x03)));
				switch (unitCode) {
				case 0:
					unit = Unit.get("varh");
					break;
				case 1:
					unit = Unit.get("kvarh");
					break;
				case 2:
				case 3:
					unit = Unit.get("Mvarh");
					break;
				default:
					unit = Unit.getUndefined();
				}
				numDigits = ProtocolUtils.byte2int((byte) ((byte) (resp[1]>>5&0x07)));
				break;
			case 2:
				//Negative varh (Q34)
				getSES = NexusCommandFactory.getFactory().getReadSingleRegisterCommand();
				((ReadCommand)getSES).setStartAddress(new byte[] {(byte) 0xCA, (byte) 0x01});//Q34 VARh/ Q14 Wh
				outputStream.write(getSES.build());
				resp = connection.receiveWriteResponse(getSES).toByteArray();

				numDecimalPlaces = ProtocolUtils.byte2int((byte) (resp[0]&0x07));
				unitCode = ProtocolUtils.byte2int((byte) ((byte) (resp[0]>>3&0x03)));
				switch (unitCode) {
				case 0:
					unit = Unit.get("varh");
					break;
				case 1:
					unit = Unit.get("kvarh");
					break;
				case 2:
				case 3:
					unit = Unit.get("Mvarh");
					break;
				default:
					unit = Unit.getUndefined();
				}
				numDigits = ProtocolUtils.byte2int((byte) ((byte) (resp[0]>>5&0x07)));
				break;

			default:
				throw new IOException("Could not load scaled energy settings for line " + line + " and point " + point);
			}
			break;
		case 584:
			switch(point) {
			case 0:
				//Positive Wh (Q14)
				getSES = NexusCommandFactory.getFactory().getReadSingleRegisterCommand();
				((ReadCommand)getSES).setStartAddress(new byte[] {(byte) 0xCA, (byte) 0x01});//Q34 VARh/ Q14 Wh
				outputStream.write(getSES.build());
				resp = connection.receiveWriteResponse(getSES).toByteArray();

				numDecimalPlaces = ProtocolUtils.byte2int((byte) (resp[1]&0x07));
				unitCode = ProtocolUtils.byte2int((byte) ((byte) (resp[1]>>3&0x03)));
				switch (unitCode) {
				case 0:
					unit = Unit.get("Wh");
					break;
				case 1:
					unit = Unit.get("kWh");
					break;
				case 2:
				case 3:
					unit = Unit.get("MWh");
					break;
				default:
					unit = Unit.getUndefined();
				}
				numDigits = ProtocolUtils.byte2int((byte) ((byte) (resp[1]>>5&0x07)));
				break;
			case 5:
				//Negative Wh (Q23)
				getSES = NexusCommandFactory.getFactory().getReadSingleRegisterCommand();
				((ReadCommand)getSES).setStartAddress(new byte[] {(byte) 0xCA, (byte) 0x04});//Q23 Wh/ Q2 VAh
				outputStream.write(getSES.build());
				resp = connection.receiveWriteResponse(getSES).toByteArray();

				numDecimalPlaces = ProtocolUtils.byte2int((byte) (resp[0]&0x07));
				unitCode = ProtocolUtils.byte2int((byte) ((byte) (resp[0]>>3&0x03)));
				switch (unitCode) {
				case 0:
					unit = Unit.get("Wh");
					break;
				case 1:
					unit = Unit.get("kWh");
					break;
				case 2:
				case 3:
					unit = Unit.get("MVh");
					break;
				default:
					unit = Unit.getUndefined();
				}
				numDigits = ProtocolUtils.byte2int((byte) ((byte) (resp[0]>>5&0x07)));
				break;
			default:
				throw new IOException("Could not load scaled energy settings for line " + line + " and point " + point);
			}
			break;
		case 589:
			switch(point) {
			case 0:
				//Pulse Accumulation, Input 1
				getSES = NexusCommandFactory.getFactory().getReadSingleRegisterCommand();
				((ReadCommand)getSES).setStartAddress(new byte[] {(byte) 0xCA, (byte) 0x14});//Pulse Accumulation, Input 1/ Pulse Accumulation Input 2
				outputStream.write(getSES.build());
				resp = connection.receiveWriteResponse(getSES).toByteArray();

				numDecimalPlaces = ProtocolUtils.byte2int((byte) (resp[0]&0x07));
				unitCode = ProtocolUtils.byte2int((byte) ((byte) (resp[0]>>3&0x03)));
				switch (unitCode) {
				default:
					unit = Unit.getUndefined();
				}
				numDigits = ProtocolUtils.byte2int((byte) ((byte) (resp[0]>>5&0x07)));
				break;
			case 1:
				//Pulse Accumulation, Input 2
				getSES = NexusCommandFactory.getFactory().getReadSingleRegisterCommand();
				((ReadCommand)getSES).setStartAddress(new byte[] {(byte) 0xCA, (byte) 0x14});//Pulse Accumulation, Input 1/ Pulse Accumulation Input 2
				outputStream.write(getSES.build());
				resp = connection.receiveWriteResponse(getSES).toByteArray();

				numDecimalPlaces = ProtocolUtils.byte2int((byte) (resp[1]&0x07));
				unitCode = ProtocolUtils.byte2int((byte) ((byte) (resp[1]>>3&0x03)));
				switch (unitCode) {
				default:
					unit = Unit.getUndefined();
				}
				numDigits = ProtocolUtils.byte2int((byte) ((byte) (resp[1]>>5&0x07)));
				break;
			case 2:
				//Pulse Accumulation, Input 3
				getSES = NexusCommandFactory.getFactory().getReadSingleRegisterCommand();
				((ReadCommand)getSES).setStartAddress(new byte[] {(byte) 0xCA, (byte) 0x15});//Pulse Accumulation, Input 3/ Pulse Accumulation Input 4
				outputStream.write(getSES.build());
				resp = connection.receiveWriteResponse(getSES).toByteArray();

				numDecimalPlaces = ProtocolUtils.byte2int((byte) (resp[0]&0x07));
				unitCode = ProtocolUtils.byte2int((byte) ((byte) (resp[0]>>3&0x03)));
				switch (unitCode) {
				default:
					unit = Unit.getUndefined();
				}
				numDigits = ProtocolUtils.byte2int((byte) ((byte) (resp[0]>>5&0x07)));
				break;
			case 3:
				//Pulse Accumulation, Input 4
				getSES = NexusCommandFactory.getFactory().getReadSingleRegisterCommand();
				((ReadCommand)getSES).setStartAddress(new byte[] {(byte) 0xCA, (byte) 0x15});//Pulse Accumulation, Input 3/ Pulse Accumulation Input 4
				outputStream.write(getSES.build());
				resp = connection.receiveWriteResponse(getSES).toByteArray();

				numDecimalPlaces = ProtocolUtils.byte2int((byte) (resp[1]&0x07));
				unitCode = ProtocolUtils.byte2int((byte) ((byte) (resp[1]>>3&0x03)));
				switch (unitCode) {
				default:
					unit = Unit.getUndefined();
				}
				numDigits = ProtocolUtils.byte2int((byte) ((byte) (resp[1]>>5&0x07)));
				break;
			case 4:
				//Pulse Accumulation, Input 5
				getSES = NexusCommandFactory.getFactory().getReadSingleRegisterCommand();
				((ReadCommand)getSES).setStartAddress(new byte[] {(byte) 0xCA, (byte) 0x16});//Pulse Accumulation, Input 5/ Pulse Accumulation Input 6
				outputStream.write(getSES.build());
				resp = connection.receiveWriteResponse(getSES).toByteArray();

				numDecimalPlaces = ProtocolUtils.byte2int((byte) (resp[0]&0x07));
				unitCode = ProtocolUtils.byte2int((byte) ((byte) (resp[0]>>3&0x03)));
				switch (unitCode) {
				default:
					unit = Unit.getUndefined();
				}
				numDigits = ProtocolUtils.byte2int((byte) ((byte) (resp[0]>>5&0x07)));
				break;
			case 5:
				//Pulse Accumulation, Input 6
				getSES = NexusCommandFactory.getFactory().getReadSingleRegisterCommand();
				((ReadCommand)getSES).setStartAddress(new byte[] {(byte) 0xCA, (byte) 0x16});//Pulse Accumulation, Input 5/ Pulse Accumulation Input 6
				outputStream.write(getSES.build());
				resp = connection.receiveWriteResponse(getSES).toByteArray();

				numDecimalPlaces = ProtocolUtils.byte2int((byte) (resp[1]&0x07));
				unitCode = ProtocolUtils.byte2int((byte) ((byte) (resp[1]>>3&0x03)));
				switch (unitCode) {
				default:
					unit = Unit.getUndefined();
				}
				numDigits = ProtocolUtils.byte2int((byte) ((byte) (resp[1]>>5&0x07)));
				break;
			case 6:
				//Pulse Accumulation, Input 7
				getSES = NexusCommandFactory.getFactory().getReadSingleRegisterCommand();
				((ReadCommand)getSES).setStartAddress(new byte[] {(byte) 0xCA, (byte) 0x17});//Pulse Accumulation, Input 7/ Pulse Accumulation Input 8
				outputStream.write(getSES.build());
				resp = connection.receiveWriteResponse(getSES).toByteArray();

				numDecimalPlaces = ProtocolUtils.byte2int((byte) (resp[0]&0x07));
				unitCode = ProtocolUtils.byte2int((byte) ((byte) (resp[0]>>3&0x03)));
				switch (unitCode) {
				default:
					unit = Unit.getUndefined();
				}
				numDigits = ProtocolUtils.byte2int((byte) ((byte) (resp[0]>>5&0x07)));
				break;
			case 7:
				//Pulse Accumulation, Input 8
				getSES = NexusCommandFactory.getFactory().getReadSingleRegisterCommand();
				((ReadCommand)getSES).setStartAddress(new byte[] {(byte) 0xCA, (byte) 0x17});//Pulse Accumulation, Input 7/ Pulse Accumulation Input 7
				outputStream.write(getSES.build());
				resp = connection.receiveWriteResponse(getSES).toByteArray();

				numDecimalPlaces = ProtocolUtils.byte2int((byte) (resp[1]&0x07));
				unitCode = ProtocolUtils.byte2int((byte) ((byte) (resp[1]>>3&0x03)));
				switch (unitCode) {
				default:
					unit = Unit.getUndefined();
				}
				numDigits = ProtocolUtils.byte2int((byte) ((byte) (resp[1]>>5&0x07)));
				break;
			default:
				throw new IOException("Could not load scaled energy settings for line " + line + " and point " + point);
			}
			break;
		default:
			throw new IOException("Could not load scaled energy settings for line " + line + " and point " + point);
		}

	}

	public int getNumDecimalPlaces() {
		return numDecimalPlaces;
	}
	public Unit getUnit() {
		return unit;
	}
	public int getNumDigits() {
		return numDigits;
	}

	public String toString() {
		return "Line " + line + " point " + point + ":\t numDecimals " + numDecimalPlaces + " unit " + unit + " numDigits " + numDigits;
	}
}
