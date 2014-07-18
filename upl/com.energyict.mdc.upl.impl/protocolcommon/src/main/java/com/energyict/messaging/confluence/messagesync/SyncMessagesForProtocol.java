package com.energyict.messaging.confluence.messagesync;

import com.energyict.messaging.confluence.messagesync.client.*;
import org.apache.commons.lang3.StringEscapeUtils;
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
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyrights EnergyICT
 * Date: 11/04/14
 * Time: 15:24
 * Author: khe
 */
public class SyncMessagesForProtocol {

    private static final Charset CHARSET = StandardCharsets.UTF_8;
    private static MessageHtmlGenerator descriptionGenerator;
    private static final Logger logger = Logger.getLogger(SyncMessagesForProtocol.class.getName());
    private static ConfluenceSoapService confluenceSoapService;

    public static void main(String[] args) throws IOException, ClassNotFoundException, ParserConfigurationException, InstantiationException, IllegalAccessException, TransformerException, RemoteException {
        if (args.length != 1) {
            throw new IllegalArgumentException("Excepted one argument (protocol java class name), received " + args.length + " arguments!");
        }
        syncMessagesForProtocol(args[0], loginToConfluence(getConfluenceSoapService()));
    }

    public static void syncMessagesForProtocol(String javaClassName, String token) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException, TransformerException, ParserConfigurationException {
        ConfluenceSoapService soapService = getConfluenceSoapService();
        String protocolDescription = getDescriptionGenerator().getProtocolDescription(javaClassName);
        RemotePage protocolPage = soapService.getPage(token, "PRTCL", protocolDescription);

        logger.info("Checking contents of page '" + protocolDescription + "', looking for the current message table");

        String currentPageContent = protocolPage.getContent();
        currentPageContent = currentPageContent.replace("\n", "");
        currentPageContent = currentPageContent.replace("\r", "");
        currentPageContent = currentPageContent.replace("\t", "");

        Matcher matcher = getMessageTablePattern().matcher(currentPageContent);
        if (matcher.find()) {
            String oldTable = matcher.group(1);
            logger.info("Updating the page with the new message table");
            if (getDescriptionGenerator().protocolHasMessages(javaClassName)) {
                String result = currentPageContent.replaceAll(getMessageTablePattern().toString(), appendHeaderToMessagesDescription(getFullMessageDescription(oldTable, javaClassName)));
                protocolPage.setContent(result);
                soapService.storePage(token, protocolPage);
                logger.info("Successfully updated the message table on page '" + protocolDescription + "'");
            } else {
                String result = currentPageContent.replaceAll(getMessageTablePattern().toString(), appendHeaderToMessagesDescription(noMessagesDescription()));
                protocolPage.setContent(result);
                soapService.storePage(token, protocolPage);
                logger.info("Protocol has no messages - removed the table on page '" + protocolDescription + "'");
            }
        } else {
            throw new IOException("Could not find (and replace) the current message table on page '" + protocolDescription + "'");
        }
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

    private static Pattern getMessageTablePattern() {
        String prefix = StringEscapeUtils.escapeJava("<h1>Device messages.*?</h1>");
        String suffix = StringEscapeUtils.escapeJava("<h1>");
        return Pattern.compile(prefix + "(.*?)" + suffix);
    }

    private static String getFullMessageDescription(String oldTable, String javaClassName) throws TransformerException, ClassNotFoundException, ParserConfigurationException, InstantiationException, IllegalAccessException, IOException {
        Document doc = getDescriptionGenerator().createMessagesForProtocol(oldTable, javaClassName);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        StringWriter stringWriter = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(stringWriter));
        return stringWriter.getBuffer().toString().replace("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>", "");
    }

    private static String noMessagesDescription() {
        return "<p>N/A</p>";
    }

    private static String appendHeaderToMessagesDescription(String content) {
        String header = StringEscapeUtils.escapeJava("<h1>Device messages</h1>");
        String suffix = "<p><hr></hr></p><h1>";
        return header + content + suffix;
    }

    private static MessageHtmlGenerator getDescriptionGenerator() throws IOException {
        if (descriptionGenerator == null) {
            descriptionGenerator = new MessageHtmlGenerator();
            descriptionGenerator.doBefore(); // Initialize MeteringWarehouse
        }
        return descriptionGenerator;
    }
}