package com.energyict.protocolimplv2.dlms.idis.aec3phase.profiledata;

import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;

import com.energyict.cbo.Unit;
import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.ComposedCosemObject;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.attributes.DemandRegisterAttributes;
import com.energyict.dlms.cosem.attributes.ExtendedRegisterAttributes;
import com.energyict.dlms.cosem.attributes.RegisterAttributes;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.exception.CommunicationException;
import com.energyict.protocol.exception.ProtocolExceptionMessageSeeds;
import com.energyict.protocolimplv2.dlms.idis.aec.profiledata.AECProfileDataReader;
import com.energyict.protocolimplv2.dlms.idis.aec3phase.AEC3Phase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AEC3PhaseProfileDataReader <T extends AEC3Phase> extends AECProfileDataReader <AEC3Phase> {
    public AEC3PhaseProfileDataReader(AEC3Phase protocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, long limitMaxNrOfDays) {
        super(protocol, collectedDataFactory, issueFactory, limitMaxNrOfDays);
    }

    protected boolean isProfileStatus(ObisCode obisCode) {
        return (obisCode.getA() == 0 && (obisCode.getB() >= 0 && obisCode.getB() <= 6) && obisCode.getC() == 96 && (obisCode.getD() == 5 || obisCode.getD() == 10 ) && (obisCode.getE() == 0 || obisCode.getE() == 1 || obisCode.getE() == 2 || obisCode.getE() == 3) && obisCode.getF() == 255);
    }

    @Override
    public Map<ObisCode, Unit> readUnits(ObisCode correctedLoadProfileObisCode, List<ObisCode> channelObisCodes) throws ProtocolException {
        Map<ObisCode, Unit> result = new HashMap<>();

        Map<ObisCode, DLMSAttribute> attributes = new HashMap<>();
        for (ObisCode channelObisCode : channelObisCodes) {
            UniversalObject uo = DLMSUtils.findCosemObjectInObjectList(this.protocol.getDlmsSession().getMeterConfig().getInstantiatedObjectList(), channelObisCode);
            if (uo != null) {
                DLMSAttribute unitAttribute;
                if (uo.getDLMSClassId() == DLMSClassId.REGISTER) {
                    unitAttribute = new DLMSAttribute(channelObisCode, RegisterAttributes.SCALER_UNIT.getAttributeNumber(), uo.getClassID());
                } else if (uo.getDLMSClassId() == DLMSClassId.EXTENDED_REGISTER) {
                    unitAttribute = new DLMSAttribute(channelObisCode, ExtendedRegisterAttributes.UNIT.getAttributeNumber(), uo.getClassID());
                } else if (uo.getDLMSClassId() == DLMSClassId.DEMAND_REGISTER) {
                    unitAttribute = new DLMSAttribute(channelObisCode, DemandRegisterAttributes.UNIT.getAttributeNumber(), uo.getClassID());
                } else {
                    throw new ProtocolException("Unexpected captured_object in load profile: " + uo.getDescription());
                }
                attributes.put(channelObisCode, unitAttribute);
            }
        }

        //Also read out the profile interval in this bulk request
        DLMSAttribute profileIntervalAttribute = null;
        if (correctedLoadProfileObisCode != null) {
            profileIntervalAttribute = new DLMSAttribute(correctedLoadProfileObisCode, 4, DLMSClassId.PROFILE_GENERIC);
            attributes.put(correctedLoadProfileObisCode, profileIntervalAttribute);
        }

        ComposedCosemObject composedCosemObject = new ComposedCosemObject(protocol.getDlmsSession(), protocol.getDlmsSessionProperties().isBulkRequest(), new ArrayList<>(attributes.values()));

        if (correctedLoadProfileObisCode != null) {
            try {
                AbstractDataType attribute = composedCosemObject.getAttribute(profileIntervalAttribute);
                getIntervalMap().put(correctedLoadProfileObisCode, attribute.intValue());
            } catch (IOException e) {
                throw DLMSIOExceptionHandler.handle(e, protocol.getDlmsSessionProperties().getRetries() + 1);
            }
        }

        for (ObisCode channelObisCode : channelObisCodes) {
            DLMSAttribute dlmsAttribute = attributes.get(channelObisCode);
            if (dlmsAttribute != null) {
                try {
                    result.put(channelObisCode, new ScalerUnit(composedCosemObject.getAttribute(dlmsAttribute)).getEisUnit());
                } catch (IOException e) {
                    if (DLMSIOExceptionHandler.isUnexpectedResponse(e, protocol.getDlmsSessionProperties().getRetries() + 1)) {
                        throw DLMSIOExceptionHandler.handle(e, protocol.getDlmsSessionProperties().getRetries() + 1);
                    } //Else: throw ConnectionCommunicationException
                } catch (IllegalArgumentException e) {
                    throw new CommunicationException(ProtocolExceptionMessageSeeds.UNEXPECTED_RESPONSE);
                }
            } else {
                String message = "The OBIS code " + channelObisCode + " found in the meter load profile capture objects list, is NOT supported by the meter itself." +
                        " If ReadCache property is not active, try again with this property enabled. Otherwise, please reprogram the meter with a valid set of capture objects.";

                if (protocol.getDlmsSessionProperties().validateLoadProfileChannels()) {
                    throw new ProtocolException(message);
                } else {
                    protocol.getLogger().warning(message);
                }
            }
        }
        return result;
    }
}
