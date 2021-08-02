package com.energyict.protocolimplv2.umi.properties;

import com.energyict.protocolimplv2.umi.security.AppPacketSecurity;
import com.energyict.protocolimplv2.umi.security.SecurityScheme;
import com.energyict.protocolimplv2.umi.types.UmiId;

public interface UmiSessionProperties extends CommunicationSessionProperties {
    SecurityScheme getCmdSignatureScheme();

    SecurityScheme getEncryptionScheme();

    void setEncryptionScheme(SecurityScheme scheme);

    SecurityScheme getRespSignatureSchemeRequest();

    short getSourceOptions();

    short getDestinationOptions();

    UmiId getSourceUmiId();

    UmiId getDestinationUmiId();

    void setDestinationUmiId(UmiId id);

    int getTransactionNumber();

    AppPacketSecurity getSecurity();

    void resetTransactionNumber();

    void incrementTransactionNumber();

    void setEstablished(boolean established);

    boolean isEstablished();
}
