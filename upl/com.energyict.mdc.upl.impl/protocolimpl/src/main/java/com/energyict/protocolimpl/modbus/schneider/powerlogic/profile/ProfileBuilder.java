package com.energyict.protocolimpl.modbus.schneider.powerlogic.profile;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.ProfileData;
import com.energyict.protocolimpl.modbus.core.functioncode.ReadGeneralReferenceRequest;
import com.energyict.protocolimpl.modbus.schneider.powerlogic.PM5561;
import com.energyict.protocolimpl.modbus.schneider.powerlogic.PM5561RegisterFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class ProfileBuilder {

    private static final int SECONDS_PER_MINUTE = 60;
    private static final int REGULAR_NUMBER_OF_PROFILE_RECORDS_PER_PROFILE_BLOCK = 1;
    private PM5561 protocol;
    private ProfileBlock lastProfileBlock;
    private double ratio = 1;

    public ProfileBuilder(PM5561 protocol) {
        this.protocol = protocol;
        setCTRatio();
    }

    public List getChannelInfos() throws IOException {
        return getLastProfileBlock().getProfileData().getChannelInfos();
    }

    public int getNumberOfChannels() throws IOException {
        return getLastProfileBlock().getProfileData().getChannelInfos().size();
    }

    public int getProfileInterval() throws IOException {
        return getLastProfileBlock().getProfileHeader().getIntegrationPeriod() * SECONDS_PER_MINUTE;
    }

    public ProfileData getProfileData(Date from, Date to, boolean generateEvents) throws IOException {
        List<ProfileBlock> profileDataBlocks = new ArrayList<ProfileBlock>();

        profileDataBlocks.add(getLastProfileBlock());
        ProfileBlock profileBlock = getLastProfileBlock();
        while (from.before(profileBlock.getOldestProfileRecordDate()) &&
                profileBlock.getProfileRecords().getProfileRecords().size() == REGULAR_NUMBER_OF_PROFILE_RECORDS_PER_PROFILE_BLOCK) { // If a ProfileRecord contains less entries, then it was the last/oldest one available > you should stop readout
            BigDecimal referenceNo  = (BigDecimal) protocol.getRegisterFactory().findRegister(PM5561RegisterFactory.LOAD_PROFILE_FIRST_RECORD).value();
            ReadGeneralReferenceRequest readGeneralReferenceRequest = protocol.getRegisterFactory().findRegister(PM5561RegisterFactory.LOAD_PROFILE_RECORD_ITEM1).getReadGeneralReferenceRequest(referenceNo.longValue());
            List deviceObjects = readGeneralReferenceRequest.getValues();
            profileBlock = new ProfileBlock(deviceObjects);
            profileDataBlocks.add(profileBlock);
        }

        return mergeProfileDataBlocks(profileDataBlocks, from, to);
    }

    private ProfileData mergeProfileDataBlocks(List<ProfileBlock> profileDataBlocks, Date from, Date to) {
        ProfileData profileData = new ProfileData();
        profileData.setChannelInfos(profileDataBlocks.get(0).getProfileData().getChannelInfos());   // The list will contain at least 1 profileBlock, so get(0) is always possible

        for (ProfileBlock profileDataBlock : profileDataBlocks) {
            for (IntervalData intervalData : profileDataBlock.getProfileData().getIntervalDatas()) {
                if (intervalData.getEndTime().after(from) && intervalData.getEndTime().before(to)) {
                    profileData.addInterval(intervalData);  // Only add entries fitting within the time boundaries
                }
            }
        }

        return profileData;
    }

    private ProfileBlock getLastProfileBlock() throws IOException {
        if (this.lastProfileBlock == null) {
            BigDecimal lastRecord = (BigDecimal) protocol.getRegisterFactory().findRegister(PM5561RegisterFactory.LOAD_PROFILE_LAST_RECORD).value();
            ReadGeneralReferenceRequest readGeneralReferenceRequest = protocol.getRegisterFactory().findRegister(PM5561RegisterFactory.LOAD_PROFILE_RECORD_ITEM1).getReadGeneralReferenceRequest(lastRecord.longValue());
            List deviceObjects = readGeneralReferenceRequest.getValues();
            this.lastProfileBlock = new ProfileBlock(deviceObjects);
        }
        return this.lastProfileBlock;
    }

    private void setCTRatio(){
        if(protocol.isApplyCtRatio()){
            try {
                BigDecimal secondaryCT = (BigDecimal) protocol.getRegisterFactory().findRegister(ObisCode.fromString("1.0.96.8.0.255")).value();
                BigDecimal primaryCT = (BigDecimal) protocol.getRegisterFactory().findRegister(ObisCode.fromString("1.0.96.9.0.255")).value();
                ratio = primaryCT.intValue()/secondaryCT.intValue();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private double getCTRatio() {
        return ratio;
    }

    public boolean isSupported() throws IOException {
        BigDecimal loadProfileStatus = (BigDecimal)protocol.getRegisterFactory().findRegister(PM5561RegisterFactory.LOAD_PROFILE_STATUS).value();
        return loadProfileStatus.intValue() == 1;
    }


}
