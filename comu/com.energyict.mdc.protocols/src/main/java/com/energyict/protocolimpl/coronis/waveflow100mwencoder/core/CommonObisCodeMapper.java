package com.energyict.protocolimpl.coronis.waveflow100mwencoder.core;

import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.coronis.waveflow100mwencoder.core.WaveFlow100mW.MeterProtocolType;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class CommonObisCodeMapper {

    static Map<ObisCode,String> registerMaps = new HashMap<ObisCode, String>();

    static {
        registerMaps.put(ObisCode.fromString("0.0.96.6.0.255"), "Available battery power in %"); // generic header
        registerMaps.put(ObisCode.fromString("0.0.96.6.1.255"), "Port A encoder info");
        registerMaps.put(ObisCode.fromString("0.0.96.6.2.255"), "Port B encoder info");
        registerMaps.put(ObisCode.fromString("0.0.96.6.3.255"), "Application status"); // generic header
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

        registerMaps.put(ObisCode.fromString("0.0.96.6.14.255"), "Operation mode"); // generic header
        registerMaps.put(ObisCode.fromString("0.0.96.6.15.255"), "RSSI level end node"); // generic header

        registerMaps.put(ObisCode.fromString("0.0.96.6.16.255"), "Alarm congiguration");
        registerMaps.put(ObisCode.fromString("0.0.96.6.17.255"), "Sampling period");

        registerMaps.put(ObisCode.fromString("0.0.96.6.18.255"), "Voltage V1");
        registerMaps.put(ObisCode.fromString("0.0.96.6.19.255"), "Voltage V2");
        registerMaps.put(ObisCode.fromString("0.0.96.6.20.255"), "Voltage VPAL");

        // specific watermeter registers start with E-field 50

        registerMaps.put(ObisCode.fromString("0.0.96.6.100.255"), "Generic header info");


        // special obis code to control the waveflow RTC
        registerMaps.put(ObisCode.fromString("0.0.96.6.200.255"), "set the waveflow clock");

    }

    private WaveFlow100mW waveFlow100mW;

    /**
     * Creates a new instance of ObisCodeMapper
     */
    public CommonObisCodeMapper(final WaveFlow100mW waveFlow100mW) {
        this.waveFlow100mW=waveFlow100mW;
    }

    final public String getRegisterExtendedLogging() {

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

    public RegisterValue getRegisterValue(ObisCode obisCode) throws NoSuchRegisterException {
        try {
            if (obisCode.equals(ObisCode.fromString("0.0.96.6.0.255"))) {
                // battery counter
                //return new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(waveFlow100mW.getParameterFactory().readBatteryLifeDurationCounter().remainingBatteryLife()), Unit.get(BaseUnit.PERCENT)),new Date());
                return new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(waveFlow100mW.getCachedGenericHeader().remainingBatteryLife()), Unit.get(BaseUnit.PERCENT)),new Date());
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
//	    		return new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(waveFlow100mW.getParameterFactory().readApplicationStatus()), Unit.get("")),new Date());
                return new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(waveFlow100mW.getCachedGenericHeader().getApplicationStatus()), Unit.get("")),new Date());
            }
            else if (obisCode.equals(ObisCode.fromString("0.0.96.6.4.255"))) {

                if (waveFlow100mW.getMeterProtocolType()==MeterProtocolType.SM150E)	{
                    // leakage detection status
                    return new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(((EncoderGenericHeader)waveFlow100mW.getCachedGenericHeader()).getLeakageDetectionStatus()), Unit.get("")),new Date());
                }
                else if (waveFlow100mW.getMeterProtocolType()==MeterProtocolType.ECHODIS) {
                    throw new NoSuchRegisterException("Register with obis code ["+obisCode+"] does not exist!");
                }

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
            else if (obisCode.equals(ObisCode.fromString("0.0.96.6.14.255"))) {
                // Operation mode
                //return new RegisterValue(obisCode, null, null, null, null, new Date(), 0, "Operating mode: "+WaveflowProtocolUtils.toHexString(waveFlow100mW.getParameterFactory().readOperatingMode()));
//	    		return new RegisterValue(obisCode, null, null, null, null, new Date(), 0, "Operating mode: "+WaveflowProtocolUtils.toHexString(waveFlow100mW.getCachedGenericHeader().readOperatingMode()));
                return new RegisterValue(obisCode,new Quantity(new BigDecimal(waveFlow100mW.getCachedGenericHeader().getOperatingMode()), Unit.get("")),new Date());
            }
            else if (obisCode.equals(ObisCode.fromString("0.0.96.6.15.255"))) {
                // QOS (RSSI) level
                return new RegisterValue(obisCode, new Quantity(waveFlow100mW.getCachedGenericHeader().getRssiLevel(), Unit.get("")), new Date());
            } else if (obisCode.equals(ObisCode.fromString("0.0.96.6.16.255"))) {
                // Alarm configuration
                return new RegisterValue(obisCode,new Quantity(new BigDecimal(waveFlow100mW.getParameterFactory().readAlarmConfiguration()), Unit.get("")),new Date());
            }
            else if (obisCode.equals(ObisCode.fromString("0.0.96.6.17.255"))) {
                // Sampling period
                return new RegisterValue(obisCode,new Quantity(new BigDecimal(waveFlow100mW.getParameterFactory().readSamplingPeriod()), Unit.get("")),new Date());
            } else if (obisCode.equals(ObisCode.fromString("0.0.96.6.18.255"))) {
                // Voltage V1
                return new RegisterValue(obisCode, new Quantity(waveFlow100mW.getRadioCommandFactory().readVoltageV1(), Unit.get(BaseUnit.VOLT)), new Date());
            } else if (obisCode.equals(ObisCode.fromString("0.0.96.6.19.255"))) {
                // Voltage V2
                return new RegisterValue(obisCode, new Quantity(waveFlow100mW.getRadioCommandFactory().readVoltageV2(), Unit.get(BaseUnit.VOLT)), new Date());
            } else if (obisCode.equals(ObisCode.fromString("0.0.96.6.20.255"))) {
                // Voltage VPAL
                return new RegisterValue(obisCode, new Quantity(waveFlow100mW.getRadioCommandFactory().readVoltageVPAL(), Unit.get(BaseUnit.VOLT)), new Date());
            } else if (obisCode.equals(ObisCode.fromString("0.0.96.6.100.255"))) {
                // encoder internal data
                if (waveFlow100mW.getCachedGenericHeader()==null) {
                    waveFlow100mW.getRadioCommandFactory().readEncoderCurrentReading();
                }
                return new RegisterValue(obisCode, null, null, null, null, new Date(), 0, waveFlow100mW.getCachedGenericHeader().toString());
            }
            else if (obisCode.equals(ObisCode.fromString("0.0.96.6.200.255"))) {
                try {
                    waveFlow100mW.setWaveFlowTime();
                    return new RegisterValue(obisCode, null, null, null, null, new Date(), 0, "WaveFlow RTC set successfull.");
                }
                catch(IOException e) {
                    return new RegisterValue(obisCode, null, null, null, null, new Date(), 0, "Error setting the WaveFlow RTC ["+e.getMessage()+"]");
                }
            }

            throw new NoSuchRegisterException("Register with obis code ["+obisCode+"] does not exist!");


        } catch (IOException e) {

            throw new NoSuchRegisterException("Register with obis code ["+obisCode+"] has an error ["+e.getMessage()+"]!");

        }

    }

}
