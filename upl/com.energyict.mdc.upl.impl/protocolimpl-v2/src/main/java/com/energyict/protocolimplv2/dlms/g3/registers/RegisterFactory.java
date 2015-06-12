package com.energyict.protocolimplv2.dlms.g3.registers;

import com.energyict.cbo.Unit;
import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.ComposedCosemObject;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.meterdata.CollectedRegister;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdc.meterdata.identifiers.RegisterIdentifier;
import com.energyict.mdw.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.dlms.g3.registers.G3Mapping;
import com.energyict.protocolimpl.dlms.g3.registers.G3RegisterMapper;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.common.composedobjects.ComposedRegister;
import com.energyict.protocolimplv2.identifiers.RegisterIdentifierById;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;

import java.io.IOException;
import java.util.*;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 9/06/2015 - 16:22
 */
public class RegisterFactory {

    private static final int BULK_RESQUEST_REGISTER_LIMIT = 5;  //The number of attributes in a bulk request should be smaller than 16. Note that 1, 2 or 3 attributes are read out for every register!

    private final DlmsSession dlmsSession;
    private G3RegisterMapper registerMapper;

    public RegisterFactory(DlmsSession dlmsSession) {
        this.dlmsSession = dlmsSession;
    }

    public DlmsSession getDlmsSession() {
        return dlmsSession;
    }

    public List<CollectedRegister> readRegisters(List<OfflineRegister> allRegisters) {
        List<OfflineRegister> subSet;
        List<CollectedRegister> result = new ArrayList<>();
        int count = 0;
        while (((count + 1) * BULK_RESQUEST_REGISTER_LIMIT) <= allRegisters.size()) {    //Read out in steps of x registers
            subSet = allRegisters.subList(count * BULK_RESQUEST_REGISTER_LIMIT, (count + 1) * BULK_RESQUEST_REGISTER_LIMIT);
            result.addAll(readSubSetOfRegisters(subSet));
            count++;
        }
        result.addAll(readSubSetOfRegisters(allRegisters.subList(count * BULK_RESQUEST_REGISTER_LIMIT, allRegisters.size()))); //Read out the remaining registers
        return result;
    }

    private List<CollectedRegister> readSubSetOfRegisters(List<OfflineRegister> registers) {

        //Map of attributes (value, unit, captureTime) per register
        Map<ObisCode, ComposedRegister> composedRegisterMap = new HashMap<>();

        //List of all attributes that need to be read out
        List<DLMSAttribute> dlmsAttributes = new ArrayList<>();

        for (OfflineRegister register : registers) {
            G3Mapping g3Mapping = getRegisterMapper().getG3Mapping(register.getObisCode());
            if (g3Mapping != null) {
                ComposedRegister composedRegister = new ComposedRegister();
                int[] attributeNumbers = g3Mapping.getAttributeNumbers();
                for (int index = 0; index < attributeNumbers.length; index++) {
                    int attributeNumber = attributeNumbers[index];
                    DLMSAttribute dlmsAttribute = new DLMSAttribute(register.getObisCode(), attributeNumber, g3Mapping.getDLMSClassId());
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
                composedRegisterMap.put(register.getObisCode(), composedRegister);
            }
        }

        ComposedCosemObject composedCosemObject = new ComposedCosemObject(this.getDlmsSession(), true, dlmsAttributes);
        List<CollectedRegister> result = new ArrayList<>();
        for (OfflineRegister offlineRegister : registers) {
            ComposedRegister composedRegister = composedRegisterMap.get(offlineRegister.getObisCode());
            G3Mapping g3Mapping = getRegisterMapper().getG3Mapping(offlineRegister.getObisCode());

            if (composedRegister == null || g3Mapping == null) {
                result.add(createFailureCollectedRegister(offlineRegister, ResultType.NotSupported));
            } else {
                try {
                    Unit unit = null;
                    if (composedRegister.getRegisterUnitAttribute() != null) {
                        unit = new ScalerUnit(composedCosemObject.getAttribute(composedRegister.getRegisterUnitAttribute())).getEisUnit();
                    }
                    Date captureTime = null;
                    if (composedRegister.getRegisterCaptureTime() != null) {
                        AbstractDataType captureTimeOctetString = composedCosemObject.getAttribute(composedRegister.getRegisterCaptureTime());
                        captureTime = captureTimeOctetString.getOctetString().getDateTime(getDlmsSession().getTimeZone()).getValue().getTime();
                    }

                    AbstractDataType attributeValue = composedCosemObject.getAttribute(composedRegister.getRegisterValueAttribute());
                    RegisterValue registerValue = g3Mapping.parse(attributeValue, unit, captureTime);
                    result.add(createCollectedRegister(registerValue, offlineRegister));
                } catch (IOException e) {
                    if (IOExceptionHandler.isUnexpectedResponse(e, getDlmsSession())) {
                        if (IOExceptionHandler.isNotSupportedDataAccessResultException(e)) {
                            result.add(createFailureCollectedRegister(offlineRegister, ResultType.NotSupported));
                        } else {
                            result.add(createFailureCollectedRegister(offlineRegister, ResultType.InCompatible, e.getMessage()));
                        }
                    } else {
                        throw MdcManager.getComServerExceptionFactory().createNumberOfRetriesReached(e, getDlmsSession().getProperties().getRetries() + 1);
                    }
                }
            }
        }

        return result;
    }

    private CollectedRegister createCollectedRegister(RegisterValue registerValue, OfflineRegister offlineRegister) {
        CollectedRegister deviceRegister = MdcManager.getCollectedDataFactory().createMaximumDemandCollectedRegister(getRegisterIdentifier(offlineRegister));
        deviceRegister.setCollectedData(registerValue.getQuantity(), registerValue.getText());
        deviceRegister.setCollectedTimeStamps(registerValue.getReadTime(), registerValue.getFromTime(), registerValue.getToTime(), registerValue.getEventTime());
        return deviceRegister;
    }

    private CollectedRegister createFailureCollectedRegister(OfflineRegister register, ResultType resultType, Object... errorMessage) {
        CollectedRegister collectedRegister = MdcManager.getCollectedDataFactory().createDefaultCollectedRegister(getRegisterIdentifier(register));
        if (resultType == ResultType.InCompatible) {
            collectedRegister.setFailureInformation(ResultType.InCompatible, MdcManager.getIssueCollector().addWarning(register.getObisCode(), "registerXissue", register.getObisCode(), errorMessage[0]));
        } else {
            collectedRegister.setFailureInformation(ResultType.NotSupported, MdcManager.getIssueCollector().addWarning(register.getObisCode(), "registerXnotsupported", register.getObisCode()));
        }
        return collectedRegister;
    }

    private RegisterIdentifier getRegisterIdentifier(OfflineRegister offlineRtuRegister) {
        return new RegisterIdentifierById(offlineRtuRegister.getRegisterId(), offlineRtuRegister.getObisCode());
    }

    private G3RegisterMapper getRegisterMapper() {
        if (registerMapper == null) {
            registerMapper = new G3RegisterMapper(getDlmsSession().getCosemObjectFactory(), getDlmsSession().getTimeZone(), getDlmsSession().getLogger());
        }
        return registerMapper;
    }
}