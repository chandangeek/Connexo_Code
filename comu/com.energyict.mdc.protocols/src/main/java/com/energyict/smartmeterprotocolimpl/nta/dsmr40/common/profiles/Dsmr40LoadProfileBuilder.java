package com.energyict.smartmeterprotocolimpl.nta.dsmr40.common.profiles;

import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.cosem.ComposedCosemObject;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.attributes.DemandRegisterAttributes;
import com.energyict.dlms.cosem.attributes.ExtendedRegisterAttributes;
import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.LoadProfileConfigurationException;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.legacy.SmartMeterProtocol;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.profiles.CapturedRegisterObject;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.profiles.LoadProfileBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Providing functionality to fetch and create {@link com.energyict.mdc.protocol.api.device.data.ProfileData ProfileData} objects for {@link SmartMeterProtocol DSMR 4.0 SmartMeterProtocols}
 *
 * @author sva
 * @since 30/01/13 - 14:25
 */
public class Dsmr40LoadProfileBuilder extends LoadProfileBuilder {

    /**
     * Default constructor
     *
     * @param meterProtocol the {@link #meterProtocol}
     */
    public Dsmr40LoadProfileBuilder(AbstractSmartNtaProtocol meterProtocol) {
        super(meterProtocol);
    }

    @Override
    protected List<ChannelInfo> constructChannelInfos(LoadProfileReader loadProfileReader, ComposedCosemObject ccoRegisterUnits) throws IOException {
        List<ChannelInfo> channelInfos = new ArrayList<>();
        for (CapturedRegisterObject registerObject : capturedObjectRegisterListMap.get(loadProfileReader)) {
            if (!"".equalsIgnoreCase(registerObject.getSerialNumber()) && isDataObisCode(registerObject.getObisCode(), registerObject.getSerialNumber())) {
                if (this.getRegisterUnitMap().containsKey(registerObject)) {
                    registerObject.getAttribute();
                    ChannelInfo configuredChannelInfo = getConfiguredChannelInfo(loadProfileReader, registerObject);
                    ScalerUnit su = getScalerUnitForCapturedRegisterObject(registerObject, ccoRegisterUnits);
                    if (su.getUnitCode() != 0) {
                        ChannelInfo ci = new ChannelInfo(channelInfos.size(), registerObject.getObisCode().toString(), su.getEisUnit(), registerObject.getSerialNumber(), isCumulativeChannel(registerObject), configuredChannelInfo.getReadingType());
                        channelInfos.add(ci);
                    } else {
                        //TODO CHECK if this is still correct!
                        ChannelInfo ci = new ChannelInfo(channelInfos.size(), registerObject.getObisCode().toString(), Unit.getUndefined(), registerObject.getSerialNumber(), true, configuredChannelInfo.getReadingType());
                        channelInfos.add(ci);
//                        throw new LoadProfileConfigurationException("Could not fetch a correct Unit for " + registerObject + " - unitCode was 0.");
                    }
                } else {
                    throw new LoadProfileConfigurationException("Could not fetch a correct Unit for " + registerObject + " - not in registerUnitMap.");
                }
            }
        }
        return channelInfos;
    }

    /**
     * Retrieve the appropriate ScalerUnit for the channel, based on the {@link com.energyict.smartmeterprotocolimpl.nta.dsmr23.profiles.CapturedRegisterObject}.<br></br>
     * Channels who store the capture timestamp will be fixed assigned ScalerUnit seconds.<br></br>
     * For all other channels, the ScalerUnit will be requested from the device.
     *
     * @param registerObject   the CapturedRegisterObject
     * @param ccoRegisterUnits the ComposedCosemObject
     * @return the ScalerUnit for the channel
     * @throws java.io.IOException
     */
    protected ScalerUnit getScalerUnitForCapturedRegisterObject(CapturedRegisterObject registerObject, ComposedCosemObject ccoRegisterUnits) throws IOException {
        ScalerUnit su = null;
        if (registerObject.getClassId() == DLMSClassId.EXTENDED_REGISTER.getClassId()) {
            if (registerObject.getAttribute() == ExtendedRegisterAttributes.CAPTURE_TIME.getAttributeNumber()) {
                su = new ScalerUnit(Unit.get(BaseUnit.SECOND));
            }
        } else if (registerObject.getClassId() == DLMSClassId.DEMAND_REGISTER.getClassId()) {
            if (registerObject.getAttribute() == DemandRegisterAttributes.CAPTURE_TIME.getAttributeNumber()) {
                su = new ScalerUnit(Unit.get(BaseUnit.SECOND));
            }
        }

        if (su == null) {
            su = new ScalerUnit(ccoRegisterUnits.getAttribute(this.getRegisterUnitMap().get(registerObject)));
        }
        return su;
    }

    /**
     * Method to check if the channel is cumulative, based on the {@link com.energyict.smartmeterprotocolimpl.nta.dsmr23.profiles.CapturedRegisterObject}.<br></br>
     * Channels who store the capture timestamp are not cumulative.
     *
     * @param registerObject the CapturedRegisterObject
     * @return true, if the channel is cumulative
     *         false, if not cumulative
     */
    protected boolean isCumulativeChannel(CapturedRegisterObject registerObject) {
        if ((registerObject.getClassId() == DLMSClassId.EXTENDED_REGISTER.getClassId()) &&
                (registerObject.getAttribute() == ExtendedRegisterAttributes.CAPTURE_TIME.getAttributeNumber())) {
            return false;
        } else if ((registerObject.getClassId() == DLMSClassId.DEMAND_REGISTER.getClassId()) &&
                (registerObject.getAttribute() == DemandRegisterAttributes.CAPTURE_TIME.getAttributeNumber())) {
            return false;
        }
        return true;
    }
}
