package com.energyict.messaging.confluence.messagesync;

import com.energyict.messaging.confluence.messagesync.client.*;
import org.apache.commons.lang3.StringEscapeUtils;
import org.w3c.dom.Document;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
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

    public static void main(String[] args) throws IOException, ClassNotFoundException, ParserConfigurationException, InstantiationException, IllegalAccessException, TransformerException, RemoteException {
        ConfluenceSoapService soapService = new ConfluenceserviceV2SoapBindingStub(new URL("http://confluence.eict.vpdc/plugins/servlet/soap-axis1/confluenceservice-v2"), new ConfluenceSoapServiceServiceLocator());
        String token = soapService.login("khe", getPasswordFromFile());

        String protocolDescription = getDescriptionGenerator().getProtocolDescription(args);
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
            String result = currentPageContent.replace(oldTable, getFullMessageDescription(oldTable, args));
            protocolPage.setContent(result);
            soapService.storePage(token, protocolPage);
            logger.info("Successfully updated the message table on page '" + protocolDescription + "'");
        } else {
            throw new IOException("Could not find (and replace) the current message table on page '" + protocolDescription + "'");
        }
    }

    private static Pattern getMessageTablePattern() {
        String prefix = StringEscapeUtils.escapeJava("<h1>.*?Device message.*?</h1>");
        String suffix = StringEscapeUtils.escapeJava("<h1>");
        return Pattern.compile(prefix + "(.*?)" + suffix);
    }

    private static String getPasswordFromFile() throws IOException {
        byte[] passwordBytes = Files.readAllBytes(Paths.get("password.txt"));
        return CHARSET.decode(ByteBuffer.wrap(passwordBytes)).toString();
    }

    private static String getFullMessageDescription(String oldTable, String[] args) throws TransformerException, ClassNotFoundException, ParserConfigurationException, InstantiationException, IllegalAccessException, IOException {
        Document doc = getDescriptionGenerator().createMessagesForProtocol(oldTable, args);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        StringWriter stringWriter = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(stringWriter));
        return stringWriter.getBuffer().toString().replace("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>", "");
    }

    private static MessageHtmlGenerator getDescriptionGenerator() {
        if (descriptionGenerator == null) {
            descriptionGenerator = new MessageHtmlGenerator();
        }
        return descriptionGenerator;
    }
}