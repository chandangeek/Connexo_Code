package com.elster.jupiter.bpm;

import aQute.bnd.annotation.ProviderType;

import java.io.IOException;
import java.util.Optional;

/**
 * Copyrights EnergyICT
 * Date: 23/09/2014
 * Time: 10:25
 */
@ProviderType
public interface BpmServer {

    String getUrl();

    long doPost(String resourceURL, String payload);

    long doPost(String resourceURL, String payload, String authorization);

    String doPost(String resourceURL, String payload, long version);

    String doPost(String resourceURL, String payload, String authorization, long version);

    String doGet(String resourceURL);

    String doGet(String resourceURL, String authorization);
}
