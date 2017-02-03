package com.energyict.smartmeterprotocolimpl.nta.dsmr40.common.profiles;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.cosem.ComposedCosemObject;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.attributes.DemandRegisterAttributes;
import com.energyict.dlms.cosem.attributes.ExtendedRegisterAttributes;
import com.energyict.mdc.upl.LoadProfileConfigurationException;
import com.energyict.mdc.upl.SmartMeterProtocol;
import com.energyict.protocol.ChannelInfo;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.profiles.CapturedRegisterObject;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.profiles.LoadProfileBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Providing functionality to fetch and create {@link com.energyict.protocol.ProfileData ProfileData} objects for {@link SmartMeterProtocol DSMR 4.0 SmartMeterProtocols}
 *
 * @author sva
 * @since 30/01/13 - 14:25
 */
public class Dsmr40LoadProfileBuilder extends LoadProfileBuilder {

    private boolean cumulativeCaptureTimeChannel = false;

    /**
     * Default constructor
     *
     * @param meterProtocol the {@link #meterProtocol}
     */
    public Dsmr40LoadProfileBuilder(AbstractSmartNtaProtocol meterProtocol) {
        super(meterProtocol);
    }

    public void setCumulativeCaptureTimeChannel(boolean cumulativeCaptureTimeChannel) {
        this.cumulativeCaptureTimeChannel = cumulativeCaptureTimeChannel;
    }

    @Override
    protected List<ChannelInfo> constructChannelInfos(List<CapturedRegisterObject> registers, ComposedCosemObject ccoRegisterUnits) throws IOException {
        List<ChannelInfo> channelInfos = new ArrayList<ChannelInfo>();
        for (CapturedRegisterObject registerObject : registers) {
            if (!registerObject.getSerialNumber().equalsIgnoreCase("") && isDataObisCode(registerObject.getObisCode(), registerObject.getSerialNumber())) {
                if (this.getRegisterUnitMap().containsKey(registerObject)) {
                    registerObject.getAttribute();
                    ScalerUnit su = getScalerUnitForCapturedRegisterObject(registerObject, ccoRegisterUnits);
                    if (su.getUnitCode() != 0) {
                        ChannelInfo ci = new ChannelInfo(channelInfos.size(), registerObject.getObisCode().toString(), su.getEisUnit(), registerObject.getSerialNumber(), isCumulativeChannel(registerObject));
                        channelInfos.add(ci);
                    } else {
                        //TODO CHECK if this is still correct!
                        ChannelInfo ci = new ChannelInfo(channelInfos.size(), registerObject.getObisCode().toString(), Unit.getUndefined(), registerObject.getSerialNumber(), true);
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
     * Retrieve the appropriate ScalerUnit for the channel, based on the {@link CapturedRegisterObject}.<br></br>
     * Channels who store the capture timestamp will be fixed assigned ScalerUnit seconds.<br></br>
     * For all other channels, the ScalerUnit will be requested from the device.
     *
     * @param registerObject   the CapturedRegisterObject
     * @param ccoRegisterUnits the ComposedCosemObject
     * @return the ScalerUnit for the channel
     * @throws IOException
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
     * Method to check if the channel is cumulative, based on the {@link CapturedRegisterObject}.<br></br>
     * Channels who store the capture timestamp are not cumulative.
     *
     * @param registerObject the CapturedRegisterObject
     * @return true, if the channel is cumulative
     * false, if not cumulative
     */
    protected boolean isCumulativeChannel(CapturedRegisterObject registerObject) {
        if ((registerObject.getClassId() == DLMSClassId.EXTENDED_REGISTER.getClassId()) &&
                (registerObject.getAttribute() == ExtendedRegisterAttributes.CAPTURE_TIME.getAttributeNumber())) {
            return cumulativeCaptureTimeChannel;
        } else if ((registerObject.getClassId() == DLMSClassId.DEMAND_REGISTER.getClassId()) &&
                (registerObject.getAttribute() == DemandRegisterAttributes.CAPTURE_TIME.getAttributeNumber())) {
            return cumulativeCaptureTimeChannel;
        }
        return true;
    }
}
