package com.energyict.protocolimplv2.ace4000.objects;

import com.elster.jupiter.calendar.DayType;
import com.elster.jupiter.calendar.EventOccurrence;
import com.elster.jupiter.calendar.ExceptionalOccurrence;
import com.elster.jupiter.calendar.FixedExceptionalOccurrence;
import com.elster.jupiter.calendar.Period;
import com.elster.jupiter.calendar.PeriodTransition;
import com.elster.jupiter.calendar.RecurringExceptionalOccurrence;
import com.energyict.mdc.common.ApplicationException;

import com.energyict.protocolimplv2.ace4000.xml.XMLTags;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Copyrights EnergyICT
 * Date: 10/08/11
 * Time: 13:18
 */
public class TariffConfiguration extends AbstractActarisObject {

    private com.elster.jupiter.calendar.Calendar calendar = null;

    public TariffConfiguration(ObjectFactory of) {
        super(of);
    }

    public void setCalendar(com.elster.jupiter.calendar.Calendar calendar) {
        this.calendar = calendar;
    }

    private int tariffNumber;
    private int numberOfRates;

    public void setNumberOfRates(int numberOfRates) {
        this.numberOfRates = numberOfRates;
    }

    public void setTariffNumber(int tariffNumber) {
        this.tariffNumber = tariffNumber;
    }

    @Override
    protected void parse(Element element) {
        //Only ack or nack is sent back
    }

    private List<String> getSwitchingTimes() {
        String failMsg = "Cannot configure tariff settings, invalid arguments";
        if (calendar == null) {
            throw new ApplicationException(failMsg);
        }

        List<String> switchingTimes = new ArrayList<>();
        if (calendar.getDayTypes().size() > 16) {
            throw new ApplicationException(failMsg);
        }

        String switchingTime;
        for (DayType dayType : calendar.getDayTypes()) {
            if (dayType.getEventOccurrences().size() > 8) {
                throw new ApplicationException(failMsg);
            }
            switchingTime = "";
            for (EventOccurrence eventOccurrence : dayType.getEventOccurrences()) {
                int hour = eventOccurrence.getFrom().getHour();
                int minute = eventOccurrence.getFrom().getMinute();
                switchingTime += pad(Integer.toString(hour, 16), 2);
                switchingTime += pad(Integer.toString(minute, 16), 2);
                long codeValue = eventOccurrence.getEvent().getCode();
                if (codeValue > numberOfRates) {
                    throw new ApplicationException(failMsg);
                }
                switchingTime += "0" + String.valueOf(codeValue);
            }
            switchingTimes.add(switchingTime);
        }
        return switchingTimes;
    }

    private List<String> getSeasonDefinitions() {
        String failMsg = "Cannot configure tariff settings, invalid arguments";
        List<Period> seasons = calendar.getPeriods();
        List<String> seasonStrings = new ArrayList<>();
        if (seasons.size() > 4 || seasons.size() < 1) {
            throw new ApplicationException(failMsg);
        }

        for (Period season : seasons) {
            int month = season.getFrom().getMonthValue();
            int dayOfMonth = season.getFrom().getDayOfMonth();
            Map<Integer, Long> dayProfileIds =
                    Stream
                        .of(DayOfWeek.values())
                        .collect(Collectors.toMap(
                            DayOfWeek::getValue,
                            dayOfWeek -> season.getDayType(dayOfWeek).getId()));
            String weekProfile = "";
            for (int i = 1; i < 8; i++) {
                Long dayProfile = dayProfileIds.get(i);
                weekProfile = String.valueOf(dayProfile) + weekProfile;
            }
            String seasonStartMoment = "64" + pad(Integer.toString(month, 16), 2) + pad(Integer.toString(dayOfMonth, 16), 2) + pad(Integer.toString(dayOfWeek, 16), 2);
            weekProfile = seasonStartMoment + "0" + weekProfile;
            seasonStrings.add(weekProfile);
        }
        return seasonStrings;
    }

    private List<PeriodTransition>
    private List<String> getSpecialDays() {
        String failMsg = "Cannot configure tariff settings, invalid arguments";
        if (calendar.getExceptionalOccurrences().size() > 36) {
            throw new ApplicationException(failMsg);
        }
        return this.calendar
                .getExceptionalOccurrences()
                .stream()
                .map(this::toSpecialDay)
                .collect(Collectors.toList());
    }

    private String toSpecialDay(ExceptionalOccurrence exceptionalOccurrence) {
        if (exceptionalOccurrence instanceof FixedExceptionalOccurrence) {
            return this.toSpecialDay((FixedExceptionalOccurrence) exceptionalOccurrence);
        } else {
            return this.toSpecialDay((RecurringExceptionalOccurrence) exceptionalOccurrence);
        }
    }

