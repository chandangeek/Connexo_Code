package com.energyict.messaging.confluence.messagesync;

import com.energyict.cim.EndDeviceEventType;
import com.energyict.cim.EndDeviceEventTypeMapping;
import com.energyict.messaging.confluence.messagesync.client.*;
import org.apache.commons.lang3.StringEscapeUtils;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyrights EnergyICT
 * Date: 11/04/14
 * Time: 15:24
 * Author: khe
 */
public class SyncCimCodeForProtocol {

    private static final Charset CHARSET = StandardCharsets.UTF_8;
    private static MessageHtmlGenerator descriptionGenerator;
    private static final Logger logger = Logger.getLogger(SyncCimCodeForProtocol.class.getName());
    private static ConfluenceSoapService confluenceSoapService;

    public static void main(String[] args) throws IOException, ClassNotFoundException, ParserConfigurationException, InstantiationException, IllegalAccessException, TransformerException, RemoteException {

        if (args.length != 1) {
            throw new IllegalArgumentException("Expected one argument (protocol java class name), received " + args.length + " arguments!");
        }
        syncCimCodeForProtocol(args[0], loginToConfluence(getConfluenceSoapService()));
    }

    public static void syncCimCodeForProtocol(String javaClassName, String token) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException, TransformerException, ParserConfigurationException {
        ConfluenceSoapService soapService = getConfluenceSoapService();
        String protocolDescription = getDescriptionGenerator().getProtocolDescription(javaClassName);
        RemotePage protocolPage = soapService.getPage(token, "PRTCL", protocolDescription);

        logger.info("Checking contents of page '" + protocolDescription + "', looking for the current events table");

        String currentPageContent = protocolPage.getContent();
        currentPageContent = currentPageContent.replace(" colspan=\"1\"", "");
        currentPageContent = currentPageContent.replace("\n", "");
        currentPageContent = currentPageContent.replace("\r", "");
        currentPageContent = currentPageContent.replace("\t", "");
        currentPageContent = currentPageContent.replace("{", "(");
        currentPageContent = currentPageContent.replace("}", ")");
        currentPageContent = currentPageContent.replace("&nbsp;", "");


        Matcher matcher = getMeterEventsTablePattern().matcher(currentPageContent);
        if (matcher.find()) {
            logger.info("Meter Events Section Found");
//            String result = currentPageContent.replaceAll(getMeterEventsTablePattern().toString(), replacement);
            String originalTable = matcher.group(1);

            String newTable = originalTable;
//           Look for CIM-CODES
            List<String> eiserverCodes = getEiserverCodes(newTable);
//           Add CIM-CODES
            for (String eiserverEventCode : eiserverCodes) {
                String search = "</td><td>" + eiserverEventCode + "</td><td>.*?</td><td></td>";
                Pattern tableRowPattern = Pattern.compile(search);
                Matcher tableRowMatcher = tableRowPattern.matcher(originalTable);
                String tableRowContent = "";
                while (tableRowMatcher.find()) {
                    tableRowContent = tableRowMatcher.group(0);

                    eiserverEventCode = eiserverEventCode.replace("<p class=\"TableBody\">","");
                    eiserverEventCode = eiserverEventCode.replace("<p align=\"right\">", "");
                    eiserverEventCode = eiserverEventCode.replace("<p align=\"right\" class=\"TableBody\">", "");
                    eiserverEventCode = eiserverEventCode.replace("</p>","");
                    eiserverEventCode = eiserverEventCode.replace("<p>","");
                    eiserverEventCode = eiserverEventCode.replace("0X","0x");

                    int decimal;
                    if (eiserverEventCode.contains("0x")) {
                        String hexPart = eiserverEventCode.split("x")[1];
                        decimal = Integer.parseInt(hexPart, 16);
                    } else {
                        try {
                            decimal = Integer.parseInt(eiserverEventCode);
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                            continue;    //Don't fail, go to next find
                        }
                    }

                    EndDeviceEventType cimCode = EndDeviceEventTypeMapping.getEventTypeCorrespondingToEISCode(decimal);
                    String cimCodeString = cimCode.toString();

                    String newTableRowContent = tableRowContent.substring(0, tableRowContent.lastIndexOf("</td>")) + cimCodeString + "</td>";
                    newTable = newTable.replaceAll(tableRowContent, newTableRowContent);
                }

            }
            String newPageContent = currentPageContent.replace(originalTable, newTable);
            protocolPage.setContent(newPageContent);
            soapService.storePage(token, protocolPage);
            logger.info("Successfully updated the page '" + protocolDescription + "'");
        } else
            logger.info("No table Found ' " + protocolDescription + "'");
    }

    private static String loginToConfluence(ConfluenceSoapService soapService) throws java.rmi.RemoteException {
        return soapService.login("protocol-api", "keCx582dws");
    }

    public static ConfluenceSoapService getConfluenceSoapService() throws org.apache.axis.AxisFault, MalformedURLException {
        if (confluenceSoapService == null) {
            confluenceSoapService = new ConfluenceserviceV2SoapBindingStub(new URL("http://confluence.eict.vpdc/plugins/servlet/soap-axis1/confluenceservice-v2"), new ConfluenceSoapServiceServiceLocator());
        }
        return confluenceSoapService;
    }

    private static Pattern getMeterEventsTablePattern() {
        String prefix = StringEscapeUtils.escapeJava("<h1>Meter Events.*?</h1>");
        String suffix = StringEscapeUtils.escapeJava("<h2>");
        return Pattern.compile(prefix + "(.*?)" + suffix);
    }

    private static Pattern getCimCodePattern() {
        String prefix = StringEscapeUtils.escapeJava("<tr><td>.*?</td><td.*?>");
        String suffix = StringEscapeUtils.escapeJava("</td><td>");
        return Pattern.compile(prefix + "(.*?)" + suffix);
    }

    private static List<String> getEiserverCodes(String serverCode) {
        Matcher cimMatcher = getCimCodePattern().matcher(serverCode);
        List<String> results = new ArrayList<>();
        while (cimMatcher.find()) {
            String result = cimMatcher.group(1);
            results.add(result);
        }
        return results;
    }

    private static MessageHtmlGenerator getDescriptionGenerator() throws IOException {
        if (descriptionGenerator == null) {
            descriptionGenerator = new MessageHtmlGenerator();
            descriptionGenerator.doBefore(); // Initialize MeteringWarehouse
        }
        return descriptionGenerator;
    }
}