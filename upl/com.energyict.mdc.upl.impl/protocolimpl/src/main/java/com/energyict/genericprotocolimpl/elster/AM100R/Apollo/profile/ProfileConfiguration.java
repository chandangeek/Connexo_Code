package com.energyict.genericprotocolimpl.elster.AM100R.Apollo.profile;

import com.energyict.obis.ObisCode;

import java.io.IOException;

/**
 * Defines the configuration of a Profile object.<br>
 * Contains:
 * <li> ProfileInterval
 * <li> nrOfProfileChannels
 * <li> ProfileObisCode
 * <li> Indicate which channels are mapped to which capturedObject
 */
public class ProfileConfiguration {

    private static final int ObisCodeIndex = 0;
    private static final int ChannelConfigIndex = 1;

    private final ObisCode defaultObisCode;
    private ObisCode profileObisCode;
    private int profileInterval;
    private String[] channelConfig;

    public ProfileConfiguration(final String defaultObisCode, final String profileConfig, final int profileInterval) throws IOException {
        this.defaultObisCode = ObisCode.fromString(defaultObisCode);
        this.profileInterval = profileInterval;
        parse(profileConfig);
    }

    private void parse(final String profileConfig) throws IOException {
        String[] config = profileConfig.split(":");

        if (config.length == 1) { // only an ObisCode
            this.profileObisCode = getProfileObisCodeFromString(profileConfig);
        } else if (config.length != 2) {
            throw new IOException("Invalid profile configuration : " + profileConfig);
        } else {
            this.profileObisCode = getProfileObisCodeFromString(config[ObisCodeIndex]);
            this.channelConfig = config[ChannelConfigIndex].split("-");
        }
    }

    /**
     * Get the profileObisCode from the given String. If the string is empty, then we use the {@link #defaultObisCode} instead.
     *
     * @param obisCode the obisCode in String-format to convert to an ObisCode
     * @return the desired ObisCode object
     * @throws IOException the of argument contained an invalid obisCode structure
     */
    private ObisCode getProfileObisCodeFromString(final String obisCode) throws IOException {
        try {
            if (obisCode.equalsIgnoreCase("")) {
                return this.defaultObisCode;
            } else {
                return ObisCode.fromString(obisCode);
            }
        } catch (IllegalArgumentException e) {
            throw new IOException("Invalid profile ObisCode : " + obisCode);
        }
    }

    public ObisCode getProfileObisCode() {
        return profileObisCode;
    }

    public int getProfileInterval() {
        return profileInterval;
    }

    public int getNrOfChannels() {
        if(this.channelConfig == null){
            return 0;
        }
        return this.channelConfig.length;
    }

    /**
     * Get the CapturedObject dataChannelIndex(zero based) for channel with the given index <b>(ZERO BASED)</b>
     *
     * @param index the zero based channelIndex
     * @return the zero base capturedObject ChannelIndex
     * @throws IOException of the channelConfig contained invalid data or if the requested index is out of range ...
     */
    public int getCapturedObjectIndexForChannel(final int index) throws IOException {
        if(this.channelConfig == null){
            return index;
        }
        try {
            if(index >= this.channelConfig.length){
                throw new IOException("Invalid channelIndex  (" + index + "), config has only " + this.channelConfig.length + " channels.");
            }
            return Integer.valueOf(this.channelConfig[index]) - 1;  // for zero basing the capturedObjectChannelIndex
        } catch (NumberFormatException e) {
            throw new IOException("Invalid channelConfiguration : " + this.channelConfig + " contains non-integer values.");
        }
    }
}
