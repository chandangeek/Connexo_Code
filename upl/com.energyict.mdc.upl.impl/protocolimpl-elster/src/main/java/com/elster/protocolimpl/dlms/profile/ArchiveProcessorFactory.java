package com.elster.protocolimpl.dlms.profile;

import com.elster.dlms.cosem.simpleobjectmodel.SimpleProfileObject;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * User: heuckeg
 * Date: 04.09.12
 * Time: 14:39
 * <p/>
 * This class creates a new archive processor for a dlms archive
 */
public class ArchiveProcessorFactory
{
    public static IArchiveProcessor createArchiveProcessor(final String meterType, final String archiveStructureString, final SimpleProfileObject profileObject, final Logger logger)
            throws IOException
    {
        IArchiveProcessor result = null;
        ArchiveStructure archiveStructure = null;

        if (meterType.equalsIgnoreCase("EK280"))
        {
            if (archiveStructureString.length() == 0)
            {
                result = new ItalyArchiveProcessor(logger);
            } else
            {
                archiveStructure = new ArchiveStructure(archiveStructureString);
                if (archiveStructure.isLis200Equivalent())
                {
                    result = new Lis200EquivalentIntervalArchiveProcessor(logger);
                }
            }
        }
        if ((result == null) && (archiveStructureString.length() > 0))
        {
            result = new DlmsIntervalArchiveProcessor(logger);
        }
        if (result != null)
        {
            result.prepare(profileObject, archiveStructure);
        }
        return result;
    }

    public static ILogProcessor createLogProcessor(final String meterType, final String logStructureString, final SimpleProfileObject profileObject, final Logger logger)
            throws IOException
    {
        if ((logStructureString == null) || (logStructureString.length() == 0))
        {
            throw new IOException("No definition for log profile structure.");
        }

        ILogProcessor result = null;
        ArchiveStructure archiveStructure = new ArchiveStructure(logStructureString);

        if (!archiveStructure.hasTSTEntry() || !archiveStructure.hasEventEntry())
        {
            throw new IOException("Definition for log profile structure contains no TST and/or Event entry.");
        }

        if (archiveStructure.getEventEntry().getTag().equalsIgnoreCase("LIS200"))
        {
            result = new Lis200LogProcessor(logger);
        } else if (archiveStructure.getEventEntry().getTag().equalsIgnoreCase("DLMS"))
        {
            result = new DlmsLogProcessor(logger);
        }
        if (result != null)
        {
            result.prepare(profileObject, archiveStructure);
        }
        return result;
    }
}