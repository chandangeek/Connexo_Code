package com.energyict.protocolimpl.modbus.schneider.powerlogic.profile;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.ProfileData;
import com.energyict.protocolimpl.modbus.core.AbstractRegister;
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
    private static final int REGULAR_NUMBER_OF_CHANNELS = 8;
    private PM5561 protocol;
    private ProfileBlock lastProfileBlock;
    private double ratio = 1;
    BigDecimal loadProfileStatus = null;
    BigDecimal numberOfRecords = null;
    BigDecimal referenceNo = null;
    BigDecimal lastRecord = null;
    BigDecimal firstRecord = null;
    AbstractRegister loadProfileRecordItem1 = null;

    public ProfileBuilder(PM5561 protocol) {
        this.protocol = protocol;
        setCTRatio();
    }

    public List getChannelInfos() throws IOException {
        return getLastProfileBlock().getProfileData().getChannelInfos();
    }

    public int getNumberOfChannels() throws IOException {
        return getChannelInfos().size();
    }

    public int getProfileInterval() throws IOException {
        return getLastProfileBlock().getProfileHeader().getIntegrationPeriod() * SECONDS_PER_MINUTE;
    }

    public ProfileData getProfileData(Date from, Date to, boolean generateEvents) throws IOException {
        if(firstRecord == null) {
            firstRecord = (BigDecimal) protocol.getRegisterFactory().findRegister(PM5561RegisterFactory.LOAD_PROFILE_FIRST_RECORD).value();
        }
        List<ProfileBlock> profileDataBlocks = new ArrayList<ProfileBlock>();
        ProfileBlock profileBlock = getLastProfileBlock();
        if(numberOfRecords == null) {
            numberOfRecords = (BigDecimal) protocol.getRegisterFactory().findRegister(PM5561RegisterFactory.LOAD_PROFILE_NUMBER_OF_RECORDS).value();
        }
        if(referenceNo == null) {
            referenceNo = (BigDecimal) protocol.getRegisterFactory().findRegister(PM5561RegisterFactory.LOAD_PROFILE_FIRST_RECORD).value();
        }
        if(loadProfileRecordItem1 == null) {
            loadProfileRecordItem1 = protocol.getRegisterFactory().findRegister(PM5561RegisterFactory.LOAD_PROFILE_RECORD_ITEM1);
        }
        int index = firstRecord.intValue();
        ReadGeneralReferenceRequest readGeneralReferenceRequest = loadProfileRecordItem1.getReadGeneralReferenceRequest(referenceNo.intValue() + index);
        profileBlock = new ProfileBlock(readGeneralReferenceRequest.getValues(), REGULAR_NUMBER_OF_CHANNELS, protocol.getTimeZone());
        if(from.before(profileBlock.getOldestProfileRecordDate())) {
            profileDataBlocks.add(profileBlock);
        }else{
            long diffInMillies = from.getTime() - profileBlock.getOldestProfileRecordDate().getTime();
            if(diffInMillies > 360000) {
                index += diffInMillies / 360000;
            }
        }
        while (index < lastRecord.intValue()) {
            readGeneralReferenceRequest = loadProfileRecordItem1.getReadGeneralReferenceRequest(referenceNo.intValue() + index);
            profileBlock = new ProfileBlock(readGeneralReferenceRequest.getValues(), REGULAR_NUMBER_OF_CHANNELS, protocol.getTimeZone());
            if(from.before(profileBlock.getOldestProfileRecordDate())) {
                profileDataBlocks.add(profileBlock);
            }else{
                long diffInMillies = from.getTime() - profileBlock.getOldestProfileRecordDate().getTime();
                if(diffInMillies > 360000) {
                    index += diffInMillies /360000;
                }
            }
            index++;
        }
        if(profileDataBlocks.size() > 0) {
            return mergeProfileDataBlocks(profileDataBlocks, from, to);
        }else{
            return new ProfileData();
        }
    }

    private ProfileData mergeProfileDataBlocks(List<ProfileBlock> profileDataBlocks, Date from, Date to) {
        ProfileData profileData = new ProfileData();
        profileData.setChannelInfos(profileDataBlocks.get(0).getProfileData().getChannelInfos());   // The list will contain at least 1 profileBlock, so get(0) is always possible

        for (ProfileBlock profileDataBlock : profileDataBlocks) {
            for (IntervalData intervalData : profileDataBlock.getProfileData().getIntervalDatas()) {
               // if (intervalData.getEndTime().after(from) && intervalData.getEndTime().before(to)) {
                    profileData.addInterval(intervalData);  // Only add entries fitting within the time boundaries
                //}
            }
        }

        return profileData;
    }

    private ProfileBlock getLastProfileBlock() throws IOException {
        if(lastRecord == null) {
            lastRecord = (BigDecimal) protocol.getRegisterFactory().findRegister(PM5561RegisterFactory.LOAD_PROFILE_LAST_RECORD).value();
        }
        if (this.lastProfileBlock == null) {
            ReadGeneralReferenceRequest readGeneralReferenceRequest = protocol.getRegisterFactory().findRegister(PM5561RegisterFactory.LOAD_PROFILE_RECORD_ITEM1).getReadGeneralReferenceRequest(lastRecord.intValue());
            this.lastProfileBlock = new ProfileBlock(readGeneralReferenceRequest.getValues(), REGULAR_NUMBER_OF_CHANNELS, protocol.getTimeZone());
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
        if(loadProfileStatus == null) {
            loadProfileStatus = (BigDecimal) protocol.getRegisterFactory().findRegister(PM5561RegisterFactory.LOAD_PROFILE_STATUS).value();
        }
        return loadProfileStatus.intValue() == 1;
    }


}
