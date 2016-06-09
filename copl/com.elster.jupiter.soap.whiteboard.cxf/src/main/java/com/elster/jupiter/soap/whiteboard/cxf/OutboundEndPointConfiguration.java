package com.elster.jupiter.soap.whiteboard.cxf;

/**
 * Created by bvn on 5/4/16.
 */
public interface OutboundEndPointConfiguration extends EndPointConfiguration {
    void setUsername(String name);

    String getUsername();

    void setPassword(String pass);

    String getPassword();
}
