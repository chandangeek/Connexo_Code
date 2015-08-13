package com.elster.protocolimpl.dlms.messaging.utils;

import com.elster.dlms.cosem.classes.class11.SpecialDayEntry;
import com.elster.dlms.types.basic.DlmsDate;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by heuckeg on 22.05.2014.
 *
 */
public class SpecialDayList
{
    private static String DATE_PATTERN = "(20\\d\\d)?-(0?[1-9]|1[012])-([12][0-9]|3[01]|0?[1-9])";
    private static Pattern datePattern = Pattern.compile(DATE_PATTERN);

    public static ArrayList<SpecialDayEntry> parseSpecialDayList(Element rootElement) throws SAXException
    {
        ArrayList<SpecialDayEntry> result = new ArrayList<SpecialDayEntry>();

        if (!rootElement.getNodeName().equals("special_days_table"))
        {
            throw new SAXException("xml structure: missing <special_days_table>");
        }

        NodeList nodes = rootElement.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++)
        {
            Node node = nodes.item(i);
            if (node instanceof Element)
            {
                parseEntries(result, node);
            }
        }

        return result;
    }

    private static void parseEntries(ArrayList<SpecialDayEntry> result, Node node) throws SAXException
    {
        if (!node.getNodeName().equals("entries"))
        {
            throw new SAXException("xml structure: missing <entries>");
        }

        NodeList nodes = node.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++)
        {
            Node subNode = nodes.item(i);
            if (subNode instanceof Element)
            {
                parseEntry(result, subNode);
            }
        }
    }

    private static void parseEntry(ArrayList<SpecialDayEntry> result, Node node) throws SAXException
    {
        if (!node.getNodeName().equals("entry"))
        {
            throw new SAXException("xml structure: missing <entry>");
        }

        DlmsDate date = null;
        Integer dayId = null;
        NodeList nodes = node.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++)
        {
            Node subNode = nodes.item(i);
            if (subNode instanceof Element)
            {
                if (isEntryDate(subNode))
                {
                    String s = SaxUtils.getValue(subNode);
                    date = parseDlmsDate(s);
                }
                if (isEntryDayId(subNode))
                {
                    String s = SaxUtils.getValue(subNode);
                    dayId = Integer.parseInt(s);
                }
            }
        }

        if ((date == null) || (dayId == null))
        {
            throw new SAXException("xml structure: missing <date> or <day_id>");
        }

        int index = result.size() + 1;
        SpecialDayEntry entry = new SpecialDayEntry(index, date, dayId);
        result.add(entry);
    }

    private static DlmsDate parseDlmsDate(String s) throws SAXException
    {
        int[] date = parseDateString(s);
        return new DlmsDate(date[0], date[1], date[2]);
    }

    private static boolean isEntryDayId(Node node)
    {
        return node.getNodeName().equals("day_id");
    }

    private static boolean isEntryDate(Node node)
    {
        return node.getNodeName().equals("date");
    }

    protected static int[] parseDateString(final String s) throws SAXException
    {
        int[] result = new int[] {0xFFFF, 0xFF, 0xFF};
        Matcher match = datePattern.matcher(s);
        if (!match.matches())
        {
            throw new SAXException("xml structure: invalid date (" + s + ")");
        }
        for (int i = 0; i < 3; i++)
        {
            String g = match.group(i + 1);
            if (g != null)
            {
                result[i] = Integer.parseInt(g);
            }
        }
        return result;
    }

}
