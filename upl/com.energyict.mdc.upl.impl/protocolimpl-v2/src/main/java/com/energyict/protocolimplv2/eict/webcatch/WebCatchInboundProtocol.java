package com.energyict.protocolimplv2.eict.webcatch;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.mdc.identifiers.DeviceIdentifierBySerialNumber;
import com.energyict.mdc.identifiers.LoadProfileIdentifierFirstOnDevice;
import com.energyict.mdc.upl.InboundDiscoveryContext;
import com.energyict.mdc.upl.ServletBasedInboundDeviceProtocol;
import com.energyict.mdc.upl.meterdata.CollectedData;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.nls.PropertyTranslationKeys;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.exception.ConnectionCommunicationException;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.eict.webcatch.model.CatchallObject;
import com.energyict.protocolimplv2.eict.webcatch.model.WebcatchChannel;
import com.energyict.protocolimplv2.eict.webcatch.model.WebcatchDevice;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.energyict.mdc.upl.tasks.support.DeviceLoadProfileSupport.GENERIC_LOAD_PROFILE_OBISCODE;

public class WebCatchInboundProtocol implements ServletBasedInboundDeviceProtocol {

    private static final String MAX_IDLE_TIME = "maxIdleTime";
    private static final BigDecimal MAX_IDLE_TIME_DEFAULT_VALUE = BigDecimal.valueOf(200000);

    private final PropertySpecService propertySpecService;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private InboundDiscoveryContext context;
    private ResponseWriter responseWriter;
    private CatchallObject catchallObject;

    public WebCatchInboundProtocol(PropertySpecService propertySpecService) {
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
        return "$Date: 2022-08-11 $";
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Collections.singletonList(
             UPLPropertySpecFactory
                        .specBuilder(MAX_IDLE_TIME, false, PropertyTranslationKeys.V2_EICT_MAX_IDLE_TIME, this.propertySpecService::bigDecimalSpec)
                        .setDefaultValue(MAX_IDLE_TIME_DEFAULT_VALUE)
                        .finish());
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        // Properties are not used in this class.
        // Note that the maxIdleTime property is used while setting up the Jetty servlet.
    }

    @Override
    public DiscoverResultType doDiscovery() {
        try {
            responseWriter = new ResponseWriter(this.response);
            catchallObject = new ObjectMapper().readValue(request.getInputStream(),CatchallObject.class);
        } catch (JsonParseException | JsonMappingException e) {
            this.responseWriter.clientSideValidationException(e.getMessage());
            throw ConnectionCommunicationException.unexpectedIOException(e);
        } catch (IOException e) {
            this.responseWriter.failure();
            throw ConnectionCommunicationException.unexpectedIOException(e);
        }

        return DiscoverResultType.DATA;
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
        return new DeviceIdentifierBySerialNumber(catchallObject.getSerial());
    }

    @Override
    public String getAdditionalInformation() {
        return "";
    }

    @Override
    public List<CollectedData> getCollectedData() {
        LoadProfileIdentifier loadProfileIdentifier = new LoadProfileIdentifierFirstOnDevice(getDeviceIdentifier(), GENERIC_LOAD_PROFILE_OBISCODE);
        CollectedLoadProfile loadProfile = getContext().getCollectedDataFactory().createCollectedLoadProfile(loadProfileIdentifier);

        ProfileData profileData = new ProfileData();
        IntervalData intervalData = new IntervalData(Date.from(Instant.ofEpochSecond(Long.parseLong(catchallObject.getUtcstamp()))));

        int channelIndex = 0;
        for (WebcatchDevice webcatchDevice : catchallObject.getDevices().stream().sorted().collect(Collectors.toList())) {
            for (WebcatchChannel webcatchChannel : webcatchDevice.getValues()) {
                profileData.addChannel(buildChannelInfo(channelIndex++));
                intervalData.addValue(webcatchChannel.getValue());
            }
        }
        profileData.addInterval(intervalData);
        loadProfile.setCollectedIntervalData(profileData.getIntervalDatas(), profileData.getChannelInfos());
        loadProfile.setDoStoreOlderValues(profileData.shouldStoreOlderValues());
        loadProfile.setAllowIncompleteLoadProfileData(true);
        return Collections.singletonList(loadProfile);
    }

    private ChannelInfo buildChannelInfo(int channelIndex) {
        return new ChannelInfo(channelIndex, channelIndex, this.buildChannelName(channelIndex), Unit.get(BaseUnit.COUNT));
    }

    private String buildChannelName(int channelId) {
        // Remember that obis codes use 1-based indexing and channelId is using zero-based indexing.
        return "0." + (channelId + 1) + ".128.0.0.255";
    }


    @Override
    public boolean hasSupportForRequestsOnInbound() {
        return false;
    }

}