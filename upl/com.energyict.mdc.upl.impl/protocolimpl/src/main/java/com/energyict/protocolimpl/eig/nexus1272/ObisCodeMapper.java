package com.energyict.protocolimpl.eig.nexus1272;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.eig.nexus1272.command.AbstractCommand;
import com.energyict.protocolimpl.eig.nexus1272.command.NexusCommandFactory;
import com.energyict.protocolimpl.eig.nexus1272.command.ReadCommand;
import com.energyict.protocolimpl.eig.nexus1272.parse.LinePoint;
import com.energyict.protocolimpl.eig.nexus1272.parse.NexusDataParser;
import com.energyict.protocolimpl.eig.nexus1272.parse.ScaledEnergySetting;
import com.energyict.protocolimpl.eig.nexus1272.parse.ScaledEnergySettingFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Date;

public class ObisCodeMapper {

	NexusCommandFactory nexusCommandFactory;
	NexusProtocolConnection connection;
	OutputStream outputStream;
	private ScaledEnergySettingFactory sesf;
	private BigDecimal multiplier = null;
	private BigDecimal PTmultiplier = null;
	private BigDecimal CTmultiplier;
	private boolean isDeltaWired;

	/** Creates a new instance of ObisCodeMapper 
	 * @param connection 
	 * @param outputStream 
	 * @param isDeltaWired */
	public ObisCodeMapper(NexusCommandFactory nexusCommandFactory, NexusProtocolConnection connection, OutputStream outputStream, ScaledEnergySettingFactory sesf, boolean isDeltaWired) {
		this.nexusCommandFactory=nexusCommandFactory;
		this.connection = connection;
		this.outputStream = outputStream;
		this.sesf = sesf;
		this.isDeltaWired=isDeltaWired;
	}

