package com.energyict.protocolimplv2.umi.properties;

import com.energyict.protocolimplv2.umi.security.AppPacketSecurity;
import com.energyict.protocolimplv2.umi.security.SecurityScheme;
import com.energyict.protocolimplv2.umi.security.scheme0.AppPacketSecurityS0;
import com.energyict.protocolimplv2.umi.types.UmiId;
import com.energyict.protocolimplv2.umi.util.Limits;
import com.energyict.mdc.upl.TypedProperties;

import java.time.Duration;

public class UmiSessionPropertiesS0 implements UmiSessionProperties {
    public static final Duration DEFAULT_TIMEOUT = Duration.ofMillis(120);
    public static final Duration DEFAULT_FORCED_DELAY = Duration.ofMillis(0);

    private final SecurityScheme    cmdSignatureScheme;
    private SecurityScheme    encryptionScheme;
    private final SecurityScheme    respSignatureSchemeRequest;
    private final short             sourceOptions;
    private final short             destinationOptions;
    private final UmiId             sourceUmiId;
    private       UmiId             destinationUmiId;
    private int                     transactionNumber;

    private final TypedProperties   typedProperties;
    private AppPacketSecurity       security = new AppPacketSecurityS0();

    private boolean established = false;

    public UmiSessionPropertiesS0(UmiPropertiesBuilder builder) {
        cmdSignatureScheme = builder.getCmdSignatureScheme();
        encryptionScheme = builder.getEncryptionScheme();
        respSignatureSchemeRequest = builder.getRespSignatureSchemeRequest();
        sourceOptions = builder.getSourceOptions();
        destinationOptions = builder.getDestinationOptions();
        sourceUmiId = builder.getSourceUmiId();
        destinationUmiId = builder.getDestinationUmiId();
        transactionNumber = 0;
        typedProperties = TypedProperties.empty();
        //this.security = createSecurity();
    }

    @Override
    public long getTimeout() {
        return typedProperties.getTypedProperty("Timeout", DEFAULT_TIMEOUT).toMillis();
    }

    @Override
    public long getForcedDelay() {
        return typedProperties.getTypedProperty("ForcedDelay", DEFAULT_FORCED_DELAY).toMillis();
    }

    @Override
    public SecurityScheme getCmdSignatureScheme() {
        return cmdSignatureScheme;
    }

    @Override
    public SecurityScheme getEncryptionScheme() {
        return encryptionScheme;
    }

    @Override
    public void setEncryptionScheme(SecurityScheme scheme) {
        this.encryptionScheme = scheme;
    }

    @Override
    public SecurityScheme getRespSignatureSchemeRequest() {
        return respSignatureSchemeRequest;
    }

    @Override
    public short getSourceOptions() {
        return sourceOptions;
    }

    @Override
    public short getDestinationOptions() {
        return destinationOptions;
    }

    @Override
    public UmiId getSourceUmiId() {
        return sourceUmiId;
    }

    @Override
    public UmiId getDestinationUmiId() {
        return destinationUmiId;
    }

    public void setDestinationUmiId(UmiId id) {
        this.destinationUmiId = id;
    }

    @Override
    public int getTransactionNumber() {
        return transactionNumber;
    }

    @Override
    public AppPacketSecurity getSecurity() { return security; }

    @Override
    public void resetTransactionNumber() {
        transactionNumber = 0;
    }

    @Override
    public void incrementTransactionNumber() {
        transactionNumber++;
        if (transactionNumber > Limits.MAX_UNSIGNED_SHORT)
            transactionNumber = 0;
    }

    TypedProperties getTypedProperties() {
        return typedProperties;
    }

    @Override
    public void setEstablished(boolean established) {
        this.established = established;
    }

    @Override
    public boolean isEstablished() {
        return established;
    }

    protected AppPacketSecurity createSecurity() { return new AppPacketSecurityS0(); }
}
