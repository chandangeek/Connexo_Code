package com.elster.protocolimpl.dlms.profile;

import com.elster.dlms.types.basic.ObisCode;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: heuckeg
 * Date: 05.09.12
 * Time: 11:31
 */
@SuppressWarnings({"unused"})
public class ArchiveStructure
{
    private final static int TST_ENTRY = -1;
    private final static int EVT_ENTRY = -2;
    private final static int SYS_ENTRY = -3;

    private final TreeMap<Integer, ArchiveStructureEntry> entries = new TreeMap<Integer, ArchiveStructureEntry>();

    public ArchiveStructure(final String structureString)
    {
        String[] entryDefs = structureString.split(",");
        for (String entryDef : entryDefs)
        {
            ArchiveStructureEntry entry = ArchiveStructureEntry.parse(entryDef);
            entries.put(entry.channelNo, entry);
        }
    }

    public boolean hasTSTEntry()
    {
        return entries.get(TST_ENTRY) != null;
    }

    public ArchiveStructureEntry getTSTEntry()
    {
        return entries.get(TST_ENTRY);
    }

    public boolean hasEventEntry()
    {
        return entries.get(EVT_ENTRY) != null;
    }

    public ArchiveStructureEntry getEventEntry()
    {
        return entries.get(EVT_ENTRY);
    }

    public boolean hasSystemStateEntry()
    {
        return entries.get(SYS_ENTRY) != null;
    }

    public ArchiveStructureEntry getSystemStateEntry()
    {
        return entries.get(SYS_ENTRY);
    }

    public int channelCount()
    {
        return getChannelEntries().length;
    }

    public ArchiveStructureEntry getChannelEntry(int index)
    {
        return getChannelEntries()[index];
    }

    public ArchiveStructureEntry[] getChannelEntries()
    {

        ArrayList<ArchiveStructureEntry> result = new ArrayList<ArchiveStructureEntry>();

        for (Integer key : entries.keySet())
        {
            ArchiveStructureEntry entry = entries.get(key);
            if (entry.channelNo >= 0)
            {
                result.add(entry);
            }
        }
        return result.toArray(new ArchiveStructureEntry[result.size()]);
    }

    public boolean isLis200Equivalent()
    {
        return this.hasTSTEntry() && this.hasEventEntry() && this.getEventEntry().getTag().equalsIgnoreCase("LIS200");
    }


    public static class ArchiveStructureEntry
    {
        private final static Pattern ENTRY_PATTERN =
                Pattern.compile(
                        "(([Cc][Hh][Nn]([0-9]+)(\\[[Cc]([0-9])*?\\])??)|TST|EVT|EVT_L2|EVT_DLMS|SYS|EVT_UMI1)=(([0-9]+\\.){5}?[0-9]+)([Aa]([0-9]+))??");
        //                ^--- CHN n --------^                    or    TST|EVT|SYS
        //                               opt ^---- [C] or [Cn] ----^
        //                                             obis code     ^---- n.n.n.n.n.n ----^
        //                                                                              opt ^--- An ----^
        //EntryDefinition: <CHN2[C9]=1.2.3.4.5.255A2>        <TST=1.2.3.4.5.6A4>
        //groupCount: 9
        //     Group[1] = <CHN2[C9]>                         <TST>
        //     Group[2] = <CHN2[C9]>                         <null>
        // ->  Group[3] = <2>                                <null>
        // ->  Group[4] = <[C9]>                             <null>
        // ->  Group[5] = <9>                                <null>
        // ->  Group[6] = <1.2.3.4.5.255>                    <1.2.3.4.5.6>
        //     Group[7] = <5.>                               <5.>
        //     Group[8] = <A2>                               <A4>
        // ->  Group[9] = <2>                                <4>
        // channelNo = -1 - TST
        // channelNo = -2 - events
        // channelNo = -3 - system state
        // ? instance state
        private final int channelNo;
        private final boolean advance;
        private final int overflow;
        private final ObisCode obisCode;
        private final int attribute;
        private final String tag;

