package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;

import com.energyict.cbo.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;

public class ObisCodeMapper {
	
	static Map<ObisCode,String> registerMaps = new HashMap<ObisCode, String>();
	
	static {
		registerMaps.put(ObisCode.fromString("0.0.96.6.0.255"), "Available battery power in %");
		registerMaps.put(ObisCode.fromString("0.0.96.6.1.255"), "Port A encoder info");
		registerMaps.put(ObisCode.fromString("0.0.96.6.2.255"), "Port B encoder info");
		registerMaps.put(ObisCode.fromString("0.0.96.6.3.255"), "Application status");
		registerMaps.put(ObisCode.fromString("0.0.96.6.4.255"), "Leakage detection status");

		registerMaps.put(ObisCode.fromString("0.0.96.6.5.255"), "Port A backflow detection date");
		registerMaps.put(ObisCode.fromString("0.0.96.6.6.255"), "Port B backflow detection date");
		registerMaps.put(ObisCode.fromString("0.0.96.6.7.255"), "Port A backflow detection flags");
		registerMaps.put(ObisCode.fromString("0.0.96.6.8.255"), "Port B backflow detection flags");
		
		registerMaps.put(ObisCode.fromString("0.0.96.6.9.255"), "Port A communication error detection date");
		registerMaps.put(ObisCode.fromString("0.0.96.6.10.255"), "Port B communication error detection date");
		registerMaps.put(ObisCode.fromString("0.0.96.6.11.255"), "Port A communication error reading date");
		registerMaps.put(ObisCode.fromString("0.0.96.6.12.255"), "Port B communication error reading date");

		registerMaps.put(ObisCode.fromString("0.0.96.6.13.255"), "Battery life end date");
		
		registerMaps.put(ObisCode.fromString("8.1.1.0.0.255"), "Port A encoder current index");
		registerMaps.put(ObisCode.fromString("8.2.1.0.0.255"), "Port B encoder current index");
		
		
		// specific severntrent registers
		registerMaps.put(ObisCode.fromString("0.0.96.6.50.255"), "Port B communication error reading date");
		
		
		
	}
	
	private WaveFlow100mW waveFlow100mW;
	
    /** Creates a new instance of ObisCodeMapper */
    public ObisCodeMapper(final WaveFlow100mW waveFlow100mW) {
        this.waveFlow100mW=waveFlow100mW;
    }
    
    final String getRegisterExtendedLogging() {
    	
    	StringBuilder strBuilder=new StringBuilder();
    	
    	Iterator<Entry<ObisCode,String>> it = registerMaps.entrySet().iterator();
    	while(it.hasNext()) {
    		Entry<ObisCode,String> o = it.next();
    		waveFlow100mW.getLogger().info(o.getKey().toString()+", "+o.getValue());
    	}
    	
    	return strBuilder.toString();
    }
    
