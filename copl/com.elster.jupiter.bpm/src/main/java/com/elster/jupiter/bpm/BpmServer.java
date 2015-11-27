package com.elster.jupiter.bpm;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 23/09/2014
 * Time: 10:25
 */
public interface BpmServer {

    String getUrl();

    long doPost(String resourceURL, String payload);

    String doGet(String resourceURL);
}
