package com.energyict.protocolimplv2.dlms.idis.hs3400.lte.pp.registers;


import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.ComposedCosemObject;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.attributes.DataAttributes;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.upl.NotInObjectListException;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.mdc.upl.tasks.support.DeviceRegisterSupport;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.exception.ConnectionCommunicationException;
import com.energyict.protocolimplv2.common.composedobjects.ComposedRegister;
import com.energyict.protocolimplv2.dlms.idis.hs3300.registers.HS3300RegisterFactory;
import com.energyict.protocolimplv2.dlms.idis.hs3400.lte.pp.HS3400LtePP;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public class HS3400RegisterFactory extends HS3300RegisterFactory implements DeviceRegisterSupport {
    private final HS3400LtePP hs3400LtePP;
    private HS3400LteRegisterMapper hs3400LteRegisterMapper;

    public HS3400RegisterFactory(HS3400LtePP hs3400LtePP, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(hs3400LtePP, collectedDataFactory, issueFactory);
        this.hs3400LtePP = hs3400LtePP;

    }

    private HS3400LteRegisterMapper getHS3400LteRegisterMapper() {
        if (hs3400LteRegisterMapper == null) {
            hs3400LteRegisterMapper = new HS3400LteRegisterMapper(getDlmsSession());
        }
        return hs3400LteRegisterMapper;
    }

    @Override
    protected DlmsSession getDlmsSession() {
        return hs3400LtePP.getDlmsSession();
    }

    @Override
    public final List<CollectedRegister> readRegisters(final List<OfflineRegister> offlineRegisterList) {

        // parse the requests and build the composed objects and list of attributes to read
        prepareReading(offlineRegisterList);

        ComposedCosemObject composedCosemObject = new ComposedCosemObject(getDlmsSession(), getDlmsSession().getProperties().isBulkRequest(), getDLMSAttributes());
        composedCosemObject.setUseAccessService(true);

        for (OfflineRegister offlineRegister : offlineRegisterList) {

            ComposedRegister composedRegister = getComposedRegisterMap().get(offlineRegister.getObisCode());
            if (composedRegister == null) {
                addResult(createFailureCollectedRegister(offlineRegister, ResultType.NotSupported));
                continue;
            }

            LteMapping lteMapping= getHS3400LteRegisterMapper().getLteMapping(offlineRegister.getObisCode());
            final ObisCode baseObisCode = lteMapping == null ? offlineRegister.getObisCode() : lteMapping.getBaseObisCode();
            final UniversalObject universalObject;
            try {
                universalObject = getDlmsSession().getMeterConfig().findObject(baseObisCode);
            } catch (final NotInObjectListException e) {
                addResult(createFailureCollectedRegister(offlineRegister, ResultType.NotSupported));
                continue;   // Move on to the next register, this one is not supported by the meter
            }

            try {
                RegisterValue registerValue;

                if (lteMapping != null) {
                    if (composedRegister.getRegisterValueAttribute() != null) {
                        registerValue = lteMapping.parse(composedCosemObject.getAttribute(composedRegister.getRegisterValueAttribute()));
                    } else {
                        registerValue = lteMapping.readRegister(getDlmsSession().getCosemObjectFactory());
                    }
                } else {
                    registerValue = parseRegisterReading(universalObject, composedCosemObject, offlineRegister, composedRegister, baseObisCode);
                }

                if (registerValue != null) {
                    addResult(createCollectedRegister(registerValue, offlineRegister));
                }
            } catch (IOException e) {
                getLogger().warning("Error while reading " + offlineRegister + ": " + e.getMessage());
                if (DLMSIOExceptionHandler.isUnexpectedResponse(e, getDlmsSession().getProperties().getRetries() + 1)) {
                    if (DLMSIOExceptionHandler.isNotSupportedDataAccessResultException(e)) {
                        addResult(createFailureCollectedRegister(offlineRegister, ResultType.NotSupported));
                    } else {
                        addResult(createFailureCollectedRegister(offlineRegister, ResultType.InCompatible, e.getMessage()));
                    }
                } else {
                    throw ConnectionCommunicationException.numberOfRetriesReachedWithConnectionStillIntact(e, getDlmsSession().getProperties().getRetries() + 1);
                }
            } catch (Exception ex) {
                getLogger().warning("Error while reading " + offlineRegister + ": " + ex.getMessage());
                addResult(createFailureCollectedRegister(offlineRegister, ResultType.Other, ex.getMessage()));
                final ProtocolException protocolException = new ProtocolException(ex, "Error while reading out "+ offlineRegister.getObisCode().toString()+": " + ex.getMessage());
                throw ConnectionCommunicationException.unExpectedProtocolError(protocolException); // this leaves the connection intact
            }
        }
        return getCollectedRegisters();
    }

        @Override
        protected void prepareReading(final List<OfflineRegister> offlineRegisters) {

            for (OfflineRegister register : offlineRegisters) {
                LteMapping lteMapping = getHS3400LteRegisterMapper().getLteMapping(register.getObisCode());
                final UniversalObject universalObject;
                final ObisCode baseObisCode = lteMapping == null ? register.getObisCode() : lteMapping.getBaseObisCode();
                try {
                    universalObject = getDlmsSession().getMeterConfig().findObject(baseObisCode);
                } catch (final NotInObjectListException e) {
                    continue;   // Move on to the next register, this one is not supported by the meter
                }

                if (lteMapping != null) {
                    prepareMappedRegister(register, lteMapping, this.hs3400LtePP);
                } else {
                    prepareStandardRegister(register, universalObject);
                }
            }
        }

    protected void prepareMappedRegister(OfflineRegister register, LteMapping lteMapping, HS3400LtePP hs3400LtePP)  {
        ComposedRegister composedRegister = new ComposedRegister();
        DLMSAttribute valueAttribute = new DLMSAttribute(lteMapping.getBaseObisCode(), register.getObisCode().getE() , lteMapping.getDLMSClassId());
        addAttributeToRead(valueAttribute);
        composedRegister.setRegisterValue(valueAttribute);
        addComposedRegister(register.getObisCode(), composedRegister);
    }

    private RegisterValue parseRegisterReading(UniversalObject universalObject, ComposedCosemObject composedCosemObject, OfflineRegister offlineRegister, ComposedRegister composedRegister, ObisCode baseObisCode) throws IOException {

        RegisterValue registerValue;
        if (universalObject.getClassID() == DLMSClassId.GPRS_SETUP.getClassId()) {
            final AbstractDataType attribute = composedCosemObject.getAttribute(composedRegister.getRegisterValueAttribute());
            if (attribute.isOctetString()) {
                registerValue = new RegisterValue(offlineRegister.getObisCode(), attribute.getOctetString().stringValue());
            } else {
                registerValue = new RegisterValue(offlineRegister.getObisCode(), new Quantity(attribute.toBigDecimal(), Unit.get(BaseUnit.UNITLESS)));
            }
        } else if (universalObject.getClassID() == DLMSClassId.PPP_SETUP.getClassId()) {
            final AbstractDataType attribute = composedCosemObject.getAttribute(composedRegister.getRegisterValueAttribute());
            if (attribute.isStructure()) {
                registerValue = new RegisterValue(offlineRegister.getObisCode(), attribute.getStructure().toString());
            } else {
                registerValue = new RegisterValue(offlineRegister.getObisCode(), new Quantity(attribute.toBigDecimal(), Unit.get(BaseUnit.UNITLESS)));
            }
        } else {
            Unit unit = Unit.get(BaseUnit.UNITLESS);
            if (composedRegister.getRegisterUnitAttribute() != null) {
                try {
                    unit = new ScalerUnit(composedCosemObject.getAttribute(composedRegister.getRegisterUnitAttribute())).getEisUnit();
                } catch (Exception ex) {
                    getLogger().warning("Cannot get unit from " + universalObject.getObisCode() + ": " + ex.getMessage());
                }

            }
            Date captureTime = null;
            if (composedRegister.getRegisterCaptureTime() != null) {
                AbstractDataType captureTimeOctetString = composedCosemObject.getAttribute(composedRegister.getRegisterCaptureTime());
                captureTime = captureTimeOctetString.getOctetString().getDateTime(getDlmsSession().getTimeZone()).getValue().getTime();
            }

            AbstractDataType attributeValue = composedCosemObject.getAttribute(composedRegister.getRegisterValueAttribute());
            registerValue = new RegisterValue(offlineRegister.getObisCode(), new Quantity(attributeValue.toBigDecimal(), unit), captureTime);
        }

        return registerValue;
    }

    protected void prepareStandardRegister(OfflineRegister register, UniversalObject universalObject) {
        ComposedRegister composedRegister = new ComposedRegister();
        final int classId = universalObject.getClassID();

        if (classId == DLMSClassId.GPRS_SETUP.getClassId()) {
            DLMSAttribute valueAttribute = new DLMSAttribute(register.getObisCode(), DataAttributes.VALUE.getAttributeNumber(), classId);
            composedRegister.setRegisterValue(valueAttribute);
        }

        if (classId == DLMSClassId.PPP_SETUP.getClassId()) {
            DLMSAttribute valueAttribute = new DLMSAttribute(register.getObisCode(), DataAttributes.VALUE.getAttributeNumber(), classId);
            composedRegister.setRegisterValue(valueAttribute);
        }
    }

}
