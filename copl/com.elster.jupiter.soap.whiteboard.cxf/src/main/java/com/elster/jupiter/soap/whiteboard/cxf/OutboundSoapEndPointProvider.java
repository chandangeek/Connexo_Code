package com.elster.jupiter.soap.whiteboard.cxf;

import javax.xml.ws.Service;

/**
 * Created by bvn on 5/9/16.
 */
public interface OutboundSoapEndPointProvider extends EndPointProvider {
    Service get();

    Class getService();
}
