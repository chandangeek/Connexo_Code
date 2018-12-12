package com.energyict.protocolimpl.dlms.g3.registers.mapping;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.cosem.ConcentratorSetup;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.utils.ProtocolTools;
import org.json.JSONException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by iulian on 3/25/2017.
 */
public class ConcentratorSetupAttributesMapping extends RegisterMapping {

    // approved arrays for this mapping
    List<Integer> attributes = Arrays.asList(2,3,4,41,42,5);


    public ConcentratorSetupAttributesMapping(CosemObjectFactory cosemObjectFactory) {
        super(cosemObjectFactory);
    }

    public ObisCode getBaseObisCode(ObisCode obisCode) {                 //Set the F-field to 255
        return ProtocolTools.setObisCodeField(obisCode, 5, (byte) 255);
    }

    @Override
    public boolean canRead(ObisCode obisCode) {
        return ConcentratorSetup.NEW_LOGICAL_NAME.equalsIgnoreBillingField(obisCode) &&
                (attributes.contains(obisCode.getF()));
    }

    @Override
    protected RegisterValue doReadRegister(ObisCode obisCode) throws IOException {
        final ConcentratorSetup concentratorSetup = getCosemObjectFactory().getConcentratorSetup(getBaseObisCode(obisCode));
        return parse(obisCode, readAttribute(obisCode, concentratorSetup));
    }

    protected AbstractDataType readAttribute(final ObisCode obisCode, ConcentratorSetup concentratorSetup) throws IOException {
        switch (obisCode.getF()){
            case 2: // is_active
                return concentratorSetup.isActive();

            case 3: // max_concurrent_sessions
                return concentratorSetup.getMaxConcurrentSessions();

            case 4: // meter info as JSON
            case 41: // meter info as serial
            case 42: // meter info as MAC
                return concentratorSetup.getMeterInfoArray();

            case 5: // protocol_event_log_level
                return concentratorSetup.getProtocolEventLogLevel();
        }

        throw new NoSuchRegisterException("ConcentratorSetupAttributeMapping attribute [" + obisCode.getF() + "] not supported!");
    }

    @Override
    public RegisterValue parse(ObisCode obisCode, AbstractDataType abstractDataType) throws IOException {
        switch (obisCode.getF()){
            case 2: // is_active
                boolean state =  abstractDataType.getBooleanObject().getState();
                return new RegisterValue(obisCode,  new Quantity(state?1:0, Unit.getUndefined()));


            case 3: // max_concurrent_sessions
                if (abstractDataType.isUnsigned16()) {
                    int sessions = abstractDataType.getUnsigned16().intValue();
                    return new RegisterValue(obisCode, new Quantity(sessions, Unit.getUndefined()));
                } else {
                    return new RegisterValue(obisCode, abstractDataType.toString());
                }

            case 4: // meter info as JSON
                try {
                    Array meterInfo = abstractDataType.getArray();
                    String json = ConcentratorSetup.buildMeterInfoJSON(meterInfo);
                    Logger.getAnonymousLogger().info(json);
                    if (json.length()<4000) { // TODO fix the register storage
                        return new RegisterValue(obisCode, json);
                    } else {
                        return new RegisterValue(obisCode, "ERROR, JSON longer than 4000 characters and cannot be stored into the database!" + json.substring(0, 3900) + "...");
                    }
                } catch (JSONException e) {
                    return new RegisterValue(obisCode, abstractDataType.toString());
                }

            case 41: // meter info as serial number
                return new RegisterValue(obisCode, ConcentratorSetup.buildMeterInfoAsSerial(abstractDataType.getArray()));

            case 42: // meter info as MAC
                return new RegisterValue(obisCode, ConcentratorSetup.buildMeterInfoAsMAC(abstractDataType.getArray()));


            case 5: // protocol_event_log_level
                TypeEnum logLevel = abstractDataType.getTypeEnum();
                String logLevelText = ConcentratorSetup.getLogLevelDescription(logLevel);
                return new RegisterValue(obisCode, logLevelText);
        }
        throw new NoSuchRegisterException("ConcentratorSetupAttributeMapping attribute [" + obisCode.getF() + "] not supported!");
    }


}
