package com.elster.protocolimpl.dlms.profile;

import com.elster.dlms.cosem.simpleobjectmodel.SimpleProfileObject;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * User: heuckeg
 * Date: 04.09.12
 * Time: 14:43
 */
public interface IArchiveProcessor
{
    public void prepare(SimpleProfileObject profileObject, Object archiveStructure) throws IOException;
    public int getNumberOfChannels() throws IOException;
    public int getInterval() throws IOException;
    public List<ChannelInfo> buildChannelInfo() throws IOException;
    public List<IntervalData> getIntervalData(final Date from, final Date to) throws IOException;
}
