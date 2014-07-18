package com.energyict.messaging.confluence.messagesync;

import com.energyict.messaging.confluence.messagesync.client.*;
import org.apache.commons.lang3.StringEscapeUtils;
import org.omg.CORBA.portable.ApplicationException;
import org.w3c.dom.Document;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringWriter;
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
            throw new IllegalArgumentException("Excepted one argument (protocol java class name), received " + args.length + " arguments!");
        }
        syncCimCodeForProtocol(args[0], loginToConfluence(getConfluenceSoapService()));
    }

    public static void syncCimCodeForProtocol(String javaClassName, String token) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException, TransformerException, ParserConfigurationException {
        ConfluenceSoapService soapService = getConfluenceSoapService();
        String protocolDescription = getDescriptionGenerator().getProtocolDescription(javaClassName);
        RemotePage protocolPage = soapService.getPage(token, "PRTCL", protocolDescription);

        logger.info("Checking contents of page '" + protocolDescription + "', looking for the current message table");

        String currentPageContent = protocolPage.getContent();
        currentPageContent = currentPageContent.replace(" colspan=\"1\"","");
        currentPageContent = currentPageContent.replace("\n", "");
        currentPageContent = currentPageContent.replace("\r", "");
        currentPageContent = currentPageContent.replace("\t", "");


        Matcher matcher = getMeterEventsTablePattern().matcher(currentPageContent);
        if (matcher.find()) {
            logger.info("Table with CIM Codes Found");
//            String result = currentPageContent.replaceAll(getMeterEventsTablePattern().toString(), replacement);
            String originalTable = matcher.group(1);

            String newTable = originalTable;
//           Look for CIM-CODES
            List<String> cimList = getCimCode(newTable);
//           Add CIM-CODES
            boolean once = true;

            for (String eiserverEventCode : cimList) {
                String search = "</td><td>" + eiserverEventCode + "</td><td>.*?</td><td></td>";

                Pattern tableRowPattern = Pattern.compile(search);
                Matcher tableRowMatcher = tableRowPattern.matcher(originalTable);
                String tableRowContent = "";
                while (tableRowMatcher.find()) {
                    tableRowContent = tableRowMatcher.group(0);

                    //TODO: replace LAST <td></td> with <td>CIM code</td>
                    //TODO the rule below appends it, this is incorrect
                    String newTableRowContent = tableRowContent + "<td>" + "CIM" + "</td>";
                    newTable = newTable.replaceAll(tableRowContent, newTableRowContent);


                    if (once) {
                        once = false;
                        String extraSearch = newTableRowContent + "(.*?)" + "</td><td>.*?</td></tr>";
                        Pattern extraSearchPattern = Pattern.compile(extraSearch);
                        Matcher extraMatcher = extraSearchPattern.matcher(newTable);
                        String extraPiece;
                        if (extraMatcher.find()) {
                            extraPiece = extraMatcher.group(1);
                        } else {
                            throw new com.energyict.cbo.ApplicationException("Error");
                        }

                        newTable = newTable.replace(extraPiece + "</td>", "");
                    }

                }

            }

//            protocolPage.setContent(result);
//            soapService.storePage(token, protocolPage);
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

    private static List<String> getCimCode(String serverCode) {
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