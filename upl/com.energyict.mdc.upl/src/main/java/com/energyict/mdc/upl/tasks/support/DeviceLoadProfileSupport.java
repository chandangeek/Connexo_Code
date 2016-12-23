package com.energyict.mdc.upl.tasks.support;

import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.issue.Issue;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.LoadProfileReader;

import java.util.List;

/**
 * Provides functionality to collect <code>LoadProfiles</code> from a Device.
 */
public interface DeviceLoadProfileSupport extends DeviceBasicTimeSupport {

    /**
     * This should be the standard <code>ObisCode</code> to use for a single profile on a legacy Protocol.
     * The B-field of the ObisCode should be replaced by the interval (in seconds) of the LoadProfile
     */
    public ObisCode GENERIC_LOAD_PROFILE_OBISCODE = ObisCode.fromString("0.0.99.1.0.255");

    /**
     * This should be the standard <code>ObisCode</code> to use for a channel referring to
     * a channel of an old legacy protocol.
     * The X in the ObisCode should be replaced by the channelIndex
     */
    public ObisCode GENERIC_CHANNEL_OBISCODE = ObisCode.fromString("0.x.128.0.0.255");

    /**
     * <b>Note:</b> This method is only called by the Collection Software if the option to "fail if channel configuration mismatch" is
     * checked on the <code>CommunicationProfile</code>
     * <p/>
     * Get the configuration(interval, number of channels, channelUnits) of all given <code>LoadProfiles</code> from the Device.
     * Build up a list of <CODE>LoadProfileConfiguration</CODE> objects and return them so the framework can validate them to the configuration
     * in EIServer.
     * <p/>
     * If a <code>LoadProfile</code> is not supported, the corresponding boolean in the <code>LoadProfileConfiguration</code> should be set to false.
     *
     *
     * @param loadProfilesToRead the <CODE>List</CODE> of <CODE>LoadProfileReaders</CODE> to indicate which profiles will be read
     * @return a list of <CODE>LoadProfileConfiguration</CODE> objects corresponding with the meter
     */
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead);

    /**
     * Collect one or more LoadProfiles from a device. Each <CODE>LoadProfileReader</CODE> contains a list of necessary
     * channels({@link com.energyict.protocol.LoadProfileReader#channelInfos}) to read. If it is possible then only these channels should be read,
     * if not then all channels may be returned.If {@link LoadProfileReader#channelInfos} contains an empty list
     * or null, then all channels from the corresponding LoadProfile should be fetched
     * <p/>
     * If for a certain <code>LoadProfile</code> not all data since {@link com.energyict.protocol.LoadProfileReader#getStartReadingTime() lastReading}
     * can be returned, then a proper {@link ResultType resultType} <b>and</b> {@link Issue issue}
     * should be set so proper logging of this action can be performed.
     * <p/>
     * In essence, the size of the returned <code>List</code> should be the same as the size of the given argument <code>List</code>.
     *
     * @param loadProfiles a list of <CODE>LoadProfileReader</CODE> which have to be read
     * @return a list of <CODE>CollectedLoadProfile</CODE> objects containing interval records
     */
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles);

}
