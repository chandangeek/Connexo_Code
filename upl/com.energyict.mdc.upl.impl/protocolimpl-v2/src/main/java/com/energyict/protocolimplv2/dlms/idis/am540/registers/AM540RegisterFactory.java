package com.energyict.protocolimplv2.dlms.idis.am540.registers;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.offline.OfflineRegister;

import com.energyict.cbo.Unit;
import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.ComposedCosemObject;
import com.energyict.dlms.cosem.G3NetworkManagement;
import com.energyict.dlms.cosem.ImageTransfer;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.dlms.g3.registers.G3Mapping;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.common.composedobjects.ComposedObject;
import com.energyict.protocolimplv2.common.composedobjects.ComposedRegister;
import com.energyict.protocolimplv2.dlms.idis.am130.registers.AM130RegisterFactory;
import com.energyict.protocolimplv2.dlms.idis.am540.AM540;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
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
    private static final ObisCode MULTICAST_FIRMWARE_UPGRADE_OBISCODE = ObisCode.fromString("0.0.44.0.128.255");
    private static final ObisCode MULTICAST_METER_PROGRESS = ProtocolTools.setObisCodeField(MULTICAST_FIRMWARE_UPGRADE_OBISCODE, 1, (byte) (-1 * ImageTransfer.ATTRIBUTE_UPGRADE_PROGRESS));

    public AM540RegisterFactory(AM540 am540, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(am540, collectedDataFactory, issueFactory);
    }

    @Override
    protected Boolean addComposedObjectToComposedRegisterMap(Map<ObisCode, ComposedObject> composedObjectMap, List<DLMSAttribute> dlmsAttributes, OfflineRegister register) {
        G3Mapping g3Mapping = getPLCRegisterMapper().getG3Mapping(register.getObisCode());
        if (g3Mapping != null) {
            ComposedRegister composedRegister = new ComposedRegister();
            int[] attributeNumbers = g3Mapping.getAttributeNumbers();

            if (dlmsAttributes.size() + attributeNumbers.length > BULK_REQUEST_ATTRIBUTE_LIMIT) {
                return null; //Don't add the new attributes, no more room
            }

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
            return true;
        } else {
            return super.addComposedObjectToComposedRegisterMap(composedObjectMap, dlmsAttributes, register);
        }
    }

    /**
     * Filter out the following registers:
     * - MBus devices (by serial number) that are not installed on the e-meter
     * - Obiscode 0.0.128.0.2.255, this register value will be filled in by executing the path request message, not by the register reader
     * - Obiscode 0.3.44.0.128.255, this register value will be filled in by executing the 'read DC multicast progress' message on the Beacon protocol
     */
    @Override
    protected List<CollectedRegister> filterOutAllInvalidRegistersFromList(List<OfflineRegister> offlineRegisters) {
        final List<CollectedRegister> invalidRegisters = super.filterOutAllInvalidRegistersFromList(offlineRegisters);

        Iterator<OfflineRegister> it = offlineRegisters.iterator();
        while (it.hasNext()) {
            OfflineRegister register = it.next();
            if (register.getObisCode().equals(G3NetworkManagement.getDefaultObisCode())) {
                invalidRegisters.add(createFailureCollectedRegister(register, ResultType.InCompatible, "Register with obiscode " + register.getObisCode() + " cannot be read out, use the path request message for this."));
                it.remove();
            }
            if (register.getObisCode().equals(MULTICAST_METER_PROGRESS)) {
                invalidRegisters.add(createFailureCollectedRegister(register, ResultType.InCompatible, "Register with obiscode " + register.getObisCode() + " cannot be read out, use the 'read DC multicast progress' message on the Beacon protocol for this."));
                it.remove();
            }
        }
        return invalidRegisters;
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
                    if (DLMSIOExceptionHandler.isUnexpectedResponse(e, getMeterProtocol().getDlmsSessionProperties().getRetries() + 1)) {
                        if (DLMSIOExceptionHandler.isNotSupportedDataAccessResultException(e)) {
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