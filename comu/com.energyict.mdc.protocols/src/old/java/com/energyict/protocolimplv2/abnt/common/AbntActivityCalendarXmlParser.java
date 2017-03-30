/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.abnt.common;

import com.energyict.protocolimpl.messages.codetableparsing.CodeTableXml;
import com.energyict.protocolimplv2.abnt.common.exception.AbntException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author sva
 * @since 7/10/2014 - 15:37
 */
public class AbntActivityCalendarXmlParser {

    private static final int HOLIDAY_LIST_SIZE = 15;
    private static final String DEFAULT_DATE = "010101";   // 01/01/2001

    private List<String> specialDays;

    private final Log logger = LogFactory.getLog(getClass());

    public AbntActivityCalendarXmlParser() {
        this.specialDays = new ArrayList<>(0);
    }

    public void parseContent(String xmlContent) throws AbntException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlContent)));
            createSpecialDays(doc.getElementsByTagName(CodeTableXml.specialDays));
        } catch (ParserConfigurationException e) {
            logger.error("AbntActivityCalendarXmlParser -> Could not create a DocumentBuilder.");
            throw new AbntException("AbntActivityCalendarXmlParser -> Could not create a DocumentBuilder. ParseConfigurationException message : " + e.getLocalizedMessage());
        } catch (SAXException e) {
            logger.error("AbntActivityCalendarXmlParser -> A parse ERROR occurred.");
            throw new AbntException("AbntActivityCalendarXmlParser -> A parse ERROR occurred. SAXException message : " + e.getLocalizedMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param specialDayList
     * @throws java.io.IOException
     */
    protected void createSpecialDays(NodeList specialDayList) throws IOException {
        this.specialDays = new ArrayList<>();
        Node specialDayProfile;
        for (int i = 0; i < specialDayList.getLength(); i++) {
            specialDayProfile = specialDayList.item(i);
            for (int j = 0; j < specialDayProfile.getChildNodes().getLength(); j++) {
                Node specialDayNode = specialDayProfile.getChildNodes().item(j);
                for (int z = 0; z < specialDayNode.getChildNodes().getLength(); z++) {
                    Node specialDayEntry = specialDayNode.getChildNodes().item(z);

                    if (specialDayEntry.getNodeName().equalsIgnoreCase(CodeTableXml.specialDayEntryDate)) {
                        String year = null;
                        String month = null;
                        String day = null;
                        int temp;
                        for (int k = 0; k < specialDayEntry.getChildNodes().getLength(); k++) {
                            Node sdTimeElement = specialDayEntry.getChildNodes().item(k);
                            if (sdTimeElement.getNodeName().equalsIgnoreCase(CodeTableXml.dYear)) {
                                temp = Integer.parseInt(sdTimeElement.getTextContent().equals("-1") ? "2000" : sdTimeElement.getTextContent());
                                year = String.format("%02d", temp - 2000); // Only the last two digits of year (e.g. 14 instead of 2014)
                            } else if (sdTimeElement.getNodeName().equalsIgnoreCase(CodeTableXml.dMonth)) {
                                temp = Integer.parseInt(sdTimeElement.getTextContent());
                                if (temp < 0) {
                                    String message = "AbntActivityCalendarXmlParser -> Encountered one or more invalid special day entries: the month cannot be left unspecified!";
                                    logger.error(message);
                                    throw new AbntException(message);
                                }
                                month = String.format("%02d", temp);
                            } else if (sdTimeElement.getNodeName().equalsIgnoreCase(CodeTableXml.dDay)) {
                                temp = Integer.parseInt(sdTimeElement.getTextContent());
                                if (temp < 0) {
                                    String message = "AbntActivityCalendarXmlParser -> Encountered one or more invalid special day entries: the day cannot be left unspecified!";
                                    logger.error(message);
                                    throw new AbntException(message);
                                }
                                day = String.format("%02d", temp);
                            }
                        }
                        this.specialDays.add(day.concat(month).concat(year));
                    }
                }
            }
        }
        if (this.specialDays.size() > 15) {
            String message = "AbntActivityCalendarXmlParser -> At max 15 special days can be programmed!";
            logger.error(message);
            throw new AbntException(message);
        }
    }

    public List<String> getSpecialDays() {
        while (specialDays.size() < HOLIDAY_LIST_SIZE) {
            specialDays.add(DEFAULT_DATE);
        }

        return specialDays;
    }
}
