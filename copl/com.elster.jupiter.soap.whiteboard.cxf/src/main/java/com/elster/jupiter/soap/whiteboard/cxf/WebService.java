package com.elster.jupiter.soap.whiteboard.cxf;

/**
 * Created by bvn on 6/6/16.
 */
public interface WebService {
    public String getName();

    public boolean isInbound();

    public WebServiceProtocol getProtocol();
}