    private String toSpecialDay(FixedExceptionalOccurrence exceptionalOccurrence) {
        int year = exceptionalOccurrence.getOccurrence().getYear();
        int month = exceptionalOccurrence.getOccurrence().getMonthValue();
        int dayOfMonth = exceptionalOccurrence.getOccurrence().getDayOfMonth();
        int dayOfWeek = exceptionalOccurrence.getOccurrence().getDayOfWeek().getValue();
        int dayRate = this.dayRateFrom(exceptionalOccurrence);
        return this.toSpecialDay(year, month, dayOfMonth, dayOfWeek, dayRate);
    }

    private String toSpecialDay(RecurringExceptionalOccurrence exceptionalOccurrence) {
        int year = 0x64;
        int month = exceptionalOccurrence.getOccurrence().getMonthValue();
        int dayOfMonth = exceptionalOccurrence.getOccurrence().getDayOfMonth();
        int dayOfWeek = 0x0F;
        int dayRate = this.dayRateFrom(exceptionalOccurrence);
        return this.toSpecialDay(year, month, dayOfMonth, dayOfWeek, dayRate);
    }

    private int dayRateFrom(ExceptionalOccurrence exceptionalOccurrence) {
        try {
            // Todo: see mail sent to Kristof, Stijn and Govanni about the purpose of parsing the day type's name to an int
            return Integer.parseInt(exceptionalOccurrence.getDayType().getName()) - 1;
        } catch (NumberFormatException e) {
            throw new ApplicationException(failMsg);
        }
    }

    private String toSpecialDay(int year, int month, int dayOfMonth, int dayOfWeek, int dayRate) {
        return pad(Integer.toString(year, 16), 2) + pad(Integer.toString(month, 16), 2) + pad(Integer.toString(dayOfMonth, 16), 2) + Integer.toString(dayOfWeek, 16) + Integer.toString(dayRate, 16);
    }

    @Override
    protected String prepareXML() {

        Document doc = createDomDocument();

        Element root = doc.createElement(XMLTags.MPULL);
        doc.appendChild(root);
        Element md = doc.createElement(XMLTags.METERDATA);
        root.appendChild(md);
        Element s = doc.createElement(XMLTags.SERIALNUMBER);
        s.setTextContent(getObjectFactory().getAce4000().getSerialNumber());
        md.appendChild(s);
        Element t = doc.createElement(XMLTags.TRACKER);
        t.setTextContent(Integer.toString(getTrackingID(), 16));
        md.appendChild(t);

        Element cf = doc.createElement(XMLTags.CONFIGURATION);
        md.appendChild(cf);
        Element tariffElement = doc.createElement(XMLTags.TARIFF);
        tariffElement.setAttribute(XMLTags.TARIFF_TYPE, "1");
        cf.appendChild(tariffElement);

        Element el = doc.createElement(XMLTags.TARIFF_NUMBER);
        el.setTextContent(Integer.toString(tariffNumber, 16));
        tariffElement.appendChild(el);
        el = doc.createElement(XMLTags.TARIFF_RATES);
        el.setTextContent(Integer.toString(numberOfRates, 16));
        tariffElement.appendChild(el);

        for (String switchingTime : getSwitchingTimes()) {
            if (!"".equals(switchingTime)) {
                el = doc.createElement(XMLTags.TARIFF_SW_DAY);
                el.setTextContent(switchingTime);
                tariffElement.appendChild(el);
            }
        }

        Element seasonsElement = doc.createElement(XMLTags.TARIFF_SEASON);
        int counter = 0;
        for (String seasonDefinition : getSeasonDefinitions()) {
            counter++;
            el = doc.createElement(XMLTags.TARIFF_SEASON + String.valueOf(counter));
            el.setTextContent(seasonDefinition);
            seasonsElement.appendChild(el);
        }
        tariffElement.appendChild(seasonsElement);

        Element specialDaysElement = doc.createElement(XMLTags.TARIFF_SPEC_DAYS);
        List<String> specialDaysResult = new ArrayList<>();
        counter = 0;
        String result = "";
        for (String specialDay : getSpecialDays()) {
            result += specialDay;
            counter++;
            if (counter == 12) {
                specialDaysResult.add(result);
                result = "";
                counter = 0;
            }
        }
        if (counter < 12) {
            specialDaysResult.add(result);
        }

        for (String specialDay : specialDaysResult) {

            el = doc.createElement(XMLTags.TARIFF_SPEC_DAY);
            el.setTextContent(specialDay);
            specialDaysElement.appendChild(el);
        }
        tariffElement.appendChild(specialDaysElement);

        String msg = convertDocumentToString(doc);
        return (msg.substring(msg.indexOf("?>") + 2));
    }

    private String pad(String text, int length) {
        while (text.length() < length) {
            text = "0" + text;
        }
        return text;
    }
}