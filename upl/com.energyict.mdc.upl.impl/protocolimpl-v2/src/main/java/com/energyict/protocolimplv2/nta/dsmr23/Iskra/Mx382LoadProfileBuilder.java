package com.energyict.protocolimplv2.nta.dsmr23.Iskra;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.dlms.cosem.ComposedCosemObject;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocolimplv2.common.composedobjects.ComposedProfileConfig;
import com.energyict.protocolimplv2.nta.dsmr23.profiles.CapturedRegisterObject;
import com.energyict.protocolimplv2.nta.dsmr23.profiles.LoadProfileBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.energyict.protocolimplv2.nta.dsmr40.common.profiles.Dsmr40LoadProfileBuilder.MBUS_LP_DUPLICATED_CHANNEL;
import static com.energyict.protocolimplv2.nta.esmr50.common.loadprofiles.ESMR50LoadProfileBuilder.setFieldAndGet;

public class Mx382LoadProfileBuilder extends LoadProfileBuilder<Mx382> {

    public Mx382LoadProfileBuilder(Mx382 meterProtocol, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(meterProtocol, collectedDataFactory, issueFactory);
    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> allLoadProfileReaders) {
        this.expectedLoadProfileReaders = filterOutAllInvalidLoadProfiles(allLoadProfileReaders);
        this.loadProfileConfigurationList = new ArrayList<>();

        ComposedCosemObject ccoLpConfigs = constructLoadProfileConfigComposedCosemObject(expectedLoadProfileReaders, meterProtocol.getDlmsSessionProperties().isBulkRequest());

        List<CapturedRegisterObject> capturedObjectRegisterList;
        try {
            capturedObjectRegisterList = createCapturedObjectRegisterList(ccoLpConfigs);
        } catch (IOException e) {
            throw DLMSIOExceptionHandler.handle(e, getMeterProtocol().getDlmsSessionProperties().getRetries() + 1);
        }
        ComposedCosemObject ccoCapturedObjectRegisterUnits = constructCapturedObjectRegisterUnitComposedCosemObject(capturedObjectRegisterList, meterProtocol.getDlmsSessionProperties().isBulkRequest());

        for (LoadProfileReader lpr : allLoadProfileReaders) {
            CollectedLoadProfileConfiguration lpc = this.collectedDataFactory.createCollectedLoadProfileConfiguration(lpr.getProfileObisCode(), lpr.getMeterSerialNumber());
            if (!expectedLoadProfileReaders.contains(lpr)) {      //Invalid LP, mark as not supported and move on to the next LP
                lpc.setSupportedByMeter(false);
                continue;
            }

            this.meterProtocol.journal( "Reading configuration from LoadProfile " + lpr);
            ComposedProfileConfig cpc = lpConfigMap.get(lpr);
            if (cpc != null) {
                try {
                    lpc.setProfileInterval(ccoLpConfigs.getAttribute(cpc.getLoadProfileInterval()).intValue());
                    List<ChannelInfo> channelInfos = constructChannelInfos(capturedObjectRegisterListMap.get(lpr), ccoCapturedObjectRegisterUnits);

                    if (lpc.getObisCode().equalsIgnoreBChannel(MBUS_HOURLY_LP_OBISCODE) ||
                            lpc.getObisCode().equalsIgnoreBChannel(MBUS_DAILY_LP_OBISCODE) ||
                            lpc.getObisCode().equalsIgnoreBChannel(MBUS_MONTHLY_LP_OBISCODE)) {
                        // remap duplicated 0.x.24.2.1.255 (timestamp) to 0.x.24.2.5.255
                        channelInfos.stream().filter(
                                ci -> ci.getChannelObisCode().equalsIgnoreBChannel(MBUS_LP_DUPLICATED_CHANNEL) &&
                                        ci.getUnit().equals(Unit.get(BaseUnit.SECOND))
                        ).forEach(
                                ci -> ci.setName( setFieldAndGet(ObisCode.fromString(ci.getName()), 5, 5).toString() )
                        );
                    }

                    lpc.setChannelInfos(channelInfos);
                    this.channelInfoMap.put(lpr, channelInfos);

                    if (lpc.getObisCode().equalsIgnoreBChannel(MBUS_DAILY_LP_OBISCODE)) {
                        this.statusMasksMap.put(lpr, constructDailyStatusMask(capturedObjectRegisterListMap.get(lpr)));
                        this.channelMaskMap.put(lpr, constructDailyChannelMask(capturedObjectRegisterListMap.get(lpr)));
                    }
                    else {
                        this.statusMasksMap.put(lpr, constructStatusMask(capturedObjectRegisterListMap.get(lpr)));
                        this.channelMaskMap.put(lpr, constructChannelMask(capturedObjectRegisterListMap.get(lpr)));
                    }

                } catch (IOException e) {
                    if (DLMSIOExceptionHandler.isUnexpectedResponse(e, getMeterProtocol().getDlmsSessionProperties().getRetries() + 1)) {
                        lpc.setSupportedByMeter(false);
                        getMeterProtocol().journal("Load profile "+lpr+" is not supported by the meter: "+e.getLocalizedMessage());
                    }
                }
            } else {
                lpc.setSupportedByMeter(false);
                getMeterProtocol().journal("Load profile configuration for "+lpr+" could not be retrieved from local configuration.");
            }
            this.loadProfileConfigurationList.add(lpc);
        }
        return this.loadProfileConfigurationList;
    }

    private int constructDailyStatusMask(List<CapturedRegisterObject> registers) {
        int statusMask = 0;
        int counter = 0;
        for (CapturedRegisterObject register : registers) {
            if (isStatusObisCode(register.getObisCode(), register.getSerialNumber())) {
                int bField = register.getObisCode().getB();
                if (bField==0) {
                    statusMask |= (int) Math.pow(2, counter);
                }
                else {
                    statusMask |= (int) Math.pow(2, bField+5);
                }
            }
            counter++;
        }
        return statusMask;
    }

    private int constructDailyChannelMask(List<CapturedRegisterObject> registers) {
        int channelMask = 0;
        int counter = 0;

        for (CapturedRegisterObject register : registers) {
            if (isValidMasterRegister(register)) {
                channelMask |= (int) Math.pow(2, counter);
            } else if (isMbusRegister(register)) {
                final int bField = register.getObisCode().getB();
                channelMask |= (int) Math.pow(2, (bField + 9));
            }
            counter++;
        }
        return channelMask;
    }
}
