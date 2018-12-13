package com.energyict.protocolimplv2.ace4000.objects;

import com.energyict.protocolimplv2.ace4000.xml.XMLTags;
import com.energyict.protocolimplv2.common.objectserialization.codetable.objects.CodeCalendarObject;
import com.energyict.protocolimplv2.common.objectserialization.codetable.objects.CodeDayTypeDefObject;
import com.energyict.protocolimplv2.common.objectserialization.codetable.objects.CodeDayTypeObject;
import com.energyict.protocolimplv2.common.objectserialization.codetable.objects.CodeObject;
import com.energyict.protocolimplv2.common.objectserialization.codetable.objects.SeasonObject;
import com.energyict.protocolimplv2.common.objectserialization.codetable.objects.SeasonTransitionObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyrights EnergyICT
 * Date: 10/08/11
 * Time: 13:18
 */
public class TariffConfiguration extends AbstractActarisObject {

    private CodeObject codeTable = null;
    private int tariffNumber;
    private int numberOfRates;
    private List<CodeCalendarObject> seasonCodeCalendars = new ArrayList<>();
    public TariffConfiguration(ObjectFactory of) {
        super(of);
    }

    public void setCodeTable(CodeObject codeTable) {
        this.codeTable = codeTable;
    }

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
        String failMsg = "Cannot configure codetable settings, invalid arguments";
        if (codeTable == null) {
            throw new IllegalArgumentException(failMsg);
        }

        List<String> switchingTimes = new ArrayList<String>();
        if (codeTable.getDayTypes().size() > 16) {
            throw new IllegalArgumentException(failMsg);
        }


