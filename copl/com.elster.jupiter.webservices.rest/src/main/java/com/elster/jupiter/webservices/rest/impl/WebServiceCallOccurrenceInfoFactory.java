package com.elster.jupiter.webservices.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrence;

import javax.inject.Inject;
import javax.ws.rs.core.UriInfo;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;

public class WebServiceCallOccurrenceInfoFactory {
    private final EndPointConfigurationInfoFactory endPointConfigurationInfoFactory;
    private final Thesaurus thesaurus;

    @Inject
    public WebServiceCallOccurrenceInfoFactory(EndPointConfigurationInfoFactory endPointConfigurationInfoFactory, Thesaurus thesaurus) {
        this.endPointConfigurationInfoFactory = endPointConfigurationInfoFactory;
        this.thesaurus = thesaurus;
    }

    public WebServiceCallOccurrenceInfo from(WebServiceCallOccurrence endPointOccurrence, UriInfo uriInfo, boolean withPayload) {
        WebServiceCallOccurrenceInfo info = new WebServiceCallOccurrenceInfo();

        info.id = endPointOccurrence.getId();
        info.startTime = endPointOccurrence.getStartTime();
        info.status = new IdWithNameInfo(endPointOccurrence.getStatus().name(), endPointOccurrence.getStatus().translate(thesaurus));
        endPointOccurrence.getEndTime().ifPresent(endTime -> info.endTime = endTime);
        endPointOccurrence.getRequest().ifPresent(request -> info.request = request);
        endPointOccurrence.getApplicationName().ifPresent(applicationName -> info.applicationName = applicationName);

        if (withPayload) {
            endPointOccurrence.getPayload().ifPresent(payload -> info.payload = prettyFormatXML(payload,4));
        }
        if (uriInfo != null && endPointOccurrence.getEndPointConfiguration() != null) {
            info.endPointConfigurationInfo = endPointConfigurationInfoFactory.from(endPointOccurrence.getEndPointConfiguration(), uriInfo);
        }
        info.appServerName = endPointOccurrence.getAppServerName().orElse(null);
        return info;
    }

    private String prettyFormatXML(String input, int indent) {
        try {
            Source xmlInput = new StreamSource(new StringReader(input));
            StreamResult xmlOutput = new StreamResult(new StringWriter());
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setAttribute("indent-number", indent);
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.transform(xmlInput, xmlOutput);
            return xmlOutput.getWriter().toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
