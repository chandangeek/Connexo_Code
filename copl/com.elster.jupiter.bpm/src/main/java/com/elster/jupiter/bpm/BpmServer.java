package com.elster.jupiter.bpm;

import java.io.IOException;
import java.util.Optional;

/**
 * Copyrights EnergyICT
 * Date: 23/09/2014
 * Time: 10:25
 */
public interface BpmServer {

    String getUrl();

    Optional<String> doPost(String resourceURL, String payload);

    Optional<String> doPost(String resourceURL, String payload, String authorization);

    String doGet(String resourceURL);

    String doGet(String resourceURL, String authorization);
}
