package com.energyict.protocolimplv2.eict.eiweb;

import com.energyict.mdc.upl.InboundDiscoveryContext;
import com.energyict.mdc.upl.ServletBasedInboundDeviceProtocol;
import com.energyict.mdc.upl.meterdata.CollectedData;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.nls.PropertyTranslationKeys;
import com.energyict.protocol.exception.ConnectionCommunicationException;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.google.common.collect.Range;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

import static com.energyict.protocolimplv2.eict.eiweb.EIWebBulkMessageSeed.DATA_IN_FUTURE_CONFIGURED_DIFFERENCE_X_MAX_Y_FINISHED_WAITING;
import static com.energyict.protocolimplv2.eict.eiweb.EIWebBulkMessageSeed.DATA_IN_FUTURE_CONFIGURED_DIFFERENCE_X_MAX_Y_START_WAITING;
import static com.energyict.protocolimplv2.eict.eiweb.EIWebBulkMessageSeed.DATA_IN_FUTURE_DIFFERENCE_X_NO_WAIT_TIME_CONFIGURED_REJECTING_MESSAGE;
import static com.energyict.protocolimplv2.eict.eiweb.EIWebBulkMessageSeed.DATA_IN_FUTURE_WHICH_DIFFERENCE_X_EXCEED_MAXIMUM_CONFIGURED_Y;
import static com.energyict.protocolimplv2.eict.eiweb.EIWebBulkMessageSeed.MAX_CONFIGURED_WAIT_TIME_X_EXCEEDS_THE_MAXIMUM_ALLOWED_Y;
import static java.text.MessageFormat.format;

/**
 * Provides an implementation for the {@link ServletBasedInboundDeviceProtocol} interface
 * that will support devices that do inbound communication using the "EIWebBulk" protocol.
 * Large parts of the class were copied from the <code>com.energyict.eiwebbulk.EIWebBulk</code> class.
 *
 * @author Rudi Vankeirsbilck (rvk)
 * @since 2012-10-12 (10:27:03)
 */
public class EIWebBulk implements ServletBasedInboundDeviceProtocol {

    private static final String MAX_IDLE_TIME = "maxIdleTime";
    private static final BigDecimal MAX_IDLE_TIME_DEFAULT_VALUE = BigDecimal.valueOf(200000);
    private static final Duration MAX_ALLOWED_WAIT_TIME = Duration.ofSeconds(30);

    private final PropertySpecService propertySpecService;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private InboundDiscoveryContext context;
    private ProtocolHandler protocolHandler;
    private ResponseWriter responseWriter;

    private Duration maxWaitTime;

