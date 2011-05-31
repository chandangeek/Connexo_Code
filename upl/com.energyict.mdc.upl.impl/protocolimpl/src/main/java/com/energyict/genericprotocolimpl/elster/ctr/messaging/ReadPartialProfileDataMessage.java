package com.energyict.genericprotocolimpl.elster.ctr.messaging;

import com.energyict.cbo.BusinessException;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRException;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRExceptionWithProfileData;
import com.energyict.genericprotocolimpl.elster.ctr.profile.ProfileChannel;
import com.energyict.mdw.core.Channel;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.utils.MessagingTools;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.util.*;

/**
 * Copyrights EnergyICT
 * Date: 23/02/11
 * Time: 9:33
 */
public class ReadPartialProfileDataMessage extends AbstractMTU155Message {

    private static final String MESSAGE_TAG = "Read_Partial_Profile_Data";
    private static final String MESSAGE_DESCRIPTION = "Read partial profile data";
    private static final String ATTR_FROM_DATE = "FromDate";
    private static final String ATTR_TO_DATE = "ToDate";
    private static final String ATTR_LOAD_PROFILE_IDS = "LoadProfileIDs";

    public ReadPartialProfileDataMessage(MTU155MessageExecutor messageExecutor) {
        super(messageExecutor);
    }

    @Override
    public boolean canExecuteThisMessage(MessageEntry messageEntry) {
        return isMessageTag(MESSAGE_TAG, messageEntry.getContent());
    }

    @Override
    public void executeMessage(MessageEntry messageEntry) throws BusinessException {
        String fromDateAsString = MessagingTools.getContentOfAttribute(messageEntry, ATTR_FROM_DATE);
        String toDateAsString = MessagingTools.getContentOfAttribute(messageEntry, ATTR_TO_DATE);
        String loadProfileIDs = MessagingTools.getContentOfAttribute(messageEntry, ATTR_LOAD_PROFILE_IDS);
        String epochFrom = ProtocolTools.getEpochTimeFromString(fromDateAsString);
        String epochTo = ProtocolTools.getEpochTimeFromString(toDateAsString);
        validateParameters(epochFrom, epochTo, loadProfileIDs);

        int[] profileIDs;
        if (loadProfileIDs != null) {
            String[] strings = loadProfileIDs.split(",");
            profileIDs = new int[strings.length];
            for (int i = 0; i < strings.length; i++) {
                String string = strings[i];
                try {
                    profileIDs[i] = Integer.valueOf(string);
                } catch (NumberFormatException e) {
                    profileIDs[i] = -1;
                }
            }

        } else {
            profileIDs = new int[0];
        }

        Calendar fromCalendar = Calendar.getInstance(getTimeZone());
        fromCalendar.setTimeInMillis(Long.valueOf(epochFrom) * 1000);
        Calendar toCalendar = Calendar.getInstance(getTimeZone());
        toCalendar.setTimeInMillis(Long.valueOf(epochTo) * 1000);
        getLogger().severe("ReadPartialProfileDataMessage fromCalendar=" + fromCalendar.getTime() + ", toCalendar=" + toCalendar.getTime());

        readPartialProfileData(fromCalendar, toCalendar, profileIDs);
    }

    /**
     * @return
     */
    private TimeZone getTimeZone() {
        TimeZone timeZone = getFactory().getTimeZone();
        return timeZone == null ? TimeZone.getDefault() : timeZone;
    }

    private void validateParameters(String epochFrom, String epochTo, String loadProfileIDs) throws BusinessException {
        if (!ProtocolTools.isNumber(epochFrom)) {
            throw new BusinessException("Invalid fromDate [" + epochFrom + "]!");
        }
        if (!ProtocolTools.isNumber(epochTo)) {
            throw new BusinessException("Invalid toDate [" + epochTo + "]!");
        }

        if (loadProfileIDs != null) {
            String[] strings = loadProfileIDs.split(",");
            for (int i = 0; i < strings.length; i++) {
                String string = strings[i];
                try {
                    Integer.valueOf(string);
                } catch (NumberFormatException e) {
                    throw new BusinessException("Invalid load profile ID [" + string + "] in id's [" + loadProfileIDs + "]!");
                }
            }
        }

    }

    private void readPartialProfileData(Calendar fromCalendar, Calendar toCalendar, int[] profileIDs) {
        List<Channel> channelList = getRtu().getChannels();
        for (Channel channel : channelList) {
            if (needToRead(channel.getLoadProfileIndex(), profileIDs)) {
                try {
                    ProfileChannel profile = new ProfileChannel(getFactory(), channel, fromCalendar, toCalendar);
                    getLogger().info("Reading profile for channel [" + channel.getName() + "]");
                    ProfileData pd = null;
                    try {
                        pd = profile.getProfileData();
                    } catch (CTRExceptionWithProfileData e) {
                        pd = e.getProfileData();
                        if (pd != null) {
                            getStoreObject().add(channel, pd);
                        }
                        throw e;
                    }
                    getStoreObject().add(channel, pd);
                } catch (CTRException e) {
                    getLogger().warning("Unable to read channelValues for channel [" + channel.getName() + "]" + e.getMessage());
                }
            } else {
                getLogger().info("Skipping profile for channel [" + channel.getName() + "]");
            }
        }
    }

    private boolean needToRead(int loadProfileId, int[] profileIDs) {
        for (int profileID : profileIDs) {
            if (loadProfileId == profileID) {
                return true;
            }
        }
        return false;
    }

    public static MessageSpec getMessageSpec(boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(MESSAGE_DESCRIPTION, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(MESSAGE_TAG);
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" ");
        tagSpec.add(new MessageAttributeSpec(ATTR_FROM_DATE, true));
        tagSpec.add(new MessageAttributeSpec(ATTR_TO_DATE, true));
        tagSpec.add(new MessageAttributeSpec(ATTR_LOAD_PROFILE_IDS, false));
        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

}