    public static RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
    	String info = registerMaps.get(obisCode);
    	if (info !=null) {
    		return new RegisterInfo(info);
    	}
    	else {
    		throw new NoSuchRegisterException("Register with obis code ["+obisCode+"] does not exist!");
    	}
    }
    
    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
    	if (obisCode.equals(ObisCode.fromString("0.0.96.6.0.255"))) {
    		// battery counter
    		return new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(waveFlow100mW.getParameterFactory().readBatteryLifeDurationCounter().remainingBatteryLife()), Unit.get(BaseUnit.PERCENT)),new Date());
   		}
    	else if (obisCode.equals(ObisCode.fromString("0.0.96.6.1.255"))) {
    		// port A encoder info
    		return new RegisterValue(obisCode, null, null, null, null, new Date(), 0, waveFlow100mW.getParameterFactory().readEncoderModel(0).getEncoderModelInfo().toString());
    	}
    	else if (obisCode.equals(ObisCode.fromString("0.0.96.6.2.255"))) {
    		// port B encoder info
    		return new RegisterValue(obisCode, null, null, null, null, new Date(), 0, waveFlow100mW.getParameterFactory().readEncoderModel(1).getEncoderModelInfo().toString());
    	}
    	else if (obisCode.equals(ObisCode.fromString("0.0.96.6.3.255"))) {
    		// application status
    		return new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(waveFlow100mW.getParameterFactory().readApplicationStatus()), Unit.get("")),new Date());
    	}
    	else if (obisCode.equals(ObisCode.fromString("0.0.96.6.4.255"))) {
    		// leakage detection status
    		return new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(waveFlow100mW.getCachedEncoderGenericHeader().getLeakageDetectionStatus()), Unit.get("")),new Date());
    	}
    	else if ((obisCode.equals(ObisCode.fromString("0.0.96.6.5.255"))) || (obisCode.equals(ObisCode.fromString("0.0.96.6.6.255")))) {
    		// Backflow detection date
    		int portId = obisCode.getE()-5;
    		Date date = waveFlow100mW.getParameterFactory().readBackflowDetectionDate(portId);
    		if (date==null) {
    			return new RegisterValue(obisCode, null, null, null, null, new Date(), 0, "No backflow detection date for port "+(portId==0?"A":"B"));
    		}
    		else {
    			return new RegisterValue(obisCode, null, date, null, null, new Date(), 0, "Backflow detection date for port "+(portId==0?"A":"B")+", "+date);
    		}
    	}
    	else if ((obisCode.equals(ObisCode.fromString("0.0.96.6.7.255"))) || (obisCode.equals(ObisCode.fromString("0.0.96.6.8.255")))) {
    		// Backflow detection flags
    		int portId = obisCode.getE()-7;
    		return new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(waveFlow100mW.getParameterFactory().readBackflowDetectionFlags(portId)), Unit.get("")),new Date());
    	}
    	else if ((obisCode.equals(ObisCode.fromString("0.0.96.6.9.255"))) || (obisCode.equals(ObisCode.fromString("0.0.96.6.10.255")))) {
    		// Communication error detection date
    		int portId = obisCode.getE()-9;
    		Date date = waveFlow100mW.getParameterFactory().readCommunicationErrorDetectionDate(portId);
    		if (date==null) {
    			return new RegisterValue(obisCode, null, null, null, null, new Date(), 0, "No communication error detection date for port "+(portId==0?"A":"B"));
    		}
    		else {
    			return new RegisterValue(obisCode, null, date, null, null, new Date(), 0, "Communication error detection date for port "+(portId==0?"A":"B")+", "+date);
    		}
    	}
    	else if ((obisCode.equals(ObisCode.fromString("0.0.96.6.11.255"))) || (obisCode.equals(ObisCode.fromString("0.0.96.6.12.255")))) {
    		// Communication error detection date
    		int portId = obisCode.getE()-11;
    		Date date = waveFlow100mW.getParameterFactory().readCommunicationErrorReadingDate(portId);
    		if (date==null) {
    			return new RegisterValue(obisCode, null, null, null, null, new Date(), 0, "No communication error reading date for port "+(portId==0?"A":"B"));
    		}
    		else {
    			return new RegisterValue(obisCode, null, date, null, null, new Date(), 0, "Communication error reading date for port "+(portId==0?"A":"B")+", "+date);
    		}
    	}
    	else if (obisCode.equals(ObisCode.fromString("0.0.96.6.13.255"))) {
    		// Battery life end date
    		Date date = waveFlow100mW.getParameterFactory().readBatteryLifeDateEnd();
    		if (date==null) {
    			return new RegisterValue(obisCode, null, null, null, null, new Date(), 0, "No battery life end date");
    		}
    		else {
    			return new RegisterValue(obisCode, null, date, null, null, new Date(), 0, "Battery life end date");
    		}
    	}
    	else if ((obisCode.equals(ObisCode.fromString("8.1.1.0.0.255"))) || (obisCode.equals(ObisCode.fromString("8.2.1.0.0.255")))) { // Port A or B
    		EncoderInternalData encoderInternalData = waveFlow100mW.getRadioCommandFactory().readEncoderInternalData().getEncoderInternalDatas()[obisCode.getB()-1];
    		Unit unit = encoderInternalData.getEncoderUnitType().toUnit();
    		BigDecimal bd = new BigDecimal(encoderInternalData.getCurrentIndex()*100+encoderInternalData.getLastPart());
    		bd = bd.movePointLeft(10-encoderInternalData.getDecimalPosition()); 
    		return new RegisterValue(obisCode,new Quantity(bd, unit),new Date());
    	}
    	else if ((obisCode.equals(ObisCode.fromString("0.0.96.6.50.255"))) || (obisCode.equals(ObisCode.fromString("0.0.96.6.51.255")))) {
    		// read status
    		int portId = obisCode.getE()-50;
    		EncoderInternalData o = waveFlow100mW.getRadioCommandFactory().readEncoderInternalData().getEncoderInternalDatas()[portId];
    		if (o==null) {
    			return new RegisterValue(obisCode, null, null, null, null, new Date(), 0, "No encoder connected to port "+(portId==0?"A":"B"));
    		}
    		else {
    			return new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(o.getStatus()), Unit.get("")),new Date());
    		}
    	}
    	else if ((obisCode.equals(ObisCode.fromString("0.0.96.6.52.255"))) || (obisCode.equals(ObisCode.fromString("0.0.96.6.53.255")))) {
    		// dry count
    		int portId = obisCode.getE()-52;
    		EncoderInternalData o = waveFlow100mW.getRadioCommandFactory().readEncoderInternalData().getEncoderInternalDatas()[portId];
    		if (o==null) {
    			return new RegisterValue(obisCode, null, null, null, null, new Date(), 0, "No encoder connected to port "+(portId==0?"A":"B"));
    		}
    		else {
    			return new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(o.getDryCount()), Unit.get("")),new Date());
    		}
    	}
    	else if ((obisCode.equals(ObisCode.fromString("0.0.96.6.54.255"))) || (obisCode.equals(ObisCode.fromString("0.0.96.6.55.255")))) {
    		// leak count
    		int portId = obisCode.getE()-54;
    		EncoderInternalData o = waveFlow100mW.getRadioCommandFactory().readEncoderInternalData().getEncoderInternalDatas()[portId];
    		if (o==null) {
    			return new RegisterValue(obisCode, null, null, null, null, new Date(), 0, "No encoder connected to port "+(portId==0?"A":"B"));
    		}
    		else {
    			return new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(o.getLeakCount()), Unit.get("")),new Date());
    		}
    	}
    	else if ((obisCode.equals(ObisCode.fromString("0.0.96.6.56.255"))) || (obisCode.equals(ObisCode.fromString("0.0.96.6.57.255")))) {
    		// no flow count
    		int portId = obisCode.getE()-56;
    		EncoderInternalData o = waveFlow100mW.getRadioCommandFactory().readEncoderInternalData().getEncoderInternalDatas()[portId];
    		if (o==null) {
    			return new RegisterValue(obisCode, null, null, null, null, new Date(), 0, "No encoder connected to port "+(portId==0?"A":"B"));
    		}
    		else {
    			return new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(o.getNoflowCount()), Unit.get("")),new Date());
    		}
    	}
    	else if ((obisCode.equals(ObisCode.fromString("0.0.96.6.58.255"))) || (obisCode.equals(ObisCode.fromString("0.0.96.6.59.255")))) {
    		// no tamper count
    		int portId = obisCode.getE()-58;
    		EncoderInternalData o = waveFlow100mW.getRadioCommandFactory().readEncoderInternalData().getEncoderInternalDatas()[portId];
    		if (o==null) {
    			return new RegisterValue(obisCode, null, null, null, null, new Date(), 0, "No encoder connected to port "+(portId==0?"A":"B"));
    		}
    		else {
    			return new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(o.getTamperCount()), Unit.get("")),new Date());
    		}
    	}
    	else if ((obisCode.equals(ObisCode.fromString("0.0.96.6.60.255"))) || (obisCode.equals(ObisCode.fromString("0.0.96.6.61.255")))) {
    		// totalizer serial 
    		int portId = obisCode.getE()-60;
    		EncoderInternalData o = waveFlow100mW.getRadioCommandFactory().readEncoderInternalData().getEncoderInternalDatas()[portId];
    		if (o==null) {
    			return new RegisterValue(obisCode, null, null, null, null, new Date(), 0, "No encoder connected to port "+(portId==0?"A":"B"));
    		}
    		else {
    			return new RegisterValue(obisCode, null, null, null, null, new Date(), 0, "Totalizer serial="+o.getTotalizerSerial());
    		}
    	}
    	else if ((obisCode.equals(ObisCode.fromString("0.0.96.6.62.255"))) || (obisCode.equals(ObisCode.fromString("0.0.96.6.63.255")))) {
    		// transducer serial 
    		int portId = obisCode.getE()-62;
    		EncoderInternalData o = waveFlow100mW.getRadioCommandFactory().readEncoderInternalData().getEncoderInternalDatas()[portId];
    		if (o==null) {
    			return new RegisterValue(obisCode, null, null, null, null, new Date(), 0, "No encoder connected to port "+(portId==0?"A":"B"));
    		}
    		else {
    			return new RegisterValue(obisCode, null, null, null, null, new Date(), 0, "Tranducer serial="+o.getTransducerSerial());
    		}
    	}
    	else if ((obisCode.equals(ObisCode.fromString("0.0.96.6.64.255"))) || (obisCode.equals(ObisCode.fromString("0.0.96.6.65.255")))) {
    		// user id
    		int portId = obisCode.getE()-64;
    		EncoderInternalData o = waveFlow100mW.getRadioCommandFactory().readEncoderInternalData().getEncoderInternalDatas()[portId];
    		if (o==null) {
    			return new RegisterValue(obisCode, null, null, null, null, new Date(), 0, "No encoder connected to port "+(portId==0?"A":"B"));
    		}
    		else {
    			return new RegisterValue(obisCode, null, null, null, null, new Date(), 0, "User id="+o.getUserId());
    		}
    	}
    	else if ((obisCode.equals(ObisCode.fromString("0.0.96.6.66.255"))) || (obisCode.equals(ObisCode.fromString("0.0.96.6.67.255")))) {
    		// version
    		int portId = obisCode.getE()-66;
    		EncoderInternalData o = waveFlow100mW.getRadioCommandFactory().readEncoderInternalData().getEncoderInternalDatas()[portId];
    		if (o==null) {
    			return new RegisterValue(obisCode, null, null, null, null, new Date(), 0, "No encoder connected to port "+(portId==0?"A":"B"));
    		}
    		else {
    			return new RegisterValue(obisCode, null, null, null, null, new Date(), 0, "Version="+Utils.toHexString(o.getVersion()));
    		}
    	}
    	else if ((obisCode.equals(ObisCode.fromString("0.0.96.6.68.255"))) || (obisCode.equals(ObisCode.fromString("0.0.96.6.69.255")))) {
    		// encoder internal data
    		int portId = obisCode.getE()-68;
    		EncoderInternalData o = waveFlow100mW.getRadioCommandFactory().readEncoderInternalData().getEncoderInternalDatas()[portId];
    		if (o==null) {
    			return new RegisterValue(obisCode, null, null, null, null, new Date(), 0, "No encoder connected to port "+(portId==0?"A":"B"));
    		}
    		else {
    			return new RegisterValue(obisCode, null, null, null, null, new Date(), 0, o.getEncoderInternalData());
    		}
    	}
    	
		throw new NoSuchRegisterException("Register with obis code ["+obisCode+"] does not exist!");

    }
	
}
