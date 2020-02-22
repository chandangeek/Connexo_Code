package com.energyict.protocolimplv2.nta.dsmr40.common.profiles;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.cosem.ComposedCosemObject;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.attributes.DemandRegisterAttributes;
import com.energyict.dlms.cosem.attributes.ExtendedRegisterAttributes;
import com.energyict.mdc.upl.LoadProfileConfigurationException;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.nta.dsmr23.profiles.CapturedRegisterObject;
import com.energyict.protocolimplv2.nta.dsmr23.profiles.LoadProfileBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 19/12/2014 - 9:28
 */
public class Dsmr40LoadProfileBuilder<T extends AbstractDlmsProtocol> extends LoadProfileBuilder<T> {

    public static final ObisCode MBUS_HOURLY_LP_OBISCODE  = ObisCode.fromString("0.x.24.3.0.255");
    public static final ObisCode MBUS_DAILY_LP_OBISCODE   = ObisCode.fromString("1.x.99.2.0.255");
    public static final ObisCode MBUS_MONTHLY_LP_OBISCODE = ObisCode.fromString("0.x.98.1.0.255");

    public static final ObisCode MBUS_LP_DUPLICATED_CHANNEL = ObisCode.fromString("0.x.24.2.1.255");

    private boolean cumulativeCaptureTimeChannel = false;

    /**
     * Default constructor
     *
     * @param meterProtocol the {com.energyict.protocolimplv2.nta.abstractnta.AbstractSmartNtaProtocol}
     */
    public Dsmr40LoadProfileBuilder(T meterProtocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(meterProtocol, collectedDataFactory, issueFactory);
    }

    public void setCumulativeCaptureTimeChannel(boolean cumulativeCaptureTimeChannel) {
        this.cumulativeCaptureTimeChannel = cumulativeCaptureTimeChannel;
    }

    @Override
    protected List<ChannelInfo> constructChannelInfos(List<CapturedRegisterObject> registers, ComposedCosemObject ccoRegisterUnits) throws IOException {
        List<ChannelInfo> channelInfos = new ArrayList<>();
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