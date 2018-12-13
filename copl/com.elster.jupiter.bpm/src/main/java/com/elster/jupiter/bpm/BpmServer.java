/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface BpmServer {

    String getUrl();

    long doPost(String resourceURL, String payload);

    long doPost(String resourceURL, String payload, String authorization);

    String doPost(String resourceURL, String payload, long version);

    String doPost(String resourceURL, String payload, String authorization, long version);

    String doGet(String resourceURL);

    String doGet(String resourceURL, String authorization);

    ProcessInstanceInfos getRunningProcesses(String authorization, String filter);
}
