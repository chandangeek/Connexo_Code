package com.elster.protocolimpl.dlms.profile;

import com.elster.dlms.cosem.simpleobjectmodel.SimpleProfileObject;

import java.io.IOException;
import java.util.TimeZone;
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
    public static IArchiveProcessor createArchiveProcessor(final String meterType, final String archiveStructureString, final SimpleProfileObject profileObject, TimeZone timeZone, final Logger logger)
            throws IOException
    {
        IArchiveProcessor result = null;
        ArchiveStructure archiveStructure = null;

        if (meterType.equalsIgnoreCase("A1V1"))
        {
            result = new GeneralArchiveProcessor(timeZone, logger);
            result.prepare(profileObject, archiveStructureString);
            return result;
        }

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
        if (result != null)
        {
            result.prepare(profileObject, archiveStructure);
        }
        return result;
    }

    @SuppressWarnings({"unused"})
    public static ILogProcessor createLogProcessor(final String meterType, final String logStructureString, final SimpleProfileObject profileObject, final TimeZone timeZone, final Logger logger)
            throws IOException
    {
        if ((logStructureString == null) || (logStructureString.length() == 0))
        {
            throw new IOException("No definition for log profile structure.");
        }

        ILogProcessor result = null;

        if (meterType.equalsIgnoreCase("A1V1"))
        {
            result = new GeneralDlmsLogProcessor(timeZone, logger);
            result.prepare(profileObject, logStructureString);
            return result;
        }

        ArchiveStructure archiveStructure = new ArchiveStructure(logStructureString);

        if (!archiveStructure.hasTSTEntry() || !archiveStructure.hasEventEntry())
        {
            throw new IOException("Definition for log profile structure contains no TST and/or Event entry.");
        }

        final String tag = archiveStructure.getEventEntry().getTag().toUpperCase();
        if (tag.equals("LIS200"))
        {
            result = new Lis200LogProcessor(logger);
        } else if (tag.equals("DLMS"))
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