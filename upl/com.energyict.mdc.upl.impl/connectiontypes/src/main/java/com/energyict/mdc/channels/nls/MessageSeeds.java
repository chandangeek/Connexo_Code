package com.energyict.mdc.channels.nls;

import com.energyict.mdc.upl.nls.MessageSeed;
import com.energyict.mdc.upl.nls.TranslationKey;

import java.util.logging.Level;

/**
 * Contains all the {@link TranslationKey}s of the I/O errors that are produces by the connection types.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-09 (11:45)
 */
public enum MessageSeeds implements MessageSeed {
    FailedToSetupTLSConnection("upl.error.failedToSetupTLSConnection", "Failed to setup the TLS connection."),
    FailedToSetupKeyManager("upl.error.failedToSetupKeyManager", "Failed to setup a Key Manager, TLS connection will not be setup."),
    FailedToSetupTrustManager("upl.error.failedToSetupTrustManager", "Failed to setup a Trust Manager, TLS connection will not be setup."),
    DefaultKeyManagerNotFound("upl.error.defaultKeyManagerNotFound", "A default key manager could not be found, TLS Connection will not be established."),
    DefaultTrustManagerNotFound("upl.error.defaultTrustManagerNotFound", "A default trust manager could not be found, TLS Connection will not be established."),
    NotSupportedOnClient("upl.error.notSupportedOnClient", "Method not supported on client side"),
    ServerNotTrusted("upl.error.serverNotTrusted", "Based on provided certificate chain and authentication type, the server cannot be trusted"),
    NestedIOException("upl.error.nestedIOException", "Nested I/O Error"),
    NestedModemException("upl.error.nestedModemException", "Nested modem error"),
    PreferredCipherSuiteIsNotSupportedByJavaVersion("upl.error.preferredCipherSuiteIsNotSupportedByJavaVersion", "The preferred cipher suite '{0}' is not supported by your current java version."),
    WavenisStackSetupError("upl.error.wavenisStackSetupError", "Error while starting the Wavenis stack"),

    AT_MODEM_BUSY("upl.error.at.modem.busy", "Receiver was currently busy, modem on COM port {0} returned BUSY command, last command send [{1}]"),
    AT_MODEM_ERROR("upl.error.at.modem.error", "Most likely an invalid command has been sent, modem on COM port {0} returned ERROR command, last command send [{1}]"),
    AT_MODEM_NO_ANSWER("upl.error.at.modem.no.answer", "Receiver was not reachable, modem on COM port {0} returned NO_ANSWER command, last command send [{1}]"),
    AT_MODEM_NO_CARRIER("upl.error.at.modem.no.carrier", "Receiver was not reachable, modem on COM port {0} returned NO_CARRIER command, last command send [{1}]"),
    AT_MODEM_NO_DIALTONE("upl.error.at.modem.no.dial.tone", "Could not dial with modem on COM port {0}, a NO_DIALTONE command was returned, last command send [{1}]");

    private final String key;
    private final String defaultTranslation;
    private final Level level;

    MessageSeeds(String key, String defaultTranslation) {
        this(key, defaultTranslation, Level.SEVERE);
    }

    MessageSeeds(String key, String defaultTranslation, Level level) {
        this.key = key;
        this.defaultTranslation = defaultTranslation;
        this.level = level;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getDefaultFormat() {
        return this.defaultTranslation;
    }

    @Override
    public int getId() {
        return this.ordinal();
    }

    @Override
    public Level getLevel() {
        return this.level;
    }
}