    public EIWebBulk(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Override
    public void initializeDiscoveryContext(InboundDiscoveryContext context) {
        this.context = context;
    }

    @Override
    public InboundDiscoveryContext getContext() {
        return this.context;
    }

    @Override
    public void init(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }

    @Override
    public String getVersion() {
        return "2023-04-06";
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> specs = new ArrayList<>();
        specs.add(UPLPropertySpecFactory
                        .specBuilder(MAX_IDLE_TIME, false, PropertyTranslationKeys.V2_EICT_MAX_IDLE_TIME, this.propertySpecService::bigDecimalSpec)
                        .setDefaultValue(MAX_IDLE_TIME_DEFAULT_VALUE)
                        .finish());
        return specs;
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        // Properties are not used in this class.
        // Note that the maxIdleTime property is used while setting up the Jetty servlet.
    }

    @Override
    public DiscoverResultType doDiscovery() {
        this.response.setContentType("text/html");
        try {
            this.responseWriter = new ResponseWriter(this.response);
            this.protocolHandler = new ProtocolHandler(this.responseWriter, this.context);
            try {
                this.protocolHandler.handle(this.request, this.context.getLogger());
                if (containsLoadProfileDataWithFutureTimeStamp()) {
                    // The device pushes data in the future, which is dropped by Connexo,
                    // We allow the http connection to stay open for a limited amount of time in order to process the data
                    // When the wait time is not configured or exceeds the configured maximum, throw an Exception
                    // in order to respond with a failure to the device, telling that we are rejecting the data.
                    waitIfConfiguredAndWaitTimeIsWithingBoundaryOrThrowException();
                }
            } catch (RuntimeException e) {
                this.responseWriter.failure();
                throw e;

            }
        } catch (IOException e) {
            throw ConnectionCommunicationException.unexpectedIOException(e);
        }
        return DiscoverResultType.DATA;
    }

    private boolean containsLoadProfileDataWithFutureTimeStamp() {
        Optional<Instant> maxEndTime = getMaxEndTimeFromCollectedLoadProfile();
        return maxEndTime.isPresent() && maxEndTime.get().isAfter(Instant.now());
    }

    private void waitIfConfiguredAndWaitTimeIsWithingBoundaryOrThrowException() throws RuntimeException {
        Optional<Duration> maxWaitTime = getMaxWaitTimeValueFromDeviceProtocol();
        long waitTimeInMs = getMaxEndTimeFromCollectedLoadProfile().get().toEpochMilli() - Instant.now().toEpochMilli();
        if (!maxWaitTime.isPresent()) {
            reject(format(DATA_IN_FUTURE_DIFFERENCE_X_NO_WAIT_TIME_CONFIGURED_REJECTING_MESSAGE, waitTimeInMs));
        }
        long maxWaitTimeInMs = maxWaitTime.get().toMillis();
        if (maxWaitTimeInMs > MAX_ALLOWED_WAIT_TIME.toMillis()) {
            reject(format(MAX_CONFIGURED_WAIT_TIME_X_EXCEEDS_THE_MAXIMUM_ALLOWED_Y, maxWaitTimeInMs, MAX_ALLOWED_WAIT_TIME.toMillis()));
        }
        if (waitTimeInMs > maxWaitTimeInMs) {
            reject(format(DATA_IN_FUTURE_WHICH_DIFFERENCE_X_EXCEED_MAXIMUM_CONFIGURED_Y, waitTimeInMs, maxWaitTimeInMs));
        }
        logInfo(format(DATA_IN_FUTURE_CONFIGURED_DIFFERENCE_X_MAX_Y_START_WAITING, waitTimeInMs, maxWaitTimeInMs));
        sleep(waitTimeInMs);
        logInfo(format(DATA_IN_FUTURE_CONFIGURED_DIFFERENCE_X_MAX_Y_FINISHED_WAITING, waitTimeInMs, maxWaitTimeInMs));
    }

    private void sleep(long millisToWait) {
        try {
            Thread.sleep(millisToWait);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void reject(String message) {
        throw new RuntimeException(message);
    }

    private void logInfo(String message) {
        context.getLogger().log(Level.INFO, message);
    }

    private Optional<Duration> getMaxWaitTimeValueFromDeviceProtocol() {
        TypedProperties properties = context.getInboundDAO().getDeviceProtocolProperties(getDeviceIdentifier());
        return Optional.ofNullable(properties.getTypedProperty(EIWeb.MAX_WAIT_TIME));
    }

    private Optional<Instant> getMaxEndTimeFromCollectedLoadProfile() {
        return protocolHandler.getCollectedData().stream()
                        .filter(CollectedLoadProfile.class::isInstance)
                        .map(CollectedLoadProfile.class::cast)
                        .map(CollectedLoadProfile::getCollectedIntervalDataRange)
                        .map(Range::upperEndpoint).findFirst();
    }

    @Override
    public void provideResponse(DiscoverResponseType responseType) {
        if (!this.response.isCommitted()) {
            switch (responseType) {
                case SUCCESS:
                    this.responseWriter.success();
                    break;
                case DATA_ONLY_PARTIALLY_HANDLED:
                case FAILURE:
                case STORING_FAILURE:
                case DEVICE_DOES_NOT_EXPECT_INBOUND:
                case ENCRYPTION_REQUIRED:
                case DEVICE_NOT_FOUND:
                case DUPLICATE_DEVICE:
                case SERVER_BUSY:
                    this.responseWriter.failure();
                    break;
            }
        }
    }

    @Override
    public DeviceIdentifier getDeviceIdentifier() {
        return this.protocolHandler.getDeviceIdentifier();
    }

    @Override
    public String getAdditionalInformation() {
        return protocolHandler.getAdditionalInfo();
    }

    @Override
    public List<CollectedData> getCollectedData() {
        return this.protocolHandler.getCollectedData();
    }

    @Override
    public boolean hasSupportForRequestsOnInbound() {
        return false;
    }

}