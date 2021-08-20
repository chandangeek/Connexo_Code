package com.energyict.protocolimplv2.umi.properties;

public interface CommunicationSessionProperties {
    /**
     * The timeout interval of the communication session, expressed in milliseconds
     */
    long getTimeout();

    /**
     * The delay before sending the requests, expressed in milliseconds
     */
    long getForcedDelay();
}
