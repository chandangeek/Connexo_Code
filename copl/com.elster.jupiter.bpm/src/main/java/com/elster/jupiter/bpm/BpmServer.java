package com.elster.jupiter.bpm;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 23/09/2014
 * Time: 10:25
 */
public interface BpmServer {

    String getUrl();

    void doPost(String resourceURL);

    String doGet(String resourceURL);
}
