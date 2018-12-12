package com.elster.protocolimpl.dlms.profile.entrymgmt;

import com.elster.dlms.cosem.classes.class03.Unit;
import com.elster.dlms.types.basic.ObisCode;
import com.elster.protocolimpl.dlms.profile.api.IArchiveLineChecker;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: heuckeg
 * Date: 15.04.13
 * Time: 11:53
 */
public class ArchiveStructureFactory
{

    private final static Pattern ENTRY_PATTERN =
            Pattern.compile(
                    "(([Cc][Hh][Nn]([0-9]+)(\\[([Cc]){0,1}([0-9]+){0,1}([Ss]:-?[0-9]){0,1}([Uu]:[_0-9a-zA-Z]+){0,1}\\])??)|TST|EVT([_0-9A-Z]*?)|SYS([_0-9A-Z]*?))=(([0-9]+\\.){5}?[0-9]+)([Aa]([0-9]+))??");
    //                    "(([Cc][Hh][Nn]([0-9]+)(\\[[Cc]([0-9])*?\\])??)|TST|EVT([_0-9A-Z]*?)|SYS([_0-9A-Z]*?))=(([0-9]+\\.){5}?[0-9]+)([Aa]([0-9]+))??");
    //                ^--- CHN n --------^                    or    TST|EVT|SYS
    //                               opt ^---- [C] or [Cn] ----^
    //                                             obis code     ^---- n.n.n.n.n.n ----^
    //                                                                              opt ^--- An ----^
    //EntryDefinition: <CHN2[C9]=1.2.3.4.5.255A2>  <TST=1.2.3.4.5.6A4> <EVT_DLMS=1.2.3.4.5.6A4 <CHN2[c9s:-3u:m3]=1.2.3.4.5.6A2>
    //groupCount: 9
    //     Group[1] = <CHN2[C9]>                   <TST>               <EVT_DLMS>              <CHN2[C9s:-3u:m3]>
    //     Group[2] = <CHN2[C9]>                   <null>              <null>                  <CHN2[C9s:-3u:m3]>
    // ->  Group[3] = <2>                          <null>              <null>                  <2>
    // ->  Group[4] = <[C9]>                       <null>              <null>                  <[C9s:-3u:m3]>
    // ->  Group[5] = <C>                          <null>              <null>                  <C>
    // ->  Group[6] = <9>                          <null>              <null>                  <9>
    //     Group[7] = <null>                       <null>              <null>                  <s:-3>
    //     Group[8] = <null>                       <null>              <null>                  <u:m3>
    // ->  Group[9] = <null>                       <null>              _DLMS                   <null>
    // ->  Group[10] = <null>                      <null>              <null>                  <null>
    // ->  Group[11] = <1.2.3.4.5.255>             <1.2.3.4.5.6>       <1.2.3.4.5.6>           <1.2.3.4.5.6>
    // ->  Group[12] = <.5>                        <.5>                <.5>                    <5.>
    // ->  Group[13] = <A2>                        <A4>                <A4>                    <A2>
    // ->  Group[14] = <2>                         <4>                 <4>                     <2>
    //
    //
    // channelNo = -1 - TST
    // channelNo = -2 - events
    // channelNo = -3 - system state
    // ? instance state
    private static final int ENTRYNAMEGROUP = 1;
    private static final int CHANNELNOGROUP = 3;
    private static final int ISADVANCEDGROUP = 5;
    private static final int OVERFLOWGROUP = 6;
    private static final int SCALERGROUP = 7;
    private static final int UNITGROUP = 8;
    private final static int OBISCODEGROUP = 11;
    private final static int ATTRIBUTEGROUP = 14;

    public static AbstractArchiveEntry parseArchiveStructureDefinition(final String entryDefinition, HashMap<String, IArchiveLineChecker> checkerList)
    {
        final Matcher matcher = ENTRY_PATTERN.matcher(entryDefinition);

        if (!matcher.matches())
        {
            throw new IllegalArgumentException("No valid archive structure entry definition: " + entryDefinition);
        }

        //System.out.println("EntryDefinition: <" + entryDefinition + ">");
        //int j = matcher.groupCount();
        //System.out.println("groupCount: " + j);
        //for (int i = 1; i <= j; i++)
        //{
        //    System.out.println("Group[" + i + "] = <" + matcher.group(i) + ">");
        //}

        ObisCode obisCode = new ObisCode(matcher.group(OBISCODEGROUP));
        int attribute = (matcher.group(ATTRIBUTEGROUP) == null) ? 2 : Integer.parseInt(matcher.group(ATTRIBUTEGROUP));

        // type of entry
        if (matcher.group(CHANNELNOGROUP) != null)
        {
            // is channel entry
            int overflow = 0;
            boolean advance = false;
            int channelNo = Integer.parseInt(matcher.group(CHANNELNOGROUP));
            if (matcher.group(ISADVANCEDGROUP) != null)
            {
                advance = true;
                if (matcher.group(OVERFLOWGROUP) != null)
                {
                    overflow = Integer.parseInt(matcher.group(OVERFLOWGROUP));
                } else
                {
                    overflow = 8;
                }
            }
            ChannelArchiveEntry entry = new ChannelArchiveEntry(obisCode, attribute, channelNo, advance, overflow);
            String st = matcher.group(SCALERGROUP);
            if (st != null)
            {
                int i = Integer.parseInt(st.substring(2));
                entry.setScaler(i);
            }
            st = matcher.group(UNITGROUP);
            if (st != null)
            {
                st = st.substring(2);
                try
                {
                    int unitId = Integer.parseInt(st);
                    Unit u = Unit.findById(unitId);
                    entry.setUnit(u);
                }
                catch (NumberFormatException ex)
                {
                    st = st.replace("3", "³");
                    st = st.replace("2", "²");
                    for (Unit u : Unit.values())
                    {
                        if (st.equalsIgnoreCase(u.getDisplayName()))
                        {
                            entry.setUnit(u);
                            break;
                        }
                    }
                }
            }
            return entry;
        }

        //non standard channel
        String n = matcher.group(ENTRYNAMEGROUP).toUpperCase();
        if (n.equals("TST"))
        {
            return new TimeStampEntry(obisCode, attribute);
        }

        IArchiveLineChecker check = checkerList.get(n);
        if (check == null)
        {
            throw new IllegalArgumentException("No valid evt definition in archive structure: " + n);
        }
        return new CheckingArchiveEntry(obisCode, attribute, n, check);
    }
}
