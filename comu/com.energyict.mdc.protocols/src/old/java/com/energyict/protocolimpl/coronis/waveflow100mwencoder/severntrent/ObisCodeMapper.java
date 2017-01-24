package com.energyict.protocolimpl.coronis.waveflow100mwencoder.severntrent;

import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.coronis.core.WaveflowProtocolUtils;
import com.energyict.protocolimpl.coronis.waveflow100mwencoder.core.CommonObisCodeMapper;
import com.energyict.protocolimpl.coronis.waveflow100mwencoder.core.EncoderInternalData;
import com.energyict.protocolimpl.coronis.waveflow100mwencoder.core.WaveFlow100mW;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class ObisCodeMapper {

    static Map<ObisCode,String> registerMaps = new HashMap<ObisCode, String>();

    static {

        // get the common obis codes from the common obis code mapper

        // specific severntrent registers
        registerMaps.put(ObisCode.fromString("0.0.96.6.50.255"), "Port A encoder internal data status");
        registerMaps.put(ObisCode.fromString("0.0.96.6.51.255"), "Port B encoder internal data status");

        registerMaps.put(ObisCode.fromString("0.0.96.6.52.255"), "Port A encoder internal data dry count");
        registerMaps.put(ObisCode.fromString("0.0.96.6.53.255"), "Port B encoder internal data dry count");

        registerMaps.put(ObisCode.fromString("0.0.96.6.54.255"), "Port A encoder internal data leak count");
        registerMaps.put(ObisCode.fromString("0.0.96.6.55.255"), "Port B encoder internal data leak count");

        registerMaps.put(ObisCode.fromString("0.0.96.6.56.255"), "Port A encoder internal data no flow count");
        registerMaps.put(ObisCode.fromString("0.0.96.6.57.255"), "Port B encoder internal data no flow count");

        registerMaps.put(ObisCode.fromString("0.0.96.6.58.255"), "Port A encoder internal data tamper count");
        registerMaps.put(ObisCode.fromString("0.0.96.6.59.255"), "Port B encoder internal data tamper count");

        registerMaps.put(ObisCode.fromString("0.0.96.6.60.255"), "Port A encoder internal data totalizer serial");
        registerMaps.put(ObisCode.fromString("0.0.96.6.61.255"), "Port B encoder internal data totalizer serial");

        registerMaps.put(ObisCode.fromString("0.0.96.6.62.255"), "Port A encoder internal data transducer serial");
        registerMaps.put(ObisCode.fromString("0.0.96.6.63.255"), "Port B encoder internal data transducer serial");

        registerMaps.put(ObisCode.fromString("0.0.96.6.64.255"), "Port A encoder internal data user id");
        registerMaps.put(ObisCode.fromString("0.0.96.6.65.255"), "Port B encoder internal data user id");

        registerMaps.put(ObisCode.fromString("0.0.96.6.66.255"), "Port A encoder internal data version");
        registerMaps.put(ObisCode.fromString("0.0.96.6.67.255"), "Port B encoder internal data version");

        registerMaps.put(ObisCode.fromString("0.0.96.6.68.255"), "Port A encoder internal data raw string");
        registerMaps.put(ObisCode.fromString("0.0.96.6.69.255"), "Port B encoder internal data raw string");

        registerMaps.put(ObisCode.fromString("8.1.1.0.0.255"), "Port A encoder current index");
        registerMaps.put(ObisCode.fromString("8.2.1.0.0.255"), "Port B encoder current index");


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

        strBuilder.append(waveFlow100mW.getCommonObisCodeMapper().getRegisterExtendedLogging());

        return strBuilder.toString();
    }

    public static RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
        String info = registerMaps.get(obisCode);
        if (info !=null) {
            return new RegisterInfo(info);
        }
        else {
            return CommonObisCodeMapper.getRegisterInfo(obisCode);
        }
    }

    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
        try {
            if ((obisCode.equals(ObisCode.fromString("0.0.96.6.50.255"))) || (obisCode.equals(ObisCode.fromString("0.0.96.6.51.255")))) {
                // read status
                int portId = obisCode.getE()-50;
                EncoderInternalData o = (EncoderInternalData)waveFlow100mW.readInternalDatas()[portId];
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
                EncoderInternalData o = (EncoderInternalData)waveFlow100mW.readInternalDatas()[portId];
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
                EncoderInternalData o = (EncoderInternalData)waveFlow100mW.readInternalDatas()[portId];
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
                EncoderInternalData o = (EncoderInternalData)waveFlow100mW.readInternalDatas()[portId];
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
                EncoderInternalData o = (EncoderInternalData)waveFlow100mW.readInternalDatas()[portId];
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
                EncoderInternalData o = (EncoderInternalData)waveFlow100mW.readInternalDatas()[portId];
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
                EncoderInternalData o = (EncoderInternalData)waveFlow100mW.readInternalDatas()[portId];
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
                EncoderInternalData o = (EncoderInternalData)waveFlow100mW.readInternalDatas()[portId];
                if (o==null) {
                    throw new NoSuchRegisterException("No encoder connected to port " + (portId == 0 ? "A" : "B"));
                } else {
                    return new RegisterValue(obisCode, null, null, null, null, new Date(), 0, o.getSerialNumber());
                }
            }
            else if ((obisCode.equals(ObisCode.fromString("0.0.96.6.66.255"))) || (obisCode.equals(ObisCode.fromString("0.0.96.6.67.255")))) {
                // version
                int portId = obisCode.getE()-66;
                EncoderInternalData o = (EncoderInternalData)waveFlow100mW.readInternalDatas()[portId];
                if (o==null) {
                    return new RegisterValue(obisCode, null, null, null, null, new Date(), 0, "No encoder connected to port "+(portId==0?"A":"B"));
                }
                else {
                    return new RegisterValue(obisCode, null, null, null, null, new Date(), 0, "Version="+WaveflowProtocolUtils.toHexString(o.getVersion()));
                }
            }
            else if ((obisCode.equals(ObisCode.fromString("0.0.96.6.68.255"))) || (obisCode.equals(ObisCode.fromString("0.0.96.6.69.255")))) {
                // encoder internal data
                int portId = obisCode.getE()-68;
                EncoderInternalData o = (EncoderInternalData)waveFlow100mW.readInternalDatas()[portId];
                if (o==null) {
                    return new RegisterValue(obisCode, null, null, null, null, new Date(), 0, "No encoder connected to port "+(portId==0?"A":"B"));
                }
                else {
                    return new RegisterValue(obisCode, null, null, null, null, new Date(), 0, o.getEncoderInternalData());
                }
            }
            else if ((obisCode.equals(ObisCode.fromString("8.1.1.0.0.255"))) || (obisCode.equals(ObisCode.fromString("8.2.1.0.0.255")))) { // Port A or B
                int portId = obisCode.getB()<=1?0:1;
                EncoderInternalData encoderInternalData = (EncoderInternalData)waveFlow100mW.readInternalDatas()[portId];
                if (encoderInternalData == null) {
                    throw new NoSuchRegisterException("Register with obis code ["+obisCode+"] does not exist. Probably port ["+(portId==0?"A":"B")+"] has no meter connected!");
                }
                else {
                    Unit unit = encoderInternalData.getEncoderUnitType().toUnit();
                    BigDecimal bd = new BigDecimal(encoderInternalData.getCurrentIndex()*100+encoderInternalData.getLastPart());
                    bd = bd.movePointLeft(10-encoderInternalData.getDecimalPosition());
                    return new RegisterValue(obisCode,new Quantity(bd, unit),new Date());
                }
            }

            else {
                return waveFlow100mW.getCommonObisCodeMapper().getRegisterValue(obisCode);
            }

        } catch (IOException e) {

            throw new NoSuchRegisterException("Register with obis code ["+obisCode+"] has an error ["+e.getMessage()+"]!");

        }

    }

}
