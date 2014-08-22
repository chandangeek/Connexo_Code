package com.energyict.messaging.confluence.messagesync;

import com.energyict.messaging.confluence.messagesync.client.*;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
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
public class SyncPageForProtocol {

    private static final Charset CHARSET = StandardCharsets.UTF_8;
    private static MessageHtmlGenerator descriptionGenerator;
    private static final Logger logger = Logger.getLogger(SyncPageForProtocol.class.getName());
    private static ConfluenceSoapService confluenceSoapService;

    public static void main(String[] args) throws IOException, ClassNotFoundException, ParserConfigurationException, InstantiationException, IllegalAccessException, TransformerException, RemoteException {

        if (args.length != 1) {
            throw new IllegalArgumentException("Excepted one argument (protocol java class name), received " + args.length + " arguments!");
        }
        syncPageForProtocol(args[0], loginToConfluence(getConfluenceSoapService()));
    }

    public static void syncPageForProtocol(String javaClassName, String token) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IOException, TransformerException, ParserConfigurationException {
        Pattern pattern = Pattern.compile("<p>\\s*&nbsp;*\\s*</p><hr /><p>\\s*&nbsp;*\\s*</p>");
        String replacement = "<p><hr /></p>";

        ConfluenceSoapService soapService = getConfluenceSoapService();
        String protocolDescription = getDescriptionGenerator().getProtocolDescription(javaClassName);
        RemotePage protocolPage = soapService.getPage(token, "PRTCL", protocolDescription);

        logger.info("Checking contents of page '" + protocolDescription + "', looking for obsolete whitespace");

        String currentPageContent = protocolPage.getContent();
        currentPageContent = currentPageContent.replace("\n", "");
        currentPageContent = currentPageContent.replace("\r", "");
        currentPageContent = currentPageContent.replace("\t", "");
        //Needed to "clean" pages made with old template which had some HTML-errors
//        currentPageContent = currentPageContent.replace("<th><p><strong>","<th>");
//        currentPageContent = currentPageContent.replace("</strong></p></th>","</th>");
//        currentPageContent = currentPageContent.replace("<p align=\"left\"><strong>","");
//        currentPageContent = currentPageContent.replaceAll("<th><p>A</p></th><th><p>B</p></th><th><p>C</p></th><th><p>D</p></th><th><p>E</p></th><th><p>F</p></th><th><p>.*?</p></th>", "<th>A</th><th>B</th><th>C</th><th>D</th><th>E</th><th>F</th><th>Manufacturer Description &ndash; (Optional: Code)</th>");

        Matcher matcher = pattern.matcher(currentPageContent);
        if (matcher.find()) {
            logger.info("Updating the page with new layout");
            String result = currentPageContent.replaceAll(pattern.toString(), replacement);
            protocolPage.setContent(result);
            soapService.storePage(token, protocolPage);
            logger.info("Successfully updated the page '" + protocolDescription + "'");
        }  else {
            logger.info("No match for page ' " + protocolDescription + "'");
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

    private static MessageHtmlGenerator getDescriptionGenerator() throws IOException {
        if (descriptionGenerator == null) {
            descriptionGenerator = new MessageHtmlGenerator();
            descriptionGenerator.doBefore(); // Initialize MeteringWarehouse
        }
        return descriptionGenerator;
    }
}