        String switchingTime;
        for (CodeDayTypeObject dayType : codeTable.getDayTypes()) {
            if (dayType.getDayTypeDefs().size() > 8) {
                throw new IllegalArgumentException(failMsg);
            }
            switchingTime = "";
            for (CodeDayTypeDefObject dayTypeDefinition : dayType.getDayTypeDefs()) {

                String start = pad(String.valueOf(dayTypeDefinition.getFrom()), 6);
                start = start.substring(0, start.length() - 2);
                int hour = Integer.parseInt(start.substring(0, 2));
                int minute = Integer.parseInt(start.substring(2, 4));
                switchingTime += pad(Integer.toString(hour, 16), 2);
                switchingTime += pad(Integer.toString(minute, 16), 2);
                int codeValue = dayTypeDefinition.getCodeValue();
                if (codeValue > numberOfRates) {
                    throw new IllegalArgumentException(failMsg);
                }
                switchingTime += "0" + String.valueOf(codeValue);
            }
            switchingTimes.add(switchingTime);
        }
        return switchingTimes;
    }

    private List<String> getSeasonDefinitions() {
        String failMsg = "Cannot configure codetable settings, invalid arguments";

        List<SeasonObject> seasons = codeTable.getSeasonSet().getSeasons();
        Calendar cal = Calendar.getInstance();
        List<String> seasonStrings = new ArrayList<String>();
        if (seasons.size() > 4 || seasons.size() < 1) {
            throw new IllegalArgumentException(failMsg);
        }

        for (SeasonObject season : seasons) {
            for (SeasonTransitionObject seasonTransition : season.getTransitions()) {
                cal.setTime(seasonTransition.getStartDate());
                int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1;
                dayOfWeek = dayOfWeek == 0 ? 7 : dayOfWeek;
                int month = cal.get(Calendar.MONTH) + 1;
                int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);

                int defaultCode = -1;
                boolean found = false;
                Map<Integer, Integer> dayProfileIds = new HashMap<Integer, Integer>();
                for (CodeCalendarObject codeCalendar : codeTable.getCalendars()) {
                    if ((codeCalendar.getYear() == -1) || codeCalendar.getYear() == Calendar.getInstance().get(Calendar.YEAR)) {
                        if (codeCalendar.getSeasonId() == season.getId()) {
                            if (codeCalendar.getMonth() == -1) {
                                if (codeCalendar.getDay() == -1) {
                                    if (codeCalendar.getDayOfWeek() == -1) {
                                        try {
                                            defaultCode = Integer.parseInt(codeCalendar.getDayTypeName()) - 1;
                                        } catch (NumberFormatException e) {
                                            throw new IllegalArgumentException(failMsg);
                                        }
                                        if (!seasonCodeCalendars.contains(codeCalendar)) {
                                            seasonCodeCalendars.add(codeCalendar);
                                        }
                                        found = true;
                                    } else {
                                        try {
                                            dayProfileIds.put(codeCalendar.getDayOfWeek(), Integer.parseInt(codeCalendar.getDayTypeName()) - 1);
                                        } catch (NumberFormatException e) {
                                            throw new IllegalArgumentException(failMsg);
                                        }
                                        if (!seasonCodeCalendars.contains(codeCalendar)) {
                                            seasonCodeCalendars.add(codeCalendar);
                                        }
                                        found = true;
                                    }
                                }
                            }
                        }
                    }
                }
                if (found) {
                    String weekProfile = "";
                    for (int i = 1; i < 8; i++) {
                        Integer dayProfile = dayProfileIds.get(i);
                        if (dayProfile != null) {
                            weekProfile = String.valueOf(dayProfile) + weekProfile;
                        } else {
                            if (defaultCode != -1) {
                                weekProfile = String.valueOf(defaultCode) + weekProfile;
                            } else {
                                throw new IllegalArgumentException(failMsg);
                            }
                        }
                    }
                    String seasonStartMoment = "64" + pad(Integer.toString(month, 16), 2) + pad(Integer.toString(dayOfMonth, 16), 2) + pad(Integer.toString(dayOfWeek, 16), 2);
                    weekProfile = seasonStartMoment + "0" + weekProfile;
                    seasonStrings.add(weekProfile);
                }
            }
        }
        return seasonStrings;
    }

    private List<String> getSpecialDays() {
        String failMsg = "Cannot configure codetable settings, invalid arguments";
        if (codeTable.getCalendars().size() - seasonCodeCalendars.size() > 36) {
            throw new IllegalArgumentException(failMsg);
        }
        List<String> specialDays = new ArrayList<String>();

        for (CodeCalendarObject codeCalendar : codeTable.getCalendars()) {
            if (!seasonCodeCalendars.contains(codeCalendar)) {
                int year = codeCalendar.getYear();
                year = year == -1 ? 0x64 : year - 2000;


                int month = codeCalendar.getMonth();
                month = month == -1 ? 0xFF : month;

                int dayOfMonth = codeCalendar.getDay();
                dayOfMonth = dayOfMonth == -1 ? 0xFF : dayOfMonth;


                int dayOfWeek = codeCalendar.getDayOfWeek();
                dayOfWeek = dayOfWeek == 0 ? 7 : dayOfWeek;
                dayOfWeek = dayOfWeek == -1 ? 0x0F : dayOfWeek;

                int dayRate;
                try {
                    dayRate = Integer.parseInt(codeCalendar.getDayTypeName()) - 1;
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(failMsg);
                }

                specialDays.add(pad(Integer.toString(year, 16), 2) + pad(Integer.toString(month, 16), 2) + pad(Integer.toString(dayOfMonth, 16), 2) + Integer.toString(dayOfWeek, 16) + Integer.toString(dayRate, 16));
            }
        }
        return specialDays;
    }

    @Override
    protected String prepareXML() {

        Document doc = createDomDocument();

        Element root = doc.createElement(XMLTags.MPULL);
        doc.appendChild(root);
        Element md = doc.createElement(XMLTags.METERDATA);
        root.appendChild(md);
        Element s = doc.createElement(XMLTags.SERIALNUMBER);
        s.setTextContent(getObjectFactory().getAce4000().getConfiguredSerialNumber());
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
        List<String> specialDaysResult = new ArrayList<String>();
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
