/**
 *
 */
package com.elster.protocolimpl.dlms.profile;

import com.elster.dlms.cosem.simpleobjectmodel.SimpleProfileObject;
import com.elster.protocolimpl.dlms.util.ProtocolLink;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * @author gh
 * @since 5/26/2010
 */
public class DlmsProfile
{
    private final IArchiveProcessor archiveProcessor;

    /**
     * Default constructor
     *
     * @param link - reference to ProtocolLink
     */
    public DlmsProfile(final ProtocolLink link, final String meterType, final String archiveStructure, final SimpleProfileObject profile)
            throws IOException
    {
        archiveProcessor = ArchiveProcessorFactory.createArchiveProcessor(meterType, archiveStructure, profile, link.getTimeZone(), link.getLogger());
    }

    /**
     * @return the number of channels
     * @throws java.io.IOException - in case of an io error
     */
    public int getNumberOfChannels()
            throws IOException
    {
        return archiveProcessor.getNumberOfChannels();
    }

    /**
     * @return the interval of the Profile
     * @throws java.io.IOException when something happens during the read
     */
    public int getInterval()
            throws IOException
    {
        return archiveProcessor.getInterval();
    }

    /**
     * Construct the channelInfos
     *
     * @return a list of {@link com.energyict.protocol.ChannelInfo}s
     * @throws java.io.IOException if an error occurred during the read of the
     *                             {@link com.energyict.protocol.ChannelInfo}s
     */
    public List<ChannelInfo> buildChannelInfo()
            throws IOException
    {
        return archiveProcessor.buildChannelInfo();
    }

    /**
     * Get interval data within the request period
     *
     * @param from - the initial date for the interval data
     * @param to   - the end date for the interval data
     * @return the requested interval data
     * @throws java.io.IOException when reading of the data failed
     */
    public List<IntervalData> getIntervalData(Date from, Date to)
            throws IOException
    {
        return archiveProcessor.getIntervalData(from, to);
    }

}
