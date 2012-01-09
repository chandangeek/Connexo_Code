package com.elster.genericprotocolimpl.dlms.ek280.executors;

import com.energyict.protocol.ProfileData;

import java.io.IOException;
import java.util.Date;

/**
 * Copyrights
 * Date: 9/06/11
 * Time: 13:57
 */
public class ProfileTaskExecuter extends AbstractExecutor<ProfileTaskExecuter.ProfileTask> {

    public ProfileTaskExecuter(AbstractExecutor executor) {
        super(executor);
    }

    @Override
    public void execute(ProfileTask profileTask) throws IOException {
        validateNumberOfChannels(profileTask);
        readProfileData(profileTask);
    }

    private void readProfileData(ProfileTask profileTask) throws IOException {
        Date lastReading = getRtu().getLastReading();
        if (lastReading == null) {
            throw new IOException("Last reading is null. Unable to read profile data.");
        }
        getLogger().info("Retrieve interval data from " + (new java.util.Date(lastReading.getTime())) + " to " + new Date());
        ProfileData profileData = getDlmsProtocol().getProfileData(lastReading, profileTask.isReadMeterEvents());
        if (profileData != null) {
            profileData.sort();
            getStoreObject().add(profileData, getRtu());
        } else {
            severe("Profile data was 'null'!");
        }
    }

    private void validateNumberOfChannels(ProfileTask profileTask) throws IOException {
        int chCountEIS = getRtu().getChannels().size();
        int chCountRtu = getDlmsProtocol().getNumberOfChannels();
        boolean wrongNumberOfChannels = chCountRtu != chCountEIS;
        if (wrongNumberOfChannels) {
            String message = "Channel mismatch! Device in EIServer has [" + chCountEIS + "] channels, but the protocol reported only [" + chCountRtu + "] channels in the device.";
            getLogger().severe(message);
            if (profileTask.isFailOnChannelMismatch()) {
                throw new IOException(message);
            }
        }
    }

    protected static class ProfileTask {

        private final boolean failOnChannelMismatch;
        private final boolean readMeterEvents;

        protected ProfileTask(boolean failOnChannelMismatch, boolean readMeterEvents) {
            this.failOnChannelMismatch = failOnChannelMismatch;
            this.readMeterEvents = readMeterEvents;
        }

        public boolean isFailOnChannelMismatch() {
            return failOnChannelMismatch;
        }

        public boolean isReadMeterEvents() {
            return readMeterEvents;
        }

    }
}
