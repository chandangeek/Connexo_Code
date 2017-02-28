package com.energyict.mdc.upl;

import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.crypto.KeyStoreService;
import com.energyict.mdc.upl.crypto.X509Service;
import com.energyict.mdc.upl.io.ConnectionType;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.legacy.CertificateAliasFinder;
import com.energyict.mdc.upl.messages.legacy.CertificateWrapperExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.mdc.upl.security.SecurityProperty;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Holds contextual information for an {@link InboundDeviceProtocol}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-04 (08:55)
 */
public interface InboundDiscoveryContext {

    Logger getLogger();

    void setLogger(Logger logger);

    ComChannel getComChannel();

    /**
     * Tests if the discovery protocol indicated that it encountered
     * a problem in the communication with the actual device
     * because the latter required that the information be encrypted.
     * In that case, the session should probably be aborted and
     * {@link com.energyict.mdc.upl.InboundDeviceProtocol.DiscoverResponseType#ENCRYPTION_REQUIRED}
     * should be returned as a discovery response.
     *
     * @return A flag that indicates that encryption was required
     */
    boolean encryptionRequired();

    /**
     * Notification from the discovery protocol to indicate
     * that it encountered a problem in the communication with
     * the actual device because the latter required that the
     * communiation be encrypted.
     *
     * @see #encryptionRequired()
     */
    void markEncryptionRequired();

    CollectedDataFactory getCollectedDataFactory();

    ObjectMapperService getObjectMapperService();

    IssueFactory getIssueFactory();

    PropertySpecService getPropertySpecService();

    NlsService getNlsService();

    Converter getConverter();

    InboundDAO getInboundDAO();

    HttpServletRequest getServletRequest();

    void setServletRequest(HttpServletRequest servletRequest);

    HttpServletResponse getServletResponse();

    void setServletResponse(HttpServletResponse servletResponse);

    DeviceGroupExtractor getDeviceGroupExtractor();

    DeviceMasterDataExtractor getDeviceMasterDataExtractor();

    DeviceExtractor getDeviceExtractor();

    DeviceMessageFileExtractor getMessageFileExtractor();

    KeyStoreService getKeyStoreService();

    CertificateWrapperExtractor getCertificateWrapperExtractor();

    CertificateAliasFinder getCertificateAliasFinder();

    X509Service getX509Service();

    /**
     * Gets the {@link TypedProperties} of the {@link ConnectionType}
     * that is currently in use in this context.
     *
     * @param deviceIdentifier The object that uniquely identifies the Device
     * @return The TypedProperties or <code>Optional.empty()</code> if the Device is not ready for inbound communication
     */
    Optional<TypedProperties> getConnectionTypeProperties(DeviceIdentifier deviceIdentifier);

    /**
     * Gets the {@link SecurityProperty security properties} of the
     * Device that is currently communicating in this context.
     *
     * @param deviceIdentifier The object that uniquely identifies the Device
     * @return The List of SecurityProperty or <code>Optional.empty()</code> if the Device is not ready for inbound communication
     */
    Optional<List<? extends SecurityProperty>> getProtocolSecurityProperties(DeviceIdentifier deviceIdentifier);

    /**
     * Returns the dialect properties for the
     * Device that is currently communicating in this context.
     */
    Optional<TypedProperties> getDeviceDialectProperties(DeviceIdentifier deviceIdentifier);

    /**
     * Tests if inbound communication is on hold for the.
     * Device that is currently communicating in this context.
     *
     * @param deviceIdentifier The object that uniquely identifies the Device
     * @return the onHold property value or <code>Optional.empty()</code> if the device does not provide the required information to property answer the question
     */
    Optional<Boolean> isInboundOnHold(DeviceIdentifier deviceIdentifier);

}