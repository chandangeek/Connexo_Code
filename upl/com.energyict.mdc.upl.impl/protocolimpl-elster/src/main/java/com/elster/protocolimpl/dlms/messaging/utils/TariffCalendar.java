package com.elster.protocolimpl.dlms.messaging.utils;

import com.elster.dlms.cosem.classes.class20.DayProfile;
import com.elster.dlms.cosem.classes.class20.WeekProfile;
import com.elster.dlms.types.basic.DlmsTime;
import com.elster.dlms.types.basic.ObisCode;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by heuckeg on 23.05.2014.
 *
 */
@SuppressWarnings("unused")
public class TariffCalendar
{
    private static String TIME_PATTERN = "^([01]?\\d|[2][0-3]):([0-5]\\d)(:[0-5]\\d)?";
    private static Pattern timePattern = Pattern.compile(TIME_PATTERN);

    private String name = "";
    private final List<WeekProfile> weekProfiles = new ArrayList<WeekProfile>();
    private final List<DayProfile> dayProfiles = new ArrayList<DayProfile>();
    private final ObisCode scriptLogicalName;

    private TariffCalendar(final ObisCode scriptLogicalName)
    {
        this.scriptLogicalName = scriptLogicalName;
    }

    public List<WeekProfile> getWeekProfiles()
    {
        return weekProfiles;
    }

    public List<DayProfile> getDayProfiles()
    {
        return dayProfiles;
    }

    public String getName()
    {
        return name;
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append("-");
        for (WeekProfile wp: weekProfiles)
            sb.append(wp.toString());
        for (DayProfile dp: dayProfiles)
            sb.append(dp.toString());
        return sb.toString();
    }

    public static TariffCalendar parseTariffCalendar(Element rootElement, ObisCode scriptLogicalName) throws SAXException
    {
        TariffCalendar cal = new TariffCalendar(scriptLogicalName);

        cal.parse(rootElement);

        if (cal.weekProfiles.size() != 1)
            throw new SAXException("xml structure: amount of week profiles not equal 1");

        if (cal.dayProfiles.size() != 3)
            throw new SAXException("xml structure: amount of day profiles not equal 3");

        return cal;
    }