	static public RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
		ObisCodeMapper ocm = new ObisCodeMapper(null,null, null, null, false);
		return (RegisterInfo)ocm.doGetRegister(obisCode,false);
	}

	public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
		return (RegisterValue)doGetRegister(obisCode, true);
	}

	private Object doGetRegister(ObisCode obisCode, boolean read) throws IOException {

		RegisterValue registerValue=null;

		// ********************************************************************************* 
		// Manufacturer specific
		// Electricity related ObisRegisters
		if ((obisCode.getA() == 1) && (obisCode.getB() >= 1) && (obisCode.getB() <= 8)) {
			switch (obisCode.getC()) {
			case 1:
				switch (obisCode.getD()) {
				case 2:
					switch (obisCode.getE()) {
					case 1:
						if (read) {
							//1966H-1967H
							ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
							c.setStartAddress(AbstractCommand.intToByteArray(0x1966));
							c.setNumRegisters(AbstractCommand.intToByteArray(2));
							outputStream.write(c.build());
							NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
							BigDecimal bd = new BigDecimal(ndp.parseF18());
							bd = applyPTCTRatio(bd);
							Quantity q = new Quantity(bd, Unit.getUndefined()); 

							registerValue = new RegisterValue(obisCode,q,new Date());
							return registerValue;

						}
						else return new RegisterInfo(obisCode.getDescription());
					case 2:
						if (read) {
							//196AH-196BH
							ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
							c.setStartAddress(AbstractCommand.intToByteArray(0x196A));
							c.setNumRegisters(AbstractCommand.intToByteArray(2));
							outputStream.write(c.build());
							NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
							BigDecimal bd = new BigDecimal(ndp.parseF18());
							bd = applyPTCTRatio(bd);
							Quantity q = new Quantity(bd, Unit.getUndefined()); 

							registerValue = new RegisterValue(obisCode,q,new Date());
							return registerValue;

						}
						else return new RegisterInfo(obisCode.getDescription());
					case 3: 
						if (read) {
							//196EH-196FH
							ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
							c.setStartAddress(AbstractCommand.intToByteArray(0x196E));
							c.setNumRegisters(AbstractCommand.intToByteArray(2));
							outputStream.write(c.build());
							NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
							BigDecimal bd = new BigDecimal(ndp.parseF18());
							bd = applyPTCTRatio(bd);
							Quantity q = new Quantity(bd, Unit.getUndefined()); 

							registerValue = new RegisterValue(obisCode,q,new Date());
							return registerValue;

						}
						else return new RegisterInfo(obisCode.getDescription());
					default:
						throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
					}

				case 6:
					// kW maximum demand
					//FIXME Get event time of max demands
					switch (obisCode.getE()) {
					case 1:
						if (read) {
							//10D0H-10D1H
							ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
							c.setStartAddress(AbstractCommand.intToByteArray(0x10D0));
							c.setNumRegisters(AbstractCommand.intToByteArray(2));
							outputStream.write(c.build());
							NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
							BigDecimal bd = ndp.parseF7();
							bd = applyPTCTRatio(bd);
							Quantity q = new Quantity(bd, Unit.get("W"));  
							registerValue = new RegisterValue(obisCode,q,new Date());
							return registerValue;
						}
						else return new RegisterInfo(obisCode.getDescription());
					case 2:
						if (read) {
							//1114H-1115H
							ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
							c.setStartAddress(AbstractCommand.intToByteArray(0x1114));
							c.setNumRegisters(AbstractCommand.intToByteArray(2));
							outputStream.write(c.build());
							NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
							BigDecimal bd = ndp.parseF7();
							bd = applyPTCTRatio(bd);
							Quantity q = new Quantity(bd, Unit.get("W"));
							registerValue = new RegisterValue(obisCode,q,new Date());
							return registerValue;
						}
						else return new RegisterInfo(obisCode.getDescription());
					case 3: 
						if (read) {
							//1158H-1159H
							ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
							c.setStartAddress(AbstractCommand.intToByteArray(0x1158));
							c.setNumRegisters(AbstractCommand.intToByteArray(2));
							outputStream.write(c.build());
							NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
							BigDecimal bd = ndp.parseF7();
							bd = applyPTCTRatio(bd);
							Quantity q = new Quantity(bd, Unit.get("W"));  
							registerValue = new RegisterValue(obisCode,q,new Date());
							return registerValue;
						}
						else return new RegisterInfo(obisCode.getDescription());
					default:
						throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
					}

				case 8:
					// kWh cumulative energy
					switch (obisCode.getE()) {
					case 0:
						if (read) {
							//0x1B05
							ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
							c.setStartAddress(AbstractCommand.intToByteArray(0x1B05));
							c.setNumRegisters(AbstractCommand.intToByteArray(2));
							outputStream.write(c.build());
							NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
							BigDecimal bd = ndp.parseF64();
							BigDecimal divisor = new BigDecimal(1);
							LinePoint lp = new LinePoint(537,0);
							Unit unitEnergy = Unit.getUndefined();
							if (lp.isScaled()) {
								ScaledEnergySetting ses = sesf.getScaledEnergySetting(lp);
								int numDecimals = ses.getNumDecimalPlaces();
								if (numDecimals!=0)
									divisor = new BigDecimal(Math.pow(10, numDecimals));
								unitEnergy=ses.getUnit();
							}
							bd = bd.divide(divisor);
							Quantity q = new Quantity(bd, unitEnergy);  
							registerValue = new RegisterValue(obisCode,q,new Date());
							return registerValue;
						}
						else return new RegisterInfo(obisCode.getDescription());
					case 1:
						if (read) {
							//1CCF-1CD0
							ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
							c.setStartAddress(AbstractCommand.intToByteArray(0x1CCF));
							c.setNumRegisters(AbstractCommand.intToByteArray(2));
							outputStream.write(c.build());
							NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
							BigDecimal bd = ndp.parseF64();
							BigDecimal divisor = new BigDecimal(1);
							LinePoint lp = new LinePoint(563,0);
							Unit unitEnergy = Unit.getUndefined();
							if (lp.isScaled()) {
								ScaledEnergySetting ses = sesf.getScaledEnergySetting(lp);
								int numDecimals = ses.getNumDecimalPlaces();
								if (numDecimals!=0)
									divisor = new BigDecimal(Math.pow(10, numDecimals));
								unitEnergy=ses.getUnit();
							}
							bd = bd.divide(divisor, MathContext.DECIMAL128);
							Quantity q = new Quantity(bd, unitEnergy);  
							registerValue = new RegisterValue(obisCode,q,new Date());
							return registerValue;
						}
						else return new RegisterInfo(obisCode.getDescription());
					case 2:
						if (read) {
							//1CE3H-1CE4H
							ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
							c.setStartAddress(AbstractCommand.intToByteArray(0x1CE3));
							c.setNumRegisters(AbstractCommand.intToByteArray(2));
							outputStream.write(c.build());
							NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
							BigDecimal bd = ndp.parseF64();
							BigDecimal divisor = new BigDecimal(1);
							LinePoint lp = new LinePoint(563,0);
							Unit unitEnergy = Unit.getUndefined();
							if (lp.isScaled()) {
								ScaledEnergySetting ses = sesf.getScaledEnergySetting(lp);
								int numDecimals = ses.getNumDecimalPlaces();
								if (numDecimals!=0)
									divisor = new BigDecimal(Math.pow(10, numDecimals));
								unitEnergy=ses.getUnit();
							}
							bd = bd.divide(divisor, MathContext.DECIMAL128);
							Quantity q = new Quantity(bd, unitEnergy);  
							registerValue = new RegisterValue(obisCode,q,new Date());
							return registerValue;
						}
						else return new RegisterInfo(obisCode.getDescription());
					case 3: 
						if (read) {
							//1CF7H-1CF8H
							ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
							c.setStartAddress(AbstractCommand.intToByteArray(0x1CF7));
							c.setNumRegisters(AbstractCommand.intToByteArray(2));
							outputStream.write(c.build());
							NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
							BigDecimal bd = ndp.parseF64();
							BigDecimal divisor = new BigDecimal(1);
							LinePoint lp = new LinePoint(563,0);
							Unit unitEnergy = Unit.getUndefined();
							if (lp.isScaled()) {
								ScaledEnergySetting ses = sesf.getScaledEnergySetting(lp);
								int numDecimals = ses.getNumDecimalPlaces();
								if (numDecimals!=0)
									divisor = new BigDecimal(Math.pow(10, numDecimals));
								unitEnergy=ses.getUnit();
							}
							bd = bd.divide(divisor, MathContext.DECIMAL128);
							Quantity q = new Quantity(bd, unitEnergy);  
							registerValue = new RegisterValue(obisCode,q,new Date());
							return registerValue;
						}
						else return new RegisterInfo(obisCode.getDescription());
					default:
						throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
					}

				default:
					throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
				}

			case 2:
				switch (obisCode.getD()) {
				case 2:
					switch (obisCode.getE()) {
					case 1:
						if (read) {
							//1968H-1969H
							ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
							c.setStartAddress(AbstractCommand.intToByteArray(0x1968));
							c.setNumRegisters(AbstractCommand.intToByteArray(2));
							outputStream.write(c.build());
							NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
							BigDecimal bd = new BigDecimal(ndp.parseF18());
							bd = applyPTCTRatio(bd);
							Quantity q = new Quantity(bd, Unit.getUndefined());
							registerValue = new RegisterValue(obisCode,q,new Date());
							return registerValue;
						}
						else return new RegisterInfo(obisCode.getDescription());
					case 2:
						if (read) {
							//196CH-196DH
							ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
							c.setStartAddress(AbstractCommand.intToByteArray(0x196C));
							c.setNumRegisters(AbstractCommand.intToByteArray(2));
							outputStream.write(c.build());
							NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
							BigDecimal bd = new BigDecimal(ndp.parseF18());
							bd = applyPTCTRatio(bd);
							Quantity q = new Quantity(bd, Unit.getUndefined());  
							registerValue = new RegisterValue(obisCode,q,new Date());
							return registerValue;
						}
						else return new RegisterInfo(obisCode.getDescription());
					case 3: 
						if (read) {
							//1970H-1971H
							ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
							c.setStartAddress(AbstractCommand.intToByteArray(0x1970));
							c.setNumRegisters(AbstractCommand.intToByteArray(2));
							outputStream.write(c.build());
							NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
							BigDecimal bd = new BigDecimal(ndp.parseF18());
							bd = applyPTCTRatio(bd);
							Quantity q = new Quantity(bd, Unit.getUndefined());  
							registerValue = new RegisterValue(obisCode,q,new Date());
							return registerValue;
						}
						else return new RegisterInfo(obisCode.getDescription());
					default:
						throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
					}
				case 6:
					switch (obisCode.getE()) {
					case 1:
						if (read) {
							//10D2H-10D3H
							ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
							c.setStartAddress(AbstractCommand.intToByteArray(0x10D2));
							c.setNumRegisters(AbstractCommand.intToByteArray(2));
							outputStream.write(c.build());
							NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
							BigDecimal bd = ndp.parseF7();
							bd = applyPTCTRatio(bd);
							Quantity q = new Quantity(bd, Unit.get("W"));  
							registerValue = new RegisterValue(obisCode,q,new Date());
							return registerValue;
						}
						else return new RegisterInfo(obisCode.getDescription());
					case 2:
						if (read) {
							//1116H-1117H
							ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
							c.setStartAddress(AbstractCommand.intToByteArray(0x1116));
							c.setNumRegisters(AbstractCommand.intToByteArray(2));
							outputStream.write(c.build());
							NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
							BigDecimal bd = ndp.parseF7();
							bd = applyPTCTRatio(bd);
							Quantity q = new Quantity(bd, Unit.get("W"));  
							registerValue = new RegisterValue(obisCode,q,new Date());
							return registerValue;
						}
						else return new RegisterInfo(obisCode.getDescription());
					case 3: 
						if (read) {
							//115AH-115BH
							ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
							c.setStartAddress(AbstractCommand.intToByteArray(0x115A));
							c.setNumRegisters(AbstractCommand.intToByteArray(2));
							outputStream.write(c.build());
							NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
							BigDecimal bd = ndp.parseF7();
							bd = applyPTCTRatio(bd);
							Quantity q = new Quantity(bd, Unit.get("W"));  
							registerValue = new RegisterValue(obisCode,q,new Date());
							return registerValue;
						}
						else return new RegisterInfo(obisCode.getDescription());
					default:
						throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
					}
				case 8: 
					switch (obisCode.getE()) {
					case 0:
						if (read) {
							//0x1B0F
							ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
							c.setStartAddress(AbstractCommand.intToByteArray(0x1B0F));
							c.setNumRegisters(AbstractCommand.intToByteArray(2));
							outputStream.write(c.build());
							NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
							BigDecimal bd = ndp.parseF64();
							BigDecimal divisor = new BigDecimal(1);
							LinePoint lp = new LinePoint(537,5);
							Unit unitEnergy = Unit.getUndefined();
							if (lp.isScaled()) {
								ScaledEnergySetting ses = sesf.getScaledEnergySetting(lp);
								int numDecimals = ses.getNumDecimalPlaces();
								if (numDecimals!=0)
									divisor = new BigDecimal(Math.pow(10, numDecimals));
								unitEnergy=ses.getUnit();
							}
							bd = bd.divide(divisor, MathContext.DECIMAL128);
							Quantity q = new Quantity(bd, unitEnergy);  
							registerValue = new RegisterValue(obisCode,q,new Date());
							return registerValue;
						}
						else return new RegisterInfo(obisCode.getDescription());
					case 1:
						if (read) {
							//1CD9-1CDA
							ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
							c.setStartAddress(AbstractCommand.intToByteArray(0x1CD9));
							c.setNumRegisters(AbstractCommand.intToByteArray(2));
							outputStream.write(c.build());
							NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
							BigDecimal bd = ndp.parseF64();
							BigDecimal divisor = new BigDecimal(1);
							//TODO Get correct line and point numbers
							LinePoint lp = new LinePoint(565,0);
							Unit unitEnergy = Unit.getUndefined();
							if (lp.isScaled()) {
								ScaledEnergySetting ses = sesf.getScaledEnergySetting(lp);
								int numDecimals = ses.getNumDecimalPlaces();
								if (numDecimals!=0)
									divisor = new BigDecimal(Math.pow(10, numDecimals));
								unitEnergy=ses.getUnit();
							}
							bd = bd.divide(divisor, MathContext.DECIMAL128);
							Quantity q = new Quantity(bd, unitEnergy);  
							registerValue = new RegisterValue(obisCode,q,new Date());
							return registerValue;
						}
						else return new RegisterInfo(obisCode.getDescription());
					case 2:
						if (read) {
							//1CED-1CEE
							ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
							c.setStartAddress(AbstractCommand.intToByteArray(0x1CED));
							c.setNumRegisters(AbstractCommand.intToByteArray(2));
							outputStream.write(c.build());
							NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
							BigDecimal bd = ndp.parseF64();
							BigDecimal divisor = new BigDecimal(1);
							//TODO Get correct line and point numbers
							LinePoint lp = new LinePoint(565,0);
							Unit unitEnergy = Unit.getUndefined();
							if (lp.isScaled()) {
								ScaledEnergySetting ses = sesf.getScaledEnergySetting(lp);
								int numDecimals = ses.getNumDecimalPlaces();
								if (numDecimals!=0)
									divisor = new BigDecimal(Math.pow(10, numDecimals));
								unitEnergy=ses.getUnit();
							}
							bd = bd.divide(divisor, MathContext.DECIMAL128);
							Quantity q = new Quantity(bd, unitEnergy);  
							registerValue = new RegisterValue(obisCode,q,new Date());
							return registerValue;
						}
						else return new RegisterInfo(obisCode.getDescription());
					case 3: 
						if (read) {
							//1D01-1D02
							ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
							c.setStartAddress(AbstractCommand.intToByteArray(0x1D01));
							c.setNumRegisters(AbstractCommand.intToByteArray(2));
							outputStream.write(c.build());
							NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
							BigDecimal bd = ndp.parseF64();
							BigDecimal divisor = new BigDecimal(1);
							//TODO Get correct line and point numbers
							LinePoint lp = new LinePoint(565,0);
							Unit unitEnergy = Unit.getUndefined();
							if (lp.isScaled()) {
								ScaledEnergySetting ses = sesf.getScaledEnergySetting(lp);
								int numDecimals = ses.getNumDecimalPlaces();
								if (numDecimals!=0)
									divisor = new BigDecimal(Math.pow(10, numDecimals));
								unitEnergy=ses.getUnit();
							}
							bd = bd.divide(divisor, MathContext.DECIMAL128);
							Quantity q = new Quantity(bd, unitEnergy);  
							registerValue = new RegisterValue(obisCode,q,new Date());
							return registerValue;
						}
						else return new RegisterInfo(obisCode.getDescription());
					default:
						throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
					}
				default:
					throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
				}

			case 3:
				switch (obisCode.getD()) {
				case 8: 
					switch (obisCode.getE()) {
					case 0:
						if (read) {
							//1B01
							ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
							c.setStartAddress(AbstractCommand.intToByteArray(0x1B01));
							c.setNumRegisters(AbstractCommand.intToByteArray(2));
							outputStream.write(c.build());
							NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
							BigDecimal bd = ndp.parseF64();
							BigDecimal divisor = new BigDecimal(1);
							LinePoint lp = new LinePoint(536,1);
							Unit unitEnergy = Unit.getUndefined();
							if (lp.isScaled()) {
								ScaledEnergySetting ses = sesf.getScaledEnergySetting(lp);
								int numDecimals = ses.getNumDecimalPlaces();
								if (numDecimals!=0)
									divisor = new BigDecimal(Math.pow(10, numDecimals));
								unitEnergy=ses.getUnit();
							}
							bd = bd.divide(divisor, MathContext.DECIMAL128);
							Quantity q = new Quantity(bd, unitEnergy);  
							registerValue = new RegisterValue(obisCode,q,new Date());
							return registerValue;
						}
						else return new RegisterInfo(obisCode.getDescription());
					case 1:
						if (read) {
							//1CD3-1CD4
							ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
							c.setStartAddress(AbstractCommand.intToByteArray(0x1CD3));
							c.setNumRegisters(AbstractCommand.intToByteArray(2));
							outputStream.write(c.build());
							NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
							BigDecimal bd = ndp.parseF64();
							c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
							c.setStartAddress(AbstractCommand.intToByteArray(0x1CDD));
							c.setNumRegisters(AbstractCommand.intToByteArray(2));
							outputStream.write(c.build());
							ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
							BigDecimal bd2 = ndp.parseF64();
							BigDecimal divisor = new BigDecimal(1);
							//TODO Get correct line and point numbers
							LinePoint lp = new LinePoint(567,0);
							Unit unitEnergy = Unit.getUndefined();
							if (lp.isScaled()) {
								ScaledEnergySetting ses = sesf.getScaledEnergySetting(lp);
								int numDecimals = ses.getNumDecimalPlaces();
								if (numDecimals!=0)
									divisor = new BigDecimal(Math.pow(10, numDecimals));
								unitEnergy=ses.getUnit();
							}
							bd = bd.divide(divisor, MathContext.DECIMAL128);
							bd2 = bd2.divide(divisor, MathContext.DECIMAL128);
							BigDecimal val = bd2.add(bd);
							Quantity q = new Quantity(val, unitEnergy);
							registerValue = new RegisterValue(obisCode,q,new Date());
							return registerValue;
						}
						else return new RegisterInfo(obisCode.getDescription());
					case 2:
						if (read) {
							//1CE7-1CE8
							ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
							c.setStartAddress(AbstractCommand.intToByteArray(0x1CE7));
							c.setNumRegisters(AbstractCommand.intToByteArray(2));
							outputStream.write(c.build());
							NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
							BigDecimal bd = ndp.parseF64();
							//1CF1-1CF2
							c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
							c.setStartAddress(AbstractCommand.intToByteArray(0x1CF1));
							c.setNumRegisters(AbstractCommand.intToByteArray(2));
							outputStream.write(c.build());
							ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
							BigDecimal bd2 = ndp.parseF64();

							BigDecimal divisor = new BigDecimal(1);
							//TODO Get correct line and point numbers
							LinePoint lp = new LinePoint(567,0);
							Unit unitEnergy = Unit.getUndefined();
							if (lp.isScaled()) {
								ScaledEnergySetting ses = sesf.getScaledEnergySetting(lp);
								int numDecimals = ses.getNumDecimalPlaces();
								if (numDecimals!=0)
									divisor = new BigDecimal(Math.pow(10, numDecimals));
								unitEnergy=ses.getUnit();
							}
							bd = bd.divide(divisor, MathContext.DECIMAL128);
							bd2 = bd2.divide(divisor, MathContext.DECIMAL128);
							BigDecimal val = bd2.add(bd);
							Quantity q = new Quantity(val, unitEnergy);
							registerValue = new RegisterValue(obisCode,q,new Date());
							return registerValue;
						}
						else return new RegisterInfo(obisCode.getDescription());
					case 3: 
						if (read) {
							//1CFB-1CFC
							ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
							c.setStartAddress(AbstractCommand.intToByteArray(0x1CFB));
							c.setNumRegisters(AbstractCommand.intToByteArray(2));
							outputStream.write(c.build());
							NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
							BigDecimal bd = ndp.parseF64();
							//1D05-1D06
							c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
							c.setStartAddress(AbstractCommand.intToByteArray(0x1D05));
							c.setNumRegisters(AbstractCommand.intToByteArray(2));
							outputStream.write(c.build());
							ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
							BigDecimal bd2 = ndp.parseF64();

							BigDecimal divisor = new BigDecimal(1);
							//TODO Get correct line and point numbers
							LinePoint lp = new LinePoint(567,0);
							Unit unitEnergy = Unit.getUndefined();
							if (lp.isScaled()) {
								ScaledEnergySetting ses = sesf.getScaledEnergySetting(lp);
								int numDecimals = ses.getNumDecimalPlaces();
								if (numDecimals!=0)
									divisor = new BigDecimal(Math.pow(10, numDecimals));
								unitEnergy=ses.getUnit();
							}
							bd = bd.divide(divisor, MathContext.DECIMAL128);
							bd2 = bd2.divide(divisor, MathContext.DECIMAL128);
							BigDecimal val = bd2.add(bd);
							Quantity q = new Quantity(val, unitEnergy);
							registerValue = new RegisterValue(obisCode,q,new Date());
							return registerValue;
						}
						else return new RegisterInfo(obisCode.getDescription());
					default:
						throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
					}
				default:
					throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
				}

			case 4:
				switch (obisCode.getD()) {
				case 8: 
					switch (obisCode.getE()) {
					case 0:
						if (read) {
							//1B03
							ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
							c.setStartAddress(AbstractCommand.intToByteArray(0x1B03));
							c.setNumRegisters(AbstractCommand.intToByteArray(2));
							outputStream.write(c.build());
							NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
							BigDecimal bd = ndp.parseF64();
							BigDecimal divisor = new BigDecimal(1);
							LinePoint lp = new LinePoint(536,2);
							Unit unitEnergy = Unit.getUndefined();
							if (lp.isScaled()) {
								ScaledEnergySetting ses = sesf.getScaledEnergySetting(lp);
								int numDecimals = ses.getNumDecimalPlaces();
								if (numDecimals!=0)
									divisor = new BigDecimal(Math.pow(10, numDecimals));
								unitEnergy=ses.getUnit();
							}
							bd = bd.divide(divisor, MathContext.DECIMAL128);
							Quantity q = new Quantity(bd, unitEnergy);  
							registerValue = new RegisterValue(obisCode,q,new Date());
							return registerValue;
						}
						else return new RegisterInfo(obisCode.getDescription());
					case 1:
						if (read) {
							//1CE3-1CE4
							ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
							c.setStartAddress(AbstractCommand.intToByteArray(0x1CE3));
							c.setNumRegisters(AbstractCommand.intToByteArray(2));
							outputStream.write(c.build());
							NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
							BigDecimal bd = ndp.parseF64();
							//1CD7-1CD8
							c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
							c.setStartAddress(AbstractCommand.intToByteArray(0x1CD7));
							c.setNumRegisters(AbstractCommand.intToByteArray(2));
							outputStream.write(c.build());
							ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
							BigDecimal bd2 = ndp.parseF64();

							BigDecimal divisor = new BigDecimal(1);
							//TODO Get correct line and point numbers
							LinePoint lp = new LinePoint(569,0);
							Unit unitEnergy = Unit.getUndefined();
							if (lp.isScaled()) {
								ScaledEnergySetting ses = sesf.getScaledEnergySetting(lp);
								int numDecimals = ses.getNumDecimalPlaces();
								if (numDecimals!=0)
									divisor = new BigDecimal(Math.pow(10, numDecimals));
								unitEnergy=ses.getUnit();
							}
							bd = bd.divide(divisor, MathContext.DECIMAL128);
							bd2 = bd2.divide(divisor, MathContext.DECIMAL128);
							BigDecimal val = bd2.add(bd);
							Quantity q = new Quantity(val, unitEnergy);
							registerValue = new RegisterValue(obisCode,q,new Date());
							return registerValue;
						}
						else return new RegisterInfo(obisCode.getDescription());
					case 2:
						if (read) {
							//1CF7-1CF8
							ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
							c.setStartAddress(AbstractCommand.intToByteArray(0x1CF7));
							c.setNumRegisters(AbstractCommand.intToByteArray(2));
							outputStream.write(c.build());
							NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
							BigDecimal bd = ndp.parseF64();
							//1CEB-1CEC
							c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
							c.setStartAddress(AbstractCommand.intToByteArray(0x1CEB));
							c.setNumRegisters(AbstractCommand.intToByteArray(2));
							outputStream.write(c.build());
							ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
							BigDecimal bd2 = ndp.parseF64();

							BigDecimal divisor = new BigDecimal(1);
							//TODO Get correct line and point numbers
							LinePoint lp = new LinePoint(569,0);
							Unit unitEnergy = Unit.getUndefined();
							if (lp.isScaled()) {
								ScaledEnergySetting ses = sesf.getScaledEnergySetting(lp);
								int numDecimals = ses.getNumDecimalPlaces();
								if (numDecimals!=0)
									divisor = new BigDecimal(Math.pow(10, numDecimals));
								unitEnergy=ses.getUnit();
							}
							bd = bd.divide(divisor, MathContext.DECIMAL128);
							bd2 = bd2.divide(divisor, MathContext.DECIMAL128);
							BigDecimal val = bd2.add(bd);
							Quantity q = new Quantity(val, unitEnergy);
							registerValue = new RegisterValue(obisCode,q,new Date());
							return registerValue;
						}
						else return new RegisterInfo(obisCode.getDescription());
					case 3: 
						if (read) {
							//1D0B-1D0C
							ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
							c.setStartAddress(AbstractCommand.intToByteArray(0x1D0B));
							c.setNumRegisters(AbstractCommand.intToByteArray(2));
							outputStream.write(c.build());
							NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
							BigDecimal bd = ndp.parseF64();
							//1CFF-1D00
							c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
							c.setStartAddress(AbstractCommand.intToByteArray(0x1CFF));
							c.setNumRegisters(AbstractCommand.intToByteArray(2));
							outputStream.write(c.build());
							ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
							BigDecimal bd2 = ndp.parseF64();

							BigDecimal divisor = new BigDecimal(1);
							//TODO Get correct line and point numbers
							LinePoint lp = new LinePoint(569,0);
							Unit unitEnergy = Unit.getUndefined();
							if (lp.isScaled()) {
								ScaledEnergySetting ses = sesf.getScaledEnergySetting(lp);
								int numDecimals = ses.getNumDecimalPlaces();
								if (numDecimals!=0)
									divisor = new BigDecimal(Math.pow(10, numDecimals));
								unitEnergy=ses.getUnit();
							}
							bd = bd.divide(divisor, MathContext.DECIMAL128);
							bd2 = bd2.divide(divisor, MathContext.DECIMAL128);
							BigDecimal val = bd2.add(bd);
							Quantity q = new Quantity(val, unitEnergy);
							registerValue = new RegisterValue(obisCode,q,new Date());
							return registerValue;
						}
						else return new RegisterInfo(obisCode.getDescription());
					default:
						throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
					}
				default:
					throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
				}

			case 13:
				switch (obisCode.getD()) {
				case 4:
					switch(obisCode.getE()) {
					case 0:
						if (read) {
							//0124H
							ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
							c.setStartAddress(AbstractCommand.intToByteArray(0x0124));
							outputStream.write(c.build());
							NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
							BigDecimal bd = ndp.parseF8();
							Quantity q = new Quantity(bd, Unit.getUndefined());  
							registerValue = new RegisterValue(obisCode,q,new Date());
							return registerValue;
						}
						else return new RegisterInfo(obisCode.getDescription());
					default:
						throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
					}
				default:
					throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
				}

			case 31:
				switch (obisCode.getD()) {
				case 7:
					switch(obisCode.getE()) {
					case 0:
						if (read) {
							//00BBH-00BCH
							ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
							c.setStartAddress(AbstractCommand.intToByteArray(0x00BB));
							c.setNumRegisters(AbstractCommand.intToByteArray(2));
							outputStream.write(c.build());
							NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
							BigDecimal bd = ndp.parseF7();
							bd = applyCTRatio(bd);
							Quantity q = new Quantity(bd, Unit.get("A"));  
							registerValue = new RegisterValue(obisCode,q,new Date());
							return registerValue;
						}
						else return new RegisterInfo(obisCode.getDescription());
					case 124:
						if (read) {
							//0A00H
							ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
							c.setStartAddress(AbstractCommand.intToByteArray(0x0A00));
							outputStream.write(c.build());
							NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
							BigDecimal bd = ndp.parseF10();
							Quantity q = new Quantity(bd, Unit.getUndefined());
							registerValue = new RegisterValue(obisCode,q,new Date());
							return registerValue;
						}
						else return new RegisterInfo(obisCode.getDescription());
					default:
						throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
					}
				default:
					throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
				}

			case 32:
				switch (obisCode.getD()) {
				case 7:
					switch(obisCode.getE()) {
					case 0:
						if (read) {
							ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
							if (!isDeltaWired) {
								//00B3H-00B4H
								c.setStartAddress(AbstractCommand.intToByteArray(0x00B3));
							} else {
								//00C5H-00C6H
								c.setStartAddress(AbstractCommand.intToByteArray(0x00C5));
							}
							c.setNumRegisters(AbstractCommand.intToByteArray(2));
							outputStream.write(c.build());
							NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
							BigDecimal bd = ndp.parseF7();
							bd = applyPTRatio(bd);
							Quantity q = new Quantity(bd, Unit.get("V"));  
							registerValue = new RegisterValue(obisCode,q,new Date());
							return registerValue;
						}
						else return new RegisterInfo(obisCode.getDescription());
					case 124:
						if (read) {
							//09FDH
							ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
							c.setStartAddress(AbstractCommand.intToByteArray(0x09FD));
							outputStream.write(c.build());
							NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
							BigDecimal bd = ndp.parseF10();
							Quantity q = new Quantity(bd, Unit.getUndefined());  
							registerValue = new RegisterValue(obisCode,q,new Date());
							return registerValue;
						}
						else return new RegisterInfo(obisCode.getDescription());
					default:
						throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
					}
				default:
					throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
				}

			case 51:
				switch (obisCode.getD()) {
				case 7:
					switch(obisCode.getE()) {
					case 0:
						if (read) {
							//00BDH-00BEH
							ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
							c.setStartAddress(AbstractCommand.intToByteArray(0x00BD));
							c.setNumRegisters(AbstractCommand.intToByteArray(2));
							outputStream.write(c.build());
							NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
							BigDecimal bd = ndp.parseF7();
							bd = applyCTRatio(bd);
							Quantity q = new Quantity(bd, Unit.get("A"));  
							registerValue = new RegisterValue(obisCode,q,new Date());
							return registerValue;
						}
						else return new RegisterInfo(obisCode.getDescription());
					case 124:
						if (read) {
							//0A01H
							ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
							c.setStartAddress(AbstractCommand.intToByteArray(0x0A01));
							outputStream.write(c.build());
							NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
							BigDecimal bd = ndp.parseF10();
							Quantity q = new Quantity(bd, Unit.getUndefined());  
							registerValue = new RegisterValue(obisCode,q,new Date());
							return registerValue;
						}
						else return new RegisterInfo(obisCode.getDescription());
					default:
						throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
					}
				default:
					throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
				}

			case 52:
				switch (obisCode.getD()) {
				case 7:
					switch(obisCode.getE()) {
					case 0:
						if (read) {
							ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
							if(!isDeltaWired) {
								//00B5H-00B6H
								c.setStartAddress(AbstractCommand.intToByteArray(0x00B5));
							} else {
								//00C7H-00C8H
								c.setStartAddress(AbstractCommand.intToByteArray(0x00C7));
							}
							c.setNumRegisters(AbstractCommand.intToByteArray(2));
							outputStream.write(c.build());
							NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
							BigDecimal bd = ndp.parseF7();
							bd = applyPTRatio(bd);
							Quantity q = new Quantity(bd, Unit.get("V"));  
							registerValue = new RegisterValue(obisCode,q,new Date());
							return registerValue;
						}
						else return new RegisterInfo(obisCode.getDescription());
					case 124:
						if (read) {
							//09FEH
							ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
							c.setStartAddress(AbstractCommand.intToByteArray(0x09FE));
							outputStream.write(c.build());
							NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
							BigDecimal bd = ndp.parseF10();
							Quantity q = new Quantity(bd, Unit.getUndefined());  
							registerValue = new RegisterValue(obisCode,q,new Date());
							return registerValue;
						}
						else return new RegisterInfo(obisCode.getDescription());
					default:
						throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
					}
				default:
					throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
				}

			case 71:
				switch (obisCode.getD()) {
				case 7:
					switch(obisCode.getE()) {
					case 0:
						if (read) {
							//00BFH-00C0H
							ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
							c.setStartAddress(AbstractCommand.intToByteArray(0x00BF));
							c.setNumRegisters(AbstractCommand.intToByteArray(2));
							outputStream.write(c.build());
							NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
							BigDecimal bd = ndp.parseF7();
							bd = applyCTRatio(bd);
							Quantity q = new Quantity(bd, Unit.get("A"));  
							registerValue = new RegisterValue(obisCode,q,new Date());
							return registerValue;
						}
						else return new RegisterInfo(obisCode.getDescription());
					case 124:
						if (read) {
							//0A02H
							ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
							c.setStartAddress(AbstractCommand.intToByteArray(0x0A02));
							outputStream.write(c.build());
							NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
							BigDecimal bd = ndp.parseF10();
							Quantity q = new Quantity(bd, Unit.getUndefined());
							registerValue = new RegisterValue(obisCode,q,new Date());
							return registerValue;
						}
						else return new RegisterInfo(obisCode.getDescription());
					default:
						throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
					}
				default:
					throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
				}

			case 72:
				switch (obisCode.getD()) {
				case 7:
					switch(obisCode.getE()) {
					case 0:
						if (read) {
							ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
							if(!isDeltaWired) {
								//00B7H-00B8H
								c.setStartAddress(AbstractCommand.intToByteArray(0x00B7));
							} else {
								//00C9H-00CAH
								c.setStartAddress(AbstractCommand.intToByteArray(0x00C9));
							}
							c.setNumRegisters(AbstractCommand.intToByteArray(2));
							outputStream.write(c.build());
							NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
							BigDecimal bd = ndp.parseF7();
							bd = applyPTRatio(bd);
							Quantity q = new Quantity(bd, Unit.get("V"));  
							registerValue = new RegisterValue(obisCode,q,new Date());
							return registerValue;
						}
						else return new RegisterInfo(obisCode.getDescription());
					case 124:
						if (read) {
							//09FFH
							ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
							c.setStartAddress(AbstractCommand.intToByteArray(0x09FF));
							outputStream.write(c.build());
							NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
							BigDecimal bd = ndp.parseF10();
							Quantity q = new Quantity(bd, Unit.getUndefined());  
							registerValue = new RegisterValue(obisCode,q,new Date());
							return registerValue;
						}
						else return new RegisterInfo(obisCode.getDescription());
					default:
						throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
					}
				default:
					throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
				}

			case 81:
				switch (obisCode.getD()) {
				case 7:
					switch(obisCode.getE()) {
					case 2:
						if (read) {
							ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
							if (!isDeltaWired) {
								//0a24H
								c.setStartAddress(AbstractCommand.intToByteArray(0x0a24));
							} else {
								//0A2AH
								c.setStartAddress(AbstractCommand.intToByteArray(0x0a2a));
							}
							outputStream.write(c.build());
							NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
							BigDecimal bd = ndp.parseF9();
							Quantity q = new Quantity(bd, Unit.get("\u00B0"));  
							registerValue = new RegisterValue(obisCode,q,new Date());
							return registerValue;
						}
						else return new RegisterInfo(obisCode.getDescription());
					case 4:
						if (read) {
							//0a25H
							ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
							c.setStartAddress(AbstractCommand.intToByteArray(0x0a25));
							outputStream.write(c.build());
							NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
							BigDecimal bd = ndp.parseF9();
							Quantity q = new Quantity(bd, Unit.get("\u00B0"));  
							registerValue = new RegisterValue(obisCode,q,new Date());
							return registerValue;
						}
						else return new RegisterInfo(obisCode.getDescription());
					case 10:
						if (read) {
							ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
							if(!isDeltaWired) {
								//0a22H
								c.setStartAddress(AbstractCommand.intToByteArray(0x0a22));
							} else {
								//0a28H
								c.setStartAddress(AbstractCommand.intToByteArray(0x0a28));
							}
							outputStream.write(c.build());
							NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
							BigDecimal bd = ndp.parseF9();
							Quantity q = new Quantity(bd, Unit.get("\u00B0"));  
							registerValue = new RegisterValue(obisCode,q,new Date());
							return registerValue;
						}
						else return new RegisterInfo(obisCode.getDescription());
					case 15:
						if (read) {
							//0a26H
							ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
							c.setStartAddress(AbstractCommand.intToByteArray(0x0a26));
							outputStream.write(c.build());
							NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
							BigDecimal bd = ndp.parseF9();
							Quantity q = new Quantity(bd, Unit.get("\u00B0"));  
							registerValue = new RegisterValue(obisCode,q,new Date());
							return registerValue;
						}
						else return new RegisterInfo(obisCode.getDescription());
					case 21:
						if (read) {
							ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
							if(!isDeltaWired) {
								//0a23H
								c.setStartAddress(AbstractCommand.intToByteArray(0x0a23));
							} else {
								//0A29H
								c.setStartAddress(AbstractCommand.intToByteArray(0x0a29));
							}
							outputStream.write(c.build());
							NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
							BigDecimal bd = ndp.parseF9();
							Quantity q = new Quantity(bd, Unit.get("\u00B0"));  
							registerValue = new RegisterValue(obisCode,q,new Date());
							return registerValue;
						}
						else return new RegisterInfo(obisCode.getDescription());
					case 26:
						if (read) {
							//0a27H
							ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
							c.setStartAddress(AbstractCommand.intToByteArray(0x0a27));
							outputStream.write(c.build());
							NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
							BigDecimal bd = ndp.parseF9();
							Quantity q = new Quantity(bd, Unit.get("\u00B0"));  
							registerValue = new RegisterValue(obisCode,q,new Date());
							return registerValue;
						}
						else return new RegisterInfo(obisCode.getDescription());
					default:
						throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
					}
				default:
					throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
				}

			case 83:
				switch (obisCode.getD()) {
				case 8:
					switch(obisCode.getE()) {
					case 49:
						if (read) {
							//0B85H-0B88H
							ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
							c.setStartAddress(AbstractCommand.intToByteArray(0x0B85));
							c.setNumRegisters(AbstractCommand.intToByteArray(4));
							outputStream.write(c.build());
							NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
							BigDecimal bd = new BigDecimal(ndp.parseF20()%100000000);
							Quantity q = new Quantity(bd, Unit.getUndefined());  
							registerValue = new RegisterValue(obisCode,q,new Date());
							return registerValue;
						}
						else return new RegisterInfo(obisCode.getDescription());
					case 50:
						if (read) {
							//0B91H-0B94H
							ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
							c.setStartAddress(AbstractCommand.intToByteArray(0x0B91));
							c.setNumRegisters(AbstractCommand.intToByteArray(4));
							outputStream.write(c.build());
							NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
							BigDecimal bd = new BigDecimal(ndp.parseF20()%100000000);

							Quantity q = new Quantity(bd, Unit.getUndefined());  
							registerValue = new RegisterValue(obisCode,q,new Date());
							return registerValue;
						}
						else return new RegisterInfo(obisCode.getDescription());
					case 69:
						if (read) {
							//0B89H-0B8CH
							ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
							c.setStartAddress(AbstractCommand.intToByteArray(0x0B89));
							c.setNumRegisters(AbstractCommand.intToByteArray(4));
							outputStream.write(c.build());
							NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
							BigDecimal bd = new BigDecimal(ndp.parseF20()%100000000);
							Quantity q = new Quantity (bd, Unit.getUndefined());
							registerValue = new RegisterValue(obisCode,q,new Date());
							return registerValue;
						}
						else return new RegisterInfo(obisCode.getDescription());
					case 70:
						if (read) {
							//0B95H-0B98H
							ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
							c.setStartAddress(AbstractCommand.intToByteArray(0x0B95));
							c.setNumRegisters(AbstractCommand.intToByteArray(4));
							outputStream.write(c.build());
							NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
							BigDecimal bd = new BigDecimal(ndp.parseF20()%100000000);
							Quantity q = new Quantity (bd, Unit.getUndefined());
							registerValue = new RegisterValue(obisCode,q,new Date());
							return registerValue;
						}
						else return new RegisterInfo(obisCode.getDescription());
					case 89:
						if (read) {
							//0B8DH-0B90H
							ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
							c.setStartAddress(AbstractCommand.intToByteArray(0x0B8D));
							c.setNumRegisters(AbstractCommand.intToByteArray(4));
							outputStream.write(c.build());
							NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
							BigDecimal bd = new BigDecimal(ndp.parseF20()%100000000);
							Quantity q = new Quantity (bd, Unit.getUndefined());
							registerValue = new RegisterValue(obisCode,q,new Date());
							return registerValue;
						}
						else return new RegisterInfo(obisCode.getDescription());
					case 90:
						if (read) {
							//0B99H-0B9CH
							ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
							c.setStartAddress(AbstractCommand.intToByteArray(0x0B99));
							c.setNumRegisters(AbstractCommand.intToByteArray(4));
							outputStream.write(c.build());
							NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
							BigDecimal bd = new BigDecimal(ndp.parseF20()%100000000);
							Quantity q = new Quantity(bd, Unit.getUndefined());  
							registerValue = new RegisterValue(obisCode,q,new Date());
							return registerValue;
						}
						else return new RegisterInfo(obisCode.getDescription());
					default:
						throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
					}
				default:
					throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
				}

			default:
				throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
			}



		}

		if ((read) && (registerValue != null))
			return registerValue;
		else
			throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
	}

	private BigDecimal applyPTCTRatio(BigDecimal bd) throws IOException {
		if (multiplier == null) {
			ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
			c.setStartAddress(AbstractCommand.intToByteArray(0xB354));
			c.setNumRegisters(AbstractCommand.intToByteArray(16));
			outputStream.write(c.build());
			NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
			int ctNum = ndp.parseFourByteInt();
			int ctDen = ndp.parseFourByteInt();
			ndp.parseFourByteInt();
			ndp.parseFourByteInt();
			int ptNum = ndp.parseFourByteInt();
			int ptDen = ndp.parseFourByteInt();
			multiplier = new BigDecimal(ctNum).divide(new BigDecimal(ctDen), MathContext.DECIMAL128).multiply(new BigDecimal(ptNum).divide(new BigDecimal(ptDen), MathContext.DECIMAL128));
		}

		return bd.multiply((multiplier));
	}

	private BigDecimal applyPTRatio(BigDecimal bd) throws IOException {
		if (PTmultiplier == null) {
			ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
			c.setStartAddress(AbstractCommand.intToByteArray(0xB354));
			c.setNumRegisters(AbstractCommand.intToByteArray(16));
			outputStream.write(c.build());
			NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
			ndp.parseFourByteInt();
			ndp.parseFourByteInt();
			ndp.parseFourByteInt();
			ndp.parseFourByteInt();
			int ptNum = ndp.parseFourByteInt();
			int ptDen = ndp.parseFourByteInt();

			PTmultiplier = new BigDecimal(ptNum).divide(new BigDecimal(ptDen), MathContext.DECIMAL128);
		}

		return bd.multiply((PTmultiplier));
	}

	private BigDecimal applyCTRatio(BigDecimal bd) throws IOException {
		if (CTmultiplier == null) {
			ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
			c.setStartAddress(AbstractCommand.intToByteArray(0xB354));
			c.setNumRegisters(AbstractCommand.intToByteArray(16));
			outputStream.write(c.build());
			NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
			int ctNum = ndp.parseFourByteInt();
			int ctDen = ndp.parseFourByteInt();

			CTmultiplier = new BigDecimal(ctNum).divide(new BigDecimal(ctDen), MathContext.DECIMAL128);
		}

		return bd.multiply((CTmultiplier));
	}

}