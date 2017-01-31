/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.nta.dsmr40.common.profiles;

import com.elster.jupiter.metering.ReadingType;
import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.LoadProfileConfigurationException;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;

import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.cosem.ComposedCosemObject;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.attributes.DemandRegisterAttributes;
import com.energyict.dlms.cosem.attributes.ExtendedRegisterAttributes;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.nta.dsmr23.profiles.LoadProfileBuilder;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.profiles.CapturedRegisterObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Dsmr40LoadProfileBuilder extends LoadProfileBuilder {

    private boolean cumulativeCaptureTimeChannel = false;

    /**
     * Default constructor
     *
     * @param meterProtocol the {@link #meterProtocol}
     * @param collectedDataFactory
     */
    public Dsmr40LoadProfileBuilder(AbstractDlmsProtocol meterProtocol, IssueService issueService, MdcReadingTypeUtilService readingTypeUtilService, boolean supportsBulkRequests, CollectedDataFactory collectedDataFactory) {
        super(meterProtocol, issueService, readingTypeUtilService, collectedDataFactory, supportsBulkRequests);
    }

    public void setCumulativeCaptureTimeChannel(boolean cumulativeCaptureTimeChannel) {
        this.cumulativeCaptureTimeChannel = cumulativeCaptureTimeChannel;
    }

    @Override
    protected List<ChannelInfo> constructChannelInfos(List<CapturedRegisterObject> registers, ComposedCosemObject ccoRegisterUnits, List<ChannelInfo> configuredChannelInfos) throws IOException {
        List<ChannelInfo> channelInfos = new ArrayList<>();
        for (CapturedRegisterObject registerObject : registers) {
            if (!registerObject.getSerialNumber().isEmpty() && isDataObisCode(registerObject.getObisCode(), registerObject.getSerialNumber())) {
                if (this.getRegisterUnitMap().containsKey(registerObject)) {
                    registerObject.getAttribute();
                    ScalerUnit su = getScalerUnitForCapturedRegisterObject(registerObject, ccoRegisterUnits);
                    Optional<ReadingType> readingTypeFromConfiguredChannels = getReadingTypeFromConfiguredChannels(registerObject.getObisCode(), configuredChannelInfos);
                    if(readingTypeFromConfiguredChannels.isPresent()){
                        if (su.getUnitCode() != 0) {
                            ChannelInfo ci = new ChannelInfo(channelInfos.size(), registerObject.getObisCode().toString(), su.getEisUnit(), registerObject.getSerialNumber(), isCumulativeChannel(registerObject),
                                    readingTypeFromConfiguredChannels.get());
                            channelInfos.add(ci);
                        } else {
                            //TODO CHECK if this is still correct!
                            ChannelInfo ci = new ChannelInfo(channelInfos.size(), registerObject.getObisCode().toString(), Unit.getUndefined(), registerObject.getSerialNumber(), true,
                                    readingTypeFromConfiguredChannels.get());
                            channelInfos.add(ci);
                        }
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