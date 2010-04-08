package com.energyict.protocolimpl.eig.nexus1272;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.Date;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.base.ObisUtils;
import com.energyict.protocolimpl.eig.nexus1272.command.AbstractCommand;
import com.energyict.protocolimpl.eig.nexus1272.command.NexusCommandFactory;
import com.energyict.protocolimpl.eig.nexus1272.command.ReadCommand;
import com.energyict.protocolimpl.eig.nexus1272.parse.NexusDataParser;

public class ObisCodeMapper {
    
	
    
    
    NexusCommandFactory nexusCommandFactory;
    NexusProtocolConnection connection;
    OutputStream outputStream;
    
    /** Creates a new instance of ObisCodeMapper 
     * @param connection 
     * @param outputStream */
    public ObisCodeMapper(NexusCommandFactory nexusCommandFactory, NexusProtocolConnection connection, OutputStream outputStream) {
        this.nexusCommandFactory=nexusCommandFactory;
        this.connection = connection;
        this.outputStream = outputStream;
    }
    
    static public RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
        ObisCodeMapper ocm = new ObisCodeMapper(null, null, null);
        return (RegisterInfo)ocm.doGetRegister(obisCode,false);
    }

    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
        return (RegisterValue)doGetRegister(obisCode, true);
    }
    
    private Object doGetRegister(ObisCode obisCode, boolean read) throws IOException {
        
        RegisterValue registerValue=null;
        String registerName=null;
        int billingPoint=-1;
        Unit unit = null;
        
        // ********************************************************************************* 
        // Manufacturer specific
        if (ObisUtils.isManufacturerSpecific(obisCode)) {
//            if (read) {
//                GenericValue genv = nexusCommandFactory.getGenericValue(obisCode.getB());                
//                if (genv==null)
//                    throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported! No command for B field "+obisCode.getB());
//                else {
//                    int value = genv.getValue(obisCode.getE(), obisCode.getF());
//                    if (value != -1) {
//                        registerValue = new RegisterValue(obisCode,
//                                                          new Quantity(BigDecimal.valueOf((long)value).multiply(nexusCommandFactory.ez7.getAdjustRegisterMultiplier()),Unit.get("")),
//                                                          null, // eventtime
//                                                          null, // fromtime
//                                                          null, // totime
//                                                          new Date(), // readtime
//                                                          0, // registerid
//                                                          "0x"+Integer.toHexString(value)); // text
//                    }
//                    else throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
//                }
//            }
//            else return new RegisterInfo("manufacturer specific ObisCode"); 
        } // if (ObisUtils.isManufacturerSpecific(obisCode)) 
//        // obis F code
//        else if (obisCode.getF() != 255)
//            throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported! No billing points supported!");
        // *********************************************************************************
        // Electricity related ObisRegisters
        else if ((obisCode.getA() == 1) && (obisCode.getB() >= 1) && (obisCode.getB() <= 8)) {
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
//            				System.out.println("0*** " + ndp.parseF18());
//                       	System.out.println(obisCode);  
                       	BigDecimal bd = new BigDecimal(ndp.parseF18());
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
//            				System.out.println("1*** " + ndp.parseF18());
//                       	System.out.println(obisCode);  
                       	BigDecimal bd = new BigDecimal(ndp.parseF18());
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
//            				System.out.println("2*** " + ndp.parseF18());
//                       	System.out.println(obisCode); 
            				BigDecimal bd = new BigDecimal(ndp.parseF18());
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
//            				System.out.println("00*** " + ndp.parseF7());
//                        	System.out.println(obisCode); 
                        	BigDecimal bd = ndp.parseF7();
                        	Quantity q = new Quantity(bd, Unit.getUndefined());  
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
//            				System.out.println("11*** " + bd);
//                        	System.out.println(obisCode);  
                           Quantity q = new Quantity(bd, Unit.getUndefined());//nexusCommandFactory.getAllMaximumDemand().getQuantity(obisCode.getB()-1, obisCode.getE()-1);
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
//            				System.out.println("22*** " + ndp.parseF7());
//                        	System.out.println(obisCode);  
                        	BigDecimal bd = ndp.parseF7();
                        	Quantity q = new Quantity(bd, Unit.getUndefined());  
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
                			//1D6F-1D70
            				ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
            				c.setStartAddress(AbstractCommand.intToByteArray(0x1D6F));
            				c.setNumRegisters(AbstractCommand.intToByteArray(2));
            				outputStream.write(c.build());
            				NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
//            				System.out.println("22*** " + ndp.parseF64());
//                       	System.out.println(obisCode);  
                       	BigDecimal bd = ndp.parseF64();
                        	Quantity q = new Quantity(bd, Unit.getUndefined());  
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
//            				System.out.println("22*** " + ndp.parseF64());
//                       	System.out.println(obisCode);  
            				BigDecimal bd = ndp.parseF64();
                        	Quantity q = new Quantity(bd, Unit.getUndefined());  
                       	registerValue = new RegisterValue(obisCode,q,new Date());
                                return registerValue;
//                          }
//                          else throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
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
//            				System.out.println("22*** " + ndp.parseF64());
//                       	System.out.println(obisCode);  
            				BigDecimal bd = ndp.parseF64();
                        	Quantity q = new Quantity(bd, Unit.getUndefined());  
                       	registerValue = new RegisterValue(obisCode,q,new Date());
                                return registerValue;
//                          }
//                          else throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
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
//            				System.out.println("22*** " + ndp.parseF64());
//                       	System.out.println(obisCode);  
            				BigDecimal bd = ndp.parseF64();
                        	Quantity q = new Quantity(bd, Unit.getUndefined());  
                       	registerValue = new RegisterValue(obisCode,q,new Date());
                                return registerValue;
//                          }
//                          else throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
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
//            				System.out.println("000*** " + ndp.parseF18());
//                        	System.out.println(obisCode);  
            				BigDecimal bd = new BigDecimal(ndp.parseF18());
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
//                				System.out.println("111*** " + ndp.parseF18());
//                        	System.out.println(obisCode);  
                        	BigDecimal bd = new BigDecimal(ndp.parseF18());
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
//                				System.out.println("222*** " + ndp.parseF18());
//                        	System.out.println(obisCode);  
                				BigDecimal bd = new BigDecimal(ndp.parseF18());
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
//            				System.out.println("*** " + ndp.parseF7());
//                        	System.out.println(obisCode);  
            				BigDecimal bd = ndp.parseF7();
                        	Quantity q = new Quantity(bd, Unit.getUndefined());  
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
//            				System.out.println("*** " + ndp.parseF7());
//                        	System.out.println(obisCode);  
            				BigDecimal bd = ndp.parseF7();
                        	Quantity q = new Quantity(bd, Unit.getUndefined());  
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
//            				System.out.println("*** " + ndp.parseF7());
//                        	System.out.println(obisCode);  
            				BigDecimal bd = ndp.parseF7();
                        	Quantity q = new Quantity(bd, Unit.getUndefined());  
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
            				//1D79-1D7A
            				ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
            				c.setStartAddress(AbstractCommand.intToByteArray(0x1D79));
            				c.setNumRegisters(AbstractCommand.intToByteArray(2));
            				outputStream.write(c.build());
            				NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
//            				System.out.println("22*** " + ndp.parseF64());
//                        	System.out.println(obisCode);  
            				BigDecimal bd = ndp.parseF64();
                        	Quantity q = new Quantity(bd, Unit.getUndefined());  
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
//            				System.out.println("22*** " + ndp.parseF64());
//                        	System.out.println(obisCode);  
            				BigDecimal bd = ndp.parseF64();
                        	Quantity q = new Quantity(bd, Unit.getUndefined());  
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
//            				System.out.println("22*** " + ndp.parseF64());
//                        	System.out.println(obisCode);  
                        	BigDecimal bd = ndp.parseF64();
                        	Quantity q = new Quantity(bd, Unit.getUndefined());  
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
//            				System.out.println("22*** " + ndp.parseF64());
//                        	System.out.println(obisCode);
            				BigDecimal bd = ndp.parseF64();
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
        		
        	case 3:
        		switch (obisCode.getD()) {
        		case 8: 
        			switch (obisCode.getE()) {
        			case 1:
            			if (read) {
            				//1CD3-1CD4
            				ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
            				c.setStartAddress(AbstractCommand.intToByteArray(0x1CD3));
            				c.setNumRegisters(AbstractCommand.intToByteArray(2));
            				outputStream.write(c.build());
            				NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
            				BigDecimal bd = ndp.parseF64();
//            				System.out.println("381-a*** " + bd);
            				//1CDD-1CDE
            				c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
            				c.setStartAddress(AbstractCommand.intToByteArray(0x1CDD));
            				c.setNumRegisters(AbstractCommand.intToByteArray(2));
            				outputStream.write(c.build());
            				ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
            				BigDecimal bd2 = ndp.parseF64();
//            				System.out.println("381-b*** " + bd2);
//            				System.out.println("381-t***" + bd2.add(bd));
//                        	System.out.println(obisCode );  
                        	BigDecimal val = bd2.add(bd);
                        	Quantity q = new Quantity(val, Unit.getUndefined());
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
//            				System.out.println("382-a*** " + bd);
            				//1CF1-1CF2
            				c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
            				c.setStartAddress(AbstractCommand.intToByteArray(0x1CF1));
            				c.setNumRegisters(AbstractCommand.intToByteArray(2));
            				outputStream.write(c.build());
            				ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
            				BigDecimal bd2 = ndp.parseF64();
//            				System.out.println("382-b*** " + bd2);
//            				System.out.println("382-t***" + bd2.add(bd));
//                        	System.out.println(obisCode );
                        	BigDecimal val = bd2.add(bd);
                        	Quantity q = new Quantity(val, Unit.getUndefined());
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
//            				System.out.println("383-a*** " + bd);
            				//1D05-1D06
            				c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
            				c.setStartAddress(AbstractCommand.intToByteArray(0x1D05));
            				c.setNumRegisters(AbstractCommand.intToByteArray(2));
            				outputStream.write(c.build());
            				ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
            				BigDecimal bd2 = ndp.parseF64();
//            				System.out.println("383-b*** " + bd2);
//            				System.out.println("383-t***" + bd2.add(bd));
//                        	System.out.println(obisCode );  
            				BigDecimal val = bd2.add(bd);
                        	Quantity q = new Quantity(val, Unit.getUndefined());
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
        			case 1:
            			if (read) {
            				//1CE3-1CE4
            				ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
            				c.setStartAddress(AbstractCommand.intToByteArray(0x1CE3));
            				c.setNumRegisters(AbstractCommand.intToByteArray(2));
            				outputStream.write(c.build());
            				NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
            				BigDecimal bd = ndp.parseF64();
//            				System.out.println("481-a*** " + bd);
            				//1CD7-1CD8
            				c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
            				c.setStartAddress(AbstractCommand.intToByteArray(0x1CD7));
            				c.setNumRegisters(AbstractCommand.intToByteArray(2));
            				outputStream.write(c.build());
            				ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
            				BigDecimal bd2 = ndp.parseF64();
//            				System.out.println("481-b*** " + bd2);
//            				System.out.println("481-t***" + bd2.add(bd));
//                        	System.out.println(obisCode );  
            				BigDecimal val = bd2.add(bd);
                        	Quantity q = new Quantity(val, Unit.getUndefined());
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
//            				System.out.println("482-a*** " + bd);
            				//1CEB-1CEC
            				c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
            				c.setStartAddress(AbstractCommand.intToByteArray(0x1CEB));
            				c.setNumRegisters(AbstractCommand.intToByteArray(2));
            				outputStream.write(c.build());
            				ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
            				BigDecimal bd2 = ndp.parseF64();
//            				System.out.println("482-b*** " + bd2);
//            				System.out.println("482-t***" + bd2.add(bd));
//                        	System.out.println(obisCode );  
            				BigDecimal val = bd2.add(bd);
                        	Quantity q = new Quantity(val, Unit.getUndefined());
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
//            				System.out.println("483-a*** " + bd);
            				//1CFF-1D00
            				c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
            				c.setStartAddress(AbstractCommand.intToByteArray(0x1CFF));
            				c.setNumRegisters(AbstractCommand.intToByteArray(2));
            				outputStream.write(c.build());
            				ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
            				BigDecimal bd2 = ndp.parseF64();
//            				System.out.println("483-b*** " + bd2);
//            				System.out.println("483-t***" + bd2.add(bd));
//                        	System.out.println(obisCode);  
            				BigDecimal val = bd2.add(bd);
                        	Quantity q = new Quantity(val, Unit.getUndefined());
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
//            				c.setNumRegisters(AbstractCommand.intToByteArray(2));
            				outputStream.write(c.build());
            				NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
//            				System.out.println("APF*** " + ndp.parseF8());
//                        	System.out.println(obisCode);  
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
//            				System.out.println("IA*** " + ndp.parseF7());
//                        	System.out.println(obisCode);  
            				BigDecimal bd = ndp.parseF7();
                        	Quantity q = new Quantity(bd, Unit.getUndefined());  
                       	registerValue = new RegisterValue(obisCode,q,new Date());
                                return registerValue;
                        }
                        else return new RegisterInfo(obisCode.getDescription());
        			case 124:
        				if (read) {
        					//0A00H
            				ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
            				c.setStartAddress(AbstractCommand.intToByteArray(0x0A00));
//            				c.setNumRegisters(AbstractCommand.intToByteArray(2));
            				outputStream.write(c.build());
            				NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
//            				System.out.println("THDIA*** " + ndp.parseF10());
//                        	System.out.println(obisCode );  
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
        					//00B3H-00B4H
            				ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
            				c.setStartAddress(AbstractCommand.intToByteArray(0x00B3));
            				c.setNumRegisters(AbstractCommand.intToByteArray(2));
            				outputStream.write(c.build());
            				NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
//            				System.out.println("VA*** " + ndp.parseF7());
//                        	System.out.println(obisCode);  
            				BigDecimal bd = ndp.parseF7();
                        	Quantity q = new Quantity(bd, Unit.getUndefined());  
                       	registerValue = new RegisterValue(obisCode,q,new Date());
                                return registerValue;
                        }
                        else return new RegisterInfo(obisCode.getDescription());
        			case 124:
        				if (read) {
        					//09FDH
            				ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
            				c.setStartAddress(AbstractCommand.intToByteArray(0x09FD));
//            				c.setNumRegisters(AbstractCommand.intToByteArray(2));
            				outputStream.write(c.build());
            				NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
//            				System.out.println("THDVA*** " + ndp.parseF10());
//                        	System.out.println(obisCode);  
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
//            				System.out.println("IB*** " + ndp.parseF7());
//                        	System.out.println(obisCode);  
            				BigDecimal bd = ndp.parseF7();
                        	Quantity q = new Quantity(bd, Unit.getUndefined());  
                       	registerValue = new RegisterValue(obisCode,q,new Date());
                                return registerValue;
                        }
                        else return new RegisterInfo(obisCode.getDescription());
        			case 124:
        				if (read) {
        					//0A01H
            				ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
            				c.setStartAddress(AbstractCommand.intToByteArray(0x0A01));
//            				c.setNumRegisters(AbstractCommand.intToByteArray(2));
            				outputStream.write(c.build());
            				NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
//            				System.out.println("THDIB*** " + ndp.parseF10());
//                        	System.out.println(obisCode);  
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
        					//00B5H-00B6H
            				ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
            				c.setStartAddress(AbstractCommand.intToByteArray(0x00B5));
            				c.setNumRegisters(AbstractCommand.intToByteArray(2));
            				outputStream.write(c.build());
            				NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
//            				System.out.println("VB*** " + ndp.parseF7());
//                        	System.out.println(obisCode);  
            				BigDecimal bd = ndp.parseF7();
                        	Quantity q = new Quantity(bd, Unit.getUndefined());  
                       	registerValue = new RegisterValue(obisCode,q,new Date());
                                return registerValue;
                        }
                        else return new RegisterInfo(obisCode.getDescription());
        			case 124:
        				if (read) {
        					//09FEH
            				ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
            				c.setStartAddress(AbstractCommand.intToByteArray(0x09FE));
//            				c.setNumRegisters(AbstractCommand.intToByteArray(2));
            				outputStream.write(c.build());
            				NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
//            				System.out.println("THDVB*** " + ndp.parseF10());
//                        	System.out.println(obisCode);  
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
//            				System.out.println("IC*** " + ndp.parseF7());
//                        	System.out.println(obisCode);  
            				BigDecimal bd = ndp.parseF7();
                        	Quantity q = new Quantity(bd, Unit.getUndefined());  
                       	registerValue = new RegisterValue(obisCode,q,new Date());
                                return registerValue;
                        }
                        else return new RegisterInfo(obisCode.getDescription());
        			case 124:
        				if (read) {
        					//0A02H
            				ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
            				c.setStartAddress(AbstractCommand.intToByteArray(0x0A02));
//            				c.setNumRegisters(AbstractCommand.intToByteArray(2));
            				outputStream.write(c.build());
            				NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
//            				System.out.println("THDIC*** " + ndp.parseF10());
//                        	System.out.println(obisCode );  
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
        					//00B7H-00B8H
            				ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
            				c.setStartAddress(AbstractCommand.intToByteArray(0x00B7));
            				c.setNumRegisters(AbstractCommand.intToByteArray(2));
            				outputStream.write(c.build());
            				NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
//            				System.out.println("VC*** " + ndp.parseF7());
//                        	System.out.println(obisCode);  
            				BigDecimal bd = ndp.parseF7();
                        	Quantity q = new Quantity(bd, Unit.getUndefined());  
                       	registerValue = new RegisterValue(obisCode,q,new Date());
                                return registerValue;
                        }
                        else return new RegisterInfo(obisCode.getDescription());
        			case 124:
        				if (read) {
        					//09FFH
            				ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
            				c.setStartAddress(AbstractCommand.intToByteArray(0x09FF));
//            				c.setNumRegisters(AbstractCommand.intToByteArray(2));
            				outputStream.write(c.build());
            				NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
//            				System.out.println("THDVC*** " + ndp.parseF10());
//                        	System.out.println(obisCode); 
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
        					//0a24H
            				ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
            				c.setStartAddress(AbstractCommand.intToByteArray(0x0a24));
//            				c.setNumRegisters(AbstractCommand.intToByteArray(2));
            				outputStream.write(c.build());
            				NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
//            				System.out.println("C*** " + ndp.parseF9());
//                        	System.out.println(obisCode);  
            				BigDecimal bd = ndp.parseF9();
                        	Quantity q = new Quantity(bd, Unit.getUndefined());  
                       	registerValue = new RegisterValue(obisCode,q,new Date());
                                return registerValue;
                        }
                        else return new RegisterInfo(obisCode.getDescription());
        			case 4:
        				if (read) {
        					//0a25H
            				ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
            				c.setStartAddress(AbstractCommand.intToByteArray(0x0a25));
//            				c.setNumRegisters(AbstractCommand.intToByteArray(2));
            				outputStream.write(c.build());
            				NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
//            				System.out.println("C*** " + ndp.parseF9());
//                        	System.out.println(obisCode);  
            				BigDecimal bd = ndp.parseF9();
                        	Quantity q = new Quantity(bd, Unit.getUndefined());  
                       	registerValue = new RegisterValue(obisCode,q,new Date());
                                return registerValue;
                        }
                        else return new RegisterInfo(obisCode.getDescription());
        			case 10:
        				if (read) {
        					//0a22H
            				ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
            				c.setStartAddress(AbstractCommand.intToByteArray(0x0a22));
//            				c.setNumRegisters(AbstractCommand.intToByteArray(2));
            				outputStream.write(c.build());
            				NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
//            				System.out.println("A*** " + ndp.parseF9());
//                        	System.out.println(obisCode);
            				BigDecimal bd = ndp.parseF9();
                        	Quantity q = new Quantity(bd, Unit.getUndefined());  
                       	registerValue = new RegisterValue(obisCode,q,new Date());
                                return registerValue;
                        }
                        else return new RegisterInfo(obisCode.getDescription());
        			case 15:
        				if (read) {
        					//0a26H
            				ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
            				c.setStartAddress(AbstractCommand.intToByteArray(0x0a26));
//            				c.setNumRegisters(AbstractCommand.intToByteArray(2));
            				outputStream.write(c.build());
            				NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
//            				System.out.println("C*** " + ndp.parseF9());
//                        	System.out.println(obisCode); 
            				BigDecimal bd = ndp.parseF9();
                        	Quantity q = new Quantity(bd, Unit.getUndefined());  
                       	registerValue = new RegisterValue(obisCode,q,new Date());
                                return registerValue;
                        }
                        else return new RegisterInfo(obisCode.getDescription());
        			case 21:
        				if (read) {
        					//0a23H
            				ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
            				c.setStartAddress(AbstractCommand.intToByteArray(0x0a23));
//            				c.setNumRegisters(AbstractCommand.intToByteArray(2));
            				outputStream.write(c.build());
            				NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
//            				System.out.println("B*** " + ndp.parseF9());
//                        	System.out.println(obisCode);  
            				BigDecimal bd = ndp.parseF9();
                        	Quantity q = new Quantity(bd, Unit.getUndefined());  
                       	registerValue = new RegisterValue(obisCode,q,new Date());
                                return registerValue;
                        }
                        else return new RegisterInfo(obisCode.getDescription());
        			case 26:
        				if (read) {
        					//0a27H
            				ReadCommand c = (ReadCommand) nexusCommandFactory.getReadSingleRegisterCommand();
            				c.setStartAddress(AbstractCommand.intToByteArray(0x0a27));
//            				c.setNumRegisters(AbstractCommand.intToByteArray(2));
            				outputStream.write(c.build());
            				NexusDataParser ndp = new NexusDataParser(connection.receiveWriteResponse(c).toByteArray());
//            				System.out.println("C*** " + ndp.parseF9());
//                        	System.out.println(obisCode);  
            				BigDecimal bd = ndp.parseF9();
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
//            				System.out.println("I2TA*** " + ndp.parseF20());
//                        	System.out.println(obisCode);  
                        	BigDecimal bd = new BigDecimal(ndp.parseF20());
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
//            				System.out.println("V2TA*** " + ndp.parseF20());
//                        	System.out.println(obisCode);  
            				BigDecimal bd = new BigDecimal(ndp.parseF20());
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
//            				System.out.println("I2TB*** " + ndp.parseF20());
//                        	System.out.println(obisCode );  
            				BigDecimal bd = new BigDecimal(ndp.parseF20());
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
//            				System.out.println("V2TB*** " + ndp.parseF20());
//                        	System.out.println(obisCode );  
            				BigDecimal bd = new BigDecimal(ndp.parseF20());
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
//            				System.out.println("I2TC*** " + ndp.parseF20());
//                        	System.out.println(obisCode );  
            				BigDecimal bd = new BigDecimal(ndp.parseF20());
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
//            				System.out.println("V2TC*** " + ndp.parseF20());
//                        	System.out.println(obisCode); 
            				BigDecimal bd = new BigDecimal(ndp.parseF20());
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
        
//        else if ((obisCode.getA() == 1) && (obisCode.getB() == 0)) {
//            // instantaneous values
//            if ((obisCode.getD() == 7) && (obisCode.getE() == 0)) {
//                if (read) {
//                    Quantity quantity=null;
//                    // Current
//                    if ((obisCode.getC() == 11) || (obisCode.getC() == 31) || (obisCode.getC() == 51) || (obisCode.getC() == 71)) {
//                       quantity = nexusCommandFactory.getPowerQuality().getAmperage((obisCode.getC()-11)/20); 
//                    }
//                    // Voltage
//                    else if ((obisCode.getC() == 12) || (obisCode.getC() == 32) || (obisCode.getC() == 52) || (obisCode.getC() == 72)) {
//                       quantity = nexusCommandFactory.getPowerQuality().getVoltage((obisCode.getC()-12)/20); 
//                    }
//                    // Power factor
//                    else if ((obisCode.getC() == 13) || (obisCode.getC() == 33) || (obisCode.getC() == 53) || (obisCode.getC() == 73)) {
//                       quantity = nexusCommandFactory.getPowerQuality().getPowerFactor((obisCode.getC()-13)/20); 
//                    }
//                    // kW load
//                    else if ((obisCode.getC() == 1) || (obisCode.getC() == 21) || (obisCode.getC() == 41) || (obisCode.getC() == 61)) {
//                       quantity = nexusCommandFactory.getPowerQuality().getKwLoad((obisCode.getC()-1)/20); 
//                    }
//                    // Frequency
//                    else if ((obisCode.getC() == 14) || (obisCode.getC() == 34) || (obisCode.getC() == 54) || (obisCode.getC() == 74)) {
//                       quantity = nexusCommandFactory.getPowerQuality().getFrequency((obisCode.getC()-14)/20); 
//                    }
//                    else if (obisCode.getC() == 81) {
//                        if (obisCode.getE() == 40) 
//                            quantity = nexusCommandFactory.getPowerQuality().getPhaseAngle(0);
//                        if (obisCode.getE() == 51) 
//                            quantity = nexusCommandFactory.getPowerQuality().getPhaseAngle(1);
//                        if (obisCode.getE() == 62) 
//                            quantity = nexusCommandFactory.getPowerQuality().getPhaseAngle(2);
//                    }
//                    
//                    if (quantity != null)
//                        registerValue = new RegisterValue(obisCode,quantity);
//                    else
//                        throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
//                }
//                else return new RegisterInfo(obisCode.getDescription());
//            }
//        }
        
        if ((read) && (registerValue != null))
            return registerValue;
        else
        	return null;
//            throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
    }
    
   
 
}