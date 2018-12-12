package com.energyict.dlms;

import com.energyict.dlms.aso.ConformanceBlock;
import com.energyict.dlms.aso.SecurityProvider;
import com.energyict.mdc.upl.properties.TypedProperties;

/**
 * Copyrights EnergyICT
 * Date: 8/02/12
 * Time: 15:08
 */
public interface DlmsSessionProperties {

    DLMSReference getReference();

    ConnectionMode getConnectionMode();

    String getSecurityLevel();

    int getAuthenticationSecurityLevel();

    int getDataTransportSecurityLevel();

    int getClientMacAddress();

    String getServerMacAddress();

    int getUpperHDLCAddress();

    int getLowerHDLCAddress();

    int getDestinationWPortNumber();

    int getAddressingMode();

    String getManufacturer();

    int getInformationFieldSize();

    boolean isWakeUp();

    int getIpPortNumber();

    CipheringType getCipheringType();

    boolean isNtaSimulationTool();

    boolean isBulkRequest();

    long getConformance();

    ConformanceBlock getConformanceBlock();

    boolean isSNReference();

    boolean isLNReference();

    byte[] getSystemIdentifier();

    InvokeIdAndPriorityHandler getInvokeIdAndPriorityHandler();

    int getMaxRecPDUSize();

    int getProposedDLMSVersion();

    int getProposedQOS();

    boolean isRequestTimeZone();

    int getRoundTripCorrection();

    int getIskraWrapper();

    SecurityProvider getSecurityProvider();

    void setSecurityProvider(SecurityProvider securityProvider);

    String getPassword();

    String getDeviceId();

    String getNodeAddress();

    String getSerialNumber();

    int getTimeout();

    int getRetries();

    int getForcedDelay();

    int getDelayAfterError();

    TypedProperties getProtocolProperties();

    int getDeviceBufferSize();

    boolean incrementFrameCounterForRetries();

    /**
     * Indicates whether or not to increment the frame counter when creating the action request for the reply_to_hls.
     *
     * @return	<code>true</code> if the frame counter needs to be bumped after generation of the f(StoC), <code>false</code> otherwise. Default is <code>false</code> for
     * 			backwards compatibility.
     */
    boolean incrementFrameCounterForReplyToHLS();

}
