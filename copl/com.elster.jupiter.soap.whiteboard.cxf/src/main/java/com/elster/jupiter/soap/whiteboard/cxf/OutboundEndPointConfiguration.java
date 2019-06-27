/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf;

/**
 * Created by bvn on 5/4/16.
 */
public interface OutboundEndPointConfiguration extends EndPointConfiguration {
    void setUsername(String name);

    String getUsername();

    void setPassword(String pass);

    String getPassword();

    default void retryOccurrence(String method, String payload){
        System.out.println("Default implemetation");
    }
}
