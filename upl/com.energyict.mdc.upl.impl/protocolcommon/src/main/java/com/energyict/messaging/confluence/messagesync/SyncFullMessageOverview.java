package com.energyict.messaging.confluence.messagesync;

import com.energyict.messaging.confluence.messagesync.client.*;
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

/**
 * Replaces the current content of the device messages confluence page with the new, up-to-date overview of all messages (based on the specs in the code).
 * The confluence page has title "All messages", is located in the PRTCL space and will look like this:
 * <h1>All messages</h1>
 * <table> (full message overview) </table>
 * Copyrights EnergyICT
 * Date: 11/04/14
 * Time: 15:24
 * Author: khe
 */
public class SyncFullMessageOverview {

    private static final Charset CHARSET = StandardCharsets.UTF_8;
    private static MessageHtmlGenerator descriptionGenerator;
    private static final Logger logger = Logger.getLogger(SyncFullMessageOverview.class.getName());

    public static void main(String[] args) throws IOException, ClassNotFoundException, ParserConfigurationException, InstantiationException, IllegalAccessException, TransformerException {
        ConfluenceSoapService soapService = new ConfluenceserviceV2SoapBindingStub(new URL("http://confluence.eict.vpdc/plugins/servlet/soap-axis1/confluenceservice-v2"), new ConfluenceSoapServiceServiceLocator());
        String token = soapService.login("khe", getPasswordFromFile());

        RemotePage protocolPage = soapService.getPage(token, "PRTCL", "All messages");

        logger.info("Checking contents of page 'All messages', looking for the current message table");

        logger.info("Updating the page with the new message table");
        protocolPage.setContent(getFullMessageDescription());
        soapService.storePage(token, protocolPage);
        logger.info("Successfully updated the message table on page 'All messages'");
    }

    private static String getPasswordFromFile() throws IOException {
        byte[] passwordBytes = Files.readAllBytes(Paths.get("password.txt"));
        return CHARSET.decode(ByteBuffer.wrap(passwordBytes)).toString();
    }

    private static String getFullMessageDescription() throws TransformerException, ClassNotFoundException, ParserConfigurationException, InstantiationException, IllegalAccessException, IOException {
        Document doc = getDescriptionGenerator().createAllMessages();

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        StringWriter stringWriter = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(stringWriter));
        return "<h1>All messages</h1>" + stringWriter.getBuffer().toString().replace("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>", "");
    }

    private static MessageHtmlGenerator getDescriptionGenerator() {
        if (descriptionGenerator == null) {
            descriptionGenerator = new MessageHtmlGenerator();
        }
        return descriptionGenerator;
    }
}