package com.energyict.protocolimpl.modbus.schneider.powerlogic.profile;

import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.ProfileData;
import com.energyict.protocolimpl.modbus.core.functioncode.ReadFileRecordRequest;
import com.energyict.protocolimpl.modbus.schneider.powerlogic.PM5561;
import com.energyict.protocolimpl.modbus.schneider.powerlogic.PM5561RegisterFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class ProfileBuilder {

    private static final int DATA_LOG_FILE_RECORD_NUMBER = 1;
    private static final int NUMBER_OF_SECONDS_PER_MINUTE = 60;

    int profileInterval = -1;
    int loadProfileStatus = -1;
    int lastRecordIndex = -1;
    int firstRecordIndex = -1;
    int recordLength = -1;
    int fileSize = -1;
    int fileStatus = -1;
    int numberOfRecords = -1;
    int[] loadProfileRecordItems;
    PM5561 protocol;

    public ProfileBuilder(PM5561 protocol) {
        this.protocol = protocol;
    }

    public int getProfileInterval() throws IOException {
        if (profileInterval == -1) {
            profileInterval = ((BigDecimal) protocol.getRegisterFactory().findRegister(PM5561RegisterFactory.LOAD_PROFILE_INTERVAL_CONTROL_MINUTES).value()).intValue() * NUMBER_OF_SECONDS_PER_MINUTE;
        }
        return profileInterval;
    }

    public int getNumberOfChannels() throws IOException {
        return getChannelInfos().size();
    }

    public List<ChannelInfo> getChannelInfos() throws IOException {
        List<ChannelInfo> channelInfos = new ArrayList<>();
        for (int i : getLoadProfileRecordItems()) {
            if (i != 0) {
                channelInfos.add(
                        new ChannelInfo(
                                channelInfos.size(),
                                "Channel " + (channelInfos.size() + 1),
                                ChannelConfigMapping.findChannelConfigurationFor(i).getUnit(),
                                "",
                                ChannelConfigMapping.findChannelConfigurationFor(i).getUnit().isVolumeUnit()
                        )
                );
            }
        }
        return channelInfos;
    }

    public ProfileData getProfileData(Date from, Date to, boolean generateEvents) throws IOException {
        // Calculate the index from which you should start reading
        int numberOfRecordsToRetrieve = calculateNumberOfRecordsToRetrieve(from, to);
        int index = getLastRecordIndex() - numberOfRecordsToRetrieve;
        if (index < 0) {
            index += getFileSize();
        }

        ProfileBlock profileBlock;
        List<ProfileBlock> profileDataBlocks = new ArrayList<>();
        ReadFileRecordRequest readFileRecordRequest;
        for (int i = 0; i <= numberOfRecordsToRetrieve; i++) {
            readFileRecordRequest = protocol.getRegisterFactory().getFunctionCodeFactory().readFileRecordRequest(DATA_LOG_FILE_RECORD_NUMBER, index, getRecordLength());
            profileBlock = new ProfileBlock(readFileRecordRequest.getValues(), getLoadProfileRecordItems());
            profileDataBlocks.add(profileBlock);

            index++;
            if (index >= getFileSize()) {    // Rolling buffer principle
                index = 0;
            }
        }

        return mergeProfileDataBlocks(profileDataBlocks, from, to);
    }

    private int calculateNumberOfRecordsToRetrieve(Date from, Date to) throws IOException {
        long numberOfSeconds = (to.getTime() - from.getTime())/1000;
        int numberOfRecords = (int) (numberOfSeconds / getProfileInterval()) + 1;
        if (numberOfRecords > getNumberOfRecordsInFile()) {   // Don't request more records than available
            return getNumberOfRecordsInFile();
        }
        return numberOfRecords;
    }

    private ProfileData mergeProfileDataBlocks(List<ProfileBlock> profileDataBlocks, Date from, Date to) throws IOException {
        ProfileData profileData = new ProfileData();
        profileData.setChannelInfos(getChannelInfos());   // The list will contain at least 1 profileBlock, so get(0) is always possible

        for (ProfileBlock profileDataBlock : profileDataBlocks) {
            if (profileDataBlock.getIntervalData().getEndTime().after(from) && profileDataBlock.getIntervalData().getEndTime().before(to)) {
                profileData.addInterval(profileDataBlock.getIntervalData());  // Only add entries fitting within the time boundaries
            }
        }
        return profileData;
    }

    private int getLastRecordIndex() throws IOException {
        if(lastRecordIndex == -1) {
            lastRecordIndex = ((BigDecimal) protocol.getRegisterFactory().findRegister(PM5561RegisterFactory.LOAD_PROFILE_LAST_RECORD).value()).intValue();
        }
        return lastRecordIndex;
    }

    private int getFirstRecordIndex() throws IOException {
        if(firstRecordIndex == -1) {
            firstRecordIndex = ((BigDecimal) protocol.getRegisterFactory().findRegister(PM5561RegisterFactory.LOAD_PROFILE_FIRST_RECORD).value()).intValue();
        }
        return firstRecordIndex;
    }

    private int getFileSize() throws IOException {
        if (fileSize == -1) {
            fileSize = ((BigDecimal) protocol.getRegisterFactory().findRegister(PM5561RegisterFactory.LOAD_PROFILE_ALLOCATED_FILE_SIZE).value()).intValue();
        }
        return fileSize;
    }

    private int getRecordLength() throws IOException {
        if(recordLength == -1) {
            recordLength = ((BigDecimal) protocol.getRegisterFactory().findRegister(PM5561RegisterFactory.LOAD_PROFILE_ALLOCATED_RECORD_SIZE).value()).intValue();
            System.out.println(recordLength);
            recordLength = 38;
        }
        return recordLength;
    }

    private int getFileStatus() throws IOException {
        if(fileStatus == -1) {
            fileStatus = ((BigDecimal) protocol.getRegisterFactory().findRegister(PM5561RegisterFactory.LOAD_PROFILE_FILE_STATUS).value()).intValue();
        }
        return fileStatus;
    }

    private int getNumberOfRecordsInFile() throws IOException {
        if(numberOfRecords == -1) {
            numberOfRecords = ((BigDecimal) protocol.getRegisterFactory().findRegister(PM5561RegisterFactory.LOAD_PROFILE_NUMBER_OF_RECORDS).value()).intValue();
        }
        return numberOfRecords;
    }

    public boolean isSupported() throws IOException {
        if(loadProfileStatus == -1) {
            loadProfileStatus = ((BigDecimal) protocol.getRegisterFactory().findRegister(PM5561RegisterFactory.LOAD_PROFILE_STATUS).value()).intValue();
        }
        return loadProfileStatus == 1;
    }

    public int[] getLoadProfileRecordItems() throws IOException {
        if(loadProfileRecordItems == null) {
            loadProfileRecordItems = protocol.getRegisterFactory().findRegister(PM5561RegisterFactory.LOAD_PROFILE_RECORD_ITEMS).values();
        }
        return loadProfileRecordItems;
    }
}