package com.energyict.protocolimplv2.dlms.idis.am540.registers;

import com.energyict.cbo.Unit;
import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.ComposedCosemObject;
import com.energyict.mdc.meterdata.CollectedRegister;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdw.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.dlms.g3.registers.G3Mapping;
import com.energyict.protocolimplv2.common.composedobjects.ComposedObject;
import com.energyict.protocolimplv2.common.composedobjects.ComposedRegister;
import com.energyict.protocolimplv2.dlms.idis.am130.registers.AM130RegisterFactory;
import com.energyict.protocolimplv2.dlms.idis.am540.AM540;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * RegisterFactory created for the AM540 protocol <br/>
 * Note: extends from AM130RegisterFactory - which enables readout of all common elements -
 * this class adds readout of the various PLC objects.
 *
 * @author sva
 * @since 11/08/2015 - 15:46
 */
public class AM540RegisterFactory extends AM130RegisterFactory {

    private AM540PLCRegisterMapper plcRegisterMapper;

    public AM540RegisterFactory(AM540 am540) {
        super(am540);
    }

    @Override
    protected void addComposedObjectToComposedRegisterMap(Map<ObisCode, ComposedObject> composedObjectMap, List<DLMSAttribute> dlmsAttributes, OfflineRegister register) {
        G3Mapping g3Mapping = getPLCRegisterMapper().getG3Mapping(register.getObisCode());
        if (g3Mapping != null) {
            ComposedRegister composedRegister = new ComposedRegister();
            int[] attributeNumbers = g3Mapping.getAttributeNumbers();
            for (int index = 0; index < attributeNumbers.length; index++) {
                int attributeNumber = attributeNumbers[index];
                DLMSAttribute dlmsAttribute = new DLMSAttribute(g3Mapping.getBaseObisCode(), attributeNumber, g3Mapping.getDLMSClassId());
                dlmsAttributes.add(dlmsAttribute);

                //If the mapping contains more than 1 attribute, the order is always value, unit, captureTime
                if (index == 0) {
                    composedRegister.setRegisterValue(dlmsAttribute);
                } else if (index == 1) {
                    composedRegister.setRegisterUnit(dlmsAttribute);
                } else if (index == 2) {
                    composedRegister.setRegisterCaptureTime(dlmsAttribute);
                }
            }
            composedObjectMap.put(register.getObisCode(), composedRegister);
        } else {
            super.addComposedObjectToComposedRegisterMap(composedObjectMap, dlmsAttributes, register);
        }
    }

    @Override
    protected CollectedRegister createCollectedRegisterFor(OfflineRegister offlineRegister, Map<ObisCode, ComposedObject> composedObjectMap, ComposedCosemObject composedCosemObject) {
        ComposedObject composedObject = composedObjectMap.get(offlineRegister.getObisCode());
        G3Mapping g3Mapping = getPLCRegisterMapper().getG3Mapping(offlineRegister.getObisCode());
        if (g3Mapping != null) {
            if (composedObject == null) {
                return createFailureCollectedRegister(offlineRegister, ResultType.NotSupported);    // Should never occur, but safety measure
            } else {
                ComposedRegister composedRegister = (ComposedRegister) composedObject;
                try {
                    Unit unit = null;
                    if (composedRegister.getRegisterUnitAttribute() != null) {
                        unit = new ScalerUnit(composedCosemObject.getAttribute(composedRegister.getRegisterUnitAttribute())).getEisUnit();
                    }
                    Date captureTime = null;
                    if (composedRegister.getRegisterCaptureTime() != null) {
                        AbstractDataType captureTimeOctetString = composedCosemObject.getAttribute(composedRegister.getRegisterCaptureTime());
                        captureTime = captureTimeOctetString.getOctetString().getDateTime(getMeterProtocol().getDlmsSession().getTimeZone()).getValue().getTime();
                    }

                    AbstractDataType attributeValue = composedCosemObject.getAttribute(composedRegister.getRegisterValueAttribute());
                    RegisterValue registerValue = g3Mapping.parse(attributeValue, unit, captureTime);
                    return createCollectedRegister(registerValue, offlineRegister);
                } catch (IOException e) {
                    if (IOExceptionHandler.isUnexpectedResponse(e, getMeterProtocol().getDlmsSession())) {
                        if (IOExceptionHandler.isNotSupportedDataAccessResultException(e)) {
                            return createFailureCollectedRegister(offlineRegister, ResultType.NotSupported);
                        } else {
                            return createFailureCollectedRegister(offlineRegister, ResultType.InCompatible, e.getMessage());
                        }
                    } // else a proper connectionCommunicationException is thrown
                    return null;
                }
            }
        } else {
            return super.createCollectedRegisterFor(offlineRegister, composedObjectMap, composedCosemObject);
        }
    }

    private AM540PLCRegisterMapper getPLCRegisterMapper() {
        if (plcRegisterMapper == null) {
            plcRegisterMapper = new AM540PLCRegisterMapper(getMeterProtocol().getDlmsSession());
        }
        return plcRegisterMapper;
    }
}