        public static ArchiveStructureEntry parse(final String entryDefinition)
        {
            final Matcher matcher = ENTRY_PATTERN.matcher(entryDefinition);

            if (!matcher.matches())
            {
                throw new IllegalArgumentException("No valid archive structure entry definition: " + entryDefinition);
            }

            int channelNo = 0;
            boolean advance = false;
            int overflow = 0;
            int attribute = 2;
            String tag = "";
            // type of entry
            if (matcher.group(3) == null)
            {
                //no standard channel
                String n = matcher.group(1);
                if (n.equalsIgnoreCase("TST"))
                {
                    channelNo = -1;
                } else if (n.equalsIgnoreCase("EVT"))
                {
                    channelNo = -2;
                    tag = "LIS200";
                } else if (n.equalsIgnoreCase("EVT_L2"))
                {
                    channelNo = -2;
                    tag = "LIS200";
                } else if (n.equalsIgnoreCase("EVT_DLMS"))
                {
                    channelNo = -2;
                    tag = "DLMS";
                } else if (n.equalsIgnoreCase("SYS"))
                {
                    channelNo = -3;
                    tag = "A1";
                } else if (n.equalsIgnoreCase("EVT_UMI1"))
                {
                    channelNo = -2;
                    tag = "A1";
                }
            } else
            {
                channelNo = Integer.parseInt(matcher.group(3));
                if (matcher.group(4) != null)
                {
                    advance = true;
                    if (matcher.group(5) != null)
                    {
                        overflow = Integer.parseInt(matcher.group(5));
                    } else
                    {
                        overflow = 8;
                    }
                }
            }
            ObisCode obisCode = new ObisCode(matcher.group(6));
            if (matcher.group(9) != null)
            {
                attribute = Integer.parseInt(matcher.group(9));
            }
            return new ArchiveStructureEntry(channelNo, advance, overflow, obisCode, attribute, tag);

/*
            System.out.println("EntryDefinition: <" + entryDefinition + ">");
            int j = matcher.groupCount();
            System.out.println("groupCount: " + j);
            for (int i = 1; i <= j; i++)
            {
                System.out.println("Group[" + i + "] = <" + matcher.group(i) + ">");
            }
*/
        }


        public ArchiveStructureEntry(final int channelNo, final boolean advance, final int overflow, final ObisCode obisCode, final int attribute)
        {
            this(channelNo, advance, overflow, obisCode, attribute, "");
        }

        public ArchiveStructureEntry(final int channelNo, final boolean advance, final int overflow, final ObisCode obisCode, final int attribute, final String tag)
        {
            this.channelNo = channelNo;
            this.advance = advance;
            this.overflow = overflow;
            this.obisCode = obisCode;
            this.attribute = attribute;
            this.tag = tag;
        }

        public int getChannelNo()
        {
            return channelNo;
        }

        public boolean isAdvance()
        {
            return advance;
        }

        public int getOverflow()
        {
            return overflow;
        }

        public ObisCode getObisCode()
        {
            return obisCode;
        }

        public int getAttribute()
        {
            return attribute;
        }

        public String getTag()
        {
            return tag;
        }

        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            if (channelNo >= 0)
            {
                sb.append("CHN");
                sb.append(channelNo);
            } else
            {
                switch (-channelNo)
                {
                    case 1:
                        sb.append("TST");
                        break;
                    case 2:
                        sb.append("EVT");
                        if ((tag.length() == 0) || tag.equalsIgnoreCase("LIS200"))
                        {
                            sb.append("_L2");
                        } else if (tag.equalsIgnoreCase("DLMS"))
                        {
                            sb.append("_DLMS");
                        }
                        break;
                    case 3:
                        sb.append("SYS");
                        break;
                    default:
                        sb.append("SP");
                        sb.append(channelNo);
                }
            }
            if (overflow > 0)
            {
                sb.append("[C");
                if (overflow != 8)
                {
                    sb.append(overflow);
                }
                sb.append("]");
            }
            sb.append("=");
            sb.append(obisCode.toString());
            sb.append("A");
            sb.append(attribute);

            return sb.toString();
        }
    }
}