    private void parse(Element rootElement) throws SAXException
    {
        if (!rootElement.getNodeName().equalsIgnoreCase("calendar"))
        {
            throw new SAXException("xml structure: missing <calendar>");
        }

        NodeList nodes = rootElement.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++)
        {
            Node node = nodes.item(i);
            if (node instanceof Element)
            {
                if (isCalendarName(node))
                {
                    name = SaxUtils.getValue(node);
                } else if (isCalendarWeekProfile(node))
                {
                    parseWeekProfile((Element) node);
                } else if (isCalendarDailySchedule(node))
                {
                    parseDailySchedule((Element) node);
                }
            }
        }

    }

    private static String[] weekdays = new String[]{
            "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

    private void parseWeekProfile(Element node) throws SAXException
    {
        String name = node.hasAttribute("name") ? node.getAttribute("name") : "";

        Integer[] weekdayId = new Integer[7];

        NodeList nodes = node.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++)
        {
            Node subNode = nodes.item(i);
            if (subNode instanceof Element)
            {
                String nodeName = subNode.getNodeName();
                for (int j = 0; j < weekdays.length; j++)
                {
                    if (weekdays[j].equalsIgnoreCase(nodeName))
                    {
                        weekdayId[j] = Integer.parseInt(SaxUtils.getValue(subNode));
                    }
                }
            }
        }

        for (Integer i : weekdayId)
        {
            if (i == null)
            {
                throw new SAXException("xml structure: week_profile missing a day");
            }
        }

        weekProfiles.add(new WeekProfile(name, weekdayId[0], weekdayId[1], weekdayId[2],
                weekdayId[3], weekdayId[4], weekdayId[5], weekdayId[6]));
    }

    private void parseDailySchedule(Element node) throws SAXException
    {
        Integer dayId = null;
        ArrayList<DayProfile.DayProfileAction> schedules = new ArrayList<DayProfile.DayProfileAction>();

        NodeList nodes = node.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++)
        {
            Node subNode = nodes.item(i);
            if (subNode instanceof Element)
            {
                if (isDailyScheduleDayId(subNode))
                {
                    dayId = Integer.parseInt(SaxUtils.getValue(subNode));
                } else if (isDailyScheduleSchedules(subNode))
                {
                    parseDailyScheduleSchedules(schedules, (Element) subNode);
                }
            }
        }

        if (dayId == null)
        {
            throw new SAXException("xml structure: daily schedule missing day id");
        }
        if (schedules.size() == 0)
        {
            throw new SAXException("xml structure: daily schedule missing any schedule");
        }
        if (schedules.size() > 5)
        {
            throw new SAXException("xml structure: daily schedule: to many schedules (5 max.)");
        }

        while (schedules.size() < 5)
        {
            schedules.add(new DayProfile.DayProfileAction(DlmsTime.NOT_SPECIFIED_TIME, scriptLogicalName, 0));
        }
        DayProfile dayProfile = new DayProfile(dayId, schedules);
        dayProfiles.add(dayProfile);
    }

    private void parseDailyScheduleSchedules(ArrayList<DayProfile.DayProfileAction> schedules, Element schedulesNode) throws SAXException
    {
        NodeList nodes = schedulesNode.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++)
        {
            Node subNode = nodes.item(i);
            if (subNode instanceof Element)
            {
                if (isSchedulesSchedule(subNode))
                {
                    parseSchedulesSchedule(schedules, subNode);
                }
            }
        }
    }

    private void parseSchedulesSchedule(ArrayList<DayProfile.DayProfileAction> schedules, Node scheduleNode) throws SAXException
    {
        DlmsTime time = null;
        Integer tariff = null;
        NodeList nodes = scheduleNode.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++)
        {
            Node subNode = nodes.item(i);
            if (subNode instanceof Element)
            {
                if (isScheduleTime(subNode))
                {
                    String timeString = SaxUtils.getValue(subNode);
                    byte[] timeData = parseTimeString(timeString);
                    time = new DlmsTime(timeData);

                } else if (isScheduleTariff(subNode))
                {
                    tariff = Integer.parseInt(SaxUtils.getValue(subNode));
                }
            }
        }

        if ((time == null) || (tariff == null))
        {
            throw new SAXException("xml structure: daily schedule definition incomplete");
        }
        schedules.add(new DayProfile.DayProfileAction(time, scriptLogicalName, tariff));

    }

    private static boolean isScheduleTariff(Node node)
    {
        return node.getNodeName().equalsIgnoreCase("tariff");
    }

    private static boolean isScheduleTime(Node node)
    {
        return node.getNodeName().equalsIgnoreCase("time");
    }

    private static boolean isSchedulesSchedule(Node node)
    {
        return node.getNodeName().equalsIgnoreCase("schedule");
    }

    private static boolean isDailyScheduleSchedules(Node node)
    {
        return node.getNodeName().equalsIgnoreCase("schedules");
    }

    private static boolean isDailyScheduleDayId(Node node)
    {
        return node.getNodeName().equalsIgnoreCase("day_id");
    }

    private static boolean isCalendarWeekProfile(Node node)
    {
        return node.getNodeName().equalsIgnoreCase("week_profile");
    }

    private static boolean isCalendarName(Node node)
    {
        return node.getNodeName().equalsIgnoreCase("name");
    }

    private static boolean isCalendarDailySchedule(Node node)
    {
        return node.getNodeName().equals("daily_schedule");
    }


    protected static byte[] parseTimeString(final String s) throws SAXException
    {
        byte[] result = new byte[]{(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF};
        Matcher match = timePattern.matcher(s);
        if (!match.matches())
        {
            throw new SAXException("xml structure: invalid time (" + s + ")");
        }
        for (int i = 0; i < 3; i++)
        {
            String g = match.group(i + 1);
            if (g != null)
            {
                if (g.startsWith(":"))
                    g = g.substring(1);
                result[i] = Byte.parseByte(g);
            }
        }
        return result;
    }


}
