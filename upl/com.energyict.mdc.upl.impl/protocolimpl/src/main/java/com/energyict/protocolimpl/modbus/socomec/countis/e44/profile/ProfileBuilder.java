package com.energyict.protocolimpl.modbus.socomec.countis.e44.profile;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.ProfileData;
import com.energyict.protocolimpl.modbus.socomec.countis.e44.E44;
import com.energyict.protocolimpl.modbus.socomec.countis.e44.RegisterFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author sva
 * @since 8/10/2014 - 16:31
 */
public class ProfileBuilder {

    private static final int RESET_READ_POINTER_VALUE = 0x0001;
    private static final int GET_NEXT_DATA_READ_POINTER_VALUE = 0xFFFE;
    private static final int SECONDS_PER_MINUTE = 60;
    private static final int REGULAR_NUMBER_OF_PROFILE_RECORDS_PER_PROFILE_BLOCK = 29;
    private E44 protocol;
    private ProfileBlock lastProfileBlock;
    private double ratio = 1;

    public ProfileBuilder(E44 protocol) {
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
            protocol.getRegisterFactory().findRegister(RegisterFactory.LP_R1_AREA).getWriteSingleRegister(GET_NEXT_DATA_READ_POINTER_VALUE);
            int[] values = protocol.getRegisterFactory().findRegister(RegisterFactory.LP_R2_AREA).values();
            profileBlock = new ProfileBlock(values, getCTRatio());
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
            protocol.getRegisterFactory().findRegister(RegisterFactory.LP_R1_AREA).getWriteSingleRegister(RESET_READ_POINTER_VALUE);
            int[] values = protocol.getRegisterFactory().findRegister(RegisterFactory.LP_R2_AREA).values();
            this.lastProfileBlock = new ProfileBlock(values, getCTRatio());
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
}
