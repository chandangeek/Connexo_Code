package com.elster.jupiter.soap.whiteboard.cxf;

import javax.xml.ws.Service;
import javax.xml.ws.WebServiceFeature;
import java.net.URL;
import java.util.List;

/**
 * Created by bvn on 5/9/16.
 */
public interface OutboundEndPointProvider extends EndPointProvider {
    Service get(URL wsdlUrl, List<WebServiceFeature> webServiceFeatures);

    Class getService();
}
